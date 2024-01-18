package com.erp.server.wms.controller.api;


import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.rpc.srm.feign.SrmDeliveryOrderFeign;
import com.erp.rpc.wms.feign.SupplierFeign;
import com.erp.server.wms.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 供应商送货单
 * @author lrp
 * @since 2024-01-12
 */
@Slf4j
@RestController
@LogSystemModule("供应商送货单")
@RequestMapping("/supplierDeliveryOrder")
public class SupplierDeliveryOrderController extends BaseController {

    @Resource
    private SrmDeliveryOrderFeign srmDeliveryFeign;

    @Resource
    private SupplierFeign supplierFeign;

    @Resource
    private CommonService commonService;

    /**
     * 获取 tab列表
     * @return
     */
    @GetMapping("/tabList")
    public ApiResult<List<DeliveryOrderDTO.TabListDTO>> tabList() {
        DeliveryOrderDTO.ParamDTO paramDTO = new DeliveryOrderDTO.ParamDTO();
        paramDTO.setSupplierIdList(supplierFeign.listByPurchaseUserId(commonService.getUserInfo().getUid()).stream().map(BaseEntity::getId).collect(Collectors.toList()));
        List<DeliveryOrderDTO.TabListDTO> tabList = srmDeliveryFeign.tabList(paramDTO);
        return success(tabList);
    }

    /**
     * 详情
     *
     * @param
     * @return
     */
    @LogViewService
    @GetMapping("/view")
    public ApiResult<DeliveryOrderDTO.ViewDTO> view(@Param("id") String id) {
        DeliveryOrderDTO.ViewDTO view = srmDeliveryFeign.view(id);
        return success(view);
    }

    /**
     * 分页
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<DeliveryOrderDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DeliveryOrderDTO.ParamDTO> dto) {
        dto.getParams().setSupplierIdList(supplierFeign.listByPurchaseUserId(commonService.getUserInfo().getUid()).stream().map(BaseEntity::getId).collect(Collectors.toList()));
        return success(srmDeliveryFeign.paging(dto));
    }


    /**
     * 打印送货单
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/print")
    public ApiResult<List<DeliveryOrderDTO.PrintDTO>> print(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(srmDeliveryFeign.print(dto));
    }

}
