package com.erp.server.wms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.AfterSalePackDetailDTO;
import com.erp.server.wms.service.AfterSalePackDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 售后装箱明细表
 *
 * @author lei.nie
 * @since 2026-05-12
 */
@Slf4j
@RestController
@LogSystemModule("售后装箱明细表")
@RequestMapping("/afterSalePackDetail")
public class AfterSalePackDetailController extends BaseController {

    @Resource
    private AfterSalePackDetailService afterSalePackDetailService;

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author lei.nie
     * @date: 2026-05-12
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "售后装箱明细表修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:afterSalePackDetail:update",
            serviceClass = AfterSalePackDetailService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AfterSalePackDetailDTO.UpdateDTO dto) {
        afterSalePackDetailService.update(dto);
        return success();
    }

    /**
     * 详情
     *
     * @param id
     * @return ApiResult<AfterSalePackDetailDTO.ViewDTO>>
     * @author lei.nie
     * @date: 2026-05-12
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:afterSalePackDetail:view",
            serviceClass = AfterSalePackDetailService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<AfterSalePackDetailDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(afterSalePackDetailService.view(id));
    }

    /**
     * 根据箱唛查询详情列表
     *
     * @param code String
     * @return ApiResult<List < AfterSalePackDetailDTO.ViewDTO>>
     * @author lei.nie
     * @date: 2026-05-12
     */
    @GetMapping("/listByCode")
    @LogViewService
    public ApiResult<List<AfterSalePackDetailDTO.ViewDTO>> listByCode(@RequestParam("code") String code, @RequestParam(value = "sourceId", required = false) String sourceId) {
        return success(afterSalePackDetailService.listByCode(code, sourceId));
    }

}
