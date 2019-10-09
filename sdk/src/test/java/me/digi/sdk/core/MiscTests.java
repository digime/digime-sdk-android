/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */
package me.digi.sdk.core;

import org.junit.Test;

import me.digi.sdk.core.internal.Util;

import static org.junit.Assert.assertEquals;

public class MiscTests {

    @Test
    public void removeNewLinesAndSpacesTest () {
        String text = "asas1\nasas2\rasas3 asas4\n\rasas5 \r asas6 \n\r asas7\r \n \r\tasas9";
        String expect = "asas1asas2asas3asas4asas5asas6asas7\tasas9";
        String result = Util.removeNewLinesAndSpaces(text);
        assertEquals(expect, result);
    }
}
