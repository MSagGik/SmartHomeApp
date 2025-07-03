// библиотеки
#include <SPI.h>                // стандартная (https://github.com/arduino/ArduinoCore-avr/tree/master/libraries/SPI, Security policy)
#include <ArduinoJson.h>        // формат упаковки данных (https://github.com/bblanchon/ArduinoJson, MIT license)
#include <GyverBME280.h>        // сенсора BME280 (https://github.com/GyverLibs/GyverBME280, MIT license)
#include <DHT.h>                // сенсора DHT11 (https://github.com/adafruit/DHT-sensor-library, MIT license)
#include <GyverOLED.h>          // OLED экрана (https://github.com/GyverLibs/GyverOLED, MIT license)
#include <RtcDS1302.h>          // часов (https://github.com/Makuna/Rtc, LGPL-3.0 license)
#include <ThreeWire.h>          // вспомогательная для часов (https://github.com/Makuna/Rtc, LGPL-3.0 license)
#include <Adafruit_NeoPixel.h>  // светодиодной ленты (https://github.com/adafruit/Adafruit_NeoPixel, LGPL-3.0 license)
#include <SmartHome.h>          // разработанная

// пины
#define PIN_2 2  // часы DAT (ввод/вывод)
#define PIN_3 3  // часы CLK (кварцевый генератор)
#define PIN_4 4  // часы RST (сессия)
#define PIN_5 5  // датчик DHT11
#define PIN_6 6  // светодиодная лента
#define PIN_7 7  // динамик звуковых сигналов

// константы
#define DHT_SENSOR DHT11  // сенсор DHT11
#define NUMBER_LED 141    // количество светодиодов
const static uint8_t arrayImage[][8] PROGMEM = { // данные библиотеки GyverOLED (https://github.com/GyverLibs/GyverOLED, MIT license)
        { 0x1e, 0x3f, 0x7f, 0xfe, 0xfe, 0x7f, 0x3f, 0x1e },  // сердце (заполненное)
        { 0x1e, 0x21, 0x41, 0x82, 0x82, 0x41, 0x21, 0x1e },  // сердце (контур)
        { 0x10, 0x30, 0x70, 0xff, 0xff, 0x70, 0x30, 0x10 },  // стрелка вниз
        { 0x08, 0x0c, 0x0e, 0xff, 0xff, 0x0e, 0x0c, 0x08 },  // стрелка вверх
        { 0x06, 0x09, 0x09, 0x06, 0x78, 0x84, 0x84, 0x48 },  // градус Цельсия
        { 0xf8, 0x84, 0x82, 0x81, 0xb1, 0xb2, 0x84, 0xf8 },  // дом
        { 0x00, 0x42, 0x24, 0x18, 0xff, 0x99, 0x66, 0x00 },  // bluetooth
        { 0x18, 0x18, 0x18, 0x18, 0xff, 0x7e, 0x3c, 0x18 },  // стрелка вправо
        { 0xf0, 0xfe, 0xf1, 0x91, 0x91, 0xf1, 0xf2, 0xf0 }   // замок

};

// объекты
ThreeWire clockLink(PIN_2, PIN_3, PIN_4);                                // выводы часов DAT, CLK, RST
RtcDS1302<ThreeWire> clock(clockLink);                                   // модуль часов
GyverBME280 sensorBME280;                                                // сенсор BME280 (температура и давление)
DHT sensorDHT11(PIN_5, DHT_SENSOR);                                      // сенсор DHT11 (температура и влажность)
Adafruit_NeoPixel lightingLED(NUMBER_LED, PIN_6, NEO_GRB + NEO_KHZ800);  // светодиодная лента
GyverOLED<SSD1306_128x64, OLED_NO_BUFFER> monitorLED;                    // светодиодный монитор 128x64

// глобальные переменные
unsigned long previousFadeMillisActive = 0;
const unsigned long FADE_INTERVAL_ACTIVE = 16000;

unsigned long previousFadeMillisWatch = 0;
const unsigned long FADE_INTERVAL_WATCH = 1000;

unsigned long previousFadeMillisWeather = 0;
const unsigned long FADE_INTERVAL_WEATHER = 5000;

unsigned long previousFadeMillisLED = 0;
bool isFadingLED = false;
byte stepColorLED = 0;
const unsigned long FADE_INTERVAL_LED = 50; // задержка между шагами (для poly композиции)

