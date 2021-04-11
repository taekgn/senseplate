#include "SoftwareSerial.h" //RF communication library
#include "SPI.h" //serial peripheral library
#include "Adafruit_GFX.h" // main graphics library
#include "MCUFRIEND_kbv.h"  // main tft library
#include "TouchScreen.h" // touch I/O library

////////////////////////////////////////////////////////
#include "HX711.h"
#define DT 45//Define where the data is coming from on the arduino.
#define SCK 52

const float calibrationFactor = -1201.27;//The calibration factor was calculated in a seperate script, this can be found in either the group report or Kathryn Hurd's
//individual report or logbook.
float scaleReading;
HX711 scale;
///////////////////////////////////////////////////////

//HEX code for each colors
#define BLACK   0x0000
#define BLUE    0x001F
#define RED     0xF800
#define GREEN   0x07E0
#define CYAN    0x07FF
#define MAGENTA 0xF81F
#define YELLOW  0xFFE0
#define WHITE   0xFFFF

//Constants for Touch I/O
#define MINPRESSURE 100
#define MAXPRESSURE 6000
#define SWAP(a, b) {uint16_t tmp = a; a = b; b = tmp;}

#define LCD_CS A3
#define LCD_CD A2
#define LCD_WR A1
#define LCD_RD A0
#define LCD_RESET A4
#define LCD_Backlight 3

#define BT_SERIAL_RX_DIO 32
#define BT_SERIAL_TX_DIO 33
SoftwareSerial BluetoothSerial(BT_SERIAL_RX_DIO, BT_SERIAL_TX_DIO); // RX, TX, insert them backward 

//PIN and Resolution declaration
const int XP = 6, XM = A2, YP = A1, YM = 7; //320x480 ID=0x9488
int TS_LEFT = 920, TS_RT = 90, TS_TOP = 940, TS_BOT = 140;
//values after doing lcd clibration. you can upload the clibration example from one of three libraries used here and then copy paste the values here

bool halt = false;
String statement = "The Weight: ";
String pre;
String first = "RESET";
String second = "FREEZE";
String third = "HELD";
String thirdplus = "Color";
String fourth = "oz";
String fifth = "lb";
volatile int grams, lb, oz;
const long interval = 2000;//refresh interval time 2 sceconds
unsigned long previousMillis = 0;

uint16_t g_identifier;
int pixel_x, pixel_y;
MCUFRIEND_kbv tft(LCD_CS, LCD_CD, LCD_WR, LCD_RD, LCD_RESET); //display instance
TouchScreen ts = TouchScreen(XP, YP, XM, YM, 350); //Touchscren area declaration
TSPoint p; //Touch I/O instance

void setup() {
  tft.reset();

  pinMode(31, OUTPUT);//the state pin of hc 05 module
  digitalWrite(31, HIGH);//activate bluetooth module

  Serial.begin(9600);
  g_identifier = tft.readID();
  if (g_identifier == 0x00D3 || g_identifier == 0xD3D3) g_identifier = 0x9481; // write-only shield
  if (g_identifier == 0xFFFF) g_identifier = 0x9341; // serial
  tft.begin(g_identifier);//to enable ILI9327 driver code
  tft.setRotation(1); //rotate to landscape view
  BluetoothSerial.begin(9600);
  /////////////////////////////////////////////////////////////
  scale.begin(DT,SCK);//Initialising the library functions with the appropriate pins.
  Serial.println("Please wait 1 second after powering to apply weight.");//Since we are about to 'zero' the scale there must not be any weight applied to the scale
  //otherwise the readings will be offset by that amount.
  scale.set_scale(calibrationFactor);//Set the code to multiply the raw data by the calibration factor to convey accurate reading in grams.
  scale.tare();//Set the output readings to zero, hence no value in the brackets.
  /////////////////////////////////////////////////////////////
}

