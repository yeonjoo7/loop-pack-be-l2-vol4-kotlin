# CLAUDE.md

## 프로젝트 개요

Loopers에서 제공하는 Spring Kotlin 기반 커머스 서비스 템플릿입니다.
Gradle 멀티 모듈 구조로 구성되어 있으며, HTTP API, Batch, Streamer 애플리케이션을 분리해 운영합니다.

## 기술 스택

- Kotlin `2.0.20`
- Java Toolchain `21`
- Gradle Wrapper `8.13`
- Spring Boot `3.4.4`
- Spring Dependency Management `1.1.7`
- Spring Cloud Dependencies `2024.0.1`
- ktlint Gradle Plugin `12.1.2`, ktlint `1.0.1`
- SpringDoc OpenAPI `2.7.0`
- JPA, QueryDSL, MySQL Connector
- Redis, Kafka 설정 모듈
- 테스트: JUnit 5, Spring Boot Test, SpringMockK, Mockito, Instancio, Testcontainers

## 모듈 구조

- `apps`
  - `commerce-api`: HTTP API 애플리케이션
  - `commerce-batch`: Spring Batch 애플리케이션
  - `commerce-streamer`: Kafka Consumer 애플리케이션
- `modules`
  - `jpa`: JPA, DataSource, QueryDSL, 테스트 픽스처 설정
  - `redis`: Redis 설정 및 테스트 픽스처
  - `kafka`: Kafka 설정
- `supports`
  - `jackson`: 공통 Jackson 설정
  - `logging`: Logback Appender 및 프로필별 로깅 설정
  - `monitoring`: Actuator, Prometheus, Grafana 설정

## 개발 규칙

### 진행 Workflow - 증강 코딩

- 개발 방향과 주요 의사결정의 최종 주도권은 개발자에게 있습니다.
- AI는 방향을 제안할 수 있지만, 승인된 결정과 `plan.md`의 다음 항목을 기준으로 작업합니다.
- 반복적인 동작, 범위 확장, 테스트 삭제 또는 약화가 필요해 보이면 즉시 멈추고 보고합니다.
- 구조 변경과 기능 변경은 가능하면 분리합니다.
- 분위기로 밀어붙이지 않고, 테스트와 동작으로 확인되는 구현만 남깁니다.

### 개발 Workflow - TDD

- Red -> Green -> Refactor 순서를 따릅니다.
- 하나의 작은 동작에 대해 실패하는 테스트를 먼저 작성합니다.
- 테스트는 3A 패턴을 따릅니다: Arrange, Act, Assert.
- Green 단계에서는 현재 실패한 테스트를 통과시키는 만큼만 구현합니다.
- Refactor 단계는 관련 테스트가 통과한 상태에서만 진행합니다.
- 요청되지 않았거나 테스트로 표현되지 않은 기능은 임의로 추가하지 않습니다.

## 주의사항

### Never Do

- 실제 동작하지 않는 코드나 임시 Mock 데이터 기반 구현을 남기지 않습니다.
- Kotlin null-safety를 깨는 방식으로 작성하지 않습니다.
- `println`이나 임시 디버그 코드를 남기지 않습니다.
- 테스트를 임의로 삭제하거나 검증을 약화하지 않습니다.

### Recommendation

- 기존 패키지와 레이어링을 따릅니다: `domain -> application -> interfaces`, 영속성 구현은 `infrastructure`.
- 예상 가능한 비즈니스 실패는 `CoreException`과 적절한 `ErrorType`으로 표현합니다.
- API 구현이 완료되면 `.http` 파일에 실행 예시를 추가합니다.
- 복잡한 추상화보다 요구사항을 만족하는 단순한 설계를 우선합니다.

### Priority

1. 테스트로 증명되는 실제 동작
2. 명확한 도메인 규칙과 null-safety
3. 기존 프로젝트 패턴과의 일관성
4. 단순한 구현
5. 현재 요구사항에 필요한 경우에만 성능과 동시성 개선
