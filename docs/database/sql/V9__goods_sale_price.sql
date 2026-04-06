-- =============================================
-- 상품 판매기간 컬럼 추가 및 가격 선분 테이블 신설
-- =============================================

-- 1. PR_GOODS_BASE 판매기간 컬럼 추가
ALTER TABLE PR_GOODS_BASE
    ADD COLUMN SALE_START_DTM TIMESTAMP NULL,  -- 판매시작일시
    ADD COLUMN SALE_END_DTM   TIMESTAMP NULL;  -- 판매종료일시

-- 2. PR_GOODS_BASE 기존 단일 가격 컬럼 제거 (PR_GOODS_PRC로 이관)
ALTER TABLE PR_GOODS_BASE
    DROP COLUMN SALE_PRC;

-- 3. 상품 가격 선분 테이블
--    현재 유효 가격: APLY_FROM_DT <= TODAY AND APLY_TO_DT >= TODAY
--    현재 가격의 APLY_TO_DT는 '99991231'
CREATE TABLE PR_GOODS_PRC (
    GOODS_NO        VARCHAR(15)     NOT NULL,
    APLY_FROM_DT    VARCHAR(8)      NOT NULL,       -- 적용시작일자 (yyyyMMdd)
    APLY_TO_DT      VARCHAR(8)      NOT NULL,       -- 적용종료일자 (yyyyMMdd, 현재: '99991231')
    SALE_PRC        NUMERIC(15,2)   NOT NULL,       -- 판매가
    REG_DTM         TIMESTAMP       NOT NULL DEFAULT NOW(),
    REG_ID          VARCHAR(50)     NOT NULL,
    MOD_DTM         TIMESTAMP       NOT NULL DEFAULT NOW(),
    MOD_ID          VARCHAR(50)     NOT NULL,
    CONSTRAINT PK_PR_GOODS_PRC PRIMARY KEY (GOODS_NO, APLY_FROM_DT),
    CONSTRAINT FK_PR_GOODS_PRC_GOODS FOREIGN KEY (GOODS_NO) REFERENCES PR_GOODS_BASE (GOODS_NO)
);

CREATE INDEX IDX_PR_GOODS_PRC_DT ON PR_GOODS_PRC (GOODS_NO, APLY_FROM_DT, APLY_TO_DT);
