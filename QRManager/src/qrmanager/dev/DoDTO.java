/**
 * 
 */
package qrmanager.dev;

/**
 * @author Roberto Fernandez
 *
 */
public class DoDTO {
		private String portName;
		private int speed;

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
}
