package vip.justlive.oxygen.core.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.PathMatcher;
import vip.justlive.oxygen.core.util.ResourceUtils;

/**
 * 抽象资源加载器
 * <br>
 * 支持查找classpath下配置文件，例如 classpath:/config/dev.properties,classpath*:/config/*.properties
 * <br>
 * 支持查找文件系统下配置文件，例如 file:/home/dev.properties, file:D:/conf/dev.properties
 *
 * @author wubo
 */
@Slf4j
public abstract class AbstractResourceLoader {

  public static final String ALL_CLASSPATH_PREFIX = "classpath*:";
  public static final String CLASSPATH_PREFIX = "classpath:";
  public static final String FILE_PREFIX = "file:";

  /**
   * 类加载器
   */
  protected ClassLoader loader;

  /**
   * 路径匹配器
   */
  protected PathMatcher matcher = new PathMatcher();

  /**
   * 资源列表
   */
  protected List<SourceResource> resources = new ArrayList<>();

  /**
   * 未找到是否忽略，启用则跳过，否则抛出异常
   */
  protected boolean ignoreNotFound;

  /**
   * 文件编码
   */
  protected String encoding;

  /**
   * 字符集
   */
  protected Charset charset;

  /**
   * 是否已初始化过
   */
  protected volatile boolean ready;

  /**
   * 初始化
   */
  public abstract void init();

  public void setIgnoreNotFound(boolean ignoreNotFound) {
    this.ignoreNotFound = ignoreNotFound;
  }

