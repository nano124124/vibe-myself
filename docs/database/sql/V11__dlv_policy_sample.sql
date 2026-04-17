-- 배송정책 샘플 데이터
-- DLV_TP_CD: FREE(무료배송) / COND_FREE(조건부 무료배송) / PAID(유료배송)

INSERT INTO PR_DLV_POLICY (DLV_POLICY_NO, DLV_POLICY_NM, DLV_TP_CD, DLV_AMT, FREE_COND_AMT, RTN_DLV_AMT, USE_YN, REG_ID, MOD_ID) VALUES
-- 무료배송: 배송비 없음, 반품배송비 3,000원
('DLV001', '무료배송', 'FREE', 0, NULL, 3000, 'Y', 'SYSTEM', 'SYSTEM'),

-- 조건부 무료배송: 5만원 이상 구매 시 무료, 미달 시 3,000원, 반품배송비 3,000원
('DLV002', '5만원 이상 무료배송', 'COND_FREE', 3000, 50000, 3000, 'Y', 'SYSTEM', 'SYSTEM'),

-- 유료배송: 항상 3,000원, 반품배송비 3,000원
('DLV003', '유료배송 (3,000원)', 'PAID', 3000, NULL, 3000, 'Y', 'SYSTEM', 'SYSTEM');
