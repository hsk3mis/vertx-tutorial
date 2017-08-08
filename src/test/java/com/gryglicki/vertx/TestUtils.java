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

public class TestUtils
{
    public static void printlnWithThread(String message) {
        System.out.println(message + " - " + Thread.currentThread());
    }
}
