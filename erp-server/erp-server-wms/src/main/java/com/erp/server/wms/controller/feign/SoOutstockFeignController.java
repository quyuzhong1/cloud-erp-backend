package com.erp.server.wms.controller.feign;

import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.SoOutstockService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("feign/soOutstock")
public class SoOutstockFeignController {
    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private SoOutstockService soOutstockService;

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

    /**
     * 销售订单ids获取销售出库单的数据
     * @Author Luo_WG
     * @Date 2023/5/25 15:42
     * @param soIds
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockDetailEntity>
     **/
    @PostMapping("/listDetailBySoIds")
    public List<SoOutstockDetailEntity> listDetailBySoIds(@RequestBody List<String> soIds) {
        return soOutstockDetailService.listDetailBySoIds(soIds);
    }

    /**
     * 销售订单ids获取销售出库单主表信息
     * @Author Luo_WG
     * @Date 2023/5/25 15:42
     * @param soIds
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockEntity>
     **/
    @PostMapping("/listBySoIds")
    List<SoOutstockEntity> listBySoIds(@RequestBody List<String> soIds) {
        return soOutstockService.listBySoIds(soIds);
    }
}
