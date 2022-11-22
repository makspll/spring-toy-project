package com.tasks.tasks.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.security.core.GrantedAuthority;

public enum Permissions implements GrantedAuthority {
    READ_TASK((short) (1 << 0)),
    WRITE_TASK((short) (1 << 1)),
    UPDATE_TASK((short) (1 << 2)),
    DELETE_TASK((short) (1 << 3));

    private short rep;

    Permissions(short rep) {
        this.rep = rep;
    }

    public short getRepresentation() {
        return rep;
    }

    /// Returns sorted list of permissions given binary representation.
    public static List<Permissions> fromRepresentation(short value) {
        ArrayList<Permissions> permissions = new ArrayList<>();
        if ((value & DELETE_TASK.rep) > 0)
            permissions.add(DELETE_TASK);
        if ((value & READ_TASK.rep) > 0)
            permissions.add(READ_TASK);
        if ((value & UPDATE_TASK.rep) > 0)
            permissions.add(UPDATE_TASK);
        if ((value & WRITE_TASK.rep) > 0)
            permissions.add(WRITE_TASK);

        return permissions;
    }

    /// Combines multiple Permissions into a bitwise reprentation. Logical opposite
    /// of fromRepresentation
    public static short toRepresentation(Stream<Permissions> permissions) {
        return permissions
                .map((p) -> p.rep)
                .reduce((short) 0, (a, b) -> (short) ((int) a | (int) b));
    }

    @Override
    public String getAuthority() {
        return "OP_" + name();
    }
}
