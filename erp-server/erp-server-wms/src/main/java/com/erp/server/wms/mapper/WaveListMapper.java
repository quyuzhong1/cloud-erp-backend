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
    List<String> listDeliveryIdByStatus(@Param("status") String status);

    List<WaveListDTO.PickingWaveDetailDTO> listDetailByMainId(@Param("waveId")String waveId, @Param("basketNo") String basketNo, @Param("skuId") String skuId);

    int countDelivery( @Param("params") PermissionsDTO params);

    IPage<WaveListEntity> paging(Page<Object> page, @Param("params") WaveListDTO.SearchParamDTO params);

    List<WaveListDTO.TabDTO> listTab();

    List<WaveListDTO.WaveDeliveryDTO> listByDeliverIds(@Param("ids") List<String> ids);

    List<String> listDeliveryIdBySql(@Param("sql") String compareCodeSplicingValueSql);

    List<WaveListDTO.WaveDeliveryStatusDTO> listDeliveryStatus(@Param("deliveryId") String deliveryId);
}
