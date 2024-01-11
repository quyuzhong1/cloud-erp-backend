package com.erp.server.wms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.server.wms.service.PoReturnService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * SRM供应商退货单
 * @author Luo_WG
 * @since 2023-04-07
 */
@RestController
@LogSystemModule("SRM供应商退货单")
@RequestMapping("/supplierPoReturn")
public class SupplierPoReturnController extends BaseController {

    @Resource
    private PoReturnService poReturnService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.WarehouseReceiveDTO.PagingViewDTO>>
     **/
//    @PostMapping("/paging")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "return_user_id",
//            menuCode = "wms:supplierPoReturn:paging",
//            tableAlias = "pro"
//    )
//    public ApiResult<PagingVO<PurchaseReturnOrderDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PurchaseReturnOrderDTO.PagingParamDTO> dto) {
//        PagingVO<PurchaseReturnOrderDTO.PagingViewDTO> pagingVO = poReturnService.supplierPaging(dto);
//        return success(pagingVO);
//    }

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:14
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.WarehouseReceiveCountDTO>>
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "return_user_id",
            menuCode = "wms:supplierPoReturn:paging",
            tableAlias = "pro")
    public ApiResult<List<PurchaseReturnOrderDTO.ReturnOrderCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<PurchaseReturnOrderDTO.ReturnOrderCountDTO> warehouseReceiveCountDTOS = poReturnService.listCount(dto);
        return success(warehouseReceiveCountDTOS);
    }
}
