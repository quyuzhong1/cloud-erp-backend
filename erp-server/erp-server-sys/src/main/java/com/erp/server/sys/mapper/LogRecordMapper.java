package com.erp.server.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.SysLogRecordDTO;
import com.erp.model.sys.entity.SysLogRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 操作日志 Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2023-08-25
 */
@Mapper
public interface LogRecordMapper extends BaseMapper<SysLogRecordEntity> {

    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<SysLogRecordDTO.ListDTO> paging(Page<?> query, @Param("params") SysLogRecordDTO.PagingParamDTO params);

}
