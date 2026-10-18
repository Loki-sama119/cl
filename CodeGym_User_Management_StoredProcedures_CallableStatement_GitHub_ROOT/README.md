# CodeGym – Gọi MySQL Stored Procedures từ JDBC (Cập nhật ứng dụng User Management)

**Bài thực hành:** Gọi MySQL Stored Procedures từ JDBC bằng `CallableStatement`.
Dự án hoàn chỉnh kế thừa bài `Quản lý User` gồm CRUD bằng JDBC, lọc country và sắp xếp name, **bổ sung gọi Stored Procedure cho xem User trước khi sửa và thêm User mới**.

## Những thay đổi của bài này

| Thành phần | Cập nhật |
|---|---|
| `database/stored_procedures.sql` | Tạo `get_user_by_id(IN user_id INT)` và `insert_user(IN name, IN email, IN country)` đúng mã mẫu đề bài |
| `IUserDAO.java` | Bổ sung `getUserById(int id)` và `insertUserStore(User user)` |
| `UserDAO.java` | Dùng `Connection.prepareCall(...)` + `CallableStatement`, `setInt`/`setString`, `executeQuery`/`executeUpdate`; đóng tài nguyên `try-with-resources` |
| `UserServlet.java` | `GET /users?action=edit&id=...` gọi `getUserById` và `POST /users?action=create` gọi `insertUserStore` |
| View JSP | Giữ nguyên giao diện CRUD, Country Search và Name Sort từ bài trước |

**Quan trọng:** `get_user_by_id` trong đề chỉ `SELECT name, email, country` (không có `id`), nên DAO dùng `id` được truyền vào để tạo `new User(id, ...)`. Procedure `insert_user` trong đề có tham số `VARCHAR(50)`; form thêm mới từ chối name/email > 50 ký tự để tránh lỗi/truncate dữ liệu. Các chức năng sửa và xóa vẫn dùng `PreparedStatement` như bài cũ; chỉ hai thao tác được yêu cầu chuyển sang Stored Procedure.

## 1. Khởi tạo MySQL trước khi chạy webapp

Chạy lần lượt **2 file SQL** bằng MySQL Workbench hoặc MySQL Command Line Client:

```sql
SOURCE C:/duong-dan-repo/database/demo.sql;
SOURCE C:/duong-dan-repo/database/stored_procedures.sql;
USE demo;
SHOW PROCEDURE STATUS WHERE Db = 'demo';
CALL get_user_by_id(1);
```

Trong Workbench, mở từng file bằng File > Open SQL Script rồi chạy toàn bộ. `DELIMITER` là cú pháp hỗ trợ bởi MySQL client/Workbench. File stored_procedures.sql dùng `DROP PROCEDURE IF EXISTS` để có thể chạy lại mà không bị lỗi "already exists" (lưu ý: DROP/CREATE thủ tục, không xóa bảng users).

## 2. Cấu hình kết nối

Dùng Java 17+, MySQL 8.x, Maven và Tomcat 10.1+ (Jakarta Servlet). Ứng dụng lấy cấu hình JDBC từ biến môi trường **của tiến trình Tomcat**:

```powershell
$env:JDBC_URL = 'jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=UTF-8'
$env:JDBC_USERNAME = 'root'
$env:JDBC_PASSWORD = 'MAT_KHAU_MYSQL_CUA_BAN'
```

Nếu Tomcat được chạy bằng Windows Service, phải thiết lập biến môi trường cho service; lệnh trên chỉ tác dụng cho tiến trình Tomcat khởi chạy từ **chính PowerShell đó**. Không commit mật khẩu lên GitHub. File `.env.example` chỉ là mẫu, chương trình không tự đọc file này.

## 3. Biên dịch + triển khai Tomcat

Tại **thư mục gốc chứa pom.xml**, chạy:

```powershell
mvn clean package
```

Đảm bảo `BUILD SUCCESS`; sau đó copy `target/user-management.war` vào thư mục `webapps/` của Tomcat 10.1 và restart/redeploy, mở:

**http://localhost:8080/user-management/users**

## 4. Kiểm thử nhanh theo tiêu chí CodeGym

1. Vào danh sách Users: thấy Minh/Kante trong MySQL.
2. Thêm User mới: GET `/users?action=create`, nhập tên/email/country <= 50 ký tự, nhấn Lưu. Servlet phải gọi `userDAO.insertUserStore()` → DAO gọi `CALL insert_user(?,?,?)` → dữ liệu xuất hiện trong MySQL.
3. Edit User: GET `/users?action=edit&id=1`: Servlet gọi `userDAO.getUserById(1)` → DAO gọi `CALL get_user_by_id(?)` → form hiển thị tên, email, country đúng.
4. Sửa User qua POST edit và xóa User qua POST delete: giữ hoạt động như bài trước.
5. Tìm `country=Viet`; sắp xếp `sort=name_asc`/`name_desc`: giữ hoạt động như bài trước.
6. Kiểm tra MySQL bằng `SELECT id, name, email, country FROM demo.users ORDER BY id;`.
7. Với `id` không tồn tại, truy cập form sửa → báo 404; `id=abc` → báo 400.

**Lưu ý:** bài không có login, role hoặc CSRF protection; chỉ chạy local cho học tập, không công khai với dữ liệu thật.

## 5. Gửi GitHub và tránh lỗi chấm thiếu file

ZIP đóng gói **không có thư mục bao ngoài**. Khi tạo repo GitHub, upload toàn bộ **nội dung giải nén** lên **gốc repository** để các mục dưới đây nằm trực tiếp ở cấp cao nhất:

```text
pom.xml
README.md
database/demo.sql
database/stored_procedures.sql
src/main/java/com/codegym/model/User.java
src/main/java/com/codegym/dao/IUserDAO.java
src/main/java/com/codegym/dao/UserDAO.java
src/main/java/com/codegym/controller/UserServlet.java
src/main/webapp/WEB-INF/web.xml
src/main/webapp/user/list.jsp
src/main/webapp/user/create.jsp
src/main/webapp/user/edit.jsp
src/main/webapp/user/delete.jsp
```

**Nếu dùng repository bài User cũ:** chép đè các file thay đổi vào đúng thư mục gốc hiện có (không chép cả thư mục dự án vào repo), thêm `database/stored_procedures.sql`, `git add -A`, `git commit -m "Add JDBC stored procedures"`, `git push origin HEAD`.

Có `kiem-tra-cau-truc.ps1` để kiểm tra các file quan trọng từ PowerShell. GitHub Actions `.github/workflows/maven-build.yml` sẽ chạy `mvn clean package` sau khi push, nếu repository bật Actions. Để nộp bài, **dán link repository công khai** chứ không dán link ZIP.

## 6. Ghi chú về kiểm thử

Bạn cần thực thi SQL và chạy trên MySQL + Tomcat thật để xác minh giao diện/kết nối môi trường của bạn. Script `tests/UserModelSmokeTest.java` kiểm tra Model thuần Java. Code không đòi nhập mật khẩu DB vào repository.
