package com.erp.server.oms.mapper;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * B2C销售订单明细表 Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Mapper
public interface SoB2cDetailMapper extends BaseMapper<SoB2cDetailEntity> {

    List<SoB2cDetailDTO.OutstockDTO> listOutstockByMainId(@Param("mainId") String mainId);
}
