package cn.admin.core.io;

import cn.admin.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;

public abstract class AbstractFileResolvingResource extends AbstractResource {

    @Override
    public boolean exists() {
        try {
            URL url = getURL();
            if (ResourceUtils.isFileURL(url)) {
                return getFile().exists();
            } else {
                URLConnection con = url.openConnection();
                customizeConnection(con);
                HttpURLConnection httpCon =
                        con instanceof HttpURLConnection ? (HttpURLConnection) con : null;
                if (httpCon != null) {
                    int code = httpCon.getResponseCode();
                    if (code == HttpURLConnection.HTTP_OK) {
                        return true;
                    } else if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                        return false;
                    }
                }
                if (con.getContentLengthLong() > 0) {
                    return true;
                }
                if (httpCon != null) {
                    httpCon.disconnect();
                    return false;
                } else {
                    getInputStream().close();
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean isReadable() {
        try {
            URL url = getURL();
            if (ResourceUtils.isFileURL(url)) {
                File file = getFile();
                return file.canRead() && !file.isDirectory();
            } else {
                URLConnection con = url.openConnection();
                customizeConnection(con);
                if (con instanceof HttpURLConnection) {
                    HttpURLConnection httpCon = (HttpURLConnection) con;
                    int code = httpCon.getResponseCode();
                    if (code != HttpURLConnection.HTTP_OK) {
                        httpCon.disconnect();
                        return false;
                    }
                }
                long contentLength = con.getContentLength();
                if (contentLength > 0) {
                    return true;
                } else if (contentLength == 0) {
                    return false;
                } else {
                    getInputStream().close();
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean isFile() {
        try {
            URL url = getURL();
            if (url.getProtocol().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
                return VfsResourceDelegate.getResource(url).isFile();
            }
            return ResourceUtils.URL_PROTOCOL_FILE.equals(url.getProtocol());
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public File getFile() throws IOException {
        URL url = getURL();
        if (url.getProtocol().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
            return VfsResourceDelegate.getResource(url).getFile();
        }
        return ResourceUtils.getFile(url, getDescription());
    }

    @Override
    protected File getFileForLastModifiedCheck() throws IOException {
        URL url = getURL();
        if (ResourceUtils.isJarURL(url)) {
            URL actualUrl = ResourceUtils.extractArchiveURL(url);
            if (actualUrl.getProtocol().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
                return VfsResourceDelegate.getResource(actualUrl).getFile();
            }
            return ResourceUtils.getFile(actualUrl,"Jar URL");
        } else {
            return getFile();
        }
    }

    protected boolean isFile(URI uri) {
        try {
            if (uri.getScheme().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
                return VfsResourceDelegate.getResource(uri).isFile();
            }
            return ResourceUtils.URL_PROTOCOL_FILE.equals(uri.getScheme());
        } catch (IOException e) {
            return false;
        }
    }

    protected File getFile(URI uri) throws IOException {
        if (uri.getScheme().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
            return VfsResourceDelegate.getResource(uri).getFile();
        }
        return ResourceUtils.getFile(uri,getDescription());
    }

    @Override
    public ReadableByteChannel readableChannel() throws IOException {
        try {
            return FileChannel.open(getFile().toPath(), StandardOpenOption.READ);
        } catch (FileNotFoundException | NoSuchFileException ex) {
            return super.readableChannel();
        }
    }

    @Override
    public long contentLength() throws IOException {
        URL url = getURL();
        if (ResourceUtils.isFileURL(url)) {
            File file = getFile();
            long length = file.length();
            if (length == 0L && !file.exists()) {
                throw new FileNotFoundException(getDescription() +
                        " cannot be resolved in the file system for checking its content length");
            }
            return length;
        } else {
            URLConnection con = url.openConnection();
            customizeConnection(con);
            return con.getContentLengthLong();
        }
    }

    @Override
    public long lastModified() throws IOException {
        URL url = getURL();
        boolean fileCheck = false;
        if (ResourceUtils.isFileURL(url) || ResourceUtils.isJarURL(url)) {
            // Proceed with file system resolution
            fileCheck = true;
            try {
                File fileToCheck = getFileForLastModifiedCheck();
                long lastModified = fileToCheck.lastModified();
                if (lastModified > 0L || fileToCheck.exists()) {
                    return lastModified;
                }
            }
            catch (FileNotFoundException ex) {
                // Defensively fall back to URL connection check instead
            }
        }
        // Try a URL connection last-modified header
        URLConnection con = url.openConnection();
        customizeConnection(con);
        long lastModified = con.getLastModified();
        if (fileCheck && lastModified == 0 && con.getContentLengthLong() <= 0) {
            throw new FileNotFoundException(getDescription() +
                    " cannot be resolved in the file system for checking its last-modified timestamp");
        }
        return lastModified;
    }

    protected void customizeConnection(URLConnection con) throws IOException {
        ResourceUtils.useCachesIfNecessary(con);
        if (con instanceof HttpURLConnection) {
            customizeConnection((HttpURLConnection) con);
        }
    }

    protected void customizeConnection(HttpURLConnection con) throws IOException {
        con.setRequestMethod("HEAD");
    }

    private static class VfsResourceDelegate {

        public static Resource getResource(URL url) throws IOException {
            return new VfsResource(VfsUtils.getRoot(url));
        }

        public static Resource getResource(URI uri) throws IOException {
            return new VfsResource(VfsUtils.getRoot(uri));
        }

    }
}
