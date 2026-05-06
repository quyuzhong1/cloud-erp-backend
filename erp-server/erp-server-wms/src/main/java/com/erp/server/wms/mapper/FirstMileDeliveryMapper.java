package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
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
     * 查询用于报关中间表生成的装箱明细
     *
     * @param ids 头程发货单id集合
     * @return 装箱明细集合
     * @throws RuntimeException 查询异常时抛出
     * @author jack
     * @date 2026-04-29
     */
    List<WmsCartonDetailDTO.ListPackingDetailDTO> listDeclarePackingDetail(@Param("ids") List<String> ids);

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

    /**
     * 根据发货单获取业务单号
     * @param deliveryCodes
     * @return
     */
    List<FirstMileDeliveryDTO.BusinessDTO> getBusinessCodeByCodes(@Param("deliveryCodes") List<String> deliveryCodes);
    Page<WmsCartonDetailDTO.ListPackingDetailDTO> firstMilePackingTaskDetail(@Param("query") Page<PackingTaskDTO.ExportDTO> query,@Param("params") PackingTaskDTO.ExportDTO page, @Param("ids") List<String> ids, @Param("permissionSql") String permissionSql);

    List<FirstMileDeliveryDTO.BusinessDTO> getDeliveryCodeByBusinessCodes(@Param("businessCodes")List<String> businessCodes);

    List<FbaTransitCalculateReportDTO.DeliveryDTO> listDeliveryByReportMonth(@Param("approveStatus") String approveStatus, @Param("sourceType") String sourceType, @Param("reportMonth") LocalDate reportMonth, @Param("shipmentCode") String shipmentCode, @Param("asin") String asin, @Param("msku") String msku);

    List<OverseasProviderWarehouseDTO.ProviderDTO> listOverseasProvider(@Param("deliveryIds") List<String> deliveryIds);

    /**
     * 查询未生成报关单的头程发货明细信息
     * @author will
     * @date 2026/4/22 10:26
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.NotGenerateDetailDTO>
     */
    IPage<TmsDeclareBillDTO.NotGenerateDetailDTO> listNotGenerateDeclareFmDetail(Page query,@Param("params") TmsDeclareBillDTO.NotGenerateParamDTO params);

    /**
     * 取消分货分页查询
     * @author will
     * @date 2026/1/23 17:03
     * @param query
     * @param params
     * @return IPage<CancelDeliveryListDTO>
     */
    IPage<FirstMileDeliveryDTO.CancelDeliveryListDTO> cancelDeliveryPaging(Page<FirstMileDeliveryDTO.CancelDeliveryListDTO> query,@Param("params") FirstMileDeliveryDTO.CancelDeliveryParamDTO params);
    /**
     * 查询合并前数据
     * @author will
     * @date 2026/4/27 17:39
     * @param ids 头程发货单id集合
     * @param detailIds 头程发货单明细id集合
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.SourceDeliveryDetailDTO>
     */
    List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> listBeforePushFmDeclare(@Param("ids") List<String> ids, @Param("detailIds") List<String> detailIds);
}
