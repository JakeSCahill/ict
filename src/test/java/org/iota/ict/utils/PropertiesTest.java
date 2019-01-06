package org.iota.ict.utils;

import org.iota.ict.Ict;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;

public class PropertiesTest {


    @Test
    public void when_encode_and_decode_as_json_then_keep_properties() {
        // given
        Properties original = createRandomProperties();

        // when
        JSONObject encoded = original.toJSON();
        Properties decoded = Properties.fromJSON(encoded);

        // then
        Assert.assertEquals("JSON encoding-decoding results in different properties.", original, decoded);
    }

    private static Properties createRandomProperties() {
        Properties properties = new Properties();
        properties.port = 1 + (int)(Math.random() * 10000);
        properties.guiEnabled = Math.random() < 0.5;
        properties.name = Trytes.randomSequenceOfLength(10);
        properties.neighbors.add(new InetSocketAddress("example.org", 1337));
        properties.neighbors.add(new InetSocketAddress("123.4.5.678", 14265));
        return properties;
    }
}