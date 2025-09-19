package com.erp.server.oms.mapper;
import com.erp.model.oms.entity.SoReceiptDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;


/**
 * <p>
 * 收款单明细 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2025-08-28
 */
@Mapper
public interface SoReceiptDetailMapper extends BaseMapper<SoReceiptDetailEntity> {

    List<String> existPaymentNo(Set<String> paymentNoSet,String id);
}
