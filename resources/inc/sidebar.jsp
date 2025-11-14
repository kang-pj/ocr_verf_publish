<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<style>
    .sidebar {
        min-height: 100vh;
        width: 250px;
        background: linear-gradient(180deg, #4e73df 10%, #224abe 100%);
    }
    .sidebar-brand {
        height: 80px;
        text-decoration: none;
        color: white !important;
    }
    .sidebar-brand-icon i {
        color: white;
    }
    .sidebar-brand-text {
        font-size: 1.1rem;
        font-weight: 600;
    }
    .sidebar .nav-item .nav-link {
        color: rgba(255, 255, 255, 0.8);
        padding: 1rem;
    }
    .sidebar .nav-item .nav-link:hover {
        color: white;
        background-color: rgba(255, 255, 255, 0.1);
    }
    .sidebar-divider {
        border-top: 1px solid rgba(255, 255, 255, 0.15);
        margin: 0;
    }
    .bg-gradient-primary {
        background: linear-gradient(180deg, #4e73df 10%, #224abe 100%);
    }
</style>

<!-- Sidebar -->
<ul class="navbar-nav bg-gradient-primary sidebar sidebar-dark accordion" id="accordionSidebar">
    <a class="sidebar-brand d-flex align-items-center justify-content-center">
        <div class="sidebar-brand-icon">
            <i class="fas fa-file-image fa-2x"></i>
        </div>
        <div class="sidebar-brand-text mx-3 text-white">OCR 이미지 전산</div>
    </a>
    <hr class="sidebar-divider my-0">
    <li class="nav-item ${currentPage == 'list' ? 'active' : ''}">
        <a class="nav-link" href="?pageChange=/ocrRsltList">
            <i class="fas fa-fw fa-list"></i>
            <span>OCR 결과 목록</span>
        </a>
    </li>
    <hr class="sidebar-divider my-0">
</ul>
