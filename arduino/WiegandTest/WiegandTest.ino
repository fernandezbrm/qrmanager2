#include "Wiegand.h"

const int DOOR_LOCK = 10;
const int DOOR_LOCK_DELAY = 2000;

const int W1_D0 = 2;
const int W1_D1 = 3;
const int W2_D0 = 4;
const int W2_D1 = 5;

WIEGAND wg1;
WIEGAND wg2;

unsigned long validCards[] = {
  0x472B03E5,
  1234
};

void setup() {
	Serial.begin(9600);  
  pinMode(DOOR_LOCK, OUTPUT);
  // Door lock starts on
  digitalWrite(DOOR_LOCK, 1);
   
	// default Wiegand Pin 2 and Pin 3 see image on README.md
	// for non UNO board, use wg.begin(pinD0, pinD1) where pinD0 and pinD1 
	// are the pins connected to D0 and D1 of wiegand reader respectively.
	wg1.begin(W1_D0, W1_D1);
  Serial.println("Reader 1 Initialized!!!!");
  wg2.begin(W2_D0, W2_D1);
  Serial.println("Reader 2 Initialized!!!!");
}

void openDoorLock() {
  digitalWrite(DOOR_LOCK, 0);
  delay(DOOR_LOCK_DELAY);
  digitalWrite(DOOR_LOCK, 1);
}

void loop() {
  int numCards;
  
	if(wg1.available()) {
		Serial.print("Wiegand HEX = ");
		Serial.print(wg1.getCode(),HEX);
		Serial.print(", DECIMAL = ");
		Serial.print(wg1.getCode());
		Serial.print(", Type W");
		Serial.println(wg1.getWiegandType());
    numCards = sizeof(validCards)/sizeof(unsigned long);
    for (int i = 0; i++; i < numCards) {
       if (wg1.getCode() == validCards[i]) {
        Serial.println("READER 1: Valid card read, release door lock");
        openDoorLock();
        break;
       }      
    }
  }

  if(wg2.available()) {
    Serial.print("Wiegand HEX = ");
    Serial.print(wg2.getCode(),HEX);
    Serial.print(", DECIMAL = ");
    Serial.print(wg2.getCode());
    Serial.print(", Type W");
    Serial.println(wg2.getWiegandType());
        numCards = sizeof(validCards)/sizeof(unsigned long);
    for (int i = 0; i++; i < numCards) {
       if (wg2.getCode() == validCards[i]) {
        Serial.println("READER 2: Valid card read
        
        , release door lock");
        openDoorLock();
        break;
       }      
    }    
  }
}
