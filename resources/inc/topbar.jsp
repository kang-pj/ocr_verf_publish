<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- Topbar -->
<nav class="navbar navbar-expand navbar-light topbar mb-3 static-top custom-navbar-height" id="topNavbar">
    <button id="sidebarToggleTop" class="btn btn-link d-md-none rounded-circle mr-3">
        <i class="fa fa-bars"></i>
    </button>
    <div class="collapse navbar-collapse">
        <ul class="navbar-nav">
            <i class="nav-item fas fa-fw fa-home"></i>
        </ul>
        <ul class="navbar-nav" id="menuNav"></ul>
    </div>
    <hr />
    <ul class="navbar-nav ml-auto">
        <div class="topbar-divider d-none d-sm-block"></div>
        <li class="nav-item dropdown no-arrow">
            <a class="nav-link dropdown-toggle" href="#" id="userDropdown" role="button" data-toggle="modal" data-target="#logoutModal">
                <span class="mr-2 d-none d-lg-inline text-gray-600"><strong>강진우</strong></span>
                <span class="mr-2 d-none d-lg-inline text-gray-600"><i class="fas fa-sign-out-alt text-gray-600"></i></span>
            </a>
        </li>
    </ul>
</nav>

<!-- Logout Modal -->
<div class="modal fade" id="logoutModal" tabindex="-1" role="dialog" aria-labelledby="logoutModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h6 class="modal-title" id="logoutModalLabel">로그아웃 하시겠습니까?</h6>
                <button class="close" type="button" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">×</span>
                </button>
            </div>
            <div class="modal-footer">
                <a class="btn btn-primary btn-sm" href="logout">확인</a>
                <button class="btn btn-secondary btn-sm" type="button" data-dismiss="modal">취소</button>
            </div>
        </div>
    </div>
</div>
