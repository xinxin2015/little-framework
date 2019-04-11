package cn.admin.core.io;

import cn.admin.lang.Nullable;
import cn.admin.util.Assert;
import cn.admin.util.ResourceUtils;
import cn.admin.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;

public class UrlResource extends AbstractFileResolvingResource {

    @Nullable
    private final URI uri;

    private final URL url;

    private final URL cleanedUrl;

    public UrlResource(URI uri) throws MalformedURLException {
        Assert.notNull(uri,"URI must not be null");
        this.uri = uri;
        this.url = uri.toURL();
        this.cleanedUrl = getCleanedUrl(this.url,uri.toString());
    }

    public UrlResource(URL url) {
        Assert.notNull(url,"URL must not be null");
        this.url = url;
        this.cleanedUrl = getCleanedUrl(url,url.toString());
        this.uri = null;
    }

    public UrlResource(String path) throws MalformedURLException {
        Assert.notNull(path,"Path must not be null");
        this.uri = null;
        this.url = new URL(path);
        this.cleanedUrl = getCleanedUrl(this.url,path);
    }

    private URL getCleanedUrl(URL originalUrl,String originalPath) {
        String cleanedPath = StringUtils.cleanPath(originalPath);
        if (!cleanedPath.equals(originalPath)) {
            try {
                return new URL(cleanedPath);
            } catch (MalformedURLException e) {
                //
            }
        }
        return originalUrl;
    }

    public UrlResource(String protocol,String location) throws MalformedURLException {
        this(protocol,location,null);
    }

    public UrlResource(String protocol,String location,@Nullable String fragment) throws MalformedURLException {
        try {
            this.uri = new URI(protocol,location,fragment);
            this.url = this.uri.toURL();
            this.cleanedUrl = getCleanedUrl(this.url,this.uri.toString());
        } catch (URISyntaxException ex) {
            MalformedURLException exToThrow = new MalformedURLException(ex.getMessage());
            exToThrow.initCause(ex);
            throw exToThrow;
        }
    }

    @Override
    public URL getURL() throws IOException {
        return this.url;
    }

    @Override
    public URI getURI() throws IOException {
        if (this.uri != null) {
            return this.uri;
        } else {
            return super.getURI();
        }
    }

    @Override
    public boolean isFile() {
        if (this.uri != null) {
            return super
        }
    }

    @Override
    public String getDescription() {
        return "URL [" + this.url + "]";
    }

    @Override
    public InputStream getInputStream() throws IOException {
        URLConnection con = this.url.openConnection();
        ResourceUtils.useCachesIfNecessary(con);
        try {
            return con.getInputStream();
        } catch (IOException ex) {
            if (con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).disconnect();
            }
            throw ex;
        }
    }
}
