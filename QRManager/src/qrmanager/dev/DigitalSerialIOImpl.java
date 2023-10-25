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
	private static DigitalSerialIOImpl myInstance = null;
	private static String portName = "COM7"; // TODO: Take from configuration map
	private static int speed = 9600; // TODO: Take from configuration map
	private RxTxSerialPort mySerialPort;
	
	public static DigitalSerialIOImpl getInstance() throws Exception {
		if (myInstance == null) {
			myInstance = new DigitalSerialIOImpl(portName, speed);
		}
		return myInstance;
	}
	/** Constructor 
	 * @throws Exception */
	private DigitalSerialIOImpl(String portName, int speed) throws Exception {
		mySerialPort = new RxTxSerialPort(portName, speed, this);
	}

	/** setOutputOn */
	public synchronized int setOutputOn(int channel) {
		// TODO Auto-generated method stub
		String cmdString;
		
		// Add output channel to string
		cmdString = Integer.toString(channel)+"-1;";
		System.out.println(">> setOutputOn = " + cmdString);
		// Get byte array from string and send it out to serial USB to 
		// digital output controller
		mySerialPort.sendBytes(cmdString.getBytes());
		return 0;
	}

	/** setOutputOff */
	public synchronized int setOutputOff(int channel) {
		// TODO Auto-generated method stub
		String cmdString;
		
		// Add output channel to string
		cmdString = Integer.toString(channel)+"-0;";
		System.out.println(">> setOutputOff = " + cmdString);
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
