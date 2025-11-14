$(document).ready(function() {
    // ========================================
    // DataTable 초기화 (Server-Side Processing)
    // ========================================
    var table = $('#ocrResultTable').DataTable({
        processing: true,
        serverSide: true,  // 서버사이드 처리 활성화
        ajax: {
            url: '/rf_ocr_verf/api/getOcrResultList.do',
            type: 'POST',
            contentType: 'application/json',  // JSON 전송
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
                console.log('서버로 전송되는 파라미터:', params);
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
            { className: "text-center", targets: [0, 1, 2, 3, 4, 5, 6] },
            { orderable: false, targets: [0, 2, 3, 4, 5, 6] },
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
                    if (data === 'Y') return '완료';
                    if (data === 'N') return '대기';
                    if (data === 'X') return '실패';
                    return '오류';
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
    
    // 하이픈 제거하고 숫자만 추출
    var cleaned = input.replace(/[^0-9]/g, '');
    
    if (cleaned.length === 0) {
        return null;
    }
    
    // 2-2-3-6 패턴으로 자르기
    var result = {
        ctrl_yr: null,
        inst_cd: null,
        prdt_cd: null,
        ctrl_no: null
    };
    
    var pos = 0;
    
    // ctrl_yr (2자리)
    if (cleaned.length >= 2) {
        result.ctrl_yr = cleaned.substring(0, 2);
        pos = 2;
    } else {
        result.ctrl_yr = cleaned;
        return result;
    }
    
    // inst_cd (2자리)
    if (cleaned.length >= 4) {
        result.inst_cd = cleaned.substring(2, 4);
        pos = 4;
    } else if (cleaned.length > 2) {
        result.inst_cd = cleaned.substring(2);
        return result;
    }
    
    // prdt_cd (3자리)
    if (cleaned.length >= 7) {
        result.prdt_cd = cleaned.substring(4, 7);
        pos = 7;
    } else if (cleaned.length > 4) {
        result.prdt_cd = cleaned.substring(4);
        return result;
    }
    
    // ctrl_no (6자리)
    if (cleaned.length > 7) {
        result.ctrl_no = cleaned.substring(7, 13); // 최대 6자리
    }
    
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
function viewDetail(ctrlYr, instCd, prdtCd, ctrlNo, docTpCd) {
    console.log('상세보기:', ctrlYr, instCd, prdtCd, ctrlNo, docTpCd);
    
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
    var day = String(date.getDate()).padStart(2, '0');
    return year + '-' + month + '-' + day;
}
