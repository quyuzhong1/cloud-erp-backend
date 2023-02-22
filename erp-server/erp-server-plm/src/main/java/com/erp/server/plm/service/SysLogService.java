package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.SysLogSelectDTO;
import com.erp.model.plm.dto.SysLogShowDTO;
import com.erp.model.plm.entity.SysLogEntity;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/5 18:19
 */
public interface SysLogService  extends IService<SysLogEntity> {


    /**
     * @description: 修改时保存日志
     * @author Will
     * @date: 2022/12/5 20:29
     * @param oldObj 旧对象
     * @param newObj 新对象
     * @param classPath 实体类路径
     * @param businessId 实体对应业务id
     * @param pid 父级id
     * @return Boolean
     */
    Boolean addSysLogByUpdate(Object oldObj,Object newObj,String classPath ,String businessId,String pid,String msg);


    /**
     * @description: 修改时保存日志
     * @author Will
     * @date: 2022/12/5 20:29
     * @param oldObj 旧对象
     * @param newObj 新对象
     * @return String
     */
    List<String> listSysLogField(Object oldObj,Object newObj);
    /**
     * @description: 新增时保存日志
     * @author Will
     * @date: 2022/12/6 14:08
     * @param content
     * @param classPath
     * @param businessId
     * @param pid
     * @return Boolean
     */
    Boolean addSysLogBySave(String content,String classPath,String businessId,String pid);

    /**
     * @description: 新增时批量保存日志
     * @author Will
     * @date: 2022/12/6 14:08
     * @param list
     * @return Boolean
     */
    Boolean addSysLogByBatchSave(List<SysLogEntity> list);
    /**
     * @description: 保存日志
     * @author Will
     * @date: 2022/12/6 14:43
     * @param entity
     * @return Boolean
     */
    Boolean addSysLogByOther(SysLogEntity entity);
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/5 20:42
     * @param dto
     * @return PagingVO<SysLogShowDTO>
     */
    PagingVO<SysLogShowDTO> paging(PagingDTO<SysLogSelectDTO> dto);

    /**
     * @description: 列表不分页查询
     * @author Will
     * @date: 2023/1/6 16:48
     * @param dto
     * @return List<SysLogShowDTO>
     */
    List<SysLogShowDTO> listSysLog(SysLogSelectDTO dto);
}
