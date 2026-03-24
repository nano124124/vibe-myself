-- =============================================
-- ST_CODE_GRP / ST_CODE_DTL CODE_GRP_CD 컬럼 길이 확장
-- VARCHAR(10) -> VARCHAR(20)
-- =============================================

ALTER TABLE ST_CODE_DTL ALTER COLUMN CODE_GRP_CD TYPE VARCHAR(20);
ALTER TABLE ST_CODE_GRP ALTER COLUMN CODE_GRP_CD TYPE VARCHAR(20);
