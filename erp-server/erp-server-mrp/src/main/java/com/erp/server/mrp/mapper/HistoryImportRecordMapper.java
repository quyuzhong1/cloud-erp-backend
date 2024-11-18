package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.HistoryImportRecordDTO;
import com.erp.model.mrp.entity.HistoryImportRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 历史导入记录 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-08-27
 */
@Mapper
public interface HistoryImportRecordMapper extends BaseMapper<HistoryImportRecordEntity> {
    /**
     * 分页查询
     * @author will
     * @date 2024/8/29 10:07
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<HistoryImportRecordDTO.ListDTO> paging(Page<HistoryImportRecordDTO.ListDTO> query,@Param("params") HistoryImportRecordDTO.PagingParamDTO params);
}
