-- ========================================
-- 1단계: base_doc 확인 (OCR_CNTS 디코딩)
-- ========================================
WITH base_doc AS (
    SELECT 
        INF.OCR_DOC_NO,
        INF.CTRL_YR,
        INF.INST_CD,
        INF.PRDT_CD,
        INF.CTRL_NO,
        INF.DOC_TP_CD,
        RSLT.OCR_RSLT_NO,
        PKG_PGCRYPTO$DECRYPT(RSLT.OCR_CNTS) AS OCR_CNTS
    FROM RFDB.OCR_DOC_INF INF
    LEFT JOIN RFDB.OCR_DOC_RSLT RSLT 
        ON RSLT.OCR_DOC_NO = INF.OCR_DOC_NO
    WHERE INF.CTRL_YR = '25'
        AND INF.INST_CD = '47'
        AND INF.PRDT_CD = '820'
        AND INF.CTRL_NO = '000903'
        AND INF.DOC_TP_CD = 'H03'
    LIMIT 1
)
SELECT 
    OCR_DOC_NO,
    INST_CD,
    PRDT_CD,
    DOC_TP_CD,
    OCR_CNTS IS NOT NULL AS HAS_OCR_CNTS,
    LENGTH(OCR_CNTS::TEXT) AS OCR_CNTS_LENGTH,
    LEFT(OCR_CNTS::TEXT, 100) AS OCR_CNTS_PREVIEW
FROM base_doc;

-- ========================================
-- 2단계: json_parsed 확인 (JSON 파싱)
-- ========================================
WITH base_doc AS (
    SELECT 
        INF.INST_CD,
        INF.PRDT_CD,
        PKG_PGCRYPTO$DECRYPT(RSLT.OCR_CNTS) AS OCR_CNTS
    FROM RFDB.OCR_DOC_INF INF
    LEFT JOIN RFDB.OCR_DOC_RSLT RSLT 
        ON RSLT.OCR_DOC_NO = INF.OCR_DOC_NO
    WHERE INF.CTRL_YR = '25'
        AND INF.INST_CD = '47'
        AND INF.PRDT_CD = '820'
        AND INF.CTRL_NO = '000903'
        AND INF.DOC_TP_CD = 'H03'
    LIMIT 1
),
json_parsed AS (
    SELECT 
        (JSON_EACH_TEXT(OCR_CNTS::JSON)).KEY AS ITEM_CD,
        (JSON_EACH_TEXT(OCR_CNTS::JSON)).VALUE AS ITEM_VALUE,
        INST_CD,
        PRDT_CD
    FROM base_doc
    WHERE OCR_CNTS IS NOT NULL
)
SELECT * FROM json_parsed LIMIT 10;

-- ========================================
-- 3단계: C_OCR_DOC_ITEM 테이블 확인
-- ========================================
SELECT 
    INST_CD,
    PRDT_CD,
    ITEM_CD,
    ITEM_NM,
    USE_YN,
    ITEM_ODR
FROM RFDB.C_OCR_DOC_ITEM
WHERE INST_CD = '47'
    AND PRDT_CD = '820'
    AND USE_YN = 'Y'
ORDER BY ITEM_ODR
LIMIT 20;

-- ========================================
-- 4단계: 조인 테스트
-- ========================================
WITH base_doc AS (
    SELECT 
        INF.INST_CD,
        INF.PRDT_CD,
        PKG_PGCRYPTO$DECRYPT(RSLT.OCR_CNTS) AS OCR_CNTS
    FROM RFDB.OCR_DOC_INF INF
    LEFT JOIN RFDB.OCR_DOC_RSLT RSLT 
        ON RSLT.OCR_DOC_NO = INF.OCR_DOC_NO
    WHERE INF.CTRL_YR = '25'
        AND INF.INST_CD = '47'
        AND INF.PRDT_CD = '820'
        AND INF.CTRL_NO = '000903'
        AND INF.DOC_TP_CD = 'H03'
    LIMIT 1
),
json_parsed AS (
    SELECT 
        (JSON_EACH_TEXT(OCR_CNTS::JSON)).KEY AS ITEM_CD,
        (JSON_EACH_TEXT(OCR_CNTS::JSON)).VALUE AS ITEM_VALUE,
        INST_CD,
        PRDT_CD
    FROM base_doc
    WHERE OCR_CNTS IS NOT NULL
)
SELECT 
    jp.ITEM_CD,
    jp.ITEM_VALUE,
    jp.INST_CD,
    jp.PRDT_CD,
    codi.ITEM_NM,
    codi.INST_CD AS CODI_INST_CD,
    codi.PRDT_CD AS CODI_PRDT_CD
FROM json_parsed jp
LEFT JOIN RFDB.C_OCR_DOC_ITEM codi 
    ON codi.ITEM_CD = jp.ITEM_CD
    AND codi.USE_YN = 'Y'
LIMIT 10;
