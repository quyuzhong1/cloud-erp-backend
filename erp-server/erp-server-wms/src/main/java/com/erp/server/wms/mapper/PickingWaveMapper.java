package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.renovation.PickingWaveDTO;
import com.erp.model.wms.entity.PickingWaveEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PickingWaveMapper extends BaseMapper<PickingWaveEntity> {
    List<String> listDeliveryIdByStatus(@Param("status") String status);

    List<PickingWaveDTO.PickingWaveDetailDTO> listDetailByMainId(@Param("waveId")String waveId,@Param("basketNo") String basketNo, @Param("skuId") String skuId);
}
