package com.erp.server.scm.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.scm.dto.SupplierRefWarehouseDTO;
import com.erp.model.scm.entity.SupplierRefWarehouseEntity;
import com.erp.server.scm.service.SupplierRefWarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 仓库绑定
 *
 * @author will
 * @since 2025-06-18
 */
@Slf4j
@RestController
@LogSystemModule("供应商关联仓库表")
@RequestMapping("/supplierRefWarehouse")
public class SupplierRefWarehouseController extends BaseController {

    @Resource
    private SupplierRefWarehouseService supplierRefWarehouseService;

    /**
     * 获取状态统计
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:supplierRefWarehouse:paging",
            tableAlias = "srw"
    )
    public ApiResult<List<SupplierRefWarehouseDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(supplierRefWarehouseService.tabList(dto));
    }

    /**
     * 分页查询
     * @author will
     * @date 2025/6/18 16:58
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:supplierRefWarehouse:paging",
            tableAlias = "srw"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<SupplierRefWarehouseDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SupplierRefWarehouseDTO.PagingParamDTO> dto) {
        return success(supplierRefWarehouseService.paging(dto));
    }


    /**
    * 新增
    * @author will
    * @date:  2025-06-18
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "供应商关联仓库表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SupplierRefWarehouseDTO.AddDTO dto) {
        return success(supplierRefWarehouseService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2025-06-18
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "供应商关联仓库表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "scm:supplierRefWarehouse:update",
        serviceClass = SupplierRefWarehouseService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SupplierRefWarehouseDTO.UpdateDTO dto) {
        supplierRefWarehouseService.update(dto);
        return success();
    }


    /**
     * 详情
     * @author will
     * @date:  2025-06-16
     * @param id
     * @return ApiResult<ContractInfoDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<SupplierRefWarehouseDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(supplierRefWarehouseService.view(id));
    }


    /**
     * 删除
     * @author will
     * @date:  2025-06-16
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/batchDelete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:supplierRefWarehouse:batchDelete",
            serviceClass = SupplierRefWarehouseService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "仓库绑定删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SupplierRefWarehouseEntity> list = supplierRefWarehouseService.lambdaQuery().in(SupplierRefWarehouseEntity::getId, ids).list();
        Map<String, SupplierRefWarehouseEntity> idEntityMap = list.stream().collect(Collectors.toMap(SupplierRefWarehouseEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = supplierRefWarehouseService.delete(id);
            }catch (Exception e){
                log.error("仓库绑定删除失败",e);
                SupplierRefWarehouseEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "仓库绑定不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getSupplierCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 更新禁用状态
     * @author will
     * @date:  2025-06-16
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/updateDisabled")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:supplierRefWarehouse:updateDisabled",
            serviceClass = SupplierRefWarehouseService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.UPDATE_STATUS, desc = "仓库绑定启用")
    public ApiResult<List<BatchResultDTO>> updateDisabled(@RequestBody @Validated SupplierRefWarehouseDTO.DisabledDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SupplierRefWarehouseEntity> list = supplierRefWarehouseService.lambdaQuery().in(SupplierRefWarehouseEntity::getId, ids).list();
        Map<String, SupplierRefWarehouseEntity> idEntityMap = list.stream().collect(Collectors.toMap(SupplierRefWarehouseEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = supplierRefWarehouseService.updateDisabled(id,dto.getDisabled());
            }catch (Exception e){
                log.error("更新禁用状态失败",e);
                SupplierRefWarehouseEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "仓库绑定不存在, 更新禁用状态失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getSupplierCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 下载模板
     * @author will
     * @date 2025/6/18 17:56
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        supplierRefWarehouseService.downloadTemplate(response);
        return success();
    }


    /**
     * 导入excek
     * @author will
     * @date 2025/6/18 17:56
     * @param excelFile
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入")
    @PostMapping("/importExcel")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = supplierRefWarehouseService.importExcel(excelFile, response);
        return result?success():failure();
    }

    /**
     * 导出
     * @author will
     * @date 2025/6/18 17:14
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/exportExcel")
    @WebAdvanceQuery
    public ApiResult exportExcel(@RequestBody SupplierRefWarehouseDTO.PagingParamDTO dto) {
        Boolean flag = supplierRefWarehouseService.exportExcel(dto);
        return flag ? success() : failure();
    }
}
