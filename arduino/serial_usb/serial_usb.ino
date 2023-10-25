//
// Serial commander to set digital outputs
//
// Command format: 
//    Output_Pin-State;
//    Where Output_Pin can be in numeric range of 0-13
//    Where State can be 0 or 1 
//
//  Examples: 13-1; 2-0; 8-1;
//

// this is the speed at which to initiate the serial over USB connection 
#define PORT_SPEED 9600
#define SERIAL_READ_TO_MS 10
#define PIN_MIN 0
#define PIN_MAX 13
#define OFF 0
#define ON  1

// State variables 
int fsmState = 0;
String outPin;
String outPinState;

void setup() {
  // Initialize serial USB port
  Serial.begin(PORT_SPEED);
  Serial.setTimeout(SERIAL_READ_TO_MS);
  // sets the digital pin 13 as output

  // All IO pins set as output
  for (int i = 0; i <= PIN_MAX; i++) {  
    pinMode(i, OUTPUT);    
  }
}

void processCommand() {
  int pin = outPin.toInt();
  int state = outPinState.toInt();
 
  if ((pin < 0) || (pin > 13)) {
    Serial.write("ERROR: Invalid pin number [");
    Serial.print(pin);
    Serial.write("]");
    return;
  }

  if (state == 0) {
    digitalWrite(pin, LOW); // sets the digital pin to OFF
  }
  else if (state == 1) {
  digitalWrite(pin, HIGH); // sets the digital pin to OFF
  }
  else {
      Serial.write("ERROR: Invalid pin state [");
      Serial.print(state);
      Serial.write("]");
      return;
  }
  Serial.write("SUCCESS");
}

void loop() { 
  char commandBuffer[30];
   bool resetFsm = false;

  // Read from serial port
	int bytesRead = Serial.readBytes(commandBuffer, sizeof(commandBuffer)-1);
  commandBuffer[bytesRead] = '\0';
  
  if (bytesRead > 0) {
    // Process all chars received
    for (int i =0; i < bytesRead; i++)
    {
      switch(fsmState) {
        case 0:
          if (commandBuffer[i] != '-') {
            if (isDigit(commandBuffer[i])) {
              outPin.concat(commandBuffer[i]); 
            }
            else {
              resetFsm = true;
              break;
            }           
          }
          else {
            // Serial.print("outPin = ");
            // Serial.println(outPin);
            fsmState = 1;
          }
          break; 
        case 1:
          if (commandBuffer[i] != ';') {
            if (isDigit(commandBuffer[i])) {
              outPinState.concat(commandBuffer[i]);
            }
            else {
              resetFsm = true;
              break;
            }           
          }
          else {
            // Serial.print("outPinState = ");
            // Serial.println(outPinState);
            // Process received command
            processCommand();
            // Reset all FSM state variables to start over
            resetFsm = true;
          }
          break;
      }
      
      if (resetFsm) {
        outPin = "";
        outPinState = "";
        fsmState = 0;
        resetFsm = false;
      }
    }
  }
}
