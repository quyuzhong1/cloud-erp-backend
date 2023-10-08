package com.erp.server.plm.controller.pda;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.PdaProductDetailDTO;
import com.erp.model.plm.dto.ProductManyDetailDTO;
import com.erp.server.plm.service.ProductDetailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * PDA:产品管理
 * @Author Luo_WG
 * @Date 2023/9/4 18:18
 **/
@RestController
@RequestMapping("/pdaProductDetail")
public class PdaProductDetailController extends BaseController {
    @Resource
    private ProductDetailService productDetailService;

    /**
     * 产品查询
     * @Author Luo_WG
     * @Date 2023/9/4 18:46
     * @param skuNo
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.plm.dto.PdaProductDetailDTO>
     **/
    @GetMapping("/productView")
    //@RequestPermissions("plm:product:detail:getManySpecDetailById")
    public ApiResult<PdaProductDetailDTO.View> productView(@RequestParam(value = "skuNo") String skuNo) {
        PdaProductDetailDTO.View view = productDetailService.pdaProductView(skuNo);
        return success(view);
    }
}
