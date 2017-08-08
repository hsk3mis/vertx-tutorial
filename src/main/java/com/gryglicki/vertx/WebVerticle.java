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
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

public class WebVerticle extends AbstractVerticle {

@Override
    public void start(Future<Void> startFuture) throws Exception
    {
        Router router = Router.router(vertx);

        /** .route(...) => dispatching requests configuration */
        /** .handler(...) => chained (multiple) handlers */
        router.route("/").handler(this::contentTypeHeaderHandler);
        router.route("/").handler(this::bodyHandler); //handlers that can be chained

        router.route("/assets/*").handler(StaticHandler.create("assets"));

        vertx
            .createHttpServer()
            .requestHandler(router::accept)
            .listen(config().getInteger("http.port", 8080), result -> {
                if (result.succeeded())
                    startFuture.complete();
                else
                    startFuture.fail(result.cause());
            });
    }

    private void contentTypeHeaderHandler(RoutingContext routingContext) {
        routingContext.response()
            .putHeader("content-type", "text/html");
        routingContext.next(); //important for another handler to be called
    }

    private void bodyHandler(RoutingContext routingContext) {
        routingContext.response()
            .end("<h1>Hello world from Vert.x Web HTTP Server</h1>");
    }
}
