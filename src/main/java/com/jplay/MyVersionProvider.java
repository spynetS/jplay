package com.jplay;

import picocli.CommandLine.IVersionProvider;

class MyVersionProvider implements IVersionProvider {
    public String[] getVersion() {
        // internally, Java gets this information from the `META-INF/MANIFEST.MF` file
        // of the JAR file that contains the `MyVersionProvider.class` file.
        String version = MyVersionProvider.class.getPackage().getImplementationVersion();
        return new String[] { "${COMMAND-FULL-NAME} version " + version };
    }
}
