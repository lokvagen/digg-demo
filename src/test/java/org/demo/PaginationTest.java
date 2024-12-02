package org.demo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.demo.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.List;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class PaginationTest {

    // Tests assumptions on test data:
    // more than 10 but less than DATABASE_NO_MORE_THAN_X_USERS users in db
    final static private int DATABASE_NO_MORE_THAN_X_USERS = 200;
    final static private int DEFAULT_PAGINATION = 10;

    @Test
    public void getUsersPaginationDefault() {
        checkPagination(DEFAULT_PAGINATION, -1);
    }

    @Test
    public void getUsersPaginationExplicitDefault() {
        checkPagination(DEFAULT_PAGINATION, 10);
    }

    @Test
    public void getUsersPaginationPageSize1() {
        checkPagination(1, 1);
    }

    @Test
    public void getUsersPaginationPageSize5() {
        checkPagination(5, 5);
    }

    @Test
    public void getUsersPaginationPageSizeHuge() {
        checkPagination(DATABASE_NO_MORE_THAN_X_USERS, DATABASE_NO_MORE_THAN_X_USERS);
    }

    public void checkPagination(int size, int sizeParam){
        List<User> users = getUserPageOnlySize(DATABASE_NO_MORE_THAN_X_USERS);
        int numberOfUsers = users.size();

        int numberOfFullPages = numberOfUsers / size;
        int numUsersOnLastPage = numberOfUsers % size;

        List<User>firstpageusers = getUserPageOnlySize(sizeParam);

        for (int pageNo = 0; pageNo < numberOfFullPages; pageNo++) {
            List<User>pageusers = getUserPage(sizeParam,pageNo);

            if( 0==pageNo ) {
                System.out.println("pageusers="+pageusers.size()+" firstpageusers: "+firstpageusers.size());
                boolean allUsersMatched = true;
                String errorMsg = "";

                List<User>diff1 = compareUserLists(firstpageusers,pageusers);
                if(diff1.size()>0){
                    String userIds = diff1.stream()
                            .map(user -> String.valueOf(user.id))
                            .collect(Collectors.joining(", "));
                    allUsersMatched = false;
                    errorMsg += "User with id "+ userIds + " missing when size=0 in query.";
                }

                List<User>diff2 = compareUserLists(pageusers,firstpageusers);
                if(diff2.size()>0){
                    String userIds = diff2.stream()
                            .map(user -> String.valueOf(user.id))
                            .collect(Collectors.joining(", "));
                    allUsersMatched = false;
                    errorMsg += "User with id "+ userIds + " missing when no size set.";
                }
                Assertions.assertTrue(allUsersMatched, "Same list if page=0 requested as when no page set (page size="+size+"). "+errorMsg);
            }
            Assertions.assertEquals(size,pageusers.size(), "Expected number of users on page "+pageNo+" (page size="+size+")");
        }

        // and last page
        if( numUsersOnLastPage>0){
            List<User>pageusers = getUserPage(sizeParam,numberOfFullPages);
            Assertions.assertEquals(numUsersOnLastPage,pageusers.size(), "Expected number of users on last page (page size="+size+",numberOfUsers="+numberOfUsers+")");
        }
    }

    public List<User> compareUserLists(List<User> expected, List<User> checkList){
        List<User> diff = new ArrayList<>();
        for (User expectedUser : expected) {
            boolean found = false;
            for (User checkUser : checkList) {
                if(expectedUser.id==checkUser.id)
                    found = true;
            }
            if(!found){
                diff.add(expectedUser);
            }
        }
        return diff;
    }

    public List<User> getUserPageOnlySize(int size){
        return getUserPage(size, -1);
    }

    public List<User> getUserPage(int size, int page){
        RequestSpecification request = given().contentType(ContentType.JSON);
        if(size!=-1){
            request = request.queryParam("size", size);
        }
        if(page!=-1){
            request = request.queryParam("page", page);
        }
        Response response = request
        .when()
                .get("/digg/users")
        .then()
                .statusCode(200)
                .extract().response();
        return response.jsonPath().getList("$", User.class);
    }
}
