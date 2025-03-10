package org.homio.bundle.serial.settings.header;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.homio.api.model.Icon;
import org.homio.api.setting.SettingPluginOptionsEnum;
import org.homio.api.setting.console.header.ConsoleHeaderSettingPlugin;
import org.jetbrains.annotations.NotNull;

public class ConsoleHeaderSerialPortSendEndLine implements ConsoleHeaderSettingPlugin<ConsoleHeaderSerialPortSendEndLine.EndLineType>,
  SettingPluginOptionsEnum<ConsoleHeaderSerialPortSendEndLine.EndLineType> {

  @Override
  public Icon getIcon() {
    return new Icon("fas fa-digital-tachograph");
  }

  @Override
  public Integer getMaxWidth() {
    return 135;
  }

  @Override
  public @NotNull Class<EndLineType> getType() {
    return EndLineType.class;
  }

  @Override
  public boolean transientState() {
    return false;
  }

  @RequiredArgsConstructor
  public enum EndLineType {
    NO_LINE_ENDING(""),
    NEW_LINE("\n"),
    CARRIAGE_RETURN("\r"),
    BOTH_NL_CR("\r\n");

    @Getter
    private final String value;
  }
}
