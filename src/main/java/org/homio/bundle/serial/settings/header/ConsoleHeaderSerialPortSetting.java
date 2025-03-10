package org.homio.bundle.serial.settings.header;

import com.fazecast.jSerialComm.SerialPort;
import org.homio.api.model.Icon;
import org.homio.api.setting.SettingPluginOptionsPort;
import org.homio.api.setting.console.header.ConsoleHeaderSettingPlugin;

public class ConsoleHeaderSerialPortSetting implements ConsoleHeaderSettingPlugin<SerialPort>, SettingPluginOptionsPort {

  @Override
  public Icon getIcon() {
    return new Icon("fas fa-project-diagram");
  }

  @Override
  public boolean isRequired() {
    return true;
  }
}
