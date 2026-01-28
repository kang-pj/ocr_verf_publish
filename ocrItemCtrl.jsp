<%@ page pageEncoding="utf-8" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <title>OCR 항목 코드 관리</title>
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
        .card-body {
            position: relative !important;
            padding: 15px 15px 40px 15px !important;
            overflow: visible !important;
        }
        #itemTable_wrapper {
            width: 100% !important;
            max-width: 100% !important;
            margin: 0 !important;
            padding: 0 !important;
        }
        .dataTables_wrapper .dataTables_paginate {
            text-align: center !important;
            margin: 10px 0 !important;
        }
        .dataTables_wrapper .dataTables_paginate .pagination {
            justify-content: center !important;
        }
        /* 상단 페이징 스타일 */
        .dataTables_wrapper .row:nth-child(2) .dataTables_paginate {
            margin-bottom: 15px;
        }
        /* 하단 페이징 스타일 */
        .dataTables_wrapper .row:nth-child(4) .dataTables_paginate {
            border-top: 1px solid #e3e6f0;
            padding-top: 10px;
            margin-top: 15px;
        }

        /* DataTable 컨테이너 스타일 */
        .dataTables_wrapper {
            width: 100% !important;
            margin: 0 !important;
        }

        /* 상단 컨트롤 영역 */
        .dataTables_wrapper .top-controls {
            margin-bottom: 15px !important;
            border-bottom: 1px solid #e3e6f0;
            padding-bottom: 10px;
            align-items: center !important;
        }
        
        /* 상단 페이징을 가운데 정렬 */
        .dataTables_wrapper .top-controls .dataTables_paginate {
            text-align: center !important;
            margin: 0 !important;
        }

        /* 테이블 컨테이너 */
        .dataTables_wrapper .table-container {
            margin: 0 !important;
        }

        /* 하단 컨트롤 영역 */
        .dataTables_wrapper .bottom-controls {
            margin-top: 15px !important;
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
        #itemTable {
            font-size: 0.85rem;
            width: 100% !important;
            table-layout: auto !important;
        }
        #itemTable thead th {
            padding: 0.5rem;
            font-size: 0.85rem;
            white-space: nowrap;
        }
        #itemTable tbody td {
            padding: 0.5rem;
            vertical-align: middle;
        }

        .badge {
            font-weight: 100;
            padding: 0.3em 0.9em;
            border-radius: 3px;
            font-size: 12px;
        }
        .badge-success {
            background-color: #d4edda !important;
            color: #28a745 !important;
        }
        .badge-danger {
            background-color: #f8d7da !important;
            color: #dc3545 !important;
        }
        /* 항목 관리 버튼 */
        .btn-item-action {
            background-color: #f8f9fa;
            border: 1px solid #d1d3e2;
            color: #5a5c69;
            font-size: 0.8rem;
            padding: 0.375rem 0.75rem;
        }
        .btn-item-action:hover:not(:disabled) {
            background-color: #eaecf4;
            border-color: #c5c7d4;
            color: #5a5c69;
        }
        .btn-item-action:disabled {
            opacity: 0.5;
            cursor: not-allowed;
        }
        .btn-item-action i {
            margin-right: 4px;
        }
    </style>
