package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import com.erp.model.wms.dto.WmsDeliveryPlanDTO;
import com.erp.model.wms.entity.WmsDeliveryPlanEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
public interface WmsDeliveryPlanMapper extends BaseMapper<WmsDeliveryPlanEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<WmsDeliveryPlanDTO.ListDTO> paging(Page query, @Param("params") WmsDeliveryPlanDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") WmsDeliveryPlanDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<WmsDeliveryPlanDTO.ListDTO> listExport(@Param("params") WmsDeliveryPlanDTO.PagingParamDTO params);
    Page<WmsDeliveryPlanDTO.ListDTO> listExport(@Param("page") Page<WmsDeliveryPlanDTO.ListDTO> page, @Param("params") WmsDeliveryPlanDTO.PagingParamDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<WmsDeliveryPlanDTO.TabListDTO> tabList(@Param("params") WmsDeliveryPlanDTO.PagingParamDTO searchParam);

    /**
     * 下推要货申请列表查询
     * @Author Luo_WG
     * @Date 2023/11/17 16:19
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO>
     **/
    List<WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> generateRequisitionApplicationView(@Param("ids") List<String> ids);

    /**
     * 下推发货单列表查询
     * @Author Luo_WG
     * @Date 2023/11/20 10:17
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.OverseasDeliveryPlanDTO.GenerateDeliverViewDTO>
     **/
    List<WmsDeliveryPlanDTO.GenerateDeliverViewDTO> generateDeliverView(@Param("ids") List<String> ids);
}
