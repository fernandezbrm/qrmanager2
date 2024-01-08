// Wiegand data 0 line is assigned to Arduino pin D2
const int W_D0 = 2; 
// Wiegand data 1 line is assigned to Arduino pin D3
const int W_D1 = 3;  
// Pulse Width Time
// const int TPW_USECS = 80;
const int TPW_USECS = 100; 
// Pulse Interval Time
// const int TPI_USECS = 240;
const int TPI_USECS = 1000; 
const boolean PARITY_ON = true; // Controls sending or not parity bits
// Control order of transmission of Wiegand bits. 
// Value true, send bits in forward order. false sends in reverse order.
const boolean FORWARD = true; 
const uint8_t CUSTOMER_CODE = 0x00;
int roll = 0;

// serial line input variables
boolean got_line; // 'true' on newline ('\n') reception
char buf[16]; // the input buffer
int index = 0; // current position in buffer

// output one Wiegand bit
void outwiegbit(unsigned int b);
// output a Wiegand-32 code 
void outwieg34(uint32_t u32); 
// process the input line received from the serial port
void process_line(const char str[]); 

void loop() 
{
  if ( got_line )
  {
    process_line(buf);
    got_line = false;
    index = 0;
  }
  else while (Serial.available())
  {
    char c = Serial.read();
    buf[index++] = c;
    if ((c == '\n') || (index == sizeof(buf)-1))
    {
      buf[index] = '\0';
      got_line = true;
    }
  }
}

void setup ()
{
  // initializations code, it runs once:
  pinMode(W_D0, OUTPUT);  
  pinMode(W_D1, OUTPUT);
  digitalWrite(W_D0, 1); // set line to IDLE state
  digitalWrite(W_D1, 1); // "             "
  got_line = false;  
  Serial.begin(9600);
  Serial.write("\n\nWiegand-34 - 11/27/2023\n");
  Serial.write("D2 <--> Wiegand D0\n");
  Serial.write("D3 <--> Wiegand D1\n");
  Serial.write("\nenter number (as text) and newline\n");
}

// outputs ONE Wiegand bit
void outwiegbit(unsigned int b)
{
  Serial.print(b);
  roll = (roll + 1) % 4;
  if (roll == 0) Serial.print("-");

  int sel = b == 0 ? W_D0 : W_D1;
  digitalWrite(sel, 0);
  delayMicroseconds(TPW_USECS);
  digitalWrite(sel, 1);
  delayMicroseconds(TPI_USECS);
}

// outputs a 40 bit Wiegand code if PARITY_ON is true including even and odd parity bits
// Otherwise, only send the 40 bits data with NO parity bits
// 1st byte is customer code followed by 4 bytes of numeric code value in argument U32
void outwieg40(uint8_t customer_code, uint32_t u32)
{
  unsigned int p_even = 0;
  unsigned int p_odd = 1;
  // compute parity on trailing group of bits
  // Now continue with numeric value (32 bits)
  uint32_t tmp = u32; 
  for (int n=0; n<20; ++n)
  {
    p_odd ^= (tmp & 1);
    tmp >>= 1;
  }
  // compute parity on heading group of bits
  for (int n=20; n<32; ++n)
  {
    p_even ^= (tmp & 1);
    tmp >>= 1;
  }
  // And finally, continue heading parity computation in customer code 8 bits
  tmp = customer_code;
  for (int n=0; n<8; ++n)
  {
    p_even ^= (tmp & 1);
    tmp >>= 1;
  }
  Serial.println("-----------------------------------------------------");
  // now output data bits framed by parity ones
  if (PARITY_ON) {
    roll=3;
    outwiegbit(p_even);
  }
  roll = 0;
  // Send data bits
  if (FORWARD) {
     // Send bits in forward order
     // Send customer code 1st
     for (int n=0; n<8; ++n)
     {
        outwiegbit((customer_code >> (7-n)) & 1);
     }
     // Send data
     for (int n=0; n<32; ++n)
     {
        outwiegbit((u32 >> (31-n)) & 1);
     }
  }
  else {
    // Send bits in reverse order
    // Send customer code 1st
     for (int n=7; n>=0; --n)
     {
        outwiegbit((CUSTOMER_CODE >> (7-n)) & 1);
     }
     // Send data
     for (int n=31; n>=0; --n)
     {
        outwiegbit((u32 >> (31-n)) & 1);
     }  
  }
  if (PARITY_ON) {
    outwiegbit(p_odd);
  }  
}

// output just 'meaningful' numbers
void process_line(const char str[])
{
  char msg[64];
  long l = atol(str);
  if (l < 0 || l > 0xFFFFFFFF)
  {
    Serial.write("ERROR\n");
    return;
  }
  sprintf(msg, "OK: %ld (0x%08lX)\n", l, l);
  Serial.write(msg);
  outwieg40(CUSTOMER_CODE, (unsigned long) l);
  delay(1000);
}

