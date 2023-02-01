package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductDetailEntity;
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
    IPage<ProductDetailShowDTO> paging(Page query, @Param("params") ProductSkuDTO productSkuDTO);

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
     * @return java.util.List<com.erp.model.plm.dto.ExportSkuExcelDTO>
     **/
    List<ExportSkuExcelDTO> getExportSkuExcel(@Param("params") ProductSkuExcelDTO productSkuExcelDTO);

    List<BaseIdDTO> getNotFinish(@Param("skuIdList") List<String> skuIdList);

    /**
     * @Description 根据sku查询sku表信息(数据清洗)
     * @Author Luo_WG
     * @Date 2022/9/28 17:04
     * @param sku：sku
     * @return com.erp.model.plm.entity.ProductDetailEntity
     **/
    CleanSkuDto getProductIdBySkuClean(@Param("sku") String sku);

    List<SkuVO> getSkuBySkuNos(@Param("skuList") List<String> skuNoList);

    List<SkuVO> searchSku(@Param("searchKeyword") String searchKeyword);

    List<ChangeInfoDTO> searchStateSku(@Param("state") Integer state,@Param("searchKeyword") String searchKeyword);

    List<SkuVO> getSkuBySkuIds(@Param("skuIdList")List<String> skuIdList);
}


