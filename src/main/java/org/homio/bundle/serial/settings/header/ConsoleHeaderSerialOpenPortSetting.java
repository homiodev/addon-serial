package org.homio.bundle.serial.settings.header;

import org.homio.api.model.Icon;
import org.homio.api.setting.SettingPluginToggle;
import org.homio.api.setting.console.header.ConsoleHeaderSettingPlugin;
import org.jetbrains.annotations.NotNull;

public class ConsoleHeaderSerialOpenPortSetting implements ConsoleHeaderSettingPlugin<Boolean>, SettingPluginToggle {

  @Override
  public @NotNull Icon getIcon() {
    return new Icon("fas fa-door-open");
  }

  @Override
  public @NotNull Icon getToggleIcon() {
    return new Icon("fas fa-door-closed");
  }
}
