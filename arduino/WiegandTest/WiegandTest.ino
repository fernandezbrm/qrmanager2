#include "Wiegand.h"

const int SPEED  = 9600;

// Wiegand reader 1 input pins
const int WG1_D0 = 2;
const int WG1_D1 = 3;

// Wiegand reader 2 input pins
const int WG2_D0 = 4;
const int WG2_D1 = 5;

// Wiegand reader 1 instance
WIEGAND wg1;
// Wiegand reader 2 instance
WIEGAND wg2;

void setup() {
	Serial.begin(SPEED);  
	
	// default Wiegand Pin 2 and Pin 3 see image on README.md
	// for non UNO board, use wg.begin(pinD0, pinD1) where pinD0 and pinD1 
	// are the pins connected to D0 and D1 of wiegand reader respectively.
	wg1.begin(WG1_D0, WG1_D1);
  wg2.begin(WG2_D0, WG2_D0);
}

void loop() {
	if(wg1.available())
	{
		Serial.print("Wiegand 1: HEX = ");
		Serial.print(wg1.getCode(),HEX);
		Serial.print(", DECIMAL = ");
		Serial.print(wg1.getCode());
		Serial.print(", Type W");
		Serial.println(wg1.getWiegandType());    
	}
  
  
  if(wg2.available())
	{
		Serial.print("Wiegand 2: HEX = ");
		Serial.print(wg2.getCode(),HEX);
		Serial.print(", DECIMAL = ");
		Serial.print(wg2.getCode());
		Serial.print(", Type W");
		Serial.println(wg2.getWiegandType());    
	}
}
