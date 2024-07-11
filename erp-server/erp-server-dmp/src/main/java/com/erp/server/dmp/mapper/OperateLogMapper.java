package com.erp.server.dmp.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.dmp.entity.OperateLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.erp.model.dmp.dto.OperateLogDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 操作日志表 Mapper 接口
 * </p>
 *
 * @author hyj
 * @since 2024-05-22
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