</head>
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
                                <label class="mb-0">항목코드/항목명</label>
                            </div>
                            <div class="col-md-3">
                                <input type="text" class="form-control form-control-sm" id="searchKeyword" placeholder="항목코드 또는 항목명 입력">
                            </div>
                        </div>
                        <div class="form-row align-items-center mb-3">
                            <div class="col-auto" style="width: 120px;">
                                <label class="mb-0">사용여부</label>
                            </div>
                            <div class="col-md-2">
                                <select class="form-control form-control-sm" id="searchUseYn">
                                    <option value="">전체</option>
                                    <option value="Y" selected>사용</option>
                                    <option value="N">미사용</option>
                                </select>
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

                <!-- OCR 항목 코드 테이블 -->
                <div class="card shadow mb-4">
                    <div class="card-header py-3 d-flex justify-content-between align-items-center">
                        <h6 class="m-0 font-weight-bold text-primary">OCR 항목 코드 목록</h6>
                        <div>
                            <button type="button" class="btn btn-sm btn-item-action mr-1" id="btnAdd">
                                <i class="fas fa-plus"></i>
                                <span>추가</span>
                            </button>
                            <button type="button" class="btn btn-sm btn-item-action mr-1" id="btnEdit" disabled>
                                <i class="fas fa-edit"></i>
                                <span>수정</span>
                            </button>
                            <button type="button" class="btn btn-sm btn-item-action" id="btnDelete" disabled>
                                <i class="fas fa-trash"></i>
                                <span>삭제</span>
                            </button>
                            <button type="button" class="btn btn-sm btn-item-action" id="btnActivate" disabled style="display: none;">
                                <i class="fas fa-check"></i>
                                <span>사용</span>
                            </button>
                        </div>
                    </div>
                    <div class="card-body">
                        <table class="table table-bordered table-hover" id="itemTable" width="100%" cellspacing="0">
                            <thead>
                            <tr>
                                <th style="width: 50px;">No</th>
                                <th>항목코드</th>
                                <th>항목명</th>
                                <th style="text-align: center;">사용여부</th>
                                <th style="text-align: center;">등록자</th>
                                <th style="text-align: center;">등록일시</th>
                                <th style="text-align: center;">수정자</th>
                                <th style="text-align: center;">수정일시</th>
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

<!-- Scroll to Top Button-->
<a class="scroll-to-top rounded" href="#page-top">
    <i class="fas fa-angle-up"></i>
</a>

<!-- 추가/수정 모달 -->
<div class="modal fade" id="itemModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="modalTitle">항목 코드 추가</h5>
                <button type="button" class="close" data-dismiss="modal">
                    <span>&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <form id="itemForm">
                    <div class="form-group">
                        <label>항목코드 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="modalItemCd" required>
                    </div>
                    <div class="form-group">
                        <label>항목명 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="modalItemNm" required>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">취소</button>
                <button type="button" class="btn btn-primary" id="btnModalConfirm">확인</button>
            </div>
        </div>
    </div>
</div>

