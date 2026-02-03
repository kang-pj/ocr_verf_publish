<%@ page pageEncoding="utf-8" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/viewerjs/1.11.7/viewer.min.css">
<script src="${pageContext.request.contextPath}/resources/viewerjs/1.11.7/viewer.min.js"></script>
<style>

    /* 로딩 오버레이 */
    #loadingOverlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(189, 189, 189, 0.50);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 9999;
        flex-direction: column;
    }

    #loadingOverlay.hidden {
        display: none;
    }

    .loading-spinner {
        border: 4px solid #f3f3f3;
        border-top: 4px solid #4e73df;
        border-radius: 50%;
        width: 50px;
        height: 50px;
        animation: spin 1s linear infinite;
    }

    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }

    .loading-text {
        margin-top: 20px;
        color: #4e73df;
        font-size: 16px;
        font-weight: 500;
    }

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
        text-align: center;
    }
    #ocrResultTable tbody td:nth-child(4) {
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

    /* OCR 재실행 버튼 */
    .btn-ocr-reset {
        background-color: #f8f9fa;
        border: 1px solid #d1d3e2;
        color: #5a5c69;
        font-size: 0.8rem;
        padding: 0.375rem 0.75rem;
    }
    .btn-ocr-reset:hover {
        background-color: #eaecf4;
        border-color: #c5c7d4;
        color: #5a5c69;
    }
    .btn-ocr-reset i {
        margin-right: 4px;
    }
</style>

<!-- 로딩 오버레이 -->
<div id="loadingOverlay">
    <div class="loading-spinner"></div>
    <div class="loading-text">데이터를 불러오는 중...</div>
