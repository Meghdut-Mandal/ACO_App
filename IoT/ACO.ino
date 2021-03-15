//sketch created by Subhidh Agarwal
int inputByte;
int RelayPin = 6;
void setup() {
 Serial.begin(9600);
 pinMode(RelayPin, OUTPUT);
 digitalWrite(RelayPin,HIGH);
}

void loop() {
  
while(Serial.available()>0){
  inputByte= Serial.read();
  Serial.println(inputByte);
  if (inputByte==0){
  digitalWrite(RelayPin,LOW);
  }
  else if (inputByte==1){
    digitalWrite(RelayPin,HIGH);
  }
  }
}
