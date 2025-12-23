package com.erp.server.wms.service;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SoB2cDeliveryDetailDTO;

import java.util.List;

/**
 * <p>
 * b2c发货单详情 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
public interface SoB2cDeliveryDetailService extends SuperService<SoB2cDeliveryDetailEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-12-13
    * @param entities
    * @param mainId
    * @return
    */
    void add(List<SoB2cDeliveryDetailEntity> entities, String mainId);

    /**
     * 主表id
     * @Author Luo_WG
     * @Date 2023/12/15 10:12
     * @param mainIds
     * @return java.util.List<com.erp.model.wms.entity.SoB2cDeliveryDetailEntity>
     **/
    List<SoB2cDeliveryDetailEntity> listByMainIds(List<String> mainIds);

    /** 
     * @description 根据
     * @param soDetailIdList 销售订单明细
     * @author Lambda
     * @return 
     * @create 2023-12-18 11:31
     */
    List<SoB2cDeliveryDetailEntity> listBySoDetailIds(List<String> soDetailIdList);

    /**
     * 根据主表id删除子表数据
     * @param ids
     */
    void removeByMainIds(List<String> ids);
}
