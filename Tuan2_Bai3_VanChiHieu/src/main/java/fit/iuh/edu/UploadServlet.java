package fit.iuh.edu;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Servlet implementation class UploadServlet
 */
public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UploadServlet() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
		
		// CONNECTION TO DATABASE
		DBConnection.getConnection();
		
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String firstName = request.getParameter("firstName");
		String lastName = request.getParameter("lastName");
		final int BUFFER_SIZE = 4096;
		InputStream inputStream = null;
		
		// luồng dữ liệu nhập của upload file
		// lấy thông tin tập tin upload trong form, form này gồm nhiều phần dữ liệu text
		// và file (multipart request)
		Part filePart = request.getPart("photo");
		String fileUploadName = "";
		if (filePart != null && filePart.getSubmittedFileName() != null && !filePart.getSubmittedFileName().isEmpty()) {

			fileUploadName = filePart.getSubmittedFileName();
			inputStream = filePart.getInputStream();
		}
		Connection conn = null;
		String message = null;
		String filePath = "T:/" + fileUploadName + ".jpg";
		// lưu Image Field của CSDL vào file này
		try {
			// connects to the database
			conn = DBConnection.getConnection();
			// chèn dữ liệu vào CSDL UploadFileServletDB, trường hợp này bảngcontacts (khóa
			// tự động tăng)
			String sqlInsert = "INSERT INTO contacts (first_name, last_name, photo) values (?, ?, ?)";
			PreparedStatement statement = conn.prepareStatement(sqlInsert);
			statement.setString(1, firstName);
			statement.setString(2, lastName);
			if (inputStream != null) {
				statement.setBlob(3, inputStream);
			}
			int row = statement.executeUpdate();
			// thực hiện lưu thông tin vào CSDL
			if (row > 0) {
				message = "File uploaded and saved into database";
			}
			// đọc CSDL lưu file
			String sqlSelect = "SELECT photo FROM contacts WHERE first_name=? AND last_name=?";
			statement = conn.prepareStatement(sqlSelect);
			statement.setString(1, firstName);

			statement.setString(2, lastName);
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				Blob blob = result.getBlob("photo");
				inputStream = blob.getBinaryStream();
				OutputStream outputStream = new FileOutputStream(filePath);
				int bytesRead = -1;
				byte[] buffer = new byte[BUFFER_SIZE];
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
				inputStream.close();
				outputStream.close();
			}
		} catch (

		SQLException ex) {
			message = "ERROR: " + ex.getMessage();
			ex.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			request.setAttribute("Message", message);
			getServletContext().getRequestDispatcher("/MessageServlet").forward(request, response);
		}
	}
}
