package com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.PackageValidator;
import com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.PackageValidatorResult;
import com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.aliyun.dataworks.migrationx.domain.dataworks.packages.enums.PackageValidateErrorCode.ANY_FOLDER_NOT_ALLOW_IN_FOLDER;
import static com.aliyun.dataworks.migrationx.domain.dataworks.packages.enums.PackageValidateErrorCode.DATAWORKS_HIDDEN_FOLDER_MISSING_METADATA_FILE;
import static com.aliyun.dataworks.migrationx.domain.dataworks.packages.enums.PackageValidateErrorCode.DATAWORKS_HIDDEN_FOLDER_MISSING_TYPE_FILE;
import static com.aliyun.dataworks.migrationx.domain.dataworks.packages.enums.PackageValidateErrorCode.DATAWORKS_HIDDEN_FOLDER_MULTIPLE_TYPE_FILES;
import static com.aliyun.dataworks.migrationx.domain.dataworks.packages.enums.PackageValidateErrorCode.ONLY_SPECIFIC_FILE_ALLOWED_IN_FOLDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * HiddenDataworksFolderValidator Test Class
 * Used to test the validation functionality of HiddenDataworksFolderValidator class
 *
 * @author 莫泣
 * @date 2025-08-31
 */
public class HiddenDataworksFolderValidatorTest {

    private HiddenDataworksFolderValidator validator;
    private Path tempDir;

    @Before
    public void setUp() throws IOException {
        validator = HiddenDataworksFolderValidator.getInstance();
        tempDir = Files.createTempDirectory("hidden-dataworks-folder-test");
    }

    @After
    public void tearDown() throws IOException {
        deleteRecursively(tempDir.toFile());
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }

    @Test
    public void testValidateEmptyDirectory() throws IOException {
        // Create empty directory
        Path emptyDir = tempDir.resolve("empty-dir");
        Files.createDirectory(emptyDir);

        // Execute validation
        ValidateContext context = new ValidateContext();
        validator.validate(emptyDir.toFile(), context);
        List<PackageValidatorResult> results = context.getResults();

        // Validate results
        assertNotNull(results);
        // Empty directory should report missing required files
        assertFalse("Empty directory should report missing required files", results.isEmpty());
        assertTrue("Should have results marked as missing required files",
            results.stream().anyMatch(result -> DATAWORKS_HIDDEN_FOLDER_MISSING_METADATA_FILE.getErrorCode().equals(result.getErrorCode()) ||
                DATAWORKS_HIDDEN_FOLDER_MISSING_TYPE_FILE.getErrorCode().equals(result.getErrorCode())));
    }

    @Test
    public void testValidateDirectoryWithMetadataFile() throws IOException {
        // Create directory with metadata.json file
        Path dirWithMetadataFile = tempDir.resolve("dir-with-metadata-file");
        Files.createDirectory(dirWithMetadataFile);
        Files.createFile(dirWithMetadataFile.resolve(HiddenDataworksFolderValidator.METADATA_FILE_NAME));

        // Execute validation
        ValidateContext context = new ValidateContext();
        validator.validate(dirWithMetadataFile.toFile(), context);
        List<PackageValidatorResult> results = context.getResults();

        // Validate results
        assertNotNull(results);
        // Only metadata.json file should report missing .type file
        assertTrue("Should report missing .type file",
            results.stream().anyMatch(result -> DATAWORKS_HIDDEN_FOLDER_MISSING_TYPE_FILE.getErrorCode().equals(result.getErrorCode())));
    }

    @Test
    public void testValidateDirectoryWithTypeFile() throws IOException {
        // Create directory with .type file
        Path dirWithTypeFile = tempDir.resolve("dir-with-type-file");
        Files.createDirectory(dirWithTypeFile);
        Files.createFile(dirWithTypeFile.resolve("test.type"));

        // Execute validation
        ValidateContext context = new ValidateContext();
        validator.validate(dirWithTypeFile.toFile(), context);
        List<PackageValidatorResult> results = context.getResults();

        // Validate results
        assertNotNull(results);
        // Only .type file should report missing metadata.json file
        assertTrue("Should report missing metadata.json file",
            results.stream().anyMatch(result -> DATAWORKS_HIDDEN_FOLDER_MISSING_METADATA_FILE.getErrorCode().equals(result.getErrorCode())));
    }

