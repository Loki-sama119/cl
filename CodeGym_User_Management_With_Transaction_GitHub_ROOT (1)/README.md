# CodeGym – Thực thi SQL có sử dụng Transaction

Dự án Maven WAR JSP/Servlet/JDBC/MySQL, kế thừa bài Quản lý User: CRUD, lọc country, sắp xếp name, stored procedures, gán quyền dùng transaction và bài demo **không dùng transaction**.

## Bài mới: `insertUpdateUseTransaction()`

- `IUserDAO` khai báo `insertUpdateUseTransaction()` và biến thể `insertUpdateUseTransaction(boolean failUpdate)`.
- `UserDAO` đặt auto-commit `false`, INSERT 2 nhân viên Quynh/Ngan cùng một `Connection`, UPDATE Quynh, `commit()` nếu thành công và **`rollback()` tường minh** nếu thất bại.
- Mặc định UPDATE cố tình lỗi vì chỉ `setBigDecimal(2, ...)` trong khi placeholder số 1 chưa được gán. Có thể thử thành công bằng `mode=success` (gán placeholder 1 đúng) mà không phải sửa code/build lại.
- `UserServlet` thêm GET `?action=test-use-tran`. Luồng demo cũ `?action=test-without-tran` vẫn có.

## Chạy và quan sát hai trường hợp

1. MySQL 8: chạy `database/demo.sql` nếu mới tạo database. Các file `database/stored_procedures.sql`, `database/transaction_permissions.sql`, `database/employee_with_transaction.sql` hỗ trợ những bài tập tương ứng. Hãy xem README cũ dưới đây để biết chi tiết.
2. Đặt `JDBC_URL`, `JDBC_USERNAME`, `JDBC_PASSWORD` theo `.env.example` cho tiến trình Tomcat.
3. Tại thư mục chứa `pom.xml`, chạy `mvn clean package`. Copy `target/user-management.war` vào `webapps` Tomcat 10.1 và khởi động/redeploy.
4. Thử **rollback**: `http://localhost:8080/user-management/users?action=test-use-tran` → SQL UPDATE lỗi, Servlet trả thông báo rollback; truy vấn `SELECT * FROM demo.Employee;` → bảng trống.
5. Thử **commit**: `http://localhost:8080/user-management/users?action=test-use-tran&mode=success` → Quynh có salary=999.99, Ngan=20.00.

**CẢNH BÁO:** Endpoint demo chủ động `DROP TABLE IF EXISTS Employee` và `CREATE TABLE Employee` ở MỖI lần truy cập. Chỉ dùng với MySQL database `demo` dành cho học tập, không chạy trên dữ liệu thật và không công khai endpoint. MySQL DDL gây implicit commit nên hai câu lệnh DROP/CREATE được đặt **ngoài transaction**; tính toàn vẹn All-or-Nothing được kiểm tra cho hai INSERT và một UPDATE. Việc đóng Connection không được coi là thay thế cho `rollback()` rõ ràng khi lỗi.

## Nộp GitHub

Giải nén ZIP và upload các mục **bên trong** lên gốc repository: `pom.xml`, `src/`, `database/`, `README.md`. Không để toàn dự án trong thư mục con.

---

# CodeGym - JDBC Transaction (User + Permission)

Bài thực hành kế thừa Quản lý User (CRUD, tìm kiếm theo country, sắp xếp name, CallableStatement tìm User theo ID), bổ sung giao dịch JDBC cho việc thêm User và gán quyền.

## Chạy trên máy

1. MySQL: chạy lần lượt `database/demo.sql`, `database/stored_procedures.sql`, `database/transaction_permissions.sql`.
2. Đặt biến môi trường `JDBC_URL`, `JDBC_USERNAME`, `JDBC_PASSWORD` theo MySQL của bạn (xem `.env.example`).
3. Tại thư mục gốc chứa `pom.xml`, chạy `mvn clean package`.
4. Deploy `target/user-management.war` vào Tomcat 10.1, mở `http://localhost:8080/user-management/users`.
5. Mở **Thêm User**, điền thông tin, chọn quyền Add và View, bấm **Lưu User và quyền**.
6. Kiểm tra trong MySQL: `SELECT * FROM users ORDER BY id DESC LIMIT 1;` và `SELECT * FROM user_permission;`.

## Transaction

`UserDAO.addUserTransaction()` dùng duy nhất một `Connection` với `setAutoCommit(false)`, `INSERT users` + `RETURN_GENERATED_KEYS`, `INSERT user_permission`, `commit()` nếu tất cả thành công, `rollback()` rồi ném lại SQLException nếu lỗi. Các tài nguyên đóng bằng try-with-resources. Có thể thử rollback bằng cách gọi DAO với permission ID không tồn tại (ví dụ 999), chỉ thử trên database test.

**Lưu ý:** Chức năng **thêm User** được chuyển sang Transaction nên không còn gọi `insert_user` Stored Procedure trong luồng tạo mới. `get_user_by_id` vẫn được dùng cho màn hình Edit, và phương thức `insertUserStore()` từ bài trước vẫn còn để tham khảo.

## Nộp GitHub

Giải nén ZIP và upload **nội dung bên trong** vào gốc repository, không upload thư mục chứa ZIP. Trên trang chính phải thấy ngay `pom.xml`, `src/`, `database/` và `README.md`.

## Mã nguồn các bài trước

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


## Bài thực hành mới: Thực thi SQL KHÔNG sử dụng Transaction

Cập nhật `IUserDAO`, `UserDAO`, `UserServlet` với `insertUpdateWithoutTransaction()` và `action=test-without-tran`. Bài thực hành tạo lại **duy nhất** bảng `Employee`, chèn hai người `Quynh` (10.00), `Ngan` (20.00), sau đó cố ý gây lỗi SQL do thiếu tham số số 1 ở câu UPDATE. Vì auto-commit bật, 2 INSERT đã được lưu ngay cả khi UPDATE lỗi.

**CẢNH BÁO:** Chức năng demo **DROP TABLE IF EXISTS Employee** khi gọi URL. Chỉ chạy trên database thử nghiệm `demo`, KHÔNG chạy trên dữ liệu thật. Endpoint demo cố ý có thể kích hoạt qua GET theo bài học, không triển khai công khai.

1. Chạy `database/demo.sql`, các migration cũ và `database/employee_without_transaction.sql` trong MySQL thử nghiệm nếu muốn tạo trước bảng Employee.
2. Cấu hình `JDBC_URL`, `JDBC_USERNAME`, `JDBC_PASSWORD`.
3. Tại thư mục gốc (nơi có `pom.xml`), chạy `mvn clean package` rồi triển khai `target/user-management.war` trên Tomcat 10.1.
4. Mở `http://localhost:8080/user-management/users?action=test-without-tran`. Trang sẽ báo lỗi SQL được bắt lại để bạn kiểm tra.
5. Trong MySQL: `USE demo; SELECT id,name,salary FROM Employee;` phải thấy Quynh/10.00 và Ngan/20.00, **không** có UPDATE thành 999.99.

Giữ nguyên các tính năng cũ: CRUD, country search, name sorting, CallableStatement stored procedures và addUserTransaction.

**Nộp GitHub:** Giải nén, upload **nội dung bên trong ZIP** lên thư mục gốc repository; xác nhận `pom.xml`, `src/main/webapp/WEB-INF/web.xml`, `database/` nằm đúng cấp.
