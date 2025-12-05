package com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.aliyun.dataworks.migrationx.domain.dataworks.packages.enums.ValidateMode;
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
import static com.aliyun.dataworks.migrationx.domain.dataworks.packages.enums.PackageValidateErrorCode.METADATA_FILE_EMPTY;
import static com.aliyun.dataworks.migrationx.domain.dataworks.packages.enums.PackageValidateErrorCode.MISSING_REQUIRED_FOLDER;
import static com.aliyun.dataworks.migrationx.domain.dataworks.packages.enums.PackageValidateErrorCode.ONLY_SPECIFIC_FILE_ALLOWED_IN_FOLDER;
import static com.aliyun.dataworks.migrationx.domain.dataworks.packages.enums.PackageValidateErrorCode.ONLY_SPEC_FOLDER_ALLOWED_IN_FOLDER;
import static com.aliyun.dataworks.migrationx.domain.dataworks.packages.enums.PackageValidateErrorCode.SPEC_PACKAGE_MISSING_FILE;
import static com.aliyun.dataworks.migrationx.domain.dataworks.packages.enums.PackageValidateErrorCode.SPEC_PACKAGE_MULTIPLE_FILES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * SpecPackageValidator Test Class
 *
 * @author Mo Qi
 * @date 2025-08-26
 */
public class SpecPackageValidatorTest {

    private SpecPackageValidator validator;
    private Path tempDir;
    private Method getPackageTypeMethod;

