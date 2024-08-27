package com.erp.server.srm.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;


/**
 * <p>
 * 采购对账单明细 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-01-19
 */
@Mapper
public interface PoReconciliationDetailMapper extends BaseMapper<PoReconciliationDetailEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2024/1/20 11:34
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<PoReconciliationDetailDTO.ListDTO> paging(Page query,@Param("params") PoReconciliationDetailDTO.PagingParamDTO params);
    /**
     * @description: 查询导出数据
     * @author Will
     * @date: 2024/1/20 12:04
     * @param params
     * @return List<ListDTO>
     */
    List<PoReconciliationDetailDTO.ListDTO> listExport(@Param("params") PoReconciliationDetailDTO.PagingParamDTO params);
    Page<PoReconciliationDetailDTO.ListDTO> listExport(@Param("page") Page<PoReconciliationDetailDTO.ListDTO> page, @Param("params") PoReconciliationDetailDTO.PagingParamDTO params);
    /**
     * @description: 根据对账单查询对账明细详情
     * @author Will
     * @date: 2024/1/23 15:51
     * @param params
     * @return List<ListDTO>
     */
    List<PoReconciliationDetailDTO.ListDTO> listDetail(@Param("params") PoReconciliationDetailDTO.PagingParamDTO params);
    /**
     * @description: 查询可自动生成对账的数据
     * @author Will
     * @date: 2024/2/2 14:48
     * @param startDate
     * @param endDate
     * @return List<PoReconciliationDetailEntity>
     */
    List<PoReconciliationDetailEntity> listAutoGeneratePoReconciliation(@Param("startDate")LocalDate startDate,@Param("endDate") LocalDate endDate);
}
