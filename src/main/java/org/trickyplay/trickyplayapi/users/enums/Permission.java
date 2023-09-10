package org.trickyplay.trickyplayapi.users.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
public enum Permission {
    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    USER_CREATE("user:create"),
    USER_DELETE("user:delete"),
    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete");

    @Getter
    private final String permission;

    /**
     * Permission creator method, takes a String value and matches it with one of the Permission constants
     *
     * One way to get an enum from a string value is to use the valueOf method
     * When given an enum constant name, valueOf returns the matched enum constant:
     * e.g. final Permission permission = Permission.valueOf(USER_READ)
     * (the name must be an exact match, or else it throws an IllegalArgumentException)
     * however, the built-in methods are not useful when we need to retrieve Permission from its permission property instead of its constant name
     * therefore, we define the fromProperty method that returns a Permission object based on the permission property
     *
     * @param permissionAttribute
     * @return
     */
    public static Permission fromProperty(String permissionAttribute) {
        // for (Permission permission : values()) {
        //     if (permission.permission.equalsIgnoreCase(permissionString)) {
        //         return permission;
        //     }
        // }
        // return null;

        return Stream.of(values())
                .filter(permission -> permission.getPermission().equals(permissionAttribute))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No constant with string: " + permissionAttribute + " found"));
                // .orElse(null); throwing an exception will be better than null
    }

    // The same functionality that the fromValue method implements according to the pattern from Joshua Bloch, Effective Java:
    //
    // private static final Map<String,MyEnum> ENUM_MAP;
    //
    // Build an immutable map of String name to enum pairs.
    // Any Map impl can be used.
    //
    // static {
    //     Map<String,MyEnum> map = new ConcurrentHashMap<String, Permission>();
    //     for (Permission instance : Permission.values()) {
    //         map.put(instance.getPermission().toLowerCase(),instance);
    //     }
    //     ENUM_MAP = Collections.unmodifiableMap(map);
    // }
    //
    // public static MyEnum get (String name) {
    //     return ENUM_MAP.get(name.toLowerCase());
    // }
}