</div>

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
                <div>
                    <button type="button" class="btn btn-sm btn-ocr-reset mr-2" id="downloadBtn" onclick="downloadFile()" style="display: none;">
                        <i class="fas fa-download"></i>
                        <span>현재 파일</span>
                    </button>
                    <button type="button" class="btn btn-sm btn-ocr-reset mr-2" id="downloadAllBtn" onclick="downloadAllFiles()" style="display: none;">
                        <i class="fas fa-file-archive"></i>
                        <span>전체 다운로드</span>
                    </button>
                    <button type="button" class="btn btn-sm btn-ocr-reset mr-2" id="resetOcrBtn" onclick="resetOcrStatus()">
                        <i class="fas fa-sync-alt"></i>
                        <span>OCR 재실행</span>
                    </button>
                    <button type="button" class="btn btn-secondary btn-sm" onclick="window.close()">
                        <i class="fas fa-times"></i> 닫기
                    </button>
                </div>
            </div>

            <!-- 상단: 서류 정보 (4x3 테이블) -->
            <div class="card shadow mb-4">
                <div class="card-header py-3 d-flex justify-content-between align-items-center">
                    <h6 class="m-0 font-weight-bold text-primary">서류 정보</h6>
                    <div>
                        <span class="badge badge-info" id="statusBadge"></span>
                    </div>
                </div>
                <div class="card-body p-0">
                    <table class="table table-bordered mb-0">
                        <tbody>
                        <tr>
                            <td class="info-label bg-light" style="width: 15%;">관리번호</td>
                            <td class="info-value" style="width: 35%;">
                                <span id="ctrlNo">-</span>
                                <button type="button" class="btn btn-sm btn-outline-secondary ml-2" id="copyCtrlNoBtn" onclick="copyToClipboard()" style="padding: 2px 8px; font-size: 0.75rem;">
                                    <i class="fas fa-copy"></i>
                                </button>
                            </td>
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
                            <div id="imageViewer" class="text-center" style="flex: 1; overflow: hidden; width: 100%; height: 100%; position: relative;">
                                <p class="text-muted">이미지를 불러오는 중...</p>
                            </div>
                            <div id="pageNavigation" style="padding: 10px; text-align: center; border-top: 1px solid #e3e6f0; flex-shrink: 0;"></div>
                        </div>
                    </div>
                </div>

                <!-- 중앙: OCR 결과 -->
                <div style="flex: 0.5;">
                    <div class="card shadow mb-4" style="height: 1000px;">
                        <div class="card-header py-2 d-flex justify-content-between align-items-center">
                            <h6 class="m-0 font-weight-bold text-primary">OCR 결과</h6>
                            <button type="button" class="btn btn-sm" id="checkCompletedBtn" onclick="toggleCheckCompleted()" style="display: none;">
                                <i class="fas fa-check-circle"></i>
                                <span id="checkCompletedText">미확인</span>
                            </button>
                        </div>
                        <div class="card-body p-2" style="height: calc(100% - 50px); overflow-y: auto;">
                            <table class="table table-sm table-bordered table-hover" id="ocrResultTable">
                                <thead>
                                <tr>
                                    <th style="width: 25%;">한글명</th>
                                    <th>추출값</th>
                                    <th style="width: 38px">상태</th>
                                    <th style="width: 38px">체크</th>
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
    // 전역 에러 핸들러 - 최우선 등록
    (function() {
        // JavaScript 런타임 에러 캐치
        window.onerror = function(message, source, lineno, colno, error) {
            console.error('전역 에러 발생:', message, 'at', source, lineno + ':' + colno);
            try {
                $('#loadingOverlay').removeClass('active').addClass('hidden');
            } catch(e) {
                document.getElementById('loadingOverlay').style.display = 'none';
            }
            return false;
        };

        // Promise rejection 에러 캐치
        window.addEventListener('unhandledrejection', function(event) {
            console.error('Promise rejection:', event.reason);
            try {
                $('#loadingOverlay').removeClass('active').addClass('hidden');
            } catch(e) {
                document.getElementById('loadingOverlay').style.display = 'none';
            }
        });
    })();

    // 전역 변수
    var ocrDocNoList = [];
    var currentIndex = 0;
    var currentOcrDocNo = null;
    var currentDocumentDetail = null;
    var currentImageInfoList = [];
    var isCheckCompleted = false; // 체크 완료 상태

    // 기관-상품명 매핑
    var organizationMapping = {
        '모바일반환보증': {
            inst_cd: ['01', '45', '47', '49'],
            prdt_cd: ['820']
        },
        '모바일임대보증': {
            inst_cd: ['01', '45', '47', '49'],
            prdt_cd: ['830']
        },
        '신한전세': {
            inst_cd: ['01'],
            prdt_cd: ['001', '003', '005', '002', '016', '041', '050', '118', '119', '120',
                '053', '200', '201', '202', '203', '217', '219', '220', '221', '007',
                '014', '020', '031', '129', '028', '037', '032', '038', '128', '029',
                '030', '036', '127', '027']
        },
        '전세안심보험(카손)': {
            inst_cd: ['61'],
            prdt_cd: ['L01']
        },
        '하나은행(사전)': {
            inst_cd: ['02'],
            prdt_cd: ['007', '222', '223', '224', '225']
        }
    };

    // 기관-상품명 한글 표기
    function getOrganizationName(instCd, prdtCd) {
        for (var orgName in organizationMapping) {
            var org = organizationMapping[orgName];
            if (org.inst_cd.includes(instCd) && org.prdt_cd.includes(prdtCd)) {
                return orgName;
            }
        }
        return instCd + '-' + prdtCd;
    }

    $(document).ready(function() {
        // jQuery AJAX 전역 에러 핸들러
        $(document).ajaxError(function(event, jqxhr, settings, thrownError) {
            console.error('AJAX 에러 발생:', thrownError);
            $('#loadingOverlay').removeClass('active').addClass('hidden');
        });

        // 관리자 모드 체크
        checkAdminMode();

        // URL 파라미터에서 관리번호 정보 가져오기
        var urlParams = new URLSearchParams(window.location.search);
        var ctrlYr = urlParams.get('ctrl_yr');
        var instCd = urlParams.get('inst_cd');
        var prdtCd = urlParams.get('prdt_cd');
        var ctrlNo = urlParams.get('ctrl_no');
        var docTpCd = urlParams.get('doc_tp_cd');
        var ocrDocNo = urlParams.get('ocr_doc_no');

        // 문서 상세 정보 로드
        try {
            loadDocumentDetail(ctrlYr, instCd, prdtCd, ctrlNo, docTpCd, ocrDocNo);
        } catch (error) {
            console.error('문서 로드 중 오류:', error);
            $('#loadingOverlay').removeClass('active').addClass('hidden');
            alert('문서를 불러오는 중 오류가 발생했습니다.');
        }
    });

    /**
     * 관리자 모드 체크 및 다운로드 버튼 표시 (30분 타임아웃)
     */
    function checkAdminMode() {
        var adminMode = sessionStorage.getItem('adminMode');
        var adminModeExpiry = sessionStorage.getItem('adminModeExpiry');

        if (adminMode === 'true' && adminModeExpiry) {
            var now = new Date().getTime();
            var expiryTime = parseInt(adminModeExpiry);

            // 만료 시간 체크
            if (now < expiryTime) {
                $('#downloadBtn').show();
                $('#downloadAllBtn').show();
            } else {
                // 만료됨 - 세션 제거
                sessionStorage.removeItem('adminMode');
                sessionStorage.removeItem('adminModeExpiry');
            }
        }
    }

    /**
     * 관리자 모드 활성화 (콘솔에서 호출)
     * 사용법: enableAdminMode()
     * 유지 시간: 30분
     */
    window.enableAdminMode = function() {
        var now = new Date().getTime();
        var expiryTime = now + (30 * 60 * 1000); // 30분 후

        sessionStorage.setItem('adminMode', 'true');
        sessionStorage.setItem('adminModeExpiry', expiryTime.toString());

        $('#downloadBtn').show();
        $('#downloadAllBtn').show();
    };

    /**
     * 관리자 모드 비활성화 (콘솔에서 호출)
     * 사용법: disableAdminMode()
     */
    window.disableAdminMode = function() {
        sessionStorage.removeItem('adminMode');
        sessionStorage.removeItem('adminModeExpiry');
        $('#downloadBtn').hide();
        $('#downloadAllBtn').hide();
    };

    /**
     * 문서 상세 정보 로드
     */
    function loadDocumentDetail(ctrlYr, instCd, prdtCd, ctrlNo, docTpCd, ocrDocNo) {
        // 로딩 오버레이 표시
        $('#loadingOverlay').removeClass('hidden');

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
                    currentOcrDocNo = response.data.ocr_doc_no;
                    currentDocumentDetail = response.data;
                    currentImageInfoList = response.imageInfoList || [];

                    // 기본 정보 표시
                    displayDocumentInfo(response.data);

                    // 서류 목록 표시
                    displayDocumentList(response.documentList || [], docTpCd);

                    // OCR 결과 표시 (extract 데이터와 함께)
                    displayOcrResults(response.ocrResults || [], response.extractData || []);

                    // 복수 이미지 정보 표시 (서버에서 받은 imageInfoList 사용)
                    if (response.imageInfoList && response.imageInfoList.length > 0) {
                        loadMultipleImagesFromInfo(response.imageInfoList);
                    } else {
                        $('#imageViewer').html('<p class="text-muted">이미지 정보가 없습니다.</p>');
                    }
                    /*
                                        // 페이지 네비게이션 표시 (이미지 로딩 상관없이 항상 표시)
                                        setTimeout(function() {
                                            displayPageNavigation(response.totalPages || 1);
                                        }, 100);*/
                } else {
                    alert('문서 정보를 불러올 수 없습니다.');
                    $('#documentTypeList').html('<p class="text-center text-danger">데이터를 불러오지 못했습니다.</p>');
                    $('#loadingOverlay').removeClass('active').addClass('hidden');
                }
            },
            error: function() {
                console.error('서버 연결 실패');
                alert('서버 연결에 실패했습니다.');
                $('#documentTypeList').html('<p class="text-center text-danger">서버 연결에 실패했습니다.</p>');
                $('#loadingOverlay').removeClass('active').addClass('hidden');
            }
        });
    }

    /**
     * 문서 기본 정보 표시
     */
    function displayDocumentInfo(data) {
        // 관리번호 조합
        var ctrlNo = data.ctrl_yr + '-' + data.inst_cd + '-' + data.prdt_cd + '-' + data.ctrl_no;
        $('#ctrlNo').text(ctrlNo);

        // 등록일시
        $('#insDttm').text(data.ins_dttm || '-');

        // 문서유형 (한글명)
        $('#docTpCd').text(data.doc_kr_nm || data.doc_tp_cd || '-');

        // 한글 서류명
        $('#docTitle').text(data.doc_title || '-');

        // 기관-상품명 (한글 표기)
        var orgProduct = getOrganizationName(data.inst_cd, data.prdt_cd);
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
        }
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

            // doc_kr_nm (한글명) 우선 표시, 없으면 doc_tp_cd
            var displayName = doc.doc_kr_nm || doc.doc_tp_cd;

            if (isCurrent) {
                html += '<div class="' + itemClass + '">';
                html += icon + ' ' + displayName;
                html += '<div><small>' + (doc.doc_page || 0) + '페이지</small></div>';
                html += '</div>';
            } else {
                html += '<a href="#" class="' + itemClass + '" onclick="changeDocument(\'' + doc.doc_tp_cd + '\'); return false;">';
                html += icon + ' ' + displayName;
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
     * OCR 결과 표시 (extract 데이터와 매칭)
     */
    function displayOcrResults(ocrResults, extractData) {
        var tbody = $('#ocrResultTable tbody');

        if (!tbody.length) {
            console.error('OCR 결과 테이블 tbody를 찾을 수 없습니다.');
            return;
        }

        if (!ocrResults || ocrResults.length === 0) {
            tbody.html('<tr><td colspan="4" class="text-center text-muted">OCR 결과가 없습니다.</td></tr>');
            $('#checkCompletedBtn').hide();
            return;
        }

        // extract 데이터를 키로 매핑 및 체크 완료 상태 확인
        var extractMap = {};
        isCheckCompleted = false;
        var hasExtractData = false; // extract 데이터 존재 여부
        
        if (extractData && extractData.length > 0) {
            extractData.forEach(function(extract) {
                if (extract.extract_key === '__CHECK_COMPLETED__') {
                    isCheckCompleted = (extract.extract_val === 'Y');
                } else {
                    extractMap[extract.extract_key] = extract;
                    hasExtractData = true; // 일반 데이터가 있으면 true
                }
            });
        }

        // 체크 완료 버튼 상태 업데이트
        updateCheckCompletedButton();

        var html = '';

        // 서버에서 받은 데이터를 테이블 행으로 변환
        ocrResults.forEach(function(item, index) {
            // null 체크
            if (!item) {
                console.warn('OCR 결과 항목이 null입니다. (인덱스: ' + index + ')');
                return;
            }

            var itemCd = item.item_cd || '';
            var itemName = item.item_nm || itemCd || '-';
            var itemValue = item.item_value || '';

            // extract 데이터와 매칭
            var extractInfo = extractMap[itemCd];
            var ocrFailType = extractInfo ? extractInfo.ocr_fail_type : null;

            // itemValue를 문자열로 변환
            var itemValueStr = itemValue != null ? String(itemValue) : '';

            // 체크박스 생성 - extract 데이터가 있을 때만 표시
            var checkboxHtml = '';
            if (hasExtractData) {
                if (ocrFailType === 'X' || ocrFailType === 'E') {
                    checkboxHtml = '<input type="checkbox" class="ocr-fail-check" data-item-cd="' + itemCd + '" data-item-value="' + itemValueStr + '" checked>';
                } else {
                    checkboxHtml = '<input type="checkbox" class="ocr-fail-check" data-item-cd="' + itemCd + '" data-item-value="' + itemValueStr + '">';
                }
            } else {
                checkboxHtml = '-'; // extract 데이터가 없으면 체크박스 대신 - 표시
            }

            // 빈 값 체크
            var isEmpty = !itemValueStr ||
                itemValueStr.trim() === '' ||
                itemValueStr === 'null' ||
                itemValueStr === 'undefined';

            var statusIcon = isEmpty
                ? '<i class="fas fa-circle text-danger"></i>'
                : '<i class="fas fa-circle text-success"></i>';

            html += '<tr>';
            html += '<td>' + itemName + '</td>';
            html += '<td>' + itemValueStr + '</td>';
            html += '<td class="text-center">' + checkboxHtml + '</td>';
            html += '<td class="text-center">' + statusIcon + '</td>';
            html += '</tr>';
        });

        tbody.html(html);

        // 체크박스 이벤트 핸들러 등록 (extract 데이터가 있을 때만)
        if (hasExtractData) {
            $('.ocr-fail-check').off('change').on('change', function() {
                handleCheckboxChange(this);
            });
        }
    }

    /**
     * 체크박스 변경 이벤트 처리
     */
    function handleCheckboxChange(checkbox) {
        var $checkbox = $(checkbox);
        var itemCd = $checkbox.data('item-cd');
        var itemValue = $checkbox.data('item-value');
        var isChecked = $checkbox.prop('checked');

        // itemValue를 문자열로 변환
        var itemValueStr = itemValue != null ? String(itemValue) : '';

        // 빈 값 체크
        var isEmpty = !itemValueStr || 
            itemValueStr.trim() === '' || 
            itemValueStr === 'null' || 
            itemValueStr === 'undefined';

        var ocrFailType = null;
        if (isChecked) {
            // 체크된 경우
            if (isEmpty) {
                ocrFailType = 'X'; // 빈 값
            } else {
                ocrFailType = 'E'; // 값이 있음
            }
        } else {
            // 체크 해제된 경우
            ocrFailType = null;
        }

        // OCR 결과 번호 가져오기
        var ocrRsltNo = currentDocumentDetail ? currentDocumentDetail.ocr_rslt_no : null;
        if (!ocrRsltNo) {
            alert('OCR 결과 번호를 찾을 수 없습니다.');
            // 원래 상태로 되돌리기
            $checkbox.prop('checked', !isChecked);
            return;
        }

        // 서버에 업데이트 요청 (extract_val 포함)
        updateOcrExtractFailType(ocrRsltNo, itemCd, ocrFailType, itemValueStr, $checkbox, isChecked);
        
        // 체크 시 자동으로 확인 상태로 변경
        if (isChecked && !isCheckCompleted) {
            autoSetCheckCompleted(ocrRsltNo);
        }
        // 체크 해제 시 다른 체크가 없으면 미확인으로 변경
        else if (!isChecked && isCheckCompleted) {
            checkAndRemoveCheckCompleted(ocrRsltNo);
        }
    }

    /**
     * 자동으로 확인 상태로 변경
     */
    function autoSetCheckCompleted(ocrRsltNo) {
        var params = {
            ocr_rslt_no: ocrRsltNo,
            action: 'complete'
        };

        $.ajax({
            url: '/rf-ocr-verf/api/toggleOcrCheckCompleted.do',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: function(response) {
                if (response.success) {
                    isCheckCompleted = true;
                    updateCheckCompletedButton();
                    console.log('자동 확인 완료 처리');
                }
            },
            error: function(xhr, status, error) {
                console.error('자동 확인 처리 오류:', error);
            }
        });
    }

    /**
     * 체크된 항목이 없으면 미확인으로 변경
     */
    function checkAndRemoveCheckCompleted(ocrRsltNo) {
        // 체크된 항목이 있는지 확인
        var hasChecked = $('.ocr-fail-check:checked').length > 0;
        
        if (!hasChecked) {
            // 체크된 항목이 없으면 미확인으로 변경
            var params = {
                ocr_rslt_no: ocrRsltNo,
                action: 'uncomplete'
            };

            $.ajax({
                url: '/rf-ocr-verf/api/toggleOcrCheckCompleted.do',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(params),
                success: function(response) {
                    if (response.success) {
                        isCheckCompleted = false;
                        updateCheckCompletedButton();
                        console.log('자동 미확인 처리');
                    }
                },
                error: function(xhr, status, error) {
                    console.error('자동 미확인 처리 오류:', error);
                }
            });
        }
    }

    /**
     * OCR 추출 데이터 fail_type 업데이트
     */
    function updateOcrExtractFailType(ocrRsltNo, extractKey, ocrFailType, extractVal, $checkbox, originalChecked) {
        var params = {
            ocr_doc_rslt: ocrRsltNo,
            extract_key: extractKey,
            extract_val: extractVal,
            ocr_fail_type: ocrFailType,
            // 메타 정보 추가 (INSERT 시 필요)
            ctrl_yr: currentDocumentDetail.ctrl_yr,
            inst_cd: currentDocumentDetail.inst_cd,
            prdt_cd: currentDocumentDetail.prdt_cd,
            ctrl_no: currentDocumentDetail.ctrl_no,
            ocr_doc_no: currentDocumentDetail.ocr_doc_no,
            doc_tp_cd: currentDocumentDetail.doc_tp_cd
        };

        $.ajax({
            url: '/rf-ocr-verf/api/updateOcrExtractFailType.do',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: function(response) {
                if (response.success) {
                    console.log('fail_type 업데이트 성공:', extractKey, '→', ocrFailType);
                } else {
                    alert('업데이트 실패: ' + (response.message || ''));
                    // 원래 상태로 되돌리기
                    $checkbox.prop('checked', !originalChecked);
                }
            },
            error: function(xhr, status, error) {
                console.error('fail_type 업데이트 오류:', error);
                alert('업데이트 중 오류가 발생했습니다.');
                // 원래 상태로 되돌리기
                $checkbox.prop('checked', !originalChecked);
            }
        });
    }

    /**
     * 체크 완료 버튼 상태 업데이트
     */
    function updateCheckCompletedButton() {
        var $btn = $('#checkCompletedBtn');
        var $text = $('#checkCompletedText');
        
        $btn.show();
        
        if (isCheckCompleted) {
            $btn.removeClass('btn-outline-secondary').addClass('btn-success');
            $text.text('확인');
        } else {
            $btn.removeClass('btn-success').addClass('btn-outline-secondary');
            $text.text('미확인');
        }
    }

    /**
     * 체크 완료 상태 토글
     */
    function toggleCheckCompleted() {
        var ocrRsltNo = currentDocumentDetail ? currentDocumentDetail.ocr_rslt_no : null;
        if (!ocrRsltNo) {
            alert('OCR 결과 번호를 찾을 수 없습니다.');
            return;
        }

        var action = isCheckCompleted ? 'uncomplete' : 'complete';
        var confirmMsg = isCheckCompleted ? '미확인으로 변경하시겠습니까?' : '확인 완료 처리하시겠습니까?';

        if (!confirm(confirmMsg)) {
            return;
        }

        var params = {
            ocr_rslt_no: ocrRsltNo,
            action: action
        };

        $.ajax({
            url: '/rf-ocr-verf/api/toggleOcrCheckCompleted.do',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: function(response) {
                if (response.success) {
                    isCheckCompleted = !isCheckCompleted;
                    updateCheckCompletedButton();
                    console.log('체크 완료 상태 변경:', isCheckCompleted);
                } else {
                    alert('업데이트 실패: ' + (response.message || ''));
                }
            },
            error: function(xhr, status, error) {
                console.error('체크 완료 상태 토글 오류:', error);
                alert('업데이트 중 오류가 발생했습니다.');
            }
        });
    }



    /**
     * 복수 이미지 표시 (더 이상 사용하지 않음 - 서버에서 imageInfoList를 직접 받음)
     */
    /*
    function displayMultipleImages(currentData, documentList) {
        // 이 함수는 더 이상 사용하지 않습니다.
        // getOcrDocumentDetail API에서 imageInfoList를 직접 받아서 사용합니다.
    }
    */

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
        var firstImage = imageInfoList[0];// image_path를 Base64로 인코딩
        var encodedImageList = imageInfoList.map(function(imageInfo) {
            var encoded = Object.assign({}, imageInfo);
            if (encoded.image_path) {
                // UTF-8 문자열을 Base64로 인코딩
                encoded.image_path = btoa(unescape(encodeURIComponent(encoded.image_path)));
            }
            return encoded;
        });

        var params = {
            inst_cd: firstImage.inst_cd,
            prdt_cd: firstImage.prdt_cd,
            image_list: encodedImageList
        };

        //console.log('이미지 로딩 파라미터:', params);

        $.ajax({
            url: '/rf-ocr-verf/api/getOcrImages.do',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            timeout: 60000,
            success: function(response) {
                //console.log('이미지 로딩 응답:', response);

                if (response.success && response.data && response.data.length > 0) {
                    //console.log('로드된 이미지 개수:', response.data.length);

                    // 컨테이너 div 생성
                    var html = '<div id="viewerContainer" style="width: 100%; height: 100%;">';
                    html += '<ul style="list-style: none; padding: 0; margin: 0; width: 100%; height: 100%;">';

                    var validImageCount = 0;
                    var failedImageCount = 0;
                    var failedImages = [];

                    response.data.forEach(function(imageData, index) {
                        //console.log('이미지 ' + index + ' 처리 중...');

                        if (imageData && imageData.startsWith('data:image')) {
                            validImageCount++;
                            // 파일명 가져오기
                            var fileName = 'Page ' + (index + 1);
                            if (currentImageInfoList && currentImageInfoList[index]) {
                                var imageInfo = currentImageInfoList[index];
                                if (imageInfo.file_name) {
                                    fileName = imageInfo.file_name + '.' + imageInfo.ext;
                                }
                            }

                            html += '<li>';
                            html += '<img src="' + imageData + '" alt="' + fileName + '" title="' + fileName + '" style="display: none;">';
                            html += '</li>';
                        } else if (imageData && imageData.startsWith('ERROR:')) {
                            failedImageCount++;
                            var fileName = 'Page ' + (index + 1);
                            if (currentImageInfoList && currentImageInfoList[index]) {
                                var imageInfo = currentImageInfoList[index];
                                if (imageInfo.file_name) {
                                    fileName = imageInfo.file_name + '.' + imageInfo.ext;
                                }
                            }
                            failedImages.push(fileName);
                            console.error('이미지 로딩 실패 [' + index + ']:', imageData);

                            // 에러 이미지 플레이스홀더 추가
                            html += '<li>';
                            html += '<div style="width: 100%; height: 600px; display: flex; align-items: center; justify-content: center; background-color: #f8f9fa; border: 2px dashed #dee2e6;">';
                            html += '<div class="text-center p-4">';
                            html += '<i class="fas fa-exclamation-triangle fa-3x text-warning mb-3"></i>';
                            html += '<h6 class="text-danger">이미지 로딩 실패</h6>';
                            html += '<p class="text-muted small mb-0">' + fileName + '</p>';
                            html += '<p class="text-muted small">파일을 불러올 수 없습니다</p>';
                            html += '</div>';
                            html += '</div>';
                            html += '</li>';
                        } else {
                            console.warn('이미지 ' + index + '이 유효하지 않습니다:', imageData);
                        }
                    });

                    html += '</ul>';
                    html += '</div>';

                    // 유효한 이미지가 하나도 없으면 에러 표시
                    if (validImageCount === 0) {
                        var errorHtml = '<div class="text-center p-4">';
                        errorHtml += '<i class="fas fa-exclamation-triangle fa-3x text-warning mb-3"></i>';
                        errorHtml += '<h5 class="text-danger">이미지를 불러올 수 없습니다</h5>';
                        errorHtml += '<p class="text-muted">외부 API에서 파일을 가져오는 중 오류가 발생했습니다.</p>';
                        errorHtml += '<p class="text-muted small">파일이 존재하지 않거나 접근 권한이 없을 수 있습니다.</p>';
                        errorHtml += '</div>';
                        $('#imageViewer').html(errorHtml);
                        $('#loadingOverlay').removeClass('active').addClass('hidden');
                        return;
                    }

                    // 일부 이미지만 실패한 경우 경고 메시지 표시
                    if (failedImageCount > 0) {
                        var warningMsg = '총 ' + response.data.length + '개 중 ' + failedImageCount + '개 이미지를 불러올 수 없습니다.';
                        console.warn(warningMsg, failedImages);
                        // 상단에 경고 배너 추가 (선택사항)
                        // alert(warningMsg);
                    }

                    $('#imageViewer').html(html);

                    // Viewer.js 초기화
                    setTimeout(function() {
                        var container = document.getElementById('viewerContainer');
                        //console.log('Viewer.js 초기화 시작 - 컨테이너:', container);

                        if (container) {
                            var viewer = new Viewer(container, {
                                inline: true,
                                container: '#imageViewer',
                                viewed: function(event) {
                                    // 현재 인덱스 항상 업데이트
                                    var newIndex = event.detail.index;
                                    currentIndex = newIndex;

                                    // 첫 로딩 시 로딩 오버레이 숨기기
                                    setTimeout(function() {
                                        $('#loadingOverlay').addClass('hidden');
                                    }, 500);

                                    // 해당 인덱스의 ocr_doc_no 가져오기
                                    if (ocrDocNoList && ocrDocNoList[newIndex]) {
                                        var newOcrDocNo = ocrDocNoList[newIndex];

                                        // ocr_doc_no가 실제로 변경되었을 때만 OCR 결과 갱신
                                        // (PDF 여러 페이지인 경우 같은 ocr_doc_no가 반복되므로)
                                        if (newOcrDocNo !== currentOcrDocNo) {
                                            currentOcrDocNo = newOcrDocNo;

                                            // OCR 결과 조회
                                            $.ajax({
                                                url: '/rf-ocr-verf/api/getOcrResultText.do',
                                                type: 'POST',
                                                contentType: 'application/json',
                                                data: JSON.stringify({
                                                    ocr_doc_no: newOcrDocNo
                                                }),
                                                success: function(response) {
                                                    if (response.success && response.data) {
                                                        displayOcrResults(response.data);
                                                    }
                                                },
                                                error: function(xhr, status, error) {
                                                    console.error('OCR 결과 조회 실패:', error);
                                                }
                                            });
                                        } else {
                                            console.log('같은 OCR_DOC_NO - OCR 결과 갱신 생략:', newOcrDocNo);
                                        }
                                    }
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
                                initialViewIndex: currentIndex/*,
                                viewed: function() {
                                    // 뷰어가 표시된 후 크기 조정
                                    viewer.update();
                                }*/
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
                $('#loadingOverlay').addClass('hidden');
            }
        });
    }

    /*


        /!**
         * 복수 이미지 로딩 API 호출 (Viewer.js 적용)
         *!/
        function loadMultipleImages(currentData, imageList) {
            // 로딩바 표시
            var loadingHtml = '<div style="display: flex; align-items: center; justify-content: center; height: 100%;">';
            loadingHtml += '<div class="spinner-border text-primary" role="status">';
            loadingHtml += '<span class="sr-only">로딩 중...</span>';
            loadingHtml += '</div></div>';
            $('#imageViewer').html(loadingHtml);

            console.log('복수 이미지 로딩 API 호출 - 이미지 개수:', imageList.length);

            var params = {
                inst_cd: currentData.inst_cd,
                prdt_cd: currentData.prdt_cd,
                image_list: imageList
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
                                html += '<li>';
                                html += '<img src="' + imageData + '" alt="Page ' + (index + 1) + '" style="display: none;">';
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
    */


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

    /**
     * 파일 다운로드 (현재 보고 있는 이미지)
     */
    function downloadFile() {
        /*
        console.log('=== downloadFile 호출 ===');
        console.log('currentIndex:', currentIndex);
        console.log('currentImageInfoList 길이:', currentImageInfoList ? currentImageInfoList.length : 0);
        console.log('currentImageInfoList:', currentImageInfoList);
        */

        if (!currentImageInfoList || currentImageInfoList.length === 0) {
            alert('이미지 정보가 없습니다.');
            return;
        }

        // 현재 인덱스의 이미지 정보 가져오기
        const currentImage = currentImageInfoList[currentIndex];

        if (!currentImage) {
            alert('현재 이미지 정보를 찾을 수 없습니다. (인덱스: ' + currentIndex + ')');
            return;
        }

        const instCd = currentImage.inst_cd;
        const prdtCd = currentImage.prdt_cd;
        const imagePath = currentImage.image_path;
        const fileName = currentImage.file_name || 'download';
        const ext = currentImage.ext;

        console.log('다운로드 정보:', {
            index: currentIndex,
            instCd: instCd,
            prdtCd: prdtCd,
            imagePath: imagePath,
            fileName: fileName,
            ext: ext
        });

        if (!imagePath) {
            alert('파일 경로 정보가 없습니다.');
            return;
        }

        // 다운로드 URL 생성
        const downloadUrl = '/rf-ocr-verf/api/downloadFile.do' +
            '?inst_cd=' + encodeURIComponent(instCd) +
            '&prdt_cd=' + encodeURIComponent(prdtCd) +
            '&image_path=' + encodeURIComponent(imagePath) +
            '&file_name=' + encodeURIComponent(fileName) +
            '&ext=' + encodeURIComponent(ext);

        console.log('다운로드 URL:', downloadUrl);

        // 새 창에서 다운로드
        window.location.href = downloadUrl;
    }

    /**
     * 전체 파일 다운로드 (ZIP)
     */
    function downloadAllFiles() {
        if (!currentDocumentDetail) {
            alert('문서 정보가 없습니다.');
            return;
        }

        const ctrlYr = currentDocumentDetail.ctrl_yr;
        const instCd = currentDocumentDetail.inst_cd;
        const prdtCd = currentDocumentDetail.prdt_cd;
        const ctrlNo = currentDocumentDetail.ctrl_no;


        if (!ctrlYr || !instCd || !prdtCd || !ctrlNo) {
            alert('관리번호 정보가 없습니다.');
            return;
        }

        // POST 방식으로 다운로드 (form submit 사용)
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/rf-ocr-verf/api/downloadAllFiles.do';

        const fields = {
            ctrl_yr: ctrlYr,
            inst_cd: instCd,
            prdt_cd: prdtCd,
            ctrl_no: ctrlNo
        };

        for (const key in fields) {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = key;
            input.value = fields[key];
            form.appendChild(input);
        }

        document.body.appendChild(form);
        form.submit();
        document.body.removeChild(form);
    }


    /**
     * OCR 상태 초기화 (재실행)
     */
    function resetOcrStatus() {
        if (!currentOcrDocNo) {
            alert('문서 정보가 없습니다.');
            return;
        }

        if (!confirm('현재 문서의 OCR 상태를 초기화하시겠습니까?\nOCR이 다시 실행됩니다.')) {
            return;
        }

        console.log('OCR 상태 초기화 - OCR_DOC_NO:', currentOcrDocNo);

        $.ajax({
            url: '/rf-ocr-verf/api/updateOcrStatus.do',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                ocr_doc_no: currentOcrDocNo,
                ocr_yn: 'N'
            }),
            success: function(response) {
                console.log('OCR 상태 업데이트 응답:', response);

                if (response.success) {
                    alert('OCR 상태가 초기화되었습니다.\nOCR이 재실행됩니다.');

                    // 페이지 새로고침
                    location.reload();
                } else {
                    alert('OCR 상태 초기화에 실패했습니다.\n' + (response.message || ''));
                }
            },
            error: function(xhr, status, error) {
                console.error('OCR 상태 업데이트 오류:', error);
                alert('OCR 상태 초기화 중 오류가 발생했습니다.');
            }
        });
    }

    /**
     * 관리번호 클립보드 복사
     */
    function copyToClipboard() {
        var ctrlNo = $('#ctrlNo').text();
        
        if (!ctrlNo || ctrlNo === '-') {
            alert('복사할 관리번호가 없습니다.');
            return;
        }

        // 클립보드 API 사용
        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(ctrlNo).then(function() {
                // 성공 시 버튼 텍스트 변경
                var btn = $('#copyCtrlNoBtn');
                var originalHtml = btn.html();
                btn.html('<i class="fas fa-check"></i> 복사됨');
                btn.removeClass('btn-outline-secondary').addClass('btn-success');
                
                // 1초 후 원래대로
                setTimeout(function() {
                    btn.html(originalHtml);
                    btn.removeClass('btn-success').addClass('btn-outline-secondary');
                }, 1000);
            }).catch(function(err) {
                console.error('클립보드 복사 실패:', err);
                fallbackCopyToClipboard(ctrlNo);
            });
        } else {
            // 구형 브라우저 대응
            fallbackCopyToClipboard(ctrlNo);
        }
    }

    /**
     * 클립보드 복사 대체 방법 (구형 브라우저용)
     */
    function fallbackCopyToClipboard(text) {
        var textArea = document.createElement('textarea');
        textArea.value = text;
        textArea.style.position = 'fixed';
        textArea.style.top = '0';
        textArea.style.left = '0';
        textArea.style.opacity = '0';
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();

        try {
            var successful = document.execCommand('copy');
            if (successful) {
                var btn = $('#copyCtrlNoBtn');
                var originalHtml = btn.html();
                btn.html('<i class="fas fa-check"></i> 복사됨');
                btn.removeClass('btn-outline-secondary').addClass('btn-success');
                
                setTimeout(function() {
                    btn.html(originalHtml);
                    btn.removeClass('btn-success').addClass('btn-outline-secondary');
                }, 1000);
            } else {
                alert('클립보드 복사에 실패했습니다.');
            }
        } catch (err) {
            console.error('클립보드 복사 오류:', err);
            alert('클립보드 복사에 실패했습니다.');
        }

        document.body.removeChild(textArea);
    }




</script>

