package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.InvoiceUploadDTO;
import com.erp.model.oms.entity.InvoiceUploadEntity;
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
public interface InvoiceUploadMapper extends BaseMapper<InvoiceUploadEntity> {

    IPage<InvoiceUploadDTO.PagingViewDTO> paging(@Param("query") Page<InvoiceUploadDTO.PagingViewDTO> query, @Param("params") InvoiceUploadDTO.PagingParamDTO params);
}