  public boolean isIgnoreNotFound() {
    return ignoreNotFound;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  public Charset getCharset() {
    return charset;
  }

  /**
   * 获取资源列表
   *
   * @return 资源列表
   */
  public List<SourceResource> resources() {
    return this.resources;
  }

  /**
   * 获取资源Reader
   *
   * @param resource 源资源
   * @return Reader
   * @throws IOException io异常
   */
  protected Reader getReader(SourceResource resource) throws IOException {
    if (this.charset != null) {
      return new InputStreamReader(resource.getInputStream(), this.charset);
    } else if (this.encoding != null) {
      return new InputStreamReader(resource.getInputStream(), this.encoding);
    } else {
      return new InputStreamReader(resource.getInputStream());
    }
  }

  /**
   * 解析路径
   *
   * @param locations 路径
   * @return 资源列表
   */
  protected List<SourceResource> parse(String... locations) {
    List<SourceResource> list = new LinkedList<>();
    for (String location : locations) {
      try {
        if (location.startsWith(ALL_CLASSPATH_PREFIX)) {
          list.addAll(
              this.resolveAllClassPathResource(location.substring(ALL_CLASSPATH_PREFIX.length())));
        } else if (location.startsWith(CLASSPATH_PREFIX)) {
          list.addAll(this.resolveClassPathResource(location.substring(CLASSPATH_PREFIX.length())));
        } else if (location.startsWith(FILE_PREFIX)) {
          list.addAll(this.resolveFileSystemResource(location));
        } else {
          list.addAll(this.resolveClassPathResource(location));
        }
      } catch (IOException e) {
        if (log.isDebugEnabled()) {
          log.debug("location [{}] cannot find resource", location, e);
        }
        if (!ignoreNotFound) {
          throw Exceptions.wrap(e);
        }
      }
    }
    return list;
  }

  /**
   * 处理所有classpath下的资源
   *
   * @param location 路径
   * @return 资源列表
   * @throws IOException io异常
   */
  protected List<SourceResource> resolveAllClassPathResource(String location) throws IOException {
    List<SourceResource> list = new LinkedList<>();
    if (matcher.isPattern(location)) {
      list.addAll(this.findMatchPath(location));
    } else {
      if (location.startsWith(Constants.ROOT_PATH)) {
        location = location.substring(Constants.ROOT_PATH.length());
      }
      Enumeration<URL> res = loader.getResources(location);
      if (res == null) {
        return list;
      }
      while (res.hasMoreElements()) {
        URL url = res.nextElement();
        list.add(new UrlResource(url));
      }
    }
    return list;
  }

  /**
   * 处理classpath下的资源
   *
   * @param location 路径
   * @return 资源列表
   * @throws IOException io异常
   */
  protected List<SourceResource> resolveClassPathResource(String location) throws IOException {
    List<SourceResource> list = new LinkedList<>();
    if (matcher.isPattern(location)) {
      list.addAll(this.findMatchPath(location));
    } else {
      list.add(new ClassPathResource(location, loader));
    }
    return list;
  }

  /**
   * 处理文件系统下的资源
   *
   * @param location 路径
   * @return 资源列表
   * @throws IOException io异常
   */
  protected List<SourceResource> resolveFileSystemResource(String location) throws IOException {
    List<SourceResource> list = new LinkedList<>();
    if (matcher.isPattern(location)) {
      list.addAll(this.findMatchPath(location));
    } else {
      list.add(new FileSystemResource(location.substring(FILE_PREFIX.length())));
    }
    return list;
  }

  /**
   * 获取匹配的路径
   *
   * @param location 路径
   * @return 资源列表
   * @throws IOException io异常
   */
  protected List<SourceResource> findMatchPath(String location) throws IOException {
    List<SourceResource> all = new LinkedList<>();
    String rootPath = this.getRootDir(location);
    String subPattern = location.substring(rootPath.length());
    List<SourceResource> rootResources = this.parse(rootPath);
    for (SourceResource resource : rootResources) {
      URL rootUrl = resource.getURL();
      if (Constants.URL_PROTOCOL_FILE.equals(rootUrl.getProtocol())) {
        all.addAll(this.findFileMatchPath(resource, subPattern));
      } else if (ResourceUtils.isJarURL(rootUrl)) {
        all.addAll(this.findJarMatchPath(resource, rootUrl, subPattern));
      } else {
        // TODO others
      }
    }
    return all;
  }

  /**
   * 获取资源下匹配的jar中的文件
   *
   * @param resource 源
   * @param rootUrl 根路径
   * @param subPattern 匹配串
   * @return 资源列表
   * @throws IOException io异常
   */
  protected List<SourceResource> findJarMatchPath(SourceResource resource, URL rootUrl,
      String subPattern) throws IOException {
    List<SourceResource> all = new LinkedList<>();
    URLConnection con = rootUrl.openConnection();
    JarFile jarFile;
    String jarFileUrl;
    String rootEntryPath;

    if (con instanceof JarURLConnection) {
      JarURLConnection jarCon = (JarURLConnection) con;
      jarFile = jarCon.getJarFile();
      jarFileUrl = jarCon.getJarFileURL().toExternalForm();
      JarEntry jarEntry = jarCon.getJarEntry();
      rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");
    } else {
      String urlFile = rootUrl.getFile();
      int separatorLength = Constants.WAR_URL_SEPARATOR.length();
      int separatorIndex = urlFile.indexOf(Constants.WAR_URL_SEPARATOR);
      if (separatorIndex == -1) {
        separatorIndex = urlFile.indexOf(Constants.JAR_URL_SEPARATOR);
        separatorLength = Constants.JAR_URL_SEPARATOR.length();
      }
      if (separatorIndex != -1) {
        jarFileUrl = urlFile.substring(0, separatorIndex);
        rootEntryPath = urlFile.substring(separatorIndex + separatorLength);
        jarFile = this.getJarFile(jarFileUrl);
      } else {
        jarFile = new JarFile(urlFile);
        jarFileUrl = urlFile;
        rootEntryPath = "";
      }
    }

    try {
      this.look(resource, subPattern, all, jarFile, jarFileUrl, rootEntryPath);
    } finally {
      jarFile.close();
    }
    return all;
  }

  /**
   * 查找jar中匹配的资源
   *
   * @param resource 源
   * @param subPattern 匹配串
   * @param all 所有资源
   * @param jarFile jar文件
   * @param jarFileUrl jar文件路径
   * @param rootEntryPath 根路径
   * @throws IOException io异常
   */
  private void look(SourceResource resource, String subPattern, List<SourceResource> all,
      JarFile jarFile, String jarFileUrl, String rootEntryPath) throws IOException {
    if (log.isDebugEnabled()) {
      log.debug("Looking for matching resources in jar file [" + jarFileUrl + "]");
    }
    if (rootEntryPath.length() > 0 && !rootEntryPath.endsWith(Constants.PATH_SEPARATOR)) {
      rootEntryPath += Constants.PATH_SEPARATOR;
    }
    for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
      JarEntry entry = entries.nextElement();
      String entryPath = entry.getName();
      if (entryPath.startsWith(rootEntryPath)) {
        String relativePath = entryPath.substring(rootEntryPath.length());
        if (matcher.match(subPattern, relativePath)) {
          all.add(resource.createRelative(relativePath));
        }
      }
    }
  }

