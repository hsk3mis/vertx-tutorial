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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;

import java.util.stream.IntStream;
public class VerticleWith2HttpServers extends AbstractVerticle {

    /** Method called when verticle is deployed. */
    @Override
    public void start(Future<Void> startFuture) throws Exception //startFuture let us inform Vert.x when out start sequence is completed or report an error.
    {
        Future<HttpServer> server8080Started = startHttpServer(8080, this::requestHandler);
        Future<HttpServer> server8081Started = startHttpServer(8081, this::requestHandlerWithVeryLongAnswer);

        CompositeFuture.all(server8080Started, server8081Started).setHandler(result -> {
            if (result.succeeded())
                startFuture.complete();
            else
                startFuture.fail(result.cause());
        });
    }

    private Future<HttpServer> startHttpServer(int port, Handler<HttpServerRequest> handler) {
        Future<HttpServer> startServerFuture = Future.future();
        vertx
            .createHttpServer()
            .requestHandler(handler)
            .listen(port, startServerFuture.completer());
        return startServerFuture;
    }

    private void requestHandler(HttpServerRequest request) {
        request.response().end("<h1>Hello world from Vert.x HTTP Server</h1>");
    }

    private void requestHandlerWithVeryLongAnswer(HttpServerRequest request) {
        StringBuilder sb = new StringBuilder("");
        IntStream.rangeClosed(0, 1000).forEach(sb::append);
        request.response().end(sb.toString());
    }

}
