package org.homio.bundle.serial;

import static com.fazecast.jSerialComm.SerialPort.TIMEOUT_NONBLOCKING;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Collection;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.console.ConsolePluginCommunicator;
import org.homio.bundle.api.console.ConsolePluginComplexLines;
import org.homio.bundle.api.port.BaseSerialPort;
import org.homio.bundle.api.port.PortFlowControl;

@Log4j2
public class RawSerialPortCommunicator extends BaseSerialPort {

  private final ConsolePluginCommunicator communicatorConsolePlugin;
  @Getter
  private final CircularFifoQueue<ConsolePluginComplexLines.ComplexString> buffer = new CircularFifoQueue<>(1000);

  public RawSerialPortCommunicator(SerialPort serialPort, EntityContext entityContext,
      ConsolePluginCommunicator consolePluginCommunicator) {
    super("", "Serial", entityContext, 9600, PortFlowControl.FLOWCONTROL_OUT_NONE, () ->
        entityContext.ui().sendErrorMessage("serial_port.exception"), null, log);
    this.serialPort = serialPort;
    this.communicatorConsolePlugin = consolePluginCommunicator;
  }

  @Override
  public boolean open(int baudRate, PortFlowControl flowControl) {
    serialPort.setComPortTimeouts(TIMEOUT_NONBLOCKING, 100, 0);
    serialPort.setComPortParameters(baudRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);

    return super.open(baudRate, flowControl);
  }

  @Override
  @SneakyThrows
  protected void handleSerialEvent(byte[] buf) {
    ConsolePluginComplexLines.ComplexString data =
        ConsolePluginComplexLines.ComplexString.of(new String(buf), System.currentTimeMillis());
    buffer.add(data);
    communicatorConsolePlugin.dataReceived(data);
  }

  public Collection<ConsolePluginComplexLines.ComplexString> getValues() {
    return buffer;
  }
}
