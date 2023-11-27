
const int W_D0 = 2; // Wiegand data 0 line is assigned to Arduino pin D2
const int W_D1 = 3; //   "       "  1  "            "            "    D3

// serial line input variables
boolean got_line; // 'true' on newline ('\n') reception
char buf[16]; // the input buffer
int index = 0; // current position in buffer

void outwiegbit(unsigned int b); // output one Wiegand bit
void outwieg26(uint32_t u32); // output a Wiegand-26 code
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
  Serial.write("\n\nWiegand-26 - Jul 2018\n");
  Serial.write("D2 <--> Wiegand D0\n");
  Serial.write("D3 <--> Wiegand D1\n");
  Serial.write("\nenter number (as text) and newline\n");
}

// outputs ONE Wiegand bit
void outwiegbit(unsigned int b)
{
  int sel = b == 0 ? W_D0 : W_D1;
  digitalWrite(sel, 0);
  delayMicroseconds(80);
  digitalWrite(sel, 1);
  delayMicroseconds(240);
}

// outputs a 26 bit Wiegand code
// u32 is actually the 24-bit numeric code
void outwieg26(uint32_t u32)
{
  uint32_t tmp = u32;
  unsigned int p_even = 0;
  unsigned int p_odd = 1;
  // compute parity on trailing group of bits 
  for (int n=0; n<12; ++n)
  {
    p_odd ^= (tmp & 1);
    tmp >>= 1;
  }
  // compute parity on heading group of bits
  for (int n=12; n<24; ++n)
  {
    p_even ^= (tmp & 1);
    tmp >>= 1;
  }
  // now output data bits framed by parity ones
  outwiegbit(p_even);
  for (int n=0; n<24; ++n)
  {
    outwiegbit((u32 >> (23-n)) & 1);
  }
  outwiegbit(p_odd);  
}

// output just 'meaningful' numbers
void process_line(const char str[])
{
  char msg[64];
  long l = atol(str);
  if (l < 0 || l > 0xFFFFFF)
  {
    Serial.write("ERROR\n");
    return;
  }
  sprintf(msg, "OK: %ld (0x%06lX)\n", l, l);
  Serial.write(msg);
  outwieg26((unsigned long) l);
  delay(1000);
}

