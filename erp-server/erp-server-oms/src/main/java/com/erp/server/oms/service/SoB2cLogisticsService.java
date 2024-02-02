package com.erp.server.oms.service;

import com.common.business.dto.PlatformOrderDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cLogisticsDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;

import java.math.BigDecimal;
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
     * @description: 更新物流单号
     * @author Will
     * @date: 2023/8/24 15:51
     * @param mainId
     * @param transportNo
     * @param trackNo
     * @return Boolean
     */
    Boolean updateLogisticsCode(String mainId, String transportNo, String trackNo);

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
   List<SoB2cLogisticsDTO.TrackNoDTO> listTrackNoEmptyList();
}
