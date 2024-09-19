package com.erp.server.scm.controller.feign;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.server.scm.service.PurchasePriceDetailService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 采购价目表Feign
 * @date 2024-09-06
 * @author tanmujin
 */
@RestController
@RequestMapping("/feign/purchasePriceDetail")
public class PurchasePriceDetailFeignController {

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

    /**
     * 查询含税单价
     * @param
     * @return 
     * @date: 2024-09-06
     * @author: tanmujin
     */
    @PostMapping("/getTaxPrice")
    public List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> getTaxPrice(@RequestBody @Validated PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO dto){
        return purchasePriceDetailService.getTaxPrice(dto);
    }
}
