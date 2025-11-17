-- OCR 결과 조회 테스트 쿼리
-- 관리번호: 25-47-820-000903, 서류: H03

WITH base_doc AS (
    -- 1단계: 기본 문서 정보 조회
    SELECT 
        INF.OCR_DOC_NO,
        INF.CTRL_YR,
        INF.INST_CD,
        INF.PRDT_CD,
        INF.CTRL_NO,
        INF.DOC_TP_CD,
        INF.DOC_FL_SAV_PTH_NM,
        INF.DOC_FL_EXT,
        INF.OCR_YN,
        INF.INS_DTTM,
        RSLT.OCR_RSLT_NO,
        PKG_PGCRYPTO$DECRYPT(RSLT.OCR_CNTS) AS OCR_CNTS,
        RSLT.SAVE_YN,
        RSLT.SAVE_DTTM,
        RSLT.VERF_YN,
        RSLT.VERF_DTTM,
        RSLT.REPLACED,
        PKG_PGCRYPTO$DECRYPT(JSN.JSON_VALUE) AS JSON_VALUE,
        COUNT(*) OVER() AS DOC_NUM
    FROM RFDB.OCR_DOC_INF INF
    LEFT JOIN RFDB.OCR_DOC_RSLT RSLT 
        ON RSLT.OCR_DOC_NO = INF.OCR_DOC_NO
    LEFT JOIN RFDB.OCR_DOC_JSON JSN 
        ON RSLT.OCR_RSLT_NO = JSN.OCR_RSLT_NO
    WHERE 1=1
        AND INF.CTRL_YR = '25'
        AND INF.INST_CD = '47'
        AND INF.PRDT_CD = '820'
        AND INF.CTRL_NO = '000903'
        AND INF.DOC_TP_CD = 'H03'
        AND (INF.OCR_DOC_NO = RSLT.OCR_DOC_NO OR RSLT.OCR_DOC_NO IS NULL)
    ORDER BY 
        RSLT.OCR_DOC_NO ASC,
        CAST(PKG_PGCRYPTO$DECRYPT(RSLT.OCR_CNTS)::TEXT::JSON ->> 'PAGE' AS INT) ASC
    LIMIT 1
),
json_parsed AS (
    -- 2단계: JSON 데이터를 Key-Value로 파싱
    SELECT 
        (JSON_EACH_TEXT(OCR_CNTS::JSON)).KEY AS ITEM_CD,
        (JSON_EACH_TEXT(OCR_CNTS::JSON)).VALUE AS ITEM_VALUE,
        base_doc.*
    FROM base_doc
    WHERE OCR_CNTS IS NOT NULL
),
item_mapped AS (
    -- 3단계: 아이템 코드를 한글명으로 매핑
    SELECT 
        CODI.ITEM_NM,
        json_parsed.*,
        RANK() OVER (
            PARTITION BY CODI.ITEM_CD 
            ORDER BY CODI.PRDT_CD, CODI.INST_CD
        ) AS RANK
    FROM json_parsed
    LEFT JOIN RFDB.C_OCR_DOC_ITEM CODI 
        ON CODI.ITEM_CD = json_parsed.ITEM_CD
        AND CODI.USE_YN = 'Y'
)
-- 4단계: 중복 제거 (RANK = 1만 선택)
SELECT 
    ITEM_CD,
    ITEM_NM,
    ITEM_VALUE,
    INST_CD,
    PRDT_CD,
    DOC_TP_CD
FROM item_mapped
WHERE RANK = 1
ORDER BY ITEM_CD;
