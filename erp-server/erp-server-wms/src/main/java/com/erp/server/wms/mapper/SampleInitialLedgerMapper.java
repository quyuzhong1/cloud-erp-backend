package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.SampleInitialLedgerEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.SampleInitialLedgerDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 样品期初台账 Mapper 接口
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Mapper
public interface SampleInitialLedgerMapper extends BaseMapper<SampleInitialLedgerEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SampleInitialLedgerDTO.ListDTO> paging(Page query, @Param("params") SampleInitialLedgerDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SampleInitialLedgerDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SampleInitialLedgerDTO.ListDTO> listExport(@Param("params") SampleInitialLedgerDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SampleInitialLedgerDTO.TabListDTO> tabList(@Param("params") SampleInitialLedgerDTO.PagingParamDTO searchParam);
}
