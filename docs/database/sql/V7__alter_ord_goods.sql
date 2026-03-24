-- =============================================
-- OP_ORD_GOODS 컬럼 변경
-- - CLAIM_TP_CD 삭제 (ORD_GOODS_STAT_CD로 통합)
-- - DLV_STAT_CD 추가 (배송 상태 분리)
-- =============================================

ALTER TABLE OP_ORD_GOODS
    DROP COLUMN CLAIM_TP_CD,
    ADD COLUMN DLV_STAT_CD VARCHAR(10) NULL;
