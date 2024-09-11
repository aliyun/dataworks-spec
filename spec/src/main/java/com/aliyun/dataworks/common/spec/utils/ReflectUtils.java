/*
 * Copyright (c) 2024, Alibaba Cloud;
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aliyun.dataworks.common.spec.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author sam.liux
 * @date 2019/12/05
 */
@SuppressWarnings("unchecked")
@Slf4j
public class ReflectUtils {
    /**
     * 递归获取父类的成员
     *
     * @param obj
     * @return
     */
    public static List<Field> getPropertyFields(Object obj) {
        List<Field> fields = new ArrayList<>();
        if (obj == null || ClassUtils.isPrimitiveOrWrapper(obj.getClass()) || obj instanceof String) {
            return fields;
        }

        getPropertyFields(fields, obj.getClass());
        return fields;
    }

    private static List<String> getPossibleGetter(Field field) {
        List<String> methods = new ArrayList<>();
        String fieldName = field.getName();
        fieldName = fieldName.replaceFirst("^" + fieldName.charAt(0),
            StringUtils.upperCase(String.valueOf(fieldName.charAt(0))));
        String fieldNameWithoutIs = fieldName.startsWith("is") ? fieldName.replaceFirst("^is", "") : fieldName;
        fieldNameWithoutIs = fieldNameWithoutIs.replaceFirst("^" + fieldName.charAt(0),
            StringUtils.upperCase(String.valueOf(fieldName.charAt(0))));
        methods.add("get" + fieldName);
        methods.add("is" + fieldName);
        methods.add("is" + fieldNameWithoutIs);
        methods.add("get" + fieldNameWithoutIs);
        return methods.stream().distinct().collect(Collectors.toList());
    }

    private static List<String> getPossibleSetter(Field field) {
        List<String> methods = new ArrayList<>();
        String fieldName = field.getName();
        fieldName = fieldName.replaceFirst("^" + fieldName.charAt(0),
            StringUtils.upperCase(String.valueOf(fieldName.charAt(0))));
        String fieldNameWithoutIs = fieldName.startsWith("is") ? fieldName.replaceFirst("^is", "") : fieldName;
        fieldNameWithoutIs = fieldNameWithoutIs.replaceFirst("^" + fieldName.charAt(0),
            StringUtils.upperCase(String.valueOf(fieldName.charAt(0))));
        methods.add("set" + fieldName);
        methods.add("set" + fieldNameWithoutIs);
        return methods.stream().distinct().collect(Collectors.toList());
    }

    private static void getPropertyFields(List<Field> fields, Class<?> clz) {
        if (clz.equals(Object.class) || ClassUtils.isPrimitiveOrWrapper(clz) || clz.equals(String.class)) {
            return;
        }

        ArrayList<Field> declared = new ArrayList<>(Arrays.asList(clz.getDeclaredFields()));
        fields.addAll(declared.stream()
            .filter(field -> !field.getName().startsWith("this$"))
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .collect(Collectors.toList())
        );
        getPropertyFields(fields, clz.getSuperclass());
    }

