<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Xóa User · CodeGym</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css"></head>
<body><header class="topbar"><div class="shell brand">CodeGym <span>· User Management</span></div></header>
<main class="shell narrow content">
    <c:url value="/users" var="baseUrl" />
    <a href="${baseUrl}" class="back-link">← Quay lại danh sách</a>
    <section class="panel form-panel">
        <div class="eyebrow danger-text">XÁC NHẬN THAO TÁC</div><h1>Xóa User?</h1>
        <p class="subtitle">Thao tác sẽ xóa bản ghi khỏi MySQL và không thể hoàn tác.</p>
        <div class="person-box">
            <div><span>ID</span><strong>#<c:out value="${requestScope.user.id}"/></strong></div>
            <div><span>Họ tên</span><strong><c:out value="${requestScope.user.name}"/></strong></div>
            <div><span>Email</span><strong><c:out value="${requestScope.user.email}"/></strong></div>
            <div><span>Quốc gia</span><strong><c:out value="${requestScope.user.country}" default="—"/></strong></div>
        </div>
        <form action="${baseUrl}?action=delete" method="post">
            <input type="hidden" name="id" value="<c:out value='${requestScope.user.id}' />">
            <div class="form-actions"><a class="button ghost" href="${baseUrl}">Hủy bỏ</a>
                <button class="button danger" type="submit">Xác nhận xóa</button></div>
        </form>
    </section>
</main></body></html>
