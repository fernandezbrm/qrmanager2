/**
 * 
 */
package qrmanager.dev;

/**
 * @author Roberto Fernandez
 *
 */
public class QRManagerDTO {
	private String name;
	private String portName;
	private int speed;
	private int doChannel;
	private int pulseLengthMs;
	
	public void setName(String name){
		this.name = name;
	}
	public String getName() {
		return this.name;
	}
	public void setPortName(String portName){
		this.portName = portName;
	}
	public String getPortName() {
		return this.portName;
	}
	public void setSpeed(int speed){
		this.speed = speed;
	}
	public int getSpeed() {
		return this.speed;
	}
	public void setDoChannel(int doChannel){
		this.doChannel = doChannel;
	}
	public int getDoChannel() {
		return this.doChannel;
	}
	public void setPulseLengthMs(int pulseLengthMs){
		this.pulseLengthMs = pulseLengthMs;
	}
	public int getpulseLenghtMs() {
		return this.pulseLengthMs;
	}
}
