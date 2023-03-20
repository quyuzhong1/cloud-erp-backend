package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.ModuleOperateLogDTO;
import com.erp.model.scm.entity.ModuleOperateLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
    IPage<ModuleOperateLogDTO.listDTO> paging(Page query, ModuleOperateLogDTO.searchDTO params);
}
