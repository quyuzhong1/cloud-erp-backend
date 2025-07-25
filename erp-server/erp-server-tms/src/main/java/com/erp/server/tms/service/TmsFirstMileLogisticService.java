package com.erp.server.tms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 头程物流单 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-03-19
 */
public interface TmsFirstMileLogisticService extends SuperService<LogisticsBillEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-03-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO addFirstMileLogistics(TmsFirstMileLogisticDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-03-19
    * @param dto
    * @return
    */
    Boolean update(TmsFirstMileLogisticDTO.UpdateDTO dto);

    /**
     * 根据来源id查询物流单
     * @Author Luo_WG
     * @Date 2024/3/20 10:35
     * @param outstockIds
     * @return List<TmsFirstMileLogisticEntity>
     **/
    List<LogisticsBillEntity> listByOutstockIds(List<String> outstockIds);


    List<TmsFirstMileLogisticDTO.TabListDTO> tabList(TmsFirstMileLogisticDTO.PagingParamDTO dto);

    PagingVO<TmsFirstMileLogisticDTO.PagingVO> paging(PagingDTO<TmsFirstMileLogisticDTO.PagingParamDTO> dto);

    List<TmsFirstMileLogisticDTO.PagingVO> hasWarnPaging(TmsFirstMileLogisticDTO.PagingParamDTO dto);

    TmsFirstMileLogisticDTO.StatisticsVO statistics(TmsFirstMileLogisticDTO.PagingParamDTO dto);

    TmsFirstMileLogisticDTO.ViewDTO view(String id);

    List<BatchResultDTO> updateLogisticsStatus(TmsFirstMileLogisticDTO.UpdateLogisticsStatusDTO dto);

    List<BatchResultDTO> updateInvoicesStatus(TmsFirstMileLogisticDTO.UpdateInvoicesStatusDTO dto);

    void exportInvoices(List<String> ids, HttpServletResponse response);

    List<BatchResultDTO> updateChannel(TmsFirstMileLogisticDTO.UpdateChannelDTO dto);

    Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) throws Exception;

    void export(TmsFirstMileLogisticDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response);

    void exportFeeDetail(TmsFirstMileLogisticDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response);

    BatchResultDTO delete(LogisticsBillEntity entity);

    TmsFirstMileLogisticDTO.HistoryTrackDTO getHistoryTrack(String id);

    List<TmsFirstMileLogisticDTO.DeliveryDTO> getCanGenerateDeliveryOrder(TmsFirstMileLogisticDTO.CanGenerateDeliveryDTO dto);

    Boolean updateRemark(TmsFirstMileLogisticDTO.UpdateRemarkDTO dto);

    TmsFirstMileLogisticDTO.LogisticsDTO getLogisticsAndShipping(TmsFirstMileLogisticDTO.CanGenerateDeliveryDTO dto);

    List<LogisticsBillEntity> listByOutstcockCode(List<String> outstockCodeList);

    void updateImport(List<LogisticsBillEntity> updateList, List<LogisticsBillDetailEntity> updateDetailList, List<LogisticsTrackEntity> addTrackList, List<LogisticsBillCostEntity> updateCostList);

    List<LogisticsBillEntity> listByTransportNo(List<String> transportNoList);

    void updateImportCost(List<LogisticsBillCostEntity> updateCostList, List<TmsCostDetailEntity> updateCostDetailList);

    BigDecimal calculateShippingCost(TmsFirstMileLogisticDTO.CalculateShippingCostDTO dto);
    void sendMsgWhenChannelChange(List<String> shopChargeIdList,String titleContent,String messageContent);

    List<TmsFirstMileLogisticDTO.WaitSubmitListDTO> waitSubmitReconciliation(TmsFirstMileLogisticDTO.WaitDTO dto);

    /**
     * 待对账物流单(分页)
     */
    IPage<TmsFirstMileReconciliationDetailDTO.ListDTO> waitReconciliationPaging(Page<?> query, TmsFirstMileReconciliationDetailDTO.PagingParamDTO params);

    /**
     * 根据物流单ID分组查询物流单信息
     */
    List<TmsFirstMileReconciliationDetailDTO.ListDTO> listReconciliationByMainIds(List<String> logisticsBillIds);


    /**
     * 根据物流跟踪单分组查询物流单信息
     */
    List<TmsFirstMileReconciliationDetailDTO.ListDTO> listByTransportNoListAndSupplierIds(List<String> trackNoList, List<String> logisticsSupplierIdList);

    List<Map<String,Object>> getTrackStatusList();


    /**
     * 生成物流单
     */
    BatchResultDTO singleGenerateReconciliation(String id, String reconciliationId, List<LocalDate> dateList, Map<String, TmsFirstMileReconciliationEntity> currentMainEntityMap, String reconciliationType, @NotBlank String type, String supplierId, String supplierName);

    /**
     * 更新对账状态
     */
    void updateReconciliation(List<String> mainIds, String status,String reconciliationId);

    /**
     * 查询周期内已签收未对账的物流单
     */

    List<TmsFirstMileReconciliationDetailDTO.ListDTO> listAutoGenerateFirstMileReconciliation(LocalDate startDate, LocalDate endDate);

    BatchResultDTO autoGenerateFirstMileLogistic(AutoGenerateBillDTO autoGenerateBillDTO);

    /**
     * 查询生成重量分摊单据所需参数
     */
    List<TmsFirstMileLogisticDTO.WeightAllocationDTO> assembleFirstMileEstimatedList();

    /**
     * 下推重量分摊
     * @param entity
     */
    BatchResultDTO pushWeightAllocation(LogisticsBillEntity entity) throws InterruptedException;

    BatchResultDTO generateLogisticsBill(FirstMileDeliveryEntity firstMileDeliveryEntity);

    /**
     * 批量更新渠道
     * @param dtoList
     * @return
     */
    List<BatchResultDTO> batchUpdateChannel(List<TmsFirstMileLogisticDTO.UpdateChannelDTO> dtoList);

    /**
     * 根据业务单号查询物流单
     * @param businessCodeList
     * @param outstockCodeList
     * @return
     */
    List<LogisticsBillEntity> listBySourceCodeList(List<String> businessCodeList, List<String> outstockCodeList, List<String> transportList);

    /**
     * 查询物流单轨迹
     * @param logisticsBillId
     * */

    LogisticsTrackDTO.ViewDTO listTrack(String logisticsBillId);
}
