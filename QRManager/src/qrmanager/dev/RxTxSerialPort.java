/**
 * 
 */
package qrmanager.dev;

import java.util.Random;

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
	private static final int PORT_OPEN_TIMEOUT_MS = 2000;
	private static final int ERROR = 0;
	private static final int SUCCESS = 1;
	private String portName;
	private int speed;
	private SerialPortListener serialPortListener;
	private InputStream in;
	private OutputStream out;

	RxTxSerialPort(String portName, int speed, SerialPortListener serialPortListener) throws Exception{
		setPortName(portName);
		setSpeed(speed);
		setSerialPortListener(serialPortListener);
		// Get streams to write and read serial port
		connect();
		// Launch thread to read QRs from reader via serial port
		Thread thread = new Thread(this);
		thread.start();
	}
	
	   void connect() throws Exception
	    {
	        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(getPortName());
	        if ( portIdentifier.isCurrentlyOwned() )
	        {
	            System.out.println("Error: Port " + getPortName() + " is currently in use");
	        }
	        else
	        {
	        	// Get CommPort for given portName, wait until specified timeout milliseconds
	            CommPort commPort = portIdentifier.open(this.getClass().getName(), PORT_OPEN_TIMEOUT_MS);
	            
	            if ( commPort instanceof SerialPort )
	            {
	                SerialPort serialPort = (SerialPort) commPort;
	                serialPort.setSerialPortParams(getSpeed(),SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
	                
	                this.in = serialPort.getInputStream();
	                this.out = serialPort.getOutputStream();
	            }
	            else
	            {
	                System.out.println("Error: Only serial ports are handled by this example.");
	            }
	        }     
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
		while (!exit) {
            byte[] buffer = new byte[1024];
            int len;
            try
            {
            	// Simple state machine, start concatenating incoming data when len is not zero
            	// until len is again zero
            	StringBuffer data = new StringBuffer();
            	len = 0;
                while ((len = this.in.read(buffer)) > 0)
                {
                	// We got 1st group of data, append it to data
                	data.append(new String(buffer,0,len, "UTF-8"));
                    // System.out.println("<<<< Serial read = " + data);
                    // Continue reading until len is zero again
                    while ((len = this.in.read(buffer)) > 0) {
                    	data.append(new StringBuffer(new String(buffer,0,len, "UTF-8")));
                    	// System.out.println("<<<< Serial read = " + data);
                    }
                    // len is zero again, we got QR data, report it to registered QRReadListener
                    getSerialPortListener().dataReceived(data.toString());
                }
                
            }
            catch ( IOException e )
            {
                e.printStackTrace();
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
