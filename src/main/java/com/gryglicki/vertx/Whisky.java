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

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
/**
 * Whisky structure
 */
public class Whisky {
    private static final AtomicInteger COUNTER = new AtomicInteger();

    public final int id;
    public String name;
    public String origin;

    /** Needed by Jackson for JSON decoding */
    public Whisky() {
        this.id = COUNTER.getAndIncrement();
    }

    public Whisky(String name, String origin) {
        this.id = COUNTER.getAndIncrement();
        this.name = name;
        this.origin = origin;
    }

    private Whisky(int id, String name, String origin)
    {
        this.id = id;
        this.name = name;
        this.origin = origin;
    }

    public static Map<Integer, Whisky> createSomeData() {
        return Stream.of(
            new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay"),
            new Whisky("Talisker 57° North", "Scotland, Island"))
        .collect(Collectors.toMap(whisky -> whisky.id, identity()));
    }

    public static Whisky merge(Whisky oldWhisky, Whisky newWhisky)
    {
        return new Whisky(oldWhisky.id,
            ofNullable(newWhisky.name).orElse(oldWhisky.name),
            ofNullable(newWhisky.origin).orElse(oldWhisky.origin));
    }

    @Override
    public String toString()
    {
        return "Whisky{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", origin='" + origin + '\'' +
            '}';
    }
}
