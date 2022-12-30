package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.dto.BiSalesMonitoringDTO;
import com.erp.model.bi.entity.BiSalesMonitoringEntity;
import com.erp.model.bi.vo.BiSalesMonitoringViewVO;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/29 16:24
 */
public interface BiSalesMonitoringService extends IService<BiSalesMonitoringEntity> {

    /**
     * @description: 批量新增
     * @author Will
     * @date: 2022/12/29 17:00
     * @param list
     */
    Boolean batchAdd(List<BiSalesMonitoringDTO> list);
    /**
     * @description: 批量更新
     * @author Will
     * @date: 2022/12/29 16:53
     * @param list
     */
    void batchUpdate(List<BiSalesMonitoringDTO> list);
    /**
     * @description: 查询所有销售监控数据
     * @author Will
     * @date: 2022/12/29 16:59
     * @return List<BiSalesMonitoringDTO>
     */
    List<BiSalesMonitoringDTO> listBiSalesMonitoring();
    /**
     * @description:
     * @author Will
     * @date: 2022/12/30 12:29
     * @return List<BiSalesMonitoringViewVO>
     */
    List<BiSalesMonitoringViewVO> listBiSalesMonitoringView();
}
