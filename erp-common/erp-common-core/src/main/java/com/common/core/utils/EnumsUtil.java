package com.common.core.utils;

import com.common.core.constant.EnumMessage;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**统一处理所有 你的项目的包名.enums下面的枚举类 （需要implements EnumMessage），根据值和枚举类型可以获得枚举对象。
 * */
public class EnumsUtil {


    /**
     * 枚举类对应的包路径
     */
    public final static String PACKAGE_NAME = "com.common.core.constant";
    /**
     * 枚举接口类全路径
     */
    public final static String ENUM_MESSAGE_PATH=PACKAGE_NAME+".EnumMessage";

    /**
     * 枚举类对应的全路径集合
     */
    public static final List<String> ENUM_OBJECT_PATH = getClassName();

    /**
     * 存放单个枚举对象 map常量定义
     */
    private static Map<Integer, EnumMessage> SINGLE_ENUM_MAP = null;
    /**
     * 所有枚举对象的 map
     */
    public static final Map<Class, Map<Integer, EnumMessage>> ENUM_MAP = initialEnumMap(true);


    /**静态初始化块*/
    static {

    }

    /**
     * 加载所有枚举对象数据
     * @param  isFouceCheck 是否强制校验枚举是否实现了EnumMessage接口
     *
     * */
    private static Map<Class, Map<Integer, EnumMessage>> initialEnumMap(boolean isFouceCheck){
        Map<Class, Map<Integer, EnumMessage>> ENUM_MAP = new HashMap<Class, Map<Integer, EnumMessage>>();
        try {
            for (String classname : ENUM_OBJECT_PATH) {
                Class<?> cls = null;
                cls = Class.forName(classname);
                Class <?>[]iter=cls.getInterfaces();
                boolean flag=false;
                if(isFouceCheck){
                    for(Class cz:iter){
                        if(cz.getName().equals(ENUM_MESSAGE_PATH)){
                            flag=true;
                            break;
                        }
                    }
                }
                if(flag==isFouceCheck){
                    SINGLE_ENUM_MAP = new HashMap<Integer, EnumMessage>();
                    initialSingleEnumMap(cls);
                    ENUM_MAP.put(cls, SINGLE_ENUM_MAP);
                }

            }
        } catch (Exception e) {

        }
        return ENUM_MAP;
    }

    /**
     * 加载每个枚举对象数据
     * */
    private static void  initialSingleEnumMap(Class<?> cls )throws Exception{
        Method method = cls.getMethod("values");
        EnumMessage inter[] = (EnumMessage[]) method.invoke(null, null);
        for (EnumMessage enumMessage : inter) {
            SINGLE_ENUM_MAP.put(enumMessage.getCode(), enumMessage);
        }
    }


    private static List<String> getClassName() {

        List<String> myClassName = new ArrayList<String>();
        Set<Class<?>> classSet= getClasses(PACKAGE_NAME);
        for (Class<?> classitem:
                classSet) {
            myClassName.add(classitem.getName());
        }

        return myClassName;
    }

    public static Set<Class<?>> getClasses(String pack){

        // 第一个class类的集合
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try{
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()){
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                }else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    JarFile jar;
                    try{
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()){
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                if ((idx != -1) || recursive) {
                                    // 如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        // 去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try{
                                            // 添加到classes
                                            classes.add(Class.forName(packageName + '.' + className));
                                        }catch (ClassNotFoundException e){

                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }catch (IOException e){

                        e.printStackTrace();
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return classes;
    }
    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    public static void findAndAddClassesInPackageByFile(
            String packageName,
            String packagePath,
            final boolean recursive,
            Set<Class<?>> classes){
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter(){

            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file){
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirfiles){
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            }else{
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try{
                    // 添加到集合中去
                    // forName会触发static方法，没有使用classLoader的load干净
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                }catch (ClassNotFoundException e){
                    e.printStackTrace();
                }
            }
        }
    }



    /**
     * 获取value返回枚举对象
     * @param value
     * @param clazz */
    public static <T extends EnumMessage>  T getEnumObject(Integer value, Class<?> clazz){

        T retobj= (T)ENUM_MAP.get(clazz).get(value);

        if(retobj == null){
            throw  new ServiceException(ApiError.Default);
        }
        return retobj;
    }

}
