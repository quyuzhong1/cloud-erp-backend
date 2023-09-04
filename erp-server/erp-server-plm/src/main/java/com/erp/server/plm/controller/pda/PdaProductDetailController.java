package com.erp.server.plm.controller.pda;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.PdaProductDetailDTO;
import com.erp.model.plm.dto.ProductManyDetailDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 产品管理
 * @Author Luo_WG
 * @Date 2023/9/4 18:18
 **/
@RestController
@RequestMapping("/pdaProductDetail")
public class PdaProductDetailController extends BaseController {

    /**
     * 产品信息-多规格-产品详情-PLM-1.3
     *
     * @param productId 产品信息表id
     * @return com.common.core.vo.ApiResult<com.erp.model.plm.dto.ProductManyDetailDTO>
     * @Author Luo_WG
     * @Date 2022/10/9 10:22
     **/

    @GetMapping("/view")
    //@RequestPermissions("plm:product:detail:getManySpecDetailById")
    public ApiResult<PdaProductDetailDTO> view(@RequestParam(value = "productId") String productId) {
//        ProductManyDetailDTO list = productDetailService.getManySpecDetailById(productId);
        return null;
    }
}
