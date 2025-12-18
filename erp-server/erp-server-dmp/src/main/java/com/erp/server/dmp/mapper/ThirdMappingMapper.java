package com.erp.server.dmp.mapper;

import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 第三方系统映射关系表 Mapper 接口
 * </p>
 *
 * @author hyj
 * @since 2024-05-17
 */
@Mapper
public interface ThirdMappingMapper extends BaseMapper<ThirdMappingEntity> {

    List<ThirdMappingDTO.ThirdAddDTO> getByThirdSysCode(@Param("thirdSysType") String thirdSysType, @Param("type") String type);

    List<ThirdMappingDTO.WarehouseMappingDTO> listMappingBySysIds(@Param("warehouseIdList") List<String> warehouseIdList, @Param("sysType") String sysType);

    ThirdMappingEntity getShopByThirdCode(@Param("thirdCode") String thirdCode,@Param("sysType") String sysType);
}
