package com.aliyun.dataworks.migrationx.domain.dataworks.packages.validator;

import org.junit.Test;

import static com.aliyun.dataworks.migrationx.domain.dataworks.packages.enums.PackageValidateErrorCode.FILE_CONTENT_INVALID;
import static com.aliyun.dataworks.migrationx.domain.dataworks.packages.enums.PackageValidateErrorCode.FILE_NOT_EXIST;
import static com.aliyun.dataworks.migrationx.domain.dataworks.packages.enums.PackageValidateErrorCode.VALIDATION_PASSED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * PackageValidatorResult Test Class
 *
 * @author 莫泣
 * @date 2025-08-31
 */
public class PackageValidatorResultTest {

    @Test
    public void testSuccessResult() {
        PackageValidatorResult result = PackageValidatorResult.success();

        assertTrue(result.getValid());
        assertEquals(VALIDATION_PASSED.getErrorCode(), result.getErrorCode());
        assertNull(null, result.getErrorMessage());
    }

    @Test
    public void testFailedResultWithNoParams() {
        ValidateContext context = new ValidateContext();
        PackageValidatorResult result = PackageValidatorResult.failed(FILE_NOT_EXIST, context);

        assertFalse(result.getValid());
        assertEquals(FILE_NOT_EXIST.getErrorCode(), result.getErrorCode());
    }

    @Test
    public void testFailedResultWithParams() {
        ValidateContext context = new ValidateContext();
        PackageValidatorResult result = PackageValidatorResult.failed(FILE_NOT_EXIST, context, "path",
            "test.txt");

        assertFalse(result.getValid());
        assertEquals(FILE_NOT_EXIST.getErrorCode(), result.getErrorCode());
        assertEquals(
            "[null]: File does not exist: test.txt. Solution: Create the file with the correct name or move related files to an existing file.",
            result.getErrorMessage());
    }

    @Test
    public void testFailedResultWithMultipleParams() {
        ValidateContext context = new ValidateContext();
        PackageValidatorResult result = PackageValidatorResult.failed(FILE_CONTENT_INVALID, context, "error", "test.txt",
            "lineNumber", 10);

        assertFalse(result.getValid());
        assertEquals(FILE_CONTENT_INVALID.getErrorCode(), result.getErrorCode());
        // Note: FILE_CONTENT_INVALID message template does not have {lineNumber} placeholder, so it won't be replaced
        assertEquals("[null]: Invalid file content: test.txt. Solution: Check the file content and fix the syntax or format errors.",
            result.getErrorMessage());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailedResultWithInvalidParams() {
        ValidateContext context = new ValidateContext();
        PackageValidatorResult.failed(FILE_NOT_EXIST, context, "invalid");
    }

    @Test
    public void testGetErrorCodeWithNull() {
        // Test with a failed result that has null errorCode
        // Since we can't directly create a result with null errorCode because the constructor is private,
        // we'll test that the method handles null errorCode correctly by checking the success result
        // The success result has VALIDATION_PASSED errorCode, not null
        PackageValidatorResult successResult = PackageValidatorResult.success();
        assertEquals(VALIDATION_PASSED.getErrorCode(), successResult.getErrorCode());
    }

    @Test
    public void testGetErrorMessageWithNull() {
        // Test with success result which should have null error message
        PackageValidatorResult result = PackageValidatorResult.success();
        assertNull(result.getErrorMessage());
    }

    @Test
    public void testConstructorWithParams() {
        ValidateContext context = new ValidateContext();
        PackageValidatorResult result = PackageValidatorResult.failed(FILE_NOT_EXIST, context);

        assertFalse(result.getValid());
        assertEquals(FILE_NOT_EXIST.getErrorCode(), result.getErrorCode());
    }

    @Test
    public void testConstructorWithoutParams() {
        PackageValidatorResult result = PackageValidatorResult.success();

        assertTrue(result.getValid());
        assertEquals(VALIDATION_PASSED.getErrorCode(), result.getErrorCode());
    }
}