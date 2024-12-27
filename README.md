# Домашнее задание №11
## Вопросы для самоконтроля:
___________________________________________________________
### 1. Как получить ссылку на текущий поток ?
Ответ: Чтобы получить ссылку на текущий поток в Java, достаточно использовать статический метод currentThread() класса Thread
Пример :

```java
Thread currentThread = Thread.currentThread();
System.out.println("Current thread: " + currentThread.getName());
```
