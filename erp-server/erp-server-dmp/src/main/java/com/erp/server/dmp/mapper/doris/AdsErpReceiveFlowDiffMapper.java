package com.erp.server.dmp.mapper.doris;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import com.erp.model.dmp.dto.AdsErpReceiveFlowDiffDTO;
import com.erp.model.dmp.entity.doris.AdsErpReceiveFlowDiffEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
public interface AdsErpReceiveFlowDiffMapper extends BaseMapper<AdsErpReceiveFlowDiffEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AdsErpReceiveFlowDiffDTO.ListDTO> paging(Page query, @Param("params") AdsErpReceiveFlowDiffDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AdsErpReceiveFlowDiffDTO.PagingParamDTO params);

    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<AdsErpReceiveFlowDiffDTO.TabListDTO> tabList(@Param("params") AdsErpReceiveFlowDiffDTO.PagingParamDTO searchParam);
    /**
     * 获取列表统计
     * @param params
     * @return
     */
    AdsErpReceiveFlowDiffDTO.TotalDTO total(@Param("params") AdsErpReceiveFlowDiffDTO.PagingParamDTO params);
}
