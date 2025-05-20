package com.erp.server.workflow.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.workflow.dto.OperateLogDTO;
import com.erp.model.workflow.entity.OperateLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 操作日志表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-05-12
 */
@Mapper
public interface OperateLogMapper extends BaseMapper<OperateLogEntity> {

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/20 15:34
     * @param query
     * @param params
     * @return IPage<listDTO>
     */
    IPage<OperateLogDTO.ListDTO> paging(Page query, @Param("params") OperateLogDTO.SearchDTO params);
}