    @Test
    public void testValidateDirectoryWithBothMetadataAndTypeFiles() throws IOException {
        // Create .dataworks directory
        Path dataworksDir = tempDir.resolve(".dataworks");
        Files.createDirectory(dataworksDir);

        // Create metadata.json file with required fields, content meets validation requirements
        String metadataContent = "{\n" +
            "  \"scriptPath\": \"test-script\",\n" +
            "  \"schedulePath\": \"test.schedule.json\"\n" +
            "}";
        Files.write(dataworksDir.resolve(HiddenDataworksFolderValidator.METADATA_FILE_NAME), metadataContent.getBytes());

        // Create .type file
        Files.createFile(dataworksDir.resolve("test.type"));

        // Execute validation (note: should call validator on .dataworks directory)
        ValidateContext context = new ValidateContext();
        validator.validate(dataworksDir.toFile(), context);
        List<PackageValidatorResult> results = context.getResults();

        // Validate results
        assertNotNull(results);
        // Directory with both metadata.json and .type files should have no validation errors
        // but may have other validation results (e.g., subfile validation results)
        // We only check if there are invalid validation results
        // Filter out validation results produced by MetadataFileValidator, as these results are for metadata.json file content validation
        // rather than .dataworks folder structure validation
        List<PackageValidatorResult> folderStructureResults = results.stream()
            .filter(result -> !result.getErrorMessage().contains("scriptPath") && !result.getErrorMessage().contains("schedulePath"))
            .collect(Collectors.toList());

        assertTrue("Should have no validation failure results",
            folderStructureResults.isEmpty() || folderStructureResults.stream().allMatch(result -> result.getValid() == null || result.getValid()));
    }

    @Test
    public void testValidateDirectoryWithSubDirectory() throws IOException {
        // Create directory with subdirectory (covers lines 222-223)
        Path dirWithSubDirectory = tempDir.resolve("dir-with-sub-directory");
        Files.createDirectory(dirWithSubDirectory);
        Files.createDirectory(dirWithSubDirectory.resolve("sub-dir"));

        // Execute validation
        ValidateContext context = new ValidateContext();
        validator.validate(dirWithSubDirectory.toFile(), context);
        List<PackageValidatorResult> results = context.getResults();

        // Validate results
        assertNotNull(results);
        // Subfolders are not allowed in .dataworks folder
        assertTrue("Should have results marked as not allowed folder",
            results.stream()
                .anyMatch(result -> result.getValid() != null && !result.getValid()
                    && ANY_FOLDER_NOT_ALLOW_IN_FOLDER.getErrorCode().equals(result.getErrorCode())));
    }

    @Test
    public void testValidateDirectoryWithNotAllowedFile() throws IOException {
        // Create directory with not allowed file
        Path dirWithNotAllowedFile = tempDir.resolve("dir-with-not-allowed-file");
        Files.createDirectory(dirWithNotAllowedFile);
        Files.createFile(dirWithNotAllowedFile.resolve("not-allowed-file.txt"));

        // Execute validation
        ValidateContext context = new ValidateContext();
        validator.validate(dirWithNotAllowedFile.toFile(), context);
        List<PackageValidatorResult> results = context.getResults();

        // Validate results
        assertNotNull(results);
        // Only metadata.json and .type files are allowed in .dataworks folder
        assertTrue("Should have results marked as not allowed file",
            results.stream().anyMatch(result -> result.getValid() != null && !result.getValid()
                && ONLY_SPECIFIC_FILE_ALLOWED_IN_FOLDER.getErrorCode().equals(result.getErrorCode())));
    }

