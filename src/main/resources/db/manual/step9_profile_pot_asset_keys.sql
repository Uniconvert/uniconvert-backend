-- Profile / Pot asset-key policy migration
--
-- 변경 내용
-- 1. User 프로필 이미지는 URL 대신 프론트 에셋 key를 저장한다.
-- 2. User의 주요 관리 목표(primaryGoal)를 저장한다.
-- 3. Pot 대표 이미지는 URL 대신 프론트 에셋 key를 저장한다.
--
-- 주의:
-- 운영/개발 DB에 적용하기 전에 기존 컬럼 존재 여부를 확인한다.
-- 이미 적용된 DB에서는 이 파일을 다시 실행하지 않는다.
-- 기존 image_url 컬럼은 롤백 호환성을 위해 이번 마이그레이션에서 삭제하지 않는다.

ALTER TABLE `user`
    ADD COLUMN `profile_image_key` VARCHAR(100) NULL,
    ADD COLUMN `primary_goal` VARCHAR(100) NULL;

ALTER TABLE `pot`
    ADD COLUMN `representative_image_key` VARCHAR(100) NULL;
