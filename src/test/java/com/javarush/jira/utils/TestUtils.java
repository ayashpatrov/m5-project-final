package com.javarush.jira.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class TestUtils {
    public static String readJson(String path) throws IOException, URISyntaxException {
        return Files.readString(Path.of(Objects.requireNonNull(ClassLoader.getSystemResource(path)).toURI()));
    }
}