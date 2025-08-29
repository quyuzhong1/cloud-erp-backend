package com.erp.server.oms.service;

import com.common.business.dto.AttachDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.OmsAttachmentDTO;
import com.erp.model.oms.entity.OmsAttachmentEntity;
import com.erp.model.scm.dto.AttachmentDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-19
 */
public interface OmsAttachmentService extends SuperService<OmsAttachmentEntity> {




    /**
     * 批量保存附件信息
     * @author yl
     * @date 2023-03-23 16:09
     * @param attachmentUrlList
     * @param type
     * @param businessId
     * @return void
     */
    void batchSaveOrUpdate(List<String> attachmentUrlList, List<String> attachmentNameList, String type, String businessId);

    /**
     * 批量保存附件信息
     * @return void
     */
    void batchSaveOrUpdate(List<AttachDTO> attachDTOS, String type);

    /**
     * 批量更新或修改
     * @author will
     * @date 2025/4/18 18:25
     * @param addOrUpdateList
     * @return void
     */
    void batchAddOrUpdate(List<OmsAttachmentDTO.UpdateDTO> addOrUpdateList);


    /**
     * 根据业务表id获取附件信息
     * @author yl
     * @date 2023-03-20 10:27
     * @param businessIds
     * @return java.util.List<com.erp.model.scm.dto.AttachmentDTO.UpdateDTO>
     */
    List<OmsAttachmentDTO.UpdateDTO> getByBusinessIds(List<String> businessIds);

    
    /**
     * 删除附件信息
     * @author yl
     * @date 2023-04-19 11:11
     * @param dto
     * @return void
     */
    void removeAttachment(OmsAttachmentDTO.DeleteDTO dto);


    /**
     * 根据业务表id 集合删除
     * @author yl
     * @date 2023-03-20 11:52
     * @param businessIdList
     * @return void
     */
    void deleteByBusinessIds(List<String> businessIdList);

    /**
     * 根据业务表id 获取附件信息
     * @author yl
     * @date 2023-03-27 9:37
     * @param businessId
     * @return com.erp.model.scm.dto.AttachmentDTO.UpdateDTO
     */
    List<AttachmentDTO.UpdateDTO> getByBusinessId(String businessId);

    List<OmsAttachmentEntity> listByBusinessIdsAndType(List<String> businessIds,String type);
}
