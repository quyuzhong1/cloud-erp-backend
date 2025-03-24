package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.CfgVatInvoiceDTO;
import com.erp.model.oms.entity.CfgVatInvoiceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * VAT发票设置 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2025-03-07
 */
@Mapper
public interface CfgVatInvoiceMapper extends BaseMapper<CfgVatInvoiceEntity> {

    IPage<CfgVatInvoiceDTO.PagingViewDTO> paging(@Param("query") Page<CfgVatInvoiceDTO.PagingViewDTO> query, @Param("params") CfgVatInvoiceDTO.PagingParamDTO params);
}
