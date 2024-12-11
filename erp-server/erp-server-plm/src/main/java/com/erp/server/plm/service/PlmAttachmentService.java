package com.erp.server.plm.service;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.service.SuperService;
import com.erp.model.plm.dto.AttachmentDTO;
import com.erp.model.plm.entity.PlmAttachmentEntity;
import org.springframework.web.multipart.MultipartFile;

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
    /**
     * @description: 上传
     * @author Will
     * @date: 2024/2/23 10:50
     * @param multipartFile
     * @return String
     */
    PlmAttachmentEntity upload(MultipartFile multipartFile,String type);
    /**
     * @description: 删除
     * @author Will
     * @date: 2024/2/23 10:58
     * @param dto
     */
    void removeAttachment(BaseIdDTO dto);
    /**
     * @description: 批量上传
     * @author Will
     * @date: 2024/2/27 15:03
     * @param multipartFileList
     * @param type
     * @return List<PlmAttachmentEntity>
     */
    List<PlmAttachmentEntity> batchUpload(List<MultipartFile> multipartFileList, String type);

    /**
     * 根据URL删除附件信息
     */
    void removeAttachmentByUrl(AttachmentDTO.DeleteDTO dto);

    List<AttachmentDTO.CommonDTO> getUrlById(String id);
}
