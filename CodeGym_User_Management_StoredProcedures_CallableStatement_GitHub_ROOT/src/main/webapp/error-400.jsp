<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>400 · Dữ liệu không hợp lệ</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css"></head>
<body><main class="shell narrow content"><section class="panel form-panel error-panel">
<h1>400</h1><h2>Dữ liệu không hợp lệ</h2><p><c:out value="${requestScope.message}" default="Yêu cầu không hợp lệ."/></p>
<c:url value="/users" var="baseUrl"/><a class="button primary" href="${baseUrl}">Quay lại danh sách</a>
</section></main></body></html>
