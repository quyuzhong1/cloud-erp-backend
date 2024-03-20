package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.entity.TmsFirstMileLogisticEntity;

import java.util.List;

/**
 * <p>
 * 头程物流单 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-03-19
 */
public interface TmsFirstMileLogisticService extends SuperService<TmsFirstMileLogisticEntity> {

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
    List<TmsFirstMileLogisticEntity> listBySourceIds(List<String> sourceIds);


}
