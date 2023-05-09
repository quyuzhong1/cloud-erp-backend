package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.OperateLogDTO;
import com.erp.model.wms.entity.OperateLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 日志表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-03-17
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
    IPage<OperateLogDTO.ListDTO> paging(Page query,@Param("params") OperateLogDTO.SearchDTO params);
}
