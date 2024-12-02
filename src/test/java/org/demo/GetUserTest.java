package org.demo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.demo.model.User;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class GetUserTest {

    // Tests assumptions on test data:
    // NON_EXISTANT_ID not id of any user
    final static private int NON_EXISTANT_ID = 1234;

    @Test
    public void addAndGetUser() {
        User user = new User() {{
            address = "Lördagsvägen 3\n12345 LUND";
            phone = "070-456789";
            email = "abc@gmail.com";
            name = "Anna Annorsson";
        }};

        int id = addUser(user);

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", id)
        .when()
                .get("/digg/users/{id}")
        .then()
                .statusCode(200)
                .body("address", equalTo(user.address))
                .body("phone", equalTo(user.phone))
                .body("email", equalTo(user.email))
                .body("name", equalTo(user.name))
                .body("id", equalTo(id));
    }

    @Test
    public void tryToGetNonExistingId() {
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", NON_EXISTANT_ID)
        .when()
                .get("/digg/users/{id}")
        .then()
                .statusCode(404);
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
}
