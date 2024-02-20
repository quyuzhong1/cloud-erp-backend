package com.erp.server.wms.service;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FbaShipmentReceiveDTO;

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
    Boolean saveAndCheckTransfer(List<FbaShipmentReceiveEntity> entityList);

    /**
     * 移除非Erp系统的签收记录的关联关系
     */
    void checkAndRemoveDetailIds(List<String> mainIds);
}
