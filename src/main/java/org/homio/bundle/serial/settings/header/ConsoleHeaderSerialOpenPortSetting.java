package org.homio.bundle.serial.settings.header;

import org.homio.bundle.api.setting.SettingPluginToggle;
import org.homio.bundle.api.setting.console.header.ConsoleHeaderSettingPlugin;

public class ConsoleHeaderSerialOpenPortSetting implements ConsoleHeaderSettingPlugin<Boolean>, SettingPluginToggle {

  @Override
  public String getIcon() {
    return "fas fa-door-open";
  }

  @Override
  public String getToggleIcon() {
    return "fas fa-door-closed";
  }
}
