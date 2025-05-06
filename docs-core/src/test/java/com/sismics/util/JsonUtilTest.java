package com.sismics.util;

import jakarta.json.Json;
import jakarta.json.JsonValue;
import junit.framework.TestCase;

public class JsonUtilTest extends TestCase {

    public void testNullable_Null() {
        JsonValue result = JsonUtil.nullable((String) null);
        assertEquals(JsonValue.NULL, result);
    }

    public void testNullable_String() {
        String input = "test";
        JsonValue result = JsonUtil.nullable(input);
        JsonValue expected = Json.createValue(input);
        assertEquals(expected, result);
    }

    public void testNullable_Integer() {
        Integer input = 123;
        JsonValue result = JsonUtil.nullable(input);
        JsonValue expected = Json.createValue(input);
        assertEquals(expected, result);
    }

    public void testNullable_Long() {
        Long input = 123L;
        JsonValue result = JsonUtil.nullable(input);
        JsonValue expected = Json.createValue(input);
        assertEquals(expected, result);
    }
}