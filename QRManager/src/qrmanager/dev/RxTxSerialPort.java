/**
 * 
 */
package qrmanager.dev;

// log4j dependencies
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

// java.io dependencies
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.io.StringWriter;
import java.io.PrintWriter;

// jSerialComm dependencies
import com.fazecast.jSerialComm.*;

/**
 * @author Roberto Fernandez
 *
 * This is a refactored version from using RxTx to JSerialComm library  
 *
 */
public class RxTxSerialPort implements Runnable {
	// Class attributes
	private static final int PORT_OPEN_TIMEOUT_MS = 2000;
	private static final int ERROR = 0;
	private static final int SUCCESS = 1;
	private static final int THREAD_SLEEP_MS = 5;
	private static final int DATA_BITS = 8; 
	private static Logger logger = LogManager.getLogger(RxTxSerialPort.class);
	
	// Instance attributes
	private String portName;
	private int speed;
	private SerialPortListener serialPortListener;
        private SerialPort serialPort; 

	RxTxSerialPort(String portName, int speed, SerialPortListener serialPortListener) throws Exception{
		setPortName(portName);
		setSpeed(speed);
		setSerialPortListener(serialPortListener);
		
		if (SUCCESS == connect()) {
			// Launch thread to read data from serial port
			Thread thread = new Thread(this);
			thread.start();
		}
		else {
			Exception e = new Exception();
			throw(e);
		}
	}
	
	   int connect() throws Exception
	    {
		// Try to get SerialPort object 
		try {
                   serialPort = SerialPort.getCommPort(getPortName());
		}
		catch (SerialPortInvalidPortException e) {
		   logger.error("Error: Port " + getPortName() + " not found!!!");
	           return ERROR;
		}

		// Now open the port
		boolean success = serialPort.openPort();
		if (success) {
		    serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
		    serialPort.setComPortParameters(getSpeed(), DATA_BITS, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
		}
		else
	        {
	            logger.error("Error: Port " + getPortName() + " not able to be opened!!!");
	            return ERROR;
	        }
     
	        return SUCCESS;
	    }	
	   
	   // Close serial port
	   void close() {
		   serialPort.closePort();
	   }
	   
	   // Send out a stream of byte out the serial port
	   public int sendBytes(byte[] cmd) {
		try {
			serialPort.getOutputStream().write(cmd);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ERROR;
		}
		return SUCCESS;
	   }
	   
	// Thread run method to receive and parse serial port data
	// When data available, serial port listener will be called back
	public void run() {
		boolean exit = false;
		byte[] buffer = new byte[1024];
		StringBuffer data = new StringBuffer();
        	int len;
		logger.debug("!!!!!!!!!!!!!!!!!!!Entering run() for " + getPortName());
		
		while (!exit) {
    
            	   try {
	            	// Simple state machine, start concatenating incoming data when len is not zero
	            	// until len is again zero
            		data.setLength(0);

			len = serialPort.getInputStream().read(buffer);
			logger.debug(">>>> port = " + this.getPortName() + ", len = " + len);
	                while ( len > 0) {
	                    // We got 1st group of data, append it to data
	                    data.append(new String(buffer,0,len, "UTF-8"));
	                    logger.debug("<<<< Serial read = " + data);
	                    // Continue reading until len is zero again
	                    while ((len = serialPort.getInputStream().read(buffer)) > 0) {
				logger.debug(">>>> port = " + this.getPortName() + ", len = " + len);
	                    	data.append(new String(buffer,0, len, "UTF-8"));
	                    	logger.debug("<<<< Serial read = " + data);
			    }
	                }
	                
			// len is zero again, we got QR data, report it to registered QRReadListener
	                getSerialPortListener().dataReceived(data.toString());
	           } // end try {}
            	   catch ( IOException e )
                   {
            	     // Report error to our creator
                     Writer writer = new StringWriter();
                     e.printStackTrace(new PrintWriter(writer));
                     String s = writer.toString();
                     getSerialPortListener().dataReceived("ERROR: reading port " + getPortName() + " " + s); 
            	     // close();
            	     // exit = true;
                  } // end catch {}            	
	     }  // end while {}
	}  // end run() {}
	
	public int getSpeed() {
		return speed;
	}
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	public String getPortName() {
		return portName;
	}
	public void setPortName(String portName) {
		this.portName = portName;
	}
	
	public SerialPortListener getSerialPortListener() {
		return serialPortListener;
	}
	
	public void setSerialPortListener(SerialPortListener serialPortListener) {
		this.serialPortListener = serialPortListener;
	}
}
