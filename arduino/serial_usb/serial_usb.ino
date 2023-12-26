//
// Serial commander to set digital outputs
//
// Command format: 
//    Output_Pin-State;
//    Where Output_Pin can be in numeric range of 2-13
//    Where State can be 0 or 1 
//
//  Examples: 13-1; 2-0; 8-1;
//

// this is the speed at which to initiate the serial over USB connection 
#define PORT_SPEED 115200
#define SERIAL_READ_TO_MS 10
// Pins 0 and 1 are serial UART, excluded
#define PIN_MIN 2
#define PIN_MAX 13
// Command pin states
#define OFF 0
#define ON  1
// Active/Inactive positive/negative logic 
#define ACTIVE   OFF
#define INACTIVE ON

// State variables 
int fsmState = 0;
String outPin;
String outPinState;

void setup() {
  // Initialize serial USB port
  Serial.begin(PORT_SPEED);
  Serial.setTimeout(SERIAL_READ_TO_MS);
  // sets the digital pin 2 to 13 as output

  // All IO pins set as output
  for (int i = PIN_MIN; i <= PIN_MAX; i++) {  
    pinMode(i, OUTPUT); 
    digitalWrite(i, INACTIVE);    
  }
}

void processCommand() {
  // Validate pin is a number
  for (int i = 0; i < outPin.length(); i++) {
    if (!isDigit(outPin[i])) {
      Serial.write("ERROR: One or more pin digits are not a decimal digit [");  
      Serial.print(outPin);
      Serial.write("]");
      return;
    }
  }

  // Validate state is a number
  for (int i = 0; i < outPinState.length(); i++) {
    if (!isDigit(outPinState[i])) {
      Serial.write("ERROR: One or more state digits are not a decimal digit [");  
      Serial.print(outPinState);
      Serial.write("]");
      return;
    }
  }

  int pin = outPin.toInt();
  // Serial.print(">>>> PIN = ");
  // Serial.print(pin);
  int state = outPinState.toInt();
 
  if ((pin < PIN_MIN) || (pin > PIN_MAX)) {
    Serial.write("ERROR: Invalid pin number [");
    Serial.print(pin);
    Serial.write("]");
    return;
  }

  if (state == OFF) {
    digitalWrite(pin, INACTIVE); // sets the digital pin to INACTIVE state
  }
  else if (state == ON) {
    digitalWrite(pin, ACTIVE); // sets the digital pin to ACTIVE state
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
            outPin.concat(commandBuffer[i]);     
          }
          else {
            fsmState = 1;
          }
          break; 
        case 1:
          if (commandBuffer[i] != ';') {
            outPinState.concat(commandBuffer[i]);
          }
          else {
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
