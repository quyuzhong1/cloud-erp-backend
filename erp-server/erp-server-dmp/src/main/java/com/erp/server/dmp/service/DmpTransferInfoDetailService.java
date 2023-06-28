package com.erp.server.dmp.service;

import com.common.business.service.SuperService;
import com.erp.model.dmp.entity.DmpTransferInfoDetailEntity;

import java.util.List;


/**
 * <p>
 * 直接调拨详情 服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-19
 */
public interface DmpTransferInfoDetailService extends SuperService<DmpTransferInfoDetailEntity> {

    /**
     * @description: 新增
     * @author Will
     * @date: 2023/6/28 18:07
     * @param detailList
     * @param mainId
     */
    Boolean add(List<DmpTransferInfoDetailEntity> detailList, String mainId);
    /**
     * @description: 根据主表id查询
     * @author Will
     * @date: 2023/6/28 18:22
     * @param mainId 
     * @return List<DmpTransferInfoDetailEntity> 
     */
    List<DmpTransferInfoDetailEntity> listByMainId(String mainId);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/6/28 18:37
     * @param detailList
     * @param mainId
     * @return Boolean
     */
    Boolean update(List<DmpTransferInfoDetailEntity> detailList, String mainId);
}
