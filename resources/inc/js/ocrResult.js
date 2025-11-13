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
                // DataTables 기본 파라미터를 서버 형식으로 변환
                var params = {
                    // 페이징
                    paging: Math.floor(d.start / d.length),
                    num: d.length,
                    
                    // 정렬
                    sort: d.order && d.order.length > 0 ? d.order[0].dir.toUpperCase() : 'DESC',
                    
                    // 검색 조건
                    ctrl_no: $('#searchManagementNo').val() || null,
                    ins_dttm_st: $('#startDate').val() || null,
                    ins_dttm_en: $('#endDate').val() || null,
                    inst_cd: getSelectedOrganizations(),
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
            { className: "text-center", targets: [0, 1, 2, 3, 4, 5] },
            { orderable: false, targets: [0, 2, 3, 4, 5] },
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
                    return '<a href="#" class="text-primary" onclick="viewDetail(\'' + ctrlNo + '\'); return false;">' + ctrlNo + '</a>';
                }
            },
            { 
                data: 'doc_tp_cd',
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
                    if (data === 'N') return '미완료';
                    return '미완료';
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
            '<option value="N">미완료</option>' +
            '<option value="F">실패</option>' +
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

// 선택된 기관 목록 가져오기 (배열로 반환)
function getSelectedOrganizations() {
    var selected = [];
    $('input[type="checkbox"]:checked').each(function() {
        selected.push($(this).val());
    });
    return selected.length > 0 ? selected : null;
}

// 상세보기 (예시)
function viewDetail(ctrlNo) {
    console.log('상세보기:', ctrlNo);
    // 상세 페이지로 이동 또는 모달 표시
    // location.href = '/rf_ocr_verf/detail?ctrl_no=' + ctrlNo;
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
