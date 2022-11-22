package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.*;
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

    IPage<ProductShowDTO> paging(Page query, @Param("params") ProductSearchDTO params,@Param("archiveProductIds") List<String> archiveProductIds);

    List<ProductExcelDTO> getExportProduct(@Param("productIds") List<String> productIds);

    List<CountDTO> getProductRelevanceList();

    IPage<ProductShowDTO> myCollectPaging(Page query, @Param("params") ProductSearchDTO params, @Param("productIds") List<String> productIds,@Param("archiveProductIds") List<String> archiveProductIds);

    List<ProductProjectDTO> getProductAndProjectList();

    ProductShowDTO getProductInfo(@Param("productId") String productId);

    List<ProductShowDTO> getProductInfoByIds(@Param("productIds") List<String> productIds);
}
