package com.erp.server.plm.controller.feign;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.erp.model.plm.dto.BasicCategoryDTO;
import com.erp.model.plm.dto.CleanSkuDto;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.dto.ProductInfoDTO;
import com.erp.model.plm.entity.ProductSaleEntity;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeService;
import com.erp.server.plm.service.BasicCategoryService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.ProductSaleService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;

/**
 * 查询sku
 * @Author Luo_WG
 * @Date 2022/12/14 15:09
 **/
@RestController
@RequestMapping("plm/feign/product")
public class ProductSkuFeignController {

    @Resource
    private ProductDetailService productDetailService;
    @Resource
    private ProductSaleService productSaleService;
    @Resource
    private BasicCategoryService basicCategoryService;
    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private SyncKingdeeService syncKingdeeService;

    /**
     * 根据sku查询sku表信息
     * @Author Luo_WG
     * @Date 2022/12/14 15:24
     * @param sku：sku
     * @return com.erp.model.plm.entity.ProductDetailEntity
     **/
    @PostMapping("/getProductIdBySku")
    public CleanSkuDto getProductIdBySku(@RequestBody String sku) {
        CleanSkuDto productIdBySkuClean = productDetailService.getProductIdBySkuClean(sku);
        Optional<ProductSaleEntity> productSaleEntity = productSaleService.lambdaQuery()
                .eq(ProductSaleEntity::getSkuId, sku)
                .oneOpt();
        if (productSaleEntity.isPresent()) {
            productIdBySkuClean.setListingTime(LocalDateTimeUtil.of(productSaleEntity.get().getListingTime()));
        }
        return productIdBySkuClean;
    }

    /**
     * @description: 查询品类
     * @author Will
     * @date: 2022/12/26 11:48
     * @param params
     * @return BasicCategoryDTO
     */
    @PostMapping("/getCategoryByParam")
    public BasicCategoryDTO getCategoryByParam(@RequestBody Map<String,String> params) {
        return basicCategoryService.getCategoryByParam(params);
    }

    /**
     * @description: 查询sku
     * @author Will
     * @date: 2022/12/26 11:49
     * @param params
     * @return ProductDetailDTO
     */
    @PostMapping("/getSkuByParam")
    public ProductDetailDTO getSkuByParam(@RequestBody Map<String,String> params) {
        return productDetailService.getSkuByParam(params);
    }

    /**
     * @description: 查询spu
     * @author Will
     * @date: 2022/12/26 11:49
     * @param params
     * @return ProductInfoDTO
     */
    @PostMapping("/getSpuByParam")
    public ProductInfoDTO getSpuByParam(@RequestBody Map<String,String> params) {
        return productInfoService.getSpuByParam(params);
    }

    /**
     * @description: 更新业务状态
     * @author Will
     * @date: 2023/3/10 15:46
     * @param params
     */
    @PostMapping("/updateBusinessSyncKingdeeStatus")
    public void updateBusinessSyncKingdeeStatus(@RequestBody Map<String,String> params) {
         syncKingdeeService.updateBusinessSyncKingdeeStatus(params);
    }
}
