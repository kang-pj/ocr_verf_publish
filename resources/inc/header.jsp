<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <title>${pageTitle != null ? pageTitle : 'OCR 이미지 전산'}</title>
    <!-- Bootstrap 4 CSS -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <!-- DataTables CSS -->
    <link rel="stylesheet" href="https://cdn.datatables.net/1.13.7/css/dataTables.bootstrap4.min.css">
    <link rel="stylesheet" href="https://cdn.datatables.net/select/1.7.0/css/select.bootstrap4.min.css">
    <!-- jQuery -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        body {
            font-family: 'Noto Sans KR', sans-serif;
            min-width: 1200px;
        }
        #wrapper {
            display: flex;
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
        .topbar {
            background-color: white;
            box-shadow: 0 0.15rem 1.75rem 0 rgba(58, 59, 69, 0.15);
        }
        .custom-navbar-height {
            height: 60px;
        }
        .sticky-footer {
            background-color: white;
            padding: 1rem 0;
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
        /* 뱃지 공통 스타일 */
        .badge {
            font-weight: 100;
            padding: 0.3em 0.9em;
            border-radius: 3px;
            font-size: 12px;
        }
        /* 뱃지 색상 - 파스텔 톤 */
        .badge-success {
            background-color: #d4edda !important;
            color: #28a745 !important;
        }
        .badge-warning {
            background-color: #fff3cd !important;
            color: #856404 !important;
        }
        .badge-danger {
            background-color: #f8d7da !important;
            color: #dc3545 !important;
        }
        .badge-secondary {
            background-color: #e2e3e5 !important;
            color: #6c757d !important;
        }
        /* Font Awesome 아이콘 색상 */
        .text-success {
            color: #28a745 !important;
        }
        .text-danger {
            color: #dc3545 !important;
        }
    </style>
</head>
<body id="page-top">
    <div id="wrapper">
