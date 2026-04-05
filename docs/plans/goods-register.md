# 상품 등록 API 구현 계획

## 개요

`POST /api/admin/goods` — 상품 기본 정보, 이미지, 옵션 그룹 연결, 단품 생성을 단일 트랜잭션으로 처리한다.

---

## API 명세

```
POST /api/admin/goods
Content-Type: application/json
Authorization: Bearer {access_token}
```

### 요청 바디

```json
{
  "goodsNm": "티셔츠",
  "goodsTpCd": "NORMAL",
  "ctgNo": 3,
  "brandNo": 1,
  "salePrc": 29000,
  "goodsDesc": "상세 설명 (HTML)",
  "saleStatCd": "SALE",
  "dlvPolicyNo": "DLV001",
  "imgUrls": ["https://cdn.example.com/1.jpg", "https://cdn.example.com/2.jpg"],
  "optGrpCds": ["COLOR", "SIZE"],
  "units": [
    {
      "optItms": [
        { "optGrpCd": "COLOR", "optItmCd": "RED" },
        { "optGrpCd": "SIZE",  "optItmCd": "S"   }
      ],
      "addPrc": 0,
      "stockQty": 100
    }
  ]
}
```

> `imgUrls`: 최대 5장, 순서 = IMG_SEQ  
> `optGrpCds`: 연결할 옵션 그룹 코드 목록 (순서 = SORT_ORD)  
> `units`: 화면에서 옵션 조합이 확정된 단품 목록. `addPrc`는 기본가 대비 추가금액

### 응답

```json
{ "success": true, "data": { "goodsNo": "G000001" } }
```

---

## DB 설계

### 사용 테이블 (tables.md 기준)

| 테이블 | 설명 |
|--------|------|
| `PR_BRAND_BASE` | 브랜드 마스터 |
| `PR_DLV_POLICY` | 배송정책 마스터 |
| `PR_GOODS_BASE` | 상품 기본정보 (`GOODS_NO VARCHAR(15)` — `G` + 6자리 zero-pad) |
| `PR_GOODS_IMG` | 상품 이미지 (PK: GOODS_NO + IMG_SEQ) |
| `PR_OPT_GRP` | 옵션 그룹 (`OPT_GRP_CD VARCHAR(10)` — 코드 기반 PK) |
| `PR_OPT_ITM` | 옵션 항목 (복합 PK: OPT_GRP_CD + OPT_ITM_CD) |
| `PR_GOODS_OPT` | 상품 ↔ 옵션 그룹 연결 |
| `PR_UNIT_BASE` | 단품 (복합 PK: GOODS_NO + UNIT_SEQ, ADD_PRC·STOCK_QTY 직접 보유) |
| `PR_UNIT_OPT` | 단품 ↔ 옵션 항목 매핑 |

### TODO (tables.md 미정의 — 추후 별도 작업)

- `PR_UNIT_PRICE` (단품 가격 선분 테이블): ADD_PRC는 현재 PR_UNIT_BASE에 직접 저장. 선분 관리 테이블 추가 후 마이그레이션 예정
- `PR_GOODS_BASE.SALE_STR_DTM / SALE_END_DTM`: PRD에 판매시작·종료일시가 있으나 tables.md 미정의 → 컬럼 추가 후 반영 예정

---

## 구현 파일 목록

### SQL
- `backend/sql/goods_setup.sql` — 상품 도메인 테이블 DDL + `seq_goods_no` 시퀀스

### Enum
- `enums/GoodsSaleStatus.java` — SALE / STOP
- `enums/GoodsType.java` — NORMAL / EGIFT / GIFT

### Entity (9개)
- `entity/PrBrandBase.java`
- `entity/PrDlvPolicy.java`
- `entity/PrGoodsBase.java`
- `entity/PrGoodsImg.java`
- `entity/PrOptGrp.java`
- `entity/PrOptItm.java`
- `entity/PrGoodsOpt.java`
- `entity/PrUnitBase.java`
- `entity/PrUnitOpt.java`

### Mapper
- `mapper/goods/GoodsMapper.java` + `resources/mapper/goods/GoodsMapper.xml`
- `mapper/goods/UnitMapper.java` + `resources/mapper/goods/UnitMapper.xml`

### DTO
- `dto/goods/CreateGoodsRequest.java`
- `dto/goods/UnitOptRequest.java`
- `dto/goods/UnitRequest.java`
- `dto/goods/CreateGoodsResponse.java`

### Service / Controller
- `service/goods/GoodsService.java`
- `controller/goods/GoodsController.java`

### ErrorCode 추가
- `BRAND_NOT_FOUND`, `DLV_POLICY_NOT_FOUND`, `OPT_GRP_NOT_FOUND`, `OPT_ITM_NOT_FOUND`, `GOODS_IMG_LIMIT_EXCEEDED`

---

## 주요 로직

### 등록 트랜잭션 순서 (`GoodsService.createGoods`)

1. 이미지 5장 초과 검증
2. 카테고리 존재 확인 (`CategoryMapper.selectByCtgNo` 재사용)
3. 브랜드 존재 확인
4. 배송정책 존재 확인
5. 옵션 그룹 전체 존재 확인
6. 각 단품 옵션 항목 존재 확인
7. `PR_GOODS_BASE` insert → selectKey로 `GOODS_NO` 취득
8. `PR_GOODS_IMG` insert (IMG_SEQ 1부터 순번)
9. `PR_GOODS_OPT` insert
10. 각 단품 (UNIT_SEQ 1부터 순번): `PR_UNIT_BASE` → `PR_UNIT_OPT`

### GOODS_NO 생성 규칙

- 형식: `G` + 6자리 zero-padded 시퀀스 = 총 7자 (VARCHAR(15) 범위 이내)
- 예: `G000001`, `G000002` ... `G999999`

```sql
-- MyBatis selectKey (order="BEFORE")
SELECT 'G' || LPAD(CAST(nextval('seq_goods_no') AS TEXT), 6, '0')
```
