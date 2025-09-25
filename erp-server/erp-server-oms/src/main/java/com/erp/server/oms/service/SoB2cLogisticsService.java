package com.erp.server.oms.service;

import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cLogisticsDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * <p>
 * B2C销售订单物流信息表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
public interface SoB2cLogisticsService extends SuperService<SoB2cLogisticsEntity> {

    /**
     * @description: 新增
     * @author Will
     * @date: 2023/8/21 17:12
     * @param logisticsDTO
     * @param mainId
     * @return Boolean
     */
    Boolean add(SoB2cLogisticsDTO.AddDTO logisticsDTO, String mainId);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/8/21 17:12
     * @param logisticsDTO
     * @param mainId
     * @return Boolean
     */
    Boolean update(SoB2cLogisticsDTO.UpdateDTO logisticsDTO, String mainId);
    /**
     * @description: 根据主表id查询
     * @author Will
     * @date: 2023/8/22 11:13
     * @param mainId
     * @return SoB2cLogisticsEntity
     */
    SoB2cLogisticsEntity getByMainId(String mainId);
    /**
     * @description: 根据主表ids查询
     * @author Will
     * @date: 2023/8/22 14:44
     * @param ids
     * @return List<SoB2cLogisticsEntity>
     */
    List<SoB2cLogisticsEntity> listByMainIds(List<String> ids);
    /**
     * @description: 根据主表删除
     * @author Will
     * @date: 2023/8/23 12:26
     * @param mainIds
     * @return Boolean
     */
    Boolean deleteByMainIds(List<String> mainIds);
    /**
     * @param mainId
     * @param transportNo
     * @param trackNo
     * @param iossTaxNo
     * @param declareOrgId
     * @return Boolean
     * @description: 更新物流单号
     * @author Will
     * @date: 2023/8/24 15:51
     */
    Boolean updateLogisticsCode(String mainId, String transportNo, String trackNo, String iossTaxNo, String declareOrgId);

    /**
     * 更新中转信息
     * @param mainId
     * @param transportNo
     * @param trackNo
     * @return
     */
    Boolean updateTransferInfo(List<SoB2cLogisticsEntity> updateLogisticList);

    /**
     * 平台订单明细更新或保存
     *
     * @Author Jim
     * @since 2023-11-10
     **/
    SoB2cLogisticsEntity saveOrUpdateEntity(PlatformOrderDTO dto, SoB2cEntity mainEntity, BigDecimal allNetWeight, BigDecimal maxLength, BigDecimal maxWidth, BigDecimal totalHeight);

    /**
     * 获取物流费用参数
     * @param orderId
     * @return
     */
    SoB2cDTO.ShippingCalculationDTO getShippingCalculationByOrderId(String orderId);

   /**  根据物流渠道查询
    * @description
    * @param
    * @author Lambda
    * @return
    * @create 2023-12-28 10:37
    */
   List<SoB2cLogisticsEntity> listByChannelId(String channelId);


   /**
    * 获取到跟踪单号为空的列表
    * @description
    * @author Lambda
    * @return 
    * @create 2024-01-05 9:29
    */
   List<SoB2cLogisticsDTO.TrackNoDTO> listTrackNoEmptyList(SoB2cDTO.QueryDTO queryDTO);

    /**
     * 根据主表id修改发货时间
     * @param mainIds
     * @param deliveryTime
     * @return
     */
   Boolean updateDeliveryTimeByMainIds(List<String> mainIds, LocalDateTime deliveryTime);

   /**
    * 根据物流跟踪号查询订单物流信息
    * @Author Luo_WG
    * @Date 2024/4/17 15:09
    * @param trackNo
    * @return java.util.List<com.erp.model.oms.entity.SoB2cLogisticsEntity>
    **/
   SoB2cLogisticsEntity getSoB2cLogisticsByTrackNo(String trackNo);

   /**
    * 清除订单物流信息的发货信息
    * @Author Luo_WG
    * @Date 2024/4/18 16:26
    * @param soIdList
    * @return java.lang.Boolean
    **/
   Boolean clearB2cLogisticsCode(List<String> soIdList);

   Boolean updateWeight(String soId, String logisticsId, BigDecimal weightByG, String operation);

    BatchResultDTO cancelLogistic(String id, List<SoB2cEntity> soB2cEntityList, List<SoB2cLogisticsEntity> soB2cLogisticsEntityList, Boolean checkBillStatus);
    /**
     * 根据物流跟踪号或运单好查询订单物流信息
     * @author will
     * @date 2024/7/5 10:28
     * @param logisticsCode
     * @return SoB2cLogisticsEntity
     */
    SoB2cLogisticsEntity getByTrackNoOrTransportNo(String logisticsCode);

    /**
     * 根据销售订单更新跟踪单号
     * @param soId
     * @param trackNo
     */
    void updateLogisticsBySoId(String soId, String trackNo);

    /**
     * 根据运单号进行跟踪号更新
     * @param trackDTOS
     */
    void updateTrackNoByTransportNo(List<LogisticsBillDTO.TrackDTO> trackDTOS);

    /**
     * 更新物流预估费用
     *
     * @param b2cSoId
     * @param totalShippingCost
     * @param currency
     */
    void updateLogisticsFee(String b2cSoId, BigDecimal totalShippingCost, String currency);

    /**
     * 转单预览
     * @param ids
     * @return
     */
    List<SoB2cLogisticsDTO.transferOrderDTO> transferOrderView(List<String> ids);

    BatchResultDTO transferOrderSave(SoB2cLogisticsDTO.transferOrderDTO dto, LogisticsChannelDTO.BaseDTO channel, SoB2cEntity soB2cEntity);
}
