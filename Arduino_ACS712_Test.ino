/*
  Analog input, analog output, serial output
 
 Reads an analog input pin, maps the result to a range from 0 to 255
 and uses the result to set the pulsewidth modulation (PWM) of an output pin.
 Also prints the results to the serial monitor.
 
 The circuit:
 * potentiometer connected to analog pin 0.
   Center pin of the potentiometer goes to the analog pin.
   side pins of the potentiometer go to +5V and ground
 * LED connected from digital pin 9 to ground
 
 created 29 Dec. 2008
 modified 9 Apr 2012
 by Tom Igoe
 
 This example code is in the public domain.
 
 */

// These constants won't change.  They're used to give names
// to the pins used:
const int analogInPin = A0;  // Analog input pin that the potentiometer is attached to

int sensorValue = 0;        // value read from the pot

long val = 0;

int count,id = 0;
int pin = 13;

void setup() {
  // initialize serial communications at 9600 bps:
  Serial.begin(115200); 
  pinMode(13, INPUT);
}

void loop() {
    
  if(count == 0 && digitalRead(13)){
    count = 1;
    Serial.print("D");
    Serial.println(id);    
  }else if(count == 1 && digitalRead(13) == 0){
    count = 0;
    Serial.print("D");
    Serial.println(id);
    id++;
  }

  val = analogRead(analogInPin);
  for(int i = 0; i< 127; i++){
    delayMicroseconds(100);
    val += analogRead(analogInPin);                
  }
  val = val >> 7;
  
  sensorValue = val;
  
  if(sensorValue >= 770){
    sensorValue = 0;
  }else{
    sensorValue = 770 - sensorValue;
  }
  
  sensorValue = (30*(415-sensorValue))/65 + sensorValue;
    
  Serial.print("$");      
  Serial.println(sensorValue);
  
  //delay(100);                     
}
