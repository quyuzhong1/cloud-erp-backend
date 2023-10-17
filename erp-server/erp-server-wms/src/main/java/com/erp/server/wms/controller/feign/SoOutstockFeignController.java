package com.erp.server.wms.controller.feign;

import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.SoOutstockService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
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
     *
     * @param sourceDetailId sourceDetailId
     * @return java.util.List<com.erp.model.oms.entity.SoOutstockDetailEntity>
     * @Author Luo_WG
     * @Date 2023/5/15 15:03
     **/
    @PostMapping("/listDetailBySourceDetailId")
    public List<SoOutstockDetailEntity> listDetailBySourceDetailId(@RequestBody List<String> sourceDetailId) {
        return soOutstockDetailService.listDetailBySourceDetailId(sourceDetailId);
    }

    /**
     * 获取销售出货单
     *
     * @param id
     * @return
     */
    @PostMapping("/getSoOutstockEntityById")
    public SoOutstockEntity getSoOutstockEntityById(@RequestParam(value = "id") String id) {
        return soOutstockService.getById(id);
    }

    /**
     * 获取销售出货单 明细
     *
     * @param id
     * @return
     */
    @PostMapping("/getSoOutstockDetailByDetailId")
    public List<SoOutstockDetailEntity> getSoOutstockDetailByDetailId(@RequestParam(value = "id") String id) {
        return soOutstockDetailService.listByMainIds(Collections.singletonList(id));
    }

    /**
     * 销售订单ids获取销售出库单的数据
     *
     * @param soIds
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockDetailEntity>
     * @Author Luo_WG
     * @Date 2023/5/25 15:42
     **/
    @PostMapping("/listDetailBySoIds")
    public List<SoOutstockDetailEntity> listDetailBySoIds(@RequestBody List<String> soIds) {
        return soOutstockDetailService.listDetailBySoIds(soIds);
    }

    /**
     * 销售订单ids获取销售出库单主表信息
     *
     * @param soIds
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockEntity>
     * @Author Luo_WG
     * @Date 2023/5/25 15:42
     **/
    @PostMapping("/listBySoIds")
    List<SoOutstockEntity> listBySoIds(@RequestBody List<String> soIds) {
        return soOutstockService.listBySoIds(soIds);
    }


    /**
     * 根据销售订单详情ids 获取对应的出库详情
     *
     * @param soDetailIds
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockDetailEntity>
     * @author yl
     * @date 2023-05-29 17:32
     */
    @PostMapping("/listDetailBySoDetailIds")
    List<SoOutstockDetailDTO.DeliveryQtyDTO> listDetailBySoDetailIds(@RequestBody List<String> soDetailIds) {
        return soOutstockDetailService.listDetailBySoDetailIds(soDetailIds);
    }
}
