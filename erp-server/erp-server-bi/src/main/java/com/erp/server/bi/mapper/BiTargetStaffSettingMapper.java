package com.erp.server.bi.mapper;
import com.erp.model.bi.dto.BiTargetStaffSettingDTO;
import com.erp.model.bi.entity.BiTargetStaffSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 人员目标设置表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Mapper
public interface BiTargetStaffSettingMapper extends BaseMapper<BiTargetStaffSettingEntity> {

    List<BiTargetStaffSettingDTO.ListDetailDTO> listByYear(@Param("year") String year);
}