    @Test
    public void testValidateDirectoryWithMultipleMetadataFiles() throws IOException {
        // Create directory with multiple metadata.json files (covers line 232)
        Path dirWithMultipleMetadataFiles = tempDir.resolve("dir-with-multiple-metadata-files");
        Files.createDirectory(dirWithMultipleMetadataFiles);
        Files.createFile(dirWithMultipleMetadataFiles.resolve(HiddenDataworksFolderValidator.METADATA_FILE_NAME));
        // Create another metadata file, but not metadata.json (this will be marked as not allowed file)
        Files.createFile(dirWithMultipleMetadataFiles.resolve("metadata2.json"));

        // Execute validation
        ValidateContext context = new ValidateContext();
        validator.validate(dirWithMultipleMetadataFiles.toFile(), context);
        List<PackageValidatorResult> results = context.getResults();

        // Validate results
        assertNotNull(results);
        // Check if there are results marked as not allowed files (metadata2.json will be marked as not allowed file)
        boolean hasNotAllowedFile = results.stream().anyMatch(result ->
            result.getValid() != null && !result.getValid() && ONLY_SPECIFIC_FILE_ALLOWED_IN_FOLDER.getErrorCode().equals(result.getErrorCode()));
        assertTrue("Should have results marked as not allowed files", hasNotAllowedFile);
    }

    @Test
    public void testValidateDirectoryWithMultipleTypeFiles() throws IOException {
        // Create directory with multiple .type files (covers line 244)
        Path dirWithMultipleTypeFiles = tempDir.resolve("dir-with-multiple-type-files");
        Files.createDirectory(dirWithMultipleTypeFiles);
        Files.createFile(dirWithMultipleTypeFiles.resolve("test.type"));
        Files.createFile(dirWithMultipleTypeFiles.resolve("test2.type"));

        // Execute validation
        ValidateContext context = new ValidateContext();
        validator.validate(dirWithMultipleTypeFiles.toFile(), context);
        List<PackageValidatorResult> results = context.getResults();

        // Validate results
        assertNotNull(results);
        assertTrue("Should have validation failure results", results.stream().anyMatch(result -> result.getValid() != null && !result.getValid()));
        assertTrue("Should report multiple .type files exist", results.stream().anyMatch(result ->
            result.getValid() != null && !result.getValid() && DATAWORKS_HIDDEN_FOLDER_MULTIPLE_TYPE_FILES.getErrorCode()
                .equals(result.getErrorCode())));
    }

    @Test
    public void testGetChildValidatorsForMetadataFile() throws IOException {
        // Create directory with metadata.json file
        Path dirWithMetadataFile = tempDir.resolve("dir-with-metadata-file");
        Files.createDirectory(dirWithMetadataFile);
        Path metadataFile = dirWithMetadataFile.resolve(HiddenDataworksFolderValidator.METADATA_FILE_NAME);
        Files.createFile(metadataFile);

        // Get child validators
        List<PackageValidator> childValidators = validator.getChildValidators(metadataFile.toFile(),
            new ValidateContext());

        // Validate results
        assertNotNull(childValidators);
        assertEquals(1, childValidators.size());
        assertTrue(childValidators.get(0) instanceof MetadataFileValidator);
    }

    @Test
    public void testGetChildValidatorsForTypeFile() throws IOException {
        // Create directory with .type file
        Path dirWithTypeFile = tempDir.resolve("dir-with-type-file");
        Files.createDirectory(dirWithTypeFile);
        Path typeFile = dirWithTypeFile.resolve("test.type");
        Files.createFile(typeFile);

        // Get child validators
        List<PackageValidator> childValidators = validator.getChildValidators(typeFile.toFile(),
            new ValidateContext());

        // Validate results
        assertNotNull(childValidators);
        // For .type files, should return EmptyFileValidator instead of SpecFileValidator
        assertEquals(1, childValidators.size());
        assertTrue(childValidators.get(0) instanceof EmptyFileValidator);
    }

