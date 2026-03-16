# Prompt: English Learning Platform for IT Professionals

> Полноценный промпт / PRD для пошаговой разработки приложения с AI-ассистентом.
> Родной язык пользователей — русский. Целевая аудитория — IT-специалисты (разработчики, QA, DevOps, аналитики).

---

## 1. Product Vision

Создай веб-приложение **"DevLingo"** — платформу для изучения английского языка, заточенную под IT-специалистов с родным русским языком.

Ключевые принципы:
- Контент генерируется и адаптируется нейронной моделью (LLM) на основе загруженных администратором материалов
- Программы обучения автоматически подстраиваются под уровень пользователя (A1–C2)
- Практика строится вокруг реальных IT-контекстов: код-ревью, документация, митинги, письма, собеседования
- Прогресс отслеживается в личном кабинете с аналитикой

---

## 2. User Roles & Personas

### 2.1 Student (обычный пользователь)
- Регистрация / вход (email + password, OAuth2 через GitHub/Google опционально)
- Прохождение входного теста для определения уровня (placement test)
- Доступ к автосгенерированным учебным программам
- Прохождение уроков и практических занятий
- Личный кабинет с прогрессом и статистикой
- Настройки профиля (имя, аватар, целевой уровень, IT-специализация)

### 2.2 Admin
- Панель управления пользователями (список, поиск, фильтрация, блокировка)
- Загрузка учебных материалов (PDF, TXT, FB2) — «база знаний»
- Управление загруженными материалами (просмотр, удаление, статус индексации)
- Просмотр агрегированной статистики по платформе
- Управление программами обучения (шаблоны, настройки генерации)

---

## 3. Core Features (подробно)

### 3.1 Placement Test (входной тест)
- При первом входе пользователь проходит адаптивный тест из 30–50 вопросов
- Типы вопросов: множественный выбор, заполнение пропусков, перевод фразы, аудирование (опционально)
- Тест адаптивный: сложность следующего вопроса зависит от ответа на предыдущий
- Результат: определение уровня (A1 / A2 / B1 / B2 / C1 / C2) + слабые зоны (grammar, vocabulary, reading, writing)
- Тест генерируется LLM с привязкой к IT-тематике

### 3.2 Learning Programs (учебные программы)
- После определения уровня система генерирует персональную программу обучения
- Программа состоит из модулей → уроков → упражнений
- Каждый модуль покрывает тему: «Git & Version Control English», «Code Review Communication», «Technical Documentation», «Job Interview Prep», «Daily Standup & Meetings», «Email & Slack Etiquette» и т.д.
- Генерация контента уроков через LLM на основе:
  - загруженных администратором материалов (RAG)
  - уровня пользователя
  - его IT-специализации
- Типы упражнений:
  - **Vocabulary**: термин → определение, перевод, использование в контексте
  - **Grammar in context**: исправь ошибку в сообщении code review, перепиши email
  - **Reading comprehension**: прочитай фрагмент документации / статьи → ответь на вопросы
  - **Writing practice**: напиши commit message / PR description / ответ на Stack Overflow
  - **Translation**: русский → английский IT-контекст и обратно
  - **Dialog simulation**: чат-симуляция митинга / собеседования с LLM

### 3.3 AI-Powered Practice Sessions (практические занятия)
- Текстовый чат с LLM-ассистентом в роли:
  - Коллеги на код-ревью
  - Интервьюера на собеседовании
  - Тимлида на стендапе
  - Техписателя, проверяющего документацию
- LLM использует RAG по загруженным материалам для контекста
- После каждой сессии: разбор ошибок, рекомендации, оценка
- Система запоминает слабые места и возвращается к ним в будущих сессиях

### 3.4 Progress Dashboard (личный кабинет)
- Текущий уровень + прогресс к следующему
- Streak (дни подряд) + общее время обучения
- Разбивка по навыкам: vocabulary, grammar, reading, writing — радарная диаграмма
- История пройденных уроков и оценки
- Список слов / фраз для повторения (spaced repetition — алгоритм SM-2 или подобный)
- Еженедельный/ежемесячный отчёт с рекомендациями от LLM

