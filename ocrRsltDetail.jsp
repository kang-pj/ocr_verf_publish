<%@ page pageEncoding="utf-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/viewerjs/1.11.7/viewer.min.css">
    <script src="${pageContext.request.contextPath}/resources/viewerjs/1.11.7/viewer.min.js"></script>
</head>
<body>
<style></style>
    body {
        font-family: 'Noto Sans KR', sans-serif;
        min-width: 1200px;
    }
    #wrapper {
        display: flex;
    }
    .sidebar {
        min-height: 100vh;
        width: 250px;
        background: linear-gradient(180deg, #4e73df 10%, #224abe 100%);
    }
    .sidebar-brand {
        height: 80px;
        text-decoration: none;
        color: white !important;
    }
    .sidebar-brand-icon i {
        color: white;
    }
    .sidebar-brand-text {
        font-size: 12px;
        font-weight: 600;
    }
    .sidebar .nav-item .nav-link {
        color: rgba(255, 255, 255, 0.8);
        padding: 1rem;
    }
    .sidebar .nav-item .nav-link:hover {
        color: white;
        background-color: rgba(255, 255, 255, 0.1);
    }
    .sidebar .nav-item.active .nav-link {
        color: white;
        background-color: rgba(255, 255, 255, 0.15);
    }
    .sidebar-divider {
        border-top: 1px solid rgba(255, 255, 255, 0.15);
        margin: 0;
    }
    .bg-gradient-primary {
        background: linear-gradient(180deg, #4e73df 10%, #224abe 100%);
    }
    #content-wrapper {
        flex: 1;
        background-color: #f8f9fc;
        display: flex;
        flex-direction: column;
        min-height: 100vh;
    }
    #content {
        flex: 1;
    }
    .topbar {
        background-color: white;
        box-shadow: 0 0.15rem 1.75rem 0 rgba(58, 59, 69, 0.15);
    }
    .custom-navbar-height {
        height: 60px;
    }
    .sticky-footer {
        background-color: white;
        padding: 1rem 0;
    }
    .scroll-to-top {
        position: fixed;
        right: 1rem;
        bottom: 1rem;
        display: none;
        width: 2.75rem;
        height: 2.75rem;
        text-align: center;
        color: white;
        background: rgba(90, 92, 105, 0.5);
        line-height: 46px;
        border-radius: 50%;
    }
    .scroll-to-top:hover {
        background: rgba(90, 92, 105, 0.7);
        color: white;
        text-decoration: none;
    }
    .info-label {
        font-weight: 600;
        color: #5a5c69;
        padding: 0.5rem !important;
        font-size: 0.85rem;
    }
    .info-value {
        color: #3a3b45;
        padding: 0.5rem !important;
        font-size: 0.85rem;
    }
    .card-header h6 {
        font-size: 0.9rem;
    }
    .card-header .badge {
        font-size: 0.75rem;
    }
    #imageViewer img {
        max-width: 100%;
        height: auto;
        box-shadow: 0 0 10px rgba(0,0,0,0.1);
    }
    .page-item {
        cursor: pointer;
        padding: 10px;
        margin-bottom: 10px;
        border: 2px solid #e3e6f0;
        border-radius: 5px;
        transition: all 0.3s;
    }
    .page-item:hover {
        border-color: #4e73df;
        background-color: #f8f9fc;
    }
    .page-item.active {
        border-color: #4e73df;
        background-color: #e7f1ff;
    }
    .page-thumbnail {
        width: 100%;
        height: auto;
        border-radius: 3px;
    }
    .doc-type-item {
        display: block;
        padding: 5px 8px;
        margin-bottom: 8px;
        border: 1px solid #e3e6f0;
        border-radius: 5px;
        text-decoration: none;
        color: #5a5c69;
        font-size: 0.8rem;
        transition: all 0.3s;
    }
    .doc-type-item:hover {
        background-color: #f8f9fc;
        border-color: #4e73df;
        color: #4e73df;
        text-decoration: none;
    }
    .doc-type-item.current {
        background-color: #e7f1ff;
        border-color: #4e73df;
        color: #4e73df;
        font-weight: 600;
        cursor: default;
    }
    .doc-type-item i {
        margin-right: 5px;
    }
    /* OCR 결과 테이블 스타일 */
    #ocrResultTable {
        font-size: 12px;
    }
    #ocrResultTable thead th {
        background-color: #f8f9fc;
        font-weight: 500;
        text-align: center;
        vertical-align: middle;
        padding: 0.2rem 0.4rem !important;
        line-height: 1.7;
    }
    #ocrResultTable tbody td {
        vertical-align: middle;
        padding: 0.2rem 0.4rem !important;
        line-height: 1.7;
    }
    #ocrResultTable tbody td:nth-child(1) {
        color: #333;
        font-size: 11px;
    }
    #ocrResultTable tbody td:nth-child(2) {
        font-weight: 500;
    }
    #ocrResultTable tbody td:nth-child(3) {
        word-break: break-all;
    }
    /* 뱃지 스타일 */
    .badge {
        font-weight: 100;
        padding: 0.3em 0.9em;
        border-radius: 3px;
        font-size: 12px;
    }
    /* 뱃지 색상 - 파스텔 톤 */
    .badge-success {
        background-color: #d4edda !important;
        color: #28a745 !important;
    }
    .badge-warning {
        background-color: #fff3cd !important;
        color: #856404 !important;
    }
    .badge-danger {
        background-color: #f8d7da !important;
        color: #dc3545 !important;
    }
    .badge-secondary {
        background-color: #e2e3e5 !important;
        color: #6c757d !important;
    }
