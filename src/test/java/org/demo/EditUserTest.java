package org.demo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.demo.model.User;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class EditUserTest {

    // Tests assumptions on test data:
    // NON_EXISTANT_ID not id of any user
    final static private int NON_EXISTANT_ID = 1234;

    @Test
    public void replaceAllUserValues() {
        int id = 1;
        User oldUser = getUser(id);
        User newUser = new User() {{
            address = "Fredagsvägen 3\n12345 MALMÖ";
            phone = "070-123456";
            email = "123@gmail.com";
            name = "Per Persson";
        }};

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", id)
                .body(newUser)
        .when()
                .patch("/digg/users/{id}")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("address", equalTo(newUser.address))
                .body("phone", equalTo(newUser.phone))
                .body("email", equalTo(newUser.email))
                .body("name", equalTo(newUser.name))
                .body("id", equalTo(id));

        // check that updated
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", id)
        .when()
                .get("/digg/users/{id}")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("address", equalTo(newUser.address))
                .body("phone", equalTo(newUser.phone))
                .body("email", equalTo(newUser.email))
                .body("name", equalTo(newUser.name))
                .body("id", equalTo(id));
    }

    @Test
    public void replaceAllUserValuesSkipReturnValueTest() {
        int id = 1;
        User oldUser = getUser(id);
        User newUser = new User() {{
            address = "Fredagsvägen 3\n12345 MALMÖ";
            phone = "070-123456";
            email = "123@gmail.com";
            name = "Per Persson";
        }};

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", id)
                .body(newUser)
        .when()
                .patch("/digg/users/{id}")
        .then()
                .statusCode(200);

        // check that updated
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", id)
        .when()
                .get("/digg/users/{id}")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("address", equalTo(newUser.address))
                .body("phone", equalTo(newUser.phone))
                .body("email", equalTo(newUser.email))
                .body("name", equalTo(newUser.name))
                .body("id", equalTo(id));
    }

    @Test
    public void editPhoneWithJson() {
        String phone = "070-123654";
        int id = 2;
        String jsonEdit = "{\"phone\": \""+phone+"\"}";

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", id)
                .body(jsonEdit)
        .when()
                .patch("/digg/users/{id}")
        .then()
                .statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", id)
        .when()
                .get("/digg/users/{id}")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("phone", equalTo(phone))
                .body("id", equalTo(id));
    }

    @Test
    public void editPhoneWithJsonCorrectId() {
        String phone = "070-123654";
        int id = 3;
        String jsonEdit = "{\"phone\": \""+phone+"\",\"id\": "+id+"}";

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", id)
                .body(jsonEdit)
        .when()
                .patch("/digg/users/{id}")
        .then()
                .statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", id)
        .when()
                .get("/digg/users/{id}")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("phone", equalTo(phone))
                .body("id", equalTo(id));
    }

    @Test
    public void editPhoneWithJsonTryToEditId() {
        String phone = "070-123654";
        int id = 4;
        int newId = 256;
        String jsonEdit = "{\"id\": "+newId+",\"phone\": \""+phone+"\"}";

        User userBefore = getUser(id);

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", id)
                .body(jsonEdit)
        .when()
                .patch("/digg/users/{id}")
        .then()
                .statusCode(400);

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", id)
        .when()
                .get("/digg/users/{id}")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("phone", equalTo(userBefore.phone))
                .body("id", equalTo(id));
    }

    @Test
    public void tryToEditNonExistantUser() {
        String phone = "070-8888888";
        int id = 5;
        String jsonEdit = "{\"phone\": \""+phone+"\"}";

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", NON_EXISTANT_ID)
                .body(jsonEdit)
        .when()
                .patch("/digg/users/{id}")
        .then()
                .statusCode(404); // not specified but resonable?
    }

    public int addUser(User user){
        Response response = given()
                .contentType(ContentType.JSON)
                .body(user)
        .when()
                .post("/digg/users")
        .then()
                .statusCode(201)
                .extract().response();
        User addedUser = response.as(User.class);
        return (int)(long)addedUser.id;
    }

    public User getUser(int id){
        Response response = given()
                .contentType(ContentType.JSON)
                .pathParam("id", id)
        .when()
                .get("/digg/users/{id}")
        .then()
                .statusCode(200)
                .extract().response();
        return response.as(User.class);
    }
}
