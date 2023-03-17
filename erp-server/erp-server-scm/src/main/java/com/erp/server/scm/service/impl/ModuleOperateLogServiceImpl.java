package com.erp.server.scm.service.impl;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.ModuleOperateLogDTO;
import com.erp.model.scm.entity.ModuleOperateLogEntity;
import com.erp.server.scm.mapper.ModuleOperateLogMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.common.core.serveice.SuperServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 日志表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-17
 */
@Service
public class ModuleOperateLogServiceImpl extends SuperServiceImpl<ModuleOperateLogMapper, ModuleOperateLogEntity> implements ModuleOperateLogService {

    @Override
    public PagingVO<ModuleOperateLogDTO.listDTO> paging(PagingDTO<ModuleOperateLogDTO.searchDTO> dto) {
        return null;
    }
}
