package ${package.Dto};
<#---------新增或修改CommonDTO不显示字段------------------>
<#assign updateNoShow=["code", "approveStatus", "invalidStatus", "invalidRemark", "approveTime", "approveUserName", "approveUserId"]>
<#assign fieldMap={}/>
<#list table.fields as field>
    <#assign fieldMap += {field.propertyName:field.propertyName} />
</#list>

<#list table.importPackages as pkg>
import ${pkg};
</#list>
<#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
import com.common.business.dto.base.SortDTO;
import java.util.List;
</#if>
<#if swagger2>
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
</#if>
<#if entityLombokModel>
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
</#if>
<#if dtoValidate>
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
</#if>
<#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
import javax.validation.constraints.NotEmpty;
</#if>
<#list table.fields as field>
<#if field.propertyType == 'BigDecimal'>
import javax.validation.constraints.Digits;
<#break>
</#if>
</#list>

/**
 * <p>
 * ${table.comment!}请求响应实体
 * </p>
 *
 * @author ${author}
 * @since ${date}
*/
<#if entityLombokModel>
@Data
@NoArgsConstructor
</#if>
public class ${table.dtoName} implements Serializable {

<#if entitySerialVersionUID>
    private static final long serialVersionUID = 1L;
</#if>

     <#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {

         /**
         * 类型
         */
         private String searchType;

         /**
         * 数量
         */
         private Integer count;

     }
     </#if>
     <#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
     /**
     * 分页列表查询参数
     */
     @Data
     @NoArgsConstructor
     public static class PagingParamDTO extends SortDTO {

         /**
         * 搜索类型
         */
         private String  searchType;

     }
     </#if>
    <#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
    /**
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id
        */
        private String  id;

        <#-- ----------  BEGIN 字段循环遍历  ---------->
        <#list table.fields as field>
        <#if field.comment!?length gt 0>
        /**
        * ${field.comment}
        */
        </#if>
        private ${field.propertyType} ${field.propertyName};
        </#list>
        <#------------  END 字段循环遍历  ---------->

        /**
        * 审核状态名称
        */
        private String approveStatusName;
        <#if fieldMap["invalidStatus"]??>
        /**
        * 作废状态名称
        */
        private String invalidStatusName;
        </#if>
        <#------------ 业务单据默认带上skuId吧------------->
        /**
        * sku id
        */
        private String skuId;
        /**
        * 产品名称
        */
        private String productName;
        <#------------ 如果包含approveStatus字段则展示createTime字段-------------->
        <#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
        /**
        * 创建时间
        */
        private LocalDateTime createTime;
        /**
        * 创建人名称
        */
        private String createUserName;
        </#if>
    }
    </#if>

    <#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
    }
    </#if>

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        <#list table.fields as field>
        <#if field.comment!?length gt 0>
        /**
        * ${field.comment}
        */
        </#if>
        private ${field.propertyType} ${field.propertyName};
        </#list>

    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>
    <#if field.keyFlag>
        <#assign keyPropertyName="${field.propertyName}"/>
    </#if>
    <#----------如果是业务单据类部分字段新增修改不展示------------------->
    <#if !updateNoShow?seq_contains("${field.propertyName}")>
    <#if field.comment!?length gt 0>
        /**
        * ${field.comment}
        */
    </#if>
    <#if dtoValidate>
        <#if field.notNullField && (field.propertyType == 'Long' || field.propertyType == 'LocalDateTime' || field.propertyType == 'BigDecimal' || field.propertyType == 'Integer'  || field.propertyType == 'Boolean')>
        @NotNull(message = "${field.validComment}不能为空")
        <#elseif field.notNullField && field.propertyType == 'String'>
        @NotBlank(message = "${field.validComment}不能为空")
        </#if>
    </#if>
    <#if field.notNullField && field.propertyType == 'String'>
        @Size(max = ${field.maxLength},message = "${field.validComment}最大长度不能超过${field.maxLength}位")
    </#if>
    <#if field.notNullField && field.propertyType == 'BigDecimal'  && field.maxPreciseLength = 0>
        @Digits(integer = ${field.maxIntegerLength}, fraction = ${field.maxPreciseLength}, message = "${field.validComment}最大长度不能超过${field.maxIntegerLength}位")
    </#if>
    <#if field.notNullField && field.propertyType == 'BigDecimal'  && field.maxPreciseLength gt 0>
        @Digits(integer = ${field.maxIntegerLength}, fraction = ${field.maxPreciseLength}, message = "${field.validComment}整数位不能超过${field.maxIntegerLength}位，小数位不能超过${field.maxPreciseLength}位")
    </#if>
        <#----------如果是业务单据类部分字段新增修改不展示------------------->
        private ${field.propertyType} ${field.propertyName};
        </#if>
</#list>
<#------------  END 字段循环遍历  ---------->

    }


}