/**
 * 
 */
package qrmanager.dev;

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
	// Instance attributes
	private RxTxSerialPort mySerialPort;
	
	public static DigitalSerialIOImpl getInstance(String configFilePath) throws Exception {
		if (myInstance == null) {
			// Read digital output configuration for USB to DO board
			// and use it to instantiate singleton DigitalSerialIOImpl accordingly
			ReadConfig conf = new ReadConfig();
			dto = conf.getDigitalOutput(configFilePath);
			myInstance = new DigitalSerialIOImpl(dto.getPortName(), dto.getSpeed());
			System.out.println("DigitalSerialIOImpl singleton instance created: portName = " + dto.getPortName() + 
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
		System.out.println(name + ": setOutputOn = " + cmdString);
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
		System.out.println(name + ": setOutputOff = " + cmdString);
		// Get byte array from string and send it out to serial USB to 
		// digital output controller
		mySerialPort.sendBytes(cmdString.getBytes());
		return 0;		
	}
	public void dataReceived(String qr) {
		// TODO Auto-generated method stub
		System.out.println(" <<<< DigitalSerialIOImpl:" + this + " serial read = " + qr);
	}

}
