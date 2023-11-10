# Неделя 5 домашнее задание по String-JSON

Это домашнее задание по теме GIT от компании Clevertec :clap:
---
___Содержание:___
* Используемый стек.
* Описание.
___     

# Используемый стек и библиотеки.
1. IntelliJ IDEA Community Edition (для проверки итоговых ветвлений)
2. GitHub
3. AssertJ
4. GSON
5. Группа сокурсников в Telegram
___

# Описание.

В интерфейсе CustomParser реализовано 3 основных метода:
 - serialize(Object o) - производит сериализацию объекта.
 - beautifyOneLineString(String jsonString, int howManySpaces) - выводит JSON в "красивом" виде.
 - deserialize(String jsonString, Class<T> clazz) - производит десериализацию объекта.
 - тесты находятся здесь: com/gmail/kovalev/util/CustomParserImplTest.java

Пояснения по каждому методу:

> **serialize(Object o)**
> - сериализует объекты с полями следующих типов: кастомные классы, "Double", "double", "Integer", "int", "Boolean", "boolean", "long", "Long", "UUID", "String", "OffsetDateTime", "LocalDate", а также Array, List.
> - для полей других типов нужно их добавить в метод valueByFieldType(String typeName, Object fieldObject) - строка 53, и добавить реализацию парсинга. 
> - возвращает значение одной строкой вида: 
> ~~~
> {"id":"550e3418-8d40-4994-ae5d-849fe2bfc137","skills":[{"nameOfSkill":"Run","description":"Fast running"},{"nameOfSkill":null,"description":"Deep sleeping"}],"firstName":"Sergey","lastName":"Kovalev","dateBirth":"1982-12-21","orders":[{"id":"01779e09-701b-4433-b684-14687b2b62f9","products":[{"id":"b12977a5-c32f-494c-a332-0494031139af","name":"Milk","price":5.11},{"id":"32770a34-424d-415e-9b36-91e18befffcf","name":"Bread","price":2.22}],"createDate":"2023-11-10T18:33:14.524564800+03:00"}],"isWeird":true,"bonusCard":null}
> ~~~

> **beautifyOneLineString(String jsonString, int howManySpaces)**
> - позволяет получить форматированную строку JSON из строки количество пробелов - второй параметр метода.
> - возвращает JSON вида(при howManySpaces = 4):
> ~~~
> {
>    "id": "550e3418-8d40-4994-ae5d-849fe2bfc137",
>    "skills": [
>        {
>            "nameOfSkill": "Run",
>            "description": "Fast running"
>        },
>        {
>            "nameOfSkill": null,
>            "description": "Deep sleeping"
>        }
>    ],
>    "firstName": "Sergey",
>    "lastName": "Kovalev",
>    "dateBirth": "1982-12-21",
>    "orders": [
>        {
>            "id": "01779e09-701b-4433-b684-14687b2b62f9",
>            "products": [
>                {
>                    "id": "b12977a5-c32f-494c-a332-0494031139af",
>                    "name": "Milk",
>                    "price": 5.11
>                },
>                {
>                    "id": "32770a34-424d-415e-9b36-91e18befffcf",
>                    "name": "Bread",
>                    "price": 2.22
>                }
>            ],
>            "createDate": "2023-11-10T18:33:14.524564800+03:00"
>        }
>    ],
>    "isWeird": true,
>    "bonusCard": null
> }
> ~~~
> **deserialize(String jsonString, Class<T> clazz)**
> - процесс десериализации основан на LL-парсинге (узнал новое сокращение :smile:)
> - во время десериализации процесс наглядно виден в консоли после слов "LL parsing begin:"
> - вторым параметром принимает класс объекта, который он будет возвращать.
> - десериализует объекты с полями следующих типов: кастомные классы, "Double", "double", "Integer", "int", "Boolean", "boolean", "long", "Long", "UUID", "String", "OffsetDateTime", "LocalDate", а также Array, List.
> - возвращает десериализованный класс... Пример этого класса toString():
> ~~~
> Customer(id=550e3418-8d40-4994-ae5d-849fe2bfc137, skills=[Skill(nameOfSkill=Run, description=Fast running), Skill(nameOfSkill=null, description=Deep sleeping)], firstName=Sergey, lastName=Kovalev, dateBirth=1982-12-21, orders=[Order(id=01779e09-701b-4433-b684-14687b2b62f9, products=[Product(id=b12977a5-c32f-494c-a332-0494031139af, name=Milk, price=5.11), Product(id=32770a34-424d-415e-9b36-91e18befffcf, name=Bread, price=2.22)], createDate=2023-11-10T18:33:14.524564800+03:00)], isWeird=true, bonusCard=null)
> > ~~~

* PS: все примеры наглядно можно увидеть в консоли после запуска Main класса. 

###### CПАСИБО ЗА ВНИМАНИЕ !!!
