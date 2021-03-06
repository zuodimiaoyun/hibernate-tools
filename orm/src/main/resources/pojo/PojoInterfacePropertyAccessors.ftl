<#-- if interface -->
<#-- Property accessors for interface -->
<#foreach property in pojo.getAllPropertiesIterator()><#if pojo.getMetaAttribAsBool(property, "gen-property", true)>
    <#assign fieldComment=c2j.toJavaDoc(c2j.getMetaAsString(property, "field-description"), 4)>

    <#if fieldComment!?trim?has_content>
    /**
${fieldComment}
     */
    </#if>
    ${pojo.getPropertyGetModifiers(property)}${pojo.getJavaTypeName(property, jdk5)} ${pojo.getGetterSignature(property)}();
    
    ${pojo.getPropertySetModifiers(property)}void ${pojo.getSetterSignature(property)}(${pojo.getJavaTypeName(property, jdk5)} ${property.name});
</#if></#foreach>