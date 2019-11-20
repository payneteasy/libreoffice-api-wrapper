# libreoffice-api-wrapper
Java wrapper of Libreoffice API

## Параметры http-сервера:
* ```SERVER_PORT``` - порт, на который будут приниматься входящие HTTP запросы. По умолчанию ```8080```
* ```SERVER_SERVLET_CONTEXT``` - контекст сервера приложений. По умолчанию ```/converter```
* ```SERVER_SERVLET_PATH``` - путь запроса для конвертации документов. По умочанию ```/convert```

## Параметры сервиса libreoffice
* ```LIBREOFFICE_HOST``` - адрес хоста, на котором разрвернут сервис libreoffice. По умолчанию ```localhost```
* ```LIBREOFFICE_PORT``` - порт, на который сервис libreoffice принимает соединения. По умолчанию ```8100```

Пераметры http-сервера и сервиса libreoffice можно задать, например, через переменные среды. Также возможно задать параметры через system properties.

## Пример команды запуска сервера
```bash
java -jar libreoffice-api-wrapper-server-1.0-jar-with-dependencies.jar
```
```bash
java -DSERVER_PORT=8081 -jar libreoffice-api-wrapper-server-1.0-jar-with-dependencies.jar
```

## Формат http запроса для конвертации документов
* Метод запроса - ```POST```
* Параметр ```inputFormat``` - формат входного документа (поддерживаемые форматы: ```csv```,```doc```,```docx```,```xls```,```xlsx```)
* Параметр ```outputFormat``` - формат выходного документа (поддерживаемые форматы: ```pdf```)
* В теле запроса передается содержимое документа для конвертации
