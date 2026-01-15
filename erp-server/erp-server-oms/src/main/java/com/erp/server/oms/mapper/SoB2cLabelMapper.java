package com.erp.server.oms.mapper;
import com.erp.model.oms.entity.SoB2cLabelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;


/**
 * <p>
 * 订单标签，面单表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2024-04-18
 */
@Mapper
public interface SoB2cLabelMapper extends BaseMapper<SoB2cLabelEntity> {

    List<String> getNotLabel(@Param("startTime")LocalDateTime startTime, @Param("endTime")LocalDateTime endTime);
}
