<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<ul class="navbar-nav bg-gradient-primary sidebar sidebar-dark accordion" id="accordionSidebar">
  <a class="sidebar-brand d-flex align-items-center justify-content-center">
    <div class="sidebar-brand-icon">
      <img class="img-profile" src="/rf-ocr-verf/resources/bootstrap/img/refine.png">
    </div>
    <div class="sidebar-brand-text mx-3 text-white">OCR 이미지 전산</div>
  </a>
  <hr class="sidebar-divider my-0">
  <li class="nav-item">
    <a class="nav-link" bgWhen="ocrRsltList" href="?pageChange=/ocrRsltList">
      <i class="fas fa-fw fa-sitemap"></i>
      <span>OCR 결과 보기</span>
    </a>
    <div id="letterCmmCtrl" class="collapse" aria-labelledby="headingPages" data-parent="#accordionSidebar">
      <div class="bg-white py-2 collapse-inner rounded">
        <a class="collapse-item" bgWhen="ocrRsltDetail" href="?pageChange=/letterTest">OCR 결과 상세보기</a>
      </div>
    </div>
  </li>

  <%
    // 개발 환경에서만 실시간 OCR 테스트 메뉴 표시
    String dbMode = System.getProperty("DBMODE");
    boolean isDevMode = !"PROD".equalsIgnoreCase(dbMode) && !"REAL".equalsIgnoreCase(dbMode);
    if (isDevMode) {
  %>
  <hr class="sidebar-divider my-0">
  <li class="nav-item">
      <a class="nav-link" bgWhen="ocrRealtimeTest"  href="?pageChange=/ocrRealtimeTest">
          <i class="fas fa-fw fa-paper-plane"></i>
          <span>실시간 OCR 테스트</span>
      </a>
  </li>
  <%
    }
  %>

  <hr class="sidebar-divider my-0">
  <li class="nav-item">
    <a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapsePages" aria-expanded="true" aria-controls="collapsePages">
      <i class="fas fa-fw fa-code"></i>
      <span>OCR정보 관리</span>
    </a>
    <div id="collapsePages" class="collapse" aria-labelledby="headingPages" data-parent="#accordionSidebar">
      <div class="bg-white py-2 collapse-inner rounded">
        <a class="collapse-item" bgWhen="ocrItemCtrl" href="?pageChange=/ocrItemCtrl">OCR코드 관리</a>
        <div class="collapse-divider"></div>
      </div>
    </div>
  </li>
</ul>
