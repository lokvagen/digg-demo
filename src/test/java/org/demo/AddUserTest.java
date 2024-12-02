package org.demo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.demo.model.User;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class AddUserTest {

    @Test
    public void addUserSettingAllFields() {
        User user = new User() {{
            address = "Lördagsvägen 3\n12345 LUND";
            phone = "070-456789";
            email = "abc@gmail.com";
            name = "Anna Annorsson";
        }};

        addValidUserTest(user);
    }

    @Test
    public void addUserWithNameOnly() {
        User user = new User() {{
            name = "Berra Bertilsson";
        }};

        addValidUserTest(user);
    }

    @Test
    public void addUserWithNameOnlyAsJson() {
        String name = "Calle Claesson";
        String jsonUser = "{\"name\": \""+name+"\"}";
        given()
                .contentType(ContentType.JSON)
                .body(jsonUser)
        .when()
                .post("/digg/users")
        .then()
                .statusCode(201)
                .body("name", equalTo(name))
                .body("id", notNullValue());
    }

    @Test
    public void addUserTryingToSetId() {
        String name = "Darin Danielsson";
        String jsonUser = "{\"name\": \""+name+"\",\"id\": 6}";
        given()
                .contentType(ContentType.JSON)
                .body(jsonUser)
        .when()
                .post("/digg/users")
        .then()
                .statusCode(400);
    }
    public void addValidUserTest(User user){
        given()
                .contentType(ContentType.JSON)
                .body(user)
        .when()
                .post("/digg/users")
        .then()
                .statusCode(201)
                .body("address", equalTo(user.address))
                .body("phone", equalTo(user.phone))
                .body("email", equalTo(user.email))
                .body("name", equalTo(user.name))
                .body("id", notNullValue());
    }
}
