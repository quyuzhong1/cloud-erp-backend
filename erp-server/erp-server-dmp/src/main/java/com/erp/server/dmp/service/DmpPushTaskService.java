package com.erp.server.dmp.service;

import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.dto.DmpTaskMsgDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 中台同步任务表 服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-09-06
 */
public interface DmpPushTaskService extends SuperService<DmpPushTaskEntity> {


    /**
     * 发送mq并保存任务
     *
     * @param dto
     */
    DmpPushTaskEntity saveTask(DmpPushTaskFeignDTO dto);

    /**
     * 根据id推送任务
     * @author Will
     * @date: 2024/5/14 9:35
     * @param list
     */
    void sendTask(List<DmpPushTaskEntity> list, Integer delayLevel);

    /**
     * 更新同步
     *
     * @param paramDTO
     */
    void updateStatus(DmpSyncMqDTO.ParamDTO paramDTO);

    /**
     * @return List<DmpPushTaskEntity>
     * @description: 查询需要推送的任务
     * @author Will
     * @date: 2023/10/12 16:35
     */
    List<DmpPushTaskEntity> listNeedPushTask();

    /**
     * @param oneDTO
     * @return DmpPushTaskEntity
     * @description: 查询单个
     * @author Will
     * @date: 2023/10/12 18:25
     */
    DmpPushTaskEntity getByParam(DmpSyncTaskDTO.OneDTO oneDTO);

    /**
     * @param listDTO
     * @return List<DmpPushTaskEntity>
     * @description: 查询多个
     * @author Will
     * @date: 2023/10/12 19:28
     */
    List<DmpPushTaskEntity> listByParam(DmpSyncTaskDTO.ListDTO listDTO);

    /**
     * @param dto
     * @return List<TabListDTO>
     * @description: 获取tab列表
     * @author Will
     * @date: 2023/10/13 11:49
     */
    List<DmpPushTaskDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * @param dto
     * @return PagingVO<ListDTO>
     * @description: 分页查询
     * @author Will
     * @date: 2023/10/13 11:50
     */
    PagingVO<DmpPushTaskDTO.ListDTO> paging(PagingDTO<DmpPushTaskDTO.ParamDTO> dto);

    /**
     * @param dto
     * @param response
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2023/10/13 15:34
     */
    Boolean exportExcel(DmpPushTaskDTO.ParamDTO dto, HttpServletResponse response);

    /**
     * @param ids
     * @return Boolean
     * @description: 批量同步
     * @author Will
     * @date: 2023/10/13 15:48
     */
    Boolean batchSync(List<String> ids);

    /**
     * @param ids
     * @return Boolean
     * @description: 批量查询数据后同步
     * @author Will
     * @date: 2023/10/30 9:42
     */
    Boolean batchFindDataSync(List<String> ids);
    /**
     * @param ids
     * @return Boolean
     * @description: 批量修改无需同步
     * @author hyj
     * @date 2024/4/11 16:51
     */
    Boolean batchNoNeedSync(List<String> ids);
    /**
     * @param sourceIds
     * @return Boolean
     * @description: 根据sourceId批量修改无需同步
     * @author hyj
     * @date 2024/4/11 16:51
     */
    Boolean batchNoNeedSyncBySourceId(List<String> sourceIds);

    /**
     * @param syncTaskId
     * @description: 预警
     * @author Will
     * @date: 2023/11/17 11:53
     */
    void sendWarnMsg(String syncTaskId);
    /**
     * @description: 判断
     * @author Will
     * @date: 2023/11/21 10:21
     * @param entity
     * @return Boolean
     */
    Boolean isSendParentBillTask (DmpPushTaskEntity entity);

    /**
     * 保存更新推送记录
     *
     * @param entity
     * @return
     */
    String saveOrUpdateDmpSyncTask(DmpPushTaskEntity entity);

    /**
     * 根据id集合删除数据
     * @param ids
     */
    void deleteByIds(List<String> ids);

    List<DmpPushTaskEntity> listByCodeParam(DmpSyncTaskDTO.ListCodeDTO listCodeDTO);

    /**
     * 批量保存旺店通任务!
     */
    List<DmpPushTaskEntity> saveWdtTaskList(List<DmpPushTaskFeignDTO> dtoList);
    /**
     * 根据sourceId重新同步
     * @param sourceIds
     * @return
     */
    Boolean batchSyncBySourceId(List<String> sourceIds);

    /**
     * 获取飞书预警信息需要推送的(PushTask任务记录)
     * @return
     */
    List<DmpPushTaskEntity> getWarnPushTaskList(List<String> statusList);

    /**
     * 获取飞书预警信息需要推送的(Task汇总报告)
     * @param statusList
     * @return
     */
    List<DmpTaskMsgDTO> getWarnTaskReport(List<String> statusList);
}
