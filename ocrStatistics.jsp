<%@ page pageEncoding="utf-8" %>
<%
    String dbMode = System.getProperty("spring.profiles.active", "dev");
    boolean isDev = "dev".equals(dbMode) || "local".equals(dbMode);
%>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <title>OCR 검토 통계</title>
    
    <!-- Font Awesome (로컬) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/font-awesome.min.css">
    
    <style>
        body {
            font-family: 'Noto Sans KR', sans-serif;
            background-color: #f8f9fc;
            min-width: 1200px;
        }
        
        .container-fluid {
            padding: 20px;
        }
        
        /* 페이지 헤더 */
        .page-header {
            margin-bottom: 30px;
        }
        
        .page-header h4 {
            color: #5a5c69;
            font-weight: 700;
        }
        
        /* 카드 공통 스타일 */
        .card {
            border: none;
            box-shadow: 0 0.15rem 1.75rem 0 rgba(58, 59, 69, 0.15);
            margin-bottom: 20px;
        }
        
        .card-header {
            background-color: white;
            border-bottom: 1px solid #e3e6f0;
            padding: 15px 20px;
        }
        
        .card-header h6 {
            margin: 0;
            font-size: 1rem;
            font-weight: 700;
            color: #4e73df;
        }
        
        .card-body {
            padding: 20px;
        }
        
        /* 검색 조건 */
        .search-section {
            background: white;
        }
        
        .form-row {
            margin-bottom: 15px;
        }
        
        .form-label {
            font-weight: 600;
            color: #5a5c69;
            margin-bottom: 5px;
            font-size: 0.9rem;
        }
        
        /* 통계 카드 - 파스텔톤 */
        .stats-card {
            border-left: 4px solid;
            height: 100%;
        }
        
        .stats-card.primary {
            border-left-color: #4e73df;
        }
        
        .stats-card.success {
            border-left-color: #87CEEB;
        }
        
        .stats-card.warning {
            border-left-color: #FFE66D;
        }
        
        .stats-card.danger {
            border-left-color: #FF6B6B;
        }
        
        .stats-card .card-body {
            padding: 15px;
        }
        
        .stats-title {
            font-size: 0.75rem;
            font-weight: 700;
            text-transform: uppercase;
            color: #858796;
            margin-bottom: 5px;
        }
        
        .stats-value {
            font-size: 1.5rem;
            font-weight: 700;
            color: #5a5c69;
        }
        
        .stats-icon {
            font-size: 2rem;
            color: #dddfeb;
        }
        
        /* 차트 컨테이너 */
        .chart-container {
            position: relative;
            height: 300px;
        }
        
        /* 테이블 */
        .table {
            font-size: 0.85rem;
        }
        
        .table thead th {
            background-color: #f8f9fc;
            border-bottom: 2px solid #e3e6f0;
            font-weight: 600;
            color: #5a5c69;
            padding: 12px;
        }
        
        .table tbody td {
            padding: 12px;
            vertical-align: middle;
        }
        
        /* 뱃지 - 파스텔톤 */
        .badge {
            font-weight: 400;
            padding: 0.35em 0.65em;
            font-size: 0.75rem;
        }
        
        .badge-success {
            background-color: #D4E9F7 !important;
            color: #4A90E2 !important;
        }
        
        .badge-warning {
            background-color: #FFF4CC !important;
            color: #D4A017 !important;
        }
        
        .badge-danger {
            background-color: #FFD6D6 !important;
            color: #E63946 !important;
        }
        
        .badge-secondary {
            background-color: #e2e3e5 !important;
            color: #6c757d !important;
        }
        
        /* 버튼 */
        .btn-sm {
            font-size: 0.85rem;
            padding: 0.375rem 0.75rem;
        }
        
        /* 프로그레스 바 */
        .progress {
            height: 8px;
            border-radius: 4px;
        }
        
        /* 문서 유형 모달 스타일 */
        .doc-type-group {
            margin-bottom: 20px;
        }
        .doc-type-group-title {
            font-weight: bold;
            color: #495057;
            margin-bottom: 10px;
            padding-bottom: 5px;
            border-bottom: 1px solid #dee2e6;
        }
        .doc-type-group .custom-control {
            margin-bottom: 8px;
        }
        .doc-type-group .custom-control-label {
            font-size: 0.9rem;
            line-height: 1.4;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            max-width: 160px;
            display: inline-block;
            cursor: pointer;
        }
        .doc-type-group .custom-control-label:hover {
            background-color: #f8f9fa;
            border-radius: 3px;
        }
        .doc-type-group .custom-control-input {
            position: static !important;
            opacity: 1 !important;
            margin-right: 8px;
        }
    </style>
