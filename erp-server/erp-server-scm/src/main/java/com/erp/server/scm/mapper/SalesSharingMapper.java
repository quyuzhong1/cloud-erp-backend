package com.erp.server.scm.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.SalesSharingDTO;
import com.erp.model.scm.entity.SalesSharingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


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
}
