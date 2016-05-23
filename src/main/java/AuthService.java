/**
 * Created by benhernandez on 5/21/16.
 */

import com.mchange.v2.c3p0.ComboPooledDataSource;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import spark.Request;
import spark.Response;
import spark.Route;

import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static spark.Spark.*;

public class AuthService {

    private static ComboPooledDataSource cpds;
    public static void main(String args[]) {
        cpds = new ComboPooledDataSource();
        cpds.setJdbcUrl("jdbc:postgresql://localhost/Users");
        port(8000);
        post("/create", create);
        post("/login", login);
        put("/update", update);
        put("/deactivate", deactivate);
        put("/activate", activate);
        get("*", error);
        post("*", error);
        put("*", error);
        delete("*", error);
    }

    private static Route create = new Route() {
        public Object handle(Request request, Response response) throws Exception {
            String[] userDataArray = request.body().split("&");
            String[] emailPair = userDataArray[0].split("=");
            String[] passwordPair = userDataArray[1].split("=");
            String email = URLDecoder.decode(emailPair[1], "UTF-8");
            String password = URLDecoder.decode(passwordPair[1], "UTF-8");
            password = BCrypt.hashpw(password, BCrypt.gensalt(10));
            Connection connection = cpds.getConnection();
            String query = "select email from users where email = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            JSONObject object = new JSONObject();
            if (resultSet.next()) {
                object.put("status", 409);
                object.put("message", "User already exists");
                response.status(409);
                response.type("application/json");
            } else {
                query = "insert into users VALUES (?, ?, ?)";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, password);
                preparedStatement.setBoolean(3, true);
                preparedStatement.execute();
                object.put("status", 201);
                object.put("message", "User created");
                object.put("token", createJWT(email));
                response.status(201);
                response.type("application/json");
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            return object.toString();
        }
    };

    private static Route login = new Route() {
        public Object handle(Request request, Response response) throws Exception {
            String[] userDataArray = request.body().split("&");
            String[] emailPair = userDataArray[0].split("=");
            String[] passwordPair = userDataArray[1].split("=");
            String email = URLDecoder.decode(emailPair[1], "UTF-8");
            String password = URLDecoder.decode(passwordPair[1], "UTF-8");
            Connection connection = cpds.getConnection();
            String query = "select email, password from users where email = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            JSONObject object = new JSONObject();
            if (!resultSet.next()) {
                object.put("status", 403);
                object.put("message", "Bad username or password");
                response.status(403);
                response.type("application/json");
            } else {
                String hashedPassword = resultSet.getString("password");
                if (!BCrypt.checkpw(password, hashedPassword)) {
                    object.put("status", 403);
                    object.put("message", "Bad username or password");
                    response.status(403);
                    response.type("application/json");
                } else {
                    object.put("status", 202);
                    object.put("message", "User found and password matches");
                    object.put("token", createJWT(email));
                    response.status(202);
                    response.type("application/json");
                }
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            return object.toString();
        }
    };

    private static Route update = new Route() {
        public Object handle(Request request, Response response) throws Exception {
            String[] userDataArray = request.body().split("&");
            String[] emailPair = userDataArray[0].split("=");
            String[] passwordPair = userDataArray[1].split("=");
            String email = URLDecoder.decode(emailPair[1], "UTF-8");
            String password = URLDecoder.decode(passwordPair[1], "UTF-8");
            password = BCrypt.hashpw(password, BCrypt.gensalt(10));
            Connection connection = cpds.getConnection();
            String query = "select email from users where email = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            JSONObject object = new JSONObject();
            if (!resultSet.next()) {
                object.put("status", 403);
                object.put("message", "User does not exist");
                response.status(403);
                response.type("application/json");
            } else {
                query = "update users set password = ? where email = ?;";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, password);
                preparedStatement.setString(2, email);
                preparedStatement.execute();
                object.put("status", 202);
                object.put("message", "User password updated");
                response.status(202);
                response.type("application/json");
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            return object.toString();
        }
    };

    private static Route activate = new Route() {
        public Object handle(Request request, Response response) throws Exception {
            String[] userData = request.body().split("=");
            String email = URLDecoder.decode(userData[1], "UTF-8");
            Connection connection = cpds.getConnection();
            String query = "select active from users where email = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            JSONObject object = new JSONObject();
            if (!resultSet.next()) {
                object.put("status", 403);
                object.put("message", "User does not exist");
                response.status(403);
                response.type("application/json");
            } else if (resultSet.getBoolean(1) == true) {
                object.put("status", 409);
                object.put("message", "User is already active");
                response.status(409);
                response.type("application/json");
            } else {
                query = "update users set active = ? where email = ?;";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setBoolean(1, true);
                preparedStatement.setString(2, email);
                preparedStatement.execute();
                object.put("status", 202);
                object.put("message", "User's active status set to true");
                response.status(202);
                response.type("application/json");
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            return object.toString();
        }
    };

    private static Route deactivate = new Route() {
        public Object handle(Request request, Response response) throws Exception {
            String[] userData = request.body().split("=");
            String email = URLDecoder.decode(userData[1], "UTF-8");
            Connection connection = cpds.getConnection();
            String query = "select active from users where email = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            JSONObject object = new JSONObject();
            if (!resultSet.next()) {
                object.put("status", 403);
                object.put("message", "User does not exist");
                response.status(403);
                response.type("application/json");
            } else if (resultSet.getBoolean(1) == false) {
                object.put("status", 409);
                object.put("message", "User is already inactive");
                response.status(409);
                response.type("application/json");
            } else {
                query = "update users set active = ? where email = ?;";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setBoolean(1, false);
                preparedStatement.setString(2, email);
                preparedStatement.execute();
                object.put("status", 202);
                object.put("message", "User's active status set to false");
                response.status(202);
                response.type("application/json");
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            return object.toString();
        }
    };

    private static Route error = new Route() {
        @Override
        public Object handle(Request request, Response response) throws Exception {
            JSONObject res = new JSONObject();
            res.put("message", "error");
            res.put("status", 404);
            res.put("requested resource", request.pathInfo());
            res.put("requested method", request.requestMethod());
            response.status(404);
            return res.toString();
        }
    };

    private static String createJWT(String email) {
        String key = "key goes here";
        return Jwts.builder().setSubject(email).signWith(SignatureAlgorithm.HS512, key).compact();
    }
}
