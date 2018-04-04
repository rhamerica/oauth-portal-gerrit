package com.rhamerica.oauth.gerrit;

import com.rhamerica.oauth.gerrit.rest.SimpleRestClient;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class HttpTest {

    @Test
    public void testGet() throws IOException {
        SimpleRestClient.Response response = SimpleRestClient.requestTo(new URL("http://jenkins.rha/login?from=%2F")).get();
        String contents = response.asString();
    }

}