  /**
   * 获取jar文件
   *
   * @param jarFileUrl jar文件路径
   * @return jar文件
   * @throws IOException io异常
   */
  protected JarFile getJarFile(String jarFileUrl) throws IOException {
    if (jarFileUrl.startsWith(FILE_PREFIX)) {
      try {
        return new JarFile(ResourceUtils.toURI(jarFileUrl).getSchemeSpecificPart());
      } catch (URISyntaxException ex) {
        // 失败可能是因为url不正确，去除协议尝试
        return new JarFile(jarFileUrl.substring(FILE_PREFIX.length()));
      }
    } else {
      return new JarFile(jarFileUrl);
    }
  }


  /**
   * 获取资源下匹配的文件
   *
   * @param resource 源
   * @param subPattern 匹配串
   * @return 资源列表
   * @throws IOException io异常
   */
  protected List<SourceResource> findFileMatchPath(SourceResource resource, String subPattern)
      throws IOException {
    List<SourceResource> list = new LinkedList<>();
    File rootDir = resource.getFile().getAbsoluteFile();
    Set<File> matchedFiles = findMatchedFiles(rootDir, subPattern);
    for (File file : matchedFiles) {
      list.add(new FileSystemResource(file));
    }
    return list;
  }

  /**
   * 获取目录下匹配的文件
   *
   * @param rootDir 根目录
   * @param subPattern 匹配串
   * @return 文件列表
   */
  protected Set<File> findMatchedFiles(File rootDir, String subPattern) {
    Set<File> files = new LinkedHashSet<>();
    if (!rootDir.exists() || !rootDir.isDirectory() || !rootDir.canRead()) {
      if (log.isWarnEnabled()) {
        log.warn("dir [{}] cannot execute search operation", rootDir.getPath());
      }
      return files;
    }
    String fullPattern =
        rootDir.getAbsolutePath().replace(File.separator, Constants.PATH_SEPARATOR);
    if (!subPattern.startsWith(Constants.PATH_SEPARATOR)) {
      fullPattern += Constants.PATH_SEPARATOR;
    }
    fullPattern += subPattern.replace(File.separator, Constants.PATH_SEPARATOR);
    this.searchMatchedFiles(fullPattern, rootDir, files);
    return files;
  }

  /**
   * 递归查询匹配的文件
   *
   * @param fullPattern 全匹配串
   * @param dir 目录
   * @param files 文件集合
   */
  protected void searchMatchedFiles(String fullPattern, File dir, Set<File> files) {
    if (log.isDebugEnabled()) {
      log.debug("search files under dir [{}]", dir);
    }
    File[] dirContents = dir.listFiles();
    for (File content : dirContents) {
      String currentPath =
          content.getAbsolutePath().replace(File.separator, Constants.PATH_SEPARATOR);
      if (content.isDirectory()) {
        if (!content.canRead() && log.isDebugEnabled()) {
          log.debug("dir [{}] has no read permission, skip");
        } else {
          this.searchMatchedFiles(fullPattern, content, files);
        }
      } else if (this.matcher.match(fullPattern, currentPath)) {
        files.add(content);
      }
    }
  }

  /**
   * 获取不含通配符的根路径
   *
   * @param location 路径
   * @return 根路径
   */
  protected String getRootDir(String location) {
    int prefixEnd = location.indexOf(Constants.COLON_SEPARATOR) + 1;
    int rootDirEnd = location.length();
    while (rootDirEnd > prefixEnd && matcher.isPattern(location.substring(prefixEnd, rootDirEnd))) {
      rootDirEnd = location.lastIndexOf(Constants.PATH_SEPARATOR, rootDirEnd - 2) + 1;
    }
    if (rootDirEnd == 0) {
      rootDirEnd = prefixEnd;
    }
    return location.substring(0, rootDirEnd);
  }

}
