package ${package.Service};
<#assign fieldMap={}/>
<#list table.fields as field>
 <#assign fieldMap += {field.propertyName:field.propertyName} />
</#list>
import ${package.Entity}.${entity};
import ${superServiceClassPackage};

<#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import ${package.Dto}.${table.dtoName};

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
     * 新增
     * @author ${author}
     * @date: ${date}
     * @param dto
     * @return
     */
     String add(${table.dtoName}.AddDTO dto);

     /**
     * 修改
     * @author ${author}
     * @date: ${date}
     * @param dto
     * @return
     */
     void update(${table.dtoName}.UpdateDTO dto);

     /**
     * 新增并提交审核
     * @author ${author}
     * @date: ${date}
     * @param dto
     * @return
     */
     void addAndSubmit(${table.dtoName}.AddDTO dto);

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
     * @param ids
     * @return
     */
     void submit(List<String> ids);

    /**
    * 审核
    * @author ${author}
    * @date: ${date}
    * @param dto
    * @return
    */
    void approve(BaseApproveParamDTO dto);

    /**
    * 反审核
    * @author ${author}
    * @date: ${date}
    * @param ids
    * @return
    */
    void disApprove(List<String> ids);

    /**
    * 删除
    * @author ${author}
    * @date: ${date}
    * @param ids
    * @return
    */
    void delete(List<String> ids);
    <#if fieldMap["invalidStatus"]?? && fieldMap["invalidRemark"]??>
    /**
    * 作废
    * @author ${author}
    * @date: ${date}
    * @param ids
    * @param remark
    * @return
    */
    void invalid(List<String> ids, String remark);
    </#if>

    /**
    * 撤销
    * @author ${author}
    * @date: ${date}
    * @param ids
    * @return
    */
    void cancelProcess(List<String> ids);

    /**
    * 导出Excel
    * @author ${author}
    * @date: ${date}
    * @param dto
    * @param response
    * @return
    */
    void exportList(${table.dtoName}.ExportDTO dto, HttpServletResponse response);
    </#if>

}
</#if>
