package top.yujiaxin.jfinalplugin.dubbo.support;

import com.jfinal.kit.PathKit;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;

public class ClassUtils {

    private ClassUtils(){}

    /**
     * 扫描指定包名下所有的class
     * @param packageName 要扫描的class的包名
     * @return 返回已加载的class列表
     */
    public static List<Class> scanClass(String packageName) throws ClassNotFoundException, IOException {
        List<String> classNames = new ArrayList<>();
        Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(packageName.replace(".", "/"));
        while (urlEnumeration.hasMoreElements()) {
            URL url = urlEnumeration.nextElement();
            if ("jar".equalsIgnoreCase(url.getProtocol())) {
                //转换为JarURLConnection
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                if (connection == null) break;
                if (connection.getJarFile() == null) break;
                //得到该jar文件下面的类实体
                Enumeration<JarEntry> jarEntryEnumeration = connection.getJarFile().entries();
                while (jarEntryEnumeration.hasMoreElements()) {
                    String jarEntryName = jarEntryEnumeration.nextElement().getName().replace('/', '.');
                    //这里我们需要过滤不是class文件和不在basePack包名下的类
                    if (jarEntryName.contains(".class")
                            && jarEntryName.startsWith(packageName)) {
                        String className = jarEntryName.substring(0, jarEntryName.lastIndexOf("."));
                        classNames.add(className);
//                        classList.add(Class.forName(className));
                    }
                }
            } else if ("file".equalsIgnoreCase(url.getProtocol())){
                File file = new File(url.getFile());
                if (file.isDirectory()){
                    Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            if (file.toString().endsWith(".class")){
                                String className = getClassName(file.toString());
                                if (className.startsWith(packageName)){
                                    classNames.add(className);
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } else if (file.toString().endsWith(".class")){
                    String className = getClassName(file.toString());
                    if (className.startsWith(packageName)){
                        classNames.add(className);
                    }
                }
            }
        }
        List<Class> classes = new ArrayList<>(classNames.size());
        for (String className : classNames) {
            classes.add(Class.forName(className));
        }
        return classes;
    }

    private static String getClassName(String filePath) {
        int start = PathKit.getRootClassPath().length();
        int end = filePath.length() - ".class".length();
        String classFile = filePath.substring(start + 1, end);
        return classFile.replace(File.separator, ".");
    }

}
