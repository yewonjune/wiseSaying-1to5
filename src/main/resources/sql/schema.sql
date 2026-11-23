-- 명언 테이블
-- 앱이 시작할 때 이 파일을 읽어 테이블이 없으면 만든다 (WiseSayingDbRepository)
CREATE TABLE IF NOT EXISTS wise_saying (
    id          INT UNSIGNED NOT NULL AUTO_INCREMENT,
    create_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modify_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    content     TEXT         NOT NULL,
    author      VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4;
