package com.erp.server.scm.service.impl;

import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.scm.entity.ScmDocumentEntity;
import com.erp.server.scm.mapper.ScmDocumentMapper;
import com.erp.server.scm.service.ScmDocumentService;
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
public class ScmDocumentServiceImpl extends SuperServiceImpl<ScmDocumentMapper, ScmDocumentEntity> implements ScmDocumentService {

}
