package org.homio.bundle.serial.settings.header;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.homio.api.model.Icon;
import org.homio.api.port.PortFlowControl;
import org.homio.api.setting.SettingPluginOptionsEnum;
import org.homio.api.setting.console.header.ConsoleHeaderSettingPlugin;

public class ConsoleHeaderSerialPortFlowControlSetting implements SettingPluginOptionsEnum<ConsoleHeaderSerialPortFlowControlSetting.FlowControl>,
  ConsoleHeaderSettingPlugin<ConsoleHeaderSerialPortFlowControlSetting.FlowControl> {

  @Override
  public Icon getIcon() {
    return new Icon("fas fa-wind");
  }

  @Override
  public Integer getMaxWidth() {
    return 110;
  }

  @Override
  public Class<FlowControl> getType() {
    return FlowControl.class;
  }

  @RequiredArgsConstructor
  public enum FlowControl {
    NONE(PortFlowControl.FLOWCONTROL_OUT_NONE),
    XONOFF(PortFlowControl.FLOWCONTROL_OUT_XONOFF),
    RTSCTS(PortFlowControl.FLOWCONTROL_OUT_RTSCTS);

    @Getter
    private final PortFlowControl portFlowControl;
  }
}
