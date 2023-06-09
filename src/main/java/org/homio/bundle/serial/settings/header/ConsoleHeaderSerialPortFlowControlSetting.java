package org.homio.bundle.serial.settings.header;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.homio.bundle.api.port.PortFlowControl;
import org.homio.bundle.api.setting.SettingPluginOptionsEnum;
import org.homio.bundle.api.setting.console.header.ConsoleHeaderSettingPlugin;

public class ConsoleHeaderSerialPortFlowControlSetting implements SettingPluginOptionsEnum<ConsoleHeaderSerialPortFlowControlSetting.FlowControl>,
    ConsoleHeaderSettingPlugin<ConsoleHeaderSerialPortFlowControlSetting.FlowControl> {

  @Override
  public String getIcon() {
    return "fas fa-wind";
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
