# Spring Boot Backend Setup Design

**Date:** 2026-03-23
**Status:** Approved

## Overview

Spring Boot 백엔드 프로젝트 초기 세팅. Gradle Kotlin DSL, Java 21 (소스 언어: Java), MyBatis + PostgreSQL, Spring Security, Spring Web 구성.

## Project Metadata

| 항목 | 값 |
|------|-----|
| Group | `com.vibemyself` |
| Artifact | `backend` |
| Root project name | `backend` |
| Main class | `com.vibemyself.VibeMyselfApplication` |
| Source language | Java 21 |
| Spring Boot | 3.4.3 |
| Gradle wrapper | 8.11 (via `gradle wrapper` 명령 생성) |
| Build | Gradle Kotlin DSL |

## Dependencies (build.gradle.kts)

| 의존성 | 버전 | 설정 방식 |
|--------|------|-----------|
| `spring-boot-starter-web` | BOM 관리 | `implementation` |
| `spring-boot-starter-security` | BOM 관리 | `implementation` |
| `mybatis-spring-boot-starter` | 3.0.3 | `implementation` |
| `postgresql` | BOM 관리 | `runtimeOnly` |
| `lombok` | BOM 관리 | `compileOnly` + `annotationProcessor` |
| `spring-boot-starter-test` | BOM 관리 | `testImplementation` |

> Lombok은 `compileOnly`와 `annotationProcessor` 두 곳 모두 등록 필요.

## build.gradle.kts 구조

```kotlin
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

## settings.gradle.kts

```kotlin
rootProject.name = "backend"
```

## File Structure

```
backend/
├── build.gradle.kts
├── settings.gradle.kts
├── .gitignore                       (application-local.yml 제외)
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties  (Gradle 8.11)
├── gradlew
├── gradlew.bat
└── src/
    ├── main/
    │   ├── java/com/vibemyself/
    │   │   ├── VibeMyselfApplication.java
    │   │   ├── config/
    │   │   │   └── SecurityConfig.java
    │   │   ├── controller/
    │   │   ├── service/
    │   │   ├── mapper/
    │   │   └── model/
    │   └── resources/
    │       ├── mapper/
    │       ├── application.yml
    │       └── application-local.yml    (git 제외)
    └── test/
        └── java/com/vibemyself/
            └── VibeMyselfApplicationTests.java
```

## Configuration

### application.yml
환경변수 기반 설정. 프로파일은 `SPRING_PROFILES_ACTIVE` 환경변수로 외부에서 오버라이드 가능.

```yaml
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

> 프로덕션/CI에서는 `SPRING_PROFILES_ACTIVE=prod` 등으로 오버라이드. `application-local.yml`은 로컬에서만 활성화.

### application-local.yml
로컬 개발 환경 전용. 실제 로컬 DB 접속 정보:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vibemyself
    username: vibemyself
    password: password
```

### SecurityConfig.java (패키지: com.vibemyself.config)
```java
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

### 테스트 전략
`VibeMyselfApplicationTests`는 `@SpringBootTest` 컨텍스트 로드 테스트만 포함.
DB 연결이 필요하므로 테스트 실행 시 로컬 환경변수 또는 `application-local.yml` 값 사용.

### .gitignore 전략
`application-local.yml`은 로컬 DB 자격증명 포함으로 git 추적 제외.

## Bootstrapping 순서

1. `build.gradle.kts`, `settings.gradle.kts` 수동 생성
2. `gradle wrapper --gradle-version 8.11` 실행하여 wrapper 파일 생성
3. 나머지 소스 파일 생성
