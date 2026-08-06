package com.codebyriley.laziestae2;

public final class Constants {
    public static final String MOD_ID = "laziestae2";
    public static final String NAME = "Laziest AE2";
    // Keep in step with the version in gradle.properties; @Mod needs a compile-time constant.
    public static final String VERSION = "1.0.2-beta";

    public static final String PACKAGE_ROOT = "com.codebyriley.laziestae2";
    public static final String CLIENT_PROXY = PACKAGE_ROOT + ".proxy.ClientProxy";
    public static final String SERVER_PROXY = PACKAGE_ROOT + ".proxy.CommonProxy";

    private Constants() { }
}
