# Elementary — сервис пользователей (user-service)

Пользователи игры **Elementary**: гостевой вход, выпуск JWT, позже регистрация, профиль, статистика и инвентарь.

Токены подписываются закрытым ключом по алгоритму RS256, открытый ключ публикуется для остальных сервисов (см. [ADR 0003](https://github.com/solomain-games/elementary-docs/blob/main/adr/0003-jwt-rs256-user-service.md)).

## Ключи JWT

Токены подписываются закрытым RSA-ключом (ADR 0003). Ключи хранятся **вне репозитория**: локально — в папке `secrets/` (она в `.gitignore`), на сервере — в файлах, пути к которым задают переменные `JWT_PRIVATE_KEY_LOCATION` и `JWT_PUBLIC_KEY_LOCATION`.

Создать пару ключей для разработки (OpenSSL входит в Git для Windows — используй Git Bash или полный путь `"C:\Program Files\Git\usr\bin\openssl.exe"`):

```bash
mkdir -p secrets
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out secrets/jwt-private.pem
openssl rsa -in secrets/jwt-private.pem -pubout -out secrets/jwt-public.pem
```

В тестах используются отдельные тестовые ключи из `src/test/resources/test-keys` (профиль `test`).

## API

| Запрос | Описание |
|---|---|
| `POST /api/auth/guest` `{ "name": "Анна" }` | гостевой вход: JWT на 24 часа (см. `docs/protocol.md`, раздел 3) |
| `GET /.well-known/jwks.json` | открытый ключ для проверки подписи токенов другими сервисами (JWKS) |

Примеры запросов для IntelliJ IDEA — в `http/auth.http`.

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
