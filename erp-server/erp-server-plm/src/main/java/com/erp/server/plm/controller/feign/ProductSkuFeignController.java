package com.erp.server.plm.controller.feign;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.BaseIdsDTO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.vo.ProductRefLabelVO;
import com.erp.model.plm.vo.ProductVO;
import com.erp.model.plm.vo.SkuInfoSimpleVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeService;
import com.erp.server.plm.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Resource
    private ProductRefLabelService productRefLabelService;
    @Resource
    private ProductCustomsService productCustomsService;

    @Resource
    private ProductLogisticsService productLogisticsService;

    /**
     * 产品包装信息
     */
    @Resource
    private ProductPackService productPackService;

    @Resource
    private WorkOptionService workOptionService;

    @Autowired
    private ProductPurchaseService productPurchaseService;

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
     * 通过子类id或名称获取到父级的分类
     *
     * @author Jim
     */
    @PostMapping("/parentCategory")
    public BasicCategoryDTO getParentCategoryByParam(@RequestBody Map<String, String> params) {
        return basicCategoryService.getParentCategoryByParam(params);
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
    public void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, Object> params) {
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
    @PostMapping("/listSkuPurchaseBySkuIds")
    public List<SkuVO> listSkuPurchaseBySkuIds(@RequestBody List<String> skuIds) {
        List<SkuVO> skuList = productDetailService.listSkuPurchaseBySkuIds(skuIds);
        return skuList;
    }

    /**
     * 根据sku no 获取sku 信息
     *
     * @param skuNoList
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author yl
     * @date 2023-06-27 17:51
     */
    @PostMapping("/listBySkuNos")
    public List<SkuVO> listBySkuNos(@RequestBody List<String> skuNoList) {
        List<SkuVO> skuList = productDetailService.getSkuBySkuNos(skuNoList);
        return skuList;
    }

    /**
     * 根据sku no 获取sku 信息
     *
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author yl
     * @date 2023-06-27 17:51
     */
    @PostMapping("/getSkuInfoAdvanceQuery")
    @WebAdvanceQuery
    public List<SkuVO> getSkuInfoAdvanceQuery(@RequestBody AdvanceQueryContainer advanceQueryContainer) {
        List<SkuVO> skuList = productDetailService.getSkuInfoAdvanceQuery(advanceQueryContainer);
        return skuList;
    }



    @PostMapping("/listBySkuNoList")
    public List<ProductDetailEntity> listBySkuNoList(@RequestBody List<String> skuNoList) {
        List<ProductDetailEntity> skuList = productDetailService.listBySkuNoList(skuNoList);
        return skuList;
    }

    /**
     * @return List<SkuVO>
     * @description: 获取已审核sku
     * @author Will
     * @date: 2023/3/21 14:13
     */
    @GetMapping("/listApproveSku")
    public List<SkuVO> listApproveSku() {
        List<SkuVO> skuList = productDetailService.searchSku(null);
        return skuList;
    }

    /**
     * 根据id查询sku信息
     *
     * @param ids ids
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @Author Luo_WG
     * @Date 2023/4/14 15:07
     **/
    @PostMapping("/getByIdList")
    public List<ProductDetailEntity> getByIdList(@RequestBody List<String> ids) {
        List<ProductDetailEntity> byIdList = productDetailService.getByIdList(ids);
        return byIdList;
    }


    /**
     * 根据sku id 集合获取对应产品信息
     *
     * @param skuIds
     * @return
     */

    @PostMapping("/getProductPackBySkuIds")
    public List<ProductVO.ProductPackVO> getProductPackBySkuIds(@RequestBody List<String> skuIds) {
        List<ProductVO.ProductPackVO> list = productPackService.getBySkuIds(skuIds);
        return list;
    }

    /**
     * 根据用户获取各任务阶段数量
     *
     * @param optionUserId optionUserId
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.StageViewDTO>
     * @Author Luo_WG
     * @Date 2023/4/24 9:34
     **/
    @PostMapping("/stageView")
    public List<WorkOptionDTO.StageViewDTO> stageView(@RequestBody String optionUserId) {
        return workOptionService.stageView(optionUserId);
    }


    /**
     * 根据skuId 获取到产品的角色人员
     *
     * @param skuIds
     * @return com.erp.model.plm.dto.ProductInfoDTO.ProductRolePeopleDTO
     * @author yl
     * @date 2023-04-28 12:23
     */
    @PostMapping("/getRolePeople")
    public List<ProductInfoDTO.ProductRolePeopleDTO> getRolePeople(@RequestBody List<String> skuIds) {
        return productInfoService.getRolePeople(skuIds);
    }

    /**
     * 根据sku id集合获取采购员、供应商信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/getPurchaseInfoBySkuIds")
    public Map<String, SkuPurchaseDTO.PurchaseInfo> getPurchaseInfoBySkuIds(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<SkuPurchaseDTO.PurchaseInfo> dataList = productPurchaseService.getInfoBySkuIds(dto.getIds());
        return dataList.stream().collect(Collectors.toMap(SkuPurchaseDTO.PurchaseInfo::getSkuId, Function.identity()));
    }

    /**
     * 更新不可删除标识
     *
     * @param skuIds skuIds
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/6/15 11:32
     **/
    @PostMapping("/updateOccupyStatus")
    public Boolean updateOccupyStatus(@RequestBody List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Boolean.FALSE;
        }
        return productDetailService.updateOccupyStatus(skuIds);
    }


    /**
     * 获取不参与库存操作的sku
     *
     * @return
     */
    @PostMapping("/getNoInventorySku")
    public List<SkuVO> getNoInventorySku() {
        return productDetailService.getNoInventorySku();
    }

    /**
     * 根据创建时间获取到对应的sku
     *
     * @return
     */
    @PostMapping("/listByCreateTimeList")
    public List<ProductDetailEntity> listByCreateTimeList(@RequestBody List<LocalDateTime> createTimeList) {
        return productDetailService.listByCreateTimeList(createTimeList);
    }

    /**
     * 根据分类id 获取分类
     */
    @PostMapping("listCategoryByIds")
    public List<BasicCategoryEntity> listCategoryByIds(@RequestBody List<String> ids) {
        return CollectionUtils.isEmpty(ids) ? Collections.emptyList() : basicCategoryService.listByIds(ids);
    }


    /**
     * 获取到父级分类
     */
    @GetMapping("listParentCategory")
    public List<BasicCategoryEntity> listCategoryByIds() {
        return basicCategoryService.listParentCategory();
    }

    @PostMapping("listSkuSalesBySkuNos")
    public List<SkuDTO.SalesDTO> listSkuSalesBySkuNos(@RequestBody List<String> skuNoList) {
        return productSaleService.listSkuSalesBySkuNos(skuNoList);
    }

    @PostMapping("/getProductRelLabelBySkuId")
    public List<ProductRefLabelVO> getProductRelLabel(@RequestBody String skuId) {
        return productRefLabelService.getLabelList(null, null, skuId);
    }

    @PostMapping("/getProductRelLabelBySkuIds")
    public List<ProductRefLabelVO> getProductRelLabel(@RequestBody Set<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.emptyList();
        }
        return productRefLabelService.getLabelListByIds(null, null, skuIds);
    }


    /**
     * 批量修改产品信息
     *
     * @param list
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     **/
    @PostMapping("/updateProductDetailBatch")
    public Boolean updateProductDetailBatch(@RequestBody List<ProductDetailEntity> list) {
        return productDetailService.updateProductDetailBatch(list);
    }

    /**
     * 批量修改产品采购信息首批下单日期
     *
     * @param list
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     **/
    @PostMapping("/updateProductPlaceOrderTimeBatch")
    public Boolean updateProductPlaceOrderTimeBatch(@RequestBody List<ProductPurchaseEntity> list) {
        return productPurchaseService.updateProductPlaceOrderTimeBatch(list);
    }

    /**
     * 批量修改产品销售信息上市日期
     *
     * @param list
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     **/
    @PostMapping("/updateProductSaleListingTimeBatch")
    public Boolean updateProductSaleListingTimeBatch(@RequestBody List<ProductSaleEntity> list) {
        return productSaleService.updateProductSaleListingTimeBatch(list);
    }


    /**
     * 根据skuId查询产品采购信息
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.entity.ProductPurchaseEntity>
     **/
    @PostMapping("/listProductPurchaseBySkuId")
    public List<ProductPurchaseEntity> listProductPurchaseBySkuId(@RequestBody List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.emptyList();
        }
        return productPurchaseService.listBySkuIds(skuIds);
    }

    /**
     * 根据skuId查询产品销售信息
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.entity.ProductSaleEntity>
     **/
    @PostMapping("/listProductSaleBySkuId")
    public List<ProductSaleEntity> listProductSaleBySkuId(@RequestBody List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.emptyList();
        }
        return productSaleService.listBySkuIds(skuIds);
    }

    /**
     * @param skuIds
     * @return List<ProductDTO>
     * @description: 根据skuid查询产品信息
     * @author Will
     * @date: 2023/11/16 15:16
     */
    @PostMapping("/listProductBySkuIds")
    public List<ProductDetailDTO.ProductDTO> listProductBySkuIds(@RequestBody List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.emptyList();
        }
        return productInfoService.listProductBySkuIds(skuIds);
    }


    /**
     * @param skuIdList
     * @return List<ProductDTO>
     * @description:  根据skuId集合信息查询物流产品信息（组合品根据combinationDeclareType判断是否拆分）
     * @author Will
     * @date: 2023/11/16 15:16
     */
    @PostMapping("/listProductLogisticsByIds")
    public List<ProductDetailDTO.ProductLogisticDTO> listProductLogisticsByIds(@RequestBody List<String> skuIdList){
        return productLogisticsService.listProductLogisticsByIds(skuIdList);
    }

    /**
     * 获取sku定义的目的国申报海关编码
     * @param dto
     * @return
     */
    @PostMapping("/listProductCustomsBySkuIds")
    public List<ProductCustomsEntity> listProductCustomsBySkuIds(@RequestBody ProductCustomsSkuDTO dto){
        return productCustomsService.listProductCustomsBySkuIds(dto);
    }

    /**
     * 根据SkuIds获取SKU简单信息
     */
    @PostMapping("/getSimpleSkuInfoByIds")
    public List<SkuInfoSimpleVO> getSimpleSkuInfoByIds(@RequestBody List<String> skuIds){
        return productDetailService.getSimpleSkuInfoByIds(skuIds);
    }
    /**
     * 根据skuid 集合获取到sku采购信息（基础信息+产品采购信息+产品采购含税单价）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2023-03-21 12:06
     */
    @PostMapping("/listSkuCostByIds")
    public List<SkuVO> listSkuCostByIds(@RequestBody List<String> skuIds) {
        List<SkuVO> skuList = productDetailService.listSkuCostByIds(skuIds);
        return skuList;
    }

    /**
     * 根据skuid 集合获取到sku产品信息（基础信息+产品信息）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2023-03-21 12:06
     */
    @PostMapping("/listSkuProductByIds")
    public List<SkuVO> listSkuProductByIds(@RequestBody List<String> skuIds){
        List<SkuVO> skuList = productDetailService.listSkuProductByIds(skuIds);
        return skuList;
    }

    /**
     * 根据skuid 集合获取到sku包装信息 （基础信息+产品信息+包装信息）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2023-03-21 12:06
     */
    @PostMapping("/listSkuPackByIds")
    public List<SkuVO> listSkuPackByIds(@RequestBody List<String> skuIds){
        List<SkuVO> skuList = productDetailService.listSkuPackByIds(skuIds);
        return skuList;
    }
    /**
     * 根据skuid 集合获取到sku销售信息 （基础信息+产品信息+销售信息）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2023-03-21 12:06
     */
    @PostMapping("/listSkuSaleByIds")
    public List<SkuVO> listSkuSaleByIds(@RequestBody List<String> skuIds){
        List<SkuVO> skuList = productDetailService.listSkuSaleByIds(skuIds);
        return skuList;
    }
    /**
     * 根据skuid 集合获取到sku物流信息 （基础信息+产品信息+物流信息）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2023-03-21 12:06
     */
    @PostMapping("/listSkuLogisticsByIds")
    public List<SkuVO> listSkuLogisticsByIds(@RequestBody List<String> skuIds){
        List<SkuVO> skuList = productDetailService.listSkuLogisticsByIds(skuIds);
        return skuList;
    }

    /**
     * 根据skuid 集合获取到sku分类信息（基础信息+产品信息+分类信息）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2023-03-21 12:06
     */
    @PostMapping("/listSkuCategoryByIds")
    public List<SkuVO> listSkuCategoryByIds(@RequestBody List<String> skuIds){
        List<SkuVO> skuList = productDetailService.listSkuCategoryByIds(skuIds);
        return skuList;
    }
    /**
     * 根据skuid 集合获取到sku分类信息（基础信息+产品信息+采购信息）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2024-04-25 12:06
     */
    @PostMapping("/listSkuPurchaseByIds")
    public List<SkuVO> listSkuPurchaseByIds(@RequestBody List<String> skuIds){
        List<SkuVO> skuList = productDetailService.listSkuPurchaseByIds(skuIds);
        return skuList;
    }

    /**
     * 根据sku查询sku信息
     */
    @GetMapping("/listSkuPurchaseByIds")
    ProductDetailEntity getBySkuNoOrEan(String skuCode){
        return productDetailService.getBySkuNoOrEan(skuCode);
    }

    /**
     *  品质测量更新产品尺寸重量
     *
     */
    @PostMapping("/dimensionalWeightMeasure")
    public String dimensionalWeightMeasure(@RequestBody DimensionalWeightDTO dto){
        return productDetailService.dimensionalWeightMeasure(dto);
    }

    @GetMapping("/listApproveAndListingSku")
    List<SkuVO> listApproveAndListingSku(){
        return productDetailService.listApproveAndListingSku();
    }

    @GetMapping("/getCategoryByQuerySql")
    List<String> getCategoryByQuerySql(@RequestParam String compareCodeSplicingValueSql){
        return productDetailService.getCategoryByQuerySql(compareCodeSplicingValueSql);
    }

    @GetMapping("/getBrandByQuerySql")
    List<String> getBrandByQuerySql(@RequestParam String compareCodeSplicingValueSql){
        return productDetailService.getBrandByQuerySql(compareCodeSplicingValueSql);
    }
}
