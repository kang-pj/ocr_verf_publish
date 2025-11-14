/**
 * 세션 만료 처리 스크립트
 * 세션이 만료되면 /Login 페이지로 리다이렉트
 */
$(document).ready(function() {
    // 컨텍스트 경로 가져오기
    function getContextPath() {
        var hostIndex = location.href.indexOf(location.host) + location.host.length;
        var contextPath = location.href.substring(hostIndex, location.href.indexOf('/', hostIndex + 1));
        return contextPath;
    }
    
    // 로그인 URL 설정
    var loginUrl = getContextPath() + '/Login';
    
    try {
        // iframe 내부인지 확인
        if (self !== top) {
            // iframe 내부라면 최상위 window를 로그인 페이지로 이동
            window.top.location.replace(loginUrl);
        } else {
            // 일반 페이지라면 현재 window를 로그인 페이지로 이동
            window.location.replace(loginUrl);
        }
    } catch (e) {
        // 크로스 도메인 오류 등의 예외 처리
        console.error('세션 만료 처리 중 오류:', e);
        window.location.replace(loginUrl);
    }
});
