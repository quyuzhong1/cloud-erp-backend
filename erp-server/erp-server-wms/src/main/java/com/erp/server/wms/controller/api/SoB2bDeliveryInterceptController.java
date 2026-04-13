package com.erp.server.wms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.SoB2bDeliveryInterceptDTO;
import com.erp.server.wms.query.SoB2bDeliveryInterceptQueryHandler;
import com.erp.server.wms.service.SoB2bDeliveryInterceptService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * B2B发货拦截单
 */
@RestController
@LogSystemModule("B2B发货拦截单")
@RequestMapping("/b2bDeliveryIntercept")
public class SoB2bDeliveryInterceptController extends BaseController {

    @Resource
    private SoB2bDeliveryInterceptService soB2bDeliveryInterceptService;

    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "sbdid.warehouse_id",
            menuCode = "wms:b2bDeliveryIntercept:paging",
            tableAlias = "sbdi")
    public ApiResult<List<SoB2bDeliveryInterceptDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(soB2bDeliveryInterceptService.tabList(dto));
    }

    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "sbdid.warehouse_id",
            menuCode = "wms:b2bDeliveryIntercept:paging",
            tableAlias = "sbdi")
    @WebAdvanceQuery(handler = SoB2bDeliveryInterceptQueryHandler.class)
    public ApiResult<PagingVO<SoB2bDeliveryInterceptDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SoB2bDeliveryInterceptDTO.PagingParamDTO> dto) {
        return success(soB2bDeliveryInterceptService.paging(dto));
    }

    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:b2bDeliveryIntercept:view",
            serviceClass = SoB2bDeliveryInterceptService.class,
            keyIdName = "id")
    public ApiResult<SoB2bDeliveryInterceptDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(soB2bDeliveryInterceptService.view(id));
    }
}
