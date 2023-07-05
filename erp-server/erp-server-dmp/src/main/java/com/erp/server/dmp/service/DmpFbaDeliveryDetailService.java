package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpFbaDeliveryDetailEntity;
import com.common.business.service.SuperService;

import java.util.List;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
public interface DmpFbaDeliveryDetailService extends SuperService<DmpFbaDeliveryDetailEntity> {

    /**
     * @description: 新增
     * @author zhangchunlin
     * @param detailList
     * @param mainId
     */
    Boolean add(List<DmpFbaDeliveryDetailEntity> detailList, String mainId);
    /**
     * @description: 根据主表id查询
     * @author zhangchunlin
     * @param mainId
     * @return List<DmpTransferInfoDetailEntity>
     */
    List<DmpFbaDeliveryDetailEntity> listByMainId(String mainId);
    /**
     * @description: 修改
     * @author zhangchunlin
     * @param detailList
     * @param mainId
     * @return Boolean
     */
    void update(List<DmpFbaDeliveryDetailEntity> detailList, String mainId);

}
