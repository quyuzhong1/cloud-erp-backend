package com.erp.server.scm.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.ModuleOperateLogDTO;
import com.erp.model.scm.entity.ModuleOperateLogEntity;
import com.common.core.serveice.SuperService;

/**
 * <p>
 * 日志表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-17
 */
public interface ModuleOperateLogService extends SuperService<ModuleOperateLogEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/17 10:59
     * @param dto
     * @return PagingVO<listDTO>
     */
    PagingVO<ModuleOperateLogDTO.listDTO> paging(PagingDTO<ModuleOperateLogDTO.searchDTO> dto);
    /**
     * @description: 生成
     * @author Will
     * @date: 2023/3/20 18:41
     * @param oldObj
     * @param newObj
     * @param moduleType
     * @param businessId
     * @param pid
     * @param msg
     * @return Boolean
     */
    Boolean addModuleOperateLogByObj(Object oldObj, Object newObj, String moduleType, String businessId, String pid, String msg);
}
