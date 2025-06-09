package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.PickingDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 拣货明细 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-05-11
 */
@Mapper
public interface PickingDetailMapper extends BaseMapper<PickingDetailEntity> {
    int updatePickedQtyByWaveIds(@Param("waveIds") List<String> waveIds);
}
