package com.erp.server.dmp.mapper;
import com.erp.model.dmp.dto.AfterSaleProgressDTO;
import com.erp.model.dmp.dto.excel.DmpAfterSaleExcelDTO;
import com.erp.model.dmp.entity.AfterSaleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.AfterSaleDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 售后申请表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-04-06
 */
@Mapper
public interface AfterSaleMapper extends BaseMapper<AfterSaleEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AfterSaleDTO.ListDTO> paging(Page query, @Param("params") AfterSaleDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AfterSaleDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    IPage<DmpAfterSaleExcelDTO> listExport(Page query, @Param("params") AfterSaleDTO.PagingParamDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<AfterSaleDTO.TabListDTO> tabList(@Param("params") AfterSaleDTO.PagingParamDTO searchParam);

    List<AfterSaleProgressDTO.RepairRecordListDTO> getRepairProgress(@Param("params")AfterSaleDTO.ProgressDTO params);

    List<AfterSaleProgressDTO.RepairHistoryListDTO> getRepairHistory(@Param("params")AfterSaleDTO.ThridUserDTO params);
}
