package com.codegym.controller;

import com.codegym.dao.IUserDAO;
import com.codegym.dao.UserDAO;
import com.codegym.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/** MVC controller handling CRUD, country search and name sorting. */
@WebServlet(name = "UserServlet", urlPatterns = "/users")
public class UserServlet extends HttpServlet {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private IUserDAO userDAO;

    @Override
    public void init() throws ServletException {
        try {
            userDAO = new UserDAO();
        } catch (RuntimeException ex) {
            throw new ServletException("Unable to initialize MySQL UserDAO", ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            switch (action(request)) {
                case "", "list" -> listUsers(request, response);
                case "create" -> forward(request, response, "/user/create.jsp");
                case "edit" -> showEditForm(request, response);
                case "delete" -> showExisting(request, response, "/user/delete.jsp");
                case "test-without-tran" -> testWithoutTran(request, response);
                default -> notFound(request, response);
            }
        } catch (SQLException ex) {
            throw new ServletException("Unable to read users from database", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            switch (action(request)) {
                case "create" -> insertUser(request, response);
                case "edit" -> updateUser(request, response);
                case "delete" -> deleteUser(request, response);
                default -> notFound(request, response);
            }
        } catch (SQLException ex) {
            throw new ServletException("Unable to save user changes", ex);
        }
    }

    /** Classroom demo endpoint; WARNING: this resets the Employee table. */
    private void testWithoutTran(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/plain; charset=UTF-8");
        try {
            userDAO.insertUpdateWithoutTransaction();
            response.getWriter().println("Demo ran without an SQL error; please inspect Employee.");
        } catch (SQLException ex) {
            getServletContext().log("Expected no-transaction demo SQL error", ex);
            response.getWriter().println("Expected SQL error: " + ex.getMessage());
            response.getWriter().println("Two Employee INSERTs have already committed.");
            response.getWriter().println("Run: SELECT * FROM demo.Employee;");
        }
    }

    private static String action(HttpServletRequest request) {
        String action = request.getParameter("action");
        return action == null ? "" : action.trim();
    }

    private void listUsers(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        String country = trim(request.getParameter("country"));
        String sort = trim(request.getParameter("sort"));
        if (country.length() > 120) {
            badRequest(request, response, "Quốc gia cần tìm không được dài quá 120 ký tự.");
            return;
        }
        if (!(sort.isEmpty() || "name_asc".equals(sort) || "name_desc".equals(sort))) {
            badRequest(request, response, "Tùy chọn sắp xếp không hợp lệ.");
            return;
        }
        List<User> listUser = userDAO.searchUsers(country, sort);
        request.setAttribute("listUser", listUser);
        request.setAttribute("searchCountry", country);
        request.setAttribute("sortOption", sort);
        forward(request, response, "/user/list.jsp");
    }

    /** GET edit now loads its User using the get_user_by_id stored procedure. */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Integer id = validId(request);
        if (id == null) {
            badRequest(request, response, "Mã User không hợp lệ.");
            return;
        }
        User user = userDAO.getUserById(id);
        if (user == null) {
            notFound(request, response);
            return;
        }
        request.setAttribute("user", user);
        forward(request, response, "/user/edit.jsp");
    }

    private void showExisting(HttpServletRequest request, HttpServletResponse response, String jsp)
            throws SQLException, ServletException, IOException {
        Integer id = validId(request);
        if (id == null) {
            badRequest(request, response, "Mã User không hợp lệ.");
            return;
        }
        User user = userDAO.selectUser(id);
        if (user == null) {
            notFound(request, response);
            return;
        }
        request.setAttribute("user", user);
        forward(request, response, jsp);
    }

    private void insertUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        User user = fromRequest(request, 0);
        String error = validate(user);
        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("user", user);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            forward(request, response, "/user/create.jsp");
            return;
        }
        String[] selectedPermissions = request.getParameterValues("permissions");
        int[] permissionIds = selectedPermissions == null ? new int[0] : new int[selectedPermissions.length];
        if (selectedPermissions != null) {
            for (int i = 0; i < selectedPermissions.length; i++) {
                try {
                    permissionIds[i] = Integer.parseInt(selectedPermissions[i]);
                } catch (NumberFormatException ex) {
                    badRequest(request, response, "Quyền đã chọn không hợp lệ.");
                    return;
                }
                if (permissionIds[i] < 1 || permissionIds[i] > 4) {
                    badRequest(request, response, "Quyền đã chọn không hợp lệ.");
                    return;
                }
                for (int j = 0; j < i; j++) {
                    if (permissionIds[i] == permissionIds[j]) {
                        badRequest(request, response, "Không được chọn quyền trùng nhau.");
                        return;
                    }
                }
            }
        }
        // Two writes share the same transaction; any FK error triggers rollback.
        userDAO.addUserTransaction(user, permissionIds);
        redirectToList(request, response);
    }

    private void updateUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Integer id = validId(request);
        if (id == null) {
            badRequest(request, response, "Mã User không hợp lệ.");
            return;
        }
        if (userDAO.selectUser(id) == null) {
            notFound(request, response);
            return;
        }
        User user = fromRequest(request, id);
        String error = validate(user);
        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("user", user);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            forward(request, response, "/user/edit.jsp");
            return;
        }
        if (!userDAO.updateUser(user)) {
            notFound(request, response);
            return;
        }
        redirectToList(request, response);
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Integer id = validId(request);
        if (id == null) {
            badRequest(request, response, "Mã User không hợp lệ.");
            return;
        }
        if (!userDAO.deleteUser(id)) {
            notFound(request, response);
            return;
        }
        redirectToList(request, response);
    }

    private static Integer validId(HttpServletRequest request) {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            return id > 0 ? id : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static User fromRequest(HttpServletRequest request, int id) {
        return new User(id,
            trim(request.getParameter("name")),
            trim(request.getParameter("email")),
            trim(request.getParameter("country")));
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String validate(User user) {
        if (user.getName().isEmpty() || user.getName().length() > 120) {
            return "Họ tên không được trống và tối đa 120 ký tự.";
        }
        if (user.getEmail().isEmpty() || user.getEmail().length() > 220
                || !EMAIL.matcher(user.getEmail()).matches()) {
            return "Email không hợp lệ hoặc dài quá 220 ký tự.";
        }
        if (user.getCountry().length() > 120) {
            return "Quốc gia tối đa 120 ký tự.";
        }
        return null;
    }

    private static void forward(HttpServletRequest request, HttpServletResponse response, String path)
            throws ServletException, IOException {
        request.getRequestDispatcher(path).forward(request, response);
    }

    private static void redirectToList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/users");
    }

    private static void badRequest(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        request.setAttribute("message", message);
        forward(request, response, "/error-400.jsp");
    }

    private static void notFound(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        forward(request, response, "/error-404.jsp");
    }
}
