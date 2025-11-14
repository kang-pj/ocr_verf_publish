$(document).ready(function() {
    // URL 파라미터에서 관리번호 가져오기
    var urlParams = new URLSearchParams(window.location.search);
    var ctrlNo = urlParams.get('ctrl_no');
    var docTpCd = urlParams.get('doc_tp_cd');
    
    if (!ctrlNo) {
        alert('관리번호가 없습니다.');
        history.back();
        return;
    }
    
    // 문서 상세 정보 로드
    loadDocumentDetail(ctrlNo, docTpCd);
});

/**
 * 문서 상세 정보 로드
 */
function loadDocumentDetail(ctrlNo, docTpCd) {
    console.log('문서 상세 조회:', ctrlNo, docTpCd);
    
    // 관리번호 파싱 (qq-ww-eee-rrrrrr)
    var parts = ctrlNo.split('-');
    
    var params = {
        ctrl_yr: parts[0] || null,
        inst_cd: parts[1] || null,
        prdt_cd: parts[2] || null,
        ctrl_no: parts[3] || null,
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
                displayDocumentInfo(response.data);
                displayDocumentList(response.data.documents || []);
            } else {
                alert('문서 정보를 불러올 수 없습니다.');
                $('#documentList').html('<p class="text-center text-danger">데이터를 불러오지 못했습니다.</p>');
            }
        },
        error: function(xhr, status, error) {
            console.error('서버 연결 실패:', error);
            alert('서버 연결에 실패했습니다.');
            $('#documentList').html('<p class="text-center text-danger">서버 연결에 실패했습니다.</p>');
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
    
    // 페이지수
    $('#docPage').text(data.doc_page || '0');
    
    // 검증여부
    var ocrYnText = '오류';
    if (data.ocr_yn === 'Y') ocrYnText = '완료';
    else if (data.ocr_yn === 'N') ocrYnText = '대기';
    else if (data.ocr_yn === 'X') ocrYnText = '실패';
    $('#ocrYn').text(ocrYnText);
}

/**
 * 서류 목록 표시
 */
function displayDocumentList(documents) {
    if (!documents || documents.length === 0) {
        $('#documentList').html('<p class="text-center text-muted">서류가 없습니다.</p>');
        return;
    }
    
    var html = '<div class="table-responsive">';
    html += '<table class="table table-bordered table-hover">';
    html += '<thead>';
    html += '<tr>';
    html += '<th class="text-center">No</th>';
    html += '<th class="text-center">서류명</th>';
    html += '<th class="text-center">페이지</th>';
    html += '<th class="text-center">상태</th>';
    html += '<th class="text-center">작업</th>';
    html += '</tr>';
    html += '</thead>';
    html += '<tbody>';
    
    documents.forEach(function(doc, index) {
        html += '<tr>';
        html += '<td class="text-center">' + (index + 1) + '</td>';
        html += '<td>' + (doc.doc_name || '-') + '</td>';
        html += '<td class="text-center">' + (doc.page_no || '-') + '</td>';
        html += '<td class="text-center">' + getStatusBadge(doc.status) + '</td>';
        html += '<td class="text-center">';
        html += '<button class="btn btn-sm btn-primary" onclick="viewDocument(\'' + doc.doc_id + '\')">보기</button>';
        html += '</td>';
        html += '</tr>';
    });
    
    html += '</tbody>';
    html += '</table>';
    html += '</div>';
    
    $('#documentList').html(html);
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
 * 문서 보기
 */
function viewDocument(docId) {
    console.log('문서 보기:', docId);
    // TODO: 문서 뷰어 구현
    alert('문서 보기 기능은 추후 구현 예정입니다.\n문서 ID: ' + docId);
}

/**
 * Scroll to top
 */
$(window).scroll(function() {
    if ($(this).scrollTop() > 100) {
        $('.scroll-to-top').fadeIn();
    } else {
        $('.scroll-to-top').fadeOut();
    }
});

$('.scroll-to-top').click(function() {
    $('html, body').animate({scrollTop: 0}, 800);
    return false;
});
