package com.erp.server.wms.service;

import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.PackingTaskEntity;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 头程发货单 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
public interface FirstMileDeliveryService extends SuperService<FirstMileDeliveryEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FirstMileDeliveryDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    Boolean update(FirstMileDeliveryDTO.UpdateDTO dto);

      /**
      * 分页列表查询
      * @author Luo_WG
      * @date: 2023-10-30
      * @param pagingParamDTO
      * @return PagingVO<FbaDeliveryDTO.ListDTO>>
      */
      PagingVO<FirstMileDeliveryDTO.ListDTO> paging(PagingDTO<FirstMileDeliveryDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 期初明细分页列表
     * @param pagingParamDTO
     * @return
     */
      PagingVO<FirstMileDeliveryDTO.ListFirstMileDTO> pagingFirstMile(PagingDTO<FirstMileDeliveryDTO.PagingParamDTO> pagingParamDTO);

     /**
     * 状态统计
     * @author Luo_WG
     * @date: 2023-10-30
     * @param dto
     * @return List<FbaDeliveryDTO.TabListDTO>>
     */
     List<FirstMileDeliveryDTO.TabListDTO> tabList(PermissionsDTO dto);

     /**
     * 详情
     * @author Luo_WG
     * @date: 2023-10-30
     * @param id
     * @return
     */
     FirstMileDeliveryDTO.ViewDTO view(String id);

     /**
     * 新增并提交审核
     * @author Luo_WG
     * @date: 2023-10-30
     * @param dto
     * @return
     */
     BaseResultDTO.AddDTO addAndSubmit(FirstMileDeliveryDTO.AddDTO dto);

     /**
     * 修改并提交审核
     * @author Luo_WG
     * @date: 2023-10-30
     * @param dto
     * @return
     */
     void updateAndSubmit(FirstMileDeliveryDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author Luo_WG
     * @date: 2023-10-30
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author Luo_WG
    * @date: 2023-10-30
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
     * 删除
     *
     * @param entity
     * @param packingTask
     * @return
     * @author Luo_WG
     * @date: 2023-10-30
     */
    BatchResultDTO delete(FirstMileDeliveryEntity entity, PackingTaskEntity packingTask);
    /**
     * 作废
     *
     * @param entity
     * @param remark
     * @param packingTask
     * @return
     * @author Luo_WG
     * @date: 2023-10-30
     */
    BatchResultDTO invalid(FirstMileDeliveryEntity entity, String remark, PackingTaskEntity packingTask);

    /**
    * 撤销
    * @author Luo_WG
    * @date: 2023-10-30
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
     * 导出Excel
     *
     * @param dto
     * @return
     * @author Luo_WG
     * @date: 2023-10-30
     */
    void exportList(FirstMileDeliveryDTO.PagingParamDTO dto);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, FirstMileDeliveryEntity entity);

    /**
     * 下推加工单列表查询
     * @Author Luo_WG
     * @Date 2023/10/31 9:44
     * @param ids
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaDeliveryDTO.GenerateGenerateMachineView>>
     **/
    List<FirstMileDeliveryDTO.GenerateMachineView> generateMachineView(List<String> ids);

    /**
     * 下推加工单保存
     * @Author Luo_WG
     * @Date 2023/10/31 9:57
     * @param list
     * @return com.common.core.controller.vo.ApiResult
     **/
    Boolean fbaDeliveryGenerateMachineSave(List<FirstMileDeliveryDTO.GenerateMachineView> list);

    /**
     * 下推加工单保存并提交
     * @Author Luo_WG
     * @Date 2023/11/7 11:38
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean fbaDeliveryGenerateMachineSubmit(List<FirstMileDeliveryDTO.GenerateMachineView> list);

    /**
     * 下推加工单提交并审核
     * @Author Luo_WG
     * @Date 2023/11/7 11:46
     * @param list
     * @return java.lang.Boolean
     **/
    List<BatchResultDTO> fbaDeliveryGenerateMachineSubmitAndApprove(List<FirstMileDeliveryDTO.GenerateMachineView> list);

    /**
     * 打印子件明细查询
     * @Author Luo_WG
     * @Date 2023/10/31 10:12
     * @param list
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaDeliveryDTO.PrintSonItem>>
     **/
    List<FirstMileDeliveryDTO.PrintSonItem> printSonItemDetail(List<FirstMileDeliveryDTO.GenerateMachineView> list);

    /**
     * 根据来源单号查询发货记录
     *
     * @param ids
     * @param fbaShipmentCode
     * @return java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.DeliverRecordView>
     * @Author Luo_WG
     * @Date 2023/11/1 18:06
     **/
    List<FirstMileDeliveryDTO.DeliverRecordView> listDeliveryRecordBySourceIds(List<String> ids, String fbaShipmentCode);

    /**
     * 根据来源单号查询发货信息
     * @Author Luo_WG
     * @Date 2023/11/8 10:00
     * @param sourceIds
     * @return java.util.List<com.erp.model.wms.entity.FbaDeliveryEntity>
     **/
    List<FirstMileDeliveryEntity> listBySourceIds(List<String> sourceIds);

    /**
     * 根据单号查询发货信息
     * @Author Luo_WG
     * @Date 2023/11/8 10:00
     * @return java.util.List<com.erp.model.wms.entity.FbaDeliveryEntity>
     **/
    List<FirstMileDeliveryEntity> listByCodes(List<String> codes);

    /**
     * 根据版本号重新获取下推加工单的子件详情
     * @Author Luo_WG
     * @Date 2023/11/8 11:18
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.FbaDeliveryDTO.SonItem>
     **/
    List<FirstMileDeliveryDTO.SonItem> sonItemDetailByVersion(FirstMileDeliveryDTO.SonItemDetailByVersion dto);

    /**
     * 下推海外仓入库单单个查询
     * @Author Luo_WG
     * @Date 2023/11/29 17:13
     * @param id
     * @return com.erp.model.wms.dto.OverseasWarehouseInboundDTO.ViewDTO
     **/
    OverseasWarehouseInboundDTO.ViewDTO getGenerateOverseasWarehouseInboundView(String id);

    /**
     * 根据sourceId 查询下推的发货单
     * @param sourceId
     * @return
     */
    FirstMileDeliveryEntity findBySourceId(String sourceId);

    /**
     * 生成状态更新为无需生成
     * @param dto
     * @return
     */
    Boolean generateStatusUpdate(FirstMileDeliveryDTO.GenerateStatusUpdateDTO dto);

    List<FirstMileDeliveryDTO.GenerateLogisticDTO> getGenerateLogisticDTO(FirstMileDeliveryDTO.GenerateLogisticReqDTO dto);

    Boolean updateStatus(FirstMileDeliveryDTO.UpdateStatusDTO dto);

    List<FirstMileDeliveryDTO.LogisticStatisticsDTO> logisticStatistics(FirstMileDeliveryDTO.StatisticsReq dto);

    List<FirstMileDeliveryEntity> advanceQuery(AdvanceQueryContainer advanceQueryContainer);

    List<TmsDeclareBillDTO.DeliveryDTO> getCanGenerateDeclare(TmsDeclareBillDTO.QuerySourceDTO dto);

    int countNotVoided(String id);

    /**
     * 更新记录状态
     * @param id
     * @param packingStatus
     */
    void updatePackingStatus(String id, String packingStatus);

    BatchResultDTO generatePackingTask(FirstMileDeliveryEntity firstMileDeliveryEntity);

    /**
     * 根据编码获取记录
     * @param key
     * @return
     */
    FirstMileDeliveryEntity getByCode(String key);

    FirstMileDeliveryEntity getBySourceCode(String key);

    List<FirstMileDeliveryDTO.DeliverRecordView> listDeliveryRecordByFbaCode(String fbaShipmentCode);

    /**
     * 根据编码获取明细列表
     * @param codes 编码
     * @param sourceCodes 来源编码
     * @return
     */
    List<FirstMileDeliveryDTO.ListFirstMileDTO> listDetailByCodes(List<String> codes,List<String> sourceCodes);

    /**
     * 根据业务单号统计签收数量
     * @param dto
     * @return
     */
    List<FirstMileDeliveryDTO.ReceiveDTO> countReceiveQtyByParams(FirstMileDeliveryDTO.RequestReceiveDTO dto);

    /**
     * 根据发货单获取业务单号
     * @param ids
     * @return
     */
    List<FirstMileDeliveryDTO.BusinessDTO> getBusinessCodeByIds(@Param("ids") List<String> ids);

    /**
     * 导出
     */
    PagingVO<FirstMileDeliveryDTO.ListDTO> exportFbaDelivery(PagingDTO<FirstMileDeliveryDTO.PagingParamDTO> dto);
    /**
     * 根据发货单获取业务单号
     * @param deliveryCodes
     * @return
     */
    List<FirstMileDeliveryDTO.BusinessDTO> getBusinessCodeByCodes(List<String> deliveryCodes);

    void exportPackingDetail(PackingTaskDTO.ExportDTO dto);

    PagingVO<WmsCartonDetailDTO.ListPackingDetailDTO> firstMilePackingTaskDetail(PagingDTO<PackingTaskDTO.ExportDTO> dto);

    WmsCartonSpecDTO.ListPackingDTO listPacking(String id);

    void exportBox(PackingTaskDTO.ExportDTO dto);

    /**
     * 更新中转仓库记录
     * @param entity
     * @param changeIds
     * @return
     */
    BatchResultDTO updateTransferWarehouse(FirstMileDeliveryEntity entity, List<String> changeIds);
    /**业务单号查询发货单号
     *
     * @param businessCodes
     * @return
     */
    List<FirstMileDeliveryDTO.BusinessDTO> getDeliveryCodeByBusinessCodes(List<String> businessCodes);

    /**
     * 获取当前月份内符合条件的发货列表
     * @param approveStatus
     * @param sourceType
     * @param reportMonth
     * @param shipmentCode
     * @param asin
     * @param msku
     * @return
     */
    List<FbaTransitCalculateReportDTO.DeliveryDTO> listDeliveryByReportMonth(String approveStatus, String sourceType, LocalDate reportMonth, String shipmentCode, String asin, String msku);

    List<FirstMileDeliveryDTO.GenerateLogisticDTO> listGenerateLogisticDTO(List<String> deliveryCodes);
    /**
     * 重新出库
     * @author will
     * @date 2025/8/6 17:01
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO retryOutstock(String id);
}
