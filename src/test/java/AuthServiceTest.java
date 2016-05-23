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
    /**
     * Starts our AuthService to test against.
     */
    @BeforeClass
    public static void beforeAll() {
        String[] args = {};
        AuthService.main(args);
    }

    /**
     * Wipes out the user database (start clean for each test).
     * @throws SQLException if we have trouble hitting database.
     */
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
    public void test404Get() throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .get("http://localhost:8080/doesnotexist");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = new JSONObject(response.getErrorBody().toString());
        JSONObject expected = new JSONObject();
        expected.put("message", "error");
        expected.put("status", 404);
        expected.put("requested resource", "/doesnotexist");
        expected.put("requested method", "GET");
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void test404Post() throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8080/doesnotexist");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = new JSONObject(response.getErrorBody().toString());
        JSONObject expected = new JSONObject();
        expected.put("message", "error");
        expected.put("status", 404);
        expected.put("requested resource", "/doesnotexist");
        expected.put("requested method", "POST");
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void test404Put() throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .put("http://localhost:8080/doesnotexist");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = new JSONObject(response.getErrorBody().toString());
        JSONObject expected = new JSONObject();
        expected.put("message", "error");
        expected.put("status", 404);
        expected.put("requested resource", "/doesnotexist");
        expected.put("requested method", "PUT");
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void test404Delete() throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .delete("http://localhost:8080/doesnotexist");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = new JSONObject(response.getErrorBody().toString());
        JSONObject expected = new JSONObject();
        expected.put("message", "error");
        expected.put("status", 404);
        expected.put("requested resource", "/doesnotexist");
        expected.put("requested method", "DELETE");
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void testCreateValidUser()  throws Exception {
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
    public void testCreatedAlreadyExistingUser() throws Exception {
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
                    .post("http://localhost:8080/create")
                    .param("email", "test@test.com")
                    .param("password", "password");
            response = request
                    .asJsonObject();
            result = new JSONObject(response.getErrorBody().toString());
            JSONObject expected = new JSONObject();
            expected.put("message", "User already exists");
            expected.put("status", 409);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(409, response.getStatusCode());
        }
    }

    @Test
    public void testReadUserExists() throws Exception {
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
            assertEquals(202, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }

    }

    @Test
    public void testReadUserDoesNotExist() throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8080/read")
                .param("email", "doesnotexist@something.com")
                .param("password", "password");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = new JSONObject(response.getErrorBody().toString());
        JSONObject expected = new JSONObject();
        expected.put("message", "Bad username or password");
        expected.put("status", 403);
        JSONAssert.assertEquals(expected, result, true);
        assertEquals(401, response.getStatusCode());
    }

    @Test
    public void testReadBadPassword() throws Exception {
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
                    .param("password", "wrongPassword");
            response = request
                    .asJsonObject();
            result = new JSONObject(response.getErrorBody().toString());
            JSONObject expected = new JSONObject();
            expected.put("message", "Bad username or password");
            expected.put("status", 401);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(403, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }
    }

    @Test
    public void existingUserUpdatesPassword() throws Exception {
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
                    .put("http://localhost:8080/update")
                    .param("email", "test@test.com")
                    .param("password", "newpassword");
            response = request
                    .asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "User does not exist");
            expected.put("status", 403);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(204, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }
    }

    @Test
    public void updateNonExistingUserReturnsErrorMessage() throws Exception {
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
                    .put("http://localhost:8080/update")
                    .param("email", "doesnotexist@something.com")
                    .param("password", "newpassword");
            response = request
                    .asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "User does not exist");
            expected.put("status", 403);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(403, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }
    }

    @Test
    public void deleteNonExistingUserReturnsErrorMessage() throws Exception {
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
                    .delete("http://localhost:8080/delete")
                    .param("email", "doesnotexist@something.com");
            response = request
                    .asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "User does not exist");
            expected.put("status", 403);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(403, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }
    }

    @Test
    public void deleteExistingUserSetsActiveStatusToFalse() throws Exception {
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
                    .delete("http://localhost:8080/delete")
                    .param("email", "test@test.com");
            response = request
                    .asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "User's active status set to false");
            expected.put("status", 202);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(202, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }
    }

    @Test
    public void deleteInactiveUserReturnsError() throws Exception {
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
                    .delete("http://localhost:8080/delete")
                    .param("email", "test@test.com");
            response = request
                    .asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            request = webb
                    .delete("http://localhost:8080/delete")
                    .param("email", "test@test.com");
            response = request.asJsonObject();
            result = response.getBody();
            JSONObject expected = new JSONObject();
            expected.put("message", "User is already inactive");
            expected.put("status", 409);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(409, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }
    }
}
