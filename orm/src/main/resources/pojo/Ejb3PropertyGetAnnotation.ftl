<#if ejb3>
<#if pojo.hasIdentifierProperty()>
<#if property.equals(clazz.identifierProperty)>
${pojo.generateAnnIdGenerator()}
</#if>
</#if>
<#if c2h.isOneToOne(property)>
${pojo.generateOneToOneAnnotation(property, md)}
<#elseif c2h.isManyToOne(property)>
${pojo.generateManyToOneAnnotation(property)}
<#--TODO support optional and targetEntity-->
${pojo.generateJoinColumnsAnnotation(property, md)}
<#elseif c2h.isCollection(property)>
${pojo.generateCollectionAnnotation(property, md)}
<#else>
<#assign basicAnno="${pojo.generateBasicAnnotation(property)}">
<#assign annColumnAnno="${pojo.generateAnnColumnAnnotation(property)}">
<#if basicAnno!?trim?has_content>
${basicAnno}
</#if>
<#if annColumnAnno!?trim?has_content>
${annColumnAnno}
</#if>
</#if>
<#if pojo.isCreatedAtProperty(property)>
${pojo.getCreatedAtAnnotation()}
<#elseif pojo.isCreatedByProperty(property)>
${pojo.getCreatedByAnnotation()}
<#elseif pojo.isModifiedAtProperty(property)>
${pojo.getModifiedAtAnnotation()}
<#elseif pojo.isModifiedByProperty(property)>
${pojo.getModifiedByAnnotation()}
</#if>
</#if>