### 3.5 Admin Panel
- **Пользователи**: таблица с пагинацией, поиск по email/имени, фильтр по уровню/активности, кнопки: заблокировать / разблокировать / сбросить прогресс
- **Материалы**: drag-and-drop загрузка файлов (PDF, TXT, FB2), просмотр списка загруженных, статус обработки (pending → processing → indexed → error), удаление, предпросмотр извлечённого текста
- **Статистика платформы**: количество пользователей, активные за неделю/месяц, средний уровень, популярные модули
- **Настройки генерации**: параметры для LLM (температура, системные промпты для разных типов занятий)

---

## 4. Technical Architecture

### 4.1 Frontend — Angular 19+
- Standalone components, Signals, OnPush + zoneless change detection
- Functional router с lazy loading по фичам
- UI-библиотека: Angular Material или PrimeNG (на выбор, но единообразно)
- Структура:
  ```
  src/app/
  ├── core/               # guards, interceptors, auth service, error handler
  ├── shared/             # UI components, pipes, directives
  ├── features/
  │   ├── auth/           # login, register, forgot-password
  │   ├── placement-test/ # адаптивный тест
  │   ├── programs/       # список программ, модули, уроки
  │   ├── practice/       # чат с LLM, упражнения
  │   ├── dashboard/      # прогресс, статистика, повторение слов
  │   └── admin/          # пользователи, материалы, статистика
  └── app.config.ts
  ```
- State management: Angular Signals + lightweight signal store (NgRx SignalStore или кастомный)
- HTTP: HttpClient с interceptors (auth token, error handling, loading state)
- i18n: интерфейс на русском, контент уроков на английском

### 4.2 Backend — Java 21 + Spring Boot 3.4+
- Layered / hexagonal architecture:
  ```
  src/main/java/com/devlingo/
  ├── config/            # Security, CORS, LLM client, async config
  ├── domain/            # Entity, value objects, enums
  ├── repository/        # Spring Data JPA repos
  ├── service/           # Business logic
  ├── web/
  │   ├── controller/    # REST controllers
  │   ├── dto/           # Java records для request/response
  │   └── advice/        # @ControllerAdvice, ProblemDetail
  ├── ai/                # LLM integration layer (RAG, prompt templates)
  ├── file/              # File upload, text extraction (PDF, FB2, TXT)
  └── scheduler/         # Scheduled tasks (индексация, spaced repetition)
  ```
- Security: Spring Security 6 + JWT (access + refresh tokens)
- Roles: `ROLE_USER`, `ROLE_ADMIN`
- Validation: Jakarta Bean Validation + custom validators
- Exception handling: RFC 7807 ProblemDetail

### 4.3 Database — PostgreSQL 16+
- Основные сущности:
  - `users` (id UUID, email, password_hash, role, level, specialization, created_at, updated_at)
  - `learning_programs` (id, user_id, level, status, created_at)
  - `modules` (id, program_id, topic, order_index)
  - `lessons` (id, module_id, type, content_json, order_index)
  - `exercises` (id, lesson_id, type, prompt, expected_answer, difficulty)
  - `user_progress` (id, user_id, lesson_id, score, completed_at)
  - `vocabulary` (id, user_id, word, translation, context, next_review_at, ease_factor, interval)
  - `uploaded_materials` (id, filename, content_type, status, uploaded_by, text_content, created_at)
  - `practice_sessions` (id, user_id, scenario_type, messages_json, feedback, score, created_at)
  - `placement_test_results` (id, user_id, level, scores_json, taken_at)
- Индексы: по user_id + created_at, по status, по next_review_at
- Flyway для миграций

### 4.4 AI / LLM Layer
- Интеграция: Spring AI или прямые HTTP-вызовы к LLM API
- Провайдер LLM: OpenAI-совместимый API (можно подставить локальный Ollama или облачный)
- **RAG pipeline**:
  1. Админ загружает файл (PDF / TXT / FB2)
  2. Backend извлекает текст (Apache PDFBox для PDF, парсер для FB2)
  3. Текст разбивается на чанки (500–1000 токенов с перекрытием)
  4. Чанки эмбеддятся (модель embedding — `text-embedding-3-small` или локальная)
  5. Эмбеддинги сохраняются в векторную БД (pgvector в PostgreSQL или отдельный Qdrant/Milvus)
  6. При генерации урока/упражнения: поиск релевантных чанков → подстановка в prompt
