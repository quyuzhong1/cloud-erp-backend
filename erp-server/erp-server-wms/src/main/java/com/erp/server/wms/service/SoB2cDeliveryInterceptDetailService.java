package com.erp.server.wms.service;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO;
import com.erp.model.wms.entity.SoB2cDeliveryInterceptDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDetailDTO;

import java.util.List;

/**
 * <p>
 * b2c发货拦截单详情 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
public interface SoB2cDeliveryInterceptDetailService extends SuperService<SoB2cDeliveryInterceptDetailEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-12-13
    * @param addDTO
    * @param mainId
    * @return
    */
    void add(SoB2cDeliveryInterceptDTO.AddDTO addDTO, String mainId);

    /**
     * 根据主标id查询详情信息
     * @Author Luo_WG
     * @Date 2023/12/25 16:58
     * @param mainIds
     * @return java.util.List<com.erp.model.wms.entity.SoB2cDeliveryInterceptDetailEntity>
     **/
    List<SoB2cDeliveryInterceptDetailEntity> listByMainIds(List<String> mainIds);
}
