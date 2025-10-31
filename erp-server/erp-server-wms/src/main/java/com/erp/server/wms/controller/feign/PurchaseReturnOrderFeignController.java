package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.entity.PoReturnEntity;
import com.erp.server.wms.service.PoReturnDetailService;
import com.erp.server.wms.service.PoReturnService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/14 11:47
 */
@RestController
@RequestMapping("feign/purchaseReturnOrder")
public class PurchaseReturnOrderFeignController extends BaseController {

    @Resource
    private PoReturnService poReturnService;

    @Resource
    private PoReturnDetailService poReturnDetailService;

    /**
     * @param SourceDetailIds
     * @return List<PurchaseReturnOrderDetailEntity>
     * @description: 根据采购订单明细ids查询退货明细
     * @author Will
     * @date: 2023/4/14 11:55
     */
    @PostMapping("/listDetailBySourceDetailIds")
    public List<PoReturnDetailEntity> listBySourceDetailIds(@RequestBody List<String> SourceDetailIds) {
        return poReturnDetailService.listBySourceDetailIds(SourceDetailIds);
    }

    @PostMapping("/listPurchaseReturnOrderDetailByMainIds")
    public List<PoReturnDetailEntity> listPurchaseReturnOrderDetailByMainIds(@RequestBody List<String> mainIds) {
        if(CollectionUtils.isEmpty(mainIds)){
            return new ArrayList<>();
        }
        return poReturnDetailService.listByMainIds(mainIds);
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
    public List<PoReturnDetailEntity> listReturnOrderDetailByPodIds(@RequestBody List<String> podIds) {
        List<PoReturnDetailEntity> purchaseReturnOrderDetailEntities = poReturnDetailService.listReturnOrderDetailByPodIds(podIds);
        return purchaseReturnOrderDetailEntities;
    }

    /**
     * 根据供应商id集合、单据日期等条件 查询退货数量信息
     * @param params
     * @return
     */
    @PostMapping("/getReturnInfo")
    public List<PurchaseReturnOrderDTO.SupplierReturnDTO> getReturnInfo(@RequestBody PurchaseReturnOrderDTO.SupplierReturnParamDTO params) {
        return poReturnService.getReturnInfo(params);
    }

    /**
     * 根据id集合查询采购退货单
     * @author Will
     * @date: 2023/9/5 14:26
     * @param poReturnIdList
     * @return List<PurchaseReturnOrderEntity>
     */
    @PostMapping("/listPoReturnByIdList")
    public List<PoReturnEntity> listPoReturnByIdList(@RequestBody List<String> poReturnIdList) {
        if (CollectionUtils.isEmpty(poReturnIdList)) {
            return Collections.emptyList();
        }
        List<PoReturnEntity> list = poReturnService.listByIds(poReturnIdList);
        return list;

    }


}
