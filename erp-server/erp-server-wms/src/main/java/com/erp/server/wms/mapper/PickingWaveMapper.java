package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.PickingWaveEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PickingWaveMapper extends BaseMapper<PickingWaveEntity> {
    List<String> listDeliveryIdByStatus(@Param("status") String status);
}
