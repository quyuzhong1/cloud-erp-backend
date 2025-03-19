package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.InvoiceInfoDTO;
import com.erp.model.oms.entity.InvoiceInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 上传记录 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2025-03-07
 */
@Mapper
public interface InvoiceInfoMapper extends BaseMapper<InvoiceInfoEntity> {

    IPage<InvoiceInfoDTO.PagingViewDTO> paging(@Param("query") Page<InvoiceInfoDTO.PagingViewDTO> query, @Param("params") InvoiceInfoDTO.PagingParamDTO params);
}
