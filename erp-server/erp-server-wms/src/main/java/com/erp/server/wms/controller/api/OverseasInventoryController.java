package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.OverseasInventoryDTO;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.server.wms.service.OverseasInventoryService;
import com.erp.server.wms.service.OverseasProviderWarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 海外仓库存
 *
 * @author Jim
 * @since 2023-11-16
 */
@Slf4j
@RestController
@LogSystemModule("海外仓库存")
@RequestMapping("/overseasInventory")
public class OverseasInventoryController extends BaseController {

    @Resource
    private OverseasInventoryService overseasInventoryService;
    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;


    /**
     * 列表查询
     * @author Jim
     * @date:  2023-11-16
     * @return ApiResult
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:overseasInventory:paging",
            tableAlias = "oi"
    )
    public ApiResult<PagingVO<OverseasInventoryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<OverseasInventoryDTO.PagingParamDTO> dto) {
        PagingVO<OverseasInventoryDTO.ListDTO> result = overseasInventoryService.paging(dto);
        return success(result);
    }


    /**
     * 列表查询合计
     * @author Jim
     * @date:  2023-11-21
     * @return ApiResult
     */
    @PostMapping("/total")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:overseasInventory:paging",
            tableAlias = "oi"
    )
    public ApiResult<OverseasInventoryDTO.ListTotalDTO> queryParamsTotal(@RequestBody @Validated OverseasInventoryDTO.PagingParamDTO dto) {
        OverseasInventoryDTO.ListTotalDTO resultDTO = overseasInventoryService.queryParamsTotal(dto);
        return success(resultDTO);
    }

    /**
     * 海外仓库存导出
     * @author Jim
     * @date:  2023-11-21
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出海外仓库存")
    @PostMapping(value = "/exportExcel")
    public ApiResult<?> exportExcel(@RequestBody @Validated OverseasInventoryDTO.ExportDTO dto) {
        Boolean flag = overseasInventoryService.exportExcel(dto);
        return flag ? success() : failure();
    }

    /**
     * 独立站配送信息查询
     *
     */
    @PostMapping("/shopifyShippedInfo")
    public ApiResult<List<OverseasProviderWarehouseDTO.ShippedViewDTO>> getShippedInfo(@RequestBody OverseasProviderWarehouseDTO.ShippedDTO shippedDTO) {
        return success(overseasProviderWarehouseService.getShippedInfo(shippedDTO));
    }
}
