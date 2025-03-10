package org.homio.bundle.serial;

import com.fazecast.jSerialComm.SerialPort;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.homio.api.Context;
import org.homio.api.console.ConsolePluginCommunicator;
import org.homio.api.exception.ServerException;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.setting.console.header.ConsoleHeaderSettingPlugin;
import org.homio.api.util.FlowMap;
import org.homio.bundle.serial.settings.header.ConsoleHeaderSerialOpenPortSetting;
import org.homio.bundle.serial.settings.header.ConsoleHeaderSerialPortBaudRateSetting;
import org.homio.bundle.serial.settings.header.ConsoleHeaderSerialPortFlowControlSetting;
import org.homio.bundle.serial.settings.header.ConsoleHeaderSerialPortSendEndLine;
import org.homio.bundle.serial.settings.header.ConsoleHeaderSerialPortSetting;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.homio.api.ui.UI.Color.PRIMARY_COLOR;

@Component
@RequiredArgsConstructor
public class SerialPortConsolePlugin implements ConsolePluginCommunicator {

  @Getter
  private final Context context;
  private RawSerialPortCommunicator rawSerialPortCommunicator;
  private ConsoleHeaderSerialPortSendEndLine.EndLineType endLineType;

  @Override
  public Collection<ComplexString> getComplexValue() {
    return rawSerialPortCommunicator == null ? null : rawSerialPortCommunicator.getValues();
  }

  @SneakyThrows
  public void init() {
    context.setting().listenValue(ConsoleHeaderSerialPortBaudRateSetting.class, "serial-baud-rate", () -> {
      if (this.rawSerialPortCommunicator != null) {
        this.reopen(true);
      }
    });
    context.setting().listenValue(ConsoleHeaderSerialPortFlowControlSetting.class, "serial-flow-control", () -> {
      if (this.rawSerialPortCommunicator != null) {
        this.reopen(true);
      }
    });
    context.setting().listenValueAndGet(ConsoleHeaderSerialPortSendEndLine.class, "serial-end-line", endLineType -> this.endLineType = endLineType);
    context.setting().listenValue(ConsoleHeaderSerialOpenPortSetting.class, "serial-open-port", this::reopen);
  }

  private void reopen(Boolean open) {
    if (this.rawSerialPortCommunicator != null) {
      String portName = this.rawSerialPortCommunicator.getSerialPort() == null ? "-" :
        this.rawSerialPortCommunicator.getSerialPort().getSystemPortName();
      this.rawSerialPortCommunicator.close();
      context.ui().toastr().info("serial.port_closed", FlowMap.of("PORT", portName));
      this.rawSerialPortCommunicator = null;
    }

    if (open) {
      SerialPort commPort = context.setting().getValue(ConsoleHeaderSerialPortSetting.class);
      if (commPort == null) {
        context.setting().setValue(ConsoleHeaderSerialOpenPortSetting.class, false);
        throw new ServerException("serial.no_port", FlowMap.of("PORT",
          defaultIfEmpty(context.setting().getRawValue(ConsoleHeaderSerialPortSetting.class), "-")));
      }
      try {
        this.openPort(commPort);
      } catch (Exception ex) {
        rawSerialPortCommunicator = null;
        context.setting().setValue(ConsoleHeaderSerialOpenPortSetting.class, false);
        throw new ServerException("serial.unable_open", FlowMap.of("PORT", commPort.getSystemPortName()));
      }
    }
  }

  private void openPort(SerialPort commPort) {
    Integer baudRate = context.setting().getValue(ConsoleHeaderSerialPortBaudRateSetting.class);
    ConsoleHeaderSerialPortFlowControlSetting.FlowControl flowControl = context.setting().getValue(ConsoleHeaderSerialPortFlowControlSetting.class);

    this.rawSerialPortCommunicator = new RawSerialPortCommunicator(commPort, context, this);
    boolean opened = this.rawSerialPortCommunicator.open(baudRate, flowControl.getPortFlowControl());
    if (!opened) {
      throw new RuntimeException("Unable open port");
    } else {
      context.ui().toastr().success("serial.open_success", FlowMap.of("PORT", commPort.getSystemPortName()));
    }
  }

  @Override
  public @NotNull Context context() {
    return context;
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
      SerialPort commPort = context.setting().getValue(ConsoleHeaderSerialPortSetting.class);
      return ActionResponseModel.showError("serial.no_open_port", "PORT", commPort == null ? "-" : commPort.getSystemPortName());
    }
    return null;
  }

  @Override
  public void dataReceived(ComplexString data) {
    context.ui().sendDynamicUpdate("-lines-serial", data.toString());
  }
}
