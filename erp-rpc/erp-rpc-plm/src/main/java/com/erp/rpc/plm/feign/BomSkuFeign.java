package com.erp.rpc.plm.feign;

import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.BomDTO;
import com.erp.model.plm.dto.ProductBomInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

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

    /**
     * @description: 根据父级skuNos查询子集sku
     * @return List<BomChildrenSkuDTO>
     */
    @PostMapping("/checkExistAndListCombinationSku")
    List<BomChildrenSkuDTO> checkExistAndListCombinationSku(@RequestBody List<String> skuNos);

    /**
     * @description: 查询单品bom
     * @return Map<String, List<BomDTO.BomSku>>
     */
    @PostMapping("/getSingleBomInfo")
    Map<String, List<BomDTO.BomSku>> getSingleBomInfo(@RequestBody List<String> skuIds);



}
