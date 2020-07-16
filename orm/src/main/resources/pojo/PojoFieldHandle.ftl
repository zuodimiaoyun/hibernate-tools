<#if pojo.hasDeleteProperty()>
    @PreRemove
    public void delete() {
        this.${pojo.getIsDeletedProperty().getName()} = true;
    }
</#if>