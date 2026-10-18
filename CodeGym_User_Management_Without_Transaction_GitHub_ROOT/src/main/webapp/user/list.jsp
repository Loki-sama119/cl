<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Danh sách User · CodeGym</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css">
</head>
<body>
<header class="topbar"><div class="shell brand">CodeGym <span>· User Management</span></div></header>
<main class="shell content">
    <c:url value="/users" var="baseUrl" />
    <div class="heading-row">
        <div>
            <div class="eyebrow">JSP · SERVLET · JDBC · MYSQL</div>
            <h1>Quản lý người dùng</h1>
            <p class="subtitle">Tìm kiếm theo quốc gia, sắp xếp theo tên và quản lý dữ liệu trong MySQL.</p>
        </div>
        <a class="button primary" href="${baseUrl}?action=create">+ Thêm User</a>
    </div>

    <section class="panel filter-panel" aria-label="Bộ lọc danh sách User">
        <form class="filters" action="${baseUrl}" method="get" role="search">
            <div class="filter-field filter-search">
                <label for="country">Tìm theo country (quốc gia)</label>
                <input id="country" name="country" type="search" maxlength="120"
                       placeholder="Ví dụ: Viet Nam, Kenia..."
                       value="<c:out value='${requestScope.searchCountry}' />">
            </div>
            <div class="filter-field filter-sort">
                <label for="sort">Sắp xếp theo name (tên)</label>
                <select id="sort" name="sort">
                    <option value="" <c:if test="${empty requestScope.sortOption}">selected</c:if>>Mặc định · ID tăng dần</option>
                    <option value="name_asc" <c:if test="${requestScope.sortOption eq 'name_asc'}">selected</c:if>>Tên: A → Z</option>
                    <option value="name_desc" <c:if test="${requestScope.sortOption eq 'name_desc'}">selected</c:if>>Tên: Z → A</option>
                </select>
            </div>
            <div class="filter-actions">
                <button class="button primary" type="submit">Tìm kiếm / Sắp xếp</button>
                <a class="button ghost" href="${baseUrl}">Xóa bộ lọc</a>
            </div>
        </form>
    </section>

    <section class="panel">
        <div class="panel-header">
            <h2>Danh sách User</h2>
            <span class="pill">${requestScope.listUser.size()} bản ghi</span>
        </div>
        <div class="table-scroll"><table>
            <thead><tr><th>ID</th><th>Họ và tên</th><th>Email</th><th>Quốc gia</th><th class="actions">Thao tác</th></tr></thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty requestScope.listUser}">
                        <tr><td class="empty-state" colspan="5">
                            <c:choose>
                                <c:when test="${not empty requestScope.searchCountry}">Không tìm thấy User có country phù hợp. Hãy thử từ khóa khác.</c:when>
                                <c:otherwise>Chưa có người dùng. Hãy thêm User đầu tiên.</c:otherwise>
                            </c:choose>
                        </td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="user" items="${requestScope.listUser}">
                            <c:url value="/users" var="editUrl">
                                <c:param name="action" value="edit" />
                                <c:param name="id" value="${user.id}" />
                            </c:url>
                            <c:url value="/users" var="deleteUrl">
                                <c:param name="action" value="delete" />
                                <c:param name="id" value="${user.id}" />
                            </c:url>
                            <tr>
                                <td class="muted">#<c:out value="${user.id}" /></td>
                                <td class="cell-strong"><c:out value="${user.name}" /></td>
                                <td><c:out value="${user.email}" /></td>
                                <td><span class="country"><c:out value="${user.country}" default="—" /></span></td>
                                <td class="actions">
                                    <a class="link" href="${editUrl}">Sửa</a>
                                    <a class="link danger-text" href="${deleteUrl}">Xóa</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table></div>
    </section>
    <p class="footnote">Lọc và sắp xếp thực hiện trên MySQL thông qua PreparedStatement. Dữ liệu: UserServlet → UserDAO → MySQL.</p>
</main>
</body>
</html>
