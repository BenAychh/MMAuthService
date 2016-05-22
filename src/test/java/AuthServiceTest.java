import com.goebl.david.Request;
import com.goebl.david.Response;
import com.goebl.david.Webb;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.sql.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * Created by benhernandez on 5/21/16.
 */
public class AuthServiceTest {
    @Before
    public void beforeEach() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost/Users");
        Statement statement = connection.createStatement();
        String query = "delete from users";
        statement.execute(query);
        statement.close();
        connection.close();
    }

    @Test
    public void testCreate()  throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8080/create")
                .param("email", "test@test.com")
                .param("password", "password");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result == null) {
            result = new JSONObject(response.getErrorBody().toString());
        }
        JSONObject expected = new JSONObject();
        expected.put("message", "User created");
        expected.put("status", 201);
        expected.put("token", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIn0.egbaJ7yWUvC4mU_C7LNJi24cPNpfx3rlr7woWn9pqsGX6LrGCK2Rf2LaD2cFiJ4AWC93QDMChuCmUM4YtDjzAw");
        JSONAssert.assertEquals(expected, result, true);
        assertEquals(201, response.getStatusCode());
    }

    @Test
    public void testRead() throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8080/create")
                .param("email", "test@test.com")
                .param("password", "password");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            request = webb
                    .post("http://localhost:8080/read")
                    .param("email", "test@test.com")
                    .param("password", "password");
            response = request
                    .asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "Logged in");
            expected.put("status", 200);
            expected.put("token", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIn0.egbaJ7yWUvC4mU_C7LNJi24cPNpfx3rlr7woWn9pqsGX6LrGCK2Rf2LaD2cFiJ4AWC93QDMChuCmUM4YtDjzAw");
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(200, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }

    }
}