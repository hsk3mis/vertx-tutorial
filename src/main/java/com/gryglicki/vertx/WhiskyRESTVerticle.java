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
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class WhiskyRESTVerticle extends AbstractVerticle {

    private Map<Integer, Whisky> repository = Whisky.createSomeData();

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Router router = Router.router(vertx);

        router.route("/assets/*").handler(StaticHandler.create("assets"));

        router.route("/api/whiskies").handler(BodyHandler.create()); //enable reading request body on this route => disabled as an optimization for performance reason if you don't need it
        //router.route().handler(BodyHandler.create()); //configuration for any route

        //GET /api/whiskies
        router.get("/api/whiskies").handler(this::getAllWhiskies);
        //POST /api/whiskies
        router.post("/api/whiskies").handler(this::addWhisky);
        //DELETE /api/whiskies/:id
        router.delete("/api/whiskies/:id").handler(this::deleteWhisky);
        //GET /api/whiskies/:id
        router.get("/api/whiskies/:id").handler(this::getWhisky);
        //PUT /api/whiskies/:id
        router.put("/api/whiskies/:id").handler(this::updateWhisky);

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

    private void getAllWhiskies(RoutingContext routingContext) {
        routingContext.response()
            .putHeader("content-type", "application/json; charset=utf-8")
            .end(Json.encodePrettily(repository.values())); //uses Jackson for serialization
    }

    private void addWhisky(RoutingContext routingContext) {
        final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString(), Whisky.class);
        repository.put(whisky.id, whisky);
        routingContext.response()
            .setStatusCode(201)
            .putHeader("content-type", "application/json; charset=utf-8")
            .end(Json.encodePrettily(whisky));
    }

    private void deleteWhisky(RoutingContext routingContext) {
        idOrRespondWithBadRequest(routingContext)
            .ifPresent(whiskyId -> {
                repository.remove(whiskyId);
                routingContext.response().setStatusCode(204).end(); //204 = No content
            });
    }

    private void getWhisky(RoutingContext routingContext) {
        Optional<Integer> idOptional = idOrRespondWithBadRequest(routingContext);
        Optional<Whisky> whiskyOptional = idOptional.map(repository::get);
        if (whiskyOptional.isPresent()) {
            routingContext.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(whiskyOptional.get()));
        } else {
            routingContext.response().setStatusCode(404).end(); //404 = Not found
        }
    }

    private void updateWhisky(RoutingContext routingContext) {
        Optional<Integer> idOptional = idOrRespondWithBadRequest(routingContext);
        final Whisky newWhisky = Json.decodeValue(routingContext.getBodyAsString(), Whisky.class);
        idOptional.map(id -> repository.merge(id, newWhisky, Whisky::merge))
            .ifPresent(whisky ->
                routingContext.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(whisky)));
    }

    private Optional<Integer> idOrRespondWithBadRequest(RoutingContext routingContext) {
        try {
            return of(Integer.valueOf(routingContext.request().getParam("id")));
        } catch (NumberFormatException nfe) {
            routingContext.response().setStatusCode(400).end(); //400 = Bad request
            return empty();
        }
    }

}
