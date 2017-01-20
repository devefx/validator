package com.devefx.validation.kit;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 * ResKit.
 */
public class ResKit {

    private static ClassLoader defaultClassLoader;
    private static Charset charset;

    public static void setDefaultClassLoader(ClassLoader defaultClassLoader) {
        ResKit.defaultClassLoader = defaultClassLoader;
    }

    public static InputStream getResourceAsStream(ClassLoader loader, String resource) throws IOException {
        InputStream in = null;
        if (loader != null) in = loader.getResourceAsStream(resource);
        if (in == null) in = ClassLoader.getSystemResourceAsStream(resource);
        if (in == null) throw new IOException("Could not find resource " + resource);
        return in;
    }

    public static InputStream getResourceAsStream(String resource) throws IOException {
        return getResourceAsStream(getClassLoader(), resource);
    }

    public static Reader getResourceAsReader(String resource) throws IOException {
        if (charset == null) {
            return new InputStreamReader(getResourceAsStream(resource));
        }
        return new InputStreamReader(getResourceAsStream(resource), charset);
    }

    public static Reader getResourceAsReader(ClassLoader loader, String resource) throws IOException {
        if (charset == null) {
            return new InputStreamReader(getResourceAsStream(loader, resource));
        }
        return new InputStreamReader(getResourceAsStream(loader, resource), charset);
    }

    public static String getResourceAsString(String resource) throws IOException {
        return getResourceAsString(getClassLoader(), resource);
    }

    public static String getResourceAsString(ClassLoader loader, String resource) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(getResourceAsReader(loader, resource));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (builder.length() != 0)
                    builder.append("\n");
                builder.append(line);
            }
        } finally {
            if (bufferedReader != null)
                bufferedReader.close();
        }
        return builder.toString();
    }

    public static InputStream getUrlAsStream(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        return conn.getInputStream();
    }
    
    public static Reader getUrlAsReader(String urlString) throws IOException {
        Reader reader;
        if (charset == null) {
            reader = new InputStreamReader(getUrlAsStream(urlString));
        } else {
            reader = new InputStreamReader(getUrlAsStream(urlString), charset);
        }
        return reader;
    }
    
    public static ClassLoader getClassLoader() {
        if (defaultClassLoader != null)
            return defaultClassLoader;
        return Thread.currentThread().getContextClassLoader();
    }

    public static Charset getCharset() {
        return charset;
    }

    public static void setCharset(Charset charset) {
        ResKit.charset = charset;
    }
}
