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
        Connection connection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/Users");
        Statement statement = connection.createStatement();
        String dropQuery = "DROP TABLE IF EXISTS public.users;";
        String query = "CREATE TABLE public.users\n" +
                "(\n" +
                "  email character varying NOT NULL,\n" +
                "  password character varying,\n" +
                "  active boolean DEFAULT false,\n" +
                "  is_teacher boolean,\n" +
                "  CONSTRAINT users_pkey PRIMARY KEY (email)\n" +
                ")";
        statement.execute(dropQuery);
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
        Connection connection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/Users");
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
        JSONObject obj = new JSONObject();
        obj.put("email", "test@test.com");
        obj.put("password", "password");
        obj.put("isTeacher", true);
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .body(obj);
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result == null) {
            result = new JSONObject(response.getErrorBody().toString());
        }
        JSONObject expected = new JSONObject();
        expected.put("message", "User created");
        expected.put("status", 201);
        JSONAssert.assertEquals(expected, result, true);
        assertEquals(201, response.getStatusCode());
    }

    @Test
    public void testCreatedAlreadyExistingUser() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("email", "test@test.com");
        obj.put("password", "password");
        obj.put("isTeacher", true);
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .body(obj);
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            request = webb
                    .post("http://localhost:8000/create")
                    .body(obj);
            response = request
                    .asJsonObject();
            result = new JSONObject(response.getErrorBody().toString());
            JSONObject expected = new JSONObject();
            expected.put("message", "User already exists");
            expected.put("status", 400);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(400, response.getStatusCode());
        }
    }

    @Test
    public void testReadUserExists() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("email", "test@test.com");
        obj.put("password", "password");
        obj.put("isTeacher", true);
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .body(obj);
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            request = webb
                    .post("http://localhost:8000/login")
                    .body(obj);
            response = request
                    .asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "User found and password matches");
            expected.put("status", 200);
            JSONAssert.assertEquals(expected, result, false);
            assertEquals(200, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }

    }

//    @Test
//    public void testReadUserDoesNotExist() throws Exception {
//        JSONObject obj = new JSONObject();
//        obj.put("email", "doesnotexist@something.com");
//        obj.put("password", "password");
//        Webb webb = Webb.create();
//        Request request = webb
//                .post("http://localhost:8000/login")
//                .body(obj);
//        Response<JSONObject> response = request
//                .asJsonObject();
//        JSONObject result = new JSONObject(response.getErrorBody().toString());
//        JSONObject expected = new JSONObject();
//        expected.put("message", "Bad username or password");
//        expected.put("status", 400);
//        JSONAssert.assertEquals(expected, result, true);
//        assertEquals(400, response.getStatusCode());
//    }

