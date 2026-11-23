# 명언 앱 (콘솔)

Java 25 · Gradle 콘솔 명언 앱입니다. 등록·목록·삭제·수정·검색·페이징·빌드를 지원하고, 명언은 MySQL 에 저장합니다.

## 1. MySQL 띄우기

```bash
docker run --name mysql-1 -p 127.0.0.1:3306:3306 -e TZ=Asia/Seoul -e MYSQL_ROOT_PASSWORD=<비밀번호> -d mysql:8.4
docker exec mysql-1 mysqladmin ping -uroot -p<비밀번호>
```

## 2. DB·테이블 만들기

개발 DB 와 테스트 DB 를 따로 씁니다.

| 용도 | DB 이름 (기본값) |
|---|---|
| 앱 실행(개발) | `wiseSaying_dev` |
| 테스트 | `wiseSaying_test` (테스트마다 `TRUNCATE` 하므로 꼭 분리) |

직접 만들 때:

```sql
CREATE DATABASE IF NOT EXISTS wiseSaying_dev  CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS wiseSaying_test CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

테이블 생성 SQL 은 [`src/main/resources/sql/schema.sql`](src/main/resources/sql/schema.sql) 에 있습니다.

```sql
CREATE TABLE IF NOT EXISTS wise_saying (
    id          INT UNSIGNED NOT NULL AUTO_INCREMENT,
    create_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modify_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    content     TEXT         NOT NULL,
    author      VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4;
```

손으로 만들지 않아도 됩니다. 앱(과 테스트)이 시작할 때 JDBC URL 의 `createDatabaseIfNotExist=true` 로 DB 를 만들고, `WiseSayingDbRepository` 가 `schema.sql` 을 실행해 테이블이 없으면 만듭니다.

## 3. 접속 정보 설정

접속 정보는 소스에 적지 않습니다. 둘 중 하나로 설정하세요.

- `db.properties.example` 을 `db.properties` 로 복사해 채우기 (`db.properties` 는 `.gitignore` 에 있어 커밋되지 않음)
- 환경변수 (파일보다 우선): `WISE_SAYING_DB_HOST`, `WISE_SAYING_DB_PORT`, `WISE_SAYING_DB_USERNAME`, `WISE_SAYING_DB_PASSWORD`, `WISE_SAYING_DB_NAME`, `WISE_SAYING_DB_TEST_NAME`

## 4. 실행·테스트

```bash
./gradlew run      # 앱 실행 (개발 DB). 명언이 0개면 샘플 10개로 시작
./gradlew test     # 통합 테스트 (테스트 DB)
```

명령: `등록`, `목록`, `목록?page=2`, `목록?keywordType=content|author&keyword=...`, `삭제?id=1`, `수정?id=1`, `빌드`, `종료`

`빌드` 는 모든 명언을 `db/wiseSaying/data.json` 으로 모읍니다.

## 리포지터리를 어떻게 갈아 끼웠나

컨트롤러 → 서비스 → 리포지터리 구조라서, 저장 방식이 바뀌어도 리포지터리 구현체만 바꾸면 됩니다.

```
WiseSayingRepository (interface)
├── WiseSayingMemoryRepository  ④  메모리 리스트
├── WiseSayingFileRepository    ⑦  db/wiseSaying/{id}.json + lastId.txt
└── WiseSayingDbRepository      ⑧  MySQL (JDBC)   ← 지금 사용 중
```

- 교체한 곳은 `AppContext` 한 줄입니다: `new WiseSayingDbRepository(AppConfig.getDbConfig(), AppConfig.getDbDir())`
- `WiseSayingController`·`WiseSayingService` 는 바꾸지 않았습니다. JDBC·SQL 코드는 `WiseSayingDbRepository` 에만 있습니다.
- 번호는 `AUTO_INCREMENT` 가 발급하므로 삭제된 번호를 재사용하지 않고, 재시작해도 이어집니다.
- 검색·페이징은 SQL 로 처리합니다: `WHERE content|author LIKE CONCAT('%', ?, '%')`, `ORDER BY id DESC`, `LIMIT ? OFFSET ?` (검색어의 `%`·`_` 는 이스케이프)
- 테스트는 `AppConfig.setTestMode()` 로 테스트 DB 를 쓰고, 매 테스트 전에 `TRUNCATE wise_saying` 합니다.
