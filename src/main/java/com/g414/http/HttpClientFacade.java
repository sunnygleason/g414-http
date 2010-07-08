/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.g414.http;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Encapsulates HTTP client operations in a streamlined interface using the
 * Builder pattern
 */
public interface HttpClientFacade {
    /** for internal use only: the method by which requests should be submitted */
    public enum RequestSubmissionType {
        GET, POST, MULTIPART
    }

    /** Treats the request as a "read" request, that is, no content returned */
    public ReadRequest asReadRequest();

    /** Treats the request as a "fetch" request where content is returned */
    public FetchRequest asFetchRequest();

    /** Treats the request as a "match" request where only a boolean is returned */
    public MatchRequest asMatchRequest();

    /**
     * Core HTTP Request interface following builder pattern: parameterized type
     * along the lines of what the request is expected to return.
     */
    public interface HttpRequest<U> {
        /** Execute the request and return result */
        public U execute() throws IOException;

        /** Set remote URL to request */
        public HttpRequest<U> withUrl(String url);

        /** Set a single HTTP header */
        public HttpRequest<U> withHeader(String name, String value);

        /** Set all HTTP headers (clears existing headers) */
        public HttpRequest<U> withHeaders(Map<String, String> headers);

        /** Sets the HTTP POST body */
        public HttpRequest<U> withPostRequest(String postRequest);

        /** Sets the HTTP POST body */
        public HttpRequest<U> withPostRequest(byte[] postRequest);

        /** Sets the HTTP Request method */
        public HttpRequest<U> withRequestMethod(HttpRequestMethod method);

        /** Adds a simple multipart form field for submission */
        public HttpRequest<U> withMultipartFormField(String name, String value);

        /** Adds a File multipart form field for submission */
        public HttpRequest<U> withMultipartFormField(String name,
                String mimeType, File input);

        /** Sets whether to follow redirects */
        public void setFollowRedirects(boolean follow);

        /** Adds a text MIME type to accept as response */
        public void addTextType(String texttype);

        /** Whether to follow redirects */
        public boolean isFollowRedirects();

        /** Gets cookies from result */
        public String[] getCookieValuesByName(String name);

        /** Gets response header from result */
        public String[] getResponseHeader(String name);

        /** Gets response code from result */
        public int getResponseCode();

        /** Gets content size of result */
        public int getContentSize();

        /** Gets content of result as a StringBuilder */
        public StringBuilder getResponseBuffer();
    }

    /** FetchRequests return a StringBuilder with content data */
    public interface FetchRequest extends HttpRequest<StringBuilder> {
    }

    /** ReadRequests return an Integer content length */
    public interface ReadRequest extends HttpRequest<Integer> {
    }

    /** MatchRequests return an boolean match result */
    public interface MatchRequest extends HttpRequest<Boolean> {
        public MatchRequest withRegex(String regex);
    }
}
