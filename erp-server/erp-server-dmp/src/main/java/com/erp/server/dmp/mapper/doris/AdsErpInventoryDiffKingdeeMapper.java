package com.erp.server.dmp.mapper.doris;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.erp.model.dmp.entity.doris.AdsErpInventoryDiffKingdeeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.AdsErpInventoryDiffKingdeeDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 金蝶库存差异 Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2025-11-13
 */
@Mapper
@DS("adsDoris")
public interface AdsErpInventoryDiffKingdeeMapper extends BaseMapper<AdsErpInventoryDiffKingdeeEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AdsErpInventoryDiffKingdeeDTO.ListDTO> paging(Page query, @Param("params") AdsErpInventoryDiffKingdeeDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AdsErpInventoryDiffKingdeeDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<AdsErpInventoryDiffKingdeeDTO.ListDTO> listExport(@Param("params") AdsErpInventoryDiffKingdeeDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<AdsErpInventoryDiffKingdeeDTO.StatisticsDTO> statistics(@Param("params") AdsErpInventoryDiffKingdeeDTO.PagingParamDTO searchParam);
}
