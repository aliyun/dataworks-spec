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

package com.aliyun.migrationx.common.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.io.ByteStreams;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sam.liux
 * @date 2019/12/18
 */
public class ZipUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipUtils.class);

    private static final String SUFFIX_TAR_GZ = ".tar.gz";
    private static final String SUFFIX_GZIP = ".gzip";
    private static final String SUFFIX_TGZ = ".tgz";
    private static final String SUFFIX_ZIP = ".zip";
    private static final String SUFFIX_TAR = ".tar";
    private static final String SUFFIX_GZ = ".gz";
    private static final String TYPE_TGZ = "tgz";
    private static final String TYPE_TAR = "tar";
    private static final String TYPE_ZIP = "zip";
    private static final String TYPE_GZIP = "gzip";

    public static File zipDir(File zipPath, File zipFile) throws IOException {
        if (!zipFile.getParentFile().exists() && !zipFile.getParentFile().mkdirs()) {
            LOGGER.error(String.format("Zip File [%s] Failed", zipPath));
            throw new RuntimeException("Zip File Error");
        }

        if (zipFile.exists()) {
            FileUtils.forceDelete(zipFile);
        }

        try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(Files.newOutputStream(zipFile.toPath()))) {
            zipDirectory(zipPath, zipArchiveOutputStream, "");
        } catch (Exception e) {
            LOGGER.error("zip directory error: {}, exception: ", zipPath, e);
            throw new RuntimeException("zip file error", e);
        }

        if (!zipFile.exists()) {
            throw new RuntimeException("zip file failed, zip not found: " + zipFile.getAbsolutePath());
        }
        return zipFile;
    }

    private static void zipDirectory(File zipPath, ZipArchiveOutputStream zipArchiveOutputStream, String parentDir)
            throws IOException {
        if (zipPath == null || !zipPath.isDirectory()) {
            return;
        }

        File[] subFiles = zipPath.listFiles();
        if (subFiles == null || subFiles.length == 0) {
            zipArchiveOutputStream.putArchiveEntry(new ZipArchiveEntry(parentDir + "/"));
            zipArchiveOutputStream.closeArchiveEntry();
            return;
        }

        for (File subFile : subFiles) {
            if (subFile.isDirectory()) {
                zipDirectory(subFile, zipArchiveOutputStream, parentDir + subFile.getName() + "/");
            } else {
                try (InputStream fileIns = new BufferedInputStream(Files.newInputStream(subFile.toPath()))) {
                    zipArchiveOutputStream.putArchiveEntry(new ZipArchiveEntry(parentDir + subFile.getName()));
                    byte[] bytes = new byte[4096];
                    int readBytes;
                    while ((readBytes = fileIns.read(bytes)) > 0) {
                        zipArchiveOutputStream.write(bytes, 0, readBytes);
                    }
                } catch (Exception e) {
                    LOGGER.error("zip file {} failed", subFile);
                    throw new RuntimeException("zip file error: " + subFile, e);
                } finally {
                    zipArchiveOutputStream.closeArchiveEntry();
                }
            }
        }
    }

    public static File decompress(File packageFile) throws IOException {
        return decompress(packageFile, TYPE_ZIP);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File decompress(File packageFile, String defaultType) throws IOException {
        File localDir = new File(packageFile.getAbsolutePath().replaceAll("\\.\\w+$", ""));
        if (localDir.equals(packageFile)) {
            localDir = new File(packageFile.getParentFile(), localDir.getName() + "_unzipped");
        }

        if (!localDir.getParentFile().exists()) {
            localDir.getParentFile().mkdirs();
        }
        LOGGER.info("unzip export file to: {}", localDir);
        LOGGER.info("force delete dir: {} ", localDir);
        if (localDir.exists()) {
            FileUtils.forceDelete(localDir);
        }

        File dirPath = localDir;
        ArchiveInputStream archiveInput = null;
        try {
            archiveInput = createArchiveInputByType(packageFile.getAbsolutePath(),
                    inferType(packageFile.getName(), defaultType));
            ArchiveEntry entry = archiveInput == null ? null : archiveInput.getNextEntry();
            while (entry != null) {
                File file = new File(dirPath, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else if (!file.isHidden()) {
                    file.getParentFile().mkdirs();
                    ByteStreams.copy(archiveInput, Files.newOutputStream(file.toPath()));
                }
                entry = archiveInput.getNextEntry();
            }
        } catch (ArchiveException | CompressorException | IOException e) {
            LOGGER.error("decompress file " + packageFile + " failed:", e);
            throw new RuntimeException(e);
        } finally {
            if (archiveInput != null) {
                try {
                    archiveInput.close();
                } catch (IOException ex) {
                    LOGGER.error("close archive exception: " + ex.getMessage());
                }
            }
        }
        return dirPath;
    }

    private static ArchiveInputStream createArchiveInputByType(String packageFile, String type)
            throws IOException, CompressorException, ArchiveException {
        ArchiveInputStream archiveInput;

        Path path = Paths.get(packageFile);
        if (TYPE_TGZ.equals(type)) {
            archiveInput = new ArchiveStreamFactory().createArchiveInputStream(
                    ArchiveStreamFactory.TAR,
                    new CompressorStreamFactory().createCompressorInputStream(
                            CompressorStreamFactory.GZIP,
                            Files.newInputStream(path)));
            return archiveInput;
        }

        if (TYPE_ZIP.equals(type)) {
            archiveInput = new ArchiveStreamFactory().createArchiveInputStream(
                    ArchiveStreamFactory.ZIP,
                    Files.newInputStream(path));
            return archiveInput;
        }

        if (TYPE_GZIP.equals(type)) {
            archiveInput = new ArchiveStreamFactory().createArchiveInputStream(
                    ArchiveStreamFactory.ZIP,
                    new CompressorStreamFactory().createCompressorInputStream(
                            CompressorStreamFactory.GZIP,
                            Files.newInputStream(path)
                    ));
            return archiveInput;
        }

        if (TYPE_TAR.equals(type)) {
            archiveInput = new ArchiveStreamFactory().createArchiveInputStream(
                    ArchiveStreamFactory.TAR,
                    Files.newInputStream(path)
            );
            return archiveInput;
        }

        return null;
    }

    private static String inferType(String packageFile, String defaultType) {
        if (packageFile.endsWith(SUFFIX_TAR_GZ)) {
            return TYPE_TGZ;
        }

        if (packageFile.endsWith(SUFFIX_GZIP)) {
            return TYPE_GZIP;
        }

        if (packageFile.endsWith(SUFFIX_TGZ)) {
            return TYPE_TGZ;
        }

        if (packageFile.endsWith(SUFFIX_TAR)) {
            return TYPE_TAR;
        }

        if (packageFile.endsWith(SUFFIX_ZIP)) {
            return TYPE_ZIP;
        }

        if (packageFile.endsWith(SUFFIX_GZ)) {
            return TYPE_GZIP;
        }

        return defaultType;
    }
}
