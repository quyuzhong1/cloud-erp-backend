package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.OperateLogShowDTO;
import com.erp.model.plm.dto.OperateLogSelectDTO;
import com.erp.model.plm.entity.OperateLogEntity;

import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/5 18:19
 */
public interface OperateLogService extends IService<OperateLogEntity> {


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
    Boolean addSysLogByBatchSave(List<OperateLogEntity> list);
    /**
     * @description: 保存日志
     * @author Will
     * @date: 2022/12/6 14:43
     * @param entity
     * @return Boolean
     */
    Boolean addSysLogByOther(OperateLogEntity entity);
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/5 20:42
     * @param dto
     * @return PagingVO<OperateLogShowDTO>
     */
    PagingVO<OperateLogShowDTO> paging(PagingDTO<OperateLogSelectDTO> dto);

    /**
     * @description: 列表不分页查询
     * @author Will
     * @date: 2023/1/6 16:48
     * @param dto
     * @return List<OperateLogShowDTO>
     */
    List<OperateLogShowDTO> listSysLog(OperateLogSelectDTO dto);

    /***
     * 操作日志-产品变更历史分页查询
     * @param dto
     * @return
     */
    PagingVO<OperateLogShowDTO.HistoryDTO> getProductChangeHistory(PagingDTO<OperateLogShowDTO.PagingParamDTO> dto);
}
