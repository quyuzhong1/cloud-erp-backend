package com.erp.rpc.plm.feign;

import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.vo.*;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.workflow.dto.WorkOptionDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * plm 远程调用接口
 *
 * @Classname PlmTaskFeign
 * @Date 2022-10-21 9:06
 * @Created by yl
 */
@FeignClient(name = "erp-plm")
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

    @GetMapping("feign/dict/listDictByType")
    List<BasicDictEntity> listDictByType(@RequestParam("type") String type);

    /**
     * 根据spu的参数查询spu，参数：id、spuNo
     */
    @PostMapping("feign/product/getSpuByParam")
    ProductInfoDTO getSpuByParam(@RequestBody Map<String, String> params);

    /**
     * 更新业务单据状态
     */
    @PostMapping("feign/product/updateBusinessSyncKingdeeStatus")
    void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, Object> params);
    /**
     * 根据skuid集合获取到sku 信息
     *
     * @param skuIds
     * @return
     * @author yl
     * @date 2023-03-21 12:19
     */
    @PostMapping("feign/product/listSkuPurchaseBySkuIds")
    List<SkuVO> listSkuPurchaseBySkuIds(@RequestBody List<String> skuIds);

    @PostMapping("feign/product/getSkuInfoAdvanceQuery")
    List<SkuVO> getSkuInfoAdvanceQuery(@RequestBody AdvanceQueryContainer advanceQueryContainer);

    /**
     * 根据sku no 获取信息
     *
     * @param skuNoList
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author yl
     * @date 2023-06-27 17:48
     */
    @PostMapping("feign/product/listBySkuNos")
    List<SkuVO> listBySkuNoList(@RequestBody List<String> skuNoList);

    @PostMapping("feign/product/listBySkuNoList")
    List<ProductDetailEntity> listBySkuNos(@RequestBody List<String> skuNoList);

    /**
     * @return List<SkuVO>
     * @description: 获取已审核sku
     * @author Will
     * @date: 2023/3/21 14:12
     */
    @GetMapping("feign/product/listApproveSku")
    List<SkuVO> listApproveSku();

    /**
     * 根据id查询sku信息
     *
     * @param ids ids
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     * @Author Luo_WG
     * @Date 2023/4/14 15:10
     **/
    @PostMapping("feign/product/getByIdList")
    List<ProductDetailEntity> getByIdList(@RequestBody List<String> ids);

    /**
     * 根据sku id 集合获取到产品包装信息
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.ProductVO.ProductPackVO>
     * @author yl
     * @date 2023-04-17 18:37
     */
    @PostMapping("feign/product/getProductPackBySkuIds")
    List<ProductVO.ProductPackVO> getProductPackBySkuIds(@RequestBody List<String> skuIds);

    /**
     * 根据用户获取各任务阶段数量
     *
     * @param optionUserId optionUserId
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.StageViewDTO>
     * @Author Luo_WG
     * @Date 2023/4/24 9:34
     **/
    @PostMapping("feign/product/stageView")
    List<WorkOptionDTO.StageViewDTO> stageView(@RequestBody String optionUserId);

    /**
     * 根据skuid 获取到产品角色的 人员
     *
     * @param skuIds
     * @return com.erp.model.plm.dto.ProductInfoDTO.ProductRolePeopleDTO
     * @author yl
     * @date 2023-04-28 12:21
     */
    @PostMapping("feign/product/getRolePeople")
    List<ProductInfoDTO.ProductRolePeopleDTO> listProductRolePeople(@RequestBody List<String> skuIds);

    /**
     * 根据入参查询单据数量
     *
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    @PostMapping("feign/plmWorkOption/getTableNum")
    List<WorkOptionDTO.MyWorkOptionDTO> getTableNum(@RequestBody List<WorkOptionDTO.MyWorkOptionDTO> tableNumDTOList);

    /**
     * bom  审核 通过
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("feign/plmWorkOption/bomInfoApprovalPass")
    void bomInfoApprovalPass(@RequestBody @Validated AuditParamDTO dto);

    /**
     * bom  审核 不通过
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("feign/plmWorkOption/bomInfoApprovalNoPass")
    void bomInfoApprovalNoPass(@RequestBody @Validated AuditParamDTO dto);

    /**
     * 产品信息-状态操作-审核通过
     *
     * @param dto
     * @return ApiResult
     */
    @PostMapping("feign/plmWorkOption/productDetailApprovalPass")
    Boolean productDetailApprovalPass(@RequestBody @Validated ProductDetailOperateDTO dto);

    /**
     * 产品信息-状态操作-审核不通过
     *
     * @param dto
     * @return ApiResult
     */
    @PostMapping("feign/plmWorkOption/productDetailApprovalNoPass")
    Boolean productDetailApprovalNoPass(@RequestBody @Validated ProductDetailOperateDTO dto);

    /**
     * 项目任务-任务分页列表 -状态操作-审核通过
     *
     * @return
     */
    @PostMapping("feign/plmWorkOption/projectTaskApprovalPass")
    Boolean projectTaskApprovalPass(@RequestBody @Validated TaskOperateDTO dto);

    /**
     * 项目任务-任务分页列表 -状态操作-审核不通过
     *
     * @return
     */
    @PostMapping("feign/plmWorkOption/projectTaskApprovalNoPass")
    Boolean projectTaskApprovalNoPass(@RequestBody @Validated TaskOperateDTO dto);

    /**
     * change 审核 通过
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("feign/plmWorkOption/productChangeApprovalPass")
    void productChangeApprovalPass(@RequestBody @Validated AuditParamDTO dto);

    /**
     * change  审核 不通过
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("feign/plmWorkOption/productChangeApprovalNoPass")
    void productChangeApprovalNoPass(@RequestBody @Validated AuditParamDTO dto);

    /**
     * @param skuIds
     * @return List<BomChildrenSkuDTO>
     * @description: 查询bom子件信息
     * @author Will
     * @date: 2023/5/17 9:32
     */
    @PostMapping("feign/bom/listBomChildBySkuIds")
    List<BomChildrenSkuDTO> listBomChildBySkuIds(@RequestBody List<String> skuIds);

    /**
     * 根据skuid获取bom类型信息
     * @param skuIds
     * @return
     */
    @PostMapping("feign/bom/listBomBySkuIds")
    List<BomChildrenSkuDTO> listBomBySkuIds(@RequestBody List<String> skuIds);

    /**
     * 查询bom子件信息
     *
     * @param skuNos
     * @return java.util.List<com.erp.model.plm.dto.BomChildrenSkuDTO>
     * @Author Luo_WG
     * @Date 2023/9/14 12:12
     **/
    @PostMapping("feign/bom/listBomChildBySkuNos")
    List<BomChildrenSkuDTO> listBomChildBySkuNos(@RequestBody List<String> skuNos);

    /**
     * @param skuIds
     * @return List<BomChildrenSkuDTO>
     * @description: 查询历史子件信息
     * @author Will
     * @date: 2023/8/21 10:36
     */
    @PostMapping("feign/bom/listHistoryBomChildBySkuIds")
    List<BomChildrenSkuDTO> listHistoryBomChildBySkuIds(@RequestBody List<String> skuIds);

    /**
     * 根据任务id获取产品id
     *
     * @return
     */
    @PostMapping("feign/plmWorkOption/getProductIdByTaskId")
    ProjectTaskEntity getProductIdByTaskId(@RequestBody String taskId);

    /**
     * @param skuIds
     * @return List<BomInfoEntity>
     * @description: 根据父级skuIds查询BOM信息
     * @author Will
     * @date: 2023/5/31 10:57
     */
    @PostMapping("feign/bom/listBomByParentSkuIds")
    List<BomInfoEntity> listBomByParentSkuIds(List<String> skuIds);

    /**
     * 根据sku id集合获取采购员、供应商信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/feign/product/getPurchaseInfoBySkuIds")
    Map<String, SkuPurchaseDTO.PurchaseInfo> getPurchaseInfoBySkuIds(@RequestBody @Validated BaseIdsDTO.IdsDTO dto);

    /**
     * 更新不可删除标识
     *
     * @param skuIds skuIds
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/6/15 11:32
     **/
    @PostMapping("/feign/product/updateOccupyStatus")
    Boolean updateOccupyStatus(@RequestBody @Validated List<String> skuIds);

    /**
     * @param skuNos
     * @return List<BomInfoEntity>
     * @description: 根据父级skuNos查询BOM信息
     * @author Will
     * @date: 2023/5/31 10:57
     */
    @PostMapping("feign/bom/listBomByParentSkuNos")
    List<BomInfoEntity> listBomByParentSkuNos(List<String> skuNos);


    /**
     * 获取不参与库存操作的sku
     *
     * @return
     */
    @PostMapping("/feign/product/getNoInventorySku")
    List<SkuVO> getNoInventorySku();

    @PostMapping("feign/product/listByCreateTimeList")
    List<ProductDetailEntity> listByCreateTimeList(@Param("createTimeList") List<LocalDateTime> skuCreateTimeList);

    /**
     * 获取到父级的分类id
     *
     * @return
     */
    @PostMapping("feign/product/listCategoryByIds")
    List<BasicCategoryEntity> listCategoryByIds(@RequestBody List<String> idList);

    /**
     * 获取分类数结构
     * @author yl
     * @date 2023-09-26 16:51
     * @param
     * @return java.util.List<com.erp.model.plm.dto.BasicCategoryDTO>
     */
    @GetMapping("feign/category/tree")
    List<BasicCategoryDTO> listCategoryTree();
    /**
     * 获取分类列表
     * @author yl
     * @date 2023-09-26 16:51
     * @param
     * @return java.util.List<com.erp.model.plm.dto.BasicCategoryDTO>
     */
    @GetMapping("feign/category/getCategoryList")
    List<BasicCategoryEntity> getCategoryList();
    /**
     * 获取到父级的分类
     *
     * @return
     */
    @GetMapping("feign/product/listParentCategory")
    List<BasicCategoryEntity> listParentCategory();

    /**
     * 获取到sku 销售信息
     */
    @PostMapping("feign/product/listSkuSalesBySkuNos")
    List<SkuDTO.SalesDTO> listSkuSalesBySkuNos(List<String> skuNoList);

    /**
     * 根据sku获取标签列表
     *
     * @param skuId
     * @return
     */
    @PostMapping("feign/product/getProductRelLabelBySkuId")
    List<ProductRefLabelVO> getProductRelLabelBySkuId(String skuId);


    /**
     * 根据sku获取标签列表
     *
     * @param skuIds
     * @return
     */
    @PostMapping("feign/product/getProductRelLabelBySkuIds")
    List<ProductRefLabelVO> getProductRelLabelBySkuIds(Set<String> skuIds);
    /**
     * 通过子类id或名称获取到父级的分类
     */
    @GetMapping("feign/product/parentCategory")
    BasicCategoryDTO getParent(Map<String, String> categoryParams);

    /**
     * 批量修改产品信息
     * @param list
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     **/
    @PostMapping("feign/product/updateProductDetailBatch")
    Boolean updateProductDetailBatch(@RequestBody List<ProductDetailEntity> list);

    /**
     * 回填产品包装信息
     */
    @PostMapping("/feign/productPack/backFillPackaging")
    void backFillPackaging(@RequestBody List<ProductPackDTO>  productPackList);

    /**
     * 批量修改产品采购信息首批下单日期
     * @param list
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     **/
    @PostMapping("feign/product/updateProductPlaceOrderTimeBatch")
    Boolean updateProductPlaceOrderTimeBatch(@RequestBody List<ProductPurchaseEntity> list);

    /**
     * 批量修改产品销售信息上市日期
     * @param list
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     **/
    @PostMapping("feign/product/updateProductSaleListingTimeBatch")
    Boolean updateProductSaleListingTimeBatch(@RequestBody List<ProductSaleEntity> list);

    /**
     * 根据skuId查询产品采购信息
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.entity.ProductPurchaseEntity>
     **/
    @PostMapping("feign/product/listProductPurchaseBySkuId")
    List<ProductPurchaseEntity> listProductPurchaseBySkuId(@RequestBody List<String> skuIds);

    /**
     * 根据skuId查询产品销售信息
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.entity.ProductSaleEntity>
     **/
    @PostMapping("feign/product/listProductSaleBySkuId")
    List<ProductSaleEntity> listProductSaleBySkuId(@RequestBody List<String> skuIds);

    /**
     * @description: 更新任务列表负责人名称
     * @author Will
     * @date: 2023/10/19 11:06
     * @param sysUserInfoDTO
     */
    @PostMapping("/feign/projectTask/updateProjectTaskChargeName")
    void updateProjectTaskChargeName(SysUserInfoDTO sysUserInfoDTO);

    /**
     * @description: 查询数据发送同步任务
     * @author Will
     * @date: 2023/10/30 10:38
     * @param syncParamDTO
     */
    @PostMapping("/feign/plmSyncTask/findDataSendSyncTask")
    void findDataSendSyncTask(@RequestBody DmpSyncMqDTO.SyncParamDTO syncParamDTO);

    /**
     * 查询sku版本信息
     * @Author Luo_WG
     * @Date 2023/11/2 8:57
     * @param skuNos
     * @return java.util.List<com.erp.model.plm.dto.ProductBomInfoDTO.skuBomVersion>
     **/
    @PostMapping("feign/bom/listBomVersionBySkuNos")
    List<ProductBomInfoDTO.SkuBomVersion> listBomVersionBySkuNos(@RequestBody List<String> skuNos);

    /**
     * @description: 根据skuId集合信息查询（只查了spu、sku表）
     * @author Will
     * @date: 2023/11/16 15:14
     * @param skuIdList
     * @return List<ProductDTO>
     */
    @PostMapping("feign/product/listProductBySkuIds")
    List<ProductDetailDTO.ProductDTO> listProductBySkuIds(@RequestBody List<String> skuIdList);

    /**
     * @description: 查询所有父级
     * @author Will
     * @date: 2023/11/23 18:13
     * @param params
     * @return ListAllSkuDTO
     */
    @PostMapping("/feign/bom/listAllLevelSku")
    BomSkuPageDTO.ListAllSkuDTO listAllLevelSku(@RequestBody BomSkuPageDTO.AllSkuParamDTO params);


    /**
     * @description: 根据skuId集合信息查询物流产品信息（组合品根据combinationDeclareType判断是否拆分）
     * @author Will
     * @date: 2023/11/16 15:14
     * @param skuIdList
     * @return List<ProductDTO>
     */
    @PostMapping("feign/product/listProductLogisticsByIds")
    List<ProductDetailDTO.ProductLogisticDTO> listProductLogisticsByIds(@RequestBody List<String> skuIdList);


    /**
     * @description: 根据skuId查询海关编码
     * @author zdy
     * @date: 2023/11/16 15:14
     * @param dto
     * @return List<ProductDTO>
     */
    @PostMapping("feign/product/listProductCustomsBySkuIds")
    List<ProductCustomsEntity> listProductCustomsBySkuIds(@RequestBody ProductCustomsSkuDTO dto);

    /**
     * 根据SkuIds获取SKU简单信息
     */
    @PostMapping("feign/product/getSimpleSkuInfoByIds")
    List<SkuInfoSimpleVO> getSimpleSkuInfoByIds(@RequestBody List<String> skuIds);

    /**
     * 根据skuid 集合获取到sku产品信息（基础信息+产品信息)
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2024-04-25 12:06
     */
    @PostMapping("feign/product/listSkuProductByIds")
    List<SkuVO> listSkuProductByIds(@RequestBody List<String> skuIds);

    /**
     * 根据skuid 集合获取到sku基础信息（基础信息+成本信息+销售信息)
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2024-04-25 12:06
     */
    @PostMapping("feign/product/listSkuCostByIds")
    List<SkuVO> listSkuCostByIds(@RequestBody List<String> skuIds);

    /**
     * 根据skuid 集合获取到sku包装信息 （基础信息+产品信息+包装信息）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2024-04-25 12:06
     */
    @PostMapping("feign/product/listSkuPackByIds")
    List<SkuVO> listSkuPackByIds(@RequestBody List<String> skuIds);
    /**
     * 根据skuid 集合获取到sku销售信息 （基础信息+产品信息+销售信息）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2024-04-25 12:06
     */
    @PostMapping("feign/product/listSkuSaleByIds")
    List<SkuVO> listSkuSaleByIds(@RequestBody List<String> skuIds);
    /**
     * 根据skuid 集合获取到sku物流信息 （基础信息+产品信息+物流信息）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2024-04-25 12:06
     */
    @PostMapping("feign/product/listSkuLogisticsByIds")
    List<SkuVO> listSkuLogisticsByIds(@RequestBody List<String> skuIds);
    /**
     * 根据skuid 集合获取到sku分类信息（基础信息+产品信息+分类信息）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2024-04-25 12:06
     */
    @PostMapping("feign/product/listSkuCategoryByIds")
    List<SkuVO> listSkuCategoryByIds(@RequestBody List<String> skuIds);
    /**
     * 根据skuid 集合获取到sku分类信息（基础信息+产品信息+采购信息）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2024-04-25 12:06
     */
    @PostMapping("feign/product/listSkuPurchaseByIds")
    List<SkuVO> listSkuPurchaseByIds(@RequestBody List<String> skuIds);

    @GetMapping("feign/product/listSkuPurchaseByIds")
    ProductDetailEntity getBySkuNoOrEan(@RequestParam("skuCode") String skuCode);

    @PostMapping("feign/product/dimensionalWeightMeasure")
    String dimensionalWeightMeasure(@RequestBody DimensionalWeightDTO dto);

    /**
     * 获取已审核且已上市sku
     * @return List<SkuVO>
     */
    @GetMapping("feign/product/listApproveAndListingSku")
    List<SkuVO> listApproveAndListingSku();

    @GetMapping("/feign/product/getCategoryByQuerySql")
    List<String> getCategoryByQuerySql(@RequestParam String compareCodeSplicingValueSql);

    @GetMapping("/feign/product/getBrandByQuerySql")
    List<String> getBrandByQuerySql(@RequestParam String compareCodeSplicingValueSql);

    /**
     * 试产量产  审核 通过
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("feign/plmWorkOption/pilotApprovalPass")
    void pilotApprovalPass(@RequestBody @Validated ApproveOneDTO approveOneDTO);

    /**
     * 查询bom (可以查询全部)
     * @return
     */
    @PostMapping("feign/bom/listAllBom")
    List<BomDTO.BomSku> listAllBom(@RequestBody List<String> childSkuIdList);
}