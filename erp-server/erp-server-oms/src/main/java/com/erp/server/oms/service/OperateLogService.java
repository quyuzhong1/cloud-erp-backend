package com.erp.server.oms.service;

import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.OperateLogEntity;
import com.erp.model.oms.dto.OperateLogDTO;
import org.apache.commons.math3.util.Pair;

import java.util.List;

/**
 * <p>
 * 操作日志表 服务类
 * </p>
 *
 * @author will
 * @since 2023-05-08
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
     * @param pid
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
    Boolean addModuleOperateLogByObj(Object oldObj, Object newObj, String moduleType, String businessId, String msg);

    /**
     *
     * @param oldObj
     * @param newObj
     * @param moduleType
     * @param businessId
     * @param msg
     * @param operation
     * @return
     */
    Boolean addModuleOperateLogByObj(Object oldObj, Object newObj, String moduleType, String businessId, String pid, String msg, String operation);

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


    Boolean addModuleOperateLog(String content, String moduleType, String businessId, String operation, FindUserDTO findUserDTO);

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
     * @description: 删除操作日志
     * @author Will
     * @date: 2023/4/3 17:19
     * @param businessIds
     */
    void removeByBusinessIds(List<String> businessIds);

    List<String> getContentByObj(Object oldObj, Object newObj,String msg);
    /**
     * 批量添加日志
     * @author yl
     * @date 2023-08-21 18:26
     * @param operateLogList
     * @return void
     */
    void batchAddModuleOperateLog(List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList);
    /**
     * 根据销售订单明细查询最新记录
     * @param soIds
     * @param operation
     * @return
     */
    List<OperateLogEntity> listLastLogBySoIds(List<String> soIds, String operation);
}
