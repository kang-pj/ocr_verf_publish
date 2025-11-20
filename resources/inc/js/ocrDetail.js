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
                
                // 이미지 정보 표시 (비동기)
                displayImage(response.data);
                
                // 페이지 네비게이션 표시 (이미지 로딩 상관없이 항상 표시)
                setTimeout(function() {
                    displayPageNavigation(response.totalPages || 1);
                }, 100);
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
 * 이미지 표시
 */
function displayImage(data) {
    if (!data || !data.doc_fl_sav_pth_nm) {
        $('#imageViewer').html('<p class="text-muted">이미지 정보가 없습니다.</p>');
        return;
    }
    
    // 이미지 로딩
    loadImage(data);
}

/**
 * 이미지 로딩 API 호출
 */
function loadImage(data) {
    // 로딩바 표시
    var loadingHtml = '<div style="display: flex; align-items: center; justify-content: center; height: 100%;">';
    loadingHtml += '<div class="spinner-border text-primary" role="status">';
    loadingHtml += '<span class="sr-only">로딩 중...</span>';
    loadingHtml += '</div></div>';
    $('#imageViewer').html(loadingHtml);
    
    var params = {
        inst_cd: data.inst_cd,
        prdt_cd: data.prdt_cd,
        image_path: data.doc_fl_sav_pth_nm,
        ext: data.doc_fl_ext
    };
    
    $.ajax({
        url: '/rf-ocr-verf/api/getOcrImage.do',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(params),
        success: function(response) {
            if (response.success && response.data) {
                // Base64 이미지 또는 경로
                if (response.data.startsWith('data:image')) {
                    var html = '<img src="' + response.data + '" style="width: 100%; height: 100%; object-fit: contain; border-radius: 5px;" onerror="handleImageError()">';
                    $('#imageViewer').html(html);
                } else {
                    displayImagePath(data, response.data, '외부 API 응답 (파일 경로)');
                }
            } else {
                displayImagePath(data, data.doc_fl_sav_pth_nm, '서버 응답 실패');
            }
        },
        error: function(xhr, status, error) {
            var errorMsg = '서버 연결 실패';
            if (xhr.status === 404) {
                errorMsg = 'API 엔드포인트 없음 (404)';
            } else if (xhr.status === 500) {
                errorMsg = '서버 오류 (500)';
            } else if (status === 'timeout') {
                errorMsg = '요청 시간 초과';
            }
            displayImagePath(data, data.doc_fl_sav_pth_nm, errorMsg);
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

