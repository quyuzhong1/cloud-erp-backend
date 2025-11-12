package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.SoB2cProcessingDTO;
import com.erp.server.wms.service.SoB2cProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * B2C虚拟仓订单跟踪
 *
 * @author will
 * @since 2024-12-18
 */
@Slf4j
@RestController
@LogSystemModule("B2C虚拟仓订单跟踪")
@RequestMapping("/soB2cProcessing")
public class SoB2cProcessingController extends BaseController {

    @Resource
    private SoB2cProcessingService soB2cProcessingService;

    /**
     * 分页查询
     * @author will
     * @date 2024/12/18 11:30
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "sbp.warehouse_id",
            shopTableField = "sbp.shop_id",
            menuCode = "wms:soB2cProcessing:paging"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<SoB2cProcessingDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SoB2cProcessingDTO.PagingParamDTO> dto) {
        return success(soB2cProcessingService.paging(dto));
    }

    /**
     * 导出
     * @author will
     * @date 2024/12/18 11:51
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/exportExcel")
    @LogAction(value = LogActionEnum.EXPORT, desc = "B2C虚拟仓列表信息导出")
    public ApiResult exportExcel(@RequestBody SoB2cProcessingDTO.PagingParamDTO dto) {
        Boolean flag = soB2cProcessingService.exportExcel(dto);
        return flag == true ? success() : failure();
    }
}
