import com.goebl.david.Request;
import com.goebl.david.Webb;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


import static org.junit.Assert.*;

/**
 * Created by benhernandez on 5/21/16.
 */
public class AuthServiceTest {
    @Test
    public void testCreate()  throws Exception {
        Webb webb = Webb.create();
        Request request = webb
                .post("http://localhost:8080/create")
                .param("email", "test@test.com")
                .param("password", "password");
        JSONObject result = request
                .asJsonObject()
                .getBody();
        if (result == null) {
            result = new JSONObject(request.asJsonObject().getErrorBody().toString());
        }
        System.out.println("result: " + result);
        JSONObject expected = new JSONObject();
        expected.put("message", "User created");
        expected.put("status", 201);
        expected.put("token", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIn0.egbaJ7yWUvC4mU_C7LNJi24cPNpfx3rlr7woWn9pqsGX6LrGCK2Rf2LaD2cFiJ4AWC93QDMChuCmUM4YtDjzAw");

        JSONAssert.assertEquals(expected, result, true);
    }
}