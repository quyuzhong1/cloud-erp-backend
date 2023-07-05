package com.erp.server.plm.service;
import com.erp.model.plm.entity.PlmAttachmentEntity;
import com.common.business.service.SuperService;

import java.util.List;


/**
 * <p>
 * 附件表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
 */
public interface PlmAttachmentService extends SuperService<PlmAttachmentEntity> {


    /**
     * 批量保存附件信息
     * @author yl
     * @date 2023-06-20 15:45
     * @param attachUrlList
     * @param attachNameList
     * @param type
     * @param businessId
     * @return void
     */
    void batchSave(List<String> attachUrlList, List<String> attachNameList, String type, String businessId);

    
    /**
     * 根据业务表ids 获取附件信息
     * @author yl
     * @date 2023-06-21 12:01
     * @param businessIdList
     * @return void
     */
    List<PlmAttachmentEntity> listByBusinessIds(List<String> businessIdList);
}
