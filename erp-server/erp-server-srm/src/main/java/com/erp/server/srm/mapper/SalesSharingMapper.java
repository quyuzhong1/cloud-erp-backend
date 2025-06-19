package com.erp.server.srm.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.srm.dto.SalesSharingDTO;
import com.erp.model.srm.entity.SalesSharingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 销量共享表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-06-18
 */
@Mapper
public interface SalesSharingMapper extends BaseMapper<SalesSharingEntity> {

    IPage<SalesSharingDTO.ListDTO> paging(Page query,  @Param("params") SalesSharingDTO.PagingParamDTO params);

    List<SalesSharingDTO.ListDTO> listByParams(@Param("params") SalesSharingDTO.PagingParamDTO params);
}