</style>

<!-- Content Wrapper -->
<div id="content-wrapper" class="d-flex flex-column">
    <div id="content">

        <!-- Main Content -->
        <div class="container-fluid" style="min-width: 1200px;">
            <!-- 페이지 헤더 -->
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h4 class="mb-0 text-gray-800">
                    <i class="fas fa-file-alt"></i> OCR 문서 상세
                </h4>
                <button type="button" class="btn btn-secondary btn-sm" onclick="window.close()">
                    <i class="fas fa-times"></i> 닫기
                </button>
            </div>

            <!-- 상단: 서류 정보 (4x3 테이블) -->
            <div class="card shadow mb-4">
                <div class="card-header py-3 d-flex justify-content-between align-items-center">
                    <h6 class="m-0 font-weight-bold text-primary">서류 정보</h6>
                    <span class="badge badge-info" id="statusBadge"></span>
                </div>
                <div class="card-body p-0">
                    <table class="table table-bordered mb-0">
                        <tbody>
                        <tr>
                            <td class="info-label bg-light" style="width: 15%;">관리번호</td>
                            <td class="info-value" style="width: 35%;" id="ctrlNo">-</td>
                            <td class="info-label bg-light" style="width: 15%;">기관-상품명</td>
                            <td class="info-value" style="width: 35%;" id="orgProduct">-</td>
                        </tr>
                        <tr>
                            <td class="info-label bg-light">문서유형</td>
                            <td class="info-value" id="docTpCd">-</td>
                            <td class="info-label bg-light">등록일시</td>
                            <td class="info-value" id="insDttm">-</td>
                        </tr>
                        <tr>
                            <td class="info-label bg-light">서류 Title</td>
                            <td class="info-value" id="docTitle">-</td>
                            <td class="info-label bg-light">검증여부 (OCR 결과번호)</td>
                            <td class="info-value">
                                <span id="ocrYn">-</span>
                                <span class="text-muted ml-2" id="ocrDocNo">(-)</span>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- 중단: 좌우 레이아웃 -->
            <div class="d-flex" style="gap: 15px;">
                <!-- 좌측: 이미지 뷰어 영역 (flex-grow) -->
                <div style="flex: 1;">
                    <div class="card shadow mb-4" style="height: 1000px; display: flex; flex-direction: column;">
                        <div class="card-header py-2">
                            <h6 class="m-0 font-weight-bold text-primary">이미지 뷰어</h6>
                        </div>
                        <div class="card-body d-flex flex-column" style="flex: 1; overflow: hidden; background-color: #f8f9fa; position: relative;">
                            <div id="imageViewer" class="text-center" style="flex: 1; overflow: auto; width: 100%;">
                                <p class="text-muted">이미지를 불러오는 중...</p>
                            </div>
                            <div id="pageNavigation" style="padding: 10px; text-align: center; border-top: 1px solid #e3e6f0; flex-shrink: 0;"></div>
                        </div>
                    </div>
                </div>

                <!-- 중앙: OCR 결과 -->
                <div style="flex: 0.5;">
                    <div class="card shadow mb-4" style="height: 1000px;">
                        <div class="card-header py-2">
                            <h6 class="m-0 font-weight-bold text-primary">OCR 결과</h6>
                        </div>
                        <div class="card-body p-2" style="height: calc(100% - 50px); overflow-y: auto;">
                            <table class="table table-sm table-bordered table-hover" id="ocrResultTable">
                                <thead>
                                <tr>
                                    <th style="width: 25%;">한글명</th>
                                    <th>추출값</th>
                                    <th style="width: 38px">상태</th>
                                </tr>
                                </thead>
                                <tbody id="ocrResultBody">
                                <tr>
                                    <td colspan="4" class="text-center text-muted">OCR 결과가 없습니다.</td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- 우측: 서류 리스트 (168px 고정) -->
                <div style="width: 168px; flex-shrink: 0;">
                    <div class="card shadow mb-4" style="height: 1000px;">
                        <div class="card-header py-2">
                            <h6 class="m-0 font-weight-bold text-primary">서류 리스트</h6>
                        </div>
                        <div class="card-body p-2" style="height: calc(100% - 50px); overflow-y: auto;">
                            <div id="documentTypeList">
                                <a href="#" class="doc-type-item">
                                    <i class="fas fa-file-alt"></i><div><small> 페이지</small></div>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>


