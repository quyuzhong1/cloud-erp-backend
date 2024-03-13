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
 * @author Luo_WG
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
    List<OverseasDeliveryPlanDTO.ListDTO> listExport(@Param("params") OverseasDeliveryPlanDTO.PagingParamDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<OverseasDeliveryPlanDTO.TabListDTO> tabList(@Param("params") OverseasDeliveryPlanDTO.PagingParamDTO searchParam);

    /**
     * 下推要货申请列表查询
     * @Author Luo_WG
     * @Date 2023/11/17 16:19
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO>
     **/
    List<OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> generateRequisitionApplicationView(@Param("ids") List<String> ids);

    /**
     * 下推发货单列表查询
     * @Author Luo_WG
     * @Date 2023/11/20 10:17
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.OverseasDeliveryPlanDTO.GenerateDeliverViewDTO>
     **/
    List<OverseasDeliveryPlanDTO.GenerateDeliverViewDTO> generateDeliverView(@Param("ids") List<String> ids);
}
