package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.ProductForbiddenWordCheckDTO;
import com.erp.model.plm.entity.ProductForbiddenWordCheckEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 产品违禁词检测记录 Mapper 接口
 * </p>
 */
@Mapper
public interface ProductForbiddenWordCheckMapper extends BaseMapper<ProductForbiddenWordCheckEntity> {

    IPage<ProductForbiddenWordCheckDTO.ListDTO> paging(Page query, @Param("params") ProductForbiddenWordCheckDTO.PagingParamDTO params);

    Integer nextSeq(@Param("reportDate") String reportDate);

    Integer countProductForDetect();

    List<ProductForbiddenWordCheckDTO.ProductScanDTO> listProductForDetect(@Param("limit") Integer limit, @Param("offset") Integer offset);
}
