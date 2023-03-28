package com.erp.rpc.plm.feign;

import com.erp.model.plm.dto.BasicCategoryDTO;
import com.erp.model.plm.dto.CleanSkuDto;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.dto.ProductInfoDTO;
import com.erp.model.plm.vo.SkuVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * plm 远程调用接口
 *
 * @Classname PlmTaskFeign
 * @Description TODO
 * @Date 2022-10-21 9:06
 * @Created by yl
 */
@FeignClient(name="erp-plm")
public interface PlmTaskFeign {

    /**
     * 获取用户权限
     */
    @PostMapping("plm/product/detail/productDetailProcessPass")
    void productDetailProcessPass(@RequestParam(value = "processId") String processId);

    /**
     * 获取用户权限
     */
    @PostMapping("plm/feign/product/getProductIdBySku")
    CleanSkuDto getProductIdBySku(@RequestBody String sku);

    /**
     * 根据品类参数查询品类，参数：id、name
     */
    @PostMapping("plm/feign/product/getCategoryByParam")
    BasicCategoryDTO getCategoryByParam(@RequestBody Map<String, String> params);

    /**
     * 根据sku的参数查询sku，参数：id、skuNo
     */
    @PostMapping("plm/feign/product/getSkuByParam")
    ProductDetailDTO getSkuByParam(@RequestBody Map<String, String> params);

    /**
     * 根据spu的参数查询spu，参数：id、spuNo
     */
    @PostMapping("plm/feign/product/getSpuByParam")
    ProductInfoDTO getSpuByParam(@RequestBody Map<String, String> params);

    /**
     * 更新业务单据状态
     */
    @PostMapping("plm/feign/product/updateBusinessSyncKingdeeStatus")
    void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, String> params);

    /**
     * 根据skuid集合获取到sku 信息
     *
     * @param skuIds
     * @return
     * @author yl
     * @date 2023-03-21 12:19
     */
    @PostMapping("plm/feign/product/getSkuInfoByIds")
    List<SkuVO> getSkuInfoByIds(@RequestBody List<String> skuIds);

    /**
     * @description: 获取已审核sku
     * @author Will
     * @date: 2023/3/21 14:12
     * @return List<SkuVO>
     */
    @GetMapping("plm/feign/product/listApproveSku")
    List<SkuVO> listApproveSku();
}