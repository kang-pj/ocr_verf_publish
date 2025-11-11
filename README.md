# OCR 이미지 전산화 검증 시스템

## 구현 내용

### 1. 메인 페이지 (main.html)
- 부트스트랩 4 기반 SB Admin 2 템플릿 사용
- 헤더, 사이드바, 푸터 레이아웃 구성
- 검색 필터 영역 (부서, 직급, 상태)
- DataTables를 활용한 OCR 결과 목록 테이블

### 2. JavaScript (ocrResult.js)
- DataTables 한글 설정
- AJAX 기반 데이터 로딩
- 체크박스 전체 선택/해제 기능
- 검색 및 초기화 기능
- 엑셀 다운로드 기능
- Select2 셀렉트박스 스타일링

### 3. 주요 기능

#### 테이블 컬럼
- 체크박스 (전체 선택 가능)
- 번호 (자동 순번)
- 이미지명 (클릭 시 새 창에서 이미지 열기)
- 상태 (배지 스타일: 대기/진행중/완료)
- 경로
- 등록자/등록일시
- 처리자/처리일시
- 비고

#### 검색 기능
- 부서별 필터
- 직급별 필터
- 상태별 필터
- 검색/초기화 버튼

#### 기타 기능
- 페이징 처리 (10/25/50/100건)
- 정렬 기능
- 엑셀 다운로드

## 백엔드 연동 필요사항

### API 엔드포인트

1. **OCR 결과 목록 조회**
   - URL: `/rf_ocr_verf/api/getOcrResultList`
   - Method: POST
   - Request:
     ```json
     {
       "searchDptCd": "부서코드",
       "searchDtyCd": "직급코드",
       "searchStatus": "상태"
     }
     ```
   - Response: `sample-data.json` 참고

2. **엑셀 다운로드**
   - URL: `/rf_ocr_verf/api/exportOcrResultExcel`
   - Method: GET
   - Query Parameters: searchDptCd, searchDtyCd, searchStatus

## 테스트 방법

실제 API가 없는 경우 `ocrResult.js`의 ajax 부분을 임시로 수정:

```javascript
// ajax 대신 임시 데이터 사용
ajax: function(data, callback, settings) {
    $.getJSON('sample-data.json', function(json) {
        callback(json);
    });
}
```

## 커스터마이징

- 테이블 컬럼 추가/삭제: `ocrResult.js`의 `columns` 배열 수정
- 페이지당 행 수 변경: `pageLength` 및 `lengthMenu` 수정
- 스타일 변경: 부트스트랩 클래스 또는 커스텀 CSS 추가
