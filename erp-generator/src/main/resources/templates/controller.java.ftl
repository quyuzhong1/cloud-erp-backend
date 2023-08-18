package ${package.Controller};
<#assign fieldMap={}/>
<#assign serviceBean='${table.serviceName?uncap_first}' />
<#list table.fields as field>
    <#assign fieldMap += {field.propertyName:field.propertyName} />
</#list>
<#assign docName = "${table.comment!}">
<#if docName?ends_with("表")>
    <#assign docName = docName[0..<docName?length-1] + "单">
</#if>

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
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
import java.util.*;
import ${package.Entity}.${entity};
</#if>

/**
 * ${table.comment!}
 *
 * @author ${author}
 * @since ${date}
 */
@Slf4j
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
   * @return ApiResult<String>
   */
   @PostMapping("/add")
   <#if dataPermission>
   @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
           tableField = "create_user_id",
           menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:add",
           serviceClass = ${table.serviceName}.class,
           keyIdName = "id")
   </#if>
   public ApiResult<String> add(@RequestBody @Validated ${table.dtoName}.AddDTO dto) {
      return success(${serviceBean}.add(dto));
   }

    /**
    * 修改
    * @author ${author}
    * @date:  ${date}
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:update",
            serviceClass = ${table.serviceName}.class,
            keyIdName = "id")
    </#if>
    public ApiResult update(@RequestBody @Validated ${table.dtoName}.UpdateDTO dto) {
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
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:submit",
            serviceClass = ${table.serviceName}.class,
            keyIdName = "ids")
    </#if>
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = ${serviceBean}.submit(id);
            }catch (Exception e){
                log.error("${docName} 提交审核失败",e);
                ${entity} entity = ${serviceBean}.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, "${docName}不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return success(resultDTOS);
    }

    /**
    * 审核
    * @author ${author}
    * @date:  ${date}
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:approve",
            serviceClass = ${table.serviceName}.class,
            keyIdName = "ids")
    </#if>
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = ${serviceBean}.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("${docName}审核失败",e);
                ${entity} entity = ${serviceBean}.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(entity.getCode(), "${docName}不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return success(resultDTOS);
    }

    /**
    * 反审核
    * @author ${author}
    * @date:  ${date}
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:disApprove",
            serviceClass = ${table.serviceName}.class,
            keyIdName = "ids")
    </#if>
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = ${serviceBean}.disApprove(id);
            }catch (Exception e){
                log.error("${docName}反审核失败",e);
                ${entity} entity = ${serviceBean}.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(entity.getCode(), "${docName}不存在, 反审核失败");
                    resultDTOS.add(disApproveResult);
                    continue;
                }
                disApproveResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(disApproveResult);
        }
        return success(resultDTOS);
    }


    /**
    * 删除
    * @author ${author}
    * @date:  ${date}
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:delete",
            serviceClass = ${table.serviceName}.class,
            keyIdName = "ids")
    </#if>
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = ${serviceBean}.delete(id);
            }catch (Exception e){
                log.error("${docName}删除失败",e);
                ${entity} entity = ${serviceBean}.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(entity.getCode(), "${docName}不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return success(resultDTOS);
    }
    <#if fieldMap["invalidStatus"]?? && fieldMap["invalidRemark"]??>
    /**
    * 作废
    * @author ${author}
    * @date:  ${date}
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:invalid",
            serviceClass = ${table.serviceName}.class,
            keyIdName = "ids")
    </#if>
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = ${serviceBean}.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("${docName}作废失败",e);
                ${entity} entity = ${serviceBean}.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(entity.getCode(), "${docName}不存在, 作废失败");
                    resultDTOS.add(invalidResult);
                    continue;
                }
                invalidResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(invalidResult);
        }
        return success(resultDTOS);
    }
    </#if>

    /**
    * 撤销
    * @author ${author}
    * @date:  ${date}
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    <#if dataPermission>
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "${modelName}<#if package.ModuleName??>:${package.ModuleName}</#if>:<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>:cancel",
            serviceClass = ${table.serviceName}.class,
            keyIdName = "ids")
    </#if>
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = ${serviceBean}.cancelProcess(id);
            }catch (Exception e){
                log.error("${docName}撤回流程失败",e);
                ${entity} entity = ${serviceBean}.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(entity.getCode(), "${docName}不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return success(resultDTOS);
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
