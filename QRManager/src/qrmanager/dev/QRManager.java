/**
 * 
 */
package qrmanager.dev;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.parser.ParseException;

import java.util.*;

/**
 * @author Roberto Fernandez 
 *
 */
public class QRManager implements SerialPortListener, Runnable{
	// Class attributes
	private static ReadConfig myReadConfig = new ReadConfig();
	// By default use constant path, it can be overriden by CLI argument
	private static String configFilePath = Constants.CONF_FILE_PATH_DEFAULT;
	private static List<QRManagerDTO> myQRMDTO = new ArrayList<QRManagerDTO>();
	private static DigitalSerialIOImpl dSerialImpl;
	private static List <String> schemaElements;
	private static final int RETRY_PERIOD_MS = 5000;
	// Instance attributes
	private QRManagerDTO myQRManagerDTO;
	private RxTxSerialPort myQRReader;
	private DigitalIOInterface myDio;
	private int doChannel;
	private int pulseLengthMs;
	private String myName;

	/** Constructor 
	 * @throws Exception */
	QRManager(QRManagerDTO qrManagerDTO, DigitalIOInterface dSerialIO) throws Exception {
		// TODO Auto-generated method stub
		/** System.out.println("QRManager: name = " + qrManagerDTO.getName() + 
							", portName = " + qrManagerDTO.getPortName() + 
							", speed = " + qrManagerDTO.getSpeed() + 
							", doChannel = " + qrManagerDTO.getDoChannel() + 
							" pulseLengthMs = " + qrManagerDTO.getpulseLenghtMs());
		*/
		
		// Save our DTO
		this.myQRManagerDTO = qrManagerDTO;
		
		/** Create RxTxReaderSerialPort */
		setMyQRReader(new RxTxSerialPort(qrManagerDTO.getPortName(), qrManagerDTO.getSpeed(), this));
		// System.out.println("QR reader serial port interface created");
		
		// Save the digital output channel bound to this QRManager instance
		this.doChannel = qrManagerDTO.getDoChannel();
		
		// Name to identify this QRManager instance
		this.myName = qrManagerDTO.getName();
		
		// Pulse length in milliseconds
		this.pulseLengthMs = qrManagerDTO.getpulseLenghtMs();
		
		/** Set digital output interface singleton instance reference*/
		setMyDio(dSerialIO);
		// System.out.println("Digital output interface singleton gotten");
	}

	private boolean validateJsonSchema(String qr) {
		// Validate that elements in schema present in read QR JSOn structure
		for (String element: schemaElements) {
			if (!qr.contains(element)) {
				return false;
			}
		}
		return true;
	}
	
	/** This listener method is invoked by our QRReaderSerialPort
	 *  instance when a new QR has been read from serial port 
	 */
	public void dataReceived(String data) {
		// Check if out thread reported an error and terminated
		if (data.contains("ERROR: reading port")) {
			// We got a fatal error with the QR reader serial to USB converter, try to recover
			System.out.println(data);
			// Destroy our RxTxSerialPort instance
			setMyQRReader(null);
			// Launch recovery thread
			// Thread thread = new Thread(this);
			// thread.start();
		}
		// Validate if QR matches schema JSON structure
		else if (validateJsonSchema(data)) {
			System.out.println(">>>> " + myName + " QR VALID = " + data + " ACCESS GRANTED");
			// If QR valid, trigger momentary corresponding DO output
			// in order to open lane barrier
			this.getMyDio().setOutputOn(myName, doChannel); 
			try {
				Thread.sleep(pulseLengthMs); 
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			this.getMyDio().setOutputOff(myName, doChannel);
		}
		else {
			System.out.println("<<<< " + myName + ": QR INVALID = " + data+ " ACCESS DENIED!!!!");
		}
	}
	
	// Recovery thread method
	public void run() {
		boolean exit = false;
		
		while (!exit) {
			System.out.println("Trying to recover serial port...");
			try {
				/** Create RxTxReaderSerialPort */
				setMyQRReader(new RxTxSerialPort(myQRManagerDTO.getPortName(), myQRManagerDTO.getSpeed(), this));
				System.out.println("QR reader serial port interface created");
				// Leave thread
				exit = true;
			}
			catch (Exception e) {
				System.out.println("Error trying to recreate RxTxSerialPort");
				setMyQRReader(null);
			}
			finally {
				try {
					System.out.println("Sleeping...");
					Thread.sleep(RETRY_PERIOD_MS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					// .printStackTrace();
				}
			}
		}
	}
	
	public void setMyDio(DigitalIOInterface dio) {
		this.myDio = dio;
	}
	
	public DigitalIOInterface getMyDio() {
		return myDio;
	}

	public void setMyQRReader(RxTxSerialPort myQRReader) {
		this.myQRReader = myQRReader;
	}
	
	public RxTxSerialPort getMyQRReader() {
		return myQRReader;
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
	    // Read command line arguments
		System.out.println("Argument count: " + args.length);
	    for (int i = 0; i < args.length; i++) {
	        System.out.println("Argument " + i + ": " + args[i]);
	        if (0 == i) {
	        	configFilePath = args[i];
	        }
	    }
	    
		// Read configuration file
		try {
			// Read configuration file
			myQRMDTO = myReadConfig.getQrManagers(configFilePath);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		// Read JSON schema validation elements from configuration
		try {
			// Read configuration file
			schemaElements = myReadConfig.getSchemaValidator(configFilePath);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}		
		
		// Get DigitalSerialIOImpl singleton instance to be used 
		// by all QRManager instances
		dSerialImpl = DigitalSerialIOImpl.getInstance(configFilePath);

		for (int i = 0; i < myQRMDTO.size(); i++) {
			try {
				// Create QRManager instance based in configuration
				QRManager qrm = new QRManager(myQRMDTO.get(i), 
											  dSerialImpl);
				// Thread.sleep(1000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("-------------------------------\n" +
								"QRManager instance with parameters " + 
								"name = " + myQRMDTO.get(i).getName() + 
								", portName = " + myQRMDTO.get(i).getPortName() + 
								", speed = " + myQRMDTO.get(i).getSpeed() +  
								" doChannel = " + myQRMDTO.get(i).getDoChannel() +  
								" pulseLengthMs = " + myQRMDTO.get(i).getpulseLenghtMs() + " created");
		}
	}
}
