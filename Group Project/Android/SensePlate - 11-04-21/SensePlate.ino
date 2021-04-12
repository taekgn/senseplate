#include "SoftwareSerial.h" //RF communication library
#include "SPI.h" //serial peripheral library
#include "Wire.h"
#include "Adafruit_GFX.h" // main graphics library
#include "MCUFRIEND_kbv.h"  // main tft library
#include "TouchScreen.h" // touch I/O library

////////////////////////////////////////////////////////
#include "HX711.h"

#define calibration_factor //USE FACTOR FOUND FROM CALIBRATION CODE

#define LOADCELL_DOUT_PIN  3
#define LOADCELL_SCK_PIN  2

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

#define BT_SERIAL_RX_DIO 10
#define BT_SERIAL_TX_DIO 11
SoftwareSerial BluetoothSerial(BT_SERIAL_RX_DIO, BT_SERIAL_TX_DIO); // RX, TX

//PIN and Resolution declaration
const int XP = 6, XM = A2, YP = A1, YM = 7; //320x480 ID=0x9488
int TS_LEFT = 920, TS_RT = 90, TS_TOP = 940, TS_BOT = 140;
//values after doing lcd clibration. you can upload the clibration example from one of three libraries used here and then copy paste the values here

bool halt = false;
String statement = "The Weight:";
String pair, pre;
String first = "RESET";
String second = "FREEZE";
String third = "HELD";
String thirdplus = "Color";
String fourth = "oz";
String fifth = "lb";
volatile float kg;
volatile float lb, oz;
const long interval = 1000;
unsigned long previousMillis = 0;

uint16_t g_identifier;
int pixel_x, pixel_y;
MCUFRIEND_kbv tft(LCD_CS, LCD_CD, LCD_WR, LCD_RD, LCD_RESET); //display instance
TouchScreen ts = TouchScreen(XP, YP, XM, YM, 300); //Touchscren area declaration
TSPoint p; //Touch I/O instance

void setup() {
  tft.reset();
  Serial.begin(9600);
  
  /////////////////////////////////////////////////////////////
  scale.begin(LOADCELL_DOUT_PIN, LOADCELL_SCK_PIN);
  scale.set_scale(calibration_factor); //This value is obtained by using the SparkFun_HX711_Calibration sketch
  scale.tare(); //Assuming there is no weight on the scale at start up, reset the scale to 0
  /////////////////////////////////////////////////////////////
  
  //BluetoothSerial.begin(9600);
  if (BluetoothSerial.available())
  { Serial.write(BluetoothSerial.read());
    pair = "It Paired!"; BluetoothSerial.println(kg);
  }
  else {
    pair = "Not Paired!";
  }
  if (Serial.available())
    BluetoothSerial.write(Serial.read());

  g_identifier = tft.readID();
  if (g_identifier == 0x00D3 || g_identifier == 0xD3D3) g_identifier = 0x9481; // write-only shield
  if (g_identifier == 0xFFFF) g_identifier = 0x9341; // serial
  tft.begin(g_identifier);//to enable ILI9327 driver code
  tft.setRotation(1); //rotate to landscape view
}

void loop() {
  kg = scale.get_units();//random(0, 10);
  lb = kg * 2.20;
  oz = lb * 16;
  p = ts.getPoint();
  if (halt != true) {
    unsigned long currentMillis = millis();
    if (currentMillis - previousMillis >= interval) {
      previousMillis = currentMillis;
      measure(GREEN, kg, "kg");
    }
  }

  if (p.z > ts.pressureThreshhold) {
    pixel_x = map(p.y, TS_LEFT = 757, TS_RT = 104, 0, 400);// mapping the values
    pixel_y = map(p.x, TS_TOP = 142, TS_BOT = 858, 0, 240);
    if (pixel_x > -80 && pixel_x < 0 && pixel_y > 175 && pixel_y < 240) {
      pinMode(XM, OUTPUT);
      pinMode(YP, OUTPUT);
      //first touch
      kg = 0.00;
      pre = "";
      measure(RED, kg, "kg");
    }
    if (pixel_x > 0 && pixel_x < 79 && pixel_y > 175 && pixel_y < 240) {
      pinMode(XM, OUTPUT);
      pinMode(YP, OUTPUT);
      //second touch
      if(halt == false){
        halt = true;}
        else{halt = false;}
      measure(BLUE, kg, "kg");
    }
    if (pixel_x > 80 && pixel_x < 159 && pixel_y > 175 && pixel_y < 240) {
      pinMode(XM, OUTPUT);
      pinMode(YP, OUTPUT);
      //third touch
      if (pre != NULL) {
        pre = pre;
      }
      else {
        pre = "previous measurement:";
        pre = pre + kg;
      }
      measure(YELLOW, kg, "kg");
    }
    if (pixel_x > 160 && pixel_x < 239 && pixel_y > 175 && pixel_y < 240) {
      pinMode(XM, OUTPUT);
      pinMode(YP, OUTPUT);
      //third touch
      measure(WHITE, oz, "oz");
      delay(1500);
    }
    if (pixel_x > 240 && pixel_x < 319 && pixel_y > 175 && pixel_y < 240) {
      pinMode(XM, OUTPUT);
      pinMode(YP, OUTPUT);
      //third touch
      measure(BLACK, lb, "lb");
      delay(1500);
    }
  }
}

void measure(int color, float data, String u) {
  int contrast;
  if (color == BLACK || color == RED || color == BLUE) {
    contrast = WHITE;
  }
  else {
    contrast = BLACK;
  }
  tft.reset();
  tft.fillScreen(color);
  tft.setTextSize(2);
  tft.setTextColor(contrast);
  tft.setCursor(5, 5);
  tft.print(pair);
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
  tft.setCursor(88, 215);
  tft.print(second);
  tft.setTextSize(3);
  tft.setTextColor(WHITE);
  tft.setCursor(265, 210);
  tft.print(fourth);
  tft.setTextColor(WHITE);
  tft.setCursor(345, 210);
  tft.print(fifth);
  tft.drawRect(80, 200, 80, 64, WHITE);//frame color
  tft.drawRect(0, 200, 80, 64, WHITE);
  tft.drawRect(160, 200, 80, 64, WHITE);
  tft.drawRect(240, 200, 80, 64, WHITE);
  tft.drawRect(320, 200, 80, 64, WHITE);
  //tft.begin(g_identifier);
}
