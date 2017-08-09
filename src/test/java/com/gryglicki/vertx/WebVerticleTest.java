package com.gryglicki.vertx;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.gryglicki.vertx.TestUtils.printlnWithThread;
@RunWith(VertxUnitRunner.class)
public class WebVerticleTest
{
    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(WebVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testServerAnswer(TestContext context) throws Exception {
        /** Asynchronous client */
        final Async async = context.async();

        vertx.createHttpClient().getNow(8080, "localhost", "/", response -> {
            response.handler(body -> {
                context.assertTrue(body.toString().contains("Hello"));
               async.complete();
            });
        });
    }

    @Test
    public void testStaticResource(TestContext context) throws Exception {
        /** Asynchronous client */
        final Async async = context.async();

        vertx.createHttpClient().getNow(8080, "localhost", "/assets/index.html", response -> {
            context.assertEquals(200, response.statusCode());
            context.assertTrue(response.headers().get("content-type").contains("text/html"));
            response.bodyHandler(body -> { //receive entire body as one piece
                context.assertTrue(body.toString().contains("<title>My Whisky Collection</title>"));
                async.complete();
            });
        });
    }
}