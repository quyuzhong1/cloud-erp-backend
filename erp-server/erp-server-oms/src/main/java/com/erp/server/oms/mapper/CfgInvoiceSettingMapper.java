package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.CfgInvoiceSettingDTO;
import com.erp.model.oms.entity.CfgInvoiceSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2025-04-07
 */
@Mapper
public interface CfgInvoiceSettingMapper extends BaseMapper<CfgInvoiceSettingEntity> {
    IPage<CfgInvoiceSettingDTO.PagingViewDTO> paging(@Param("query") Page<CfgInvoiceSettingDTO.PagingViewDTO> query, @Param("params") CfgInvoiceSettingDTO.PagingParamDTO params);
}
