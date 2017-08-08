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
@RunWith(VertxUnitRunner.class)
public class VerticleWith2HttpServersTest
{
    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(VerticleWith2HttpServers.class.getName(), context.asyncAssertSuccess());
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

        vertx.createHttpClient().getNow(8080, "localhost", "/", response -> {
            response.handler(body -> {
               context.assertTrue(body.toString().contains("Hello")); //interrupts test framework immediately when assertion fail
                printlnWithThread("Asynchronous: " + body.toString());
               async.complete();
            });
        });
    }

    @Test
    public void testVeryLongAnswer(TestContext context) throws Exception {
        /** Synchronous client */
        URLConnection connection = new URL("http://localhost:8081/").openConnection();
        try(BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line = in.readLine();
            printlnWithThread("Synchronous: " + line);
        }

        /** Asynchronous client */
        final Async async = context.async(); //allows to notify test framework when we finish successfully or fail - controls test execution like context.asyncAssertSuccess()

        //A lot of data can cause handler to be called multiple times with chunks of data !!!
        vertx.createHttpClient().getNow(8081, "localhost", "/", response -> {
            response.handler(body -> {
                printlnWithThread("Asynchronous: " + body.toString());
                async.complete();
            });
        });
    }
}