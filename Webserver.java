import java.io.BufferedReader; 					// IO
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.sun.net.httpserver.HttpServer; 		// http, sock
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import java.sql.Connection;						// mariadb
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Webserver {
	static String htmlfile = "html/login.html"; // define html file
	static int port = 9999;						// sock port

	// mariadb config
	static String IP = "127.0.0.1";
	static int maria_port = 3306;
	static String mariadatabase = "TESTING";
	static String maria_username = "roda";
	static String maria_password = "gmb";

	public String html(String datafile) {		// html reader
		StringBuilder htmlcontent = new StringBuilder();
		try(BufferedReader reader = new BufferedReader(new FileReader(datafile))) {
			String line;
			while ((line = reader.readLine()) != null) {
				htmlcontent.append(line).append("\n");
			}
		} catch(IOException e) {System.out.println("Error reading html file");}
		return htmlcontent.toString();
	}

	// http server handler
	public class LeHandler implements HttpHandler {
		private String html;

		public void setHtml(String html) {
			this.html = html;
		}

		public boolean checkMaria(String user, String pass) {
			try {
				// Connection
				String url = "jdbc:mariadb://" + IP + ":" + maria_port + "/" + mariadatabase;

				try (Connection connection = DriverManager.getConnection(url, maria_username, maria_password)) {

					// sql query for user
					String sql = "SELECT password FROM USERS WHERE username = ?";
					try (PreparedStatement statement = connection.prepareStatement(sql)) {
						statement.setString(1, user);
						try (ResultSet resultSet = statement.executeQuery()) {
							if (resultSet.next()) {
								String storedPassword = resultSet.getString("password");
								return storedPassword.equals(pass);
							}
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
			return false;
		}

		public int makeNewMariadb(String the_new_username, String the_new_password) {
			String url = "jdbc:mariadb://" + IP + ":" + maria_port + "/" + mariadatabase;
			try (Connection connection = DriverManager.getConnection(url, maria_username, maria_password)) {
				// check if user exists
				String sql_if_exist = "SELECT 1 FROM USERS WHERE username = ?";
				try (PreparedStatement preparedStatement = connection.prepareStatement(sql_if_exist)) {
					preparedStatement.setString(1, the_new_username);

					ResultSet resx = preparedStatement.executeQuery();

					// no rows, no user
					if (!resx.next()) {
						System.out.println("user no exist, making entry");

						String sql = "INSERT INTO USERS (username, password) VALUES (?, ?)";
						try (PreparedStatement preparedStatement1 = connection.prepareStatement(sql)) {
							preparedStatement1.setString(1, the_new_username);
							preparedStatement1.setString(2, the_new_password);
							int rowsAffected1 = preparedStatement1.executeUpdate();

							if (rowsAffected1 > 0) {
								System.out.println("user created");
							}
						} catch (SQLException e) {
							e.printStackTrace();
							System.out.println("database nuked, fix database");
						}

						return 0; // user added

					} else {
						System.out.println("user exist");
					}
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
			return 1;
		}
		
		@Override	// set your priorities, otherwise compiler complain
		public void handle(HttpExchange t) throws IOException {
			// current path
			String thepath = t.getRequestURI().getPath();
			System.out.println(thepath);

			// get ip address of visitor
			InetSocketAddress remoteAddress = t.getHttpContext().getServer().getAddress();
            String getip = remoteAddress.getAddress().getHostAddress();
			System.out.println("special agent from " + getip + " universe has arrived");

			// read request body
			BufferedReader br = new BufferedReader(new InputStreamReader(t.getRequestBody(), "utf-8"));
			String requestBody = br.lines().collect(java.util.stream.Collectors.joining());

			// in case of register, catch POST request :REGISTER
			if (thepath.equals("/register")) {
				if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
					// extract user, passwd
					String[] itemz = requestBody.split("&");
					if (itemz.length == 2) {
						String the_new_username = itemz[0].split("=")[1];
						String the_new_password = itemz[1].split("=")[1];

						// craft an sql query
						int res = makeNewMariadb(the_new_username, the_new_password);

						// ret value for success
						if (res == 0) {
							html = html("html/login.html");

						  // case if fail
						} else {
							html = html("html/register.html");
						}
					}

				}
				html = html("html/register.html");

			} else if (thepath.equals("/")) {
				html = html("html/login.html");

			  // extract user and passwd :LOGIN
			} else if (thepath.equals("/login")) {
				String[] parts = requestBody.split("&");
				if (parts.length == 2) {
					String the_username = parts[0].split("=")[1];
					String the_password = parts[1].split("=")[1];

					boolean ss = checkMaria(the_username, the_password);
					
					// is verified
					if (ss) {
						html = html("html/welcome.html");
					} else {
						html = html("html/login.html");
					}

				} else { // load login
					html = html("html/login.html");
				}
			}

			t.sendResponseHeaders(200, html.length());  // 200 ok, byte size
			OutputStream os = t.getResponseBody();		// stream for writing body response
			os.write(html.getBytes()); 					// conv bytes
			os.close();									// close output stream
		}
	}

	// Exception for http server
	public static void main(String[] args) throws IOException {

		// open a handle to manage methods
		Webserver classhandle = new Webserver();

		// open htmlfile for read
		String htmlstuff = classhandle.html(htmlfile);

		// init socket
		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

		// serve html
		LeHandler leHandler = classhandle.new LeHandler();
		leHandler.setHtml(htmlstuff);

		// exec server
		server.createContext("/", leHandler);
		server.createContext("/login", leHandler);
		server.createContext("/register", leHandler);
		server.createContext("/welcome", leHandler);
		server.setExecutor(null);
		System.out.println("The server is listening on port " + port);
		server.start();
	}
}
// vjezba 13 - simple http server
// add session
// add permission based auth
// in database fix autoincrement id value, doesn't reset after user deletion, maybe good for future but ugly
// rewrite path to relevant string, fix html
// fix html/ css
//		- print if user created, failed
