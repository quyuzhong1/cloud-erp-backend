package com.erp.server.wms.controller.feign;

import com.erp.model.wms.dto.PurchaseStockInDTO;
import com.erp.model.wms.entity.PurchaseStockInDetailEntity;
import com.erp.server.wms.service.PurchaseStockInDetailService;
import com.erp.server.wms.service.PurchaseStockInService;
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
    private PurchaseStockInDetailService purchaseStockInDetailService;

    @Resource
    private PurchaseStockInService purchaseStockInService;

    /**
     * @description: 根据来源明细id查询
     * @author Will
     * @date: 2023/4/18 10:47
     * @param SourceDetailIds
     * @return List<PurchaseStockInDetailEntity>
     */
    @PostMapping("/listDetailBySourceDetailIds")
    public List<PurchaseStockInDetailEntity> listDetailBySourceDetailIds(@RequestBody List<String> SourceDetailIds) {
        return purchaseStockInDetailService.listDetailBySourceDetailIds(SourceDetailIds);
    }

    /**
     * @description: 批量新增入库单
     * @author Will
     * @date: 2023/4/18 10:48
     * @param resultList
     * @return Boolean
     */
    @PostMapping("/batchAddPurchaseStockIn")
    public Boolean batchAddPurchaseStockIn(@RequestBody @Validated List<PurchaseStockInDTO.AddDTO> resultList) {
        return purchaseStockInService.batchAddPurchaseStockIn(resultList);
    }
}