void loop() {

  scaleReading = scale.get_units();//The function get_units gets the data from the scale after it has been affected by the calibration factor and is not raw, this is
  //the final output reading, this is being saved as a value so that it can be used in the sending via bluetooth and display on the lcd.

  Serial.print("Weight: ");//For the sake of this code being used on it's own the next 3 lines display the reading from from the scale to the serial moniter.
  Serial.print(scaleReading);
  Serial.print("g");

  String buffer = String (scale.get_units());//wrapping into string, it prevents datum conversion error by buffering
  grams = buffer.toInt();//converts into integer
  oz = grams/28;//converts into ounce
  lb = oz/16; //converts into pound

  p = ts.getPoint();
  if (p.z > ts.pressureThreshhold) {
    pixel_x = map(p.y, TS_LEFT = 757, TS_RT = 104, 0, 400);// mapping the values
    pixel_y = map(p.x, TS_TOP = 142, TS_BOT = 858, 0, 240);
    if (pixel_x > -80 && pixel_x < 0 && pixel_y > 175 && pixel_y < 240) {
      pinMode(XM, OUTPUT);
      pinMode(YP, OUTPUT);
      //first touch
      grams = 0;
      pre = "";
      measure(RED, grams, "g");
    }
    if (pixel_x > 0 && pixel_x < 79 && pixel_y > 175 && pixel_y < 240) {
      pinMode(XM, OUTPUT);
      pinMode(YP, OUTPUT);
      //second touch
      if(halt == false)
      { halt = true;}
        else {halt = false;}
      measure(BLUE, grams, "g");
    }
    if (pixel_x > 80 && pixel_x < 159 && pixel_y > 175 && pixel_y < 240) {
      pinMode(XM, OUTPUT);
      pinMode(YP, OUTPUT);
      //third touch
      if (pre != NULL) {pre = pre;}
      else {pre = "previous measurement:"; pre = pre + grams;}
      measure(YELLOW, grams, "g");
    }
    if (pixel_x > 160 && pixel_x < 239 && pixel_y > 175 && pixel_y < 240) {
      pinMode(XM, OUTPUT);
      pinMode(YP, OUTPUT);
      //4th touch
      measure(WHITE, oz, "oz");
      delay(2000);
    }
    if (pixel_x > 240 && pixel_x < 319 && pixel_y > 175 && pixel_y < 240) {
      pinMode(XM, OUTPUT);
      pinMode(YP, OUTPUT);
      //5th touch
      measure(BLACK, lb, "lb");
      delay(2000);
    }
  }

  if (halt != true) {
    unsigned long currentMillis = millis();
    if (currentMillis - previousMillis >= interval) {
      previousMillis = currentMillis;//by this recursive if statement, it automatically refresh itself
      measure(GREEN, grams, "g");
    }
  }

  if (BluetoothSerial.available())
  {Serial.write(BluetoothSerial.read());}
  if(Serial.available())
  {BluetoothSerial.write(Serial.read());}
  //bluetooth connectivity declaration

  BluetoothSerial.println(grams);
  //Transmit weight datum by printing on bluetooth device.
}

void measure(int color, int data, String u) {
  int contrast;
  if (color == BLACK || color == RED || color == BLUE) 
  {contrast = WHITE;}
  else {contrast = BLACK;}
  tft.reset();
  tft.fillScreen(color);
  tft.setTextSize(2);
  tft.setTextColor(contrast);
  tft.setCursor(5, 5);
  tft.print("");
  tft.setCursor(50, 180);
  tft.print(pre);
  tft.setTextSize(3);
  tft.setCursor(50, 95);
  tft.print(statement + data + u);
  basetemplelate();
}

void basetemplelate() {
  tft.fillRect(80, 200, 80, 64, BLUE);//inside of frame
  tft.fillRect(0, 200, 80, 64, RED);
  tft.fillRect(160, 200, 80, 64, YELLOW);
  tft.fillRect(240, 200, 80, 64, CYAN);
  tft.fillRect(320, 200, 80, 64, MAGENTA);
  tft.setTextColor(BLACK);
  tft.setTextSize(2);
  tft.setCursor(175, 215);
  tft.print(third);
  tft.setTextSize(2);
  tft.setTextColor(BLACK);
  tft.setCursor(13, 215);
  tft.print(first);
  tft.setCursor(85, 215);
  tft.setTextColor(WHITE);
  tft.print(second);
  tft.setTextSize(3);
  tft.setCursor(265, 210);
  tft.print(fourth);
  tft.setCursor(345, 210);
  tft.print(fifth);
  tft.drawRect(80, 200, 80, 64, WHITE);//frame color
  tft.drawRect(0, 200, 80, 64, WHITE);
  tft.drawRect(160, 200, 80, 64, WHITE);
  tft.drawRect(240, 200, 80, 64, WHITE);
  tft.drawRect(320, 200, 80, 64, WHITE);
}