<script>
    var dataTable;

    $(document).ready(function() {
        // DataTable 초기화
        dataTable = $('#itemTable').DataTable({
            processing: true,
            serverSide: false,
            paging: true,
            pageLength: 50,
            lengthMenu: [[10, 25, 50, 100, -1], [10, 25, 50, 100, "전체"]],
            order: [[1, 'asc']],  // 항목코드로 정렬
            dom: '<"top-controls row"<"col-sm-12 col-md-4"l><"col-sm-12 col-md-4"p><"col-sm-12 col-md-4"f>>' +
                '<"table-container row"<"col-sm-12"t>>' +
                '<"bottom-controls row"<"col-sm-12 col-md-5"i><"col-sm-12 col-md-7"p>>',
            language: {
                emptyTable: "데이터가 없습니다.",
                info: "총 _TOTAL_건 중 _START_~_END_건 표시",
                infoEmpty: "0건",
                infoFiltered: "(전체 _MAX_건 중 검색결과)",
                lengthMenu: "_MENU_ 개씩 보기",
                search: "검색:",
                zeroRecords: "검색 결과가 없습니다.",
                paginate: {
                    first: "처음",
                    last: "마지막",
                    next: "다음",
                    previous: "이전"
                }
            },
            columns: [
                {
                    data: null,
                    render: function(data, type, row, meta) {
                        return '<div style="text-align: center;">' + (meta.row + 1) + '</div>';
                    },
                    orderable: false,
                    width: '50px'
                },
                { data: 'item_cd' },
                { data: 'item_nm' },
                {
                    data: 'use_yn',
                    render: function(data) {
                        var badge = '';
                        if (data === 'Y') {
                            badge = '<span class="badge badge-success">사용</span>';
                        } else {
                            badge = '<span class="badge badge-danger">미사용</span>';
                        }
                        return '<div style="text-align: center;">' + badge + '</div>';
                    }
                },
                {
                    data: 'insr_id',
                    render: function(data) {
                        return '<div style="text-align: center;">' + (data || '-') + '</div>';
                    }
                },
                {
                    data: 'ins_dttm',
                    render: function(data) {
                        return '<div style="text-align: center;">' + (data || '-') + '</div>';
                    }
                },
                {
                    data: 'updr_id',
                    render: function(data) {
                        return '<div style="text-align: center;">' + (data || '-') + '</div>';
                    }
                },
                {
                    data: 'upd_dttm',
                    render: function(data) {
                        return '<div style="text-align: center;">' + (data || '-') + '</div>';
                    }
                }
            ]
        });

        // 초기 데이터 로드
        searchItems();

        // 검색 버튼 클릭
        $('#btnSearch').on('click', function() {
            searchItems();
        });

        // 초기화 버튼 클릭
        $('#btnReset').on('click', function() {
            $('#searchKeyword').val('');
            $('#searchUseYn').val('Y');
            searchItems();
        });

        // 검색 조건에서 엔터키 입력 시 검색
        $('#searchKeyword').on('keypress', function(e) {
            if (e.which === 13) {  // Enter key
                e.preventDefault();
                searchItems();
            }
        });

        $('#searchUseYn').on('keypress', function(e) {
            if (e.which === 13) {  // Enter key
                e.preventDefault();
                searchItems();
            }
        });

        // Scroll to top 버튼
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
    });

    /**
     * 항목 코드 검색
     */
    function searchItems() {
        var keyword = $('#searchKeyword').val();
        var params = {
            inst_cd: '99',  // 고정값
            prdt_cd: '999', // 고정값
            keyword: keyword,  // 항목코드 또는 항목명 검색
            use_yn: $('#searchUseYn').val()
        };

        $.ajax({
            url: '/rf-ocr-verf/api/getOcrItemList.do',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: function(response) {
                //console.log('응답:', response);

                if (response.success) {
                    // DataTable 데이터 갱신
                    dataTable.clear();
                    if (response.data && response.data.length > 0) {
                        dataTable.rows.add(response.data);
                    }
                    dataTable.draw();
                } else {
                    alert('데이터 조회 실패: ' + (response.message || ''));
                }
            },
            error: function(xhr, status, error) {
                console.error('조회 오류:', error);
                alert('서버 연결에 실패했습니다.');
            }
        });
    }

    var selectedRow = null;
    var modalMode = 'add';  // 'add' or 'edit'

    $(document).ready(function() {
        // 행 선택 기능
        $('#itemTable tbody').on('click', 'tr', function() {
            if ($(this).hasClass('selected')) {
                $(this).removeClass('selected');
                selectedRow = null;
                $('#btnEdit, #btnDelete, #btnActivate').prop('disabled', true).hide();
            } else {
                dataTable.$('tr.selected').removeClass('selected');
                $(this).addClass('selected');
                selectedRow = dataTable.row(this).data();

                // 사용여부에 따라 버튼 표시
                if (selectedRow.use_yn === 'Y') {
                    $('#btnEdit, #btnDelete').prop('disabled', false).show();
                    $('#btnActivate').hide();
                } else {
                    $('#btnEdit, #btnDelete').hide();
                    $('#btnActivate').prop('disabled', false).show();
                }
            }
        });

        // 추가 버튼
        $('#btnAdd').on('click', function() {
            modalMode = 'add';
            $('#modalTitle').text('항목 코드 추가');
            $('#modalItemCd').prop('readonly', false);
            $('#modalItemCd').val('');
            $('#modalItemNm').val('');
            $('#itemModal').modal('show');
        });

        // 수정 버튼
        $('#btnEdit').on('click', function() {
            if (!selectedRow) {
                alert('수정할 항목을 선택해주세요.');
                return;
            }

            modalMode = 'edit';
            $('#modalTitle').text('항목 코드 수정');
            $('#modalItemCd').prop('readonly', false);
            $('#modalItemCd').val(selectedRow.item_cd);
            $('#modalItemNm').val(selectedRow.item_nm);
            $('#itemModal').modal('show');
        });

        // 삭제 버튼
        $('#btnDelete').on('click', function() {
            if (!selectedRow) {
                alert('삭제할 항목을 선택해주세요.');
                return;
            }

            if (confirm('정말 삭제하시겠습니까?')) {
                deleteItem();
            }
        });

        // 활성화 버튼
        $('#btnActivate').on('click', function() {
            if (!selectedRow) {
                alert('활성화할 항목을 선택해주세요.');
                return;
            }

            if (confirm('사용 상태로 변경하시겠습니까?')) {
                activateItem();
            }
        });

        // 모달 확인 버튼
        $('#btnModalConfirm').on('click', function() {
            var itemCd = $('#modalItemCd').val().trim();
            var itemNm = $('#modalItemNm').val().trim();

            if (!itemCd || !itemNm) {
                alert('모든 필드를 입력해주세요.');
                return;
            }

            if (modalMode === 'add') {
                addItem(itemCd, itemNm);
            } else {
                updateItem(itemCd, itemNm);
            }
        });
    });

    /**
     * 항목 추가
     */
    function addItem(itemCd, itemNm) {
        var params = {
            inst_cd: '99',
            prdt_cd: '999',
            item_cd: itemCd,
            item_nm: itemNm,
            item_odr: null,
            use_yn: 'Y'
        };

        $.ajax({
            url: '/rf-ocr-verf/api/insertOcrItem.do',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: function(response) {
                if (response.success) {
                    alert(response.message);
                    $('#itemModal').modal('hide');
                    searchItems();
                } else {
                    alert('추가 실패: ' + response.message);
                }
            },
            error: function() {
                alert('서버 연결에 실패했습니다.');
            }
        });
    }

    /**
     * 항목 수정
     */
    function updateItem(itemCd, itemNm) {
        var params = {
            inst_cd: '99',
            prdt_cd: '999',
            old_item_cd: selectedRow.item_cd,  // 기존 항목 코드
            item_cd: itemCd,                    // 새 항목 코드
            item_nm: itemNm
        };

        $.ajax({
            url: '/rf-ocr-verf/api/updateOcrItem.do',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: function(response) {
                if (response.success) {
                    alert(response.message);
                    $('#itemModal').modal('hide');
                    searchItems();
                    selectedRow = null;
                    $('#btnEdit, #btnDelete').prop('disabled', true);
                } else {
                    alert('수정 실패: ' + response.message);
                }
            },
            error: function() {
                alert('서버 연결에 실패했습니다.');
            }
        });
    }

    /**
     * 항목 삭제 (use_yn = N)
     */
    function deleteItem() {
        var params = {
            inst_cd: '99',
            prdt_cd: '999',
            item_cd: selectedRow.item_cd
        };

        $.ajax({
            url: '/rf-ocr-verf/api/deleteOcrItem.do',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: function(response) {
                if (response.success) {
                    alert(response.message);
                    searchItems();
                    selectedRow = null;
                    $('#btnEdit, #btnDelete, #btnActivate').prop('disabled', true).hide();
                } else {
                    alert('삭제 실패: ' + response.message);
                }
            },
            error: function() {
                alert('서버 연결에 실패했습니다.');
            }
        });
    }

    /**
     * 항목 활성화 (use_yn = Y)
     */
    function activateItem() {
        var params = {
            inst_cd: '99',
            prdt_cd: '999',
            item_cd: selectedRow.item_cd
        };

        $.ajax({
            url: '/rf-ocr-verf/api/activateOcrItem.do',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(params),
            success: function(response) {
                if (response.success) {
                    alert(response.message);
                    searchItems();
                    selectedRow = null;
                    $('#btnEdit, #btnDelete, #btnActivate').prop('disabled', true).hide();
                } else {
                    alert('활성화 실패: ' + response.message);
                }
            },
            error: function() {
                alert('서버 연결에 실패했습니다.');
            }
        });
    }
</script>

<style>
    #itemTable tbody tr.selected {
        background-color: #d1ecf1 !important;
    }
    #itemTable tbody tr:hover {
        cursor: pointer;
        background-color: #f8f9fa;
    }
</style>
</body>
</html>
