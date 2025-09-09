package com.erp.server.plm.controller.api;

import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.server.plm.service.ProductPurchaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zdy
 * @ClassName ProductPurchaseController
 * @description: 产品采购信息表
 * @date 2025年09月08日
 * @version: 1.0
 */
@Slf4j
@RestController
@LogSystemModule("产品采购信息表")
@RequestMapping("/productPurchase")
public class ProductPurchaseController extends BaseController {

    @Resource
    private ProductPurchaseService productPurchaseService;

    /**
     * 根据ean或skuNo查询sku信息
     * @param dto
     * @return
     */
    @PostMapping("/listSkuInfoByEanOrSkuNo")
    public ApiResult<List<ProductDetailDTO.SkuDTO>> listSkuInfoByEanOrSkuNo(@RequestBody ProductDetailDTO.SearchDTO dto) {
        List<ProductDetailDTO.SkuDTO> list = productPurchaseService.listSkuInfoByEanOrSkuNo(dto);
        return success(list);
    }

}
