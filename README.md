
# 📘 Návod: Kotlin + Spring Boot + Thymeleaf – jednoduchá CRUD aplikace

## 1. Vytvoření projektu v IntelliJ IDEA

1. **File → New → Project… → Spring Initializr**
2. Parametry projektu:
    - **Language:** Kotlin
    - **Build system:** Gradle (Groovy DSL)
    - **Java version:** 21
    - **Group:** `cz.uhk`
    - **Artifact:** `ppro-demo`
    - **Name:** `ppro-demo`
3. **Dependencies**:
    - Spring Web
    - Spring Data JPA
    - Spring Boot DevTools
    - Validation
    - H2 Database
    - Thymeleaf

IDEA vygeneruje základní kostru projektu.

---

## 2. Konfigurace Gradle

Otevřete `build.gradle` a nastavte ho takto:

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.9.25'
    id 'org.jetbrains.kotlin.plugin.spring' version '1.9.25'
    id 'org.jetbrains.kotlin.plugin.jpa' version '1.9.25'
    id 'org.springframework.boot' version '3.5.6'
    id 'io.spring.dependency-management' version '1.1.7'
}

group = 'cz.uhk'
version = '0.0.1-SNAPSHOT'
description = 'ppro-demo'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'com.fasterxml.jackson.module:jackson-module-kotlin'
    implementation 'org.jetbrains.kotlin:kotlin-reflect'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    runtimeOnly 'com.h2database:h2'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.jetbrains.kotlin:kotlin-test-junit5'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll '-Xjsr305=strict'
    }
}

allOpen {
    annotation 'jakarta.persistence.Entity'
    annotation 'jakarta.persistence.MappedSuperclass'
    annotation 'jakarta.persistence.Embeddable'
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).configureEach {
    kotlinOptions {
        jvmTarget = "21"
    }
}

tasks.named('test') {
    useJUnitPlatform()
}
```

---

## 3. Konfigurace aplikace

Soubor: **`src/main/resources/application.yml`**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:todos;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driverClassName: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  h2:
    console:
      enabled: true
      path: /h2-console

server:
  error:
    include-message: always
    include-binding-errors: always
```

Volitelné: **seed data** – `src/main/resources/data.sql`
```sql
INSERT INTO todos (title, description, done, created_at, updated_at)
VALUES
 ('Buy milk', '2L semi-skimmed', FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
 ('Write seminar slides', 'CRUD in Kotlin + Spring Boot', TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
```

---

## 4. Aplikační kód (Kotlin)

### Application.kt
```kotlin
package cz.uhk.ppro

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
```

### Entita Todo.kt
```kotlin
package cz.uhk.ppro

import jakarta.persistence.*
import java.time.Instant
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

@Entity
@Table(name = "todos")
class Todo(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var title: String = "",

    @Column
    var description: String? = null,

    @Column(nullable = false)
    var done: Boolean = false,

    @CreationTimestamp
    var createdAt: Instant? = null,

    @UpdateTimestamp
    var updatedAt: Instant? = null
)
```

### Repository
```kotlin
package cz.uhk.ppro
import org.springframework.data.jpa.repository.JpaRepository

interface TodoRepository : JpaRepository<Todo, Long>
```

### Service
```kotlin
package cz.uhk.ppro
import org.springframework.stereotype.Service

@Service
class TodoService(val repo: TodoRepository) {
    fun list(): List<Todo> = repo.findAll()
    fun get(id: Long): Todo = repo.findById(id).orElseThrow { RuntimeException("Not found") }
    fun save(todo: Todo): Todo = repo.save(todo)
    fun delete(id: Long) = repo.delete(get(id))
}
```

### Controller
```kotlin
package cz.uhk.ppro

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/todos")
class TodoController(val service: TodoService) {

    @GetMapping
    fun list(model: Model): String {
        model.addAttribute("todos", service.list())
        return "index"
    }

    @GetMapping("/new")
    fun form(model: Model): String {
        model.addAttribute("todo", Todo())
        return "form"
    }

    @PostMapping
    fun create(todo: Todo): String {
        service.save(todo)
        return "redirect:/todos"
    }

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Long, model: Model): String {
        model.addAttribute("todo", service.get(id))
        return "detail"
    }

    @PostMapping("/{id}/delete")
    fun delete(@PathVariable id: Long): String {
        service.delete(id)
        return "redirect:/todos"
    }
}
```

---

## 5. Thymeleaf šablony

📂 `src/main/resources/templates/`

### index.html
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="en">
<head><meta charset="UTF-8"><title>Todo List</title></head>
<body>
<h1>Todo List</h1>
<a th:href="@{/todos/new}">➕ Add New Todo</a>
<ul>
  <li th:each="t : ${todos}">
    <a th:href="@{/todos/{id}(id=${t.id})}" th:text="${t.title}">Todo</a>
    (done: <span th:text="${t.done}">false</span>)
  </li>
</ul>
</body>
</html>
```

### form.html
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="en">
<head><meta charset="UTF-8"><title>Create Todo</title></head>
<body>
<h1>Create New Todo</h1>
<form th:action="@{/todos}" method="post">
  Title: <input type="text" name="title" th:value="${todo.title}"><br>
  Description: <input type="text" name="description" th:value="${todo.description}"><br>
  Done: <input type="checkbox" name="done" th:checked="${todo.done}"><br>
  <button type="submit">Save</button>
</form>
<a th:href="@{/todos}">⬅ Back to list</a>
</body>
</html>
```

### detail.html
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="en">
<head><meta charset="UTF-8"><title>Todo Detail</title></head>
<body>
<h1 th:text="${todo.title}">Todo</h1>
<p th:text="${todo.description}">Description</p>
<p>Done: <span th:text="${todo.done}">false</span></p>
<form th:action="@{/todos/{id}/delete(id=${todo.id})}" method="post">
  <button type="submit">❌ Delete</button>
</form>
<a th:href="@{/todos}">⬅ Back to list</a>
</body>
</html>
```

---

## 6. Spuštění aplikace

V IntelliJ:
- Otevři `Application.kt` → **Run** ▶️
- Aplikace poběží na: [http://localhost:8080/todos](http://localhost:8080/todos)

---

## 7. Funkce

- `GET /todos` → zobrazí seznam úkolů
- `GET /todos/new` → formulář pro vytvoření
- `POST /todos` → uloží nový úkol
- `GET /todos/{id}` → detail úkolu
- `POST /todos/{id}/delete` → smaže úkol  
