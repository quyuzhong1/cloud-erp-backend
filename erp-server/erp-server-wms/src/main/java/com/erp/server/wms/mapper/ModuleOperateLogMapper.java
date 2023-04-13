package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.ModuleOperateLogDTO;
import com.erp.model.wms.entity.ModuleOperateLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 日志表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-03-17
 */
@Mapper
public interface ModuleOperateLogMapper extends BaseMapper<ModuleOperateLogEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/20 15:34
     * @param query
     * @param params
     * @return IPage<listDTO>
     */
    IPage<ModuleOperateLogDTO.ListDTO> paging(Page query, ModuleOperateLogDTO.SearchDTO params);
}
