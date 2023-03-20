package com.erp.server.scm.service;

import com.common.core.serveice.SuperService;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.entity.AttachmentEntity;

import java.util.List;

/**
 * <p>
 * 公共附件表 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface AttachmentService extends SuperService<AttachmentEntity> {

    
    /**
     * 根据业务表id获取附件信息
     * @author yl
     * @date 2023-03-20 10:27
     * @param businessIds
     * @return java.util.List<com.erp.model.scm.dto.AttachmentDTO.UpdateDTO>
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
}
