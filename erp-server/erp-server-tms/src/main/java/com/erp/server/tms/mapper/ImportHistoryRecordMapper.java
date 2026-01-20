package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.model.tms.entity.ImportHistoryRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 物流授权表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2026-01-19
 */
@Mapper
public interface ImportHistoryRecordMapper extends BaseMapper<ImportHistoryRecordEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<ImportHistoryRecordDTO.ListDTO> paging(Page query, @Param("params") ImportHistoryRecordDTO.PagingParamDTO params);
}
