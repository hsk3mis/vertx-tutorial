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
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
/**
 * Verticle => component (Actor like) in Vert.x. Deployment Unit.
 */
public class MyFirstVerticle extends AbstractVerticle {

    /** Method called when verticle is deployed. */
    @Override
    public void start(Future<Void> startFuture) throws Exception //startFuture let us inform Vert.x when out start sequence is completed or report an error.
    {
        vertx
            .createHttpServer()
            .requestHandler(this::requestHandler)
            .listen(8080, result -> { //handler executed when server is actually started listening
                if (result.succeeded())
                    startFuture.complete(); //inform Vert.x that this Verticle initialization completed successfully
                else
                    startFuture.fail(result.cause());
            });
    }

    private void requestHandler(HttpServerRequest request) {
        request.response().end("<h1>Hello world from Vert.x HTTP Server</h1>");
    }

}
