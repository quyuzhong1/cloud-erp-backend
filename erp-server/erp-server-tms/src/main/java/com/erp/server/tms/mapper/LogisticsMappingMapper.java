package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.LogisticsMappingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 物流渠道映射表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Mapper
public interface LogisticsMappingMapper extends BaseMapper<LogisticsMappingEntity> {

    LogisticsSaleChannelEntity getBySalesPlatform(@Param("salesPlatform") String salesPlatform, @Param("channelId")String channelId);
}
