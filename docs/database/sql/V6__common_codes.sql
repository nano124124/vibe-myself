-- =============================================
-- 공통코드 초기 데이터
-- ST_CODE_GRP / ST_CODE_DTL
-- =============================================

-- ---------------------------------
-- 코드 그룹
-- ---------------------------------
INSERT INTO ST_CODE_GRP (CODE_GRP_CD, CODE_GRP_NM, USE_YN, REG_ID, MOD_ID) VALUES
('ROLE_CD',             '관리자 역할',              'Y', 'SYSTEM', 'SYSTEM'),
('MBR_STAT_CD',         '회원 상태',                'Y', 'SYSTEM', 'SYSTEM'),
('SNS_TP_CD',           'SNS 유형',                 'Y', 'SYSTEM', 'SYSTEM'),
('AST_TP_CD',           '자산 유형',                'Y', 'SYSTEM', 'SYSTEM'),
('TRNS_TP_CD',          '자산 거래 유형',           'Y', 'SYSTEM', 'SYSTEM'),
('GOODS_TP_CD',         '상품 유형',                'Y', 'SYSTEM', 'SYSTEM'),
('SALE_STAT_CD',        '판매 상태',                'Y', 'SYSTEM', 'SYSTEM'),
('DLV_TP_CD',           '배송 유형',                'Y', 'SYSTEM', 'SYSTEM'),
('ORD_GOODS_STAT_CD',   '주문상품 상태',            'Y', 'SYSTEM', 'SYSTEM'),
('DLV_STAT_CD',         '배송 상태',                'Y', 'SYSTEM', 'SYSTEM'),
('FEE_TP_CD',           '배송비 유형',              'Y', 'SYSTEM', 'SYSTEM'),
('PAY_MTHD_CD',         '결제 수단',                'Y', 'SYSTEM', 'SYSTEM'),
('PAY_STAT_CD',         '결제 상태',                'Y', 'SYSTEM', 'SYSTEM'),
('PAY_TP_CD',           '결제 처리 유형',           'Y', 'SYSTEM', 'SYSTEM'),
('APPL_TP_CD',          '프로모션 적용 유형',       'Y', 'SYSTEM', 'SYSTEM'),
('PROM_KIND_CD',        '프로모션 적용 종류',       'Y', 'SYSTEM', 'SYSTEM'),
('PROM_TP_CD',          '행사 유형',                'Y', 'SYSTEM', 'SYSTEM'),
('DC_TP_CD',            '할인 유형',                'Y', 'SYSTEM', 'SYSTEM'),
('ISS_TP_CD',           '쿠폰 발급 유형',           'Y', 'SYSTEM', 'SYSTEM'),
('AUTO_ISS_COND_CD',    '쿠폰 자동발급 조건',       'Y', 'SYSTEM', 'SYSTEM'),
('EVT_TP_CD',           '이벤트 유형',              'Y', 'SYSTEM', 'SYSTEM');


