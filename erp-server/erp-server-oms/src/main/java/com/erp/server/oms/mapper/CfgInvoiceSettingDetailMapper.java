package com.erp.server.oms.mapper;

import com.erp.model.oms.dto.CfgInvoiceSettingDetailDTO;
import com.erp.model.oms.entity.CfgInvoiceSettingDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 发票设置明细 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2025-04-07
 */
@Mapper
public interface CfgInvoiceSettingDetailMapper extends BaseMapper<CfgInvoiceSettingDetailEntity> {
    List<CfgInvoiceSettingDetailDTO.ViewDetailShop> selectDetailShop();
    List<CfgInvoiceSettingDetailDTO.ViewDTO> selectDetailDict(String mianId);
}
