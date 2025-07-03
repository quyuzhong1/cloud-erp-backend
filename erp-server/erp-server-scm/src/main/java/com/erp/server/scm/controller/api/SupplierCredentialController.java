package com.erp.server.scm.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.model.scm.entity.SupplierCredentialEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.server.scm.query.SupplierCredentialQueryHandler;
import com.erp.server.scm.service.SupplierCredentialService;
import com.erp.server.scm.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 供应商证照表
 *
 * @author jack
 * @since 2025-06-21
 */
@Slf4j
@RestController
@RequestMapping("/SupplierCredential")
public class SupplierCredentialController extends BaseController {

    @Resource
    private SupplierCredentialService supplierCredentialService;

    /**
    * 新增
    * @author jack
    * @date:  2025-06-21
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "供应商证照新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody  @Validated SupplierCredentialDTO. AddListDTO dto) {
        SupplierCredentialDTO.AddDTO addDTO = dto.getList().stream().filter(e -> StringUtils.isNotBlank(e.getSupplierId())).findFirst().orElse(null);
        if(Objects.isNull(addDTO)){
            return failure();
        }
        supplierCredentialService.saveBatchCredential(dto.getList());
        return success();
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-06-21
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "供应商证照修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "scm:SupplierCredential:update",
        serviceClass = SupplierCredentialService.class,
        keyIdName = "id")
    public ApiResult<Object> update(@RequestBody  @Validated SupplierCredentialDTO. UpdateListDTO dto) {
        SupplierCredentialDTO.UpdateDTO updateDTO = dto.getList().stream().filter(e -> StringUtils.isNotBlank(e.getSupplierId())).findFirst().orElse(null);
        if(Objects.isNull(updateDTO)){
            return failure();
        }
        supplierCredentialService.updateBatchCredential(dto.getList());
        return success();
    }

    /**
     * 获取状态统计
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:SupplierCredential:paging",
            tableAlias = "sc"
    )
    public ApiResult<List<SupplierCredentialDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(supplierCredentialService.tabList(dto));
    }

    /**
     * 列表查询
     * @author jack
     * @date: 2025-06-21
     * @param dto
     * @return ApiResult<PagingVO<SupplierCredentialDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:SupplierCredential:paging",
            tableAlias = "sc"
    )
    @WebAdvanceQuery(handler = SupplierCredentialQueryHandler.class)
    public ApiResult<PagingVO<SupplierCredentialDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SupplierCredentialDTO.PagingParamDTO> dto) {
        return success(supplierCredentialService.paging(dto));
    }


    /**
     * 详情
     * @author jack
     * @date:  2025-06-21
     * @return ApiResult<SupplierCredentialDTO.ViewDTO>>
     */
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:SupplierCredential:view",
            serviceClass = SupplierCredentialService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<List<SupplierCredentialDTO.ViewDTO>> view(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(supplierCredentialService.view(dto.getIds()));
    }


    /**
     * 删除
     * @author jack
     * @date:  2025-06-21
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:SupplierCredential:delete",
            serviceClass = SupplierCredentialService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "供应商证照删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SupplierCredentialEntity> list = supplierCredentialService.lambdaQuery().in(SupplierCredentialEntity::getId, ids).list();
        Map<String, SupplierCredentialEntity> idEntityMap = list.stream().collect(Collectors.toMap(SupplierCredentialEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = supplierCredentialService.delete(id);
            }catch (Exception e){
                log.error("供应商证照删除失败",e);
                SupplierCredentialEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "供应商证照不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }else{
                    deleteResult = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
                }
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 删除
     * @author jack
     * @date:  2025-06-21
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/updateStatus")
    @LogAction(value = LogActionEnum.DELETE, desc = "供应商证照更新时间")
    public ApiResult<List<BatchResultDTO>> updateStatus(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SupplierCredentialEntity> list = supplierCredentialService.lambdaQuery().in(SupplierCredentialEntity::getId, ids).list();
        Map<String, SupplierCredentialEntity> idEntityMap = list.stream().collect(Collectors.toMap(SupplierCredentialEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = supplierCredentialService.updateStatus(id);
            }catch (Exception e){
                log.error("供应商证照更新失败",e);
                SupplierCredentialEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "供应商证照不存在, 更新失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出Excel数据
     * @author jack
     * @date:  2025-06-21
     * @param dto
     * @param response
     * @return
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:SupplierCredential:export",
            tableAlias = "sc"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    @WebAdvanceQuery(handler = SupplierCredentialQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated SupplierCredentialDTO.PagingParamDTO dto, HttpServletResponse response) {
        supplierCredentialService.exportList(dto, response);
        return success();
    }


    /**
     * 保存拜访管理字典
     * @param dto
     * @return DictBasicDTO
     */
    @PostMapping("/addDictCredential")
    public ApiResult<DictBasicDTO> addDictCredential(@RequestBody @Validated SupplierCredentialDTO.DictCredentialDTO dto) {
        DictBasicDTO dictBasicDTO = supplierCredentialService.addDictCredential(dto.getCredentialName());
        return Objects.nonNull(dictBasicDTO) ? success(dictBasicDTO) : failure();
    }


}
