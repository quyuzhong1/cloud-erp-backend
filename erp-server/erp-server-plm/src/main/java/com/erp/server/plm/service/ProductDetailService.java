package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductDetailEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface ProductDetailService extends IService<ProductDetailEntity> {

    /**
     * @Description 产品信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param pagingDTO:查询参数
     * @return PagingVO
     **/
    PagingVO<ProductDetailShowDTO> paging(PagingDTO<ProductSkuDTO> pagingDTO);

    /**
     * @Description 条件查询产品信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param name:产品名称
     * @return ProductDetailShowDTO
     **/
    ProductDetailShowDTO getProductBy(String name, String skuNo);

    /**
     * @Description 无规格产品信息明细
     * @Author Luo_WG
     * @Date 2022/9/22 12:05
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductNoDetailDTO>
     **/
    ProductNoSpecDetailAllDTO getNoSpecDetailById(String productId);

    /**
     * @Description 多规格产品信息明细
     * @Author Luo_WG
     * @Date 2022/9/22 12:05
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductVariantShowDTO>
     **/
    ProductManyDetailDTO getManySpecDetailById(String productId);

    /**
     * @Description 保存/修改产品sku信息表数据
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productSkuBaseInfoDTO 新增产品无规格sku信息请求参数
     * @return java.lang.String
     **/
    String saveOrUpdate(ProductSkuBaseInfoDTO productSkuBaseInfoDTO);

    /**
     * @Description 保存/修改产品sku信息表数据-批量
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productDetailList 新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdateBatch(List<ProductDetailDTO> productDetailList);

    /**
     * @Description 新增无规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/21 16:31
     * @param productNoSpecDTO:新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdateNoSpec(ProductNoSpecDTO productNoSpecDTO);

    /**
     * @Description 新增多规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:52
     * @param productManySpecDTO:新增产品多规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdateManySpec(ProductManySpecDTO productManySpecDTO);

    /**
     * @Description 多规格自动生成
     * @Author Luo_WG
     * @Date 2022/9/26 14:54
     * @param variantAutoAddDTO:自动生成请求参数
     * @return java.lang.Boolean
     **/
    List<ProductDetailEntity> insertManySpecAuto(VariantAutoAddDTO variantAutoAddDTO);

    /**
     * @Description 删除多规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 11:32
     * @param skuId:产品sku表主键id
     * @return java.lang.Boolean
     **/
    Boolean delete(String skuId);

    /**
     * @Description 根据产品id删除产品信息
     * @Author Luo_WG
     * @Date 2022/9/22 11:32
     * @param id:产品sku表主键id
     * @return java.lang.Boolean
     **/
    Boolean deleteByProductId(String id);

    /**
     * @Description 删除多规格sku信息-批量
     * @Author Luo_WG
     * @Date 2022/9/22 11:32
     * @param skuIds:产品sku表主键id
     * @return java.lang.Boolean
     **/
    Boolean deleteBatch(List<String> skuIds);

    /**
     * @Description 根据产品主键id查询sku明细
     * @Author Luo_WG
     * @Date 2022/9/26 18:25
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     **/
    List<ProductDetailEntity> queryByProductId(String productId);

    /**
     * @Description 检查sku是否重复-集合
     * @Author Luo_WG
     * @Date 2022/9/27 9:17
     * @param skuList:sku集合
     **/
    Boolean checkSkuNos(List<String> skuList);

    /**
     * @Description 检查sku是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:17
     * @param sku sku
     **/
    Boolean checkSkuNo(String sku, String id);

    /**
     * @Description 检查spu编号是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:28
     * @param spuNo spu编号
     * @param id 主键id
     * @return void
     **/
    Boolean checkSpuNo(String spuNo, String id);

    /**
     * @Description 检查spu编号是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:28
     * @param name spu编号
     * @param id 主键id
     * @return void
     **/
    Boolean checkName(String name, String id);


    /**
     * @Description 根据sku查询sku表信息
     * @Author Luo_WG
     * @Date 2022/9/28 17:04
     * @param sku：sku
     * @return com.erp.model.plm.entity.ProductDetailEntity
     **/
    ProductDetailEntity getProductIdBySku(String sku);
    
    /**
     * 方法说明
     * @author yl
     * @date 2022-11-21 17:13
     * @param productId
     * @return 
     */
    List<ProductDetailEntity> getSkuListByProductId(String productId);
    

    /**
     * @Description 新增无规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:55
     * @param productNoSpecDTO:新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    Boolean inportExcel(ProductNoSpecDTO productNoSpecDTO);

    /**
     * 导出excel的sku数据
     * @Author Luo_WG
     * @Date 2022/10/9 11:49
     * @param productSkuExcelDTO productSkuExcelDTO
     * @param response response
     * @return void
     **/
    void exportProduct(ProductSkuExcelDTO productSkuExcelDTO, HttpServletResponse response);

    /**
     * 根据sku id集合
     * @author yl
     * @date 2022-11-24 9:20
     * @param skuIdList
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     */
    List<ProductDetailEntity> getByIdList(List<String> skuIdList);

    List<String> getNotFinish(List<String> skuIdList);
}
