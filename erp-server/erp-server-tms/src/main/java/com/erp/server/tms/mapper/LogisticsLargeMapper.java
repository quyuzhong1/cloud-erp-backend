package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.LogisticsLargeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 物流大表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2024-11-29
 */
@Mapper
public interface LogisticsLargeMapper extends BaseMapper<LogisticsLargeEntity> {


    void listLargeDataById(@Param("ids") List<String> ids);

}