</head>
<body>
    <div class="container-fluid">
        <!-- 페이지 헤더 -->
        <div class="page-header d-flex justify-content-between align-items-center">
            <div style="display: none;">
                <button type="button" class="btn btn-primary btn-sm">
                    <i class="fas fa-download"></i> 엑셀 다운로드
                </button>
            </div>
        </div>
        
        <!-- 검색 조건 -->
        <div class="card search-section">
            <div class="card-header">
                <h6><i class="fas fa-filter"></i> 검색 조건</h6>
            </div>
            <div class="card-body">
                <div class="form-row">
                    <div class="col-md-3">
                        <label class="form-label">기간</label>
                        <div class="input-group input-group-sm">
                            <input type="date" class="form-control" id="startDate">
                            <div class="input-group-prepend input-group-append">
                                <span class="input-group-text">~</span>
                            </div>
                            <input type="date" class="form-control" id="endDate">
                        </div>
                    </div>
                    <div class="col-md-2">
                        <label class="form-label">기간 선택</label>
                        <div class="btn-group btn-group-sm d-block" role="group">
                            <button type="button" class="btn btn-outline-secondary active" data-period="all">전체</button>
                            <button type="button" class="btn btn-outline-secondary" data-period="today">오늘</button>
                            <button type="button" class="btn btn-outline-secondary" data-period="week">1주일</button>
                            <button type="button" class="btn btn-outline-secondary" data-period="month">1개월</button>
                        </div>
                    </div>
                    <div class="col-md-2">
                        <label class="form-label">기관(상품명)</label>
                        <select class="form-control form-control-sm" id="organization">
                            <option value="">전체</option>
                            <option value="모바일반환보증">모바일반환보증</option>
                            <option value="모바일임대보증">모바일임대보증</option>
                            <option value="신한전세">신한전세</option>
                            <option value="전세안심보험(카손)">전세안심보험(카손)</option>
                            <option value="하나은행(사전)">하나은행(사전)</option>
                            <% if (isDev) { %>
                            <option value="테스트">테스트 (99-OCR)</option>
                            <% } %>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label class="form-label">문서 유형</label>
                        <button type="button" class="btn btn-outline-secondary btn-sm btn-block" id="btnDocTypeFilter" data-toggle="modal" data-target="#docTypeModal">
                            <i class="fas fa-filter"></i> 선택 (<span id="selectedDocTypeCount">0</span>)
                        </button>
                    </div>
                    <div class="col-md-2">
                        <label class="form-label">오류 유형</label>
                        <select class="form-control form-control-sm" id="failType">
                            <option value="">전체</option>
                            <option value="E">오류(E)</option>
                            <option value="X">빈값(X)</option>
                        </select>
                    </div>
                    <div class="col-md-1 d-flex align-items-end">
                        <button type="button" class="btn btn-primary btn-sm btn-block" id="btnSearch">
                            <i class="fas fa-search"></i> 조회
                        </button>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- 통계 카드 -->
        <div class="row" style="margin-bottom: 20px;">
            <div class="col-xl-3 col-md-6">
                <div class="card stats-card primary">
                    <div class="card-body">
                        <div class="row align-items-center">
                            <div class="col">
                                <div class="stats-title">전체 추출 항목</div>
                                <div class="stats-value">0</div>
                            </div>
                            <div class="col-auto">
                                <i class="fas fa-database stats-icon"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="col-xl-3 col-md-6">
                <div class="card stats-card success">
                    <div class="card-body">
                        <div class="row align-items-center">
                            <div class="col">
                                <div class="stats-title">정상 추출</div>
                                <div class="stats-value">0 <small class="text-muted">(0%)</small></div>
                            </div>
                            <div class="col-auto">
                                <i class="fas fa-check-circle stats-icon"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="col-xl-3 col-md-6">
                <div class="card stats-card warning">
                    <div class="card-body">
                        <div class="row align-items-center">
                            <div class="col">
                                <div class="stats-title">오류 (E)</div>
                                <div class="stats-value">0 <small class="text-muted">(0%)</small></div>
                            </div>
                            <div class="col-auto">
                                <i class="fas fa-exclamation-triangle stats-icon"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="col-xl-3 col-md-6">
                <div class="card stats-card danger">
                    <div class="card-body">
                        <div class="row align-items-center">
                            <div class="col">
                                <div class="stats-title">빈값 (X)</div>
                                <div class="stats-value">0 <small class="text-muted">(0%)</small></div>
                            </div>
                            <div class="col-auto">
                                <i class="fas fa-times-circle stats-icon"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- 차트 영역 -->
        <div class="row">
            <!-- 추출 오류 분포 -->
            <div class="col-xl-4">
                <div class="card">
                    <div class="card-header">
                        <h6><i class="fas fa-chart-pie"></i> 추출 오류 분포</h6>
                    </div>
                    <div class="card-body">
                        <div class="chart-container">
                            <canvas id="reviewStatusChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- 기관별 오류율 -->
            <div class="col-xl-4">
                <div class="card">
                    <div class="card-header">
                        <h6><i class="fas fa-chart-bar"></i> 기관별 오류율</h6>
                    </div>
                    <div class="card-body">
                        <div class="chart-container">
                            <canvas id="organizationChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- 서류 유형별 오류율 -->
            <div class="col-xl-4">
                <div class="card">
                    <div class="card-header">
                        <h6><i class="fas fa-chart-bar"></i> 서류 유형별 오류율</h6>
                    </div>
                    <div class="card-body">
                        <div class="chart-container">
                            <canvas id="documentTypeChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- 상세 데이터 테이블 -->
        <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h6><i class="fas fa-table"></i> 추출 오류 상세 내역</h6>
                <div>
                    <select class="form-control form-control-sm d-inline-block" style="width: auto;">
                        <option>10개씩 보기</option>
                        <option>25개씩 보기</option>
                        <option>50개씩 보기</option>
                        <option>100개씩 보기</option>
                    </select>
                </div>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead>
                            <tr>
                                <th style="width: 5%;">No</th>
                                <th style="width: 12%;">등록일시</th>
                                <th style="width: 15%;">관리번호</th>
                                <th style="width: 12%;">기관(상품명)</th>
                                <th style="width: 10%;">서류 유형</th>
                                <th style="width: 8%;">전체 항목</th>
                                <th style="width: 8%;">정상</th>
                                <th style="width: 8%;">오류(E)</th>
                                <th style="width: 8%;">빈값(X)</th>
                                <th style="width: 8%;">오류율</th>
                                <th style="width: 6%;">상세</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td colspan="11" class="text-center">데이터를 조회해주세요.</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <small class="text-muted">0 - 0 / 총 0건</small>
                    </div>
                    <nav>
                        <ul class="pagination pagination-sm mb-0">
                            <li class="page-item disabled">
                                <a class="page-link" href="#">이전</a>
                            </li>
                            <li class="page-item active">
                                <a class="page-link" href="#">1</a>
                            </li>
                            <li class="page-item disabled">
                                <a class="page-link" href="#">다음</a>
                            </li>
                        </ul>
                    </nav>
                </div>
            </div>
        </div>
    </div>
    
    <!-- 문서 유형 선택 모달 -->
    <div class="modal fade" id="docTypeModal" tabindex="-1" role="dialog" aria-labelledby="docTypeModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="docTypeModalLabel">문서 유형 선택</h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="row mb-3">
                        <div class="col-12">
                            <button type="button" class="btn btn-sm btn-outline-primary mr-2" id="btnSelectAllDocTypes">전체 선택</button>
                            <button type="button" class="btn btn-sm btn-outline-secondary" id="btnDeselectAllDocTypes">전체 해제</button>
                        </div>
                    </div>
                    <div class="row" id="docTypeCheckboxContainer">
                        <div class="col-12 text-center">
                            <div class="spinner-border text-primary" role="status">
                                <span class="sr-only">로딩 중...</span>
                            </div>
                            <p class="mt-2">문서 유형을 불러오는 중...</p>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">취소</button>
                    <button type="button" class="btn btn-primary" id="btnApplyDocTypeFilter">적용</button>
                </div>
            </div>
        </div>
    </div>
    
    <!-- Chart.js -->
    <script src="${pageContext.request.contextPath}/resources/js/chart.min.js"></script>
    <!-- Chart.js Datalabels Plugin -->
    <script src="${pageContext.request.contextPath}/resources/js/chartjs-plugin-datalabels.min.js"></script>
    
    <script>
        // Chart.js 로드 확인
        if (typeof Chart === 'undefined') {
            console.error('Chart.js가 로드되지 않았습니다.');
        }
        
        let reviewStatusChart, organizationChart, documentTypeChart;
        
        // 문서 유형 관련 전역 변수
        let selectedDocTypes = []; // 선택된 문서 유형 코드 배열
        let docTypeMapping = {}; // 문서 유형 코드 -> 한글명 매핑
        
        // 기관별 inst_cd, prdt_cd 매핑 (ocrRsltList.jsp와 동일)
        const organizationMapping = {
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
            }<% if (isDev) { %>,
            '테스트': {
                inst_cd: ['99'],
                prdt_cd: ['OCR']
            }<% } %>
        };
        
        // 날짜 초기화
        $(document).ready(function() {
            // Chart.js 로드 대기
            if (typeof Chart === 'undefined') {
                console.error('Chart.js를 찾을 수 없습니다.');
                alert('차트 라이브러리 로드에 실패했습니다.');
                return;
            }
            
            // 기간 초기화 - 전체 기간으로 설정 (빈값)
            $('#startDate').val('');
            $('#endDate').val('');
            
            // 기간 선택 버튼 클릭
            $('.btn-group[role="group"] button').on('click', function() {
                $('.btn-group[role="group"] button').removeClass('active');
                $(this).addClass('active');
                
                const period = $(this).data('period');
                const today = new Date();
                let startDate = '';
                let endDate = '';
                
                switch(period) {
                    case 'all':
                        startDate = '';
                        endDate = '';
                        break;
                    case 'today':
                        startDate = today.toISOString().split('T')[0];
                        endDate = today.toISOString().split('T')[0];
                        break;
                    case 'week':
                        const weekAgo = new Date(today);
                        weekAgo.setDate(today.getDate() - 7);
                        startDate = weekAgo.toISOString().split('T')[0];
                        endDate = today.toISOString().split('T')[0];
                        break;
                    case 'month':
                        const monthAgo = new Date(today);
                        monthAgo.setMonth(today.getMonth() - 1);
                        startDate = monthAgo.toISOString().split('T')[0];
                        endDate = today.toISOString().split('T')[0];
                        break;
                }
                
                $('#startDate').val(startDate);
                $('#endDate').val(endDate);
            });
            
            // 초기 데이터 로드
            loadStatistics();
            
            // 문서 유형 목록 로드
            loadDocumentTypes();
            
            // 조회 버튼 클릭
            $('#btnSearch').on('click', function() {
                loadStatistics();
            });
            
            // 문서 유형 모달 이벤트
            $('#btnSelectAllDocTypes').on('click', function() {
                $('.doc-type-checkbox').prop('checked', true);
            });
            
            $('#btnDeselectAllDocTypes').on('click', function() {
                $('.doc-type-checkbox').prop('checked', false);
            });
            
            $('#btnApplyDocTypeFilter').on('click', function() {
                selectedDocTypes = [];
                $('.doc-type-checkbox:checked').each(function() {
                    selectedDocTypes.push($(this).val());
                });
                updateDocTypeDisplay();
                $('#docTypeModal').modal('hide');
            });
        });
        
        // 통계 데이터 로드
        function loadStatistics() {
            const organization = $('#organization').val();
            let instCdList = null;
            let prdtCdList = null;
            
            if (organization && organizationMapping[organization]) {
                instCdList = organizationMapping[organization].inst_cd;
                prdtCdList = organizationMapping[organization].prdt_cd;
            }
            
            const params = {
                ins_dttm_st: $('#startDate').val() || null,
                ins_dttm_en: $('#endDate').val() || null,
                inst_cd: instCdList,
                prdt_cd: prdtCdList,
                doc_tp_cd: selectedDocTypes.length > 0 ? selectedDocTypes[0] : null,
                fail_type: $('#failType').val() || null
            };
            
            $.ajax({
                url: '${pageContext.request.contextPath}/api/getOcrStatistics.do',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(params),
                success: function(response) {
                    console.log('통계 조회 응답:', response);
                    if (response.success) {
                        updateSummaryCards(response.summary);
                        updateCharts(response);
                        updateDetailTable(response.detailList, response.totalCount);
                    } else {
                        console.error('통계 조회 실패:', response);
                        alert('통계 조회에 실패했습니다.\n' + 
                              '오류: ' + (response.message || '') + '\n' +
                              '상세: ' + (response.errorDetail || ''));
                    }
                },
                error: function(xhr, status, error) {
                    console.error('통계 조회 오류:', error);
                    console.error('응답:', xhr.responseText);
                    alert('통계 조회 중 오류가 발생했습니다.\n' + 
                          'Status: ' + status + '\n' +
                          'Error: ' + error);
                }
            });
        }
        
        // 요약 카드 업데이트
        function updateSummaryCards(summary) {
            const total = summary.total_count || 0;
            const normal = summary.normal_count || 0;
            const error = summary.error_count || 0;
            const empty = summary.empty_count || 0;
            
            $('.stats-card.primary .stats-value').text(total.toLocaleString());
            $('.stats-card.success .stats-value').html(
                normal.toLocaleString() + 
                ' <small class="text-muted">(' + ((normal/total*100) || 0).toFixed(1) + '%)</small>'
            );
            $('.stats-card.warning .stats-value').html(
                error.toLocaleString() + 
                ' <small class="text-muted">(' + ((error/total*100) || 0).toFixed(1) + '%)</small>'
            );
            $('.stats-card.danger .stats-value').html(
                empty.toLocaleString() + 
                ' <small class="text-muted">(' + ((empty/total*100) || 0).toFixed(1) + '%)</small>'
            );
        }
        
        // 차트 업데이트
        function updateCharts(response) {
            const summary = response.summary;
            const orgStats = response.organizationStats || [];
            const docStats = response.documentTypeStats || [];
            
            // 추출 오류 분포 차트
            if (reviewStatusChart) reviewStatusChart.destroy();
            const reviewStatusCtx = document.getElementById('reviewStatusChart').getContext('2d');
            reviewStatusChart = new Chart(reviewStatusCtx, {
                type: 'doughnut',
                data: {
                    labels: ['정상', '오류(E)', '빈값(X)'],
                    datasets: [{
                        data: [
                            summary.normal_count || 0,
                            summary.error_count || 0,
                            summary.empty_count || 0
                        ],
                        backgroundColor: ['#87CEEB', '#FFE66D', '#FF6B6B'],
                        borderWidth: 2,
                        borderColor: '#fff'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'bottom' },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    const label = context.label || '';
                                    const value = context.parsed || 0;
                                    const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                    const percentage = ((value / total) * 100).toFixed(1);
                                    return label + ': ' + value + ' (' + percentage + '%)';
                                }
                            }
                        },
                        datalabels: {
                            color: '#fff',
                            font: { weight: 'bold', size: 14 },
                            formatter: function(value, context) {
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = ((value / total) * 100).toFixed(1);
                                return value.toLocaleString() + '\n(' + percentage + '%)';
                            }
                        }
                    }
                },
                plugins: [ChartDataLabels]
            });
            
            // 기관별 오류율 차트
            if (organizationChart) organizationChart.destroy();
            const organizationCtx = document.getElementById('organizationChart').getContext('2d');
            organizationChart = new Chart(organizationCtx, {
                type: 'bar',
                data: {
                    labels: orgStats.map(item => item.inst_cd),
                    datasets: [{
                        label: '정상',
                        data: orgStats.map(item => item.normal_count || 0),
                        backgroundColor: '#87CEEB'
                    }, {
                        label: '오류(E)',
                        data: orgStats.map(item => item.error_count || 0),
                        backgroundColor: '#FFE66D'
                    }, {
                        label: '빈값(X)',
                        data: orgStats.map(item => item.empty_count || 0),
                        backgroundColor: '#FF6B6B'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'bottom' },
                        datalabels: { display: false }
                    },
                    scales: {
                        x: { stacked: true },
                        y: { stacked: true, beginAtZero: true }
                    }
                }
            });
            
            // 서류 유형별 오류율 차트
            if (documentTypeChart) documentTypeChart.destroy();
            const documentTypeCtx = document.getElementById('documentTypeChart').getContext('2d');
            documentTypeChart = new Chart(documentTypeCtx, {
                type: 'bar',
                data: {
                    labels: docStats.map(item => item.doc_tp_cd),
                    datasets: [{
                        label: '오류(E) %',
                        data: docStats.map(item => {
                            const total = item.total_count || 1;
                            return ((item.error_count || 0) / total * 100).toFixed(1);
                        }),
                        backgroundColor: '#FFE66D'
                    }, {
                        label: '빈값(X) %',
                        data: docStats.map(item => {
                            const total = item.total_count || 1;
                            return ((item.empty_count || 0) / total * 100).toFixed(1);
                        }),
                        backgroundColor: '#FF6B6B'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'bottom' },
                        datalabels: { display: false }
                    },
                    scales: {
                        x: { stacked: true },
                        y: {
                            stacked: true,
                            beginAtZero: true,
                            max: 50,
                            ticks: {
                                callback: function(value) {
                                    return value + '%';
                                }
                            }
                        }
                    }
                }
            });
        }
        
        // 상세 테이블 업데이트
        function updateDetailTable(detailList, totalCount) {
            const tbody = $('.table tbody');
            tbody.empty();
            
            if (!detailList || detailList.length === 0) {
                tbody.append('<tr><td colspan="11" class="text-center">데이터가 없습니다.</td></tr>');
                return;
            }
            
            detailList.forEach((item, index) => {
                const errorRate = ((item.error_count + item.empty_count) / item.total_count * 100).toFixed(1);
                const errorWidth = (item.error_count / item.total_count * 100).toFixed(1);
                const emptyWidth = (item.empty_count / item.total_count * 100).toFixed(1);
                
                const row = '<tr>' +
                    '<td class="text-center">' + (index + 1) + '</td>' +
                    '<td>' + (item.ins_dttm || '') + '</td>' +
                    '<td>' + item.ctrl_yr + '-' + item.inst_cd + '-' + item.prdt_cd + '-' + item.ctrl_no + '</td>' +
                    '<td>' + item.inst_cd + '</td>' +
                    '<td>' + item.doc_tp_cd + '</td>' +
                    '<td class="text-center">' + (item.total_count || 0) + '</td>' +
                    '<td class="text-center">' + (item.normal_count || 0) + '</td>' +
                    '<td class="text-center">' + (item.error_count || 0) + '</td>' +
                    '<td class="text-center">' + (item.empty_count || 0) + '</td>' +
                    '<td>' +
                        '<div class="progress">' +
                            '<div class="progress-bar" style="width: ' + errorWidth + '%; background-color: #FFE66D;"></div>' +
                            '<div class="progress-bar" style="width: ' + emptyWidth + '%; background-color: #FF6B6B;"></div>' +
                        '</div>' +
                        '<small class="text-muted">' + errorRate + '%</small>' +
                    '</td>' +
                    '<td class="text-center">' +
                        '<button class="btn btn-sm btn-outline-primary" onclick="viewDetail(\'' + item.ocr_doc_no + '\')">' +
                            '<i class="fas fa-eye"></i>' +
                        '</button>' +
                    '</td>' +
                '</tr>';
                tbody.append(row);
            });
            
            $('.text-muted:contains("총")').text('1 - ' + detailList.length + ' / 총 ' + totalCount + '건');
        }
        
        // 상세 보기
        function viewDetail(ocrDocNo) {
            // 현재 행의 데이터를 찾아서 필요한 파라미터 추출
            const row = event.target.closest('tr');
            const cells = row.cells;
            const ctrlInfo = cells[2].textContent.split('-'); // ctrl_yr-inst_cd-prdt_cd-ctrl_no
            const docTpCd = cells[4].textContent;
            
            const url = '${pageContext.request.contextPath}/main' +
                '?pageChange=/ocrRsltDetail' +
                '&ctrl_yr=' + ctrlInfo[0] +
                '&inst_cd=' + ctrlInfo[1] +
                '&prdt_cd=' + ctrlInfo[2] +
                '&ctrl_no=' + ctrlInfo[3] +
                '&doc_tp_cd=' + docTpCd +
                '&ocr_doc_no=' + ocrDocNo;
            
            window.open(url, '_blank');
        }
        
        // 문서 유형 목록 로드
        function loadDocumentTypes() {
            $.ajax({
                url: '${pageContext.request.contextPath}/api/getDocumentTypes.do',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({}),
                success: function(response) {
                    if (response.success && response.data) {
                        displayDocumentTypes(response.data);
                    }
                },
                error: function(xhr, status, error) {
                    console.error('문서 유형 조회 오류:', error);
                    $('#docTypeCheckboxContainer').html('<div class="col-12 text-center text-danger">문서 유형을 불러오는데 실패했습니다.</div>');
                }
            });
        }
        
        // 문서 유형 표시
        function displayDocumentTypes(docTypes) {
            const serviceGroups = {
                '모바일반환보증': [],
                '모바일임대보증': [],
                '신한전세': [],
                '전세안심보험(카손)': [],
                '하나은행(사전)': [],
                '테스트': [],
                '기타': []
            };
            
            docTypes.forEach(function(docType) {
                const instCd = docType.inst_cd;
                const prdtCd = docType.prdt_cd;
                const docTpCd = docType.doc_tp_cd;
                const displayName = docType.doc_kr_nm || docTpCd;
                
                docTypeMapping[docTpCd] = displayName;
                
                let serviceName = '기타';
                if ((instCd === '01' || instCd === '45' || instCd === '47' || instCd === '49') && prdtCd === '820') {
                    serviceName = '모바일반환보증';
                } else if ((instCd === '01' || instCd === '45' || instCd === '47' || instCd === '49') && prdtCd === '830') {
                    serviceName = '모바일임대보증';
                } else if (instCd === '01' && ['001', '003', '005', '002', '016', '041', '050', '118', '119', '120',
                    '053', '200', '201', '202', '203', '217', '219', '220', '221', '007',
                    '014', '020', '031', '129', '028', '037', '032', '038', '128', '029',
                    '030', '036', '127', '027'].includes(prdtCd)) {
                    serviceName = '신한전세';
                } else if (instCd === '61' && prdtCd === 'L01') {
                    serviceName = '전세안심보험(카손)';
                } else if (instCd === '02' && ['007', '222', '223', '224', '225'].includes(prdtCd)) {
                    serviceName = '하나은행(사전)';
                } else if (instCd === '99' && prdtCd === 'OCR') {
                    serviceName = '테스트';
                }
                
                const exists = serviceGroups[serviceName].some(item => item.doc_tp_cd === docTpCd);
                if (!exists) {
                    serviceGroups[serviceName].push({
                        doc_tp_cd: docTpCd,
                        doc_kr_nm: displayName
                    });
                }
            });
            
            let html = '';
            let index = 0;
            
            Object.keys(serviceGroups).forEach(function(serviceName) {
                const group = serviceGroups[serviceName];
                if (group.length > 0) {
                    html += '<div class="col-12 doc-type-group">';
                    html += '<div class="doc-type-group-title">' + serviceName + '</div>';
                    html += '<div class="row">';
                    
                    group.forEach(function(docType) {
                        const displayText = '[' + docType.doc_tp_cd + '] ' + docType.doc_kr_nm;
                        html += '<div class="col-md-4 mb-2">';
                        html += '<div class="custom-control custom-checkbox">';
                        html += '<input type="checkbox" class="custom-control-input doc-type-checkbox" id="docType' + index + '" value="' + docType.doc_tp_cd + '">';
                        html += '<label class="custom-control-label" for="docType' + index + '" title="' + displayText + '">' + displayText + '</label>';
                        html += '</div>';
                        html += '</div>';
                        index++;
                    });
                    
                    html += '</div></div>';
                }
            });
            
            $('#docTypeCheckboxContainer').html(html);
        }
        
        // 문서 유형 선택 상태 표시 업데이트
        function updateDocTypeDisplay() {
            const count = selectedDocTypes.length;
            $('#selectedDocTypeCount').text(count);
        }
    </script>
</body>
</html>
