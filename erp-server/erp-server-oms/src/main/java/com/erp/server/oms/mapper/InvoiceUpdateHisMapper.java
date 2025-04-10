package com.erp.server.oms.mapper;
import com.erp.model.oms.entity.InvoiceUpdateHisEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 发票更新历史 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-04-07
 */
@Mapper
public interface InvoiceUpdateHisMapper extends BaseMapper<InvoiceUpdateHisEntity> {
    /**
     * 根据发票id查询修改的历史数量
     * @author will
     * @date 2025/4/9 15:00
     * @param invoiceInfoId
     * @return Integer
     */
    Integer countByInvoiceInfoId(@Param("invoiceInfoId") String invoiceInfoId);
}
