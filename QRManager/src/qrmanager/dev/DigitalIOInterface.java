/**
 * 
 */
package qrmanager.dev;

/**
 * @author Roberto Fernandez 
 *
 */
public interface DigitalIOInterface {
	public int setOutputOn(String name, int channel);
	public int setOutputOff(String name, int channel);
}
