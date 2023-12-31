/**
 * 
 */
package qrmanager.dev;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.json.simple.parser.ParseException;

import java.util.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * @author Roberto Fernandez 
 *
 */
public class QRManager implements SerialPortListener, Runnable{
	/** Class attributes */
	private static ReadConfig myReadConfig = new ReadConfig();
	// By default use constant path, it can be overriden by CLI argument
	private static String configFilePath = Constants.CONF_FILE_PATH_DEFAULT;
	// List of QRManager instances
	private static List<QRManagerDTO> myQRMDTO = new ArrayList<QRManagerDTO>();
	// Digital pins serial adapter singleton instance
	private static DigitalSerialIOImpl dSerialImpl;
	// QR JSON schema elements to validated
	private static List <String> schemaElements;
	// Recovery time out. NOT USED
	private static final int RETRY_PERIOD_MS   = 5000;
	// Activity LED attributes
	private static final int RUN_LED_PIN 	   = 13;
	private static final int RUN_LED_PERIOD_MS = 3000;
	private static final String QRMANAGER_NAME = "QRManager Main Thread";
	// Logger
	private static Logger logger = LogManager.getLogger(QRManager.class);

	/** Instance attributes */
	// QRManager configuration parameters DTO
	private QRManagerDTO myQRManagerDTO;
	// QR reader port
	private RxTxSerialPort myQRReader;
	// Digital pins serial adapter singleton instance
	private DigitalIOInterface myDio;
	// Digital pin to control barrier
	private int doChannel;
	// Barrier activation pulse length in milliseconds
	private int pulseLengthMs;
	// QRManager instance logging name
	private String myName;
	
	/** 
    static {
        try {
        	System.out.println("Changing log4j.xml config..........");
            InputStream inputStream = new FileInputStream("C:/temp/log4j2.xml");
            ConfigurationSource source = new ConfigurationSource(inputStream);
            Configurator.initialize(null, source);
        } catch (Exception ex) {
            // Handle here
        	System.out.println("ERROR: log4j configuration change failed!!!!!!!!!!!!!!!");
        }
    }
    */

	/** Constructor 
	 * @throws Exception */
	QRManager(QRManagerDTO qrManagerDTO, DigitalIOInterface dSerialIO) throws Exception {
		logger.debug("QRManager: name = " + qrManagerDTO.getName() + 
			     ", portName = " + qrManagerDTO.getPortName() + 
			     ", speed = " + qrManagerDTO.getSpeed() + 
			     ", doChannel = " + qrManagerDTO.getDoChannel() + 
			     " pulseLengthMs = " + qrManagerDTO.getpulseLenghtMs());
		
		// Save our DTO
		this.myQRManagerDTO = qrManagerDTO;
		
		// Create RxTxReaderSerialPort
		setMyQRReader(new RxTxSerialPort(qrManagerDTO.getPortName(), qrManagerDTO.getSpeed(), this));
		logger.debug("QR reader serial port interface created");
		
		// Save the barrier activation digital output channel bound to this QRManager instance
		this.doChannel = qrManagerDTO.getDoChannel();
		
		// Name to identify this QRManager instance
		this.myName = qrManagerDTO.getName();
		
		// Barrier activation pulse length in milliseconds
		this.pulseLengthMs = qrManagerDTO.getpulseLenghtMs();
		
		// Set digital output interface singleton instance reference
		setMyDio(dSerialIO);
		logger.debug("Digital output interface singleton gotten");
	}

	/** Validate read QR JSON string versus expected elements. 
	 *  Returns true if valid JSON, otherwise false            
 	 */
	private boolean validateJsonSchema(String qr) {
		// Validate that elements in schema are present in read QR JSON structure
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
		   logger.error(data);
		   // Destroy our RxTxSerialPort instance
		   setMyQRReader(null);
		   // Launch recovery thread
		   // Thread thread = new Thread(this);
		   // thread.start();
		}
		// Validate if QR matches schema JSON structure
		else if (validateJsonSchema(data)) {
		   logger.info(">>>> " + myName + " QR VALID = " + data + " ACCESS GRANTED");
		   // If QR valid, trigger momentary corresponding DO output
		   // in order to open lane barrier
		   this.getMyDio().setOutputOn(myName, doChannel); 
		   try {
		      Thread.sleep(pulseLengthMs); 
		   } catch (InterruptedException e) {
		      // TODO Auto-generated catch block
		      e.printStackTrace();
		   } 
		   // Turn off barrier activation DO output
		   this.getMyDio().setOutputOff(myName, doChannel);
		}
		else {
		   logger.info("<<<< " + myName + ": QR INVALID = " + data+ " ACCESS DENIED!!!!");
		}
	}
	
	/**  Serial port recovery thread method. NOT USED */
	public void run() {
		boolean exit = false;
		
		while (!exit) {
			logger.info("Trying to recover serial port...");
			try {
				/** Create RxTxReaderSerialPort */
				setMyQRReader(new RxTxSerialPort(myQRManagerDTO.getPortName(), myQRManagerDTO.getSpeed(), this));
				logger.info("QR reader serial port interface created");
				// Leave thread
				exit = true;
			}
			catch (Exception e) {
				logger.error("Error trying to recreate RxTxSerialPort");
				setMyQRReader(null);
			}
			finally {
				try {
					logger.debug("Sleeping...");
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
            logger.info("Argument count: " + args.length);
	    for (int i = 0; i < args.length; i++) {
	    	logger.info("Argument " + i + ": " + args[i]);
	        if (0 == i) {
		  // First argument is path to qrmanager_conf.j
	          configFilePath = args[i];
	        }
	    }
	    
	    // Read configuration file
	    try {
	       // Read QRManager instances from configuration file
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
	    logger.info("-------------------------------\n");
	    dSerialImpl = DigitalSerialIOImpl.getInstance(configFilePath);

	    // Create QRManager instances based in configuration file
	    for (int i = 0; i < myQRMDTO.size(); i++) {
	       try {
	          // Create QRManager instance based in configuration
		  QRManager qrm = new QRManager(myQRMDTO.get(i), dSerialImpl);
		  // Thread.sleep(1000);
	       } catch (Exception e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
	       }
	       logger.info("-------------------------------\n" +
	                   "QRManager instance with parameters " + 
		           "name = " + myQRMDTO.get(i).getName() + 
			   ", portName = " + myQRMDTO.get(i).getPortName() + 
			   ", speed = " + myQRMDTO.get(i).getSpeed() +  
			   " doChannel = " + myQRMDTO.get(i).getDoChannel() +  
			   " pulseLengthMs = " +
 			   myQRMDTO.get(i).getpulseLenghtMs() + " created");
	    }
            logger.info("-------------------------------\n");
		
	    // Report QRManager is up and running by blinking pin 13 LED
	    while (true) {
	       dSerialImpl.setOutputOn(QRMANAGER_NAME, RUN_LED_PIN); 
	       try {
	          Thread.sleep(RUN_LED_PERIOD_MS); 
	       } catch (InterruptedException e) {
	          // TODO Auto-generated catch block
		  e.printStackTrace();
	       } 
	       dSerialImpl.setOutputOff(QRMANAGER_NAME, RUN_LED_PIN);
	       try {
	          Thread.sleep(RUN_LED_PERIOD_MS); 
	       } catch (InterruptedException e) {
	          // TODO Auto-generated catch block
		  e.printStackTrace();
	       } 
	    } 
	} // End of main() method
} // End of QRManager class
