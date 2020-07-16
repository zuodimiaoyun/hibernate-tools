package org.hibernate.tool.maven;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.MetaAttribute;
import org.hibernate.mapping.Table;
import org.hibernate.tool.api.reveng.AssociationInfo;
import org.hibernate.tool.api.reveng.TableIdentifier;
import org.hibernate.tool.internal.reveng.strategy.DefaultStrategy;
import org.jboss.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import java.beans.Introspector;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author yangshuaichao
 * @date 2020/07/11 12:19
 * @description TODO
 */
public class MyRevengStrategy extends DefaultStrategy {
    static final private Logger log = Logger.getLogger(MyRevengStrategy.class);
    static final Map<Integer, String> typeMapping = new HashMap<>();

    static final String classDesc = "@author %s\n" +
        "@date %s\n" +
        "@description %s";

    public MyRevengStrategy() {
        super();

    }

    static {
        typeMapping.put(Types.TIMESTAMP, "java.time.LocalDateTime");
        typeMapping.put(Types.DATE, "java.time.LocalDate");
        typeMapping.put(Types.TIME, "java.time.LocalTime");
        typeMapping.put(Types.NUMERIC, "java.lang.Double");
        typeMapping.put(Types.DECIMAL, "java.lang.Double");
        typeMapping.put(Types.CHAR, "java.lang.String");

    }

    @Override
    public List<SchemaSelection> getSchemaSelections() {
        return Collections.singletonList(new SchemaSelection(){
            @Override
            public String getMatchCatalog() {
                return GeneratorConfig.getDb();
            }

            @Override
            public String getMatchSchema() {
                return GeneratorConfig.getDb();
            }

            @Override
            public String getMatchTable() {
                return null;
            }
        });
    }

    @Override
    public String columnToPropertyName(TableIdentifier table, String columnName) {
        return super.columnToPropertyName(table, columnName);
    }

    @Override
    public String tableToClassName(TableIdentifier tableIdentifier) {
        return super.tableToClassName(tableIdentifier) + GeneratorConfig.getClassSuffix();
    }


    @Override
    public boolean excludeTable(TableIdentifier ti) {
        return !ti.getName().matches(GeneratorConfig.getIncludeTables()) || ti.getName().matches(GeneratorConfig.getExcludeTables());
    }


    @Override
    public Map<String, MetaAttribute> tableToMetaAttributes(Table table) {
        Map<String, MetaAttribute> tableMeta = new HashMap<>();
        //class-description
        String comment = table.getComment() == null || table.getComment().isEmpty() ? "TODO" : table.getComment();
        MetaAttribute classDescMeta = new MetaAttribute("class-description");
        classDescMeta.addValue(String.format(classDesc, GeneratorConfig.getAutor(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), comment));
        tableMeta.put(classDescMeta.getName(),classDescMeta);
        //interface
        if(GeneratorConfig.isGenInterface()){
            MetaAttribute interfaceMeta = new MetaAttribute("interface");
            interfaceMeta.addValue("true");
            tableMeta.put(interfaceMeta.getName(),interfaceMeta);
        }
        return tableMeta;
    }



    @Override
    public String foreignKeyToEntityName(String keyname, TableIdentifier fromTable, List<?> fromColumnNames, TableIdentifier referencedTable, List<?> referencedColumnNames, boolean uniqueReference) {
        String propertyName = getTrimSuffixSimpleClassName(referencedTable);

        if(!uniqueReference) {
            if(fromColumnNames!=null && fromColumnNames.size()==1) {
                String columnName = ( (Column) fromColumnNames.get(0) ).getName();
                propertyName = toUpperCamelCase(GeneratorUtil.trimSuffix(columnName, "_id"));
            }
            else { // composite key or no columns at all safeguard
                propertyName = propertyName + "By" + toUpperCamelCase(keyname);
            }
        }
        return Introspector.decapitalize(propertyName);
    }

