package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.entity.WmsAttachmentEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-19
 */
public interface WmsAttachmentService extends SuperService<WmsAttachmentEntity> {




    /**
     * 批量保存附件信息
     * @author yl
     * @date 2023-03-23 16:09
     * @param attachmentUrlList
     * @param type
     * @param businessId
     * @return void
     */
    void batchSave(List<String> attachmentUrlList, List<String> attachmentNameList, String type, String businessId);

    /**
     * 批量保存附件信息(不删除原有附件)
     * @Author Luo_WG
     * @Date 2023/5/22 16:08
     * @param attachmentUrlList
     * @param type
     * @param businessId
     * @return void
     **/
    void batchSaveNotDel(List<String> attachmentUrlList, List<String> attachmentNameList, String type, String businessId);

    /**
     * 根据业务表id获取附件信息
     * @author yl
     * @date 2023-03-20 10:27
     * @param businessIds
     * @return java.util.List<com.erp.model.scm.dto.AttachmentDTO.UpdateDTO>
     */
    List<WmsAttachmentDTO.UpdateDTO> getByBusinessIds(List<String> businessIds);

    
    /**
     * 删除附件信息
     * @author yl
     * @date 2023-04-19 11:11
     * @param dto
     * @return void
     */
    void removeAttachment(AttachmentDTO.DeleteDTO dto);
}