- **Prompt templates** (хранятся в конфиге, не хардкодятся):
  - `placement-test-question`: генерация вопроса для теста
  - `lesson-content`: генерация контента урока по теме + уровню + материалам
  - `exercise-generate`: генерация упражнения определённого типа
  - `practice-chat`: системный промпт для чат-симуляции (роль + контекст)
  - `feedback-generate`: анализ ответа пользователя, разбор ошибок
  - `weekly-report`: генерация еженедельного отчёта по прогрессу

### 4.5 File Processing
- Поддерживаемые форматы: PDF, TXT, FB2
- PDF → текст: Apache PDFBox
- FB2 → текст: XML-парсинг (SAX/StAX, FB2 — это XML)
- TXT → текст: прямое чтение с детекцией кодировки (UTF-8, Windows-1251)
- Файлы хранятся: MinIO / S3 / локальная файловая система
- Текстовое содержимое — в БД + векторном хранилище
- Лимит на файл: 50 МБ
- Асинхронная обработка: загрузка → очередь → extraction → chunking → embedding → indexed

### 4.6 Infrastructure (рекомендация)
- Docker Compose для локальной разработки:
  - `app-frontend` (Node + nginx)
  - `app-backend` (Java 21)
  - `postgres` (+ pgvector extension)
  - `ollama` (локальная LLM, опционально)
  - `minio` (файловое хранилище)
- CI/CD: GitLab CI (`.gitlab-ci.yml`)
- Мониторинг: Spring Boot Actuator + Prometheus + Grafana (опционально)

---

## 5. API Design (ключевые эндпоинты)

```
# Auth
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/refresh

# Profile
GET    /api/profile
PUT    /api/profile
PUT    /api/profile/specialization

# Placement Test
POST   /api/placement-test/start
POST   /api/placement-test/answer
GET    /api/placement-test/result

# Programs
GET    /api/programs                    # список программ пользователя
POST   /api/programs/generate           # сгенерировать новую программу
GET    /api/programs/{id}               # детали программы с модулями
GET    /api/programs/{id}/modules/{mid}/lessons
GET    /api/lessons/{id}                # контент урока
POST   /api/lessons/{id}/complete       # отметить как пройденный

# Exercises
GET    /api/lessons/{id}/exercises
POST   /api/exercises/{id}/submit       # отправить ответ, получить фидбек

# Practice (чат с LLM)
POST   /api/practice/start              # начать сессию (тип сценария)
POST   /api/practice/{sessionId}/message # отправить сообщение
POST   /api/practice/{sessionId}/end    # завершить, получить разбор

# Vocabulary (spaced repetition)
GET    /api/vocabulary                   # слова для повторения
GET    /api/vocabulary/review            # слова, которые пора повторить
POST   /api/vocabulary/{id}/review       # результат повторения (знаю / не знаю)
POST   /api/vocabulary                   # добавить слово вручную

# Dashboard / Progress
GET    /api/dashboard/summary            # общая статистика
GET    /api/dashboard/skills             # разбивка по навыкам
GET    /api/dashboard/history            # история занятий
GET    /api/dashboard/weekly-report      # еженедельный AI-отчёт

# Admin — Users
GET    /api/admin/users                  # список с пагинацией и фильтрами
GET    /api/admin/users/{id}
PUT    /api/admin/users/{id}/block
PUT    /api/admin/users/{id}/unblock

# Admin — Materials
POST   /api/admin/materials/upload       # загрузка файла
GET    /api/admin/materials              # список материалов
GET    /api/admin/materials/{id}         # детали + статус индексации
DELETE /api/admin/materials/{id}

# Admin — Stats
GET    /api/admin/stats/overview         # сводная статистика платформы
```

---

## 6. Data Flow: Lesson Generation (пример)