    @Override
    public String foreignKeyToCollectionName(String keyname, TableIdentifier fromTable, List<?> fromColumns, TableIdentifier referencedTable, List<?> referencedColumns, boolean uniqueReference) {
        String propertyName = pluralize(Introspector.decapitalize(getTrimSuffixSimpleClassName(fromTable)));
        if(!uniqueReference) {
            if(fromColumns!=null && fromColumns.size()==1) {
                String columnName = ( (Column) fromColumns.get(0) ).getName();
                propertyName = propertyName + "For" + toUpperCamelCase(columnName);
            }
            else { // composite key or no columns at all safeguard
                propertyName = propertyName + "For" + toUpperCamelCase(keyname);
            }
        }
        return propertyName;
    }


    @Override
    public String columnToHibernateTypeName(TableIdentifier table, String columnName, int sqlType, int length, int precision, int scale, boolean nullable, boolean generatedIdentifier) {
        String preferredHibernateType = typeMapping.get(sqlType);
        if(preferredHibernateType != null){
            return preferredHibernateType;
        }
        return super.columnToHibernateTypeName(table, columnName, sqlType, length, precision, scale, nullable, generatedIdentifier);
    }

    @Override
    public String getEntityProxyInterfaceClass(String entityClassName) {
        if(GeneratorConfig.isGenInterface() || entityClassName == null){
            return null;
        }
        String simpleEntityName = getTrimSuffixSimpleClassName(entityClassName);
        String interfaceClassName = GeneratorConfig.getInterfaceClassByName(simpleEntityName);
        if(interfaceClassName != null){
            return interfaceClassName;
        }
        return super.getEntityProxyInterfaceClass(entityClassName);
    }

    @Override
    public AssociationInfo foreignKeyToAssociationInfo(ForeignKey foreignKey) {
        return new AssociationInfo() {
            @Override
            public String getCascade() {
                return CascadeType.ALL.name();
            }

            @Override
            public String getFetch() {
                return FetchType.LAZY.name();
            }

            @Override
            public Boolean getUpdate() {
                return null;
            }

            @Override
            public Boolean getInsert() {
                return null;
            }
        };
    }

    @Override
    public String foreignKeyToManyToManyName(ForeignKey fromKey, TableIdentifier middleTable, ForeignKey toKey, boolean uniqueReference) {
        String propertyName = Introspector.decapitalize( getTrimSuffixSimpleClassName( TableIdentifier.create(toKey.getReferencedTable())) );
        propertyName = pluralize( propertyName );

        if(!uniqueReference) {
            //TODO: maybe use the middleTable name here ?
            if(toKey.getColumns()!=null && toKey.getColumns().size()==1) {
                String columnName = ( (Column) toKey.getColumns().get(0) ).getName();
                propertyName = propertyName + "For" + toUpperCamelCase(columnName);
            }
            else { // composite key or no columns at all safeguard
                propertyName = propertyName + "For" + toUpperCamelCase(toKey.getName());
            }
        }
        return propertyName;
    }

    @Override
    public Map<String, MetaAttribute> columnToMetaAttributes(Table table, String columnName) {
        Column column = table.getColumn(Identifier.toIdentifier(columnName));
        if(column == null){
            return null;
        }
        if(column.getComment() != null && !column.getComment().isEmpty()){
            String descMetaName = "field-description";
            MetaAttribute descMeta = new MetaAttribute(descMetaName);
            descMeta.addValue(column.getComment());
            Map<String, MetaAttribute> metas = new HashMap<>();
            metas.put(descMeta.getName(), descMeta);
            return metas;
        }
        return null;
    }

    private String getTrimSuffixSimpleClassName(TableIdentifier tableIdentifier){
        return getTrimSuffixSimpleClassName(getRoot().tableToClassName(tableIdentifier));
    }

    private String getTrimSuffixSimpleClassName(String className){
        return GeneratorUtil.trimSuffix(StringHelper.unqualify(className), GeneratorConfig.getClassSuffix());
    }


    public static void main(String[] args) {
        byte[] bs = new byte[]{84,65,66,76,69};
        System.out.println(new String(bs));
    }
}
