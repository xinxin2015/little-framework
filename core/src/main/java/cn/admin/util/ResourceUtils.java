package cn.admin.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.*;

public abstract class ResourceUtils {

    public static final String CLASSPATH_URL_PREFIX = "classpath:";

    public static final String FILE_URL_PREFIX = "file:";

    public static final String JAR_URL_PREFIX = "jar:";

    public static final String WAR_URL_PREFIX = "war:";

    public static final String URL_PROTOCOL_FILE = "file";

    /** URL protocol for an entry from a jar file: "jar". */
    public static final String URL_PROTOCOL_JAR = "jar";

    /** URL protocol for an entry from a war file: "war". */
    public static final String URL_PROTOCOL_WAR = "war";

    /** URL protocol for an entry from a zip file: "zip". */
    public static final String URL_PROTOCOL_ZIP = "zip";

    /** URL protocol for an entry from a WebSphere jar file: "wsjar". */
    public static final String URL_PROTOCOL_WSJAR = "wsjar";

    /** URL protocol for an entry from a JBoss jar file: "vfszip". */
    public static final String URL_PROTOCOL_VFSZIP = "vfszip";

    /** URL protocol for a JBoss file system resource: "vfsfile". */
    public static final String URL_PROTOCOL_VFSFILE = "vfsfile";

    /** URL protocol for a general JBoss VFS resource: "vfs". */
    public static final String URL_PROTOCOL_VFS = "vfs";

    /** File extension for a regular jar file: ".jar". */
    public static final String JAR_FILE_EXTENSION = ".jar";

    /** Separator between JAR URL and file path within the JAR: "!/". */
    public static final String JAR_URL_SEPARATOR = "!/";

    /** Special separator between WAR URL and jar part on Tomcat. */
    public static final String WAR_URL_SEPARATOR = "*/";

    public static URI toURI(URL url) throws URISyntaxException {
        return toURI(url.toString());
    }

    public static URI toURI(String location) throws URISyntaxException {
        return new URI(StringUtils.replace(location," ","%20"));
    }

    public static boolean isFileURL(URL url) {
        String protocol = url.getProtocol();
        return URL_PROTOCOL_FILE.equals(protocol) || URL_PROTOCOL_VFSFILE.equals(protocol)
                || URL_PROTOCOL_VFS.equals(protocol);
    }

    public static boolean isJarURL(URL url) {
        String protocol = url.getProtocol();
        return (URL_PROTOCOL_JAR.equals(protocol) || URL_PROTOCOL_WAR.equals(protocol) ||
                URL_PROTOCOL_ZIP.equals(protocol) || URL_PROTOCOL_VFSZIP.equals(protocol) ||
                URL_PROTOCOL_WSJAR.equals(protocol));
    }

    public static void useCachesIfNecessary(URLConnection con) {
        con.setUseCaches(con.getClass().getSimpleName().startsWith("JNLP"));
    }

    public static File getFile(String resourceLocation) throws FileNotFoundException {
        Assert.notNull(resourceLocation,"Resource location must not be null");
        if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
            String path = resourceLocation.substring(CLASSPATH_URL_PREFIX.length());
            String description = "class path resource [" + path + "]";
            ClassLoader cl = ClassUtils.getDefaultClassLoader();
            URL url = cl != null ? cl.getResource(path) : ClassLoader.getSystemResource(path);
            if (url == null) {
                throw new FileNotFoundException(description +
                        " cannot be resolved to absolute file path because it does not exist");
            }
            return getFile(url,description);
        }
        try {
            return getFile(new URL(resourceLocation));
        } catch (MalformedURLException e) {
            return new File(resourceLocation);
        }
    }

    public static File getFile(URL resourceUrl) throws FileNotFoundException {
        return getFile(resourceUrl,"URL");
    }

    public static File getFile(URL resourceUrl,String description) throws FileNotFoundException {
        Assert.notNull(resourceUrl,"Resource URL must not be null");
        if (!URL_PROTOCOL_FILE.equals(resourceUrl.getProtocol())) {
            throw new FileNotFoundException(
                    description + " cannot be resolved to absolute file path " +
                            "because it does not reside in the file system: " + resourceUrl);
        }
        try {
            return new File(toURI(resourceUrl).getSchemeSpecificPart());
        } catch (URISyntaxException e) {
            return new File(resourceUrl.getFile());
        }
    }

    public static URL extractJarFileURL(URL jarUrl) throws MalformedURLException {
        String urlFile = jarUrl.getFile();
        int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
        if (separatorIndex != -1) {
            String jarFile = urlFile.substring(0,separatorIndex);
            try {
                return new URL(jarFile);
            } catch (MalformedURLException e) {
                if (!jarFile.startsWith("/")) {
                    jarFile = "/" + jarFile;
                }
                return new URL(FILE_URL_PREFIX + jarFile);
            }
        } else {
            return jarUrl;
        }
    }

    public static URL extractArchiveURL(URL jarUrl) throws MalformedURLException {
        String urlFile = jarUrl.getFile();
        int endIndex = urlFile.indexOf(WAR_URL_SEPARATOR);
        if (endIndex != -1) {
            String warFile = urlFile.substring(0,endIndex);
            if (URL_PROTOCOL_WAR.equals(jarUrl.getProtocol())) {
                return new URL(warFile);
            }
            int startIndex = warFile.indexOf(WAR_URL_PREFIX);
            if (startIndex != -1) {
                return new URL(warFile.substring(startIndex + WAR_URL_PREFIX.length()));
            }
        }
        return extractJarFileURL(jarUrl);
    }

}