-- ---------------------------------
-- ROLE_CD 관리자 역할
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('ROLE_CD', 'SUPER', '슈퍼관리자', 1, 'SYSTEM', 'SYSTEM'),
('ROLE_CD', 'ADMIN', '관리자',     2, 'SYSTEM', 'SYSTEM'),
('ROLE_CD', 'OPS',   '운영자',     3, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- MBR_STAT_CD 회원 상태
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('MBR_STAT_CD', 'NORMAL',   '정상', 1, 'SYSTEM', 'SYSTEM'),
('MBR_STAT_CD', 'STOP',     '정지', 2, 'SYSTEM', 'SYSTEM'),
('MBR_STAT_CD', 'WITHDRAW', '탈퇴', 3, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- SNS_TP_CD SNS 유형
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('SNS_TP_CD', 'KAKAO',  '카카오', 1, 'SYSTEM', 'SYSTEM'),
('SNS_TP_CD', 'NAVER',  '네이버', 2, 'SYSTEM', 'SYSTEM'),
('SNS_TP_CD', 'GOOGLE', '구글',   3, 'SYSTEM', 'SYSTEM'),
('SNS_TP_CD', 'APPLE',  '애플',   4, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- AST_TP_CD 자산 유형
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('AST_TP_CD', 'POINT', '포인트',   1, 'SYSTEM', 'SYSTEM'),
('AST_TP_CD', 'MILE',  '마일리지', 2, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- TRNS_TP_CD 자산 거래 유형
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('TRNS_TP_CD', 'EARN',   '적립',        1, 'SYSTEM', 'SYSTEM'),
('TRNS_TP_CD', 'USE',    '사용',        2, 'SYSTEM', 'SYSTEM'),
('TRNS_TP_CD', 'CANCEL', '취소',        3, 'SYSTEM', 'SYSTEM'),
('TRNS_TP_CD', 'EXPIRE', '소멸',        4, 'SYSTEM', 'SYSTEM'),
('TRNS_TP_CD', 'ADMIN',  '관리자 조정', 5, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- GOODS_TP_CD 상품 유형
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('GOODS_TP_CD', 'NORMAL',  '일반상품', 1, 'SYSTEM', 'SYSTEM'),
('GOODS_TP_CD', 'ECOUPON', 'e쿠폰',    2, 'SYSTEM', 'SYSTEM'),
('GOODS_TP_CD', 'GIFT',    '사은품',   3, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- SALE_STAT_CD 판매 상태
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('SALE_STAT_CD', 'READY',   '판매준비', 1, 'SYSTEM', 'SYSTEM'),
('SALE_STAT_CD', 'ON_SALE', '판매중',   2, 'SYSTEM', 'SYSTEM'),
('SALE_STAT_CD', 'SOLDOUT', '품절',     3, 'SYSTEM', 'SYSTEM'),
('SALE_STAT_CD', 'STOP',    '판매중지', 4, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- DLV_TP_CD 배송 유형
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('DLV_TP_CD', 'FREE',      '무료배송',        1, 'SYSTEM', 'SYSTEM'),
('DLV_TP_CD', 'COND_FREE', '조건부 무료배송', 2, 'SYSTEM', 'SYSTEM'),
('DLV_TP_CD', 'PAID',      '유료배송',        3, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- ORD_GOODS_STAT_CD 주문상품 상태
-- 주문의 처리 결과 구분 (클레임 포함)
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('ORD_GOODS_STAT_CD', 'ORD',  '주문', 1, 'SYSTEM', 'SYSTEM'),
('ORD_GOODS_STAT_CD', 'CNCL', '취소', 2, 'SYSTEM', 'SYSTEM'),
('ORD_GOODS_STAT_CD', 'EXCH', '교환', 3, 'SYSTEM', 'SYSTEM'),
('ORD_GOODS_STAT_CD', 'RTN',  '반품', 4, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- DLV_STAT_CD 배송 상태
-- 정상 주문의 배송 진행 상태
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('DLV_STAT_CD', 'ORD_DONE',  '주문완료',   1, 'SYSTEM', 'SYSTEM'),
('DLV_STAT_CD', 'PREP',      '상품준비중', 2, 'SYSTEM', 'SYSTEM'),
('DLV_STAT_CD', 'SHIP',      '배송중',     3, 'SYSTEM', 'SYSTEM'),
('DLV_STAT_CD', 'DLV_DONE',  '배송완료',   4, 'SYSTEM', 'SYSTEM'),
('DLV_STAT_CD', 'CONFIRM',   '구매확정',   5, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- FEE_TP_CD 배송비 유형
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('FEE_TP_CD', 'INIT', '초도배송비', 1, 'SYSTEM', 'SYSTEM'),
('FEE_TP_CD', 'EXCH', '교환배송비', 2, 'SYSTEM', 'SYSTEM'),
('FEE_TP_CD', 'RTN',  '반품배송비', 3, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- PAY_MTHD_CD 결제 수단
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('PAY_MTHD_CD', 'CARD',     '신용카드',   1, 'SYSTEM', 'SYSTEM'),
('PAY_MTHD_CD', 'VBANK',    '가상계좌',   2, 'SYSTEM', 'SYSTEM'),
('PAY_MTHD_CD', 'BANK',     '계좌이체',   3, 'SYSTEM', 'SYSTEM'),
('PAY_MTHD_CD', 'KAKAOPAY', '카카오페이', 4, 'SYSTEM', 'SYSTEM'),
('PAY_MTHD_CD', 'NAVERPAY', '네이버페이', 5, 'SYSTEM', 'SYSTEM'),
('PAY_MTHD_CD', 'POINT',    '포인트',     6, 'SYSTEM', 'SYSTEM'),
('PAY_MTHD_CD', 'MILE',     '마일리지',   7, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- PAY_STAT_CD 결제 상태
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('PAY_STAT_CD', 'WAIT',      '결제대기', 1, 'SYSTEM', 'SYSTEM'),
('PAY_STAT_CD', 'DONE',      '결제완료', 2, 'SYSTEM', 'SYSTEM'),
('PAY_STAT_CD', 'CNCL',      '결제취소', 3, 'SYSTEM', 'SYSTEM'),
('PAY_STAT_CD', 'PART_CNCL', '부분취소', 4, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- PAY_TP_CD 결제 처리 유형
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('PAY_TP_CD', 'PAY',       '결제',     1, 'SYSTEM', 'SYSTEM'),
('PAY_TP_CD', 'CNCL',      '취소',     2, 'SYSTEM', 'SYSTEM'),
('PAY_TP_CD', 'PART_CNCL', '부분취소', 3, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- APPL_TP_CD 프로모션 적용 유형
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('APPL_TP_CD', 'ORDER',  '주문 적용',    1, 'SYSTEM', 'SYSTEM'),
('APPL_TP_CD', 'CANCEL', '취소 적용해제', 2, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- PROM_KIND_CD 프로모션 적용 종류
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('PROM_KIND_CD', 'COUPON', '쿠폰',     1, 'SYSTEM', 'SYSTEM'),
('PROM_KIND_CD', 'PROM',   '프로모션', 2, 'SYSTEM', 'SYSTEM'),
('PROM_KIND_CD', 'GIFT',   '사은품',   3, 'SYSTEM', 'SYSTEM'),
('PROM_KIND_CD', 'POINT',  '포인트',   4, 'SYSTEM', 'SYSTEM'),
('PROM_KIND_CD', 'MILE',   '마일리지', 5, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- PROM_TP_CD 행사 유형
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('PROM_TP_CD', 'COUPON', '쿠폰행사',     1, 'SYSTEM', 'SYSTEM'),
('PROM_TP_CD', 'PROM',   '프로모션행사', 2, 'SYSTEM', 'SYSTEM'),
('PROM_TP_CD', 'GIFT',   '사은품행사',   3, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- DC_TP_CD 할인 유형
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('DC_TP_CD', 'RATE', '정률할인', 1, 'SYSTEM', 'SYSTEM'),
('DC_TP_CD', 'AMT',  '정액할인', 2, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- ISS_TP_CD 쿠폰 발급 유형
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('ISS_TP_CD', 'MANUAL', '수동발급', 1, 'SYSTEM', 'SYSTEM'),
('ISS_TP_CD', 'AUTO',   '자동발급', 2, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- AUTO_ISS_COND_CD 쿠폰 자동발급 조건
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('AUTO_ISS_COND_CD', 'JOIN',     '회원가입', 1, 'SYSTEM', 'SYSTEM'),
('AUTO_ISS_COND_CD', 'BIRTHDAY', '생일',     2, 'SYSTEM', 'SYSTEM'),
('AUTO_ISS_COND_CD', 'GRD_UP',   '등급업',   3, 'SYSTEM', 'SYSTEM');

-- ---------------------------------
-- EVT_TP_CD 이벤트 유형
-- ---------------------------------
INSERT INTO ST_CODE_DTL (CODE_GRP_CD, CODE_CD, CODE_NM, SORT_ORD, REG_ID, MOD_ID) VALUES
('EVT_TP_CD', 'PLAN',   '기획전', 1, 'SYSTEM', 'SYSTEM'),
('EVT_TP_CD', 'BANNER', '배너',   2, 'SYSTEM', 'SYSTEM'),
('EVT_TP_CD', 'EVENT',  '이벤트', 3, 'SYSTEM', 'SYSTEM');
