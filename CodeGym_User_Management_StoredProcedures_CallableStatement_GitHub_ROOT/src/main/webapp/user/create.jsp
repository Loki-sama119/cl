<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Thêm User · CodeGym</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css"></head>
<body><header class="topbar"><div class="shell brand">CodeGym <span>· User Management</span></div></header>
<main class="shell narrow content">
    <c:url value="/users" var="baseUrl" />
    <a href="${baseUrl}" class="back-link">← Quay lại danh sách</a>
    <section class="panel form-panel">
        <div class="eyebrow">THÊM MỚI</div><h1>Thêm User</h1><p class="subtitle">Nhập thông tin để lưu vào bảng <code>demo.users</code>.</p>
        <c:if test="${not empty requestScope.error}"><div class="alert" role="alert"><c:out value="${requestScope.error}"/></div></c:if>
        <form action="${baseUrl}?action=create" method="post" accept-charset="UTF-8">
            <label for="name">Họ và tên <strong>*</strong></label>
            <input id="name" name="name" type="text" maxlength="120" required autofocus
                   value="<c:out value='${requestScope.user.name}' />" placeholder="Nguyễn Văn A">
            <label for="email">Email <strong>*</strong></label>
            <input id="email" name="email" type="email" maxlength="220" required
                   value="<c:out value='${requestScope.user.email}' />" placeholder="user@example.com">
            <label for="country">Quốc gia</label>
            <input id="country" name="country" type="text" maxlength="120"
                   value="<c:out value='${requestScope.user.country}' />" placeholder="Việt Nam">
            <div class="form-actions"><a class="button ghost" href="${baseUrl}">Hủy</a><button class="button primary" type="submit">Lưu User</button></div>
        </form>
    </section>
</main></body></html>
