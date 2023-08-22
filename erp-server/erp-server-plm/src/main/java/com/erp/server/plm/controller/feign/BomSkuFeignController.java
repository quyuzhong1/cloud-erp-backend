package com.erp.server.plm.controller.feign;

import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.server.plm.service.BomSkuService;
import com.erp.server.plm.service.ProductBomSkuHistoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: bom远程调用
 * @date 2023/5/17 9:36
 */
@RestController
@RequestMapping("feign/bom")
public class BomSkuFeignController {

    @Resource
    private BomSkuService bomSkuService;

    @Resource
    private ProductBomSkuHistoryService productBomSkuHistoryService;


    /**
     * @description: 根据父级skuIds查询子集sku
     * @author Will
     * @date: 2023/5/17 10:11
     * @param skuIds
     * @return List<BomChildrenSkuDTO>
     */
    @PostMapping("/listBomChildBySkuIds")
    public List<BomChildrenSkuDTO> listBomChildBySkuIds(@RequestBody List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return new ArrayList<>();
        }
        return bomSkuService.listBomChildBySkuIds(skuIds);
    }

    /**
     * @description: 根据父级skuIds查询历史bom子集sku
     * @author Will
     * @date: 2023/8/21 10:38
     * @param skuIds
     * @return List<BomChildrenSkuDTO>
     */
    @PostMapping("/listHistoryBomChildBySkuIds")
    public List<BomChildrenSkuDTO> listHistoryBomChildBySkuIds(@RequestBody List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return new ArrayList<>();
        }
        return productBomSkuHistoryService.listHistoryBomChildBySkuIds(skuIds);
    }
    
    /**
     * @description: 根据父级skuIds查询BOM
     * @author Will
     * @date: 2023/5/31 10:59
     * @param skuIds 
     * @return List<BomInfoEntity>
     */
    @PostMapping("/listBomByParentSkuIds")
    public List<BomInfoEntity> listBomByParentSkuIds(@RequestBody List<String> skuIds) {
        return bomSkuService.listBomByParentSkuIds(skuIds);
    }

    /**
     * @description: 根据父级skuNos查询BOM
     * @author Will
     * @date: 2023/5/31 10:59
     * @param skuNos
     * @return List<BomInfoEntity>
     */
    @PostMapping("/listBomByParentSkuNos")
    public List<BomInfoEntity> listBomByParentSkuNos(@RequestBody List<String> skuNos) {
        return bomSkuService.listBomByParentSkuNos(skuNos);
    }

}
