/*
 * Hibernate Tools, Tooling for your Hibernate Projects
 * 
 * Copyright 2016-2020 Red Hat, Inc.
 *
 * Licensed under the GNU Lesser General Public License (LGPL), 
 * version 2.1 or later (the "License").
 * You may not use this file except in compliance with the License.
 * You may read the licence in the 'lgpl.txt' file in the root folder of 
 * project or obtain a copy at
 *
 *     http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.tool.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.tools.ant.BuildException;
import org.hibernate.tool.GeneratorConfig;
import org.hibernate.tool.api.metadata.MetadataDescriptor;
import org.hibernate.tool.api.metadata.MetadataDescriptorFactory;
import org.hibernate.tool.api.metadata.MetadataConstants;
import org.hibernate.tool.api.reveng.RevengSettings;
import org.hibernate.tool.api.reveng.RevengStrategy;
import org.hibernate.tool.api.reveng.RevengStrategyFactory;

public abstract class AbstractGenerationMojo extends AbstractMojo {

    // For reveng strategy
    /** The default package name to use when mappings for classes are created. */
    @Parameter
    private String packageName;
   
    /** The name of a property file, e.g. hibernate.properties. */
    @Parameter
    private File revengFile;
    
    /** The class name of the reverse engineering strategy to use.
     * Extend the DefaultReverseEngineeringStrategy and override the corresponding methods, e.g.
     * to adapt the generate class names or to provide custom type mappings. */
    @Parameter
    private String revengStrategy;

    /** If true, tables which are pure many-to-many link tables will be mapped as such.
     * A pure many-to-many table is one which primary-key contains exactly two foreign-keys pointing
     * to other entity tables and has no other columns. */
    @Parameter(defaultValue = "true")
    private boolean detectManyToMany;

    /** If true, a one-to-one association will be created for each foreignkey found. */
    @Parameter(defaultValue = "true")
    private boolean detectOneToOne;

    /** If true, columns named VERSION or TIMESTAMP with appropriate types will be mapped with the appropriate
     * optimistic locking corresponding to &lt;version&gt; or &lt;timestamp&gt;. */
    @Parameter(defaultValue = "true")
    private boolean detectOptimisticLock;

    /** If true, a collection will be mapped for each foreignkey. */
    @Parameter(defaultValue = "true")
    private boolean createCollectionForForeignKey;

    /** If true, a many-to-one association will be created for each foreignkey found. */
    @Parameter(defaultValue = "true")
    private boolean createManyToOneForForeignKey;

    // For configuration
    /** The name of a property file, e.g. hibernate.properties. */
    @Parameter(defaultValue = "${project.basedir}/src/main/hibernate/hibernate.properties")
    private File propertyFile;

    @Parameter(defaultValue = "Entity")
    private String entitySuffix;

    @Parameter(defaultValue = "Repository")
    private String daoSuffix;

    @Parameter(defaultValue = "any")
    private String author;

    @Parameter(defaultValue = ".*")
    private String includeTables;

    @Parameter(defaultValue = "(flyway_.*)|(qrtz_.*)")
    private String excludeTables;

    @Parameter(defaultValue = "oneops")
    private String db;

    @Parameter(defaultValue = "com.beisen.oneops.api.model")
    private String interfaceLoadPackage;

    @Parameter(defaultValue = "com.beisen.oneops.internal.entity")
    private String entityLoadPackage;

    @Parameter(defaultValue = "true")
    private boolean overWrite;

    // Not exposed for now
    private boolean preferBasicCompositeIds = true;

    public void execute() {
        settingConfig();
        getLog().info("Starting " + this.getClass().getSimpleName() + "...");
        RevengStrategy strategy = setupReverseEngineeringStrategy();
        Properties properties = loadPropertiesFile();
        MetadataDescriptor jdbcDescriptor = createJdbcDescriptor(strategy, properties);
        executeExporter(jdbcDescriptor);
        getLog().info("Finished " + this.getClass().getSimpleName() + "!");
    }

    public void settingConfig() {
        GeneratorConfig.setInterfaceLoadPackage(interfaceLoadPackage);
        GeneratorConfig.setEntityLoadPackage(entityLoadPackage);
        GeneratorConfig.setAuthor(author);
        GeneratorConfig.setDb(db);
        GeneratorConfig.setExcludeTables(excludeTables);
        GeneratorConfig.setIncludeTables(includeTables);
        GeneratorConfig.setEntitySuffix(entitySuffix);
        GeneratorConfig.setDaoSuffix(daoSuffix);
        GeneratorConfig.setOverWrite(overWrite);
        if(this instanceof GenerateDaoMojo){
            GeneratorConfig.setGenEntity();
            GeneratorConfig.setClassSuffix(daoSuffix);
        }else if(this instanceof GenerateJavaMojo){
            GeneratorConfig.setGenEntity();
            GeneratorConfig.setClassSuffix(entitySuffix);
        }else if(this instanceof GenerateEntityInterfaceMojo){
            GeneratorConfig.setGenInterface();
            GeneratorConfig.setClassSuffix("");
        }
    }

    private RevengStrategy setupReverseEngineeringStrategy() {
    	File[] revengFiles = null;
    	if (revengFile != null) {
    		revengFiles = new File[] { revengFile };
    	}
        RevengStrategy strategy = 
        		RevengStrategyFactory.createReverseEngineeringStrategy(
        				revengStrategy, 
        				revengFiles);
        RevengSettings settings =
                new RevengSettings(strategy)
                        .setDefaultPackageName(packageName)
                        .setDetectManyToMany(detectManyToMany)
                        .setDetectOneToOne(detectOneToOne)
                        .setDetectOptimisticLock(detectOptimisticLock)
                        .setCreateCollectionForForeignKey(createCollectionForForeignKey)
                        .setCreateManyToOneForForeignKey(createManyToOneForForeignKey);
        strategy.setSettings(settings);
        return strategy;
    }

    private Properties loadPropertiesFile() {
        if (propertyFile == null) {
            return null;
        }

        Properties properties = new Properties();
        try (FileInputStream is = new FileInputStream(propertyFile)) {
            properties.load(is);
            return properties;
        } catch (FileNotFoundException e) {
            throw new BuildException(propertyFile + " not found.", e);
        } catch (IOException e) {
            throw new BuildException("Problem while loading " + propertyFile, e);
        }
    }

    private MetadataDescriptor createJdbcDescriptor(RevengStrategy strategy, Properties properties) {
    	properties.put(MetadataConstants.PREFER_BASIC_COMPOSITE_IDS, preferBasicCompositeIds);
        return MetadataDescriptorFactory
                .createReverseEngineeringDescriptor(
                        strategy,
                        properties);
    }

    protected abstract void executeExporter(MetadataDescriptor metadataDescriptor);
}
