package com.erp.server.oms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.entity.SoB2cExtendEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;


/**
 * <p>
 * 销售订单-tiktok全托管属性表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2025-03-24
 */
@Mapper
public interface SoB2cExtendMapper extends BaseMapper<SoB2cExtendEntity> {
    /**
     * 获取无军区的订单
     * @param query
     * @return
     */
    IPage<SoB2cExtendEntity> pagePartitionIsNull(Page query);
}
