package com.erp.server.wms.controller.feign;

import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.server.wms.service.PoInstockDetailService;
import com.erp.server.wms.service.PoInstockService;
import org.springframework.validation.annotation.Validated;
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
 * @date 2023/3/17 16:01
 */
@RestController
@RequestMapping("feign/purchaseStockIn")
public class PurchaseStockInFeignController {

    @Resource
    private PoInstockDetailService poInstockDetailService;

    @Resource
    private PoInstockService poInstockService;

    /**
     * @description: 根据来源明细id查询
     * @author Will
     * @date: 2023/4/18 10:47
     * @param sourceDetailIds
     * @return List<PurchaseStockInDetailEntity>
     */
    @PostMapping("/listDetailBySourceDetailIds")
    public List<PoInstockDetailEntity> listDetailBySourceDetailIds(@RequestBody List<String> sourceDetailIds) {
        return poInstockDetailService.listDetailBySourceDetailIds(sourceDetailIds);
    }

    /**
     * @description: 根据podIds查询
     * @author Will
     * @date: 2023/4/18 10:47
     * @param podIds
     * @return List<PurchaseStockInDetailEntity>
     */
    @PostMapping("/listDetailByPodIds")
    public List<PoInstockDetailEntity> listDetailByPodIds(@RequestBody List<String> podIds) {
        return poInstockDetailService.listDetailByPodIds(podIds);
    }


    /**
     * @description: 批量新增入库单
     * @author Will
     * @date: 2023/4/18 10:48
     * @param resultList
     * @return Boolean
     */
    @PostMapping("/batchAddPurchaseStockIn")
    public Boolean batchAddPurchaseStockIn(@RequestBody @Validated List<PoInstockDTO.AddDTO> resultList) {
        return poInstockService.batchAddPurchaseStockIn(resultList);
    }

    /**
     * 获取入库数量
     * @Author Luo_WG
     * @Date 2023/4/19 10:52
     * @param ids ids
     * @return java.util.List<com.erp.model.wms.dto.PurchaseStockInDTO.GetStockInQty>
     **/
    @PostMapping("/getStockInQty")
    public List<PoInstockDTO.GetStockInQty> getStockInQty(@RequestBody  List<String> ids) {
        return poInstockService.getStockInQty(ids);
    }

}
