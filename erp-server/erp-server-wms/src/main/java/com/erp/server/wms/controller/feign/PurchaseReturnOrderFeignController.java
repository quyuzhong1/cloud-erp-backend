package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.entity.PurchaseReturnOrderDetailEntity;
import com.erp.server.wms.service.PurchaseReturnOrderDetailService;
import com.erp.server.wms.service.PurchaseReturnOrderService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/14 11:47
 */
@RestController
@RequestMapping("feign/purchaseReturnOrder")
public class PurchaseReturnOrderFeignController extends BaseController {

    @Resource
    private PurchaseReturnOrderService purchaseReturnOrderService;

    @Resource
    private PurchaseReturnOrderDetailService purchaseReturnOrderDetailService;

    /**
     * @param SourceDetailIds
     * @return List<PurchaseReturnOrderDetailEntity>
     * @description: 根据采购订单明细ids查询退货明细
     * @author Will
     * @date: 2023/4/14 11:55
     */
    @PostMapping("/listDetailBySourceDetailIds")
    public List<PurchaseReturnOrderDetailEntity> listBySourceDetailIds(@RequestBody List<String> SourceDetailIds) {
        return purchaseReturnOrderDetailService.listBySourceDetailIds(SourceDetailIds);
    }

    /**
     * 根据采购单详情表id查询收货单详情
     *
     * @param podIds podIds
     * @return java.lang.String
     * @Author Luo_WG
     * @Date 2023/4/18 16:41
     **/
    @PostMapping("/listReturnOrderDetailByPodIds")
    public List<PurchaseReturnOrderDetailEntity> listReturnOrderDetailByPodIds(@RequestBody List<String> podIds) {
        List<PurchaseReturnOrderDetailEntity> purchaseReturnOrderDetailEntities = purchaseReturnOrderDetailService.listReturnOrderDetailByPodIds(podIds);
        return purchaseReturnOrderDetailEntities;
    }

    @PostMapping("/addReturnOrder")
    public Boolean addReturnOrder(@RequestBody List<PurchaseReturnOrderDTO.AddDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }
        Boolean flag = purchaseReturnOrderService.batchAdd(list);
        return flag ;

    }

}
