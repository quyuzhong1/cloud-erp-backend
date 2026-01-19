package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.ImportHistoryRecordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

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

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") ImportHistoryRecordDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<ImportHistoryRecordDTO.ListDTO> listExport(@Param("params") ImportHistoryRecordDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<ImportHistoryRecordDTO.TabListDTO> tabList(@Param("params") ImportHistoryRecordDTO.PagingParamDTO searchParam);
}
