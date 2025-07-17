package com.erp.server.workflow.service;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.OperateLogDTO;
import com.erp.model.workflow.entity.OperateLogEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import org.apache.commons.math3.util.Pair;

import java.util.List;

/**
 * <p>
 * 操作日志表 服务类
 * </p>
 *
 * @author will
 * @since 2025-05-12
 */
public interface OperateLogService extends SuperService<OperateLogEntity> {

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/17 10:59
     * @param dto
     * @return PagingVO<listDTO>
     */
    PagingVO<OperateLogDTO.ListDTO> paging(PagingDTO<OperateLogDTO.SearchDTO> dto);
    /**
     * @description: 生成
     * @author Will
     * @date: 2023/3/20 18:41
     * @param oldObj
     * @param newObj
     * @param moduleType
     * @param businessId
     * @param pid  产品id 用于关联相同产品的日志
     * @param msg
     * @return Boolean
     */
    Boolean addModuleOperateLogByObj(Object oldObj, Object newObj, String moduleType, String businessId, String pid, String msg);

    /**
     * @description: 生成
     * @author Will
     * @date: 2023/3/20 18:41
     * @param oldObj
     * @param newObj
     * @param moduleType
     * @param businessId
     * @param msg
     * @return Boolean
     */
    Boolean addModuleOperateLogByObj(Object oldObj, Object newObj, String moduleType, String businessId,String msg);

    /**
     * @description: 新增日志
     * @author Will
     * @date: 2023/3/21 10:14
     * @param content
     * @param moduleType
     * @param businessId
     * @param operation
     * @return Boolean
     */
    Boolean addModuleOperateLog(String content, String moduleType, String businessId,String operation);
    /**
     * @description: 批量新增日志
     * @author Will
     * @date: 2023/3/21 10:32
     * @param content
     * @param moduleType
     * @param pairList
     * @param operation
     * @return Boolean
     */
    Boolean batchAddModuleOperateLog(String content, String moduleType, List<Pair<String, String>> pairList, String operation);

    /**
     * 批量添加日志
     * @author yl
     * @date 2023-08-21 18:26
     * @param operateLogList
     * @return void
     */
    void batchAddModuleOperateLog(List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList);

    /**
     * @description: 新增日志(带用户信息的日志记录) 用于记录用户操作日志
     * @param content
     * @param moduleType
     * @param businessId
     * @param operation
     * @param uid
     * @param username
     */
    Boolean addModuleOperateLog(String content, String moduleType, String businessId,String operation, String uid, String username);

}
