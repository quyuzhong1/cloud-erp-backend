package com.erp.server.oms.controller.feign;


import com.common.business.dto.base.*;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.ExhibitionOrderDTO;
import com.erp.server.oms.service.ExhibitionOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

/**
 * 展会订单信息
 *
 * @author jack
 * @since 2025-08-29
 */
@Slf4j
@RestController
@LogSystemModule("展会订单信息")
@RequestMapping("/feign/exhibitionOrder")
public class ExhibitionOrderFeignController extends BaseController {

    @Resource
    private ExhibitionOrderService exhibitionOrderService;

    /**
    * 新增
    * @author jack
    * @date:  2025-08-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/listFreezeQtyBySku")
    public List<ExhibitionOrderDTO.FreezeQtyBySku> listFreezeQtyBySku(@RequestBody ExhibitionOrderDTO.SearchDTO dto) {
        return exhibitionOrderService.listFreezeQtyBySku(dto);
    }
}