<script>
    // 전역 변수
    var ocrDocNoList = [];
    var currentIndex = 0;

    $(document).ready(function() {
        // URL 파라미터에서 관리번호 정보 가져오기
        var urlParams = new URLSearchParams(window.location.search);
        var ctrlYr = urlParams.get('ctrl_yr');
        var instCd = urlParams.get('inst_cd');
        var prdtCd = urlParams.get('prdt_cd');
        var ctrlNo = urlParams.get('ctrl_no');
        var docTpCd = urlParams.get('doc_tp_cd');
        var ocrDocNo = urlParams.get('ocr_doc_no');

        // 문서 상세 정보 로드
        loadDocumentDetail(ctrlYr, instCd, prdtCd, ctrlNo, docTpCd, ocrDocNo);
    });

    /**
     * 문서 상세 정보 로드
     */
    function loadDocumentDetail(ctrlYr, instCd, prdtCd, ctrlNo, docTpCd, ocrDocNo) {
        console.log('문서 상세 조회:', ctrlYr, instCd, prdtCd, ctrlNo, docTpCd, 'ocrDocNo:', ocrDocNo);

        var params = {
            ctrl_yr: ctrlYr,
            inst_cd: instCd,
            prdt_cd: prdtCd,
            ctrl_no: ctrlNo,
            doc_tp_cd: docTpCd || null,
            ocr_doc_no: ocrDocNo || null
        };

        $.ajax({
            url: '/rf-ocr-verf/api/getOcrDocumentDetail.do',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: function(response) {
                console.log('서버 응답:', response);

                if (response.success && response.data) {
                    // 전역 변수에 저장
                    ocrDocNoList = response.ocrDocNoList || [];
                    currentIndex = response.currentIndex || 0;

                    // 기본 정보 표시
                    displayDocumentInfo(response.data);

                    // 서류 목록 표시
                    displayDocumentList(response.documentList || [], docTpCd);

                    // OCR 결과 표시
                    displayOcrResults(response.ocrResults || []);

                    // 복수 이미지 정보 표시 (비동기)
                    displayMultipleImages(response.data, response.documentList || []);

                    // 페이지 네비게이션 표시
                    displayPageNavigation(response.totalPages || 1);
                } else {
                    alert('문서 정보를 불러올 수 없습니다.');
                    $('#documentTypeList').html('<p class="text-center text-danger">데이터를 불러오지 못했습니다.</p>');
                }
            },
            error: function() {
                console.error('서버 연결 실패');
                alert('서버 연결에 실패했습니다.');
                $('#documentTypeList').html('<p class="text-center text-danger">서버 연결에 실패했습니다.</p>');
            }
        });
    }

    /**
     * 문서 기본 정보 표시
     */
    function displayDocumentInfo(data) {
        console.log('문서 정보 표시:', data);

        if (!data) {
            console.error('문서 데이터가 없습니다.');
            return;
        }

        // 관리번호 조합
        var ctrlNo = (data.ctrl_yr || '-') + '-' + (data.inst_cd || '-') + '-' + (data.prdt_cd || '-') + '-' + (data.ctrl_no || '-');
        $('#ctrlNo').text(ctrlNo);

        // 등록일시
        $('#insDttm').text(data.ins_dttm || '-');

        // 문서유형
        $('#docTpCd').text(data.doc_tp_cd || '-');

        // 한글 서류명 (doc_title 또는 doc_fl_nm 사용)
        var docTitle = data.doc_title || data.doc_fl_nm || '-';
        $('#docTitle').text(docTitle);

        // 기관-상품명 (매핑 필요 시 추가)
        var orgProduct = (data.inst_cd || '-') + '-' + (data.prdt_cd || '-');
        $('#orgProduct').text(orgProduct);

        // 검증여부
        var ocrYnText = '오류';
        var badgeClass = 'badge-secondary';
        if (data.ocr_yn === 'Y') {
            ocrYnText = '완료';
            badgeClass = 'badge-success';
        } else if (data.ocr_yn === 'N') {
            ocrYnText = '대기';
            badgeClass = 'badge-warning';
        } else if (data.ocr_yn === 'X') {
            ocrYnText = '실패';
            badgeClass = 'badge-danger';
        }
        $('#ocrYn').text(ocrYnText);
        $('#statusBadge').removeClass('badge-info badge-success badge-warning badge-danger badge-secondary').addClass(badgeClass).text(ocrYnText);

        // OCR 결과번호
        if (data.ocr_doc_no) {
            $('#ocrDocNo').text('(' + data.ocr_doc_no + ')');
        } else {
            $('#ocrDocNo').text('(-)');
        }

        console.log('문서 정보 표시 완료');
    }

    /**
     * 서류 목록 표시
     */
    function displayDocumentList(documents, currentDocTpCd) {
        if (!documents || documents.length === 0) {
            $('#documentTypeList').html('<p class="text-center text-muted">서류가 없습니다.</p>');
            return;
        }

        var html = '';

        documents.forEach(function(doc) {
            var isCurrent = doc.doc_tp_cd === currentDocTpCd;
            var itemClass = isCurrent ? 'doc-type-item current' : 'doc-type-item';
            var icon = isCurrent ? '<i class="fas fa-check-circle text-success"></i>' : '<i class="fas fa-file-alt"></i>';

            if (isCurrent) {
                html += '<div class="' + itemClass + '">';
                html += icon + ' ' + (doc.doc_title || doc.doc_tp_cd);
                html += '<div><small>' + (doc.doc_page || 0) + '페이지</small></div>';
                html += '</div>';
            } else {
                html += '<a href="#" class="' + itemClass + '" onclick="changeDocument(\'' + doc.doc_tp_cd + '\'); return false;">';
                html += icon + ' ' + (doc.doc_title || doc.doc_tp_cd);
                html += '<div><small>' + (doc.doc_page || 0) + '페이지</small></div>';
                html += '</a>';
            }
        });

        $('#documentTypeList').html(html);
    }

    /**
     * 서류 변경
     */
    function changeDocument(docTpCd) {
        var urlParams = new URLSearchParams(window.location.search);
        urlParams.set('doc_tp_cd', docTpCd);
        urlParams.delete('ocr_doc_no'); // OCR 문서 번호 초기화
        window.location.search = urlParams.toString();
    }

    /**
     * 페이지 네비게이션 표시
     */
    function displayPageNavigation(totalPages) {
        if (!totalPages || totalPages <= 1) {
            return; // 페이지가 1개 이하면 네비게이션 불필요
        }

        var html = '<div class="btn-group" role="group">';

        // 이전 버튼
        if (currentIndex > 0) {
            html += '<button type="button" class="btn btn-sm btn-outline-primary" onclick="changePage(' + (currentIndex - 1) + ')">';
            html += '<i class="fas fa-chevron-left"></i> 이전';
            html += '</button>';
        }

        // 페이지 정보
        html += '<button type="button" class="btn btn-sm btn-primary" disabled>';
        html += (currentIndex + 1) + ' / ' + totalPages;
        html += '</button>';

        // 다음 버튼
        if (currentIndex < totalPages - 1) {
            html += '<button type="button" class="btn btn-sm btn-outline-primary" onclick="changePage(' + (currentIndex + 1) + ')">';
            html += '다음 <i class="fas fa-chevron-right"></i>';
            html += '</button>';
        }

        html += '</div>';

        // pageNavigation 컨테이너에 추가
        $('#pageNavigation').html(html);
    }


    /**
     * 페이지 변경
     */
    function changePage(index) {
        if (index < 0 || index >= ocrDocNoList.length) {
            return;
        }

        var urlParams = new URLSearchParams(window.location.search);
        urlParams.set('ocr_doc_no', ocrDocNoList[index]);
        window.location.search = urlParams.toString();
    }

    /**
     * 상태 뱃지 생성
     */
    function getStatusBadge(status) {
        if (status === 'Y') {
            return '<span class="badge badge-success">완료</span>';
        } else if (status === 'N') {
            return '<span class="badge badge-warning">대기</span>';
        } else if (status === 'X') {
            return '<span class="badge badge-danger">실패</span>';
        } else {
            return '<span class="badge badge-secondary">오류</span>';
        }
    }


    /**
     * OCR 결과 표시
     */
    function displayOcrResults(ocrResults) {
        var tbody = $('#ocrResultTable tbody');

        if (!tbody.length) {
            console.error('OCR 결과 테이블 tbody를 찾을 수 없습니다.');
            return;
        }

        if (!ocrResults || ocrResults.length === 0) {
            console.warn('OCR 결과가 없습니다.');
            tbody.html('<tr><td colspan="3" class="text-center text-muted">OCR 결과가 없습니다.</td></tr>');
            return;
        }

        console.log('OCR 결과 데이터 개수:', ocrResults.length);
        console.log('첫 번째 항목:', ocrResults[0]);

        var html = '';

        // 서버에서 받은 데이터를 테이블 행으로 변환
        ocrResults.forEach(function(item, index) {
            if (!item) {
                console.warn('항목 ' + index + '이 null입니다.');
                return;
            }

            // 필드명 정규화 (snake_case 또는 camelCase 모두 지원)
            var itemName = item.item_nm || item.itemNm || item.item_cd || item.itemCd || '-';
            var itemValue = item.item_value || item.itemValue || '';
            var itemCd = item.item_cd || item.itemCd || '';

            console.log('항목 ' + index + ':', {
                cd: itemCd,
                name: itemName,
                value: itemValue
            });

            // 빈 값 체크
            var isEmpty = !itemValue ||
                itemValue.trim() === '' ||
                itemValue === 'null' ||
                itemValue === 'undefined';

            var statusIcon = isEmpty
                ? '<i class="fas fa-circle text-danger"></i>'
                : '<i class="fas fa-circle text-success"></i>';

            html += '<tr>';
            html += '<td>' + escapeHtml(itemName) + '</td>';
            html += '<td>' + escapeHtml(itemValue) + '</td>';
            html += '<td class="text-center">' + statusIcon + '</td>';
            html += '</tr>';
        });

        tbody.html(html);
    }

    /**
     * HTML 특수문자 이스케이프
     */
    function escapeHtml(text) {
        if (!text) return '';
        var map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.toString().replace(/[&<>"']/g, function(m) { return map[m]; });
    }



    /**
     * 복수 이미지 표시
     */
    function displayMultipleImages(currentData, documentList) {
        console.log('복수 이미지 로딩 시작');
        console.log('currentData:', currentData);
        console.log('ocrDocNoList:', ocrDocNoList);

        if (!ocrDocNoList || ocrDocNoList.length === 0) {
            $('#imageViewer').html('<p class="text-muted">이미지 정보가 없습니다.</p>');
            return;
        }

        // 1단계: 각 ocr_doc_no의 이미지 정보 조회
        $.ajax({
            url: '/rf-ocr-verf/api/getOcrDocumentImages.do',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                ocr_doc_no_list: ocrDocNoList
            }),
            success: function(response) {
                console.log('이미지 정보 조회 응답:', response);
                
                if (response.success && response.data && response.data.length > 0) {
                    var imageInfoList = response.data;
                    console.log('조회된 이미지 정보:', imageInfoList);
                    
                    // 2단계: 이미지 로딩
                    loadMultipleImagesFromInfo(imageInfoList);
                } else {
                    console.error('이미지 정보 조회 실패');
                    $('#imageViewer').html('<p class="text-muted">이미지 정보를 불러올 수 없습니다.</p>');
                }
            },
            error: function(xhr, status, error) {
                console.error('이미지 정보 조회 오류:', error);
                $('#imageViewer').html('<p class="text-danger">이미지 정보 조회 중 오류가 발생했습니다.</p>');
            }
        });
    }

    /**
     * 이미지 정보 기반으로 복수 이미지 로딩
     */
    function loadMultipleImagesFromInfo(imageInfoList) {
        // 로딩바 표시
        var loadingHtml = '<div style="display: flex; align-items: center; justify-content: center; height: 100%;">';
        loadingHtml += '<div class="spinner-border text-primary" role="status">';
        loadingHtml += '<span class="sr-only">로딩 중...</span>';
        loadingHtml += '</div></div>';
        $('#imageViewer').html(loadingHtml);

        console.log('복수 이미지 로딩 시작 - 이미지 개수:', imageInfoList.length);

        // 첫 번째 이미지의 inst_cd, prdt_cd 사용
        var firstImage = imageInfoList[0];
        
        var params = {
            inst_cd: firstImage.inst_cd,
            prdt_cd: firstImage.prdt_cd,
            image_list: imageInfoList
        };

        console.log('이미지 로딩 파라미터:', params);

        $.ajax({
            url: '/rf-ocr-verf/api/getOcrImages.do',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            timeout: 60000,
            success: function(response) {
                console.log('이미지 로딩 응답:', response);

                if (response.success && response.data && response.data.length > 0) {
                    console.log('로드된 이미지 개수:', response.data.length);
                    
                    // 컨테이너 div 생성
                    var html = '<div id="viewerContainer" style="width: 100%; height: 100%;">';
                    html += '<ul style="list-style: none; padding: 0; margin: 0;">';
                    
                    response.data.forEach(function(imageData, index) {
                        console.log('이미지 ' + index + ' 처리 중...');
                        
                        if (imageData && imageData.startsWith('data:image')) {
                            html += '<li style="display: inline-block;">';
                            html += '<img src="' + imageData + '" alt="Page ' + (index + 1) + '" style="max-width: 100%; height: auto;">';
                            html += '</li>';
                        } else {
                            console.warn('이미지 ' + index + '이 base64 형식이 아닙니다.');
                        }
                    });
                    
                    html += '</ul>';
                    html += '</div>';
                    
                    $('#imageViewer').html(html);
                    
                    // Viewer.js 초기화
                    setTimeout(function() {
                        var container = document.getElementById('viewerContainer');
                        console.log('Viewer.js 초기화 시작 - 컨테이너:', container);
                        
                        if (container) {
                            var viewer = new Viewer(container, {
                                inline: true,
                                viewed: function() {
                                    console.log('Viewer.js viewed 이벤트 - 현재 인덱스:', viewer.index);
                                },
                                toolbar: {
                                    zoomIn: 4,
                                    zoomOut: 4,
                                    oneToOne: 4,
                                    reset: 4,
                                    prev: 4,
                                    play: false,
                                    next: 4,
                                    rotateLeft: 4,
                                    rotateRight: 4,
                                    flipHorizontal: 4,
                                    flipVertical: 4
                                },
                                navbar: true,
                                title: true,
                                tooltip: true,
                                movable: true,
                                zoomable: true,
                                rotatable: true,
                                scalable: true,
                                transition: true,
                                fullscreen: true,
                                keyboard: true,
                                initialViewIndex: currentIndex
                            });
                            
                            // 현재 인덱스로 이동
                            viewer.view(currentIndex);
                            
                            console.log('Viewer.js 초기화 완료 - 현재 인덱스:', currentIndex);
                        }
                    }, 300);
                } else {
                    console.error('이미지 로딩 실패 - 응답 데이터 없음');
                    $('#imageViewer').html('<p class="text-danger text-center">이미지를 불러올 수 없습니다.</p>');
                }
            },
            error: function(xhr, status, error) {
                console.error('이미지 로딩 AJAX 오류:', {status: status, error: error, xhr: xhr});
                
                var errorMsg = '서버 연결 실패';
                if (xhr.status === 404) {
                    errorMsg = 'API 엔드포인트 없음 (404)';
                } else if (xhr.status === 500) {
                    errorMsg = '서버 오류 (500)';
                } else if (status === 'timeout') {
                    errorMsg = '요청 시간 초과';
                }
                
                $('#imageViewer').html('<p class="text-danger text-center">' + errorMsg + '</p>');
            }
        });
    }



    /**
     * 이미지 로드 오류 처리
     */
    function handleImageError() {
        var errorHtml = '<div class="text-center p-3" style="font-size: 0.9rem;">';
        errorHtml += '<p class="text-danger mb-2">이미지 파일을 찾을 수 없습니다.</p>';
        errorHtml += '<p class="text-muted small">파일이 삭제되었거나 경로가 잘못되었을 수 있습니다.</p>';
        errorHtml += '</div>';
        $('#imageViewer').html(errorHtml);
    }

    /**
     * 이미지 경로 표시
     */
    function displayImagePath(data, imagePath, reason) {
        var html = '<div class="text-center p-3" style="font-size: 0.9rem;">';
        html += '<p class="text-danger mb-2">이미지를 불러올 수 없습니다.</p>';
        html += '<p class="text-muted small mb-2">' + (reason || '알 수 없는 오류') + '</p>';
        html += '<p class="text-break small" style="word-break: break-all; color: #999;">' + imagePath + '</p>';
        html += '</div>';
        $('#imageViewer').html(html);
    }





</script>

