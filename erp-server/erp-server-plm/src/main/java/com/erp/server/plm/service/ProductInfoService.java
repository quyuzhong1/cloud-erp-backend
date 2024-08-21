package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductInfoEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 产品信息表 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface ProductInfoService extends IService<ProductInfoEntity> {

    int countByCategoryId(String id);

    /**
     * 保存或者修改产品
     * @param dto
     * @return
     */
    String saveOrUpdateProduct(ProductDTO dto);

    Boolean updateCategory(MoveCategoryDTO dto);

    Boolean removeProduct(RemoveProductDTO dto);

    void exportTemplate(HttpServletRequest request, HttpServletResponse response);

    PagingVO<ProductShowDTO> paging(PagingDTO<ProductSearchDTO.PagingParamDTO> dto);

    /**
     * 处理部门数据
     */
    Boolean handlePagingDept(ProductSearchDTO.PagingParamDTO params);

    /**
     * @description: 查询产品列表数据（无分页）
     * @author Will
     * @date: 2023/2/10 14:35
     * @param dto
     * @return List<ProductShowDTO>
     */
    List<BasicDTO> listProductInfo(ProductSearchDTO.PagingParamDTO dto);

    /**
     * 保存模板
     * @author yl
     * @date 2023-03-07 9:36
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean saveTemplate(SaveProductTemplateDTO dto);

    /**
     * 更改项目状态
     * @author yl
     * @date 2023-03-07 9:37
     * @param productId
     * @param state
     * @return void
     */
    void updateProjectStatus(String productId, Integer state);

    List<Map<String, Object>> getListObjs();

    ProductDTO info(String id);
    /**
     * @Description 无规格sku修改产品信息
     * @Author Luo_WG
     * @Date 2022/9/21 18:44
     * @param dto:产品基础信息请求参数
     * @return java.lang.String
     **/
    String updateSpec(ProductInfoDTO dto);

    void updateProduct(UpdateProductDTO dto);

    void exportProductData(ExportProductDataDTO dto);


    List<CountDTO> getProductRelevanceList();


    List<ProductProjectDTO> getProductAndProjectList();


    ProductShowDTO getProductInfo(String productId);

    List<ProductShowDTO> getProductInfoByIds(List<String> productIds);

    void checkProduct(String productId);
    /**
     * @description: 根据spu参数查询
     * @author Will
     * @date: 2022/12/26 11:59
     * @param params
     * @return ProductInfoDTO
     */
    ProductInfoDTO getSpuByParam(Map<String, String> params);

    /**
     * 更改信息 基于sku 变更的
     * @author yl
     * @date 2023-02-07 19:58
     * @param productInfoDTO
     * @return void
     */
    void updateSpecByChangeSku(ProductInfoDTO productInfoDTO);
    /**
     * @description: 根据spu编码查询产品
     * @author Will
     * @date: 2023/2/8 14:49
     * @param spuNo
     * @return ProductInfoEntity
     */
    ProductInfoEntity getBySpuNo(String spuNo);

    /**
     * @description: 根据名称查询产品
     * @author Will
     * @date: 2023/2/22 19:27
     * @param name
     * @return ProductInfoEntity
     */
    ProductInfoEntity getByName(String name);

    Boolean setProgressStatus(SetProductProgressStatusDTO dto);

    Boolean setSchematicImageUrl(SetSchematicImageUrlDTO dto);

    /**
     * 根据分类id 获取到产品信息
     * @author yl
     * @date 2023-02-28 17:10
     * @param categoryIds
     * @return java.util.List<com.erp.model.plm.entity.ProductInfoEntity>
     */
    List<ProductInfoEntity> getByCategoryIds(List<String> categoryIds,Integer isFinishedProductDev);


    /**
     * 查询产品列表 和产品开发列表的分类产品
     * @author yl
     * @date 2023-03-02 11:56
     * @param categoryIds
     * @param isFinishedProductDev  是否是产品开发
     * @param  isArchive 是否 是产品归档数据 true 是
     * @return java.util.List<com.erp.model.plm.entity.ProductInfoEntity>
     */
    List<ProductInfoEntity> getListByCategoryIds(List<String> categoryIds, boolean isFinishedProductDev,boolean isArchive);

    /**
     * 查询角色分类 列表信息
     * @author yl
     * @date 2023-03-03 15:15
     * @param isFinishedProductDev 是否是产品开发管理
     * @param isArchive 是否是归档
     * @return java.util.List<com.erp.model.plm.entity.ProductInfoEntity>
     */
    List<ProductInfoEntity> getRoleClassifyList(boolean isFinishedProductDev, boolean isArchive);

    /**
     * 根据名称查询产品信息
     * @Author Luo_WG
     * @Date 2023/3/29 14:26
     * @param name 产品名称
     * @return com.erp.model.plm.entity.ProductInfoEntity
     **/
    ProductInfoEntity getProductByName(String name);

    /**
     * 获取所有产品信息包括删除，用来同步到DMP
     * @Author Luo_WG
     * @Date 2023/3/29 14:26
     * @return com.erp.model.plm.entity.ProductInfoEntity
     **/
    List<ProductInfoEntity> getProductInfoAll();

    /**
     * 根据sku id
     * 获取产品 角色信息
     * 质检通知 要发送信息
     * @author yl
     * @date 2023-04-28 12:24
     * @param skuIds
     * @return com.erp.model.plm.dto.ProductInfoDTO.ProductRolePeopleDTO
     */
    List<ProductInfoDTO.ProductRolePeopleDTO> getRolePeople(List<String>  skuIds);

    /**
     * 我的项目
     * @author yl
     * @date 2023-06-12 16:56
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.plm.dto.ProductShowDTO>
     */
    PagingVO<ProductShowDTO> myProject(PagingDTO<ProductSearchDTO.PagingParamDTO> dto);

    /**
     * 收藏的项目
     * @author yl
     * @date 2023-06-12 16:58
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.plm.dto.ProductShowDTO>
     */
    PagingVO<ProductShowDTO> collect(PagingDTO<ProductSearchDTO.PagingParamDTO> dto);

    /**
     * 获取所有项目的统计
     * @author yl
     * @date 2023-06-13 14:51
     * @param
     * @return com.erp.model.plm.dto.ProductDTO.ProductCountDTO
     */
    ProductDTO.ProductCountDTO allCount();

    /**
     * 我的项目统计
     * @author yl
     * @date 2023-06-13 14:51
     * @param
     * @return com.erp.model.plm.dto.ProductDTO.ProductCountDTO
     */
    ProductDTO.ProductCountDTO myProjectCount();

    /**
     * 收藏的项目
     * @author yl
     * @date 2023-06-13 16:34
     * @param
     * @return com.erp.model.plm.dto.ProductDTO.ProductCountDTO
     */
    ProductDTO.ProductCountDTO collectCount();

    /**
     * 获取下拉列表
     * @return
     */
    List<ProductDTO.DropdownDTO> getItemDropdown();


    /**
     * 批量立项
     * @param idsTimeDto
     * @return
     */
    Boolean batchEstablish(ProductInfoDTO.IdsDateDto idsTimeDto);

    /**
     * 产品概览
     * @author yl
     * @date 2023-06-14 17:44
     * @param id
     * @return com.erp.model.plm.dto.ProductOverviewDTO.InfoDTO
     */
    ProductOverviewDTO.InfoDTO overview(String id);

    /**
     * 导出数据
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-15 9:19
     */
    Boolean allExport(ProductSearchDTO.ExportDTO dto);

    /**
     * 我的项目导出
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-15 10:02
     */
    Boolean myProjectExport(ProductSearchDTO.ExportDTO dto);

    /**
     * 收藏项目导出
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-15 10:10
     */
    Boolean collectExport(ProductSearchDTO.ExportDTO dto);

    /**
     * @description: 根据负责人id查询
     * @author Will
     * @date: 2023/10/24 18:15
     * @param chargeId
     * @return List<ProductInfoEntity>
     */
    List<ProductInfoEntity> listByChargeId(String chargeId);
    /**
     * @description: 根据skuid查询产品信息
     * @author Will
     * @date: 2023/11/16 15:16
     * @param skuIds
     * @return List<ProductDTO>
     */
    List<ProductDetailDTO.ProductDTO> listProductBySkuIds(List<String> skuIds);
}
