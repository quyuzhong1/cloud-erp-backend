package com.erp.server.scm.service.impl;

import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.scm.entity.ScmAttachmentEntity;
import com.erp.server.scm.mapper.ScmAttachmentMapper;
import com.erp.server.scm.service.ScmAttachmentService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 公共附件表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class ScmAttachmentServiceImpl extends SuperServiceImpl<ScmAttachmentMapper, ScmAttachmentEntity> implements ScmAttachmentService {

}
