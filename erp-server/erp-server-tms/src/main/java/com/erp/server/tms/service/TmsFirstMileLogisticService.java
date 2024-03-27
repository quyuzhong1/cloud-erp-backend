package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.entity.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;

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
    BaseResultDTO.AddDTO add(TmsFirstMileLogisticDTO.AddDTO dto);

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
     * @param sourceIds
     * @return List<TmsFirstMileLogisticEntity>
     **/
    List<LogisticsBillEntity> listBySourceIds(List<String> sourceIds);


    List<TmsFirstMileLogisticDTO.TabListDTO> tabList();

    PagingVO<TmsFirstMileLogisticDTO.PagingVO> paging(PagingDTO<TmsFirstMileLogisticDTO.PagingParamDTO> dto);

    List<TmsFirstMileLogisticDTO.PagingVO> hasWarnPaging(TmsFirstMileLogisticDTO.PagingParamDTO dto);

    TmsFirstMileLogisticDTO.StatisticsVO statistics();

    TmsFirstMileLogisticDTO.ViewDTO view(String id);

    List<BatchResultDTO> updateLogisticsStatus(TmsFirstMileLogisticDTO.UpdateLogisticsStatusDTO dto);

    List<BatchResultDTO> updateInvoicesStatus(TmsFirstMileLogisticDTO.UpdateInvoicesStatusDTO dto);

    void exportInvoices(List<String> ids, HttpServletResponse response);

    List<BatchResultDTO> updateChannel(TmsFirstMileLogisticDTO.UpdateChannelDTO dto);

    List<BatchResultDTO> generateReconciliation(TmsFirstMileLogisticDTO.GenerateReconciliationDTO dto);

    Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) throws Exception;

    void export(TmsFirstMileLogisticDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response);

    void exportFeeDetail(TmsFirstMileLogisticDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response);

    List<BatchResultDTO> delete(List<String> ids);

    TmsFirstMileLogisticDTO.HistoryTrackDTO getHistoryTrack(String id);

    List<TmsFirstMileLogisticDTO.DeliveryDTO> getCanGenerateDeliveryOrder(TmsFirstMileLogisticDTO.CanGenerateDeliveryDTO dto);

    Boolean updateRemark(TmsFirstMileLogisticDTO.UpdateRemarkDTO dto);

    TmsFirstMileLogisticDTO.LogisticsDTO getLogisticsAndShipping(TmsFirstMileLogisticDTO.CanGenerateDeliveryDTO dto);

    List<LogisticsBillEntity> listByOutstcockCode(List<String> outstockCodeList);

    void updateImport(List<LogisticsBillEntity> updateList, List<LogisticsBillDetailEntity> updateDetailList, List<LogisticsTrackEntity> addTrackList);

    List<LogisticsBillEntity> listByTransportNo(List<String> transportNoList);

    void updateImportCost(List<LogisticsBillCostEntity> updateCostList, List<TmsLogisticsBillCostDetailEntity> updateCostDetailList);

    BigDecimal calculateShippingCost(TmsFirstMileLogisticDTO.CalculateShippingCostDTO dto);
    void sendMsgWhenChannelChange(List<String> shopChargeIdList,String titleContent,String messageContent);
}
