package com.erp.server.dmp.mapper;
import com.erp.model.dmp.dto.DmpThirdCityDTO;
import com.erp.model.dmp.entity.DmpThirdCityEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 第三方城市字典表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-12-17
 */
@Mapper
public interface DmpThirdCityMapper extends BaseMapper<DmpThirdCityEntity> {

    List<DmpThirdCityDTO.ThirdAddressMappingDTO> getThirdByAddress(@Param("params") DmpThirdCityDTO.SysAddressParamsDTO params);
}
