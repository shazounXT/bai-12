package com.example.demosql.servlet;

import com.example.demosql.SQL.SQLUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.*;

public class SQLGatewayServlet extends HttpServlet {

    // Thông tin kết nối tới Aiven MySQL
    private static final String DB_URL  = System.getenv("DB_URL");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASS = System.getenv("DB_PASS");


    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String sqlStatement = request.getParameter("sqlStatement");
        String sqlResult = "";

        try {
            // load driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // kết nối tới Aiven MySQL
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            // parse câu lệnh SQL
            sqlStatement = sqlStatement.trim();

            if (sqlStatement.length() >= 6) {
                String sqlType = sqlStatement.substring(0, 6);

                if (sqlType.equalsIgnoreCase("select")) {
                    // SELECT → hiển thị bảng
                    PreparedStatement ps = connection.prepareStatement(sqlStatement);
                    ResultSet resultSet = ps.executeQuery();
                    sqlResult = SQLUtil.getHtmlTable(resultSet);
                    resultSet.close();
                    ps.close();
                } else {
                    // INSERT, UPDATE, DELETE, CREATE, DROP...
                    PreparedStatement ps = connection.prepareStatement(sqlStatement);
                    int i = ps.executeUpdate();
                    if (i == 0) {
                        sqlResult = "<p>The statement executed successfully.</p>";
                    } else {
                        sqlResult = "<p>The statement executed successfully.<br>"
                                + i + " row(s) affected.</p>";
                    }
                    ps.close();
                }
            }

            connection.close();
        } catch (ClassNotFoundException e) {
            sqlResult = "<p>Error loading the database driver:<br>"
                    + e.getMessage() + "</p>";
        } catch (SQLException e) {
            sqlResult = "<p>Error executing the SQL statement:<br>"
                    + e.getMessage() + "</p>";
        }

        HttpSession session = request.getSession();
        session.setAttribute("sqlResult", sqlResult);
        session.setAttribute("sqlStatement", sqlStatement);

        String url = "/index.jsp";
        getServletContext()
                .getRequestDispatcher(url)
                .forward(request, response);
    }
}
