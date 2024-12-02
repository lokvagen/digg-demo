package org.demo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.demo.model.User;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class DeleteUserTest {

    // Tests assumptions on test data:
    // NON_EXISTANT_ID not id of any user
    final static private int NON_EXISTANT_ID = 1234;

    @Test
    public void addAndDeleteUser() {
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
                .delete("/digg/users/{id}")
        .then()
                .statusCode(204);

        // TODO should check that it is gone
    }

    @Test
    public void tryToRemoveNonExistingId() {
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", NON_EXISTANT_ID)
        .when()
                .delete("/digg/users/{id}")
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
