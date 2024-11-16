package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.dto.excel.TaskExportDTO;
import com.erp.model.plm.entity.ProductInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 产品信息表 Mapper 接口
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Mapper
public interface ProductInfoMapper extends BaseMapper<ProductInfoEntity> {

    IPage<ProductShowDTO> paging(Page<ProductSearchDTO.PagingParamDTO> query, @Param("params") ProductSearchDTO.PagingParamDTO params,@Param("categoryIdList") List<String> categoryIdList);

    List<ProductShowDTO> listAllExport(@Param("params") ProductSearchDTO.ExportDTO params,@Param("categoryIdList") List<String> categoryIdList);

    List<TaskExportDTO.ProductTaskExcelDTO> listAllTaskExport(@Param("params") ProductSearchDTO.ExportDTO params, @Param("categoryIdList")List<String> categoryIdList);
    /**
     * 我的项目
     * @param query
     * @param params
     * @param categoryIdList
     * @return
     */
    IPage<ProductShowDTO> myProjectPaging(Page<ProductSearchDTO.PagingParamDTO> query, @Param("params")ProductSearchDTO.PagingParamDTO params, @Param("categoryIdList")List<String> categoryIdList,@Param("userId") String userId);

    /**
     * 我的项目导出
     * @param params
     * @param categoryIdList
     * @param userId
     * @return
     */
    List<ProductShowDTO> listMyProjectExport(@Param("params") ProductSearchDTO.ExportDTO params, @Param("categoryIdList")List<String> categoryIdList, @Param("userId")String userId);

    List<TaskExportDTO.ProductTaskExcelDTO> listMyProjectTaskExport(@Param("params")ProductSearchDTO.ExportDTO params, @Param("categoryIdList")List<String> categoryIdList,@Param("userId")String userId);


    /**
     * 收藏的项目
     * @param query
     * @param params
     * @param categoryIdList
     * @return
     */
    IPage<ProductShowDTO> collect(Page<ProductSearchDTO.PagingParamDTO> query, @Param("params") ProductSearchDTO.PagingParamDTO params, @Param("categoryIdList")List<String> categoryIdList,@Param("userId")String userId);

    List<ProductShowDTO> collectExport(@Param("params") ProductSearchDTO.ExportDTO params,@Param("categoryIdList") List<String> categoryIdList);


    List<TaskExportDTO.ProductTaskExcelDTO> collectTaskExport(@Param("params") ProductSearchDTO.ExportDTO params,@Param("categoryIdList") List<String> categoryIdList);

    List<BasicDTO> listNotPaging(@Param("params") ProductSearchDTO.PagingParamDTO params,@Param("archiveProductIds") List<String> archiveProductIds,@Param("categoryIdList") List<String> categoryIdList);

    List<ProductExcelDTO> getExportProduct(@Param("productIds") List<String> productIds);

    List<CountDTO> getProductRelevanceList();

    List<ProductProjectDTO> getProductAndProjectList(@Param("archiveProductIdList") List<String> archiveProductIdList,@Param("projectStatus") Integer projectStatus);

    ProductShowDTO getProductInfo(@Param("productId") String productId);

    List<ProductShowDTO> getProductInfoByIds(@Param("productIds") List<String> productIds);

    /**
     * 获取所有产品信息包括删除，用来同步到DMP
     * @Author Luo_WG
     * @Date 2023/4/19 16:22
     * @return java.util.List<com.erp.model.plm.entity.ProductInfoEntity>
     **/
    List<ProductInfoEntity> getProductInfoAll();

    /**
     * 根据sku id
     * 获取产品 角色信息
     * 质检通知 要发送信息
     * @author yl
     * @date 2023-04-28 12:24
     * @param skuIdList
     * @return com.erp.model.plm.dto.ProductInfoDTO.ProductRolePeopleDTO
     */
    List<ProductInfoDTO.ProductRolePeopleDTO> getRolePeopleBySkuId(@Param("skuIdList") List<String> skuIdList);


    List<ProductDTO.CountBaseDTO> listStatusCount(@Param("productIdList") List<String> productIdList);

    List<ProductDTO.CountBaseStrDTO> listProgressStatusCount(@Param("productIdList") List<String> productIdList);

    List<String> listProductIdByUserId(@Param("userId")String userId);

    /**
     * 获取概览的基本信息
     * @param productId
     * @return
     */
    ProductOverviewDTO.InfoDTO overviewBase(@Param("productId")String productId);


    List<ProductDTO.CountBaseDTO> listMyProjectStatusCount(@Param("userId") String userId);

    List<ProductDTO.CountBaseStrDTO> listMyProjectProgressStatusCount(@Param("userId") String userId);

    /**
     * 收藏的统计
     * @author yl
     * @date 2023-06-16 15:12
     * @param
     * @return java.util.List<com.erp.model.plm.dto.ProductDTO.CountBaseDTO>
     */
    List<ProductDTO.CountBaseDTO> listCollectStatusCount(@Param("userId") String userId);

    List<ProductDTO.CountBaseStrDTO> listCollectProgressStatusCount(@Param("userId") String userId);
    /**
     * @description: 根据负责人id查询
     * @author Will
     * @date: 2023/10/24 18:16
     * @param chargeId
     * @return List<ProductInfoEntity>
     */
    List<ProductInfoEntity> listByChargeId(@Param("chargeId")String chargeId);
    /**
     * @description: 根据skuid查询产品信息
     * @author Will
     * @date: 2023/11/16 15:17
     * @param skuIds
     * @return List<ProductDTO>
     */
    List<ProductDetailDTO.ProductDTO> listProductBySkuIds(@Param("skuIds") List<String> skuIds);
}
