package com.erp.server.dmp.mapper.doris;
import com.erp.model.dmp.entity.doris.AdsErpDiffOutstockSyncEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.AdsErpDiffOutstockSyncDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * ERP出库单差异表 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2025-11-18
 */
@Mapper
public interface AdsErpDiffOutstockSyncMapper extends BaseMapper<AdsErpDiffOutstockSyncEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AdsErpDiffOutstockSyncDTO.ListDTO> paging(Page query, @Param("params") AdsErpDiffOutstockSyncDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AdsErpDiffOutstockSyncDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<AdsErpDiffOutstockSyncDTO.ListDTO> listExport(@Param("params") AdsErpDiffOutstockSyncDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<AdsErpDiffOutstockSyncDTO.TabListDTO> tabList(@Param("params") AdsErpDiffOutstockSyncDTO.PagingParamDTO searchParam);
}
