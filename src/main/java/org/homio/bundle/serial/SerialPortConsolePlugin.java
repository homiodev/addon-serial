package org.homio.bundle.serial;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.homio.bundle.api.util.Constants.PRIMARY_COLOR;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.console.ConsolePluginCommunicator;
import org.homio.bundle.api.exception.ServerException;
import org.homio.bundle.api.model.ActionResponseModel;
import org.homio.bundle.api.setting.console.header.ConsoleHeaderSettingPlugin;
import org.homio.bundle.api.util.FlowMap;
import org.homio.bundle.serial.settings.header.ConsoleHeaderSerialOpenPortSetting;
import org.homio.bundle.serial.settings.header.ConsoleHeaderSerialPortBaudRateSetting;
import org.homio.bundle.serial.settings.header.ConsoleHeaderSerialPortFlowControlSetting;
import org.homio.bundle.serial.settings.header.ConsoleHeaderSerialPortSendEndLine;
import org.homio.bundle.serial.settings.header.ConsoleHeaderSerialPortSetting;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SerialPortConsolePlugin implements ConsolePluginCommunicator {

  @Getter
  private final EntityContext entityContext;
  private RawSerialPortCommunicator rawSerialPortCommunicator;
  private ConsoleHeaderSerialPortSendEndLine.EndLineType endLineType;

  @Override
  public Collection<ComplexString> getComplexValue() {
    return rawSerialPortCommunicator == null ? null : rawSerialPortCommunicator.getValues();
  }

  @SneakyThrows
  public void init() {
    entityContext.setting().listenValue(ConsoleHeaderSerialPortBaudRateSetting.class, "serial-baud-rate", () -> {
      if (this.rawSerialPortCommunicator != null) {
        this.reopen(true);
      }
    });
    entityContext.setting().listenValue(ConsoleHeaderSerialPortFlowControlSetting.class, "serial-flow-control", () -> {
      if (this.rawSerialPortCommunicator != null) {
        this.reopen(true);
      }
    });
    entityContext.setting().listenValueAndGet(ConsoleHeaderSerialPortSendEndLine.class, "serial-end-line", endLineType -> this.endLineType = endLineType);
    entityContext.setting().listenValue(ConsoleHeaderSerialOpenPortSetting.class, "serial-open-port", this::reopen);
  }

  private void reopen(Boolean open) {
    if (this.rawSerialPortCommunicator != null) {
      String portName = this.rawSerialPortCommunicator.getSerialPort() == null ? "-" :
          this.rawSerialPortCommunicator.getSerialPort().getSystemPortName();
      this.rawSerialPortCommunicator.close();
      entityContext.ui().sendInfoMessage("serial.port_closed", FlowMap.of("PORT", portName));
      this.rawSerialPortCommunicator = null;
    }

    if (open) {
      SerialPort commPort = entityContext.setting().getValue(ConsoleHeaderSerialPortSetting.class);
      if (commPort == null) {
        entityContext.setting().setValue(ConsoleHeaderSerialOpenPortSetting.class, false);
        throw new ServerException("serial.no_port", FlowMap.of("PORT",
            defaultIfEmpty(entityContext.setting().getRawValue(ConsoleHeaderSerialPortSetting.class), "-")));
      }
      try {
        this.openPort(commPort);
      } catch (Exception ex) {
        rawSerialPortCommunicator = null;
        entityContext.setting().setValue(ConsoleHeaderSerialOpenPortSetting.class, false);
        throw new ServerException("serial.unable_open", FlowMap.of("PORT", commPort.getSystemPortName()));
      }
    }
  }

  private void openPort(SerialPort commPort) {
    Integer baudRate = entityContext.setting().getValue(ConsoleHeaderSerialPortBaudRateSetting.class);
    ConsoleHeaderSerialPortFlowControlSetting.FlowControl flowControl = entityContext.setting().getValue(ConsoleHeaderSerialPortFlowControlSetting.class);

    this.rawSerialPortCommunicator = new RawSerialPortCommunicator(commPort, entityContext, this);
    boolean opened = this.rawSerialPortCommunicator.open(baudRate, flowControl.getPortFlowControl());
    if (!opened) {
      throw new RuntimeException("Unable open port");
    } else {
      entityContext.ui().sendSuccessMessage("serial.open_success", FlowMap.of("PORT", commPort.getSystemPortName()));
    }
  }

  @Override
  public Map<String, Class<? extends ConsoleHeaderSettingPlugin<?>>> getHeaderActions() {
    Map<String, Class<? extends ConsoleHeaderSettingPlugin<?>>> headerActions = new LinkedHashMap<>();

    headerActions.put("openPort", ConsoleHeaderSerialOpenPortSetting.class);
    headerActions.put("port", ConsoleHeaderSerialPortSetting.class);
    headerActions.put("baudRate", ConsoleHeaderSerialPortBaudRateSetting.class);
    headerActions.put("flowControl", ConsoleHeaderSerialPortFlowControlSetting.class);
    headerActions.put("endLine", ConsoleHeaderSerialPortSendEndLine.class);

    return headerActions;
  }

  @Override
  @SneakyThrows
  public ActionResponseModel commandReceived(String value) {
    if (rawSerialPortCommunicator != null) {
      ComplexString data = ComplexString.of(value, System.currentTimeMillis(), "#81A986", true);
      try {
        rawSerialPortCommunicator.getOutputStream().write((value + this.endLineType.getValue()).getBytes());
        rawSerialPortCommunicator.getBuffer().add(data);
      } catch (Exception ex) {
        rawSerialPortCommunicator.getBuffer().add(data.setColor(PRIMARY_COLOR));
        throw ex;
      }
    } else {
      SerialPort commPort = entityContext.setting().getValue(ConsoleHeaderSerialPortSetting.class);
      return ActionResponseModel.showError("serial.no_open_port", "PORT", commPort == null ? "-" : commPort.getSystemPortName());
    }
    return null;
  }

  @Override
  public void dataReceived(ComplexString data) {
    entityContext.ui().sendNotification("-lines-serial", data.toString());
  }
}
