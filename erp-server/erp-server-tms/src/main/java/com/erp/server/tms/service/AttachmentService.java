package com.erp.server.tms.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.tms.entity.TmsAttachmentEntity;

import java.util.List;

/**
 * <p>
 * 公共附件表 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface AttachmentService extends SuperService<TmsAttachmentEntity> {


    /**
     * 根据业务表id获取附件信息
     *
     * @param businessIds
     * @return java.util.List<com.erp.model.scm.dto.AttachmentDTO.UpdateDTO>
     * @author yl
     * @date 2023-03-20 10:27
     */
    List<AttachmentDTO.UpdateDTO> getByBusinessIds(List<String> businessIds);


    /**
     * 根据业务表id 集合删除
     *
     * @param businessIdList
     * @return void
     * @author yl
     * @date 2023-03-20 11:52
     */
    void deleteByBusinessIds(List<String> businessIdList);


    /**
     * 方法说明
     *
     * @param attachmentUrlList
     * @param type
     * @param businessId
     * @return void
     * @author yl
     * @date 2023-03-23 16:09
     */
    void batchSave(List<String> attachmentUrlList, List<String> attachmentNameList, String type, String businessId);


    /**
     * 根据业务表id 获取附件信息
     *
     * @param businessId
     * @return com.erp.model.scm.dto.AttachmentDTO.UpdateDTO
     * @author yl
     * @date 2023-03-27 9:37
     */
    List<AttachmentDTO.UpdateDTO> getByBusinessId(String businessId);


    /**
     * 删除附件
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-04-03 14:13
     */
    void removeAttachment(AttachmentDTO.DeleteDTO dto);
}
