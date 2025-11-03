package ${package.Service};
<#assign fieldMap={}/>
<#list table.fields as field>
 <#assign fieldMap += {field.propertyName:field.propertyName} />
</#list>
import ${package.Entity}.${entity};
import ${superServiceClassPackage};
import com.common.business.dto.base.*;
import ${package.Dto}.${table.dtoName};
<#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
import com.common.business.vo.PagingVO;
import com.common.business.dto.ApproveDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
</#if>

/**
 * <p>
 * ${table.comment!} 服务类
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
<#if kotlin>
interface ${table.serviceName} : ${superServiceClass}<${entity}>
<#else>
public interface ${table.serviceName} extends ${superServiceClass}<${entity}> {

    /**
    * 新增
    * @author ${author}
    * @date: ${date}
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(${table.dtoName}.AddDTO dto);

    /**
    * 修改
    * @author ${author}
    * @date: ${date}
    * @param dto
    * @return
    */
    Boolean update(${table.dtoName}.UpdateDTO dto);

    <#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
    /**
    * 分页列表查询
    * @author ${author}
    * @date: ${date}
    * @param pagingParamDTO
    * @return PagingVO<${table.dtoName}.ListDTO>>
    */
    PagingVO<${table.dtoName}.ListDTO> paging(PagingDTO<${table.dtoName}.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author ${author}
    * @date: ${date}
    * @param dto
    * @return List<${table.dtoName}.TabListDTO>>
    */
    List<${table.dtoName}.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author ${author}
    * @date: ${date}
    * @param id
    * @return
    */
    ${table.dtoName}.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author ${author}
    * @date: ${date}
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(${table.dtoName}.AddDTO dto);

    /**
    * 修改并提交审核
    * @author ${author}
    * @date: ${date}
    * @param dto
    * @return
    */
    void updateAndSubmit(${table.dtoName}.UpdateDTO dto);

     /**
     * 提交审核
     * @author ${author}
     * @date: ${date}
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author ${author}
    * @date: ${date}
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author ${author}
    * @date: ${date}
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author ${author}
    * @date: ${date}
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    <#if fieldMap["invalidStatus"]?? && fieldMap["invalidRemark"]??>
    /**
    * 作废
    * @author ${author}
    * @date: ${date}
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);
    </#if>

    /**
    * 撤销
    * @author ${author}
    * @date: ${date}
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto);

    /**
    * 导出Excel
    * @author ${author}
    * @date: ${date}
    * @param dto
    * @param response
    * @return
    */
    void exportList(${table.dtoName}.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, ${entity} entity);
    </#if>

}
</#if>
