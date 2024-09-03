package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.dto.FbaShipmentPackingDTO;
import com.erp.model.wms.entity.FbaShipmentPackingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * fba货件装箱信息 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-09-03
 */
@Mapper
public interface FbaShipmentPackingMapper extends BaseMapper<FbaShipmentPackingEntity> {

    List<FbaShipmentPackingDTO.ViewDTO> getPacking(List<String> ids);

    Page<FbaShipmentPackingDTO.ViewDTO> exportFbaShipmentPacking(@Param("page") Page<FbaShipmentDTO.ListDTO> page, @Param("params") FbaShipmentDTO.PagingParamDTO dto);
}
