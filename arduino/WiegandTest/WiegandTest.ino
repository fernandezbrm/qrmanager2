#include "Wiegand.h"

// Door lock configuration
const int DOOR_LOCK = 10;
const int DOOR_LOCK_DELAY = 2000;
// Door lock shall always be active. NC relay output to be used.
const int ACTIVE   = 0;
const int INACTIVE = 1;

// Wiegand input configuration
const int W1_D0 = 2;
const int W1_D1 = 3;
const int W2_D0 = 4;
const int W2_D1 = 5;

// Wiegand reader instances
WIEGAND wg1;
WIEGAND wg2;

// Valid cards to be granted access
unsigned long validCards[] = {
  0x472B03E5,
  1234
};

void setup() {
	Serial.begin(9600);  
  pinMode(DOOR_LOCK, OUTPUT);
  // Door lock starts on
  digitalWrite(DOOR_LOCK, INACTIVE);
   
	// default Wiegand Pin 2 and Pin 3 see image on README.md
	// for non UNO board, use wg.begin(pinD0, pinD1) where pinD0 and pinD1 
	// are the pins connected to D0 and D1 of wiegand reader respectively.
	wg1.begin(W1_D0, W1_D1);
  Serial.println("Reader 1 Initialized!!!!");
  wg2.begin(W2_D0, W2_D1);
  Serial.println("Reader 2 Initialized!!!!");
}

void openDoorLock() {
  digitalWrite(DOOR_LOCK, ACTIVE);
  delay(DOOR_LOCK_DELAY);
  digitalWrite(DOOR_LOCK, INACTIVE);
}

void loop() {
  int numCards;
  bool validCard;

	if(wg1.available()) {
		Serial.print("Wiegand HEX = ");
		Serial.print(wg1.getCode(),HEX);
		Serial.print(", DECIMAL = ");
		Serial.print(wg1.getCode());
		Serial.print(", Type W");
		Serial.println(wg1.getWiegandType());
    numCards = sizeof(validCards)/sizeof(unsigned long);
    validCard = false;
    for (int i = 0; i < numCards; i++) {
      if (wg1.getCode() == validCards[i]) {
        Serial.print("READER 1: Valid card read, ");
        Serial.print(validCards[i], HEX);
        Serial.println(", release door lock");
        validCard = true;
        openDoorLock();
        break;
      }      
    }
    if (!validCard) {
      Serial.print("READER 1: INVALID card read, ");
      Serial.println(wg1.getCode(), HEX);
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
    validCard = false;
    for (int i = 0; i < numCards; i++) {
       if (wg2.getCode() == validCards[i]) {
        Serial.print("READER 1: Valid card read, ");
        Serial.print(validCards[i], HEX);
        Serial.println(", release door lock");
        validCard = true;
        openDoorLock();
        break;
       }      
    }    
    if (!validCard) {
      Serial.print("READER 2: INVALID card read, ");
      Serial.println(wg2.getCode(), HEX);
    }
  }

}
