package com.crediblebadger.storage;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StorageControllerTest {
    
    @Test
    public void validateFileNameTest() {
        Assertions.assertTrue(StorageController.validateFileName("file"));
        Assertions.assertTrue(StorageController.validateFileName("test_003-1.jpg"));
        
        Assertions.assertFalse(StorageController.validateFileName(null));
        Assertions.assertFalse(StorageController.validateFileName(""));
        Assertions.assertFalse(StorageController.validateFileName("/file.txt"));
        Assertions.assertFalse(StorageController.validateFileName("file.txt+"));
        Assertions.assertFalse(StorageController.validateFileName("folder/file.txt"));

        String tooLong = RandomStringUtils.randomAlphanumeric(256);
        Assertions.assertFalse(StorageController.validateFileName(tooLong));
    }
}
