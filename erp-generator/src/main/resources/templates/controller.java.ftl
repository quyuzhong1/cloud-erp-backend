package ${package.Controller};
<#assign fieldMap={}/>
<#assign serviceBean='${table.serviceName?uncap_first}' />
<#list table.fields as field>
    <#assign fieldMap += {field.propertyName:field.propertyName} />
</#list>

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
<#if restControllerStyle>
import org.springframework.web.bind.annotation.RestController;
<#else>
import org.springframework.stereotype.Controller;
</#if>

<#if superControllerClassPackage??>
import ${superControllerClassPackage};
</#if>
import ${package.Service}.${table.serviceName};
import com.common.core.controller.vo.ApiResult;
<#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
<#if dataPermission>
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
</#if>
import ${package.Dto}.${table.dtoName};
</#if>

<#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
import javax.servlet.http.HttpServletResponse;
import java.util.List;
</#if>

/**
 * <p>
 * ${table.comment!}
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
<#if restControllerStyle>
@RestController
<#else>
@Controller
</#if>
@RequestMapping("<#if package.ModuleName??>/${package.ModuleName}</#if>/<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>")
<#if kotlin>
class ${table.controllerName}<#if superControllerClass??> : ${superControllerClass}()</#if>
<#else>
<#if superControllerClass??>
public class ${table.controllerName} extends ${superControllerClass} {
<#else>
public class ${table.controllerName} {
</#if>

    @Autowired
    private ${table.serviceName} ${serviceBean};

   <#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:paging",
            tableAlias = ""
    )
    </#if>
    public ApiResult<List<${table.dtoName}.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(${serviceBean}.tabList(dto));
    }

    /**
    * 列表查询
    * @author ${author}
    * @date: ${date}
    * @param dto
    * @return ApiResult<PagingVO<${table.dtoName}.ListDTO>>
    */
    @PostMapping("/paging")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:paging",
            tableAlias = ""
    )
    </#if>
    public ApiResult<PagingVO<${table.dtoName}.ListDTO>> paging(@RequestBody @Validated PagingDTO<${table.dtoName}.PagingParamDTO> dto) {
        return success(${serviceBean}.paging(dto));
    }

   /**
   * 新增
   * @author ${author}
   * @date:  ${date}
   * @param dto
   * @return ApiResult<Void>
   */
   @PostMapping("/add")
   <#if dataPermission>
   @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
           tableField = "create_user_id",
           menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:add",
           serviceClass = ${table.serviceName}.class,
           keyIdName = "id")
   </#if>
   public ApiResult<Void> add(@RequestBody @Validated ${table.dtoName}.AddDTO dto) {
      ${serviceBean}.add(dto);
      return success();
   }

    /**
    * 修改
    * @author ${author}
    * @date:  ${date}
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/update")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:update",
            serviceClass = ${table.serviceName}.class,
            keyIdName = "id")
    </#if>
    public ApiResult<Void> update(@RequestBody @Validated ${table.dtoName}.UpdateDTO dto) {
        ${serviceBean}.update(dto);
        return success();
    }

    /**
    * 新增并提交审核
    * @author ${author}
    * @date:  ${date}
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:addAndSubmit",
            serviceClass = ${table.serviceName}.class,
            keyIdName = "id")
    </#if>
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated ${table.dtoName}.AddDTO dto) {
        ${serviceBean}.addAndSubmit(dto);
        return success();
    }

    /**
    * 修改并提交审核
    * @author ${author}
    * @date:  ${date}
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:updateAndSubmit",
            serviceClass = ${table.serviceName}.class,
            keyIdName = "id")
    </#if>
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated ${table.dtoName}.UpdateDTO dto) {
        ${serviceBean}.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author ${author}
    * @date:  ${date}
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/submit")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:submit",
            serviceClass = ${table.serviceName}.class,
            keyIdName = "ids")
    </#if>
    public ApiResult<Void> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        ${serviceBean}.submit(dto.getIds());
        return success();
    }

    /**
    * 审核
    * @author ${author}
    * @date:  ${date}
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/approve")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:approve",
            serviceClass = ${table.serviceName}.class,
            keyIdName = "ids")
    </#if>
    public ApiResult<Void> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        ${serviceBean}.approve(dto);
        return success();
    }

    /**
    * 反审核
    * @author ${author}
    * @date:  ${date}
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/disApprove")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:disApprove",
            serviceClass = ${table.serviceName}.class,
            keyIdName = "ids")
    </#if>
    public ApiResult<Void> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        ${serviceBean}.disApprove(dto.getIds());
        return success();
    }


    /**
    * 删除
    * @author ${author}
    * @date:  ${date}
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/delete")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:delete",
            serviceClass = ${table.serviceName}.class,
            keyIdName = "ids")
    </#if>
    public ApiResult<Void> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        ${serviceBean}.delete(dto.getIds());
        return success();
    }
    <#if fieldMap["invalidStatus"]?? && fieldMap["invalidRemark"]??>
    /**
    * 作废
    * @author ${author}
    * @date:  ${date}
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/invalid")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:invalid",
            serviceClass = ${table.serviceName}.class,
            keyIdName = "ids")
    </#if>
    public ApiResult<Void> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        ${serviceBean}.invalid(dto.getIds(), dto.getRemark());
        return success();
    }
    </#if>

    /**
    * 撤销
    * @author ${author}
    * @date:  ${date}
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/cancelProcess")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:cancel",
            serviceClass = ${table.serviceName}.class,
            keyIdName = "ids")
    </#if>
    public ApiResult<Void> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        ${serviceBean}.cancelProcess(dto.getIds());
        return success();
    }

    /**
    * 详情
    * @author ${author}
    * @date:  ${date}
    * @param id
    * @return ApiResult<${table.dtoName}.ViewDTO>>
    */
    @GetMapping("/view")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:view",
            serviceClass = ${table.serviceName}.class,
            keyIdName = "id")
    </#if>
    public ApiResult<${table.dtoName}.ViewDTO> view(@RequestParam("id") String id) {
        return success(${serviceBean}.view(id));
    }

    /**
    * 导出Excel数据
    * @author ${author}
    * @date:  ${date}
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:export",
            tableAlias = ""
    )
    </#if>
    public void exportList(@RequestBody @Validated ${table.dtoName}.ExportDTO dto, HttpServletResponse response) {
        ${serviceBean}.exportList(dto, response);
    }
   </#if>


}
</#if>
