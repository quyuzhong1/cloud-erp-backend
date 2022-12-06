package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.SysLogSelectDTO;
import com.erp.model.plm.dto.SysLogShowDTO;
import com.erp.model.plm.entity.RoleRefMemberEntity;
import com.erp.model.plm.entity.SysLogEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/5 18:19
 */
public interface SysLogService  extends IService<SysLogEntity> {


    /**
     * @description: 保存日志
     * @author Will
     * @date: 2022/12/5 20:29
     * @param oldObj 旧对象
     * @param newObj 新对象
     * @param classPath 实体类路径
     * @param businessId 实体对应业务id
     * @return Boolean
     */
    Boolean addSysLog(Object oldObj,Object newObj,String classPath ,String businessId);
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/5 20:42
     * @param dto
     * @return PagingVO<SysLogShowDTO>
     */
    PagingVO<SysLogShowDTO> paging(PagingDTO<SysLogSelectDTO> dto);
}
