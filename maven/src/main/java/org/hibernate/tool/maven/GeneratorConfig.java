package org.hibernate.tool.maven;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author yangshuaichao
 * @date 2020/07/16 11:29
 * @description TODO
 */
public class GeneratorConfig {
    private static final Properties properties = new Properties();
    private static final String INCLUDE_TABLES = "includeTables";
    private static final String EXCLUDE_TABLES = "excludeTables";
    private static final String CLASS_SUFFIX = "classSuffix";
    private static final String AUTHOR = "author";
    private static final String DB = "db";
    private static final String GEN_CLASS_TYPE = "genClassType";
    private static final String USE_LOMBOK = "useLombok";

    private static final String INTERFACE_PACKAGE = "interfacePackage";

    private static final Map<String, String> apiClassMapping = new HashMap<>();

    static{
        try {
            properties.load(new FileInputStream("src/main/resources/generator.properties"));
            List<Class<?>> classes = GeneratorUtil.getClasses(getInterfacePackage());
            classes.forEach( c -> apiClassMapping.put(c.getSimpleName(), c.getName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String getClassSuffix(){
        String suffix = properties.getProperty(CLASS_SUFFIX);
        return suffix == null ? "" : suffix;
    }

    public static String getAutor(){
        return properties.getProperty(AUTHOR);
    }

    public static String getIncludeTables(){
        return properties.getProperty(INCLUDE_TABLES);
    }

    public static String getExcludeTables(){
        return properties.getProperty(EXCLUDE_TABLES);
    }

    public static String getDb(){
        return properties.getProperty(DB);
    }

    public static String getGenClassType(){
        return properties.getProperty(GEN_CLASS_TYPE);
    }

    public static String getInterfacePackage(){
        return properties.getProperty(INTERFACE_PACKAGE);
    }

    public static boolean useLombok(){
        return Boolean.parseBoolean(properties.getProperty(USE_LOMBOK));
    }

    public static boolean isGenInterface(){
        return getGenClassType().equals(GenClassType.INTERFACE.name());
    }

    public static boolean isGenEntity(){
        return getGenClassType().equals(GenClassType.ENTITY.name());
    }

    public static String getInterfaceClassByName(String interfaceName){
        return apiClassMapping.get(interfaceName);
    }

    public enum GenClassType{
        /**
         * 生成类的类型
         */
        INTERFACE,
        ENTITY
    }
}
