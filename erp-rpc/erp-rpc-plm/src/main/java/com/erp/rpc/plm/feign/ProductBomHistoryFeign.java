package com.erp.rpc.plm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.ProductBomHistoryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * BOM 历史 Feign
 *
 * @author jack
 * @since 2026-06-23
 */
@FeignClient(name = "erp-plm", path = "/feign/productBomHistory", contextId = "productBomHistoryFeign", configuration = {FeignErrorDecoder.class})
public interface ProductBomHistoryFeign {

    /**
     * 根据 BOM 历史 id 查询历史父子件。
     */
    @PostMapping("/listBomHistoryByIds")
    List<BomChildrenSkuDTO> listBomHistoryByIds(@RequestBody ProductBomHistoryDTO.BomHistoryQueryDTO dto);

    /**
     * 根据组合品父 SKU 查询全部历史父子件。
     */
    @PostMapping("/listBomHistoryByParentSkuIds")
    List<BomChildrenSkuDTO> listBomHistoryByParentSkuIds(@RequestBody ProductBomHistoryDTO.BomHistoryQueryDTO dto);
}
