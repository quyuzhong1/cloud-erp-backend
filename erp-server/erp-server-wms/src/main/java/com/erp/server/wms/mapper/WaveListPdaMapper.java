package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.WaveListEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 波次列表（PDA）
 * @date 2024-07-01
 * @author tanmujin
 */
@Mapper
public interface WaveListPdaMapper extends BaseMapper<WaveListEntity> {
}
