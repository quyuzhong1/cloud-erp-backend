package com.erp.server.fms.service;

import com.common.business.dto.AttachDTO;
import com.common.business.service.SuperService;
import com.erp.model.fms.dto.AttachmentDTO;
import com.erp.model.fms.entity.AttachmentEntity;

import java.util.List;

/**
 * <p>
 * FMS附件 服务类
 * </p>
 *
 * @author wuht
 * @since 2025-10-16
 */
public interface AttachmentService extends SuperService<AttachmentEntity> {

    /**
     * 批量保存附件信息
     * @param attachmentUrlList 附件URL列表
     * @param attachmentNameList 附件名称列表
     * @param type 类型
     * @param businessId 业务ID
     */
    void batchSave(List<String> attachmentUrlList, List<String> attachmentNameList, String type, String businessId);

    /**
     * 批量保存附件信息
     * @param attachmentList 附件列表
     * @param type 类型
     * @param businessId 业务ID
     */
    void batchSave(List<AttachDTO> attachmentList, String type, String businessId);

    /**
     * 批量保存附件信息(不删除原有附件)
     * @param attachmentUrlList 附件URL列表
     * @param attachmentNameList 附件名称列表
     * @param type 类型
     * @param businessId 业务ID
     */
    void batchSaveNotDel(List<String> attachmentUrlList, List<String> attachmentNameList, String type, String businessId);

    /**
     * 根据业务表id获取附件信息
     * @param businessIds 业务ID列表
     * @return 附件列表
     */
    List<AttachmentDTO.UpdateDTO> getByBusinessIds(List<String> businessIds);

    /**
     * 删除附件信息
     * @param dto 删除DTO
     */
    void removeAttachment(AttachmentDTO.DeleteDTO dto);

    /**
     * 批量删除附件信息
     * @param businessIds 业务ID列表
     */
    void batchRemoveAttachment(List<String> businessIds);

    /**
     * 保存文件信息
     * @param dto 添加DTO
     */
    void addAttachment(AttachmentDTO.AddDTO dto);

    /**
     * 根据URL列表删除附件
     * @param urlList URL列表
     */
    void deleteByUrlList(List<String> urlList);
}

