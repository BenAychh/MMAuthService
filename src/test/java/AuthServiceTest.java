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
    public static void beforeAll() throws SQLException{
        String host = System.getenv("PG_PORT_5432_TCP_ADDR");
        String port = System.getenv("PG_PORT_5432_TCP_PORT");
        if (host == null) {
            host = "localhost";
        }
        if (port == null) {
            port = "5432";
        }
        Connection connection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/Users?user=postgres");
        Statement statement = connection.createStatement();
        String query = "CREATE TABLE public.users\n" +
                "(\n" +
                "  email character varying NOT NULL,\n" +
                "  password character varying,\n" +
                "  active boolean DEFAULT false,\n" +
                "  CONSTRAINT users_pkey PRIMARY KEY (email)\n" +
                ")";
        statement.execute(query);
        statement.close();
        connection.close();
        String[] args = {};
        AuthService.main(args);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wipes out the user database (start clean for each test).
     * @throws SQLException if we have trouble hitting database.
     */
    @Before
    public void beforeEach() throws SQLException {
        String host = System.getenv("PG_PORT_5432_TCP_ADDR");
        String port = System.getenv("PG_PORT_5432_TCP_PORT");
        if (host == null) {
            host = "localhost";
        }
        if (port == null) {
            port = "5432";
        }
        Connection connection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/Users?user=postgres");
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
                .get("http://localhost:8000/doesnotexist");
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
                .post("http://localhost:8000/doesnotexist");
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
                .put("http://localhost:8000/doesnotexist");
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
                .delete("http://localhost:8000/doesnotexist");
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
    public void testCreateValidUser() throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
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
                .post("http://localhost:8000/create")
                .param("email", "test@test.com")
                .param("password", "password");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            request = webb
                    .post("http://localhost:8000/create")
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
                .post("http://localhost:8000/create")
                .param("email", "test@test.com")
                .param("password", "password");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            request = webb
                    .post("http://localhost:8000/login")
                    .param("email", "test@test.com")
                    .param("password", "password");
            response = request
                    .asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "User found and password matches");
            expected.put("status", 202);
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
                .post("http://localhost:8000/login")
                .param("email", "doesnotexist@something.com")
                .param("password", "password");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = new JSONObject(response.getErrorBody().toString());
        JSONObject expected = new JSONObject();
        expected.put("message", "Bad username or password");
        expected.put("status", 403);
        JSONAssert.assertEquals(expected, result, true);
        assertEquals(403, response.getStatusCode());
    }

    @Test
    public void testReadBadPassword() throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .param("email", "test@test.com")
                .param("password", "password");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            request = webb
                    .post("http://localhost:8000/login")
                    .param("email", "test@test.com")
                    .param("password", "wrongPassword");
            response = request
                    .asJsonObject();
            result = new JSONObject(response.getErrorBody().toString());
            JSONObject expected = new JSONObject();
            expected.put("message", "Bad username or password");
            expected.put("status", 403);
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
                .post("http://localhost:8000/create")
                .param("email", "test@test.com")
                .param("password", "password");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            request = webb
                    .put("http://localhost:8000/update")
                    .param("email", "test@test.com")
                    .param("password", "newpassword");
            response = request
                    .asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "User password updated");
            expected.put("status", 202);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(202, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }
    }

    @Test
    public void updateNonExistingUserReturnsErrorMessage() throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .param("email", "test@test.com")
                .param("password", "password");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            request = webb
                    .put("http://localhost:8000/update")
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
                .post("http://localhost:8000/create")
                .param("email", "test@test.com")
                .param("password", "password");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            request = webb
                    .put("http://localhost:8000/deactivate")
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
                .post("http://localhost:8000/create")
                .param("email", "test@test.com")
                .param("password", "password");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            request = webb
                    .put("http://localhost:8000/deactivate")
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
                .post("http://localhost:8000/create")
                .param("email", "test@test.com")
                .param("password", "password");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            request = webb
                    .put("http://localhost:8000/deactivate")
                    .param("email", "test@test.com");
            response = request
                    .asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            request = webb
                    .put("http://localhost:8000/deactivate")
                    .param("email", "test@test.com");
            response = request.asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "User is already inactive");
            expected.put("status", 409);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(409, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }
    }

    @Test
    public void activateNonExistingUserReturnsErrorMessage() throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .param("email", "test@test.com")
                .param("password", "password");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            request = webb
                    .put("http://localhost:8000/activate")
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
    public void activateActiveUserReturnsErrorMessage() throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .param("email", "test@test.com")
                .param("password", "password");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            request = webb
                    .put("http://localhost:8000/activate")
                    .param("email", "test@test.com");
            response = request
                    .asJsonObject();
            result = response.getBody();
            System.out.println(response.getErrorBody().toString());
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "User is already active");
            expected.put("status", 409);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(409, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }
    }

    @Test
    public void activateInactiveUserActivatesUser() throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .param("email", "test@test.com")
                .param("password", "password");
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            request = webb
                    .put("http://localhost:8000/deactivate")
                    .param("email", "test@test.com");
            response = request
                    .asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            request = webb
                    .put("http://localhost:8000/activate")
                    .param("email", "test@test.com");
            response = request.asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "User's active status set to true");
            expected.put("status", 202);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(202, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }
    }
}
