<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Sửa User · CodeGym</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css"></head>
<body><header class="topbar"><div class="shell brand">CodeGym <span>· User Management</span></div></header>
<main class="shell narrow content">
    <c:url value="/users" var="baseUrl" />
    <a href="${baseUrl}" class="back-link">← Quay lại danh sách</a>
    <section class="panel form-panel">
        <div class="eyebrow">CHỈNH SỬA · ID #<c:out value="${requestScope.user.id}"/></div>
        <h1>Cập nhật User</h1><p class="subtitle">Thay đổi thông tin người dùng và lưu vào MySQL.</p>
        <c:if test="${not empty requestScope.error}"><div class="alert" role="alert"><c:out value="${requestScope.error}"/></div></c:if>
        <form action="${baseUrl}?action=edit" method="post" accept-charset="UTF-8">
            <input type="hidden" name="id" value="<c:out value='${requestScope.user.id}' />">
            <label for="name">Họ và tên <strong>*</strong></label>
            <input id="name" name="name" type="text" maxlength="120" required
                   value="<c:out value='${requestScope.user.name}' />">
            <label for="email">Email <strong>*</strong></label>
            <input id="email" name="email" type="email" maxlength="220" required
                   value="<c:out value='${requestScope.user.email}' />">
            <label for="country">Quốc gia</label>
            <input id="country" name="country" type="text" maxlength="120"
                   value="<c:out value='${requestScope.user.country}' />">
            <div class="form-actions"><a class="button ghost" href="${baseUrl}">Hủy</a><button class="button primary" type="submit">Lưu thay đổi</button></div>
        </form>
    </section>
</main></body></html>
