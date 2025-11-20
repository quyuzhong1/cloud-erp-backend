package com.erp.server.dmp.mapper.doris;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.erp.model.dmp.entity.doris.AdsErpFirstMileInTransitDiffEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.AdsErpFirstMileInTransitDiffDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 平台在途报告 Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2025-11-13
 */
@Mapper
@DS("adsDoris")
public interface AdsErpFirstMileInTransitDiffMapper extends BaseMapper<AdsErpFirstMileInTransitDiffEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AdsErpFirstMileInTransitDiffDTO.ListDTO> paging(Page query, @Param("params") AdsErpFirstMileInTransitDiffDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AdsErpFirstMileInTransitDiffDTO.PagingParamDTO params);

}
