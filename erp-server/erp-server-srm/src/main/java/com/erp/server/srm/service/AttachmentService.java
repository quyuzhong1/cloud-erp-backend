package com.erp.server.srm.service;
import com.erp.model.srm.entity.AttachmentEntity;
import com.common.business.service.SuperService;
import com.erp.model.srm.dto.AttachmentDTO;

import java.util.List;

/**
 * <p>
 * 公共附件表 服务类
 * </p>
 *
 * @author will
 * @since 2024-01-20
 */
public interface AttachmentService extends SuperService<AttachmentEntity> {

    /**
     * @description: 批量添加
     * @author Will
     * @date: 2024/1/20 15:07
     * @param attachmentUrlList
     * @param attachmentNameList
     * @param type
     * @param businessId
     */
    void batchSave(List<String> attachmentUrlList, List<String> attachmentNameList, String type, String businessId);

    /**
     * @description: 删除
     * @author Will
     * @date: 2024/1/20 15:05
     * @param dto

     */
    void removeAttachment(AttachmentDTO.DeleteDTO dto);

    /**
     * @description: 根据业务ids查询
     * @author Will
     * @date: 2024/1/23 15:19
     * @param businessIds
     * @return List<UpdateDTO>
     */
    List<AttachmentDTO.UpdateDTO> listByBusinessIds(List<String> businessIds);
}
