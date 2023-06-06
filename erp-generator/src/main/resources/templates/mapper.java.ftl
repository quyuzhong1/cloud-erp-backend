package ${package.Mapper};
<#assign fieldMap={}/>
<#list table.fields as field>
 <#assign fieldMap += {field.propertyName:field.propertyName} />
</#list>
import ${package.Entity}.${entity};
import ${superMapperClassPackage};

import org.apache.ibatis.annotations.Mapper;
<#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import ${package.Dto}.${table.dtoName};
import com.common.business.dto.base.ApproveStatusQtyDTO;
</#if>

<#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
import java.util.List;
</#if>

/**
 * <p>
 * ${table.comment!} Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
<#if kotlin>
interface ${table.mapperName} : ${superMapperClass}<${entity}>
<#else>
@Mapper
public interface ${table.mapperName} extends ${superMapperClass}<${entity}> {

    <#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<${table.dtoName}.ListDTO> paging(Page query, @Param("params") ${table.dtoName}.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") ${table.dtoName}.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<${table.dtoName}.ListDTO> listExport(@Param("params") ${table.dtoName}.ExportDTO params);
    </#if>

}
</#if>
