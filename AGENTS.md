# LIBLANKA-BE KNOWLEDGE BASE

## OVERVIEW

Spring Boot 3.3.4 / Java 21 REST API. Domain-driven structure. JWT auth + Spring Security. PostgreSQL (schema `liblanka`) + Liquibase migrations. Port `7890`, context `/api/v1/`.

## STRUCTURE

```
src/main/java/me/astroreen/liblanka/
├── domain/
│   ├── auth/           # JWT-аутентификация, Spring Security, User management
│   │   ├── config/     # SecurityConfiguration, JwtAuthenticationFilter, ApplicationConfig
│   │   ├── controller/ # AuthenticationController, UserController
│   │   ├── service/    # AuthenticationService, JwtService, UserService
│   │   ├── repository/ # UserRepository
│   │   ├── entity/     # User.java
│   │   ├── dto/        # Request/Response DTOs
│   │   ├── exception/  # Domain exceptions
│   │   └── UserRole.java
│   └── product/        # Основная бизнес-логика (каталог товаров)
│       ├── controller/ # ProductController, ProductColorController, ProductImageController, ...
│       ├── service/    # ProductService, ProductImageService, ...
│       ├── repository/ # JPA репозитории + Specifications
│       ├── entity/     # Product, ProductVariant, ProductColor, ProductSize, ProductType, ProductImage
│       ├── dto/        # DTO классы для каждой сущности
│       ├── util/       # ColorValidator
│       └── entity/specifications/  # ProductSpecifications (JPA Criteria)
└── LiblankaApplication.java
src/main/resources/
├── application.yaml        # Единственный конфиг, env-переменные через ${VAR}
└── db/changelog/
    ├── main-changelog.xml  # Liquibase root changelog
    ├── product/            # Product DDL changesets
    └── auth/               # Auth DDL changesets
```

## DOMAIN LAYER PATTERN

Каждый домен изолирован: `controller → service → repository → entity + dto`. Никаких cross-domain зависимостей через репозитории — только через сервисы.

## WHERE TO LOOK

| Task                 | Location                                                          |
| -------------------- | ----------------------------------------------------------------- |
| Добавить endpoint    | `domain/{name}/controller/`                                       |
| Бизнес-логика        | `domain/{name}/service/`                                          |
| DB запросы           | `domain/{name}/repository/`                                       |
| Новая таблица        | `resources/db/changelog/{domain}/` + новый entity                 |
| Security правила     | `domain/auth/config/SecurityConfiguration.java`                   |
| JWT логика           | `domain/auth/service/JwtService.java`                             |
| Фильтрация продуктов | `domain/product/entity/specifications/ProductSpecifications.java` |

## KEY DEPENDENCIES

- **Lombok** — `@Data`, `@Builder`, `@AllArgsConstructor` везде на entities и DTOs
- **dotenv-java** — `.env` файл читается автоматически при старте, переменные доступны через `${VAR}` в application.yaml
- **Liquibase** — DDL только через changesets, `ddl-auto: none`, никакого `create`/`update`
- **springdoc** — OpenAPI UI доступен на `/api/v1/swagger-ui/index.html`
- **webp-imageio** — конвертация изображений в WebP в `ProductImageService`
- **jjwt 0.12.6** — JWT токены

## CONVENTIONS

- DTOs — отдельный класс per use-case (не переиспользовать Request для Response)
- Exceptions — кастомные классы в `domain/{name}/exception/`, не бросать голые RuntimeException
- Конфиг через env vars: `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`, `POSTGRES_HOST` — обязательны в `.env`
- Единственный `application.yaml` (нет profile-specific файлов кроме `local`)

## ANTI-PATTERNS

- Не использовать `ddl-auto: create/update` — только Liquibase
- Не делать cross-domain вызовы через Repository — только через Service
- Не хранить секреты в application.yaml — только `${ENV_VAR}`
- Не использовать `bin/` директорию — это скомпилированный output Gradle

## COMMANDS

```bash
./gradlew bootRun --args='--spring.profiles.active=local'   # dev запуск
./gradlew build                                              # сборка + тесты
./gradlew test                                               # только тесты
./gradlew bootJar                                            # fat JAR
```

## NOTES

- `GRADLE_USER_HOME` установлен в `/tmp/gradle-*-cache` через devenv, не в `~/.gradle`
- `start-linux.sh` в корне BE — альтернативный скрипт запуска
- Тесты в `src/test/` используют Spring Security Test + Mockito
