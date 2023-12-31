/**
 * 
 */
package qrmanager.dev;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Roberto Fernandez 
 *
 */
public class RxTxSerialPort implements Runnable {
	// Class attributes
	private static final int PORT_OPEN_TIMEOUT_MS = 2000;
	private static final int ERROR = 0;
	private static final int SUCCESS = 1;
	private static final int THREAD_SLEEP_MS = 5;
	private static Logger logger = LogManager.getLogger(RxTxSerialPort.class);
	// Instance attributes
	private String portName;
	private int speed;
	private SerialPortListener serialPortListener;
	private CommPortIdentifier portIdentifier;
	private CommPort commPort;
	private InputStream in;
	private OutputStream out;

	RxTxSerialPort(String portName, int speed, SerialPortListener serialPortListener) throws Exception{
		setPortName(portName);
		setSpeed(speed);
		setSerialPortListener(serialPortListener);
		// Get streams to write and read serial port
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
	        portIdentifier = CommPortIdentifier.getPortIdentifier(getPortName());
	        if ( portIdentifier.isCurrentlyOwned() )
	        {
	            logger.error("Error: Port " + getPortName() + " is currently in use");
	            return ERROR;
	        }
	        else
	        {
	        	// Get CommPort for given portName, wait until specified timeout milliseconds
	            commPort = portIdentifier.open(this.getClass().getName(), PORT_OPEN_TIMEOUT_MS);
	            
	            if ( commPort instanceof SerialPort )
	            {
	                SerialPort serialPort = (SerialPort) commPort;
	                serialPort.setSerialPortParams(getSpeed(),SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
	                
	                this.in = serialPort.getInputStream();
	                this.out = serialPort.getOutputStream();
	            }
	            else
	            {
	            	logger.error("Error: Only serial ports are handled by this example.");
	                return ERROR;
	            }
	        }     
	        return SUCCESS;
	    }	
	   
	   void close() {
		   commPort.close();
		   portIdentifier = null;
		   in = null;
		   out = null;
	   }
	   
	   // Send out a stream of byte out the serial port
	   public int sendBytes(byte[] cmd) {
		try {
			out.write(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ERROR;
		}
		return SUCCESS;
	   }
	   
	// Thread run method
	public void run() {
		boolean exit = false;
		byte[] buffer = new byte[1024];
		StringBuffer data = new StringBuffer();
        int len;
		logger.debug("!!!!!!!!!!!!!!!!!!!Entering run() for " + getPortName());
		
		while (!exit) {
            
            try
            {
	            	// Simple state machine, start concatenating incoming data when len is not zero
	            	// until len is again zero
            		data.setLength(0);
	            	// len = 0;
			len = this.in.read(buffer);
			logger.debug(">>>> port = " + this.getPortName() + ", len = " + len);
	                while ( len > 0)
	                {
	                	// We got 1st group of data, append it to data
	                	data.append(new String(buffer,0,len, "UTF-8"));
	                	logger.debug("<<<< Serial read = " + data);
	                    // Continue reading until len is zero again
	                    while ((len = this.in.read(buffer)) > 0) {
	                    	data.append(new String(buffer,0,len, "UTF-8"));
	                    	logger.debug("<<<< Serial read = " + data);
	                    }
	                    // len is zero again, we got QR data, report it to registered QRReadListener
	                    getSerialPortListener().dataReceived(data.toString());
	                }
            }
            catch ( IOException e )
            {
            	// Report error to our creator
            	getSerialPortListener().dataReceived("ERROR: reading port " + getPortName() + ", leaving thread run() method. This port not usable until fixing port issue and restarting QRmanager app !!!!!!!!!!!!");
            	close();
            	exit = true;
            	// e.printStackTrace();
            }            	
		}
	}
	
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
