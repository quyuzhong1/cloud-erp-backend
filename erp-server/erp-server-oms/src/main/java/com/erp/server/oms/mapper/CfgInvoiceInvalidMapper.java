package com.erp.server.oms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.CfgInvoiceInvalidDTO;
import com.erp.model.oms.dto.CfgInvoiceSettingDTO;
import com.erp.model.oms.entity.CfgInvoiceInvalidEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;


/**
 * <p>
 * 作废发票号 Mapper 接口
 * </p>
 *
 * @author hcg
 * @since 2025-04-14
 */
@Mapper
public interface CfgInvoiceInvalidMapper extends BaseMapper<CfgInvoiceInvalidEntity> {

    IPage<CfgInvoiceInvalidDTO.PagingViewDTO> paging(Page<CfgInvoiceSettingDTO.PagingViewDTO> query, CfgInvoiceInvalidDTO.PagingParamDTO params);
}
