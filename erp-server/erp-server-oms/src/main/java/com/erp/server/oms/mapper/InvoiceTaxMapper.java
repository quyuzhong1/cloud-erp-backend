package com.erp.server.oms.mapper;

import com.erp.model.oms.dto.InvoiceTaxDTO;
import com.erp.model.oms.entity.InvoiceTaxEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 发票税务信息 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-04-07
 */
@Mapper
public interface InvoiceTaxMapper extends BaseMapper<InvoiceTaxEntity> {

    List<InvoiceTaxDTO.UpdateDTO> invoiceAddressView(@Param("ids") List<String> ids);
}
