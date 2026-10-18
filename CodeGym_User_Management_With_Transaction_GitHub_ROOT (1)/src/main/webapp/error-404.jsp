<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>404 · Không tìm thấy User</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css"></head>
<body><main class="shell narrow content"><section class="panel form-panel error-panel">
<h1>404</h1><h2>Không tìm thấy User</h2><p>Người dùng bạn tìm có thể đã bị xóa hoặc không tồn tại.</p>
<c:url value="/users" var="baseUrl"/><a class="button primary" href="${baseUrl}">Quay lại danh sách</a>
</section></main></body></html>
