-- 비회원 전자문진은 회원 정보 없이도 저장할 수 있어야 합니다.
-- Hibernate ddl-auto=update는 기존 NOT NULL 제약을 자동으로 완화하지 않으므로 1회 실행합니다.
ALTER TABLE questionnaire_submission
    MODIFY COLUMN member_id BIGINT NULL;
