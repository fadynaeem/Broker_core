package com.example.payment.config;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.*;
public class CachedBodyRequestWrapper extends HttpServletRequestWrapper {
    private final byte[] cachedBody;
    public CachedBodyRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = request.getInputStream().readAllBytes();
    }
    public byte[] getCachedBody() { return cachedBody; }
    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream stream = new ByteArrayInputStream(cachedBody);
        return new ServletInputStream() {
            public int read() { return stream.read(); }
            public boolean isFinished() { return stream.available() == 0; }
            public boolean isReady() { return true; }
            public void setReadListener(ReadListener rl) {}
        };
    }
    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }
}