const byte NUM_COLORS = 5;             // количество цветов (для poly композиции)
const byte COLOR_SIZE = 3;             // R, G, B (для poly композиции)
const byte LED_STEPS = 50;             // количество шагов плавного перехода (для poly композиции)
static byte ledState[17] = { 0 };      // массив параметров светодиодной ленты (mode, alfa, red_1, green_1, blue_1, ..., red_5, green_5, blue_5) mode = 0 - mono, >0 - poly
byte currentColorIndex = 0;
byte nextColorIndex = 1;

const byte BUFFER_SIZE = 128;
char inputBuffer[BUFFER_SIZE];         // буфер для хранения входящих данных
byte bufferIndex = 0;
byte stateCom = 0;                  // состояние включения/выключения получения данных от сенсоров

// метод выполняемый на старте и финише работы контроллера
void setup() {
    Serial.begin(9600);  // запуск порта
    // загрузка даты и времени с ПК при прошивке
    clock.Begin();  // инициализация часов
    // сенсоры
    sensorBME280.begin();  // инициализация сенсора (температура и давление)
    sensorDHT11.begin();   // инициализация сенсора (температура и влажность)
    // светодиодная лента
    lightingLED.begin();                 // инициализируем ленту
    lightingLED.setBrightness(ledState[1]);  // указываем яркость светодиодов (максимум 255)
    // звуковой вывод
    pinMode(PIN_7, OUTPUT);
    // светодиодный экран
    monitorLED.init();       // инициализация
    monitorLED.clear();      // очистка
    monitorLED.setScale(1);  // масштаб текста (1..4)
    monitorLED.home();       // курсор в 0,0
    monitorLED.print(F("Умный дом 1.0 "));
    drawImage(5);
    monitorLED.setCursor(0, 3);  // перевод курсора на 3 строку
    monitorLED.println(F("Данные температуры,"));
    monitorLED.println(F("давления и влажности"));
    monitorLED.clear();  // очистка
    previousFadeMillisActive = millis();
}

// цикличный метод запускаемый после метода setup()
void loop() {
    readSerialData();
    if (isFadingLED) {
        handleFadeLED();
    }
    handleFadeWatchAndWeather();
}

void yield() {
    readSerialData();
}

void handleFadeLED() {
    unsigned long currentMillis = millis();
    if (currentMillis - previousFadeMillisLED >= FADE_INTERVAL_LED) {
        previousFadeMillisLED = currentMillis;
        setLedColorPoly();
    }
}

