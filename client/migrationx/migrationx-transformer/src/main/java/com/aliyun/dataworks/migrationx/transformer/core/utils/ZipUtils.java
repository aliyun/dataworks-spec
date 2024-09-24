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

package com.aliyun.dataworks.migrationx.transformer.core.utils;

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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author sam.liux
 * @date 2019/12/18
 */
public class ZipUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipUtils.class);

    private static String SUFFIX_TAR_GZ = ".tar.gz";
    private static String SUFFIX_GZIP = ".gzip";
    private static String SUFFIX_TGZ = ".tgz";
    private static String SUFFIX_ZIP = ".zip";
    private static String SUFFIX_TAR = ".tar";
    private static String SUFFIX_GZ = ".gz";
    private static String TYPE_TGZ = "tgz";
    private static String TYPE_TAR = "tar";
    private static String TYPE_ZIP = "zip";
    private static String TYPE_GZIP = "gzip";

    public static File unzipExportFile(File localFile) throws IOException {
        File localDir = new File(localFile.getAbsolutePath().replaceAll("\\.\\w+$", ""));
        if (!localDir.getParentFile().exists()) {
            localDir.getParentFile().mkdirs();
        }
        LOGGER.info("unzip export file to: {}", localDir);
        LOGGER.info("force delete dir: {} ", localDir);
        if (localDir.exists()) {
            FileUtils.forceDelete(localDir);
        }

        File unzippedDir = localDir;
        byte[] buffer = new byte[10240];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(localFile));
        ZipEntry zipEntry = zis.getNextEntry();
        try {
            while (zipEntry != null) {
                File newFile = new File(localDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    FileUtils.forceMkdir(newFile);
                    zipEntry = zis.getNextEntry();
                    continue;
                } else {
                    if (!newFile.getParentFile().exists()) {
                        newFile.getParentFile().mkdirs();
                    }

                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    zipEntry = zis.getNextEntry();
                }
            }
        } finally {
            zis.closeEntry();
            zis.close();
        }
        return unzippedDir;
    }

    /**
     * @param zipDir
     * @param zipFile
     * @return
     * @see ZipUtils
     * @deprecated
     */
    public static File _zipDir(File zipDir, File zipFile) {
        zipDeploymentPackage(zipDir.getAbsolutePath(), zipFile.getAbsolutePath(), "gbk");
        if (zipFile.exists() == false) {
            throw new RuntimeException("zip file failed, zip not found: " + zipFile.getAbsolutePath());
        }
        return zipFile;
    }

    /**
     * 将生成好的发布包，包括xml文件，打包成zip包 在执行发布包的时候进行zip操作，创建发布包只是生成发布包目录
     *
     * @param deploymentPath 发布包路径， 最后目录应为 唯一标识，tenantId_projectId_deploymentId_timestamp? 可能不需要timestamp
     * @param zipPath        zip包生成路径
     */
    public static void zipDeploymentPackage(String deploymentPath, String zipPath, String encoding) {
        File zipFile = new File(zipPath);
        if (!zipFile.getParentFile().exists() && !zipFile.getParentFile().mkdirs()) {
            LOGGER.error(String.format("Zip File [%s] Failed", zipPath));
            throw new RuntimeException("Zip File Error");
        }
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(new File(zipPath)));
            zos.setEncoding(encoding);
            handleDir(new File(deploymentPath), zos, "");
        } catch (Exception e) {
            LOGGER.error(String.format("File path [%s] not valid", zipPath), e);
            throw new RuntimeException("zip file error", e);
        } finally {
            IOUtils.closeQuietly(zos);
        }
    }

    private static void handleDir(File file, ZipOutputStream zos, String parentDir) {
        handleDir(file, zos, parentDir, new byte[4096]);
    }

    /**
     * 递归遍历目录并添加到zip文件中 需要考虑到大文件的压缩，等待测试
     */
    private static void handleDir(File file, ZipOutputStream zos, String parentDir, byte[] zipInputBuf) {
        File[] fileList = file.listFiles();
        // 如果路径不为目录，异常
        if (fileList == null) {
            LOGGER.error(String.format("may be path [%s] is not directory or I/O error occurs",
                file.getAbsolutePath()));
            throw new RuntimeException("zip file path error");
        }

        try {
            if (fileList.length == 0) {
                // 空目录，直接压缩到文件中
                zos.putNextEntry(new org.apache.tools.zip.ZipEntry(parentDir + "/"));
                zos.closeEntry();
            } else {
                // 非空目录,递归压缩
                int readBytes = 0;
                for (File subFile : fileList) {
                    if (subFile.isDirectory()) {
                        // 目录，递归调用
                        handleDir(subFile, zos, parentDir + subFile.getName() + "/", zipInputBuf);
                    } else {
                        // 文件，压缩
                        InputStream fileIns = new BufferedInputStream(new FileInputStream(subFile));
                        try {
                            zos.putNextEntry(new org.apache.tools.zip.ZipEntry(parentDir + subFile.getName()));

                            while ((readBytes = fileIns.read(zipInputBuf)) > 0) {
                                zos.write(zipInputBuf, 0, readBytes);
                            }
                        } catch (Exception e) {
                            LOGGER.error("zip file create failed");
                            throw new RuntimeException("zip file create failed", e);
                        } finally {
                            fileIns.close();
                            zos.closeEntry();
                        }

                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Creating Zip File Failed");
            throw new RuntimeException("zip file create failed", e);
        }
    }

    /**
     * 解压缩zip包，得到执行的发布包，并解析
     *
     * @param zipPath        zip包的路径
     * @param deploymentPath 解压缩目标的发布包路径，压缩文件解压缩到该目录
     */
    public static void unzipDeploymentPackage(String zipPath, String deploymentPath, String encoding) {
        InputStream zipInputStream = null;
        OutputStream fileOutputStream = null;
        int readBytes = 0;
        // 解压文件的缓冲区，后面可以考虑提到配置文件中
        byte[] byteBuf = new byte[4096];

        try {
            ZipFile zipFile = new ZipFile(zipPath, encoding);
            for (Enumeration entries = zipFile.getEntries(); entries.hasMoreElements(); ) {
                org.apache.tools.zip.ZipEntry zipEntry = (org.apache.tools.zip.ZipEntry)entries.nextElement();
                File file = new File(deploymentPath + "/" + zipEntry.getName());

                if (zipEntry.isDirectory()) {
                    // 当前解压项目为目录,新建目录 判断不能合并，否则会报拒绝访问错误
                    if (!file.mkdirs()) {
                        LOGGER.error(String.format("Make Dirs [%s] Failed", file.getName()));
                        throw new RuntimeException("unzip file failed");
                    } else {
                        continue;
                    }
                } else {
                    File parentFile = file.getParentFile();
                    // 判断文件的父目录是否在，不在则新建
                    if (!parentFile.exists() && !parentFile.mkdirs()) {
                        LOGGER.error(String.format("Make Dirs [%s] Failed", parentFile.getName()));
                        throw new RuntimeException("unzip file failed");
                    }
                    try {
                        // 创建输出流
                        zipInputStream = zipFile.getInputStream(zipEntry);
                        fileOutputStream = new FileOutputStream(file);
                        // 按字节数组大小读取
                        while ((readBytes = zipInputStream.read(byteBuf)) > 0) {
                            fileOutputStream.write(byteBuf, 0, readBytes);
                        }
                    } catch (Exception e) {
                        LOGGER.error(String.format("unzip file [%s] failed", zipPath));
                        throw new RuntimeException("unzip file failed", e);
                    } finally {
                        // 关闭文件和zip流
                        IOUtils.closeQuietly(fileOutputStream);
                        IOUtils.closeQuietly(zipInputStream);
                    }
                }
            }
            zipFile.close();
        } catch (IOException e) {
            LOGGER.error(String.format("Unzip File [%s] failed", zipPath));
            throw new RuntimeException("unzip file failed", e);
        }
    }

    public static File unzipExportFile(File zipFile, File targetDir) throws IOException {
        LOGGER.info("unzip export file to: {}", targetDir);
        if (targetDir.exists() && !targetDir.isDirectory()) {
            targetDir = targetDir.getParentFile();
        }

        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        File unzippedDir = targetDir;
        byte[] buffer = new byte[10240];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry zipEntry = zis.getNextEntry();
        try {
            while (zipEntry != null) {
                File newFile = new File(unzippedDir, zipEntry.getName());
                if (!newFile.getParentFile().exists()) {
                    newFile.getParentFile().mkdirs();
                }

                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
        } finally {
            zis.closeEntry();
            zis.close();
        }
        return unzippedDir;
    }

    private static ArchiveInputStream createArchiveInputByType(String packageFile, String type)
        throws FileNotFoundException, CompressorException, ArchiveException {
        ArchiveInputStream archiveInput;

        if (TYPE_TGZ.equals(type)) {
            archiveInput = new ArchiveStreamFactory().createArchiveInputStream(
                ArchiveStreamFactory.TAR,
                new CompressorStreamFactory().createCompressorInputStream(
                    CompressorStreamFactory.GZIP,
                    new FileInputStream(packageFile)));
            return archiveInput;
        }

        if (TYPE_ZIP.equals(type)) {
            archiveInput = new ArchiveStreamFactory().createArchiveInputStream(
                ArchiveStreamFactory.ZIP,
                new FileInputStream(packageFile));
            return archiveInput;
        }

        if (TYPE_GZIP.equals(type)) {
            archiveInput = new ArchiveStreamFactory().createArchiveInputStream(
                ArchiveStreamFactory.ZIP,
                new CompressorStreamFactory().createCompressorInputStream(
                    CompressorStreamFactory.GZIP,
                    new FileInputStream(packageFile)
                ));
            return archiveInput;
        }

        if (TYPE_TAR.equals(type)) {
            archiveInput = new ArchiveStreamFactory().createArchiveInputStream(
                ArchiveStreamFactory.TAR,
                new FileInputStream(packageFile)
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

    public static File decompress(File packageFile) throws IOException {
        return decompress(packageFile, TYPE_ZIP);
    }

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
                    ByteStreams.copy(archiveInput, new FileOutputStream(file));
                }
                entry = archiveInput.getNextEntry();
            }
        } catch (ArchiveException | CompressorException e) {
            LOGGER.error(ExceptionUtils.getFullStackTrace(e));
        } catch (FileNotFoundException e) {
            LOGGER.error("decompress file " + packageFile + " failed:", e);
        } catch (IOException e) {
            LOGGER.error("decompress file " + packageFile + " exception: ", e);
            LOGGER.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            if (archiveInput != null) {
                try {
                    archiveInput.close();
                } catch (IOException e) {
                    LOGGER.error("close archive exception: " + e.getMessage());
                }
            }
        }
        return dirPath;
    }

    public static File zipDir(File zipPath, File zipFile) throws IOException {
        if (!zipFile.getParentFile().exists() && !zipFile.getParentFile().mkdirs()) {
            LOGGER.error(String.format("Zip File [%s] Failed", zipPath));
            throw new RuntimeException("Zip File Error");
        }

        if (zipFile.exists()) {
            FileUtils.forceDelete(zipFile);
        }

        try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(
            new FileOutputStream(zipFile))) {
            zipDirectory(zipPath, zipArchiveOutputStream, "");
        } catch (Exception e) {
            LOGGER.error("zip directory error: {}, exception: ", zipPath, e);
            throw new RuntimeException("zip file error", e);
        }

        if (zipFile.exists() == false) {
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
                try (InputStream fileIns = new BufferedInputStream(new FileInputStream(subFile))) {
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
}
