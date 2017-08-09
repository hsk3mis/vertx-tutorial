/*
 * Copyright 2001,2017 (c) Point Of Sale Solutions (POSS) of Sabre Inc. All
 * rights reserved.
 * 
 * This software and documentation is the confidential and proprietary
 * information of Sabre Inc. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with Sabre Inc.
 */
package com.gryglicki.vertx;

import com.jayway.restassured.RestAssured;
import org.awaitility.Awaitility;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Duration.FIVE_SECONDS;
import static org.hamcrest.Matchers.equalTo;
/** Integration Test => Failsafe plugin recognizes "IT" ending and executes this tests in an integration-test maven phase */
public class MyRestIT {

    @BeforeClass
    public static void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = Integer.getInteger("http.port", 8080);
        System.out.println("MyRestIT port = " + RestAssured.port);
        //TODO: How to set socket read timeout with RestAssured ???
        Awaitility.await().atMost(FIVE_SECONDS).until(() -> canConnectToServer(RestAssured.baseURI + ":" + RestAssured.port)); //Timeout 10 seconds by default
        System.out.println("MyRestIT canConnectToServer = true");
    }

    private static boolean canConnectToServer(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();
            connection.disconnect();
            return true;
        } catch (IOException ioEx) {
            return false;
        }
    }

    @AfterClass
    public static void unconfigureRestAssured() {
        RestAssured.reset();
    }

    @Test(timeout = 5_000)
    public void checkThatWeCanRetrieveIndividualProduct() {
        // Get the list of bottles, ensure it's a success and extract the first id.
        final int id = RestAssured.get("/api/whiskies").then()
            .assertThat()
            .statusCode(200)
            .extract()
            .jsonPath().getInt("find { it.name=='Bowmore 15 Years Laimrig' }.id");
        // Now get the individual resource and check the content
        RestAssured.get("/api/whiskies/" + id).then()
            .assertThat()
            .statusCode(200)
            .body("name", equalTo("Bowmore 15 Years Laimrig"))
            .body("origin", equalTo("Scotland, Islay"))
            .body("id", equalTo(id));
    }

    @Test(timeout = 5_000)
    public void checkWeCanAddAndDeleteAProduct() {
        // Create a new bottle and retrieve the result (as a Whisky instance).
        Whisky whisky = RestAssured.given()
            .body("{\"name\":\"Jameson\", \"origin\":\"Ireland\"}").request().post("/api/whiskies").thenReturn().as(Whisky.class);
        assertThat(whisky.name).isEqualToIgnoringCase("Jameson");
        assertThat(whisky.origin).isEqualToIgnoringCase("Ireland");
        assertThat(whisky.id).isNotZero();
        // Check that it has created an individual resource, and check the content.
        RestAssured.get("/api/whiskies/" + whisky.id).then()
            .assertThat()
            .statusCode(200)
            .body("name", equalTo("Jameson"))
            .body("origin", equalTo("Ireland"))
            .body("id", equalTo(whisky.id));
        // Delete the bottle
        RestAssured.delete("/api/whiskies/" + whisky.id).then().assertThat().statusCode(204);
        // Check that the resource is not available anymore
        RestAssured.get("/api/whiskies/" + whisky.id).then()
            .assertThat()
            .statusCode(404);
    }
}
