package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.SampleLedgerFlowDTO;
import com.erp.model.wms.entity.SampleLedgerFlowEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 样品台账流水 Mapper 接口
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Mapper
public interface SampleLedgerFlowMapper extends BaseMapper<SampleLedgerFlowEntity> {

    /**
     * 分页查询
     * @param query 分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    IPage<SampleLedgerFlowDTO.ListDTO> paging(Page query, @Param("params") SampleLedgerFlowDTO.PagingParamDTO params);

    /**
     * 分页导出Excel查询
     * @param query 分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    IPage<SampleLedgerFlowDTO.ListDTO> listExport(Page query, @Param("params") SampleLedgerFlowDTO.ExportDTO params);
}
