package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.CfgRuleInvoiceDTO;
import com.erp.model.oms.entity.CfgRuleInvoiceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.ss.formula.functions.T;


/**
 * <p>
 * 开票规则 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2025-05-23
 */
@Mapper
public interface CfgRuleInvoiceMapper extends BaseMapper<CfgRuleInvoiceEntity> {

    IPage<CfgRuleInvoiceDTO.PagingViewDTO> paging(@Param("query") Page<T> query, @Param("params") CfgRuleInvoiceDTO.PagingParamDTO params);
}
