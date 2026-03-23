# Spring Boot Backend Setup Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `backend/` 디렉토리에 Spring Boot 3.4.3 + Gradle Kotlin DSL + MyBatis + PostgreSQL + Spring Security 프로젝트를 완전히 세팅한다.

**Architecture:** 플랫 레이어드 구조(controller → service → mapper → db). MyBatis XML 방식 SQL 관리. Spring Security는 개발용 permitAll 설정.

**Tech Stack:** Java 21, Spring Boot 3.4.3, Gradle 8.11 (Kotlin DSL), MyBatis Spring Boot Starter 3.0.3, PostgreSQL, Spring Security, Lombok

---

## Chunk 1: Gradle 빌드 설정

### Task 1: settings.gradle.kts 생성

**Files:**
- Create: `backend/settings.gradle.kts`

- [ ] **Step 1: settings.gradle.kts 파일 생성**

```kotlin
// backend/settings.gradle.kts
rootProject.name = "backend"
```

### Task 2: build.gradle.kts 생성

**Files:**
- Create: `backend/build.gradle.kts`

- [ ] **Step 1: build.gradle.kts 파일 생성**

```kotlin
// backend/build.gradle.kts
plugins {
    java
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.vibemyself"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3")
    runtimeOnly("org.postgresql:postgresql")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

### Task 3: Gradle Wrapper 생성

**Files:**
- Create: `backend/gradle/wrapper/gradle-wrapper.properties`
- Create: `backend/gradle/wrapper/gradle-wrapper.jar`
- Create: `backend/gradlew`
- Create: `backend/gradlew.bat`

- [ ] **Step 1: Gradle이 설치되어 있는지 확인**

```bash
gradle --version
```

Expected: Gradle 버전 출력 (어떤 버전이든 OK)

- [ ] **Step 2: backend 디렉토리에서 wrapper 생성**

```bash
cd /Users/nyj/Documents/git/vibe-myself/backend && gradle wrapper --gradle-version 8.11
```

Expected: `BUILD SUCCESSFUL` 및 `gradle/`, `gradlew`, `gradlew.bat` 파일 생성

- [ ] **Step 3: 의존성 다운로드 확인**

```bash
cd /Users/nyj/Documents/git/vibe-myself/backend && ./gradlew dependencies --configuration compileClasspath
```

Expected: `BUILD SUCCESSFUL` 및 의존성 트리 출력

- [ ] **Step 4: 커밋**

```bash
cd /Users/nyj/Documents/git/vibe-myself && git add backend/build.gradle.kts backend/settings.gradle.kts backend/gradle backend/gradlew backend/gradlew.bat
git commit -m "chore: add Gradle build configuration for Spring Boot backend"
```

---

## Chunk 2: 소스 코드 구조

### Task 4: 소스 디렉토리 구조 생성

**Files:**
- Create: `backend/src/main/java/com/vibemyself/` (패키지 디렉토리)
- Create: `backend/src/main/java/com/vibemyself/config/` (.gitkeep)
- Create: `backend/src/main/java/com/vibemyself/controller/` (.gitkeep)
- Create: `backend/src/main/java/com/vibemyself/service/` (.gitkeep)
- Create: `backend/src/main/java/com/vibemyself/mapper/` (.gitkeep)
- Create: `backend/src/main/java/com/vibemyself/model/` (.gitkeep)
- Create: `backend/src/main/resources/mapper/` (.gitkeep)
- Create: `backend/src/test/java/com/vibemyself/`

- [ ] **Step 1: 디렉토리 생성**

```bash
mkdir -p /Users/nyj/Documents/git/vibe-myself/backend/src/main/java/com/vibemyself/{config,controller,service,mapper,model}
mkdir -p /Users/nyj/Documents/git/vibe-myself/backend/src/main/resources/mapper
mkdir -p /Users/nyj/Documents/git/vibe-myself/backend/src/test/java/com/vibemyself
```

Expected: 오류 없이 디렉토리 생성 완료

- [ ] **Step 2: 빈 패키지 디렉토리를 git이 추적하도록 .gitkeep 생성**

```bash
touch /Users/nyj/Documents/git/vibe-myself/backend/src/main/java/com/vibemyself/controller/.gitkeep
touch /Users/nyj/Documents/git/vibe-myself/backend/src/main/java/com/vibemyself/service/.gitkeep
touch /Users/nyj/Documents/git/vibe-myself/backend/src/main/java/com/vibemyself/mapper/.gitkeep
touch /Users/nyj/Documents/git/vibe-myself/backend/src/main/java/com/vibemyself/model/.gitkeep
touch /Users/nyj/Documents/git/vibe-myself/backend/src/main/resources/mapper/.gitkeep
```

### Task 5: 컨텍스트 로드 테스트 작성 (TDD - 먼저 실패하는 테스트)

**Files:**
- Create: `backend/src/test/java/com/vibemyself/VibeMyselfApplicationTests.java`

- [ ] **Step 1: 테스트 파일 생성**

```java
// backend/src/test/java/com/vibemyself/VibeMyselfApplicationTests.java
package com.vibemyself;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class VibeMyselfApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 2: 테스트 실행 — 실패 확인 (main class 없음)**

