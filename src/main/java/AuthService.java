/**
 * Created by benhernandez on 5/21/16.
 */

import com.google.gson.Gson;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;
import spark.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static spark.Spark.*;

public class AuthService {

    private static ComboPooledDataSource cpds;
    public static void main(String args[]) {
        cpds = new ComboPooledDataSource();
        String host = System.getenv("AUTHPG_PORT_5432_TCP_ADDR");
        String port = System.getenv("AUTHPG_PORT_5432_TCP_PORT");
        if (host == null) {
            host = "localhost";
        }
        if (port == null) {
            port = "5432";
        }
        cpds.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/Users");
        port(8000);
        post("/create", create);
        post("/login", login);
        put("/update", update);
        put("/deactivate", deactivate);
        put("/activate", activate);
        get("/isteacher", isTeacher);
        get("*", error);
        post("*", error);
        put("*", error);
        delete("*", error);
    }

    private static Route create = new Route() {
        public Object handle(Request request, Response response) throws Exception {
            JSONObject userInfo = new JSONObject(request.body());
            String email = userInfo.getString("email");
            String password = userInfo.getString("password");
            boolean isTeacher = userInfo.getBoolean("isTeacher");
            password = BCrypt.hashpw(password, BCrypt.gensalt(10));
            Connection connection = cpds.getConnection();
            String query = "select email from users where email = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            JSONObject object = new JSONObject();
            if (resultSet.next()) {
                object.put("status", 400);
                object.put("message", "User already exists");
                response.status(400);
            } else {
                query = "insert into users (email, password, active, is_teacher) VALUES (?, ?, ?, ?)";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, password);
                preparedStatement.setBoolean(3, true);
                preparedStatement.setBoolean(4, isTeacher);
                try {
                    preparedStatement.execute();
                } catch (Exception e) {
                    System.out.println(e);
                }
                object.put("status", 201);
                object.put("message", "User created");
                response.status(201);
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            response.type("application/json");
            return object.toString();
        }
    };

    private static Route login = new Route() {
        public Object handle(Request request, Response response) throws Exception {
            JSONObject userInfo = new JSONObject(request.body());
            String email = userInfo.getString("email");
            String password = userInfo.getString("password");
            Connection connection = cpds.getConnection();
            String query = "select email, password, is_teacher from users where email = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            JSONObject object = new JSONObject();
            if (!resultSet.next()) {
                object.put("status", 401);
                object.put("message", "Wrong email or password");
                response.status(401);
            } else {
                String hashedPassword = resultSet.getString("password");
                if (!BCrypt.checkpw(password, hashedPassword)) {
                    object.put("status", 401);
                    object.put("message", "Wrong email or password");
                    response.status(401);
                } else {
                    object.put("status", 200);
                    object.put("message", "User found and password matches");
                    JSONObject tokenize = new JSONObject();
                    tokenize.put("email", email);
                    tokenize.put("isTeacher", resultSet.getBoolean("is_teacher"));
                    object.put("tokenize", tokenize);
                    response.status(200);
                }
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            response.type("application/json");
            return object.toString();
        }
    };

    private static Route update = new Route() {
        public Object handle(Request request, Response response) throws Exception {
            JSONObject userInfo = new JSONObject(request.body());
            String email = userInfo.getString("email");
            String password = userInfo.getString("password");
            password = BCrypt.hashpw(password, BCrypt.gensalt(10));
            Connection connection = cpds.getConnection();
            String query = "select email from users where email = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            JSONObject object = new JSONObject();
            if (!resultSet.next()) {
                object.put("status", 400);
                object.put("message", "User does not exist");
                response.status(400);
                response.type("application/json");
            } else {
                query = "update users set password = ? where email = ?;";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, password);
                preparedStatement.setString(2, email);
                try {
                    preparedStatement.execute();
                } catch (Exception e) {
                    System.out.println(e);
                }
                object.put("status", 200);
                object.put("message", "User password updated");
                response.status(200);
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            return object.toString();
        }
    };

    private static Route activate = new Route() {
        public Object handle(Request request, Response response) throws Exception {
            JSONObject userInfo = new JSONObject(request.body());
            String email = userInfo.getString("email");
            Connection connection = cpds.getConnection();
            String query = "select active from users where email = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            JSONObject object = new JSONObject();
            if (!resultSet.next()) {
                object.put("status", 400);
                object.put("message", "User does not exist");
                response.status(400);
                response.type("application/json");
            } else if (resultSet.getBoolean(1) == true) {
                object.put("status", 400);
                object.put("message", "Already activated");
                response.status(400);
                response.type("application/json");
            } else {
                query = "update users set active = ? where email = ?;";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setBoolean(1, true);
                preparedStatement.setString(2, email);
                try {
                    preparedStatement.execute();
                } catch (Exception e) {
                    System.out.println(e);
                }
                object.put("status", 200);
                object.put("message", "Account activated");
                response.status(200);
                response.type("application/json");
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            response.type("application/json");
            return object.toString();
        }
    };

    private static Route isTeacher = new Route() {
        @Override
        public Object handle(Request request, Response response) throws Exception {
            Connection connection = cpds.getConnection();
            String query = "select is_teacher from Users where email=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, request.queryParams("email"));
            ResultSet resultSet = preparedStatement.executeQuery();
            JSONObject returner = new JSONObject();
            if (resultSet.next()) {
                response.status(200);
                returner.put("isTeacher", resultSet.getBoolean("is_teacher"));
            } else {
                response.status(400);
                returner.put("error", "Not found");
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            response.type("application/json");
            return returner;
        }
    };

    private static Route deactivate = new Route() {
        public Object handle(Request request, Response response) throws Exception {
            JSONObject userInfo = new JSONObject(request.body());
            String email = userInfo.getString("email");
            Connection connection = cpds.getConnection();
            String query = "select active from users where email = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            JSONObject object = new JSONObject();
            if (!resultSet.next()) {
                object.put("status", 400);
                object.put("message", "User does not exist");
                response.status(400);
                response.type("application/json");
            } else if (resultSet.getBoolean(1) == false) {
                object.put("status", 400);
                object.put("message", "Already deactivated");
                response.status(400);
                response.type("application/json");
            } else {
                query = "update users set active = ? where email = ?;";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setBoolean(1, false);
                preparedStatement.setString(2, email);
                try {
                    preparedStatement.execute();
                } catch (Exception e) {
                    System.out.println(e);
                }
                object.put("status", 200);
                object.put("message", "Account deactivated");
                response.status(200);
                response.type("application/json");
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            response.type("application/json");
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
            response.type("application/json");
            return res.toString();
        }
    };

//    private static String createJWT(String email, boolean isTeacher) {
//        JSONObject payload = new JSONObject();
//        payload.put("email", email);
//        payload.put("isTeacher", isTeacher);
//        String returner;
//        try {
//            returner = Jwts.builder().setPayload(payload.toString()).signWith(SignatureAlgorithm.HS256, "shhhhh").compact();
//        } catch (Exception e) {
//            System.out.println(e);
//            returner = e.toString();
//        }
//        return returner;
//    }
    public class JsonTransformer implements ResponseTransformer {

        private Gson gson = new Gson();

        @Override
        public String render(Object model) {
            return gson.toJson(model);
        }

    }
}