```
1. User запрашивает следующий урок
2. Backend определяет: текущий модуль, тему, уровень пользователя, слабые зоны
3. RAG: поиск релевантных чанков из загруженных материалов по теме
4. Формирование prompt:
   - System: "Ты преподаватель английского для IT. Уровень студента: B1. Тема: Code Review."
   - Context: [чанки из материалов]
   - User: "Сгенерируй урок с 3 секциями: теория, примеры, упражнения. Формат: JSON."
5. LLM генерирует контент
6. Backend валидирует JSON, сохраняет в lessons
7. Frontend рендерит урок
```

---

## 7. Non-Functional Requirements

- **Performance**: ответ API < 500ms (кроме LLM-генерации), LLM-ответ стримится через SSE
- **Security**: JWT, rate limiting, CORS, input sanitization, file upload validation
- **Scalability**: stateless backend, можно горизонтально масштабировать
- **Accessibility**: WCAG 2.1 AA для интерфейса
- **Mobile**: responsive design, PWA (опционально)
- **Offline**: кэширование словаря и текущего урока (опционально, через Service Worker)

---

## 8. Development Phases (рекомендация)

### Phase 1 — MVP (4–6 недель)
- Auth (register/login/JWT)
- Placement test (упрощённый, 20 вопросов)
- Одна программа с 3 модулями, генерация уроков через LLM
- Базовый прогресс-дашборд
- Админка: список пользователей + загрузка материалов (без RAG)

### Phase 2 — RAG & Practice (3–4 недели)
- RAG pipeline: загрузка → extraction → embedding → retrieval
- Генерация контента на основе загруженных материалов
- Чат-практика с LLM (2–3 сценария)
- Spaced repetition для словаря

### Phase 3 — Polish (2–3 недели)
- Расширенная аналитика в дашборде
- Еженедельные AI-отчёты
- Больше типов упражнений
- Админ-статистика платформы
- PWA + offline mode

---

## 9. Instructions for AI Assistant

При разработке этого приложения:

1. **Начинай с backend**: domain model → entities → repositories → services → controllers
2. **Для каждой фичи**: сначала API contract (DTO + endpoint), потом реализация, потом тесты
3. **Frontend**: компоненты по фичам, сервисы для HTTP, signal store для состояния
4. **Тесты**: пиши параллельно с кодом, не откладывай. Unit + integration
5. **LLM-интеграция**: используй абстракцию (интерфейс), чтобы можно было подставить любого провайдера
6. **Промпты для LLM**: храни как конфигурацию, не хардкодь в Java-коде
7. **Миграции БД**: Flyway, каждая фича — отдельная миграция
8. **Docker Compose**: поддерживай актуальным на каждом этапе
9. **Документируй API**: OpenAPI / Swagger annotations на контроллерах
10. **Код-стиль**: следуй userPreferences из контекста (Angular 19+, Java 21+, signals, records, etc.)

---

## 10. Prompt для начала разработки (копируй и вставляй)

```
Я разрабатываю веб-приложение "DevLingo" — платформу изучения английского языка
для IT-специалистов (родной язык — русский).

Стек:
- Frontend: Angular 19, standalone components, Signals, OnPush, Angular Material
- Backend: Java 21, Spring Boot 3.4, Spring Security 6, Spring Data JPA
- Database: PostgreSQL 16 + pgvector
- AI: Spring AI + OpenAI-compatible API (Ollama для локальной разработки)
- Infrastructure: Docker Compose

Сейчас я на Phase 1 (MVP). Мне нужно реализовать [КОНКРЕТНАЯ ФИЧА].

Требования к коду:
- Java: records для DTO, constructor injection, hexagonal architecture
- Angular: standalone components, signals, functional router, OnPush
- Тесты: JUnit 5 + AssertJ + Testcontainers / Vitest + testing-library
- API: RESTful, ProblemDetail для ошибок, JWT auth

Пожалуйста:
1. Объясни выбранный подход (1–4 предложения)
2. Дай код
3. Объясни неочевидные решения
4. Укажи trade-offs если есть

[ОПИШИ ЧТО КОНКРЕТНО НУЖНО СДЕЛАТЬ]
```
