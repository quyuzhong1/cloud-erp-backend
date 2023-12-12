package com.erp.server.wms.controller.pda;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.server.wms.service.SoDeliveryNoticeService;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * PDA:销售发货通知单
 * @author Luo_WG
 * @since 2023-04-07
 */
@RestController
@RequestMapping("/pdaSoDeliveryNotice")
public class PdaSoDeliveryNoticeController extends BaseController {
    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;

    /**
     * 根据sku编号查询发货通知单
     * @Author Luo_WG
     * @Date 2023/8/11 14:18
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.scm.dto.PurchaseOrderDTO.PdaPurchaseOrder>>
     **/
    @PostMapping("/pdaList")
    public ApiResult<List<SoDeliveryNoticeDTO.PdaSoDeliveryNotice>> pdaList(@RequestBody SoDeliveryNoticeDTO.PdaSoDeliveryNoticeParam dto) {
        List<SoDeliveryNoticeDTO.PdaSoDeliveryNotice> list = soDeliveryNoticeService.pdaList(dto);
        return success(list);
    }

    /**
     * 查询发货通知单详情
     * @Author Luo_WG
     * @Date 2023/8/22 19:00
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SoDeliveryNoticeDTO.View>
     **/
    @GetMapping("/view")
    public ApiResult<SoDeliveryNoticeDTO.View> view(@Param("id") String id) {
        SoDeliveryNoticeDTO.View view = soDeliveryNoticeService.pdaView(id);
        return success(view);
    }
}
