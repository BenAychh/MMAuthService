/**
 * Created by benhernandez on 5/21/16.
 */

import com.mchange.v2.c3p0.ComboPooledDataSource;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
        port(8080);
        post("/create", create);
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
                object.put("status", 400);
                object.put("message", "User already exists");
                response.status(400);
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

    private static String createJWT(String email) {
        String key = "key goes here";
        return Jwts.builder().setSubject(email).signWith(SignatureAlgorithm.HS512, key).compact();
    }
}
