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
        url: '/rf_ocr_verf/api/getOcrDocumentDetail.do',
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
                
                // 이미지 정보 표시
                displayImage(response.data);
                
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
    // 관리번호 조합
    var ctrlNo = data.ctrl_yr + '-' + data.inst_cd + '-' + data.prdt_cd + '-' + data.ctrl_no;
    $('#ctrlNo').text(ctrlNo);
    
    // 등록일시
    $('#insDttm').text(data.ins_dttm || '-');
    
    // 문서유형
    $('#docTpCd').text(data.doc_tp_cd || '-');
    
    // 한글 서류명
    $('#docTitle').text(data.doc_title || '-');
    
    // 기관-상품명 (매핑 필요 시 추가)
    var orgProduct = data.inst_cd + '-' + data.prdt_cd;
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
    
    var html = '<div class="text-center" style="position: absolute; bottom: 10px; left: 0; right: 0;">';
    html += '<div class="btn-group" role="group">';
    
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
    
    html += '</div></div>';
    
    // 이미지 뷰어 컨테이너에 position: relative 추가
    $('#imageViewer').parent().css('position', 'relative');
    $('#imageViewer').append(html);
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
        tbody.html('<tr><td colspan="3" class="text-center text-muted">OCR 결과가 없습니다.</td></tr>');
        return;
    }
    
    var html = '';
    
    // 서버에서 받은 데이터를 테이블 행으로 변환
    ocrResults.forEach(function(item, index) {
        // 한글명 표시 (ITEM_NM), 영문 코드는 숨김
        var itemName = item.item_nm || item.item_cd || '-';
        var itemValue = item.item_value || '';
        
        // 빈 값 체크
        var isEmpty = !itemValue || 
                      itemValue.trim() === '' || 
                      itemValue === 'null' || 
                      itemValue === 'undefined';
        
        var statusIcon = isEmpty 
            ? '<i class="fas fa-circle text-danger"></i>' 
            : '<i class="fas fa-circle text-success"></i>';
        
        html += '<tr>';
        html += '<td>' + itemName + '</td>';
        html += '<td>' + itemValue + '</td>';
        html += '<td class="text-center">' + statusIcon + '</td>';
        html += '</tr>';
    });
    
    tbody.html(html);
}

/**
 * 이미지 표시 (임시: 경로만 표시)
 */
function displayImage(data) {
    if (!data) {
        $('#imageViewer').html('<p class="text-muted">데이터가 없습니다.</p>');
        return;
    }
    
    var html = '<div class="text-left p-3">';
    
    if (data.doc_fl_sav_pth_nm) {
        html += '<p><strong>이미지 경로:</strong></p>';
        html += '<p class="text-break" style="word-break: break-all;">' + data.doc_fl_sav_pth_nm + '</p>';
    } else {
        html += '<p class="text-warning">이미지 경로 정보가 없습니다.</p>';
    }
    
    html += '<hr>';
    html += '<p><strong>OCR 문서 번호:</strong> ' + (data.ocr_doc_no || '-') + '</p>';
    html += '<p><strong>OCR 결과 번호:</strong> ' + (data.ocr_rslt_no || '-') + '</p>';
    html += '<p><strong>문서 유형:</strong> ' + (data.doc_tp_cd || '-') + '</p>';
    html += '</div>';
    
    $('#imageViewer').html(html);
}

