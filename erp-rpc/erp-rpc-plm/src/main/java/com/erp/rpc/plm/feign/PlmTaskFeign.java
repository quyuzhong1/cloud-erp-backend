package com.erp.rpc.plm.feign;

import com.erp.model.plm.dto.BasicCategoryDTO;
import com.erp.model.plm.dto.CleanSkuDto;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.dto.ProductInfoDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.ProductVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.workflow.dto.WorkOptionDTO;
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
    @PostMapping("product/detail/productDetailProcessPass")
    void productDetailProcessPass(@RequestParam(value = "processId") String processId);

    /**
     * 获取用户权限
     */
    @PostMapping("feign/product/getProductIdBySku")
    CleanSkuDto getProductIdBySku(@RequestBody String sku);

    /**
     * 根据品类参数查询品类，参数：id、name
     */
    @PostMapping("feign/product/getCategoryByParam")
    BasicCategoryDTO getCategoryByParam(@RequestBody Map<String, String> params);

    /**
     * 根据sku的参数查询sku，参数：id、skuNo
     */
    @PostMapping("feign/product/getSkuByParam")
    ProductDetailDTO getSkuByParam(@RequestBody Map<String, String> params);

    /**
     * 根据spu的参数查询spu，参数：id、spuNo
     */
    @PostMapping("feign/product/getSpuByParam")
    ProductInfoDTO getSpuByParam(@RequestBody Map<String, String> params);

    /**
     * 更新业务单据状态
     */
    @PostMapping("feign/product/updateBusinessSyncKingdeeStatus")
    void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, String> params);

    /**
     * 根据skuid集合获取到sku 信息
     *
     * @param skuIds
     * @return
     * @author yl
     * @date 2023-03-21 12:19
     */
    @PostMapping("feign/product/getSkuInfoByIds")
    List<SkuVO> getSkuInfoByIds(@RequestBody List<String> skuIds);

    /**
     * @description: 获取已审核sku
     * @author Will
     * @date: 2023/3/21 14:12
     * @return List<SkuVO>
     */
    @GetMapping("feign/product/listApproveSku")
    List<SkuVO> listApproveSku();

    /**
     * 根据id查询sku信息
     * @Author Luo_WG
     * @Date 2023/4/14 15:10
     * @param ids ids
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     **/
    @PostMapping("feign/product/getByIdList")
    List<ProductDetailEntity> getByIdList(@RequestBody List<String> ids);

    /**
     * 根据sku id 集合获取到产品包装信息
     * @author yl
     * @date 2023-04-17 18:37
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.ProductVO.ProductPackVO>
     */
    @PostMapping("feign/product/getProductPackBySkuIds")
    List<ProductVO.ProductPackVO> getProductPackBySkuIds(@RequestBody List<String> skuIds);

    /**
     * 根据用户获取各任务阶段数量
     * @Author Luo_WG
     * @Date 2023/4/24 9:34
     * @param optionUserId optionUserId
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.StageViewDTO>
     **/
    @PostMapping("feign/product/stageView")
    List<WorkOptionDTO.StageViewDTO> stageView(@RequestBody String optionUserId);

    /**
     * 根据skuid 获取到产品角色的 人员
     * @author yl
     * @date 2023-04-28 12:21
     * @param skuIds
     * @return com.erp.model.plm.dto.ProductInfoDTO.ProductRolePeopleDTO
     */
    @PostMapping("feign/product/getRolePeople")
    List<ProductInfoDTO.ProductRolePeopleDTO> listProductRolePeople(@RequestBody List<String> skuIds);
}