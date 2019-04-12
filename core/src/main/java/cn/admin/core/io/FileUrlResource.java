package cn.admin.core.io;

import cn.admin.lang.Nullable;
import cn.admin.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class FileUrlResource extends UrlResource implements WritableResource {

    @Nullable
    private volatile File file;

    public FileUrlResource(URL url) {
        super(url);
    }

    public FileUrlResource(String location) throws MalformedURLException {
        super(ResourceUtils.URL_PROTOCOL_FILE,location);
    }

    @Override
    public File getFile() throws IOException {
        File file = this.file;
        if (file != null) {
            return file;
        }
        file = super.getFile();
        this.file = file;
        return file;
    }

    @Override
    public boolean isWritable() {
        try {
            URL url = getURL();
            if (ResourceUtils.isFileURL(url)) {
                File file = getFile();
                return file.canWrite() && !file.isDirectory();
            } else {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return Files.newOutputStream(getFile().toPath());
    }

    @Override
    public WritableByteChannel writableChannel() throws IOException {
        return FileChannel.open(getFile().toPath(), StandardOpenOption.WRITE);
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return new FileUrlResource(new URL(getURL(),relativePath));
    }
}
