package com.erp.server.srm.controller.feign;


import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.server.srm.service.DeliveryOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 送货单
 * @author lrp
 * @since 2024-01-12
 */
@Slf4j
@RestController
@LogSystemModule("送货单")
@RequestMapping("/feign/deliveryOrder")
public class DeliveryOrderFeginController extends BaseController {

    @Resource
    private DeliveryOrderService deliveryOrderService;

    /**
     * 获取 tab列表
     * @return
     */
    @PostMapping("/tabList")
    public List<DeliveryOrderDTO.TabListDTO> tabList(@RequestBody DeliveryOrderDTO.ParamDTO paramDTO) {
        return deliveryOrderService.tabList(paramDTO.getSupplierIdList());
    }

    /**
     * 详情
     *
     * @param
     * @return
     */
    @GetMapping("/view")
    public DeliveryOrderDTO.ViewDTO view(@RequestParam("id") String id) {
        return deliveryOrderService.view(id);
    }

    /**
     * 分页
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     */
    @PostMapping("/paging")
    public PagingVO<DeliveryOrderDTO.ListDTO> paging(@RequestBody @Validated PagingDTO<DeliveryOrderDTO.ParamDTO> dto) {
        return deliveryOrderService.paging(dto);
    }

    /**
     * 打印送货单
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/print")
    public List<DeliveryOrderDTO.PrintDTO> print(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return deliveryOrderService.print(dto.getIds());
    }
}