    public static <T> void setObjectFieldIfNotSetOrBlank(Object res, String fieldName, T value) {
        if (res == null) {
            return;
        }

        try {
            Field field = res.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            T v = (T)field.get(res);
            if (v == null || (v instanceof String && StringUtils.isBlank((String)v))) {
                field.set(res, value);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.warn("set field {} failed for object type: {}, exception: {}", fieldName, res.getClass(), e.getMessage());
        }
    }

    public static <T> T getObjectField(Object res, String fieldName) {
        if (res == null) {
            return null;
        }

        try {
            Field field = res.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T)field.get(res);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return null;
    }

    public static Field getField(Object dto, String name) {
        Field field = null;
        try {
            field = dto.getClass().getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            List<Field> fields = ReflectUtils.getPropertyFields(dto);
            if (CollectionUtils.isNotEmpty(fields)) {
                field = fields.stream().filter(f -> f.getName().equalsIgnoreCase(name)).findAny().orElse(null);
            }
        }

        if (field == null) {
            return null;
        }

        field.setAccessible(true);
        return field;
    }

    public static <T> T getFieldValue(Object dto, String name) {
        Field field = null;
        try {
            field = dto.getClass().getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            List<Field> fields = ReflectUtils.getPropertyFields(dto);
            if (CollectionUtils.isNotEmpty(fields)) {
                field = fields.stream().filter(f -> f.getName().equalsIgnoreCase(name)).findAny().orElse(null);
            }
        }

        if (field == null) {
            return null;
        }

        try {
            field.setAccessible(true);
            return (T)field.get(dto);
        } catch (IllegalAccessException ignored) {
        }
        return null;
    }

    public static <T> void setFieldValue(Object dto, String name, T value) {
        Field field = null;
        try {
            field = dto.getClass().getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            List<Field> fields = ReflectUtils.getPropertyFields(dto);
            if (CollectionUtils.isNotEmpty(fields)) {
                field = fields.stream().filter(f -> f.getName().equalsIgnoreCase(name)).findAny().orElse(null);
            }
        }

        if (field == null) {
            return;
        }

        try {
            field.setAccessible(true);
            field.set(dto, value);
        } catch (IllegalAccessException ignored) {
        }
    }

    public static Method getUniqueMethod(Class clazz, String methodName) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (StringUtils.equals(methodName, method.getName())) {
                return method;
            }
        }
        return null;
    }

    public static Set<Class<?>> getSubTypeOf(String packageName, Class<?> supperClass) {
        Set<Class<?>> classes = new HashSet<>();
        scanPackage(packageName, supperClass::isAssignableFrom, classes);
        return SetUtils.unmodifiableSet(classes);
    }

    /**
     * scan subtype classes of specific java package
     *
     * @param packageName Package name
     * @param predicate   Predicate
     * @param resultSet   Result Set
     * @see Package#getName()
     */
    public static void scanPackage(String packageName, Predicate<Class<?>> predicate, Set<Class<?>> resultSet) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Optional.ofNullable(classLoader.getResource(packageName.replaceAll("[.]", "/"))).ifPresent(url -> {
            File file = new File(url.getFile());
            if (StringUtils.equalsIgnoreCase("jar", url.getProtocol())) {
                // jar protocol
                try {
                    List<JarEntry> entities = openJarUrl(url);
                    // recursive scan package directory
                    SetUtils.emptyIfNull(listSubdirectories(entities, url.getPath())).forEach(
                        dir -> scanPackage(packageName + "." + dir, predicate, resultSet));
                    // load class type
                    entities.forEach(jarEntry -> {
                        String className = jarEntry.getName().replaceAll("/", ".");
                        if (!jarEntry.isDirectory() && jarEntry.getName().endsWith(".class") && className.startsWith(packageName)) {
                            String classFileName = className.substring(StringUtils.length(packageName) + 1);
                            resultSet.add(getClass(classFileName, packageName));
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } else {
                // file protocol
                // recursive scan package directory
                Optional.ofNullable(file.listFiles(File::isDirectory)).map(Stream::of).ifPresent(s ->
                    s.forEach(dir -> scanPackage(packageName + "." + dir.getName(), predicate, resultSet)));
                // load class type
                readClassesByStream(packageName, predicate, resultSet, url);
            }
        });
    }

    private static List<JarEntry> openJarUrl(URL url) throws IOException {
        try {
            JarURLConnection connection = (JarURLConnection)url.openConnection();
            JarFile jarFile = connection.getJarFile();
            return jarFile.stream().collect(Collectors.toList());
        } catch (IllegalStateException e) {
            log.warn("open jar failed: {}", e.getMessage());
            JarURLConnection connection = (JarURLConnection)url.openConnection();
            JarFile jarFile = connection.getJarFile();
            return jarFile.stream().collect(Collectors.toList());
        }
    }

    private static void readClassesByStream(String packageName, Predicate<Class<?>> predicate, Set<Class<?>> resultSet, URL url) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
            bufferedReader.lines().filter(line -> line.endsWith(".class"))
                .peek(line -> log.debug("line: {}", line))
                .map(classFileName -> getClass(classFileName, packageName))
                .filter(predicate)
                .forEach(resultSet::add);
        } catch (IOException e) {
            log.error("scan classes error: ", e);
            throw new RuntimeException(e);
        }
    }

    private static Set<String> listSubdirectories(List<JarEntry> entities, String jarPath) {
        Set<String> directories = new HashSet<>();
        String directoryPath = jarPath.contains("!") ? jarPath.split("!")[1] : "";
        directoryPath = StringUtils.startsWith(directoryPath, "/") ? directoryPath.substring(1) : directoryPath;
        String finalDirectoryPath = directoryPath;
        ListUtils.emptyIfNull(entities).forEach(entry -> {
            if (entry.isDirectory() && entry.getName().startsWith(finalDirectoryPath)) {
                String name = entry.getName().substring(finalDirectoryPath.length());
                name = RegExUtils.replacePattern(name, "^/", "");
                name = RegExUtils.replacePattern(name, "/$", "");
                if (StringUtils.isNotBlank(name)) {
                    // Extract the subdirectory name and add it to the set
                    directories.add(name);
                }
            }
        });
        return SetUtils.unmodifiableSet(directories);
    }

    private static Class<?> getClass(String classFileName, String packageName) {
        try {
            return Class.forName(packageName + "." + classFileName.substring(0, classFileName.lastIndexOf(".")));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
