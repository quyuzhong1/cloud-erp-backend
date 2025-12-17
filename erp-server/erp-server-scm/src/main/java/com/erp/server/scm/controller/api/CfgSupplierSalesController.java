package com.erp.server.scm.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.entity.CfgSupplierSalesEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.scm.service.CfgSupplierSalesService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.scm.dto.CfgSupplierSalesDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 销量设置
 *
 * @author jack
 * @since 2025-06-13
 */
@Slf4j
@RestController
@LogSystemModule("销量设置")
@RequestMapping("/cfgSupplierSales")
public class CfgSupplierSalesController extends BaseController {

    @Resource
    private CfgSupplierSalesService cfgSupplierSalesService;

    /**
    * 新增
    * @author jack
    * @date:  2025-06-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "销量设置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgSupplierSalesDTO.CommonDTO dto) {
        return success(cfgSupplierSalesService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-06-13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "销量设置修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "scm:cfgSupplierSales:update",
        serviceClass = CfgSupplierSalesService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgSupplierSalesDTO.CommonDTO dto) {
        cfgSupplierSalesService.update(dto);
        return success();
    }

    /**
     * 详情
     * @author jack
     * @date:  2025-06-16
     * @param id
     * @return ApiResult<CfgThirdNoticeDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:cfgSupplierSales:update",
            serviceClass = CfgSupplierSalesService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CfgSupplierSalesDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgSupplierSalesService.view(id));
    }

    /**
     * 列表查询
     * @author jack
     * @date: 2025-06-13
     * @param pagingParamDTO
     * @return ApiResult<PagingVO<CfgSupplierSalesDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:cfgSupplierSales:paging",
            tableAlias = "css"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<CfgSupplierSalesDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgSupplierSalesDTO.PagingParamDTO> pagingParamDTO) {
        return success(cfgSupplierSalesService.paging(pagingParamDTO));
    }


    /**
     * 删除
     * @author jack
     * @date:  2025-06-13
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:cfgSupplierSales:delete",
            serviceClass = CfgSupplierSalesService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "销量设置删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgSupplierSalesEntity> list = cfgSupplierSalesService.lambdaQuery().in(CfgSupplierSalesEntity::getId, ids).list();
        Map<String, CfgSupplierSalesEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgSupplierSalesEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = cfgSupplierSalesService.delete(id);
            }catch (Exception e){
                log.error("销量设置删除删除失败",e);
                CfgSupplierSalesEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "销量设置删除不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getSupplierName(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 启用/停用
     * @author jack
     * @date:  2025-06-13
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/enable")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:cfgSupplierSales:enable",
            serviceClass = CfgSupplierSalesService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.UPDATE, desc = "销量设置启用/停用")
    public ApiResult<List<BatchResultDTO>> enable(@RequestBody @Validated  CfgSupplierSalesDTO.EnableStatusDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgSupplierSalesEntity> list = cfgSupplierSalesService.lambdaQuery().in(CfgSupplierSalesEntity::getId, ids).list();
        Map<String, CfgSupplierSalesEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgSupplierSalesEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = cfgSupplierSalesService.enable(id,dto.getDisabled());
            }catch (Exception e){
                log.error("销量设置更新失败",e);
                CfgSupplierSalesEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "销量设置不存在, 更新失败");
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
     * @date:  2025-06-13
     * @param pagingParamDTO
     * @param response
     * @return
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:cfgSupplierSales:export",
            tableAlias = "css"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    @WebAdvanceQuery
    public ApiResult<Object> exportList(@RequestBody @Validated CfgSupplierSalesDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {
        cfgSupplierSalesService.exportList(pagingParamDTO, response);
        return success();
    }

    /**
     * 根据供应商id查询 字段显示
     * @author jack
     * @date: 2025-06-25
     */
    @GetMapping("/getDisplayField")
    public ApiResult<List<String>> getDisplayField() {
        return success(cfgSupplierSalesService.getDisplayField());
    }

}
