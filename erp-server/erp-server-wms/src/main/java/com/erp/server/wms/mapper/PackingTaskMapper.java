package com.erp.server.wms.mapper;

import com.erp.model.wms.dto.PackingTaskDTO;
import com.erp.model.wms.entity.PackingTaskEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 装箱任务表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-07-02
 */
@Mapper
public interface PackingTaskMapper extends BaseMapper<PackingTaskEntity> {
    /**
     * 根据类型进行汇总
     * @param permissionSql
     * @return
     */
    List<PackingTaskDTO.TypeCountDTO> listTabCount(@Param("permissionSql") String permissionSql);
}
