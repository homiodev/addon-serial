package org.homio.bundle.serial;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.homio.api.AddonConfiguration;
import org.homio.api.AddonEntrypoint;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@AddonConfiguration
@RequiredArgsConstructor
public class SerialEntrypoint implements AddonEntrypoint {

  private final SerialPortConsolePlugin serialPortConsolePlugin;

  public void init() {
    serialPortConsolePlugin.init();
  }
}
