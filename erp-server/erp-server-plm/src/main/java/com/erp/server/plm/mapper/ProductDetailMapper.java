package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuInfoSimpleVO;
import com.erp.model.plm.vo.SkuSimpleVO;
import com.erp.model.plm.vo.SkuVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductDetailMapper extends BaseMapper<ProductDetailEntity> {
    /**
     * @Description 分页查询主页信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:47
     * @param query:分页参数
     * @param productSkuDTO:查询参数
     * @return java.util.List<com.erp.model.plm.dto.ProductDetailShowDTO>
     **/
    IPage<ProductDetailShowDTO> paging(Page<ProductSkuDTO> query, @Param("params") ProductSkuDTO productSkuDTO);

    /**
     * @Description 条件查询产品信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:47
     * @param name:产品名称
     * @return java.util.List<com.erp.model.plm.dto.ProductDetailShowDTO>
     **/
    ProductDetailShowDTO listProduct(@Param("name") String name, @Param("skuNo") String skuNo);

    /**
     * @Description 根据skuid获取产品主键信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:47
     * @param skuId:sku表Id
     * @return java.util.List<com.erp.model.plm.dto.ProductKeyDTO>
     **/
    ProductKeyDTO getProductKey(@Param("skuId") String skuId);

    /**
     * @Description 无规格产品信息明细
     * @Author Luo_WG
     * @Date 2022/9/22 14:09
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductNoDetailDTO>
     **/
    ProductNoDetailDTO getNoSpecDetailById(@Param("productId") String productId);

    /**
     * @Description 无规格产品信息明细根据SkuId查询
     * @Author Luo_WG
     * @Date 2022/9/22 14:09
     * @param skuId:skuId
     * @return java.util.List<com.erp.model.plm.dto.ProductNoDetailDTO>
     **/
    ProductNoDetailDTO getNoSpecDetailBySkuId(@Param("skuId") String skuId);

    /**
     * @Description 多规格产品信息明细
     * @Author Luo_WG
     * @Date 2022/9/22 15:11
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductManyDetailDTO>
     **/
    ProductManySpecBaseDTO getManySpecDetailById(@Param("productId") String productId);

    /**
     * 获得导出Excel的sku数据
     * @Author Luo_WG
     * @Date 2022/10/9 14:14
     * @param productSkuExcelDTO productSkuExcelDTO
     * @return java.util.List<com.erp.model.plm.dto.ProductDetailExcelDTO>
     **/
    List<ProductDetailExcelExportDTO> getExportSkuExcel(@Param("params") ProductSkuExcelDTO productSkuExcelDTO);

    List<BaseIdDTO> getNotFinish(@Param("skuIdList") List<String> skuIdList);

    /**
     * @Description 根据sku查询sku表信息(数据清洗)
     * @Author Luo_WG
     * @Date 2022/9/28 17:04
     * @param sku：sku
     * @return com.erp.model.plm.entity.ProductDetailEntity
     **/
    CleanSkuDto getProductIdBySkuClean(@Param("sku") String sku);

    List<SkuVO> getSkuBySkuNos(@Param("skuList") List<String> skuNoList,@Param("status") Integer status);
    /**
     * @description: 根据sku编号查询（带权限）
     * @author Will
     * @date: 2023/10/24 12:09
     * @param skuParamDTO
     * @return List<ProductSearchDTO.SkuListDTO>
     */
    List<ProductSearchDTO.SkuListDTO> listSkuBySkuNos(@Param("params") ProductSearchDTO.SkuParamDTO skuParamDTO);

    List<SkuVO> searchSku(@Param("searchKeyword") String searchKeyword,@Param("state") Integer state);

    List<ChangeInfoDTO> searchStateSku(@Param("state") Integer state,@Param("searchKeyword") String searchKeyword);

    List<SkuVO> getSkuBySkuIds(@Param("skuIdList")List<String> skuIdList);

    List<SkuVO> searchParentSku(@Param("searchKeyword") String searchKeyword,@Param("state") Integer state,@Param("bomId") String bomId);


    /**
     * 根据skuid 集合获取到sku 信息
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author yl
     * @date 2023-03-21 12:06
     */
    List<SkuVO> getSkuInfoBySkuIds(@Param("skuIds") List<String> skuIds);
    /**
     * 获取sku基础信息
     * @param skuIds
     * @return
     */
    List<SkuVO> getSkuBaseBySkuIds(@Param("skuIds") List<String> skuIds);

    /**
     * 获取sku 采购信息
     * @param skuIds
     * @return
     */
    List<SkuVO> listSkuPurchaseBySkuIds(@Param("skuIds") List<String> skuIds);

    /**
     * 获取所有产品明细包括删除，用来同步到DMP
     * @Author Luo_WG
     * @Date 2023/4/19 16:12
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     **/
    List<ProductDetailEntity> getProductDetailAll();

    /**
     * 批量更新字段
     * @Author Luo_WG
     * @Date 2023/6/15 12:17
     * @param ids
     * @param tableName
     * @param filedName
     * @param values
     * @return java.lang.Boolean
     **/
    Boolean updateFiledBatch(@Param("ids") List<String> ids, @Param("tableName") String tableName, @Param("filedName") String filedName, @Param("values") Object values, @Param("keyName") String keyName);

    /**
     * 获取不参与库存操作的sku
     * @return
     */
    List<SkuVO> getNoInventorySku();

    /**
     * PDA:条件查询sku
     * @Author Luo_WG
     * @Date 2023/8/21 12:09
     * @param dto
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     **/
    List<SkuVO> pdaSearchSku(ProductDetailDTO.PdaSearchDTO dto);

    /**
     * PDA:产品查询
     * @Author Luo_WG
     * @Date 2023/9/4 18:49
     * @param skuNo
     * @return com.erp.model.plm.dto.PdaProductDetailDTO.View
     **/
    PdaProductDetailDTO.View pdaProductView(@Param("skuNo") String skuNo);
    /**
     * @description: 根据负责人id查询
     * @author Will
     * @date: 2023/10/24 18:23
     * @param chargeId
     * @return List<ProductDetailEntity>
     */
    List<ProductDetailEntity> listByChargeId(@Param("chargeId") String chargeId);

    /**
     * 物流产品分页
     * @param query
     * @param params
     * @return
     */
    IPage<LogisticsProductDTO.PagingVO> logisticsProductPaging(Page query,@Param("params") LogisticsProductDTO.PagingParamDTO params,@Param("approveStatus") Integer approveStatus);

    /**
     * 获取基础数据
     * @author yl
     * @date 2023-11-07 14:52
     * @param skuId
     * @return com.erp.model.plm.dto.LogisticsProductDTO.ProductBaseInfoDTO
     */
    LogisticsProductDTO.ProductBaseInfoDTO getProductBaseInfo(@Param("skuId") String skuId);

    /**
     * 导出
     * @author yl
     * @date 2023-11-08 11:34
     * @param dto
     * @param approvalStatus
     * @return java.util.List<com.erp.model.plm.dto.LogisticsProductDTO.PagingVO>
     */
    List<LogisticsProductDTO.ExportInfoDTO> listExport(@Param("params") LogisticsProductDTO.ExportDTO dto, @Param("approveStatus")Integer approvalStatus);
    Page<LogisticsProductDTO.ExportInfoDTO> listExport(@Param("page") Page<LogisticsProductDTO.ExportInfoDTO> page,@Param("params") LogisticsProductDTO.ExportDTO dto, @Param("approveStatus")Integer approvalStatus);

    /**
     * 更新分页
     * @param query
     * @param params
     * @param approvalStatus
     * @param fieldList
     * @return
     */
    IPage<LogisticsProductDTO.UpdatePagingDTO> logisticsProductUpdatePaging(Page<LogisticsProductDTO.UpdatePagingParamDTO> query,@Param("params") LogisticsProductDTO.UpdatePagingParamDTO params,@Param("approveStatus") Integer approvalStatus,@Param("fieldList") List<String> fieldList);

    Integer logisticsProductUpdateCount(@Param("approveStatus")Integer approvalStatus,@Param("fieldList") List<String> fieldList,@Param("permissionSql")String permissionSql);

    /**
     * 根据sku IdList 获取物流产品信息
     *@parms skuIdList
     *@return
     *@author yl
     *@date 2023-11-27
     */
    List<LogisticsProductDTO.ProductDTO> listLogisticsProduct(@Param("skuIdList") List<String> skuIdList,@Param("skuNoList") List<String> skuNoList);

    /**
     * 搜索SKU只带组合信息
     */
    List<SkuSimpleVO> searchSkuWithCombination(@Param("searchKeyword") String searchKeyword, @Param("state") Integer state);

    List<SkuVO> getSkuInfoAdvanceQuery(@Param("params") AdvanceQueryContainer advanceQueryContainer);

    /**
     *  搜索sku
     * @param query
     * @param params
     * @return
     */
    IPage<ProductDetailDTO.SkuDTO> listSku(Page<ProductDetailDTO.SkuDTO> query, @Param("params") ProductSkuDTO params);

    /**
     * 根据SkuIds获取SKU简单信息
     */
    List<SkuInfoSimpleVO> getSimpleSkuInfoByIds(@Param("skuIds") List<String> skuIds);
    /**
     * @description: 远程搜索包装辅料SKU
     * @author Will
     * @date: 2024/4/18 14:18
     * @param searchKeyword
     * @param state
     * @return List<SkuVO>
     */
    List<SkuVO> accessoriesSku(@Param("searchKeyword") String searchKeyword,@Param("state") Integer state);
    /**
     * 根据skuid 集合获取到sku基础信息 + 采购信息（产品采购信息+产品采购含税单价）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2023-03-21 12:06
     */
    List<SkuVO> listSkuCostByIds(@Param("skuIds")List<String> skuIds);
    /**
     * 根据skuid 集合获取到sku基础信息 + 产品信息
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2023-03-21 12:06
     */
    List<SkuVO> listSkuProductByIds(@Param("skuIds") List<String> skuIds);
    /**
     * 根据skuid 集合获取到sku基础信息 + 产品信息 + 包裹信息
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2023-03-21 12:06
     */
    List<SkuVO> listSkuPackByIds(@Param("skuIds")List<String> skuIds);
    /**
     * 根据skuid 集合获取到sku基础信息 + 产品信息 +销售信息
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2023-03-21 12:06
     */
    List<SkuVO> listSkuSaleByIds(@Param("skuIds")List<String> skuIds);
    /**
     * 根据skuid 集合获取到sku基础信息 + 产品信息 + 物流信息
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2023-03-21 12:06
     */
    List<SkuVO> listSkuLogisticsByIds(@Param("skuIds")List<String> skuIds);
    /**
     * 根据skuid 集合获取到sku基础信息 + 产品信息 + 品类信息
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2023-03-21 12:06
     */
    List<SkuVO> listSkuCategoryByIds(@Param("skuIds")List<String> skuIds);
    /**
     * 根据skuid 集合获取到sku基础信息 + 产品信息 + 采购信息
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2023-03-21 12:06
     */
    List<SkuVO> listSkuPurchaseByIds(@Param("skuIds")List<String> skuIds);
    /**
     * @description: tab
     * @author Will
     * @date: 2024/3/18 19:26
     * @param searchParamDTO
     * @return Integer
     */
    Integer listCount(@Param("params") LogisticsProductDTO.PagingParamDTO searchParamDTO);

    /**
     * 获取已审核sku 未计算目的国申报价数据
     * @return
     */
    List<ProductDetailEntity> getProductDetailByDestDeclarePrice();
    IPage<SkuVO> pagingSelect(Page<SkuVO.SelectDTO> query, @Param("params")SkuVO.SelectDTO params);

    List<SkuVO.ProductChargeInfoDTO> listProductChargeInfoByIds(@Param("skuIds")List<String> skuIds);
    List<SkuVO> listApproveAndListingSku();

    List<String> getCategoryByQuerySql(@Param("compareCodeSplicingValueSql") String compareCodeSplicingValueSql);

    List<String> getBrandByQuerySql(@Param("compareCodeSplicingValueSql") String compareCodeSplicingValueSql);

    /**
     * 根据skuIds获取产品包装尺寸明细
     */
    List<ProductPackViewDTO> listProductPackBySkuIds(@Param("skuIds") List<String> skuIds);
}


