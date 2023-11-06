package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.entity.FbaDeliveryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.FbaDeliveryDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * FBI发货单 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Mapper
public interface FbaDeliveryMapper extends BaseMapper<FbaDeliveryEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<FbaDeliveryDTO.ListDTO> paging(Page query, @Param("params") FbaDeliveryDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") FbaDeliveryDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<FbaDeliveryDTO.ListDTO> listExport(@Param("params") FbaDeliveryDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<FbaDeliveryDTO.TabListDTO> tabList(@Param("params") FbaDeliveryDTO.PagingParamDTO searchParam);

    /**
     * 根据来源单号查询发货记录
     * @Author Luo_WG
     * @Date 2023/11/1 18:07
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.DeliverRecordView>
     **/
    List<FbaShipmentDTO.DeliverRecordView> listDeliveryRecordBySourceIds(@Param("ids") List<String> ids);

    /**
     * 下推加工单列表查询
     * @Author Luo_WG
     * @Date 2023/11/6 11:19
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.FbaDeliveryDTO.GenerateMachineView>
     **/
    List<FbaDeliveryDTO.GenerateMachineView> generateMachineView(List<String> ids);
}
