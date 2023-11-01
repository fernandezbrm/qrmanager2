/**
 * 
 */
package qrmanager.dev;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * 
 * This is a singleton class to manage digital outputs
 * via serial port.
 * 
 * @author Roberto Fernandez 
 *
 */
public class DigitalSerialIOImpl implements DigitalIOInterface, SerialPortListener{
	// Class attributes
	private static DoDTO dto; 
	private static DigitalSerialIOImpl myInstance = null;
	private static Logger logger = LogManager.getLogger(DigitalSerialIOImpl.class);
	// Instance attributes
	private RxTxSerialPort mySerialPort;
	
	public static DigitalSerialIOImpl getInstance(String configFilePath) throws Exception {
		if (myInstance == null) {
			// Read digital output configuration for USB to DO board
			// and use it to instantiate singleton DigitalSerialIOImpl accordingly
			ReadConfig conf = new ReadConfig();
			dto = conf.getDigitalOutput(configFilePath);
			myInstance = new DigitalSerialIOImpl(dto.getPortName(), dto.getSpeed());
			logger.info("DigitalSerialIOImpl singleton instance created: portName = " + dto.getPortName() + 
					            ", speed = " + dto.getSpeed());
		}
		return myInstance;
	}
	/** Constructor 
	 * @throws Exception */
	private DigitalSerialIOImpl(String portName, int speed) throws Exception {
		mySerialPort = new RxTxSerialPort(portName, speed, this);
	}

	/** setOutputOn */
	public synchronized int setOutputOn(String name, int channel) {
		// TODO Auto-generated method stub
		String cmdString;
		
		// Add output channel to string
		cmdString = Integer.toString(channel)+"-1;";
		logger.debug(": setOutputOn = " + cmdString);
		
		// Get byte array from string and send it out to serial USB to 
		// digital output controller
		mySerialPort.sendBytes(cmdString.getBytes());
		return 0;
	}

	/** setOutputOff */
	public synchronized int setOutputOff(String name, int channel) {
		// TODO Auto-generated method stub
		String cmdString;
		
		// Add output channel to string
		cmdString = Integer.toString(channel)+"-0;";
		logger.debug(name + ": setOutputOff = " + cmdString);
		
		// Get byte array from string and send it out to serial USB to 
		// digital output controller
		mySerialPort.sendBytes(cmdString.getBytes());
		return 0;		
	}
	public void dataReceived(String reply) {
		// TODO Auto-generated method stub
		if (!reply.contains("SUCCESS")) {
			logger.error(" <<<< DigitalSerialIOImpl ERROR:" + this + " serial read = " + reply);
		}
	}

}
