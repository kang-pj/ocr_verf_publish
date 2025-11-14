$(document).ready(function() {
    // URL 파라미터에서 관리번호 정보 가져오기
    var urlParams = new URLSearchParams(window.location.search);
    var ctrlYr = urlParams.get('ctrl_yr');
    var instCd = urlParams.get('inst_cd');
    var prdtCd = urlParams.get('prdt_cd');
    var ctrlNo = urlParams.get('ctrl_no');
    var docTpCd = urlParams.get('doc_tp_cd');
    
    if (!ctrlYr || !instCd || !prdtCd || !ctrlNo) {
        alert('관리번호 정보가 없습니다.');
        history.back();
        return;
    }
    
    // 문서 상세 정보 로드
    loadDocumentDetail(ctrlYr, instCd, prdtCd, ctrlNo, docTpCd);
});

/**
 * 문서 상세 정보 로드
 */
function loadDocumentDetail(ctrlYr, instCd, prdtCd, ctrlNo, docTpCd) {
    console.log('문서 상세 조회:', ctrlYr, instCd, prdtCd, ctrlNo, docTpCd);
    
    var params = {
        ctrl_yr: ctrlYr,
        inst_cd: instCd,
        prdt_cd: prdtCd,
        ctrl_no: ctrlNo,
        doc_tp_cd: docTpCd || null
    };
    
    $.ajax({
        url: '/rf_ocr_verf/api/getOcrDocumentDetail.do',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(params),
        success: function(response) {
            console.log('서버 응답:', response);
            
            if (response.success && response.data) {
                // 기본 정보 표시
                displayDocumentInfo(response.data);
                
                // 서류 목록 표시
                displayDocumentList(response.documentList || [], docTpCd);
                
                // OCR 결과 표시
                displayOcrResults(response.ocrResults || []);
                
                // 이미지 표시 (경로가 있는 경우)
                if (response.data.img_path) {
                    displayImage(response.data.img_path);
                }
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
    if (!ocrResults || ocrResults.length === 0) {
        $('#pageList').html('<p class="text-center text-muted">OCR 결과가 없습니다.</p>');
        return;
    }
    
    var html = '';
    
    ocrResults.forEach(function(result, index) {
        html += '<div class="card mb-3">';
        html += '<div class="card-header py-2">';
        html += '<small class="font-weight-bold">페이지 ' + (index + 1) + '</small>';
        html += '</div>';
        html += '<div class="card-body p-2">';
        html += '<pre style="white-space: pre-wrap; font-size: 0.75rem; margin: 0;">' + (result.ocr_rslt_txt || '결과 없음') + '</pre>';
        html += '</div>';
        html += '</div>';
    });
    
    $('#pageList').html(html);
}

/**
 * 이미지 표시
 */
function displayImage(imgPath) {
    if (!imgPath) {
        $('#imageViewer').html('<p class="text-muted">이미지가 없습니다.</p>');
        return;
    }
    
    var html = '<img src="' + imgPath + '" alt="문서 이미지" onerror="this.parentElement.innerHTML=\'<p class=text-danger>이미지를 불러올 수 없습니다.</p>\'">';
    $('#imageViewer').html(html);
}

