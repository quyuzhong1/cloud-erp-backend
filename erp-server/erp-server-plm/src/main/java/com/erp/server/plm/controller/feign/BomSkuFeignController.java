package com.erp.server.plm.controller.feign;

import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.server.plm.service.BomSkuService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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

    /**
     * @description: 根据父级skuIds查询子集sku
     * @author Will
     * @date: 2023/5/17 10:11
     * @param skuIds
     * @return List<BomChildrenSkuDTO>
     */
    @PostMapping("/listBomChildBySkuIds")
    public List<BomChildrenSkuDTO> listBomChildBySkuIds(@RequestBody List<String> skuIds) {
        return bomSkuService.listBomChildBySkuIds(skuIds);
    }
}
