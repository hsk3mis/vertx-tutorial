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
                printlnWithThread("Asynchronous: " + body.toString());
               async.complete();
            });
        });
    }

    @Test
    public void testStaticResource(TestContext context) throws Exception {
        /** Asynchronous client */
        final Async async = context.async();

        vertx.createHttpClient().getNow(8080, "localhost", "/assets/index.html", response -> {
            response.handler(body -> {
                //TODO: In this case handler is called multiple times (long answer) => how to assert on this kind of response ???
                context.assertTrue(body.toString() != null);
                printlnWithThread("Asynchronous: " + body.toString());
                async.complete();
            });
        });
    }
}