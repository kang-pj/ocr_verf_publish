<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<c:set var="pageTitle" value="OCR 문서 상세" />
<c:set var="currentPage" value="detail" />

<%@ include file="resources/inc/header.jsp" %>
<%@ include file="resources/inc/sidebar.jsp" %>

<style>
    /* 상세 페이지 전용 스타일 */
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
    }
    #ocrResultTable tbody td {
        vertical-align: middle;
    }
    #ocrResultTable tbody td:nth-child(1) {
        font-weight: 500;
    }
    #ocrResultTable tbody td:nth-child(2) {
        word-break: break-all;
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
                    <span class="badge badge-info" id="statusBadge">대기</span>
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
                    <div class="card shadow mb-4" style="height: 1000px;">
                        <div class="card-header py-2">
                            <h6 class="m-0 font-weight-bold text-primary">이미지 뷰어</h6>
                        </div>
                        <div class="card-body d-flex align-items-center justify-content-center" style="height: calc(100% - 50px); overflow: auto; background-color: #f8f9fa;">
                            <div id="imageViewer" class="text-center">
                                <p class="text-muted">이미지를 불러오는 중...</p>
                            </div>
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
                                        <th style="width: 30%;">항목명</th>
                                        <th>추출값</th>
                                        <th style="width: 35px;">상태</th>
                                    </tr>
                                </thead>
                                <tbody id="ocrResultBody">
                                    <tr>
                                        <td colspan="3" class="text-center text-muted">OCR 결과가 없습니다.</td>
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
                                    <i class="fas fa-file-alt"></i> 서류 001
                                </a>
                                <a href="#" class="doc-type-item">
                                    <i class="fas fa-file-alt"></i> 서류 002
                                </a>
                                <div class="doc-type-item current">
                                    <i class="fas fa-check-circle text-success"></i> 서류 003
                                </div>
                                <a href="#" class="doc-type-item">
                                    <i class="fas fa-file-alt"></i> 서류 004
                                </a>
                                <a href="#" class="doc-type-item">
                                    <i class="fas fa-file-alt"></i> 서류 005
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

