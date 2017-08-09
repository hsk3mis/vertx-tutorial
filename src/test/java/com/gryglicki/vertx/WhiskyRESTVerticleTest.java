package com.gryglicki.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.gryglicki.vertx.TestUtils.printlnWithThread;
import static org.junit.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class WhiskyRESTVerticleTest
{
    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(WhiskyRESTVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testServerAnswer(TestContext context) throws Exception {
        /** Asynchronous client */
        final Async async = context.async();

        vertx.createHttpClient().getNow(8080, "localhost", "/api/whiskies", response -> {
            response.handler(body -> {
                WhiskiesList whiskiesList = Json.decodeValue(WhiskiesList.jsonWithArrayOfWhiskiesToDecodeableWhiskiesList(body.toString()), WhiskiesList.class);
                context.assertTrue(whiskiesList.whiskies.length == 2);
                printlnWithThread("Asynchronous: " + body.toString());
                async.complete();
            });
        });
    }

    @Test
    public void jsonDecodeTest() {
        //Given
        String json = "[ {\n"
            + "  \"id\" : 0,\n"
            + "  \"name\" : \"Bowmore 15 Years Laimrig\",\n"
            + "  \"origin\" : \"Scotland, Islay\"\n"
            + "}, {\n"
            + "  \"id\" : 1,\n"
            + "  \"name\" : \"Talisker 57° North\",\n"
            + "  \"origin\" : \"Scotland, Island\"\n"
            + "} ]";
        WhiskiesList whiskiesList = Json.decodeValue(WhiskiesList.jsonWithArrayOfWhiskiesToDecodeableWhiskiesList(json), WhiskiesList.class);
        assertEquals(2, whiskiesList.whiskies.length);
        assertEquals("Bowmore 15 Years Laimrig", whiskiesList.whiskies[0].name);
    }

    /** Sending POST request */
    @Test
    public void itIsPossibleToAddWhisky(TestContext context) {
        final Async async = context.async();

        final String json = Json.encodePrettily(new Whisky("Jameson", "Ireland"));
        final String jsonLength = Integer.toString(json.length());

        vertx.createHttpClient().post(8080, "localhost", "/api/whiskies")
            .putHeader("content-type", "application/json")
            .putHeader("content-length", jsonLength)
            .handler(response -> { //setup response handler before sending request
                context.assertEquals(201, response.statusCode());
                context.assertTrue(response.headers().get("content-type").contains("application/json"));
                response.bodyHandler(body -> {
                    final Whisky whisky = Json.decodeValue(body.toString(), Whisky.class);
                    context.assertNotNull(whisky.id);
                    context.assertEquals(whisky.name, "Jameson");
                    context.assertEquals(whisky.origin, "Ireland");
                    async.complete();
                });
            })
            .write(json) //write request
            .end();

    }
}