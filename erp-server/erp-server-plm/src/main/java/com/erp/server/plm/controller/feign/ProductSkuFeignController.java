package com.erp.server.plm.controller.feign;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.erp.model.plm.dto.BasicCategoryDTO;
import com.erp.model.plm.dto.CleanSkuDto;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.dto.ProductInfoDTO;
import com.erp.model.plm.entity.ProductSaleEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeService;
import com.erp.server.plm.service.BasicCategoryService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.ProductSaleService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 查询sku
 *
 * @Author Luo_WG
 * @Date 2022/12/14 15:09
 **/
@RestController
@RequestMapping("feign/product")
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
     *
     * @param sku：sku
     * @return com.erp.model.plm.entity.ProductDetailEntity
     * @Author Luo_WG
     * @Date 2022/12/14 15:24
     **/
    @PostMapping("/getProductIdBySku")
    public CleanSkuDto getProductIdBySku(@RequestBody String sku) {
        CleanSkuDto productIdBySkuClean = productDetailService.getProductIdBySkuClean(sku);
        Optional<ProductSaleEntity> productSaleEntity = productSaleService.lambdaQuery()
                .eq(ProductSaleEntity::getSkuId, sku)
                .oneOpt();
        if (productSaleEntity.isPresent()) {
            productIdBySkuClean.setListingTime(LocalDateTimeUtil.of(productSaleEntity.get().getListingTime()).toLocalDate());
        }
        return productIdBySkuClean;
    }

    /**
     * @param params
     * @return BasicCategoryDTO
     * @description: 查询品类
     * @author Will
     * @date: 2022/12/26 11:48
     */
    @PostMapping("/getCategoryByParam")
    public BasicCategoryDTO getCategoryByParam(@RequestBody Map<String, String> params) {
        return basicCategoryService.getCategoryByParam(params);
    }

    /**
     * @param params
     * @return ProductDetailDTO
     * @description: 查询sku
     * @author Will
     * @date: 2022/12/26 11:49
     */
    @PostMapping("/getSkuByParam")
    public ProductDetailDTO getSkuByParam(@RequestBody Map<String, String> params) {
        return productDetailService.getSkuByParam(params);
    }

    /**
     * @param params
     * @return ProductInfoDTO
     * @description: 查询spu
     * @author Will
     * @date: 2022/12/26 11:49
     */
    @PostMapping("/getSpuByParam")
    public ProductInfoDTO getSpuByParam(@RequestBody Map<String, String> params) {
        return productInfoService.getSpuByParam(params);
    }

    /**
     * @param params
     * @description: 更新业务状态
     * @author Will
     * @date: 2023/3/10 15:46
     */
    @PostMapping("/updateBusinessSyncKingdeeStatus")
    public void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, String> params) {
        syncKingdeeService.updateBusinessSyncKingdeeStatus(params);
    }


    /**
     * 根据skuid 集合获取到sku 信息
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author yl
     * @date 2023-03-21 12:06
     */
    @PostMapping("/getSkuInfoByIds")
    public List<SkuVO> getSkuInfoBySkuIds(@RequestBody List<String> skuIds) {
        List<SkuVO> skuList = productDetailService.getSkuInfoBySkuIds(skuIds);
        return skuList;
    }
    /**
     * @description: 获取已审核sku
     * @author Will
     * @date: 2023/3/21 14:13
     * @return List<SkuVO>
     */
    @GetMapping("/listApproveSku")
    public List<SkuVO> listApproveSku() {
        List<SkuVO> skuList = productDetailService.searchSku(null);
        return skuList;
    }

}
