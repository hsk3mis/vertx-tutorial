package com.gryglicki.vertx;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static com.gryglicki.vertx.TestUtils.printlnWithThread;
/**
 * VertxUnitRunner and TestContext - controls asynchronous aspect of the test
 * context.assertTrue(...) => assertions need to be run from context, because of synchronous nature.
 * Otherwise if normal assertion fails then handler fails, async.complete() is not called and test waits for it to be called to move forward.
 */
@RunWith(VertxUnitRunner.class)
public class MyFirstVerticleTest {
    private Vertx vertx;

    /**
     *
     * Deployment happens asynchronously, so we can't check anything until it finishes starting correctly.
     * context.asyncAssertSuccess() => creates event handler and holds VertxUnitRunner from running tests until this handler is not executed.
     * When deployment finishes then completionHandler is executed (taken from context) and VertxUnitRunner knows it can move forward.
     */
    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(MyFirstVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testServerAnswer(TestContext context) throws Exception {
        /** Synchronous client */
        URLConnection connection = new URL("http://localhost:8080/").openConnection();
        try(BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line = in.readLine();
            printlnWithThread("Synchronous: " + line);
        }

        /** Asynchronous client */
        final Async async = context.async(); //allows to notify test framework when we finish successfully or fail - controls test execution like context.asyncAssertSuccess()

        //A lot of data can cause handler to be called multiple times with chunks of data !!!
        vertx.createHttpClient().getNow(8080, "localhost", "/", response -> {
            response.handler(body -> {
                //Assert.assertTrue(...) if fail then async.complete() would not be called
                context.assertTrue(body.toString().contains("Hello")); //interrupts test framework immediately when assertion fail
                printlnWithThread("Asynchronous: " + body.toString());
               async.complete();
            });
        });
    }
}