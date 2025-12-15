<%@ page pageEncoding="utf-8" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <title>이미지 전산화 검증</title>
    <style>
        body {
            font-family: 'Noto Sans KR', sans-serif;
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
        .sidebar-divider {
            border-top: 1px solid rgba(255, 255, 255, 0.15);
            margin: 0;
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
        .bg-gradient-primary {
            background: linear-gradient(180deg, #4e73df 10%, #224abe 100%);
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
        /* DataTables wrapper를 card-body 안에 완전히 고정 */
        .card-body {
            position: relative !important;
            padding: 1.25rem !important;
        }
        #ocrResultTable_wrapper {
            width: 100% !important;
            max-width: 100% !important;
            margin: 0 !important;
            padding: 0 !important;
        }
        /* DataTables 페이징 가운데 정렬 */
        .dataTables_wrapper .dataTables_paginate {
            text-align: center !important;
        }
        .dataTables_wrapper .dataTables_paginate .pagination {
            justify-content: center !important;
        }
        .dataTables_wrapper .row {
            margin-left: 0 !important;
            margin-right: 0 !important;
            width: 100% !important;
        }
        .dataTables_wrapper .row > div {
            padding-left: 0 !important;
            padding-right: 0 !important;
        }
        /* 테이블 크기 조정 */
        #ocrResultTable {
            font-size: 0.85rem;
        }
        #ocrResultTable thead th {
            padding: 0.5rem;
            font-size: 0.85rem;
            white-space: nowrap;
        }
        #ocrResultTable tbody td {
            padding: 0.5rem;
            vertical-align: middle;
        }
        .table-responsive {
            font-size: 0.85rem;
        }
        /* 뱃지 공통 스타일 */
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
</head>
<script type="text/javascript">
    // 마우스 우클릭 방지, 드래그 방지, 선택 방지
    /*$(document).ready(function() {
        $(document).on('contextmenu dragstart selectstart', function() {
            return false;
        });
    });*/
</script>
<body id="page-top">
<div id="wrapper">
    <!-- Content Wrapper -->
    <div id="content-wrapper" class="d-flex flex-column">
        <div id="content">
            <!-- Main Content -->
            <div class="container-fluid">
                <!-- 검색 필터 -->
                <div class="card shadow mb-4">
                    <div class="card-header py-3">
                        <h6 class="m-0 font-weight-bold text-primary">검색 조건</h6>
                    </div>
                    <div class="card-body">
                        <div class="form-row align-items-center mb-2">
                            <div class="col-auto" style="width: 120px;">
                                <label class="mb-0">관리번호</label>
                            </div>
                            <div class="col-md-2">
                                <input type="text" class="form-control form-control-sm" id="searchManagementNo" placeholder="관리번호 입력">
                            </div>
                        </div>
                        <div class="form-row align-items-center mb-2">
                            <div class="col-auto" style="width: 120px;">
                                <label class="mb-0">기간</label>ㄱ
                            </div>
                            <div class="col-auto">
                                <div class="input-group input-group-sm" style="width: 300px;">
                                    <input type="date" class="form-control form-control-sm" id="startDate">
                                    <div class="input-group-prepend input-group-append">
                                        <span class="input-group-text">~</span>
                                    </div>
                                    <input type="date" class="form-control form-control-sm" id="endDate">
                                </div>
                            </div>
                            <div class="col-auto">
                                <div class="btn-group btn-group-sm" role="group">
                                    <button type="button" class="btn btn-outline-secondary period-btn active" data-period="all">전체</button>
                                    <button type="button" class="btn btn-outline-secondary period-btn" data-period="today">오늘</button>
                                    <button type="button" class="btn btn-outline-secondary period-btn" data-period="month">1개월</button>
                                    <button type="button" class="btn btn-outline-secondary period-btn" data-period="3month">3개월</button>
                                </div>
                            </div>
                        </div>
                        <div class="form-row align-items-center mb-3">
                            <div class="col-auto" style="width: 120px;">
                                <label class="mb-0">기관(상품명)</label>
                            </div>
                            <div class="col">
                                <div class="custom-control custom-checkbox custom-control-inline" style="padding-left: 0.5rem !important;">
                                    <input type="checkbox" class="custom-control-input" id="org1" value="모바일반환보증">
                                    <label class="custom-control-label" for="org1">모바일반환보증</label>
                                </div>
                                <div class="custom-control custom-checkbox custom-control-inline" style="padding-left: 0.5rem !important;">
                                    <input type="checkbox" class="custom-control-input" id="org2" value="신한전세">
                                    <label class="custom-control-label" for="org2">신한전세</label>
                                </div>
                                <div class="custom-control custom-checkbox custom-control-inline" style="padding-left: 0.5rem !important;">
                                    <input type="checkbox" class="custom-control-input" id="org3" value="전세안심보험(카손)">
                                    <label class="custom-control-label" for="org3">전세안심보험(카손)</label>
                                </div>
                                <div class="custom-control custom-checkbox custom-control-inline" style="padding-left: 0.5rem !important;">
                                    <input type="checkbox" class="custom-control-input" id="org4" value="하나은행(사전)">
                                    <label class="custom-control-label" for="org4">하나은행(사전)</label>
                                </div>
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="col-md-12 text-center">
                                <button type="button" class="btn btn-secondary btn-sm mr-2" id="btnReset">
                                    <i class="fas fa-redo"></i> 초기화
                                </button>
                                <button type="button" class="btn btn-primary btn-sm" id="btnSearch">
                                    <i class="fas fa-search"></i> 검색
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
cr
                <!-- OCR 결과 테이블 -->
                <div class="card shadow mb-4">
                    <div class="card-header py-3">
                        <h6 class="m-0 font-weight-bold text-primary">OCR 결과 목록</h6>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-bordered table-hover" id="ocrResultTable" width="100%" cellspacing="0">
                                <thead>
                                <tr>
                                    <th>No</th>
                                    <th>등록일시</th>
                                    <th>관리번호</th>
                                    <th>문서유형</th>
                                    <th>코드 서류명</th>
                                    <th>한글 서류명 (결과)</th>
                                    <th>페이지수</th>
                                    <th>검증여부</th>
                                </tr>
                                </thead>
                                <tbody>
                                <!-- 데이터는 JavaScript로 동적 로드 -->
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Scroll to Top Button-->
<a class="scroll-to-top rounded" href="#page-top">
    <i class="fas fa-angle-up"></i>