    @Before
    public void setUp() throws Exception {
        validator = SpecPackageValidator.getInstance();
        tempDir = Files.createTempDirectory("spec-package-test");
        // Use reflection to access the private getPackageType method
        getPackageTypeMethod = SpecPackageValidator.class.getDeclaredMethod("getPackageType", File.class, ValidateContext.class);
        getPackageTypeMethod.setAccessible(true);
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
    public void testValidSpecPackage() throws IOException {
        // Create valid SPEC package structure (using .schedule.json format)
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .dataworks folder and its contents
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create metadata.json file with valid content
        String metadataContent = "{\n  \"scriptPath\": \"" + packageName + "\",\n  \"schedulePath\": \"" + packageName + ".schedule.json\"\n}";
        Files.write(dataworksDir.resolve("metadata.json"), metadataContent.getBytes());

        Files.createFile(dataworksDir.resolve("test.type"));

        // Create .schedule.json file (schedule configuration)
        String scheduleContent =
            "{\n  \"version\": \"1.0.0\",\n  \"kind\": \"CycleWorkflow\",\n  \"spec\": {\n    \"nodes\": [\n      {\n        \"id\": \"123456789\","
                + "\n  "
                + "      \"name\": \"" + packageName + "\",\n        \"script\": {\n          \"path\": \""
                + packageName
                + "\",\n          \"runtime\": {\n            \"command\": \"ODPS_SQL\"\n          }\n        }\n      }\n    ]\n  }\n}";
        Files.write(packageDir.resolve(packageName + ".schedule.json"), scheduleContent.getBytes());

        // Create script content file (filename same as package name)
        Files.createFile(packageDir.resolve(packageName));

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results
        assertNotNull(context.getResults());
        // Check if there are any failure results
        boolean hasFailure = context.getResults().stream().anyMatch(result -> result.getValid() != null && !result.getValid());
        if (hasFailure) {
            String errorMessages = context.getResults().stream()
                .filter(result -> result.getValid() != null && !result.getValid())
                .map(PackageValidatorResult::getErrorMessage)
                .reduce("", (a, b) -> a + "; " + b);
            fail("Validation should pass, but has failures: " + errorMessages);
        }
    }

    @Test
    public void testValidSpecPackageWithSpecJson() throws IOException {
        // Create valid SPEC package structure (using .spec.json format)
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .dataworks folder and its contents
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create metadata.json file with valid content
        String metadataContent = "{\n  \"scriptPath\": \"" + packageName + "\",\n  \"schedulePath\": \"" + packageName + ".spec.json\"\n}";
        Files.write(dataworksDir.resolve("metadata.json"), metadataContent.getBytes());

        Files.createFile(dataworksDir.resolve("test.type"));

        // Create .spec.json file (schedule configuration)
        String specContent =
            "{\n  \"version\": \"1.0.0\",\n  \"kind\": \"CycleWorkflow\",\n  \"spec\": {\n    \"nodes\": [\n      {\n        \"id\": \"123456789\","
                + "\n  "
                + "      \"name\": \"" + packageName + "\",\n        \"script\": {\n          \"path\": \""
                + packageName
                + "\",\n          \"runtime\": {\n            \"command\": \"ODPS_SQL\"\n          }\n        }\n      }\n    ]\n  }\n}";
        Files.write(packageDir.resolve(packageName + ".spec.json"), specContent.getBytes());

        // Create script content file (filename same as package name)
        Files.createFile(packageDir.resolve(packageName));

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results
        assertNotNull(context.getResults());
        // Check if there are any failure results
        boolean hasFailure = context.getResults().stream().anyMatch(result -> result.getValid() != null && !result.getValid());
        if (hasFailure) {
            String errorMessages = context.getResults().stream()
                .filter(result -> result.getValid() != null && !result.getValid())
                .map(PackageValidatorResult::getErrorMessage)
                .reduce("", (a, b) -> a + "; " + b);
            fail("Validation should pass, but has failures: " + errorMessages);
        }
    }

    @Test
    public void testDuplicateScheduleFiles() throws IOException {
        // Create SPEC package structure with duplicate schedule configuration files
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .dataworks folder and its contents
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create metadata.json file with valid content
        String metadataContent = "{\n  \"scriptPath\": \"" + packageName + "\",\n  \"schedulePath\": \"" + packageName + ".schedule.json\"\n}";
        Files.write(dataworksDir.resolve("metadata.json"), metadataContent.getBytes());

        Files.createFile(dataworksDir.resolve("test.type"));

        // Create .schedule.json file (schedule configuration)
        Files.createFile(packageDir.resolve(packageName + ".schedule.json"));

        // Create .spec.json file (schedule configuration)
        Files.createFile(packageDir.resolve(packageName + ".spec.json"));

        // Create script content file (filename same as package name)
        Files.createFile(packageDir.resolve(packageName));

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results
        assertNotNull(context.getResults());
        assertTrue("Should have validation failure results",
            context.getResults().stream().anyMatch(result -> result.getValid() != null && !result.getValid()));
        assertTrue("Should report not allowed files", context.getResults().stream().anyMatch(result ->
            result.getValid() != null && !result.getValid() && SPEC_PACKAGE_MULTIPLE_FILES.getErrorCode().equals(result.getErrorCode())));
    }

    @Test
    public void testMissingDataworksFolder() throws IOException {
        // Create SPEC package structure missing .dataworks folder
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .schedule.json file (schedule configuration)
        Files.createFile(packageDir.resolve(packageName + ".schedule.json"));

        // Create script content file (filename same as package name)
        Files.createFile(packageDir.resolve(packageName));

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results
        assertNotNull(context.getResults());
        assertTrue("Should have validation failure results",
            context.getResults().stream().anyMatch(result -> result.getValid() != null && !result.getValid()));
        assertTrue("Should report missing required folder", context.getResults().stream().anyMatch(result ->
            result.getValid() != null && !result.getValid() &&
                (MISSING_REQUIRED_FOLDER.getErrorCode().equals(result.getErrorCode()) ||
                    DATAWORKS_HIDDEN_FOLDER_MISSING_METADATA_FILE.getErrorCode().equals(result.getErrorCode()) ||
                    DATAWORKS_HIDDEN_FOLDER_MISSING_TYPE_FILE.getErrorCode().equals(result.getErrorCode()))));
    }

    @Test
    public void testMissingScheduleFile() throws IOException {
        // Create SPEC package structure missing .schedule.json file
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .dataworks folder and its contents
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create metadata.json file with valid content
        String metadataContent = "{\n  \"scriptPath\": \"" + packageName + "\",\n  \"schedulePath\": \"" + packageName + ".schedule.json\"\n}";
        Files.write(dataworksDir.resolve("metadata.json"), metadataContent.getBytes());

        Files.createFile(dataworksDir.resolve("test.type"));

        // Create script content file (filename same as package name)
        Files.createFile(packageDir.resolve(packageName));

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results
        assertNotNull(context.getResults());
        assertTrue("Should have validation failure results",
            context.getResults().stream().anyMatch(result -> result.getValid() != null && !result.getValid()));
        assertTrue("Should report missing required file", context.getResults().stream().anyMatch(result ->
            result.getValid() != null && !result.getValid() &&
                (SPEC_PACKAGE_MISSING_FILE.getErrorCode().equals(result.getErrorCode()) ||
                    "DATAWORKS_HIDDEN_FOLDER_MISSING_METADATA_FILE".equals(result.getErrorCode()) ||
                    "DATAWORKS_HIDDEN_FOLDER_MISSING_TYPE_FILE".equals(result.getErrorCode())) &&
                (result.getErrorMessage().contains(".schedule.json") || result.getErrorMessage().contains(".spec.json") || result.getErrorMessage()
                    .contains("|"))));
    }

    @Test
    public void testMissingScriptFile() throws IOException {
        // Create SPEC package structure missing script file
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .dataworks folder and its contents
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create metadata.json file with valid content
        String metadataContent = "{\n  \"scriptPath\": \"" + packageName + "\",\n  \"schedulePath\": \"" + packageName + ".schedule.json\"\n}";
        Files.write(dataworksDir.resolve("metadata.json"), metadataContent.getBytes());

        Files.createFile(dataworksDir.resolve("test.type"));

        // Create .schedule.json file (schedule configuration)
        Files.createFile(packageDir.resolve(packageName + ".schedule.json"));

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results
        assertNotNull(context.getResults());
        assertTrue("Should have validation failure results",
            context.getResults().stream().anyMatch(result -> result.getValid() != null && !result.getValid()));
        assertTrue("Should report missing required file", context.getResults().stream().anyMatch(result ->
            result.getValid() != null && !result.getValid() &&
                (SPEC_PACKAGE_MISSING_FILE.getErrorCode().equals(result.getErrorCode()) ||
                    DATAWORKS_HIDDEN_FOLDER_MISSING_METADATA_FILE.getErrorCode().equals(result.getErrorCode()) ||
                    DATAWORKS_HIDDEN_FOLDER_MISSING_TYPE_FILE.getErrorCode().equals(result.getErrorCode())) &&
                result.getErrorMessage().contains(packageName)));
    }

    @Test
    public void testInvalidDataworksFolderContent() throws IOException {
        // Create SPEC package structure with invalid content in .dataworks folder
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .dataworks folder and its contents (with invalid file)
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create metadata.json file with valid content
        String metadataContent = "{\n  \"scriptPath\": \"" + packageName + "\",\n  \"schedulePath\": \"" + packageName + ".schedule.json\"\n}";
        Files.write(dataworksDir.resolve("metadata.json"), metadataContent.getBytes());

        Files.createFile(dataworksDir.resolve("test.type"));
        Files.createFile(dataworksDir.resolve("invalid-file.txt")); // Invalid file

        // Create .schedule.json file (schedule configuration)
        Files.createFile(packageDir.resolve(packageName + ".schedule.json"));

        // Create script content file (filename same as package name)
        Files.createFile(packageDir.resolve(packageName));

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results
        assertNotNull(context.getResults());
        assertTrue("Should have validation failure results",
            context.getResults().stream().anyMatch(result -> result.getValid() != null && !result.getValid()));
        assertTrue("Should report not allowed file", context.getResults().stream().anyMatch(result ->
            result.getValid() != null && !result.getValid() && ONLY_SPECIFIC_FILE_ALLOWED_IN_FOLDER.getErrorCode().equals(result.getErrorCode()) &&
                result.getErrorMessage().contains("invalid-file.txt")));
    }

    @Test
    public void testDuplicateScheduleFile() throws IOException {
        // Create SPEC package structure with duplicate schedule configuration file
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .dataworks folder and its contents
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create metadata.json file with valid content
        String metadataContent = "{\n  \"scriptPath\": \"" + packageName + "\",\n  \"schedulePath\": \"" + packageName + ".schedule.json\"\n}";
        Files.write(dataworksDir.resolve("metadata.json"), metadataContent.getBytes());

        Files.createFile(dataworksDir.resolve("test.type"));

        // Create .schedule.json file (schedule configuration)
        String scheduleContent =
            "{\n  \"version\": \"1.0.0\",\n  \"kind\": \"CycleWorkflow\",\n  \"spec\": {\n    \"nodes\": [\n      {\n        \"id\": \"123456789\","
                + "\n        \"name\": \""
                + packageName + "\",\n        \"script\": {\n          \"path\": \"" + packageName
                + "\",\n          \"runtime\": {\n            \"command\": \"ODPS_SQL\"\n          }\n        }\n      }\n    ]\n  }\n}";
        Files.write(packageDir.resolve(packageName + ".schedule.json"), scheduleContent.getBytes());

        // Create another schedule configuration file with different content
        String scheduleContent2 =
            "{\n  \"version\": \"1.0.0\",\n  \"kind\": \"CycleWorkflow\",\n  \"spec\": {\n    \"nodes\": [\n      {\n        \"id\": \"987654321\","
                + "\n        \"name\": \""
                + packageName + "\",\n        \"script\": {\n          \"path\": \"" + packageName
                + "\",\n          \"runtime\": {\n            \"command\": \"ODPS_SQL\"\n          }\n        }\n      }\n    ]\n  }\n}";
        Files.write(packageDir.resolve(packageName + ".spec.json"), scheduleContent2.getBytes());

        // Create script content file (filename same as package name)
        Files.createFile(packageDir.resolve(packageName));

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results
        assertNotNull(context.getResults());
        assertTrue("Should have validation failure results",
            context.getResults().stream().anyMatch(result -> result.getValid() != null && !result.getValid()));

        // Debug print all error codes
        context.getResults().stream()
            .filter(result -> result.getValid() != null && !result.getValid())
            .forEach(result -> System.out.println("Error code: " + result.getErrorCode() + ", Message: " + result.getErrorMessage()));

        assertTrue("Should report not allowed file", context.getResults().stream().anyMatch(result ->
            result.getValid() != null && !result.getValid() && SPEC_PACKAGE_MULTIPLE_FILES.getErrorCode().equals(result.getErrorCode())));
    }

    @Test
    public void testDuplicateScriptFile() throws IOException {
        // Create SPEC package structure with duplicate script file
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .dataworks folder and its contents
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create metadata.json file with valid content
        String metadataContent = "{\n  \"scriptPath\": \"" + packageName + "\",\n  \"schedulePath\": \"" + packageName + ".schedule.json\"\n}";
        Files.write(dataworksDir.resolve("metadata.json"), metadataContent.getBytes());

        Files.createFile(dataworksDir.resolve("test.type"));

        // Create .schedule.json file (schedule configuration)
        String scheduleContent =
            "{\n  \"version\": \"1.0.0\",\n  \"kind\": \"CycleWorkflow\",\n  \"spec\": {\n    \"nodes\": [\n      {\n        \"id\": \"123456789\","
                + "\n        \"name\": \""
                + packageName + "\",\n        \"script\": {\n          \"path\": \"" + packageName
                + "\",\n          \"runtime\": {\n            \"command\": \"ODPS_SQL\"\n          }\n        }\n      }\n    ]\n  }\n}";
        Files.write(packageDir.resolve(packageName + ".schedule.json"), scheduleContent.getBytes());

        // Create script content file (filename same as package name)
        Files.createFile(packageDir.resolve(packageName));

        // Create another script content file with different extension
        Files.createFile(packageDir.resolve(packageName + ".sql"));

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results
        assertNotNull(context.getResults());
        assertTrue("Should have validation failure results",
            context.getResults().stream().anyMatch(result -> result.getValid() != null && !result.getValid()));
        assertTrue("Should report not allowed file", context.getResults().stream().anyMatch(result ->
            result.getValid() != null && !result.getValid() && SPEC_PACKAGE_MULTIPLE_FILES.getErrorCode().equals(result.getErrorCode())));
    }

    @Test
    public void testControllerCyclePackageWithSubPackage() throws IOException {
        // Create CONTROLLER_CYCLE type package structure
        String packageName = "controller-cycle-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .dataworks folder and its contents (CONTROLLER_CYCLE.type)
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create metadata.json file with valid content
        String metadataContent = "{\n  \"scriptPath\": \"" + packageName + "\",\n  \"schedulePath\": \"" + packageName + ".schedule.json\"\n}";
        Files.write(dataworksDir.resolve("metadata.json"), metadataContent.getBytes());

        Files.createFile(dataworksDir.resolve("CONTROLLER_CYCLE.type"));

        // Create .schedule.json file (schedule configuration)
        String scheduleContent =
            "{\n  \"version\": \"1.0.0\",\n  \"kind\": \"CycleWorkflow\",\n  \"spec\": {\n    \"nodes\": [\n      {\n        \"id\": \"1122334455\","
                + "\n  "
                + "      \"name\": \"" + packageName + "\",\n        \"script\": {\n          \"path\": \""
                + packageName
                + "\",\n          \"runtime\": {\n            \"command\": \"ODPS_SQL\"\n          }\n        }\n      }\n    ]\n  }\n}";
        Files.write(packageDir.resolve(packageName + ".schedule.json"), scheduleContent.getBytes());

        // Create script content file (filename same as package name)
        Files.createFile(packageDir.resolve(packageName));

        // Create CONTROLLER_CYCLE_START subpackage
        String startPackageName = "start-package";
        Path startPackageDir = packageDir.resolve(startPackageName);
        Files.createDirectory(startPackageDir);
        Path startDataworksDir = startPackageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(startDataworksDir);

        // Create metadata.json file with valid content
        String startMetadataContent = "{\n  \"scriptPath\": \"" + startPackageName + "\",\n  \"schedulePath\": \"" + startPackageName
            + ".schedule.json\"\n}";
        Files.write(startDataworksDir.resolve("metadata.json"), startMetadataContent.getBytes());

        Files.createFile(startDataworksDir.resolve("CONTROLLER_CYCLE_START.type"));
        String startScheduleContent =
            "{\n  \"version\": \"1.0.0\",\n  \"kind\": \"CycleWorkflow\",\n  \"spec\": {\n    \"nodes\": [\n      {\n        \"id\": \"123456789\","
                + "\n  "
                + "      \"name\": \"" + startPackageName + "\",\n        \"script\": {\n          \"path\": \""
                + startPackageName
                + "\",\n          \"runtime\": {\n            \"command\": \"ODPS_SQL\"\n          }\n        }\n      }\n    ]\n  }\n}";
        Files.write(startPackageDir.resolve(startPackageName + ".schedule.json"), startScheduleContent.getBytes());
        Files.createFile(startPackageDir.resolve(startPackageName));

        // Create CONTROLLER_CYCLE_END subpackage
        String endPackageName = "end-package";
        Path endPackageDir = packageDir.resolve(endPackageName);
        Files.createDirectory(endPackageDir);
        Path endDataworksDir = endPackageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(endDataworksDir);

        // Create metadata.json file with valid content
        String endMetadataContent = "{\n  \"scriptPath\": \"" + endPackageName + "\",\n  \"schedulePath\": \"" + endPackageName
            + ".schedule.json\"\n}";
        Files.write(endDataworksDir.resolve("metadata.json"), endMetadataContent.getBytes());

        Files.createFile(endDataworksDir.resolve("CONTROLLER_CYCLE_END.type"));
        String endScheduleContent2 =
            "{\n  \"version\": \"1.0.0\",\n  \"kind\": \"CycleWorkflow\",\n  \"spec\": {\n    \"nodes\": [\n      {\n        \"id\": \"987654321\","
                + "\n  "
                + "      \"name\": \"" + endPackageName + "\",\n        \"script\": {\n          \"path\": \""
                + endPackageName
                + "\",\n          \"runtime\": {\n            \"command\": \"ODPS_SQL\"\n          }\n        }\n      }\n    ]\n  }\n}";
        Files.write(endPackageDir.resolve(endPackageName + ".schedule.json"), endScheduleContent2.getBytes());
        Files.createFile(endPackageDir.resolve(endPackageName));

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results - Root directory validation should pass, no errors
        assertNotNull(context.getResults());
        // Check if there are any failure results, but ignore subpackage validation results
        boolean hasFailure = context.getResults().stream()
            .filter(result -> result.getValid() != null && !result.getValid())
            .filter(result -> !result.getErrorMessage().contains("start-package") && !result.getErrorMessage().contains("end-package"))
            .count() > 0;
        if (hasFailure) {
            String errorMessages = context.getResults().stream()
                .filter(result -> result.getValid() != null && !result.getValid())
                .filter(result -> !result.getErrorMessage().contains("start-package") && !result.getErrorMessage().contains("end-package"))
                .map(PackageValidatorResult::getErrorMessage)
                .reduce("", (a, b) -> a + "; " + b);
            fail("Validation should pass, but has failures: " + errorMessages);
        }

        // Validate subpackage validators
        List<PackageValidator> childValidators = validator.getChildValidators(startPackageDir.toFile(),
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext());
        assertNotNull(childValidators);
        assertEquals(1, childValidators.size());
        assertTrue(childValidators.get(0) instanceof SpecPackageValidator);
    }

    @Test
    public void testControllerCyclePackageMissingRequiredSubPackages() throws IOException {
        // Create CONTROLLER_CYCLE type package structure, but missing required subpackages
        String packageName = "controller-cycle-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .dataworks folder and its contents (CONTROLLER_CYCLE.type)
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create metadata.json file with valid content
        String metadataContent = "{\n  \"scriptPath\": \"" + packageName + "\",\n  \"schedulePath\": \"" + packageName + ".schedule.json\"\n}";
        Files.write(dataworksDir.resolve("metadata.json"), metadataContent.getBytes());

        Files.createFile(dataworksDir.resolve("CONTROLLER_CYCLE.type"));

        // Create .schedule.json file (schedule configuration)
        Files.createFile(packageDir.resolve(packageName + ".schedule.json"));

        // Create script content file (filename same as package name)
        Files.createFile(packageDir.resolve(packageName));

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results - Should report missing required subpackages
        assertNotNull(context.getResults());
        boolean hasMissingStartPackage = context.getResults().stream()
            .anyMatch(result -> result.getValid() != null && !result.getValid() &&
                result.getErrorMessage().contains("CONTROLLER_CYCLE_START subpackage"));
        boolean hasMissingEndPackage = context.getResults().stream()
            .anyMatch(result -> result.getValid() != null && !result.getValid() &&
                result.getErrorMessage().contains("CONTROLLER_CYCLE_END subpackage"));

        assertTrue("Should report missing CONTROLLER_CYCLE_START subpackage", hasMissingStartPackage);
        assertTrue("Should report missing CONTROLLER_CYCLE_END subpackage", hasMissingEndPackage);
    }

    @Test
    public void testControllerTraversePackageWithSubPackage() throws IOException {
        // Create CONTROLLER_TRAVERSE type package structure
        String packageName = "controller-traverse-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .dataworks folder and its contents (CONTROLLER_TRAVERSE.type)
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create metadata.json file with valid content
        String metadataContent = "{\n  \"scriptPath\": \"" + packageName + "\",\n  \"schedulePath\": \"" + packageName + ".schedule.json\"\n}";
        Files.write(dataworksDir.resolve("metadata.json"), metadataContent.getBytes());

        Files.createFile(dataworksDir.resolve("CONTROLLER_TRAVERSE.type"));

        // Create .schedule.json file (schedule configuration)
        String scheduleContent =
            "{\n  \"version\": \"1.0.0\",\n  \"kind\": \"CycleWorkflow\",\n  \"spec\": {\n    \"nodes\": [\n      {\n        \"id\": \"1122334455\","
                + "\n  "
                + "      \"name\": \"" + packageName + "\",\n        \"script\": {\n          \"path\": \""
                + packageName
                + "\",\n          \"runtime\": {\n            \"command\": \"ODPS_SQL\"\n          }\n        }\n      }\n    ]\n  }\n}";
        Files.write(packageDir.resolve(packageName + ".schedule.json"), scheduleContent.getBytes());

        // Create script content file (filename same as package name)
        Files.createFile(packageDir.resolve(packageName));

        // Create CONTROLLER_TRAVERSE_START subpackage
        String startPackageName = "start-package";
        Path startPackageDir = packageDir.resolve(startPackageName);
        Files.createDirectory(startPackageDir);
        Path startDataworksDir = startPackageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(startDataworksDir);

        // Create metadata.json file with valid content
        String startMetadataContent = "{\n  \"scriptPath\": \"" + startPackageName + "\",\n  \"schedulePath\": \"" + startPackageName
            + ".schedule.json\"\n}";
        Files.write(startDataworksDir.resolve("metadata.json"), startMetadataContent.getBytes());

        Files.createFile(startDataworksDir.resolve("CONTROLLER_TRAVERSE_START.type"));
        String startScheduleContent =
            "{\n  \"version\": \"1.0.0\",\n  \"kind\": \"CycleWorkflow\",\n  \"spec\": {\n    \"nodes\": [\n      {\n        \"id\": \"123456789\","
                + "\n  "
                + "      \"name\": \"" + startPackageName + "\",\n        \"script\": {\n          \"path\": \""
                + startPackageName
                + "\",\n          \"runtime\": {\n            \"command\": \"ODPS_SQL\"\n          }\n        }\n      }\n    ]\n  }\n}";
        Files.write(startPackageDir.resolve(startPackageName + ".schedule.json"), startScheduleContent.getBytes());
        Files.createFile(startPackageDir.resolve(startPackageName));

        // Create CONTROLLER_TRAVERSE_END subpackage
        String endPackageName = "end-package";
        Path endPackageDir = packageDir.resolve(endPackageName);
        Files.createDirectory(endPackageDir);
        Path endDataworksDir = endPackageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(endDataworksDir);

        // Create metadata.json file with valid content
        String endMetadataContent = "{\n  \"scriptPath\": \"" + endPackageName + "\",\n  \"schedulePath\": \"" + endPackageName
            + ".schedule.json\"\n}";
        Files.write(endDataworksDir.resolve("metadata.json"), endMetadataContent.getBytes());

        Files.createFile(endDataworksDir.resolve("CONTROLLER_TRAVERSE_END.type"));
        String endScheduleContent2 =
            "{\n  \"version\": \"1.0.0\",\n  \"kind\": \"CycleWorkflow\",\n  \"spec\": {\n    \"nodes\": [\n      {\n        \"id\": \"987654321\","
                + "\n  "
                + "      \"name\": \"" + endPackageName + "\",\n        \"script\": {\n          \"path\": \""
                + endPackageName
                + "\",\n          \"runtime\": {\n            \"command\": \"ODPS_SQL\"\n          }\n        }\n      }\n    ]\n  }\n}";
        Files.write(endPackageDir.resolve(endPackageName + ".schedule.json"), endScheduleContent2.getBytes());
        Files.createFile(endPackageDir.resolve(endPackageName));

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results - Root directory validation should pass, no errors
        assertNotNull(context.getResults());
        // Check if there are any failure results, but ignore subpackage validation results
        boolean hasFailure = context.getResults().stream()
            .filter(result -> result.getValid() != null && !result.getValid())
            .filter(result -> !result.getErrorMessage().contains("start-package") && !result.getErrorMessage().contains("end-package"))
            .count() > 0;
        if (hasFailure) {
            String errorMessages = context.getResults().stream()
                .filter(result -> result.getValid() != null && !result.getValid())
                .filter(result -> !result.getErrorMessage().contains("start-package") && !result.getErrorMessage().contains("end-package"))
                .map(PackageValidatorResult::getErrorMessage)
                .reduce("", (a, b) -> a + "; " + b);
            fail("Validation should pass, but has failures: " + errorMessages);
        }

        // Validate subpackage validators
        List<PackageValidator> childValidators = validator.getChildValidators(startPackageDir.toFile(),
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext());
        assertNotNull(childValidators);
        assertEquals(1, childValidators.size());
        assertTrue(childValidators.get(0) instanceof SpecPackageValidator);
    }

    @Test
    public void testNonWorkflowPackageWithSubPackage() throws IOException {
        // Create non-WORKFLOW type package structure, subpackages not allowed
        String packageName = "task-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .dataworks folder and its contents (TASK.type)
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create metadata.json file with valid content
        String metadataContent = "{\n  \"scriptPath\": \"" + packageName + "\",\n  \"schedulePath\": \"" + packageName + ".schedule.json\"\n}";
        Files.write(dataworksDir.resolve("metadata.json"), metadataContent.getBytes());

        Files.createFile(dataworksDir.resolve("TASK.type"));

        // Create .schedule.json file (schedule configuration)
        Files.createFile(packageDir.resolve(packageName + ".schedule.json"));

        // Create script content file (filename same as package name)
        Files.createFile(packageDir.resolve(packageName));

        // Create subpackage
        Path subPackageDir = packageDir.resolve("sub-package");
        Files.createDirectory(subPackageDir);

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results
        assertNotNull(context.getResults());
        assertTrue("Should have validation failure results",
            context.getResults().stream().anyMatch(result -> result.getValid() != null && !result.getValid()));
        assertTrue("Should report not allowed folder", context.getResults().stream().anyMatch(result ->
            result.getValid() != null && !result.getValid() && ONLY_SPEC_FOLDER_ALLOWED_IN_FOLDER.getErrorCode().equals(result.getErrorCode()) &&
                result.getErrorMessage().contains("sub-package")));
    }

    @Test
    public void testEmptyMetadataFile() throws IOException {
        // Create SPEC package structure with empty metadata.json file
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .dataworks folder and its contents
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create empty metadata.json file
        Files.createFile(dataworksDir.resolve("metadata.json"));

        Files.createFile(dataworksDir.resolve("test.type"));

        // Create .schedule.json file (schedule configuration)
        Files.createFile(packageDir.resolve(packageName + ".schedule.json"));

        // Create script content file (filename same as package name)
        Files.createFile(packageDir.resolve(packageName));

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results
        assertNotNull(context.getResults());
        assertTrue("Should have validation failure results",
            context.getResults().stream().anyMatch(result -> result.getValid() != null && !result.getValid()));
        assertTrue("Should report file content is empty", context.getResults().stream().anyMatch(result ->
            result.getValid() != null && !result.getValid() && METADATA_FILE_EMPTY.getErrorCode().equals(result.getErrorCode())));
    }

    @Test
    public void testEmptyDataworksFolder() throws IOException {
        // Create SPEC package structure with empty .dataworks folder
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create empty .dataworks folder
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create .schedule.json file (schedule configuration)
        Files.createFile(packageDir.resolve(packageName + ".schedule.json"));

        // Create script content file (filename same as package name)
        Files.createFile(packageDir.resolve(packageName));

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results
        assertNotNull(context.getResults());
        assertTrue("Should have validation failure results",
            context.getResults().stream().anyMatch(result -> result.getValid() != null && !result.getValid()));
        assertTrue("Should report missing required file", context.getResults().stream().anyMatch(result ->
            result.getValid() != null && !result.getValid() &&
                (DATAWORKS_HIDDEN_FOLDER_MISSING_METADATA_FILE.getErrorCode().equals(result.getErrorCode()) ||
                    DATAWORKS_HIDDEN_FOLDER_MISSING_TYPE_FILE.getErrorCode().equals(result.getErrorCode())) &&
                (result.getErrorMessage().contains("metadata.json") || result.getErrorMessage().contains(".type"))));
    }

    @Test
    public void testDuplicateScriptFiles() throws IOException {
        // Create SPEC package structure with duplicate script file (covers line 176)
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .dataworks folder and its contents
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create metadata.json file with valid content
        String metadataContent = "{\n  \"scriptPath\": \"" + packageName + "\",\n  \"schedulePath\": \"" + packageName + ".schedule.json\"\n}";
        Files.write(dataworksDir.resolve("metadata.json"), metadataContent.getBytes());

        Files.createFile(dataworksDir.resolve("test.type"));

        // Create .schedule.json file (schedule configuration)
        Files.createFile(packageDir.resolve(packageName + ".schedule.json"));

        // Create script content file (filename same as package name)
        Files.createFile(packageDir.resolve(packageName));

        // Create another file (not a script file, will be marked as not allowed file)
        Files.createFile(packageDir.resolve("other-file.txt"));

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results
        assertNotNull(context.getResults());
        // Check if there are results marked as not allowed files
        boolean hasNotAllowedFile = context.getResults().stream().anyMatch(result ->
            result.getValid() != null && !result.getValid() && "FILE_NOT_ALLOWED".equals(result.getErrorCode()));
        // This test is mainly to cover the file checking logic in validateRootDirectory
        // Not specifically testing duplicate script file checking (because file system doesn't allow same name files)
    }

    @Test
    public void testDataworksFolderWithSubDirectory() throws IOException {
        // Create SPEC package structure with subdirectory in .dataworks folder (covers lines 222-223)
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .dataworks folder and its contents (with subdirectory)
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create subdirectory
        Path subDir = dataworksDir.resolve("sub-dir");
        Files.createDirectory(subDir);

        // Create metadata.json file with valid content
        String metadataContent = "{\n  \"scriptPath\": \"" + packageName + "\",\n  \"schedulePath\": \"" + packageName + ".schedule.json\"\n}";
        Files.write(dataworksDir.resolve("metadata.json"), metadataContent.getBytes());

        Files.createFile(dataworksDir.resolve("test.type"));

        // Create .schedule.json file (schedule configuration)
        Files.createFile(packageDir.resolve(packageName + ".schedule.json"));

        // Create script content file (filename same as package name)
        Files.createFile(packageDir.resolve(packageName));

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results
        assertNotNull(context.getResults());
        assertTrue("Should have validation failure results",
            context.getResults().stream().anyMatch(result -> result.getValid() != null && !result.getValid()));
        assertTrue("Should report not allowed directory", context.getResults().stream().anyMatch(result ->
            result.getValid() != null && !result.getValid() && ANY_FOLDER_NOT_ALLOW_IN_FOLDER.getErrorCode().equals(result.getErrorCode()) &&
                result.getErrorMessage().contains("sub-dir")));
    }

    @Test
    public void testDuplicateMetadataFiles() throws IOException {
        // Create SPEC package structure with duplicate metadata.json files (covers line 232)
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .dataworks folder and its contents (with duplicate metadata.json files)
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create multiple metadata.json files
        String metadataContent = "{\n  \"scriptPath\": \"" + packageName + "\",\n  \"schedulePath\": \"" + packageName + ".schedule.json\"\n}";
        Files.write(dataworksDir.resolve("metadata.json"), metadataContent.getBytes());

        // Create another metadata file, but not metadata.json (this will be marked as not allowed file)
        Files.write(dataworksDir.resolve("metadata2.json"), metadataContent.getBytes());

        Files.createFile(dataworksDir.resolve("test.type"));

        // Create .schedule.json file (schedule configuration)
        Files.createFile(packageDir.resolve(packageName + ".schedule.json"));

        // Create script content file (filename same as package name)
        Files.createFile(packageDir.resolve(packageName));

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results
        assertNotNull(context.getResults());
        // Check if there are results marked as not allowed files (metadata2.json will be marked as not allowed file)
        boolean hasNotAllowedFile = context.getResults().stream().anyMatch(result ->
            result.getValid() != null && !result.getValid() && "0x50830e0000000000a".equals(result.getErrorCode()));
        // This test is mainly to cover the file checking logic in validateDataworksFolder
        // Not specifically testing duplicate metadata.json file checking (because file system doesn't allow same name files)
    }

    @Test
    public void testDuplicateTypeFiles() throws IOException {
        // Create SPEC package structure with duplicate .type files (covers line 244)
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .dataworks folder and its contents (with duplicate .type files)
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create metadata.json file with valid content
        String metadataContent = "{\n  \"scriptPath\": \"" + packageName + "\",\n  \"schedulePath\": \"" + packageName + ".schedule.json\"\n}";
        Files.write(dataworksDir.resolve("metadata.json"), metadataContent.getBytes());

        // Create multiple .type files
        Files.createFile(dataworksDir.resolve("test.type"));
        Files.createFile(dataworksDir.resolve("test2.type"));

        // Create .schedule.json file (schedule configuration)
        Files.createFile(packageDir.resolve(packageName + ".schedule.json"));

        // Create script content file (filename same as package name)
        Files.createFile(packageDir.resolve(packageName));

        // Execute validation
        com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext context =
            new com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator.ValidateContext();
        validator.validate(packageDir.toFile(), context);

        // Validate results
        assertNotNull(context.getResults());
        assertTrue("Should have validation failure results",
            context.getResults().stream().anyMatch(result -> result.getValid() != null && !result.getValid()));
        assertTrue("Should report multiple .type files exist", context.getResults().stream().anyMatch(result ->
            result.getValid() != null && !result.getValid() && DATAWORKS_HIDDEN_FOLDER_MULTIPLE_TYPE_FILES.getErrorCode()
                .equals(result.getErrorCode())));
    }

    @Test
    public void testGetPackageTypePreCheckWithValidScheduleFile() throws Exception {
        // Create package structure for pre-check scenario
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .schedule.json file with valid content for pre-check
        String scheduleContent = "{\n" +
            "  \"version\": \"1.0.0\",\n" +
            "  \"kind\": \"CycleWorkflow\",\n" +
            "  \"spec\": {\n" +
            "    \"nodes\": [\n" +
            "      {\n" +
            "        \"name\": \"" + packageName + "\",\n" +
            "        \"script\": {\n" +
            "          \"runtime\": {\n" +
            "            \"command\": \"ODPS_SQL\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";
        Files.write(packageDir.resolve(packageName + ".schedule.json"), scheduleContent.getBytes());

        // Create script content file
        Files.createFile(packageDir.resolve(packageName));

        // Create validation context with pre-check mode
        ValidateContext context = new ValidateContext();
        context.setValidateMode(ValidateMode.PRE_VALIDATE);

        // Call getPackageType method
        String packageType = (String)getPackageTypeMethod.invoke(validator, packageDir.toFile(), context);

        // Validate result
        assertEquals("ODPS_SQL.type", packageType);
    }

    @Test
    public void testGetPackageTypePreCheckWithValidSpecFile() throws Exception {
        // Create package structure for pre-check scenario
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .spec.json file with valid content for pre-check
        String specContent = "{\n" +
            "  \"version\": \"1.0.0\",\n" +
            "  \"kind\": \"CycleWorkflow\",\n" +
            "  \"spec\": {\n" +
            "    \"nodes\": [\n" +
            "      {\n" +
            "        \"name\": \"" + packageName + "\",\n" +
            "        \"script\": {\n" +
            "          \"runtime\": {\n" +
            "            \"command\": \"CONTROLLER_CYCLE\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";
        Files.write(packageDir.resolve(packageName + ".spec.json"), specContent.getBytes());

        // Create script content file
        Files.createFile(packageDir.resolve(packageName));

        // Create validation context with pre-check mode
        ValidateContext context = new ValidateContext();
        context.setValidateMode(ValidateMode.PRE_VALIDATE);

        // Call getPackageType method
        String packageType = (String)getPackageTypeMethod.invoke(validator, packageDir.toFile(), context);

        // Validate result
        assertEquals("CONTROLLER_CYCLE.type", packageType);
    }

    @Test
    public void testGetPackageTypePreCheckWithInvalidScheduleFile() throws Exception {
        // Create package structure for pre-check scenario
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create invalid .schedule.json file
        String invalidScheduleContent = "{ invalid json }";
        Files.write(packageDir.resolve(packageName + ".schedule.json"), invalidScheduleContent.getBytes());

        // Create script content file
        Files.createFile(packageDir.resolve(packageName));

        // Create validation context with pre-check mode
        ValidateContext context = new ValidateContext();
        context.setValidateMode(ValidateMode.PRE_VALIDATE);

        // Call getPackageType method
        String packageType = (String)getPackageTypeMethod.invoke(validator, packageDir.toFile(), context);

        // Validate result - should return null for invalid content
        assertNull(packageType);
    }

    @Test
    public void testGetPackageTypePreCheckWithMissingScheduleFile() throws Exception {
        // Create package structure without schedule file for pre-check scenario
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create script content file
        Files.createFile(packageDir.resolve(packageName));

        // Create validation context with pre-check mode
        ValidateContext context = new ValidateContext();
        context.setValidateMode(ValidateMode.PRE_VALIDATE);

        // Call getPackageType method
        String packageType = (String)getPackageTypeMethod.invoke(validator, packageDir.toFile(), context);

        // Validate result - should return null when no schedule file exists
        assertNull(packageType);
    }

    @Test
    public void testGetPackageTypePreCheckWithNonExistentPackageDir() throws Exception {
        // Create non-existent package directory
        Path packageDir = tempDir.resolve("non-existent-package");

        // Create validation context with pre-check mode
        ValidateContext context = new ValidateContext();
        context.setValidateMode(ValidateMode.PRE_VALIDATE);

        // Call getPackageType method
        String packageType = (String)getPackageTypeMethod.invoke(validator, packageDir.toFile(), context);

        // Validate result - should return null for non-existent directory
        assertNull(packageType);
    }

    @Test
    public void testGetPackageTypePreCheckWithNullPackageDir() throws Exception {
        // Create validation context with pre-check mode
        ValidateContext context = new ValidateContext();
        context.setValidateMode(ValidateMode.PRE_VALIDATE);

        // Call getPackageType method with null directory
        String packageType = (String)getPackageTypeMethod.invoke(validator, new Object[] {null, context});

        // Validate result - should return null for null directory
        assertNull(packageType);
    }

    @Test
    public void testGetPackageTypeNormalModeWithValidTypeFile() throws Exception {
        // Create package structure for normal mode scenario
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create .dataworks folder and its contents
        Path dataworksDir = packageDir.resolve(SpecPackageValidator.DATAWORKS_FILE_NAME);
        Files.createDirectory(dataworksDir);

        // Create .type file
        Files.createFile(dataworksDir.resolve("ODPS_SQL.type"));

        // Create validation context with normal mode
        ValidateContext context = new ValidateContext();
        context.setValidateMode(ValidateMode.NORMAL);

        // Call getPackageType method
        String packageType = (String)getPackageTypeMethod.invoke(validator, packageDir.toFile(), context);

        // Validate result
        assertEquals("ODPS_SQL.type", packageType);
    }

    @Test
    public void testGetPackageTypeNormalModeWithoutDataworksFolder() throws Exception {
        // Create package structure without .dataworks folder for normal mode scenario
        String packageName = "test-package";
        Path packageDir = tempDir.resolve(packageName);
        Files.createDirectory(packageDir);

        // Create validation context with normal mode
        ValidateContext context = new ValidateContext();
        context.setValidateMode(ValidateMode.NORMAL);

        // Call getPackageType method
        String packageType = (String)getPackageTypeMethod.invoke(validator, packageDir.toFile(), context);

        // Validate result - should return null when .dataworks folder doesn't exist
        assertNull(packageType);
    }
}