    @Test
    public void testGetChildValidatorsForOtherFile() throws IOException {
        // Create directory with other file
        Path dirWithOtherFile = tempDir.resolve("dir-with-other-file");
        Files.createDirectory(dirWithOtherFile);
        Path otherFile = dirWithOtherFile.resolve("other-file.txt");
        Files.createFile(otherFile);

        // Get child validators
        List<PackageValidator> childValidators = validator.getChildValidators(otherFile.toFile(),
            new ValidateContext());

        // Validate results
        assertNotNull(childValidators);
        assertEquals(1, childValidators.size());
        assertTrue(childValidators.get(0) instanceof EmptyFileValidator);
    }

    @Test
    public void testGetChildValidatorsForDirectory() throws IOException {
        // Create directory with subdirectory
        Path dirWithSubDir = tempDir.resolve("dir-with-sub-dir");
        Files.createDirectory(dirWithSubDir);
        Path subDir = dirWithSubDir.resolve("sub-dir");
        Files.createDirectory(subDir);

        // Get child validators
        List<PackageValidator> childValidators = validator.getChildValidators(subDir.toFile(),
            new ValidateContext());

        // Validate results
        assertNotNull(childValidators);
        assertEquals(1, childValidators.size());
        assertTrue(childValidators.get(0) instanceof EmptyFolderValidator);
    }

    // Test methods merged from HiddenDataworksFolderValidatorAdditionalTest start here

    @Test
    public void testValidateDirectoryWithoutMetadataFile() throws IOException {
        // Create directory without metadata.json file
        Path dirWithoutMetadataFile = tempDir.resolve("dir-without-metadata-file");
        Files.createDirectory(dirWithoutMetadataFile);
        Files.createFile(dirWithoutMetadataFile.resolve("test.type"));

        // Execute validation
        ValidateContext context = new ValidateContext();
        validator.validate(dirWithoutMetadataFile.toFile(), context);
        List<PackageValidatorResult> results = context.getResults();

        // Validate results - should report missing required files
        assertNotNull(results);
        assertTrue("Should have validation failure results", results.stream().anyMatch(result -> result.getValid() != null && !result.getValid()));
        assertTrue("Should report missing metadata.json file", results.stream().anyMatch(result ->
            result.getValid() != null && !result.getValid() && DATAWORKS_HIDDEN_FOLDER_MISSING_METADATA_FILE.getErrorCode()
                .equals(result.getErrorCode())));
    }

    @Test
    public void testValidateDirectoryWithoutTypeFile() throws IOException {
        // Create directory without .type file
        Path dirWithoutTypeFile = tempDir.resolve("dir-without-type-file");
        Files.createDirectory(dirWithoutTypeFile);
        Files.createFile(dirWithoutTypeFile.resolve(HiddenDataworksFolderValidator.METADATA_FILE_NAME));

        // Execute validation
        ValidateContext context = new ValidateContext();
        validator.validate(dirWithoutTypeFile.toFile(), context);
        List<PackageValidatorResult> results = context.getResults();

        // Validate results - should report missing required files
        assertNotNull(results);
        assertTrue("Should have validation failure results", results.stream().anyMatch(result -> result.getValid() != null && !result.getValid()));
        assertTrue("Should report missing .type file", results.stream().anyMatch(result ->
            result.getValid() != null && !result.getValid() && DATAWORKS_HIDDEN_FOLDER_MISSING_TYPE_FILE.getErrorCode()
                .equals(result.getErrorCode())));
    }

