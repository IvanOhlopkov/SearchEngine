# Search Engine

___
### Данный поисковый движок позволяет искать информацию на страницах заранее указанных сайтов по словам, введенными в поле поиска (Search) 


![Screenshoot of dashboard search engine](https://github.com/IvanOhlopkov/images/blob/main/img/searchengine.PNG)

![Screenshoot of management page](https://github.com/IvanOhlopkov/images/blob/main/img/management.PNG)

![Screenshoot of search page](https://github.com/IvanOhlopkov/images/blob/main/img/search.PNG)

### Стек используемых технологий:
- Java
- Spring Boot
- Hibernate
- MySql
- HTML/JS/CSS/Thymeleaf

***
### Инструкция по локальному запуску проекта
1. Склонируйте проект с помощью IDE
2. Установите MySql Server, настройте сервер на порт на 3306 и запустите его:
   * либо используйте Docker, перейдите с помощью терминала в папку с проектом, далее docker и запустите команду **docker-compose up -d**
3. Дождитесь загрузки всех зависимостей из pom.xml
4. Отредактируйте используемые сайты в файле application.yaml в графе *indexing-settings - sites*
5. Добавьте JAR-файлы лемматизатора (В IntelliJ IDEA > Project structure > Modules > + > в корне проекта всю папку lemmatizator > Ok)
6. Запустите Application.java
7. В браузере откройте https://localhost:8080