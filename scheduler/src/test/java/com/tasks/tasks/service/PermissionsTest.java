package com.tasks.tasks.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class PermissionsTest {

    @Test
    public void testRepresentation() {
        List<Permissions> original = Arrays.asList(Permissions.DELETE_TASK,
                Permissions.READ_TASK,
                Permissions.WRITE_TASK);

        short representation = Permissions.toRepresentation(original.stream());

        assertEquals(0b1011, (byte) representation);
    }

    @Test
    public void testConvertToAndBackFromBinary() {
        List<Permissions> original = Arrays.asList(Permissions.DELETE_TASK,
                Permissions.READ_TASK,
                Permissions.UPDATE_TASK,
                Permissions.WRITE_TASK);

        short representation = Permissions.toRepresentation(original.stream());
        List<Permissions> retrieved = Permissions.fromRepresentation(representation);

        assertIterableEquals(original, retrieved);
    }
}
