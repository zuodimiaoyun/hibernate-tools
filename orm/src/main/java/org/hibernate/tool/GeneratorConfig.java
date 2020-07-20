package org.hibernate.tool;

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
    private static String includeTables;
    private static String excludeTables;
    private static String classSuffix;
    private static String author;
    private static String db;
    private static GenClassType genClassType;
    private static String entitySuffix;
    private static String daoSuffix;
    private static final Map<String, String> apiClassMapping = new HashMap<>();
    private static final Map<String, String> entityClassMapping = new HashMap<>();



    public static void setInterfaceLoadPackage(String loadPackage){
        List<Class<?>> classes = GeneratorUtil.getClasses(loadPackage);
        classes.forEach( c -> apiClassMapping.put(c.getSimpleName(), c.getName()));
    }

    public static void setEntityLoadPackage(String loadPackage){
        List<Class<?>> entityClasses = GeneratorUtil.getClasses(loadPackage);
        entityClasses.forEach( c -> entityClassMapping.put(c.getSimpleName(), c.getName()));
    }

    public static void setIncludeTables(String includeTables) {
        GeneratorConfig.includeTables = includeTables;
    }

    public static void setExcludeTables(String excludeTables) {
        GeneratorConfig.excludeTables = excludeTables;
    }

    public static void setClassSuffix(String classSuffix) {
        GeneratorConfig.classSuffix = classSuffix;
    }

    public static void setAuthor(String author) {
        GeneratorConfig.author = author;
    }

    public static void setDb(String db) {
        GeneratorConfig.db = db;
    }

    public static void setGenClassType(GenClassType genClassType) {
        GeneratorConfig.genClassType = genClassType;
    }

    public static String getIncludeTables() {
        return includeTables;
    }

    public static String getExcludeTables() {
        return excludeTables;
    }

    public static String getClassSuffix() {
        return classSuffix == null ? "" : classSuffix;
    }

    public static String getAuthor() {
        return author;
    }

    public static String getDb() {
        return db;
    }

    public static GenClassType getGenClassType() {
        return genClassType;
    }

    public static void setGenEntity(){
        setGenClassType(GenClassType.ENTITY);
    }

    public static void setGenInterface(){
        setGenClassType(GenClassType.INTERFACE);
    }

    public static String getEntitySuffix() {
        return entitySuffix;
    }

    public static void setEntitySuffix(String entitySuffix) {
        GeneratorConfig.entitySuffix = entitySuffix;
    }

    public static String getDaoSuffix() {
        return daoSuffix;
    }

    public static void setDaoSuffix(String daoSuffix) {
        GeneratorConfig.daoSuffix = daoSuffix;
    }

    public static boolean isGenInterface(){
        return getGenClassType() == GenClassType.INTERFACE;
    }

    public static boolean isGenEntity(){
        return getGenClassType() == GenClassType.ENTITY;
    }

    public static String getInterfaceClassByName(String interfaceName){
        return apiClassMapping.get(interfaceName);
    }

    public static String getEntityClassByName(String entityName){
        return entityClassMapping.get(entityName);
    }

    public enum GenClassType{
        /**
         * 生成类的类型
         */
        INTERFACE,
        ENTITY
    }
}
