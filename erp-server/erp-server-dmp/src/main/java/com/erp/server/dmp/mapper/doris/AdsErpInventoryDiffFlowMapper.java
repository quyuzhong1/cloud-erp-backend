package com.erp.server.dmp.mapper.doris;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import com.erp.model.dmp.dto.AdsErpInventoryDiffFlowDTO;
import com.erp.model.dmp.dto.excel.PlatformInitStockExcelDTO;
import com.erp.model.dmp.entity.doris.AdsErpInventoryDiffFlowEntity;

/**
 * <p>
 * 第三方仓流水差异表 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2025-11-14
 */
@Mapper
public interface AdsErpInventoryDiffFlowMapper extends BaseMapper<AdsErpInventoryDiffFlowEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AdsErpInventoryDiffFlowDTO.ListDTO> paging(Page query, @Param("params") AdsErpInventoryDiffFlowDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AdsErpInventoryDiffFlowDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<AdsErpInventoryDiffFlowDTO.ListDTO> listExport(@Param("params") AdsErpInventoryDiffFlowDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<AdsErpInventoryDiffFlowDTO.TabListDTO> tabList(@Param("params") AdsErpInventoryDiffFlowDTO.PagingParamDTO searchParam);
    
    AdsErpInventoryDiffFlowDTO.TotalDTO total(@Param("params") AdsErpInventoryDiffFlowDTO.PagingParamDTO params);
    
    List<PlatformInitStockExcelDTO> listInit(@Param("warehouseNameList") List<String> warehouseNameList , @Param("checkMonthList") List<String> checkMonthList);
    
    void batchInsertInit(@Param("dtoList") List<PlatformInitStockExcelDTO> dtoList);
}
