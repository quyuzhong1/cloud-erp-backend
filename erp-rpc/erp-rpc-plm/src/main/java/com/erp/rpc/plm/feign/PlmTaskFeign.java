package com.erp.rpc.plm.feign;

import com.common.business.dto.base.BaseIdsDTO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.vo.ProductRefLabelVO;
import com.erp.model.plm.vo.ProductVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.workflow.dto.WorkOptionDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
    @PostMapping("feign/product/getSkuInfoByIds")
    List<SkuVO> getSkuInfoByIds(@RequestBody List<String> skuIds);


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
    List<ProductDetailEntity> listByCreateTimeList(@Param("createTimeList") List skuCreateTimeList);

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
     * 回填产品包装信息
     */
    @PostMapping("/feign/productPack/backFillPackaging")
    void backFillPackaging(@RequestBody List<ProductPackDTO>  productPackList);

    /**
     * @description: 更新任务列表负责人名称
     * @author Will
     * @date: 2023/10/19 11:06
     * @param sysUserInfoDTO
     */
    @PostMapping("/feign/projectTask/updateProjectTaskChargeName")
    void updateProjectTaskChargeName(SysUserInfoDTO sysUserInfoDTO);
}