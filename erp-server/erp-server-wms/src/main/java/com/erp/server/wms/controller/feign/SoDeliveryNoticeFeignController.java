package com.erp.server.wms.controller.feign;

import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.server.wms.service.SoDeliveryNoticeDetailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("feign/soDeliveryNotice")
public class SoDeliveryNoticeFeignController {
    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;
    /**
     * 根据来源明细id查询出库表
     * @Author Luo_WG
     * @Date 2023/5/15 15:03
     * @param sourceDetailId sourceDetailId
     * @return java.util.List<com.erp.model.oms.entity.SoOutstockDetailEntity>
     **/
    @PostMapping("/listDetailBySourceDetailId")
    public List<SoDeliveryNoticeDetailEntity> listDetailBySourceDetailIds(@RequestBody List<String> sourceDetailId) {
        return soDeliveryNoticeDetailService.listDetailBySourceDetailIds(sourceDetailId);
    }
}