//    @Test
//    public void testReadBadPassword() throws Exception {
//        JSONObject obj = new JSONObject();
//        obj.put("email", "test@test.com");
//        obj.put("password", "password");
//        obj.put("isTeacher", true);
//        Webb webb = Webb.create();
//        Request request = webb
//                .post("http://localhost:8000/create")
//                .body(obj);
//        Response<JSONObject> response = request
//                .asJsonObject();
//        JSONObject result = response.getBody();
//        if (result != null) {
//            JSONObject loginObject = new JSONObject();
//            loginObject.put("email", "test@test.com");
//            loginObject.put("password", "wrongPassword");
//            request = webb
//                    .post("http://localhost:8000/login")
//                    .body(loginObject);
//            response = request
//                    .asJsonObject();
//            result = new JSONObject(response.getErrorBody().toString());
//            JSONObject expected = new JSONObject();
//            expected.put("message", "Wrong email or password");
//            expected.put("status", 401);
//            JSONAssert.assertEquals(expected, result, true);
//            assertEquals(401, response.getStatusCode());
//        } else {
//            fail("Unable to even create the user");
//        }
//    }

    @Test
    public void existingUserUpdatesPassword() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("email", "test@test.com");
        obj.put("password", "password");
        obj.put("isTeacher", true);
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .body(obj);
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            obj.remove("password");
            obj.put("oldPassword", "password");
            obj.put("newPassword", "newPassword");
            request = webb
                    .put("http://localhost:8000/update")
                    .body(obj);
            response = request
                    .asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "User password updated");
            expected.put("status", 200);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(200, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }
    }

    @Test
    public void updateNonExistingUserReturnsErrorMessage() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("email", "test@test.com");
        obj.put("password", "password");
        obj.put("isTeacher", true);
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .body(obj);
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            obj.remove("email");
            obj.put("email", "doesnotexist@something.com");
            obj.put("oldPassword", "password");
            obj.put("newPassword", "newPassword");
            request = webb
                    .put("http://localhost:8000/update")
                    .body(obj);
            response = request
                    .asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "User does not exist");
            expected.put("status", 400);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(400, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }
    }

    @Test
    public void deleteNonExistingUserReturnsErrorMessage() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("email", "test@test.com");
        obj.put("password", "password");
        obj.put("isTeacher", true);
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .body(obj);
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            obj.remove("password");
            obj.remove("email");
            obj.put("email", "doesnotexist@something.com");
            request = webb
                    .put("http://localhost:8000/deactivate")
                    .body(obj);
            response = request
                    .asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "User does not exist");
            expected.put("status", 400);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(400, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }
    }

    @Test
    public void deleteExistingUserSetsActiveStatusToFalse() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("email", "test@test.com");
        obj.put("password", "password");
        obj.put("isTeacher", true);
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .body(obj);
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            obj.remove("password");
            request = webb
                    .put("http://localhost:8000/deactivate")
                    .body(obj);
            response = request
                    .asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "Account deactivated");
            expected.put("status", 200);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(200, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }
    }

    @Test
    public void deleteInactiveUserReturnsError() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("email", "test@test.com");
        obj.put("password", "password");
        obj.put("isTeacher", true);
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .body(obj);
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            obj.remove("password");
            request = webb
                    .put("http://localhost:8000/deactivate")
                    .body(obj);
            response = request
                    .asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            request = webb
                    .put("http://localhost:8000/deactivate")
                    .body(obj);
            response = request.asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "Already deactivated");
            expected.put("status", 400);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(400, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }
    }

    @Test
    public void activateNonExistingUserReturnsErrorMessage() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("email", "test@test.com");
        obj.put("password", "password");
        obj.put("isTeacher", true);
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .body(obj);
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            obj.remove("password");
            obj.remove("email");
            obj.put("email", "doesnotexist@something.com");
            request = webb
                    .put("http://localhost:8000/activate")
                    .body(obj);
            response = request
                    .asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "User does not exist");
            expected.put("status", 400);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(400, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }
    }

    @Test
    public void activateActiveUserReturnsErrorMessage() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("email", "test@test.com");
        obj.put("password", "password");
        obj.put("isTeacher", true);
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .body(obj);
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            obj.remove("password");
            request = webb
                    .put("http://localhost:8000/activate")
                    .body(obj);
            response = request
                    .asJsonObject();
            result = response.getBody();
            System.out.println(response.getErrorBody().toString());
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "Already activated");
            expected.put("status", 400);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(400, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }
    }

    @Test
    public void activateInactiveUserActivatesUser() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("email", "test@test.com");
        obj.put("password", "password");
        obj.put("isTeacher", true);
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8000/create")
                .body(obj);
        Response<JSONObject> response = request
                .asJsonObject();
        JSONObject result = response.getBody();
        if (result != null) {
            obj.remove("password");
            request = webb
                    .put("http://localhost:8000/deactivate")
                    .body(obj);
            response = request
                    .asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            request = webb
                    .put("http://localhost:8000/activate")
                    .body(obj);
            response = request.asJsonObject();
            result = response.getBody();
            if (result == null) {
                result = new JSONObject(response.getErrorBody().toString());
            }
            JSONObject expected = new JSONObject();
            expected.put("message", "Account activated");
            expected.put("status", 200);
            JSONAssert.assertEquals(expected, result, true);
            assertEquals(200, response.getStatusCode());
        } else {
            fail("Unable to even create the user");
        }
    }
}
