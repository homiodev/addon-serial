package org.homio.bundle.serial;

import com.fazecast.jSerialComm.SerialPort;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.homio.api.Context;
import org.homio.api.console.ConsolePluginCommunicator;
import org.homio.api.console.ConsolePluginComplexLines;
import org.homio.api.port.BaseSerialPort;
import org.homio.api.port.PortFlowControl;

import java.util.Collection;

import static com.fazecast.jSerialComm.SerialPort.TIMEOUT_NONBLOCKING;

@Log4j2
public class RawSerialPortCommunicator extends BaseSerialPort {

  private final ConsolePluginCommunicator communicatorConsolePlugin;
  @Getter
  private final CircularFifoQueue<ConsolePluginComplexLines.ComplexString> buffer = new CircularFifoQueue<>(1000);

  public RawSerialPortCommunicator(SerialPort serialPort, Context entityContext,
                                   ConsolePluginCommunicator consolePluginCommunicator) {
    super("", "Serial", entityContext, 9600, PortFlowControl.FLOWCONTROL_OUT_NONE, () ->
      entityContext.ui().toastr().error("serial_port.exception"), null, log);
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
