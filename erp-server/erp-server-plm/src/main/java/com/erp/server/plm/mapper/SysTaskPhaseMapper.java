package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.TaskPhaseDTO;
import com.erp.model.plm.entity.SysTaskPhaseEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Classname SysTaskPhaseMapper
 * @Description TODO
 * @Date 2022-09-13 16:36
 * @Created by yl
 */
@Mapper
public interface SysTaskPhaseMapper extends BaseMapper<SysTaskPhaseEntity> {
    List<TaskPhaseDTO> getSysTaskPhase();
}
