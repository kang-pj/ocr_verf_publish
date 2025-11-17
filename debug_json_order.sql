-- ========================================
-- 현재 getOcrResultText 쿼리 테스트
-- ========================================
WITH base_doc AS (
    SELECT
        INF.OCR_DOC_NO,
        INF.CTRL_YR,
        INF.INST_CD,
        INF.PRDT_CD,
        INF.CTRL_NO,
        INF.DOC_TP_CD,
        PKG_PGCRYPTO$DECRYPT(RSLT.OCR_CNTS) AS OCR_CNTS
    FROM RFDB.OCR_DOC_INF INF
    LEFT JOIN RFDB.OCR_DOC_RSLT RSLT
        ON RSLT.OCR_DOC_NO = INF.OCR_DOC_NO
    WHERE INF.OCR_DOC_NO = '25470820000903H0300001'  -- 실제 OCR_DOC_NO로 변경
    LIMIT 1
),
json_keys AS (
    -- 2단계: JSON 키 순서 보존
    SELECT
        ROW_NUMBER() OVER() AS JSON_SEQ,
        json_object_keys(OCR_CNTS::JSON) AS ITEM_CD,
        OCR_CNTS::JSON
    FROM base_doc
    WHERE OCR_CNTS IS NOT NULL
),
json_parsed AS (
    -- 3단계: 키에 해당하는 값 추출
    SELECT
        json_keys.JSON_SEQ,
        json_keys.ITEM_CD,
        json_keys.OCR_CNTS ->> json_keys.ITEM_CD AS ITEM_VALUE
    FROM json_keys
)
-- 4단계: 아이템 코드를 한글명으로 매핑
SELECT
    json_parsed.JSON_SEQ,
    json_parsed.ITEM_CD,
    json_parsed.ITEM_VALUE,
    COALESCE(CODI.ITEM_NM, json_parsed.ITEM_CD) AS ITEM_NM
FROM json_parsed
LEFT JOIN RFDB.C_OCR_DOC_ITEM CODI
    ON CODI.ITEM_CD = json_parsed.ITEM_CD
    AND CODI.USE_YN = 'Y'
ORDER BY json_parsed.JSON_SEQ;

-- ========================================
-- 비교: JSON_EACH_TEXT 사용 (기존 방식)
-- ========================================
WITH base_doc AS (
    SELECT
        INF.OCR_DOC_NO,
        PKG_PGCRYPTO$DECRYPT(RSLT.OCR_CNTS) AS OCR_CNTS
    FROM RFDB.OCR_DOC_INF INF
    LEFT JOIN RFDB.OCR_DOC_RSLT RSLT
        ON RSLT.OCR_DOC_NO = INF.OCR_DOC_NO
    WHERE INF.OCR_DOC_NO = '25470820000903H0300001'  -- 실제 OCR_DOC_NO로 변경
    LIMIT 1
),
json_parsed AS (
    SELECT
        ROW_NUMBER() OVER() AS JSON_SEQ,
        (JSON_EACH_TEXT(OCR_CNTS::JSON)).KEY AS ITEM_CD,
        (JSON_EACH_TEXT(OCR_CNTS::JSON)).VALUE AS ITEM_VALUE
    FROM base_doc
    WHERE OCR_CNTS IS NOT NULL
)
SELECT
    json_parsed.JSON_SEQ,
    json_parsed.ITEM_CD,
    json_parsed.ITEM_VALUE,
    COALESCE(CODI.ITEM_NM, json_parsed.ITEM_CD) AS ITEM_NM
FROM json_parsed
LEFT JOIN RFDB.C_OCR_DOC_ITEM CODI
    ON CODI.ITEM_CD = json_parsed.ITEM_CD
    AND CODI.USE_YN = 'Y'
ORDER BY json_parsed.JSON_SEQ;