```bash
cd /Users/nyj/Documents/git/vibe-myself/backend && ./gradlew test
```

Expected: FAIL — `VibeMyselfApplication` 클래스가 없어서 컴파일 오류 또는 컨텍스트 로드 실패

### Task 6: VibeMyselfApplication.java 생성

**Files:**
- Create: `backend/src/main/java/com/vibemyself/VibeMyselfApplication.java`

- [ ] **Step 1: 메인 클래스 생성**

```java
// backend/src/main/java/com/vibemyself/VibeMyselfApplication.java
package com.vibemyself;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VibeMyselfApplication {

    public static void main(String[] args) {
        SpringApplication.run(VibeMyselfApplication.class, args);
    }
}
```

### Task 7: SecurityConfig.java 생성

**Files:**
- Create: `backend/src/main/java/com/vibemyself/config/SecurityConfig.java`

- [ ] **Step 1: SecurityConfig 파일 생성**

```java
// backend/src/main/java/com/vibemyself/config/SecurityConfig.java
package com.vibemyself.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
```

---

## Chunk 3: 설정 파일 + 최종 검증

### Task 8: application.yml 생성

**Files:**
- Create: `backend/src/main/resources/application.yml`

- [ ] **Step 1: application.yml 생성**

```yaml
# backend/src/main/resources/application.yml
spring:
  profiles:
    active: local
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver

mybatis:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: com.vibemyself.model
  configuration:
    map-underscore-to-camel-case: true

server:
  port: 8080
```

> 프로덕션/CI 환경에서는 `SPRING_PROFILES_ACTIVE` 환경변수로 프로파일 오버라이드.

### Task 9: application-local.yml 생성

**Files:**
- Create: `backend/src/main/resources/application-local.yml`

- [ ] **Step 1: application-local.yml 생성**

```yaml
# backend/src/main/resources/application-local.yml
# 로컬 개발 환경 전용. 이 파일은 git에 포함되지 않음.
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vibemyself
    username: vibemyself
    password: password
```

### Task 10: .gitignore 생성

**Files:**
- Create: `backend/.gitignore`

- [ ] **Step 1: .gitignore 생성**

```gitignore
# backend/.gitignore

# 로컬 환경 설정 (민감 정보 포함)
src/main/resources/application-local.yml

# Gradle
.gradle/
build/

# IDE
.idea/
*.iml
.vscode/

# OS
.DS_Store
```

### Task 11: 최종 빌드 및 테스트 실행

- [ ] **Step 1: 컴파일 확인**

```bash
cd /Users/nyj/Documents/git/vibe-myself/backend && ./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: 테스트 실행 (PostgreSQL 로컬 DB 실행 필요)**

> PostgreSQL이 실행 중이고 `vibemyself` DB가 존재해야 합니다.
> 실행 중이지 않다면 `./gradlew compileTestJava`로 컴파일만 확인해도 됩니다.

```bash
cd /Users/nyj/Documents/git/vibe-myself/backend && ./gradlew test
```

Expected: `BUILD SUCCESSFUL`, `VibeMyselfApplicationTests > contextLoads() PASSED`

- [ ] **Step 3: 최종 커밋**

```bash
cd /Users/nyj/Documents/git/vibe-myself && \
git add \
  backend/src/main/java/com/vibemyself/VibeMyselfApplication.java \
  backend/src/main/java/com/vibemyself/config/SecurityConfig.java \
  backend/src/main/java/com/vibemyself/controller/.gitkeep \
  backend/src/main/java/com/vibemyself/service/.gitkeep \
  backend/src/main/java/com/vibemyself/mapper/.gitkeep \
  backend/src/main/java/com/vibemyself/model/.gitkeep \
  backend/src/main/resources/mapper/.gitkeep \
  backend/src/main/resources/application.yml \
  backend/src/test/java/com/vibemyself/VibeMyselfApplicationTests.java \
  backend/.gitignore && \
git commit -m "feat: initialize Spring Boot backend with MyBatis, PostgreSQL, and Security"
```

> `application-local.yml`은 .gitignore에 의해 자동 제외됩니다.