void handleFadeWatchAndWeather() {
    unsigned long currentMillis = millis();
    if (currentMillis - previousFadeMillisWatch >= FADE_INTERVAL_WATCH) {
        previousFadeMillisWatch = currentMillis;
        // watch
        RtcDateTime updateTime;
        char time[10];
        updateTime = clock.GetDateTime();
        monitorLED.setScale(2);  // масштаб текста (1..4)
        monitorLED.home();       // курсор в 0,0
        sprintf(time, "%02d:%02d:%02d", updateTime.Hour(), updateTime.Minute(), updateTime.Second());
        monitorLED.print(time);
        if (updateTime.Minute() == 0 && updateTime.Second() == 0) outputSoundOne(PIN_7, 180);  // запуск мелодии на динамике в начале часа
        monitorLED.setScale(1);
        monitorLED.setCursor(109, 0);
        monitorLED.print(updateTime.Day());
        switch (updateTime.Month()) {
            case 1:
                monitorLED.setCursor(107, 1);
                monitorLED.print(F("Янв"));
                break;
            case 2:
                monitorLED.setCursor(107, 1);
                monitorLED.print(F("Фев"));
                break;
            case 3:
                monitorLED.setCursor(99, 1);
                monitorLED.print(F("Марта"));
                break;
            case 4:
                monitorLED.setCursor(107, 1);
                monitorLED.print(F("Апр"));
                break;
            case 5:
                monitorLED.setCursor(107, 1);
                monitorLED.print(F("Мая"));
                break;
            case 6:
                monitorLED.setCursor(99, 1);
                monitorLED.print(F("Июня"));
                break;
            case 7:
                monitorLED.setCursor(99, 1);
                monitorLED.print(F("Июля"));
                break;
            case 8:
                monitorLED.setCursor(107, 1);
                monitorLED.print(F("Авг"));
                break;
            case 9:
                monitorLED.setCursor(107, 1);
                monitorLED.print(F("Сен"));
                break;
            case 10:
                monitorLED.setCursor(107, 1);
                monitorLED.print(F("Окт"));
                break;
            case 11:
                monitorLED.setCursor(107, 1);
                monitorLED.print(F("Ноя"));
                break;
            case 12:
                monitorLED.setCursor(107, 1);
                monitorLED.print(F("Дек"));
                break;
        }
        monitorLED.setCursor(102, 2);
        monitorLED.print(updateTime.Year());
        // weather
        if (currentMillis - previousFadeMillisWeather >= FADE_INTERVAL_WEATHER) {
            previousFadeMillisWeather = currentMillis;
            monitorLED.setScale(1);
            monitorLED.setCursor(0, 2);
            float temperatureBME280 = sensorBME280.readTemperature();
            float temperatureDHT11 = sensorDHT11.readTemperature();
            float temperatureMedian = (temperatureBME280 + temperatureDHT11) / 2;
            float humidityDHT11 = sensorDHT11.readHumidity();
            float pressurePascalBME280 = sensorBME280.readPressure();
            float pressureToMmHgBME280 = pressureToMmHg(pressurePascalBME280);
            monitorLED.print(F("Метеоданные"));
            monitorLED.setCursor(0, 4);
            monitorLED.setScale(1);
            monitorLED.print(temperatureMedian);
            drawImage(4);
            monitorLED.print(F(" "));
            drawState(temperatureMedian, 18, 24);
            monitorLED.print(F(" "));
            monitorLED.print(humidityDHT11);
            monitorLED.print(F("% "));
            drawState(humidityDHT11, 40, 60);
            monitorLED.setCursor(0, 6);
            monitorLED.print(pressureToMmHgBME280);
            monitorLED.print(F(" мм.р.ст. "));
            drawState(pressureToMmHgBME280, 747, 760);
            monitorLED.print(F(" "));
            drawImage(6);
            if (stateCom == 1) {
                drawImage(7);
                sendSmartHomeData(temperatureMedian, humidityDHT11, pressureToMmHgBME280);
            } else {
                drawImage(8);
            }
        }
    }
}

// функция для отправки данных сенсоров и освещения в формате JSON
void sendSmartHomeData(float temperature, float humidity, float pressure) {
    StaticJsonDocument<256> doc;
    doc["type"] = "SMART_HOME_DATA";
    doc["temperature"] = temperature;
    doc["humidity"] = humidity;
    doc["pressure"] = pressure;
    doc["led_m"] = ledState[0];
    doc["led_a"] = ledState[1];
    doc["led_r"] = ledState[2];
    doc["led_g"] = ledState[3];
    doc["led_b"] = ledState[4];
    serializeJson(doc, Serial);
    Serial.println();  // добавление разделителя (новая строка)
}

// функция для чтения данных из Serial порта
void readSerialData() {
    while (Serial.available() > 0) {
        char incomingChar = Serial.read();
        if (incomingChar == '\n') {     // если получен символ новой строки
            inputBuffer[bufferIndex] = '\0';  // завершить строку
            processCommand(inputBuffer);
            bufferIndex = 0; // сбросить индекс
        } else {
            if (bufferIndex < BUFFER_SIZE - 1) {
                inputBuffer[bufferIndex++] = incomingChar;
            } else {
                // переполнение буфера — можно сбросить или обработать как ошибку
                bufferIndex = 0;
            }
        }
        previousFadeMillisActive = millis();
        stateCom = 1;
    }
    if (millis() - previousFadeMillisActive >= FADE_INTERVAL_ACTIVE) {
        stateCom = 0;
    }
}

