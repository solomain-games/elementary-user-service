# Elementary — сервис пользователей (user-service)

Пользователи игры **Elementary**: гостевой вход, выпуск JWT, позже регистрация, профиль, статистика и инвентарь.

Токены подписываются закрытым ключом по алгоритму RS256, открытый ключ публикуется для остальных сервисов (см. [ADR 0003](https://github.com/solomain-games/elementary-docs/blob/main/adr/0003-jwt-rs256-user-service.md)).

## Запуск локально

Требуется JDK 25.

```powershell
.\mvnw spring-boot:run
```

Сервис стартует на порту **8082** с профилем `local`.

Проверка: http://localhost:8082/actuator/health → `"status": "UP"`.

## Профили

| Профиль | Где | Настройки |
|---|---|---|
| `local` (по умолчанию) | компьютер разработчика | `application-local.yaml` |
| `prod` | боевой сервер | `application-prod.yaml`, значения из переменных окружения |

Профиль задаётся переменной `SPRING_PROFILES_ACTIVE`.

## Сборка и тесты

```powershell
.\mvnw verify
```
