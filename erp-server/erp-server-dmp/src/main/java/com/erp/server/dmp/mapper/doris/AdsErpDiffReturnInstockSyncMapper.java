package com.erp.server.dmp.mapper.doris;
import java.util.List;

import com.erp.model.dmp.dto.AdsErpDiffOutstockSyncDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import com.erp.model.dmp.dto.AdsErpDiffReturnInstockSyncDTO;
import com.erp.model.dmp.entity.doris.AdsErpDiffOutstockSyncEntity;
import com.erp.model.dmp.entity.doris.AdsErpDiffReturnInstockSyncEntity;

/**
 * <p>
 * ERP退货入库单差异表 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2025-11-19
 */
@Mapper
public interface AdsErpDiffReturnInstockSyncMapper extends BaseMapper<AdsErpDiffReturnInstockSyncEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AdsErpDiffReturnInstockSyncDTO.ListDTO> paging(Page query, @Param("params") AdsErpDiffReturnInstockSyncDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AdsErpDiffReturnInstockSyncDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<AdsErpDiffReturnInstockSyncDTO.ListDTO> listExport(@Param("params") AdsErpDiffReturnInstockSyncDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<AdsErpDiffReturnInstockSyncDTO.TabListDTO> tabList(@Param("params") AdsErpDiffReturnInstockSyncDTO.PagingParamDTO searchParam);
    
    AdsErpDiffReturnInstockSyncDTO.TotalDTO total(@Param("params") AdsErpDiffReturnInstockSyncDTO.PagingParamDTO params);
    
    void updateDws(@Param("params") List<AdsErpDiffReturnInstockSyncEntity> params);

    void updateSuggestType(@Param("params") AdsErpDiffOutstockSyncDTO.UpdateSuggestTypeParamsDTO updateSuggestTypeParamsDTO);
}
