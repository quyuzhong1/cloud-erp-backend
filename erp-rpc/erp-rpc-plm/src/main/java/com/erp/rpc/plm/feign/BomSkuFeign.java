package com.erp.rpc.plm.feign;

import com.erp.model.plm.dto.BomChildrenSkuDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * BomSku Feign
 * @date 2024-08-31
 * @author tanmujin
 */
@FeignClient(name = "erp-plm", path = "/feign/bom", contextId = "bomSkuFeign")
public interface BomSkuFeign {

    /**
     * @description: 根据父级skuIds查询子集sku
     * @param skuIds
     * @return List<BomChildrenSkuDTO>
     */
    @PostMapping("/listBomChildBySkuIds")
    List<BomChildrenSkuDTO> listBomChildBySkuIds(@RequestBody List<String> skuIds);
}
