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
import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import com.erp.model.wms.dto.AliexpressDeliveryDTO;
import com.erp.server.wms.query.AliexpressDeliveryQueryHandler;
import com.erp.server.wms.service.AliexpressDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 速卖通发货单
 *
 * @author Luo_WG
 * @since 2024-01-26
 */
@Slf4j
@RestController
@LogSystemModule("速卖通发货单")
@RequestMapping("/aliexpressDelivery")
public class AliexpressDeliveryController extends BaseController {

    @Resource
    private AliexpressDeliveryService aliexpressDeliveryService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2024/1/26 16:26
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<AliexpressDeliveryDTO.ListDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:aliexpressDelivery:paging",
            tableAlias = "ad"
    )
    @WebAdvanceQuery(handler = AliexpressDeliveryQueryHandler.class)
    public ApiResult<PagingVO<AliexpressDeliveryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AliexpressDeliveryDTO.SearchParamDTO> dto) {
        PagingVO<AliexpressDeliveryDTO.ListDTO> pagingVO = aliexpressDeliveryService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 导出excel
     * @Author Luo_WG
     * @Date 2024/1/26 16:51
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出速卖通发货单")
    @PostMapping(value = "/exportExcel")
    public ApiResult<T> exportExcel(@RequestBody @Validated AliexpressDeliveryDTO.SearchParamDTO dto) {
        Boolean flag = aliexpressDeliveryService.exportExcel(dto);
        return flag ? success() : failure();
    }


    /**
     * 下拉用户拥有权限的速卖通店铺
     * @Author Luo_WG
     * @Date 2024/1/31 11:11
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.oms.dto.ShopSysUserAuthDTO.ViewShopDTO>>
     **/
    @GetMapping("/listUserAuthShop")
    public ApiResult<List<ShopSysUserAuthDTO.ViewShopDTO>> listUserAuthShop() {
        return success(aliexpressDeliveryService.listUserAuthShop());
    }
}
