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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
/**
 * Structure representing list of Whiskies for Json transformation.
 */
public class WhiskiesList {
    @JsonProperty("whiskies")
    Whisky[] whiskies;

    @Override
    public String toString()
    {
        return "WhiskiesList{" +
            "whiskies=" + Arrays.toString(whiskies) +
            '}';
    }

    public static String jsonWithArrayOfWhiskiesToDecodeableWhiskiesList(String jsonWithArray) {
        return String.format("{\"whiskies\":%s}", jsonWithArray);
    }
}
