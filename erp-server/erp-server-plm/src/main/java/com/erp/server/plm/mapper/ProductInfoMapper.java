package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.CountDTO;
import com.erp.model.plm.dto.ProductExcelDTO;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.model.plm.dto.ProductShowDTO;
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

    IPage<ProductShowDTO> paging(IPage query, @Param("params") ProductSearchDTO params);

    List<ProductExcelDTO> getExportProduct(@Param("productIds") List<String> productIds);

    List<CountDTO> getProductRelevanceList();
}
