package com.erp.server.wms.controller.feign;

import com.erp.model.oms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoReturnNoticeEntity;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.SoReturnNoticeDetailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("feign/soOutstock")
public class SoOutstockFeignController {
    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    /**
     * 根据来源明细id查询出库表
     * @Author Luo_WG
     * @Date 2023/5/15 15:03
     * @param sourceDetailId sourceDetailId
     * @return java.util.List<com.erp.model.oms.entity.SoOutstockDetailEntity>
     **/
    @PostMapping("/listDetailBySourceDetailId")
    public List<SoOutstockDetailEntity> listDetailBySourceDetailId(@RequestBody List<String> sourceDetailId) {
        return soOutstockDetailService.listDetailBySourceDetailId(sourceDetailId);
    }
}
