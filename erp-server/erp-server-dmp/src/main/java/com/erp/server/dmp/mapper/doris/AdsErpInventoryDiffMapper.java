package com.erp.server.dmp.mapper.doris;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.erp.model.dmp.entity.doris.AdsErpInventoryDiffEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.AdsErpInventoryDiffDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 平台库存差异 Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2025-11-13
 */
@Mapper
@DS("adsDoris")
public interface AdsErpInventoryDiffMapper extends BaseMapper<AdsErpInventoryDiffEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AdsErpInventoryDiffDTO.ListDTO> paging(Page query, @Param("params") AdsErpInventoryDiffDTO.PagingParamDTO params);


    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<AdsErpInventoryDiffDTO.ListDTO> listExport(@Param("params") AdsErpInventoryDiffDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param params
    * @return
    */
    List<AdsErpInventoryDiffDTO.StatisticsDTO> statistics(@Param("params") AdsErpInventoryDiffDTO.PagingParamDTO params);
}
