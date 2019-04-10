package cn.admin.core.io;

import cn.admin.core.NestedIOException;
import cn.admin.lang.Nullable;
import cn.admin.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public abstract class AbstractResource implements Resource {

    @Override
    public boolean exists() {
        try {
            return getFile().exists();
        } catch (IOException ex) {
            try {
                getInputStream().close();
                return true;
            } catch (Throwable t) {
                return false;
            }
        }
    }

    @Override
    public boolean isReadable() {
        return exists();
    }


    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public URL getURL() throws IOException {
        throw new FileNotFoundException(getDescription() + " cannot be resolved to URL");
    }

    @Override
    public URI getURI() throws IOException {
        URL url = getURL();
        try {
            return ResourceUtils.toURI(url);
        } catch (URISyntaxException ex) {
            throw new NestedIOException("Invalid URI [" + url + "]", ex);
        }
    }

    @Override
    public File getFile() throws IOException {
        throw new FileNotFoundException(getDescription() + " cannot be resolved to absolute file path");
    }

    @Override
    public ReadableByteChannel readableChannel() throws IOException {
        return Channels.newChannel(getInputStream());
    }

    @Override
    public long contentLength() throws IOException {
        try (InputStream is = getInputStream()) {
            long size = 0;
            byte[] buffer = new byte[256];
            int read;
            while ((read = is.read(buffer)) != -1) {
                size += read;
            }
            return size;
        }
    }

    @Override
    public long lastModified() throws IOException {
        File fileToCheck = getFileForLastModifiedCheck();
        long lastModified = fileToCheck.lastModified();
        if (lastModified == 0L && !fileToCheck.exists()) {
            throw new FileNotFoundException(getDescription() +
                    " cannot be resolved in the file system for checking its last-modified timestamp");
        }
        return lastModified;
    }

    protected File getFileForLastModifiedCheck() throws IOException {
        return getFile();
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        throw new FileNotFoundException("Cannot create a relative resource for " + getDescription());
    }

    @Override
    @Nullable
    public String getFilename() {
        return null;
    }

    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof Resource &&
                ((Resource) other).getDescription().equals(getDescription())));
    }

    @Override
    public int hashCode() {
        return getDescription().hashCode();
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
