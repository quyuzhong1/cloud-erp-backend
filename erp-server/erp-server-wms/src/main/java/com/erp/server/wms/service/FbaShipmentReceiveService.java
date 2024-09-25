package com.erp.server.wms.service;
import com.erp.model.dmp.lingxing.FbaReceiveGroupEntity;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.common.business.service.SuperService;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * FBA货件签收信息 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-11-01
 */
public interface FbaShipmentReceiveService extends SuperService<FbaShipmentReceiveEntity> {

    /**
     * 根据详情id查询收货记录
     * @param detailIds
     * @return java.util.List<com.erp.model.wms.entity.FbaShipmentReceiveEntity>
     **/
    List<FbaShipmentReceiveEntity> listByDetailIds(List<String> detailIds);

    /**
     * 检查和设置签收的映射关系
     * @param oldDetailEntityList
     * @param receiveEntityList
     * @return
     */
    List<FbaShipmentReceiveEntity> checkAndSetReceiveSkuMapping(List<FbaShipmentDetailEntity> oldDetailEntityList, List<FbaShipmentReceiveEntity> receiveEntityList);

    /**
     * 保存签收记录并检查调拨
     */
    Boolean saveAndCheckTransfer(List<FbaShipmentReceiveEntity> entityList, FbaShipmentEntity fbaShipmentEntity);

    /**
     * 移除非Erp系统的签收记录的关联关系
     */
    void checkAndRemoveDetailIds(List<String> mainIds, LocalDate checkBillDate);

    /**
     * 检查并绑定历史的签收记录
     */
    List<FbaShipmentReceiveEntity> checkAndBindHistory(FbaShipmentEntity entity, List<FbaShipmentDetailEntity> newDetailEntityList, String sourceType);

    /**
     * 获取或拉取货件
     */
    FbaShipmentEntity getAndPullResend(FbaReceiveGroupEntity groupEntity);

    /**
     * 指定详情ID和来源类型查询明细
     */
    List<FbaShipmentReceiveEntity> listByDetailIdsAndSourceType(List<String> detailIds, String sourceType);

    /**
     * 发送预警
     */
    void sendWarnMsg(String tableId, String errorMsg);


    /**
     * 反审核并删除历史调拨单, 遇到关账或异常发送预警
     */
    void checkAndSendWarn(FbaShipmentEntity fbaShipmentEntity, LocalDate billDate);

    /**
     * 根据md5查询历史
     */
    List<FbaShipmentReceiveEntity> listByUniqueMd5AndReceivedDate(List<String> md5List, String fbaShipmentId, LocalDate billDate);

    /**
     * 汇总 亚马逊签收报告/第三方仓签收报告签收数量
     * @param dto
     * @return
     */
    List<FirstMileDeliveryDTO.ReceiveDTO> countReceiveQtyByParams(FirstMileDeliveryDTO.RequestReceiveDTO dto);
}
