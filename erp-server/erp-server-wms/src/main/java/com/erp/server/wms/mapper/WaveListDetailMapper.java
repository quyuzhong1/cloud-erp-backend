package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.WaveListDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WaveListDetailMapper extends BaseMapper<WaveListDetailEntity> {
    List<WaveListDetailEntity> listByWaveListCode(@Param("deliveryIds") List<String> deliveryIds, @Param("code") String... code);

}
