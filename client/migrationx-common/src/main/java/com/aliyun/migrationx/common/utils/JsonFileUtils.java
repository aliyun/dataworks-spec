package com.aliyun.migrationx.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;

public class JsonFileUtils {
    public static List<JsonNode> readJsonLines(final File file) throws IOException {
        try (InputStream in = openInputStream(file)) {
            return readJsonLines(in, null);
        }
    }

    public static List<JsonNode> readJsonLines(final File file, String key) throws IOException {
        try (InputStream in = openInputStream(file)) {
            return readJsonLines(in, key);
        }
    }

    public static List<JsonNode> readJsonLines(final InputStream input, String key) throws IOException {
        return readJsonLines(input, key, StandardCharsets.UTF_8);
    }

    public static List<JsonNode> readJsonLines(final InputStream input, String key, final Charset charset) throws IOException {
        final InputStreamReader reader = new InputStreamReader(input, Charsets.toCharset(charset));
        return readJsonLines(reader, key);
    }

    public static FileInputStream openInputStream(final File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canRead() == false) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

    public static List<JsonNode> readJsonLines(final Reader input, String key) throws IOException {
        final BufferedReader reader = toBufferedReader(input);
        final List<JsonNode> list = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            JsonNode jsonNode = JSONUtils.toJsonNode(line);
            if (StringUtils.isNotEmpty(key)) {
                if (jsonNode.has(key)) {
                    JsonNode taskNode = jsonNode.get(key);
                    list.add(taskNode);
                }
            } else {
                list.add(jsonNode);
            }

            line = reader.readLine();
        }
        return list;
    }

    public static BufferedReader toBufferedReader(final Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }
}
