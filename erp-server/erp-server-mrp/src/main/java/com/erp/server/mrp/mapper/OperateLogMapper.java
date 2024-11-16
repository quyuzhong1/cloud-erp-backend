package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.OperateLogDTO;
import com.erp.model.mrp.entity.OperateLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 操作日志表 Mapper 接口
 *
 * @author will
 * @since 2023-05-08
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
    IPage<OperateLogDTO.ListDTO> paging(Page<OperateLogDTO.ListDTO> query, @Param("params") OperateLogDTO.SearchDTO params);
}