// функция для обработки полученной команды
void processCommand(const char* command) {
    StaticJsonDocument<256> doc;
    DeserializationError error = deserializeJson(doc, command);

    if (error) {
        Serial.print(F("deserializeJson() failed: "));
        Serial.println(error.c_str());
        return;
    }

    const char *type = doc["type"];

    if (strcmp(type, "LED_ALPHA") == 0) {
        ledState[1] = doc["a"].as<byte>();
        setLedColorAlpha();
    } else if (strcmp(type, "LED_MONO") == 0) {
        ledState[0] = doc["m"].as<byte>();
        ledState[1] = doc["a"].as<byte>();
        ledState[2] = doc["r"].as<byte>();
        ledState[3] = doc["g"].as<byte>();
        ledState[4] = doc["b"].as<byte>();
        checkStateLED();
    } else if (strcmp(type, "LED_FLOW_A") == 0) {
        ledState[0] = 0;
        ledState[1] = doc["a"].as<byte>();
        ledState[2] = doc["r0"].as<byte>();
        ledState[3] = doc["g0"].as<byte>();
        ledState[4] = doc["b0"].as<byte>();
        ledState[5] = doc["r1"].as<byte>();
        setLedColorMono();
    } else if (strcmp(type, "LED_FLOW_B") == 0) {
        ledState[6] = doc["g1"].as<byte>();
        ledState[7] = doc["b1"].as<byte>();
        ledState[8] = doc["r2"].as<byte>();
        ledState[9] = doc["g2"].as<byte>();
        ledState[10] = doc["b2"].as<byte>();
    } else if (strcmp(type, "LED_FLOW_C") == 0) {
        ledState[11] = doc["r3"].as<byte>();
        ledState[12] = doc["g3"].as<byte>();
        ledState[13] = doc["b3"].as<byte>();
        ledState[14] = doc["r4"].as<byte>();
        ledState[15] = doc["g4"].as<byte>();
    } else if (strcmp(type, "LED_FLOW_FIN") == 0) {
        ledState[16] = doc["b4"].as<byte>();
        ledState[0] = doc["m"].as<byte>();
        checkStateLED();
    } else if (strcmp(type, "TIME_UPDATE") == 0) {
        long epoch = doc["epoch"];
        setTime(epoch);
    } else if (strcmp(type, "COM_ENABLED") == 0) {
        byte state = doc["state_com"].as<byte>(); // 1 - вкл., 0 - выкл.
        stateCom = state;
    }
}

void checkStateLED() {
    if (ledState[0] > 0) {
        isFadingLED = true;
    } else {
        isFadingLED = false;
        setLedColorMono();
    }
}

void setLedColorAlpha() {
    lightingLED.setBrightness(ledState[1]);
    lightingLED.show();
}

void setLedColorMono() {
    lightingLED.clear();
    lightingLED.setBrightness(ledState[1]);
    for (byte i = 0; i < NUMBER_LED; i++) {
        lightingLED.setPixelColor(i, lightingLED.Color(ledState[2], ledState[3], ledState[4]));
    }
    lightingLED.show();
}

void setLedColorPoly() {
    byte r_start = ledState[2 + currentColorIndex * COLOR_SIZE];
    byte g_start = ledState[3 + currentColorIndex * COLOR_SIZE];
    byte b_start = ledState[4 + currentColorIndex * COLOR_SIZE];

    byte r_finish = ledState[2 + nextColorIndex * COLOR_SIZE];
    byte g_finish = ledState[3 + nextColorIndex * COLOR_SIZE];
    byte b_finish = ledState[4 + nextColorIndex * COLOR_SIZE];

    // Интерполяция цвета
    byte r = r_start + ((long)(r_finish - r_start) * stepColorLED) / LED_STEPS;
    byte g = g_start + ((long)(g_finish - g_start) * stepColorLED) / LED_STEPS;
    byte b = b_start + ((long)(b_finish - b_start) * stepColorLED) / LED_STEPS;

    lightingLED.clear();
    lightingLED.setBrightness(ledState[1]);
    for (byte i = 0; i < NUMBER_LED; i++) {
        lightingLED.setPixelColor(i, lightingLED.Color(r, g, b));
    }
    lightingLED.show();
    stepColorLED++;
    if (stepColorLED >= LED_STEPS) {
        stepColorLED = 0;
        currentColorIndex = nextColorIndex;
        nextColorIndex = (nextColorIndex + 1) % NUM_COLORS;
    }
}

void setTime(long epoch) {
    RtcDateTime newTime;
    newTime.InitWithEpoch32Time(epoch);
    clock.SetDateTime(newTime);
}

void drawState(float value, int min, int max) {
    drawImage(value > max ? 3 : (value < min ? 2 : 0));
}

void drawImage(byte index) {
    size_t s = sizeof arrayImage[index];
    for (unsigned int i = 0; i < s; i++) {
        monitorLED.drawByte(pgm_read_byte(&(arrayImage[index][i])));
    }
}