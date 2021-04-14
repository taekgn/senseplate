# Senseplate Android Application
### Introduction
This is an android application, that interacts with senseplate microcontroller through bluetooth communication.  
Even though cell phone is not paired with microcontroller, it still can show default nutrient data (per 100g).
On the other hand, if it is paired with microcontroller then, ListView window will pop up and shows received data after bluetooth device selected.
However, this is a static application so have to press refresh button to synchronize data receiving.
### Comparison
|Name of Version|Feature|
|------|------|
|JH|Enhanced API, database and GUI (Latest Ver.) but only has essetial functions|
|Uni|Food detection by camera and shows nutrient data through website|
# Caution
James had worked on **Windows** and I had used **MacOS** so it might give SDK error message
when it was executed through Android Studio Emulator, Solution is changing local.properties file of sdk directory into unix or windows way vice-versa.  
### Example
```C
  sdk.dir=C\:\\Users\\james\\AppData\\Local\\Android\\Sdk
```
  into
```C
  sdk.dir=/Users/taek/Library/Android/sdk
```
On the other hand vice-versa way.
