package com.relationalai.utils;

import com.relationalai.errors.HttpError;
import com.relationalai.models.transaction.TransactionAsyncFile;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.ParameterParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.fileupload.FileUploadBase.ATTACHMENT;
import static org.apache.commons.fileupload.FileUploadBase.FORM_DATA;

public class MultipartReader {

    public static List<TransactionAsyncFile> parseMultipartResponse(HttpResponse<byte[]> response) throws HttpError, IOException {
        var output = new ArrayList<TransactionAsyncFile>();
        String contentType = response.headers().firstValue("Content-type").orElse("");
        if ("".equals(contentType)) {
            throw new HttpError(404, "missing Content-type header");
        }

        MultipartStream multipartStream = new MultipartStream(
                new ByteArrayInputStream(response.body()),
                getBoundary(contentType),
                1024,
                null
        );

        boolean nextPart = multipartStream.skipPreamble();

        while (nextPart) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            String headersString = multipartStream.readHeaders();
            var headers = getHeadersMap(headersString);

            String name = getFieldName(headers.get("Content-Disposition"));
            String filename = getFileName(headers.get("Content-Disposition"));
            String fileContentType = headers.get("Content-Type");
            multipartStream.readBodyData(out);

            output.add(new TransactionAsyncFile(name, out.toByteArray(), filename, fileContentType));

            nextPart = multipartStream.readBoundary();
        }
        return output;
    }

    protected static byte[] getBoundary(String contentType) {
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        Map<String,String> params = parser.parse(contentType, new char[] {';', ','});
        String boundaryStr = (String) params.get("boundary");

        if (boundaryStr == null) {
            return null;
        }

        return boundaryStr.getBytes(StandardCharsets.UTF_8);
    }
    private static int parseEndOfLine(String headerPart, int end) {
        int index = end;
        for (;;) {
            int offset = headerPart.indexOf('\r', index);
            if (offset == -1 || offset + 1 >= headerPart.length()) {
                throw new IllegalStateException("Expected headers to be terminated by an empty line.");
            }
            if (headerPart.charAt(offset + 1) == '\n') {
                return offset;
            }
            index = offset + 1;
        }
    }
    protected static Map<String,String> getHeadersMap(String headerPart) {
        final int len = headerPart.length();
        final Map<String,String> headers = new HashMap<String,String>();

        int start = 0;
        for (;;) {
            int end = parseEndOfLine(headerPart, start);
            if (start == end) {
                break;
            }
            String header = headerPart.substring(start, end);
            start = end + 2;
            while (start < len) {
                int nonWs = start;
                while (nonWs < len) {
                    char c = headerPart.charAt(nonWs);
                    if (c != ' '  &&  c != '\t') {
                        break;
                    }
                    ++nonWs;
                }
                if (nonWs == start) {
                    break;
                }
                // continuation line found
                end = parseEndOfLine(headerPart, nonWs);
                header += " " + headerPart.substring(nonWs, end);
                start = end + 2;
            }

            // parse header line
            final int colonOffset = header.indexOf(':');
            if (colonOffset == -1) {
                // this header line is malformed, skip it.
                continue;
            }
            String headerName = header.substring(0, colonOffset).trim();
            String headerValue = header.substring(header.indexOf(':') + 1).trim();

            if (headers.containsKey(headerName)) {
                headers.put( headerName, headers.get(headerName) + "," + headerValue );
            } else {
                headers.put(headerName, headerValue);
            }
        }

        return headers;
    }
    private static String getFieldName(String contentDisposition) {
        String fieldName = null;

        if (contentDisposition != null && contentDisposition.toLowerCase().startsWith(FORM_DATA)) {

            ParameterParser parser = new ParameterParser();
            parser.setLowerCaseNames(true);

            // parameter parser can handle null input
            Map<String,String> params = parser.parse(contentDisposition, ';');
            fieldName = (String) params.get("name");
            if (fieldName != null) {
                fieldName = fieldName.trim();
            }
        }

        return fieldName;
    }

    private static String getFileName(String contentDisposition) {
        String fileName = null;

        if (contentDisposition != null) {
            String cdl = contentDisposition.toLowerCase();

            if (cdl.startsWith(FORM_DATA) || cdl.startsWith(ATTACHMENT)) {

                ParameterParser parser = new ParameterParser();
                parser.setLowerCaseNames(true);

                // parameter parser can handle null input
                Map<String,String> params = parser.parse(contentDisposition, ';');
                if (params.containsKey("filename")) {
                    fileName = (String) params.get("filename");
                    if (fileName != null) {
                        fileName = fileName.trim();
                    } else {
                        // even if there is no value, the parameter is present,
                        // so we return an empty file name rather than no file
                        // name.
                        fileName = "";
                    }
                }
            }
        }

        return fileName;
    }
}
