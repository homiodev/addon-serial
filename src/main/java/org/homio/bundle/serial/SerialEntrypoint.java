package org.homio.bundle.serial;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.homio.bundle.api.BundleEntrypoint;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class SerialEntrypoint implements BundleEntrypoint {

  private final SerialPortConsolePlugin serialPortConsolePlugin;

  public void init() {
    serialPortConsolePlugin.init();
  }

  @Override
  public int order() {
    return 2000;
  }
}
