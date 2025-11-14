<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<c:set var="pageTitle" value="이미지 전산화 검증" />
<c:set var="currentPage" value="list" />

<%@ include file="resources/inc/header.jsp" %>
<%@ include file="resources/inc/sidebar.jsp" %>

<!-- Content Wrapper -->
<div id="content-wrapper" class="d-flex flex-column">
    <div id="content">
        <%@ include file="resources/inc/topbar.jsp" %>
        
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
                            <label class="mb-0">기간</label>
                        </div>
                        <div class="col-auto">
                            <div class="input-group input-group-sm" style="width: 250px;">
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
                            <div class="custom-control custom-checkbox custom-control-inline">
                                <input type="checkbox" class="custom-control-input" id="org1" value="모바일반환보증">
                                <label class="custom-control-label" for="org1">모바일반환보증</label>
                            </div>
                            <div class="custom-control custom-checkbox custom-control-inline">
                                <input type="checkbox" class="custom-control-input" id="org2" value="신한전세">
                                <label class="custom-control-label" for="org2">신한전세</label>
                            </div>
                            <div class="custom-control custom-checkbox custom-control-inline">
                                <input type="checkbox" class="custom-control-input" id="org3" value="전세안심보험(카손)">
                                <label class="custom-control-label" for="org3">전세안심보험(카손)</label>
                            </div>
                            <div class="custom-control custom-checkbox custom-control-inline">
                                <input type="checkbox" class="custom-control-input" id="org4" value="하나은행(사전)">
                                <label class="custom-control-label" for="org4">하나은행(사전)</label>
                            </div>
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="col-auto" style="width: 120px;"></div>
                        <div class="col-auto">
                            <button type="button" class="btn btn-primary btn-sm" id="btnSearch">
                                <i class="fas fa-search"></i> 검색
                            </button>
                            <button type="button" class="btn btn-secondary btn-sm" id="btnReset">
                                <i class="fas fa-redo"></i> 초기화
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- OCR 결과 테이블 -->
            <div class="card shadow mb-4">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">OCR 결과 목록</h6>
                </div>
                <div class="card-body">
                    <table class="table table-bordered table-hover" id="ocrResultTable" width="100%" cellspacing="0">
                        <thead>
                            <tr>
                                <th>No</th>
                                <th>등록일시</th>
                                <th>관리번호</th>
                                <th>문서유형</th>
                                <th>한글 서류명 (결과)</th>
                                <th>페이지수</th>
                                <th>검증여부</th>
                            </tr>
                        </thead>
                        <tbody></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
    
    <%@ include file="resources/inc/footer.jsp" %>
    
    <!-- DataTables JS -->
    <script src="https://cdn.datatables.net/1.13.7/js/jquery.dataTables.min.js"></script>
    <script src="https://cdn.datatables.net/1.13.7/js/dataTables.bootstrap4.min.js"></script>
    <!-- Custom JS -->
    <script src="resources/inc/js/ocrResult.js?v=2"></script>
