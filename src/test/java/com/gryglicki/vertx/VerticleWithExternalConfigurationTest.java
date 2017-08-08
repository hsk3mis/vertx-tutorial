package com.gryglicki.vertx;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;

import static com.gryglicki.vertx.TestUtils.printlnWithThread;

/**
 * Using external configuration from file:
 *  java -jar target/vertx-tutorial-1.0-SNAPSHOT-fat.jar -conf src/main/resources/conf/my-application-conf.json
 * -conf parameter is handled by FAT JAR vertx.Starter
 */
@RunWith(VertxUnitRunner.class)
public class VerticleWithExternalConfigurationTest
{
    private Vertx vertx;
    private int port;

    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        port = randomPort();
        DeploymentOptions options = new DeploymentOptions()
            .setConfig(new JsonObject().put("http.port", port));
        vertx.deployVerticle(VerticleWithExternalConfiguration.class.getName(), options, context.asyncAssertSuccess());
        System.out.println("Running Verticle on port: " + port);
    }

    /** Can fail if port gets picked between socket.close() and verticle start, but will work in most cases. */
    private int randomPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testServerAnswer(TestContext context) throws Exception {
        /** Asynchronous client */
        final Async async = context.async();

        vertx.createHttpClient().getNow(port, "localhost", "/", response -> {
            response.handler(body -> {
                context.assertTrue(body.toString().contains("Hello")); //interrupts test framework immediately when assertion fail
                printlnWithThread("Asynchronous: " + body.toString());
               async.complete();
            });
        });
    }
}