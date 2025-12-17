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
     * 定位扫描字段类型
     * @param dto
     * @return
     */
    @PostMapping("/scanFieldType")
    public ApiResult<ProductDetailDTO.SkuSearchDTO> scanFieldType(@RequestBody ProductDetailDTO.SearchSkuDTO dto) {
        ProductDetailDTO.SkuSearchDTO skuSearchDTO = productPurchaseService.scanFieldType(dto);
        return success(skuSearchDTO);
    }


}