</a>
</body>
</html>


<script>
    /*검증 페이지 메인 js*/


    $(document).ready(function() {
        // ========================================
        // DataTable 초기화 (Server-Side Processing)
        // ========================================
        var table = $('#ocrResultTable').DataTable({
            processing: true,
            serverSide: true,  // 서버사이드 처리 활성화
            ajax: {
                url: '/rf-ocr-verf/api/getOcrResultList.do',
                type: 'POST',
                contentType: 'application/json',
                data: function(d) {
                    // 관리번호 파싱
                    var parsedMgmtNo = parseManagementNo($('#searchManagementNo').val());

                    // 선택된 기관 코드
                    var orgCodes = getSelectedOrganizationCodes();

                    // DataTables 기본 파라미터를 서버 형식으로 변환
                    var params = {
                        // 페이징
                        paging: Math.floor(d.start / d.length),
                        num: d.length,

                        // 정렬
                        sort: d.order && d.order.length > 0 ? d.order[0].dir.toUpperCase() : 'DESC',

                        // 검색 조건
                        // 관리번호 입력 시: 파싱된 값 우선 사용
                        // 체크박스 선택 시: 기관별 코드 배열 사용
                        ctrl_yr: parsedMgmtNo ? parsedMgmtNo.ctrl_yr : null,
                        inst_cd: parsedMgmtNo ? parsedMgmtNo.inst_cd : orgCodes.inst_cd,
                        prdt_cd: parsedMgmtNo ? parsedMgmtNo.prdt_cd : orgCodes.prdt_cd,
                        ctrl_no: parsedMgmtNo ? parsedMgmtNo.ctrl_no : null,
                        ins_dttm_st: $('#startDate').val() || null,
                        ins_dttm_en: $('#endDate').val() || null,
                        ocr_yn: $('#verifiedFilter').val() ? [$('#verifiedFilter').val()] : null
                    };
                    return JSON.stringify(params);  // JSON 문자열로 변환
                },
                dataSrc: function(json) {
                    // 서버 응답을 DataTables 형식으로 변환
                    if (json.success) {
                        // 서버에서 받은 전체 건수 사용
                        json.recordsTotal = json.recordsTotal || 0;
                        json.recordsFiltered = json.recordsFiltered || 0;

                        return json.data || [];
                    }
                    // 데이터 없을 때
                    console.warn('서버 응답 실패:', json);
                    json.recordsTotal = 0;
                    json.recordsFiltered = 0;
                    return [];
                },
                error: function(xhr, error, thrown) {
                    console.error('서버 연결 실패:', error);
                    alert('데이터를 불러오는데 실패했습니다.');
                }
            },
            language: {
                "decimal": "",
                "emptyTable": "데이터가 없습니다.",
                "info": "_START_ - _END_ (총 _TOTAL_ 건)",
                "infoEmpty": "0건",
                "infoFiltered": "(전체 _MAX_ 건 중 검색결과)",
                "infoPostFix": "",
                "thousands": ",",
                "lengthMenu": "_MENU_ 개씩 보기",
                "loadingRecords": "로딩중...",
                "processing": "처리중...",
                "search": "검색:",
                "zeroRecords": "검색된 데이터가 없습니다.",
                "paginate": {
                    "first": "첫 페이지",
                    "last": "마지막 페이지",
                    "next": "다음",
                    "previous": "이전"
                },
                "aria": {
                    "sortAscending": ": 오름차순 정렬",
                    "sortDescending": ": 내림차순 정렬"
                }
            },
            pageLength: 50,
            lengthMenu: [[10, 25, 50, 100], [10, 25, 50, 100]],
            dom: '<"row"<"col-sm-12 col-md-3"li><"col-sm-12 col-md-6 text-center"p><"col-sm-12 col-md-3 text-right"<"verified-filter-top">>>' +
                '<"row"<"col-sm-12"tr>>' +
                '<"row"<"col-sm-12 col-md-3"i><"col-sm-12 col-md-12 text-center"p><"col-sm-12 col-md-3">>',
            order: [[1, 'desc']],
            columnDefs: [
                { className: "text-center", targets: [0, 1, 2, 3, 4, 5, 6, 7] },
                { orderable: false, targets: [0, 2, 3, 4, 5, 6, 7] },
                { orderable: true, targets: [1] }
            ],
            columns: [
                {
                    data: 'row_number',
                    render: function(data) {
                        return data;
                    }
                },
                {
                    data: 'ins_dttm',
                    render: function(data) {
                        return data || '';
                    }
                },
                {
                    data: null,
                    render: function(data, type, row) {
                        // ctrl_yr-inst_cd-prdt_cd-ctrl_no 조합
                        var ctrlNo = row.ctrl_yr + '-' + row.inst_cd + '-' + row.prdt_cd + '-' + row.ctrl_no;
                        var docTpCd = row.doc_tp_cd || '';
                        return '<a href="#" class="text-primary" onclick="viewDetail(\'' + row.ctrl_yr + '\', \'' + row.inst_cd + '\', \'' + row.prdt_cd + '\', \'' + row.ctrl_no + '\', \'' + docTpCd + '\'); return false;">' + ctrlNo + '</a>';
                    }
                },
                {
                    data: 'doc_tp_cd',
                    render: function(data) {
                        return data || '';
                    }
                },
                {
                    data: 'doc_kr_nm',
                    render: function(data) {
                        return data || '';
                    }
                },
                {
                    data: 'doc_title',
                    render: function(data) {
                        return data || '';
                    }
                },
                {
                    data: 'doc_page',
                    render: function(data) {
                        return data || '0';
                    }
                },
                {
                    data: 'ocr_yn',
                    render: function(data) {
                        if (data === 'Y') {
                            return '<span class="badge badge-success">완료</span>';
                        }
                        if (data === 'N') {
                            return '<span class="badge badge-warning">대기</span>';
                        }
                        if (data === 'X') {
                            return '<span class="badge badge-danger">실패</span>';
                        }
                        return '<span class="badge badge-secondary">오류</span>';
                    }
                }
            ]
        });

        // 검증여부 필터 추가
        setTimeout(function() {
            $('.verified-filter-top').html(
                '<label class="mr-2 mb-0 d-inline-block">검증여부</label>' +
                '<select class="form-control form-control-sm d-inline-block" id="verifiedFilter" style="width: 100px;">' +
                '<option value="">전체</option>' +
                '<option value="Y">완료</option>' +
                '<option value="N">대기</option>' +
                '<option value="X">실패</option>' +
                '</select>'
            );
        }, 100);


        // 검증여부 필터 변경 시 - 서버 재호출
        $(document).on('change', '#verifiedFilter', function() {
            table.ajax.reload();
        });

        // 기간 버튼 클릭
        $('.period-btn').on('click', function() {
            $('.period-btn').removeClass('active');
            $(this).addClass('active');
            var period = $(this).data('period');
            setDateRange(period);
        });

        // 관리번호 입력창에서 엔터키 이벤트
        $('#searchManagementNo').on('keypress', function(e) {
            if (e.which === 13) { // Enter key
                e.preventDefault();
                table.ajax.reload();
            }
        });

        // 검색 버튼 - 서버 재호출
        $('#btnSearch').on('click', function() {
            table.ajax.reload();
        });

        // 초기화 버튼
        $('#btnReset').on('click', function() {
            $('#searchManagementNo').val('');
            $('.period-btn').removeClass('active');
            $('.period-btn[data-period="all"]').addClass('active');
            $('input[type="checkbox"]').prop('checked', false);
            $('#verifiedFilter').val('');
            setDateRange('all');
            table.ajax.reload();
        });

        // 초기 날짜 설정
        setDateRange('all');

        // DataTables wrapper가 card-body 안에 있는지 확인
        setTimeout(function() {
            var wrapper = $('#ocrResultTable_wrapper');
            var cardBody = wrapper.closest('.card-body');
            if (cardBody.length === 0) {
                console.warn('DataTables wrapper가 card-body 밖에 있습니다.');
            }
        }, 500);
    });


    // ========================================
    // 유틸리티 함수
    // ========================================

    // 기관별 inst_cd, prdt_cd 매핑
    var organizationMapping = {
        '모바일반환보증': {
            inst_cd: ['01', '45', '47', '49'],
            prdt_cd: ['820', '830']
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

    // 관리번호 파싱 (qq-ww-eee-rrrrrr 패턴)
    function parseManagementNo(input) {
        if (!input || input.trim() === '') {
            return null;
        }

        // 하이픈 제거
        var cleaned = input.trim().replace(/-/g, '');
        
        if (cleaned.length < 4) {
            return null;
        }

        // 2-2-3-6 패턴으로 자르기
        var result = {
            ctrl_yr: cleaned.substring(0, 2),
            inst_cd: cleaned.substring(2, 4),
            prdt_cd: cleaned.length > 4 ? cleaned.substring(4, Math.min(cleaned.length, 7)) : null,
            ctrl_no: cleaned.length > 7 ? cleaned.substring(7) : null
        };

        return result;
    }

    // 선택된 기관의 inst_cd, prdt_cd 가져오기
    function getSelectedOrganizationCodes() {
        var instCodes = [];
        var prdtCodes = [];

        $('input[type="checkbox"]:checked').each(function() {
            var orgName = $(this).val();
            var mapping = organizationMapping[orgName];

            if (mapping) {
                // inst_cd 추가 (중복 제거)
                mapping.inst_cd.forEach(function(code) {
                    if (instCodes.indexOf(code) === -1) {
                        instCodes.push(code);
                    }
                });

                // prdt_cd 추가 (중복 제거)
                mapping.prdt_cd.forEach(function(code) {
                    if (prdtCodes.indexOf(code) === -1) {
                        prdtCodes.push(code);
                    }
                });
            }
        });

        return {
            inst_cd: instCodes.length > 0 ? instCodes : null,
            prdt_cd: prdtCodes.length > 0 ? prdtCodes : null
        };
    }


    // 상세보기
    // 상세보기
    function viewDetail(ctrlYr, instCd, prdtCd, ctrlNo, docTpCd) {
        // pageChange 방식으로 상세 페이지를 새 탭에서 열기
        var currentPath = window.location.pathname;
        var site = currentPath.substring(1); // '/rf_ocr_verf' -> 'rf_ocr_verf'

        // 파라미터 구성
        var params = [];
        params.push('pageChange=/ocrRsltDetail');
        params.push('ctrl_yr=' + encodeURIComponent(ctrlYr));
        params.push('inst_cd=' + encodeURIComponent(instCd));
        params.push('prdt_cd=' + encodeURIComponent(prdtCd));
        params.push('ctrl_no=' + encodeURIComponent(ctrlNo));
        if (docTpCd) {
            params.push('doc_tp_cd=' + encodeURIComponent(docTpCd));
        }

        var url = '/' + site + '?' + params.join('&');
        window.open(url, '_blank');
    }

    // ========================================
    // 날짜 관련 함수
    // ========================================
    function setDateRange(period) {
        var today = new Date();
        var startDate = new Date();
        var endDate = new Date();

        switch(period) {
            case 'all':
                $('#startDate').val('');
                $('#endDate').val('');
                return;
            case 'today':
                startDate = today;
                endDate = today;
                break;
            case 'month':
                startDate.setMonth(today.getMonth() - 1);
                break;
            case '3month':
                startDate.setMonth(today.getMonth() - 3);
                break;
        }

        $('#startDate').val(formatDateInput(startDate));
        $('#endDate').val(formatDateInput(endDate));
    }

    function formatDateInput(date) {
        var year = date.getFullYear();
        var month = String(date.getMonth() + 1).padStart(2, '0');
        var day = String(date.getDate()).padStart(2, '0');e
        return year + '-' + month + '-' + day;
    }
</script>