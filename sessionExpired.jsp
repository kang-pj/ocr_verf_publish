<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" session="false"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>세션만료처리</title>
    <script type="text/javascript" src="${pageContext.request.contextPath}/resources/inc/js/jquery-1.8.3.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/resources/inc/js/sessionExpired.js"></script>
</head>
<body>
    <div style="text-align: center; padding: 50px;">
        <p>세션이 만료되었습니다. 로그인 페이지로 이동합니다...</p>
    </div>
</body>
</html>
