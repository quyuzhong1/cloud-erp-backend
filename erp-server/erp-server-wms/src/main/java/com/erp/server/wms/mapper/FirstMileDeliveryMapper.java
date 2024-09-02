package com.erp.server.wms.mapper;
import com.common.business.dto.AdvanceQueryContainer;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 头程发货单 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Mapper
public interface FirstMileDeliveryMapper extends BaseMapper<FirstMileDeliveryEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<FirstMileDeliveryDTO.ListDTO> paging(Page query, @Param("params") FirstMileDeliveryDTO.PagingParamDTO params);
    IPage<FirstMileDeliveryDTO.ListFirstMileDTO> pagingFirstMile(Page query, @Param("params") FirstMileDeliveryDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") FirstMileDeliveryDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<FirstMileDeliveryDTO.ListDTO> listExport(@Param("params") FirstMileDeliveryDTO.PagingParamDTO params);

    Page<FirstMileDeliveryDTO.ListDTO> listExport(@Param("page") Page<FirstMileDeliveryDTO.ListDTO> page, @Param("params") FirstMileDeliveryDTO.PagingParamDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<FirstMileDeliveryDTO.TabListDTO> tabList(@Param("params") FirstMileDeliveryDTO.PagingParamDTO searchParam);

    /**
     * 根据来源单号查询发货记录
     * @Author Luo_WG
     * @Date 2023/11/1 18:07
     * @param sourceIds
     * @return java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.DeliverRecordView>
     **/
    List<FirstMileDeliveryDTO.DeliverRecordView> listDeliveryRecord(@Param("sourceIds") List<String> sourceIds, @Param("fbaShipmentCode") String fbaShipmentCode);

    /**
     * 下推加工单列表查询
     * @Author Luo_WG
     * @Date 2023/11/6 11:19
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.FbaDeliveryDTO.GenerateMachineView>
     **/
    List<FirstMileDeliveryDTO.GenerateMachineView> generateMachineView(@Param("ids") List<String> ids);

    /**
     * 根据发货单id查询装箱清单
     * @Author Luo_WG
     * @Date 2023/11/28 17:15
     * @return java.util.List<com.erp.model.wms.dto.FirstMileCartonDetailDTO.ListPackingDetailDTO>
     **/
    List<WmsCartonDetailDTO.ListPackingDetailDTO> listPackingDetail(@Param("ids") List<String> ids);

    /**
     * 导出装箱清单Excel
     * @Author Luo_WG
     * @Date 2023/11/29 12:17
     * @param params
     * @return java.util.List<com.erp.model.wms.dto.FirstMileCartonDTO.ExportPackingDTO>
     **/
    List<WmsCartonSpecDTO.ExportPackingDTO> exportPacking(@Param("params") FirstMileDeliveryDTO.ExportDTO params);

    List<FirstMileDeliveryDTO.GenerateLogisticDTO> getGenerateLogisticDTO(@Param("params") FirstMileDeliveryDTO.GenerateLogisticReqDTO dto);

    List<FirstMileDeliveryDTO.LogisticStatisticsDTO> logisticStatistics(FirstMileDeliveryDTO.StatisticsReq dto);

    List<FirstMileDeliveryEntity> advanceQuery(@Param("params") AdvanceQueryContainer advanceQueryContainer);

    List<TmsDeclareBillDTO.DeliveryDTO> getGenerateDeclare(@Param("params") TmsDeclareBillDTO.QuerySourceDTO dto);

    /**
     * 根据编码获取发货明细列表
     * @param codes
     * @return
     */
    List<FirstMileDeliveryDTO.ListFirstMileDTO> listDetailByCodes(@Param("codes") List<String> codes,@Param("sourceCodes") List<String> sourceCodes);

    /**
     * 根据发货单获取业务单号
     * @param ids
     * @return
     */
    List<FirstMileDeliveryDTO.BusinessDTO> getBusinessCodeByIds(@Param("ids") List<String> ids);
}
