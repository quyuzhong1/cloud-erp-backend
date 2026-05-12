package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PermissionsDTO;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.model.wms.entity.WaveListEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WaveListMapper extends BaseMapper<WaveListEntity> {

    List<WaveListDTO.PickingWaveDetailDTO> listDetailByMainId(@Param("waveId")String waveId, @Param("basketNo") String basketNo, @Param("skuId") String skuId);

    IPage<WaveListEntity> paging(Page<Object> page, @Param("params") WaveListDTO.SearchParamDTO params);

    List<WaveListDTO.TabDTO> listTab(@Param("params") WaveListDTO.SearchParamDTO params);

    List<WaveListDTO.WaveDeliveryDTO> listByDeliverIds(@Param("ids") List<String> ids);

    List<WaveListDTO.WaveDeliveryStatusDTO> listDeliveryStatus(@Param("deliveryId") String deliveryId);
}
