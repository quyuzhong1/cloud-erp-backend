package com.erp.server.oms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;


/**
 * <p>
 * B2C销售订单买家信息表 Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Mapper
public interface SoB2cReceiverMapper extends BaseMapper<SoB2cReceiverEntity> {

    IPage<SoB2cReceiverEntity> pagePartitionIsNull(Page query);
}
