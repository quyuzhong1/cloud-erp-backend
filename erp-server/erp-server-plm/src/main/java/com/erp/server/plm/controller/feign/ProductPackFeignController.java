package com.erp.server.plm.controller.feign;

import com.erp.model.plm.dto.ProductPackDTO;
import com.erp.server.plm.service.ProductPackService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Will
 * @version 1.0
 * @description: 产品包装信息
 * @date 2023/9/20 11:36
 */
@RestController
@RequestMapping("feign/productPack")
public class ProductPackFeignController {

    @Resource
    private ProductPackService productPackService;

    /**
     * 回填产品包装信息
     * @author Will
     * @date: 2023/9/20 11:39
     * @param productPackDTO
     */
    @PostMapping("/backFillPackaging")
    public void backFillPackaging(@RequestBody ProductPackDTO productPackDTO) {
        productPackService.backFillPackaging(productPackDTO);
    }
}
