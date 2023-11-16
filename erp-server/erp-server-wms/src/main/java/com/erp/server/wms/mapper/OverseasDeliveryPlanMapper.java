package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.OverseasDeliveryPlanEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.OverseasDeliveryPlanDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 发货计划 Mapper 接口
 * </p>
 *
 * @author Luo_wg
 * @since 2023-11-16
 */
@Mapper
public interface OverseasDeliveryPlanMapper extends BaseMapper<OverseasDeliveryPlanEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<OverseasDeliveryPlanDTO.ListDTO> paging(Page query, @Param("params") OverseasDeliveryPlanDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") OverseasDeliveryPlanDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<OverseasDeliveryPlanDTO.ListDTO> listExport(@Param("params") OverseasDeliveryPlanDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<OverseasDeliveryPlanDTO.TabListDTO> tabList(@Param("params") OverseasDeliveryPlanDTO.PagingParamDTO searchParam);
}