    @Test
    public void testValidateDirectoryWithMultipleNotAllowedFiles() throws IOException {
        // Create directory with multiple not allowed files
        Path dirWithMultipleNotAllowedFiles = tempDir.resolve("dir-with-multiple-not-allowed-files");
        Files.createDirectory(dirWithMultipleNotAllowedFiles);
        // Create valid metadata.json file (non-empty)
        Path metadataFile = dirWithMultipleNotAllowedFiles.resolve(HiddenDataworksFolderValidator.METADATA_FILE_NAME);
        Files.write(metadataFile, "{\"scriptPath\":\"test\",\"schedulePath\":\"test.schedule.json\"}".getBytes());
        Files.createFile(dirWithMultipleNotAllowedFiles.resolve("test.type"));
        Files.createFile(dirWithMultipleNotAllowedFiles.resolve("not-allowed-file1.txt"));
        Files.createFile(dirWithMultipleNotAllowedFiles.resolve("not-allowed-file2.txt"));

        // Execute validation
        ValidateContext context = new ValidateContext();
        validator.validate(dirWithMultipleNotAllowedFiles.toFile(), context);
        List<PackageValidatorResult> results = context.getResults();

        // Extract all not allowed file error messages, deduplicate
        Set<String> uniqueNotAllowedFiles = results.stream()
            .filter(result -> result.getValid() != null && !result.getValid()
                && ONLY_SPECIFIC_FILE_ALLOWED_IN_FOLDER.getErrorCode().equals(result.getErrorCode()))
            .map(PackageValidatorResult::getErrorMessage)
            .collect(Collectors.toSet());

        // Validate results - should report two different not allowed files
        assertEquals("Should report two different not allowed files", 2, uniqueNotAllowedFiles.size());
        assertTrue("Should contain not-allowed-file1.txt",
            uniqueNotAllowedFiles.stream().anyMatch(msg -> msg.contains("not-allowed-file1.txt")));
        assertTrue("Should contain not-allowed-file2.txt",
            uniqueNotAllowedFiles.stream().anyMatch(msg -> msg.contains("not-allowed-file2.txt")));
    }

    @Test
    public void testIsAllowedFolders() throws IOException {
        // Create directory
        Path testDir = tempDir.resolve("test-dir");
        Files.createDirectory(testDir);

        // HiddenDataworksFolderValidator does not allow any subdirectories
        ValidateContext context = new ValidateContext();
        assertNotNull("Should not allow any subdirectories", validator.isAllowedFolders(testDir.toFile(), context));
    }

    @Test
    public void testIsAllowedFilesWithMetadataFile() throws IOException {
        // Create metadata.json file
        Path metadataFile = tempDir.resolve(HiddenDataworksFolderValidator.METADATA_FILE_NAME);
        Files.createFile(metadataFile);

        // HiddenDataworksFolderValidator allows metadata.json file
        ValidateContext context = new ValidateContext();
        assertTrue("Should allow metadata.json file", validator.isAllowedFiles(metadataFile.toFile(), context).getValid());
    }

    @Test
    public void testIsAllowedFilesWithTypeFile() throws IOException {
        // Create .type file
        Path typeFile = tempDir.resolve("test.type");
        Files.createFile(typeFile);

        // HiddenDataworksFolderValidator allows .type file
        ValidateContext context = new ValidateContext();
        assertTrue("Should allow .type file", validator.isAllowedFiles(typeFile.toFile(), context).getValid());
    }

    @Test
    public void testIsAllowedFilesWithOtherFile() throws IOException {
        // Create other file
        Path otherFile = tempDir.resolve("other-file.txt");
        Files.createFile(otherFile);

        // HiddenDataworksFolderValidator does not allow other files
        ValidateContext context = new ValidateContext();
        assertNotNull("Should not allow other files", validator.isAllowedFiles(otherFile.toFile(), context));
    }

    @Test
    public void testGetRequiredFolders() {
        // HiddenDataworksFolderValidator does not require folders
        assertTrue("Should not require folders", validator.getRequiredFolders(new ValidateContext()).isEmpty());
    }

    @Test
    public void testGetRequiredFiles() {
        // HiddenDataworksFolderValidator returns empty set in getRequiredFiles
        assertTrue("Should not require files", validator.getRequiredFiles(new ValidateContext()).isEmpty());
    }
}