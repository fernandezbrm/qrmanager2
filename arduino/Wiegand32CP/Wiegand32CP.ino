const int W_D0 = 2; // Wiegand data 0 line is assigned to Arduino pin D2
const int W_D1 = 3; //   "       "  1  "            "            "    D3
// const int TPW_USECS = 80; // Pulse Width Time
// const int TPI_USECS = 240; // Pulse Interval Time
const int TPW_USECS = 100; // Pulse Width Time
const int TPI_USECS = 1000; // Pulse Interval Time
const boolean PARITY_ON = false; // Controls sending or not parity bits

// serial line input variables
boolean got_line; // 'true' on newline ('\n') reception
char buf[16]; // the input buffer
int index = 0; // current position in buffer

void outwiegbit(unsigned int b); // output one Wiegand bit
void outwieg34(uint32_t u32); // output a Wiegand-32 code
void process_line(const char str[]); // process the input line received from the serial port

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
  int sel = b == 0 ? W_D0 : W_D1;
  digitalWrite(sel, 0);
  delayMicroseconds(TPW_USECS);
  digitalWrite(sel, 1);
  delayMicroseconds(TPI_USECS);
}

// outputs a 34 bit Wiegand code if PARITY_ON is true including even and odd parity bits
// Otherwise, only send the 32 bits data with NO parity bits
// u32 is actually the 32-bit numeric code
void outwieg34(uint32_t u32)
{
  uint32_t tmp = u32;
  unsigned int p_even = 0;
  unsigned int p_odd = 1;
  // compute parity on trailing group of bits 
  for (int n=0; n<16; ++n)
  {
    p_odd ^= (tmp & 1);
    tmp >>= 1;
  }
  // compute parity on heading group of bits
  for (int n=16; n<32; ++n)
  {
    p_even ^= (tmp & 1);
    tmp >>= 1;
  }
  // now output data bits framed by parity ones
  if (PARITY_ON) {
    Serial.println("Parity even");
    outwiegbit(p_even);
  }
  for (int n=0; n<32; ++n)
  {
    outwiegbit((u32 >> (31-n)) & 1);
  }
  if (PARITY_ON) {
    Serial.println("Parity odd");
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
  outwieg34((unsigned long) l);
  delay(1000);
}

