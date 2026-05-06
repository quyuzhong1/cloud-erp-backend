package com.erp.server.dmp.service;

import com.common.business.service.SuperService;
import com.erp.model.dmp.dto.AttachmentDTO;
import com.erp.model.dmp.entity.DmpAttachmentEntity;

import java.util.List;

/**
 * <p>
 * 附件表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-04-06
 */
public interface AttachmentService extends SuperService<DmpAttachmentEntity> {


    /**
     * 根据业务表id获取附件信息
     * @author yl
     * @date 2023-03-20 10:27
     * @param businessIds
     * @return java.util.List<AttachmentDTO.UpdateDTO>
     */
    List<AttachmentDTO.UpdateDTO> getByBusinessIds(List<String> businessIds);


    /**
     * 根据业务表id 集合删除
     * @author yl
     * @date 2023-03-20 11:52
     * @param businessIdList
     * @return void
     */
    void deleteByBusinessIds(List<String> businessIdList);


    /**
     * 方法说明
     * @author yl
     * @date 2023-03-23 16:09
     * @param attachmentUrlList
     * @param type
     * @param businessId
     * @return void
     */
    void batchSave(List<String> attachmentUrlList,List<String> attachmentNameList, String type, String businessId);


    /**
     * 根据业务表id 获取附件信息
     * @author yl
     * @date 2023-03-27 9:37
     * @param businessId
     * @return AttachmentDTO.UpdateDTO
     */
    List<AttachmentDTO.UpdateDTO> getByBusinessId(String businessId);


    /**
     * 删除附件
     * @author yl
     * @date 2023-04-03 14:13
     * @param dto
     * @return void
     */
    void removeAttachment(AttachmentDTO.DeleteDTO dto);
}
