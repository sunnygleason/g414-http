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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

/**
 * MultiPartForm implementation, aims to be compatible with RFC 2388.
 * 
 * Reproduced from g414-http, see http://github.com/sunnygleason/g414-http
 * 
 * @see http://www.ietf.org/rfc/rfc2388.txt
 */
public class MultiPartForm {
    protected static final String CR_LF = "\r\n";
    protected static final String DASH_DASH = "--";
    protected static final Random random = new Random();

    protected final DataOutputStream out;
    protected final String boundary;

    /**
     * Creates a new <code>MultiPartFormOutputStream</code> object using the
     * specified output stream and boundary
     */
    public MultiPartForm(OutputStream out, String boundary) {
        if (out == null) {
            throw new NullPointerException("OutputStream must not be null!");
        }

        if (boundary == null || boundary.length() == 0) {
            throw new NullPointerException("Boundary string must not be null!");
        }

        this.out = new DataOutputStream(out);
        this.boundary = boundary;
    }

    /**
     * Outputs a string value
     */
    public void outputString(String name, String value) throws IOException {
        if (name == null) {
            throw new NullPointerException("Field name must not be null!");
        }

        if (value == null) {
            value = "";
        }

        writeBoundaryHeaderWithMimeType(out, boundary, name, null, null);

        out.writeBytes(value);
        out.writeBytes(CR_LF);
        out.flush();
    }

    /**
     * Outputs a byte[] value as if it was a file
     */
    public void outputByteArray(String name, String mimeType, String fileName,
            byte[] data) throws IOException {
        if (data == null) {
            throw new NullPointerException("Data must not be null!");
        }

        if (fileName == null || fileName.length() == 0) {
            throw new NullPointerException("File name must be provided!");
        }

        writeBoundaryHeaderWithMimeType(out, boundary, name, mimeType, fileName);

        out.write(data);
        out.writeBytes(CR_LF);
        out.flush();
    }

    /**
     * Outputs contents of InputStream as if it was a File
     */
    public void outputStream(String name, String mimeType, String fileName,
            InputStream stream) throws IOException {
        if (stream == null) {
            throw new NullPointerException("InputStream must not be null!");
        }

        if (fileName == null || fileName.length() == 0) {
            throw new NullPointerException("File name must be provided!");
        }

        writeBoundaryHeaderWithMimeType(out, boundary, name, mimeType, fileName);

        byte[] data = new byte[8192];
        int bytesRead = 0;
        while ((bytesRead = stream.read(data, 0, data.length)) != -1) {
            out.write(data, 0, bytesRead);
        }

        try {
            stream.close();
        } catch (Exception ignored) {
        }

        out.writeBytes(CR_LF);
        out.flush();
    }

    /**
     * Outputs a File field value
     */
    public void outputFile(String name, String mimeType, File file)
            throws IOException {
        if (file == null) {
            throw new NullPointerException("File must not be null!");
        }

        if (!file.exists()) {
            throw new IllegalArgumentException("File must exist!");
        }

        if (file.isDirectory()) {
            throw new IllegalArgumentException("File cannot be a directory.");
        }

        outputStream(name, mimeType, file.getCanonicalPath(),
                new FileInputStream(file));
    }

    /**
     * Completes the transmission of the multipart form
     */
    public void close() throws IOException {
        out.writeBytes(DASH_DASH);
        out.writeBytes(boundary);
        out.writeBytes(DASH_DASH);
        out.writeBytes(CR_LF);
        out.flush();
        out.close();
    }

    /**
     * Creates a random boundary String
     */
    public static String createRandomBoundary() {
        return "--------------------" + Long.toString(random.nextLong(), 16);
    }

    /**
     * Creates the MIME type for the given boundary String
     */
    public static String getContentType(String boundary) {
        return "multipart/form-data; boundary=" + boundary;
    }

    /**
     * Examples:
     * 
     * <pre>
     * --boundary\r\n Content-Disposition: form-data; name="<fieldName>"\r\n
     * \r\n <value>\r\n
     * </pre>
     * 
     * <pre>
     * --boundary\r\n Content-Disposition: form-data; name="<fieldName>";
     * filename="<filename>"\r\n Content-Type: <mime-type>\r\n \r\n
     * <file-data>\r\n
     * </pre>
     */
    private static void writeBoundaryHeaderWithMimeType(DataOutputStream out,
            String boundary, String fieldName, String mimeType, String fileName)
            throws IOException {
        out.writeBytes(DASH_DASH);
        out.writeBytes(boundary);
        out.writeBytes(CR_LF);

        out.writeBytes("Content-Disposition: form-data; name=\"" + fieldName
                + "\"");

        if (fileName != null) {
            out.writeBytes("; filename=\"" + fileName + "\"");
        }

        out.writeBytes(CR_LF);

        if (mimeType != null) {
            out.writeBytes("Content-Type: " + mimeType);
            out.writeBytes(CR_LF);
        }
        out.writeBytes(CR_LF);
    }
}
