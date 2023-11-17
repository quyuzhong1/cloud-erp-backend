package com.erp.server.dmp.service;

import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
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
     * @param dto
     */
    void sendMqAndSaveTask(DmpPushTaskFeignDTO dto);

    /**
     * 更新同步
     * @param paramDTO
     */
    void updateStatus(DmpSyncMqDTO.ParamDTO paramDTO);

    /**
     * @description: 查询需要推送的任务
     * @author Will
     * @date: 2023/10/12 16:35
     * @return List<DmpPushTaskEntity>
     */
    List<DmpPushTaskEntity> listNeedPushTask();

    /**
     * @description: 查询单个
     * @author Will
     * @date: 2023/10/12 18:25
     * @param oneDTO
     * @return DmpPushTaskEntity
     */
    DmpPushTaskEntity getByParam(DmpSyncTaskDTO.OneDTO oneDTO);
    /**
     * @description: 查询多个
     * @author Will
     * @date: 2023/10/12 19:28
     * @param listDTO
     * @return List<DmpPushTaskEntity>
     */
    List<DmpPushTaskEntity> listByParam(DmpSyncTaskDTO.ListDTO listDTO);

    /**
     * @description: 获取tab列表
     * @author Will
     * @date: 2023/10/13 11:49
     * @param dto
     * @return List<TabListDTO>
     */
    List<DmpPushTaskDTO.TabListDTO> tabList(PermissionsDTO dto);
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/10/13 11:50
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<DmpPushTaskDTO.ListDTO> paging(PagingDTO<DmpPushTaskDTO.ParamDTO> dto);
    /**
     * @description: 导出
     * @author Will
     * @date: 2023/10/13 15:34
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(DmpPushTaskDTO.ParamDTO dto, HttpServletResponse response);
    /**
     * @description: 批量同步
     * @author Will
     * @date: 2023/10/13 15:48
     * @param ids
     * @return Boolean
     */
    Boolean batchSync(List<String> ids);
    /**
     * @description: 批量查询数据后同步
     * @author Will
     * @date: 2023/10/30 9:42
     * @param ids
     * @return Boolean
     */
    Boolean batchFindDataSync(List<String> ids);
    /**
     * @description: 预警
     * @author Will
     * @date: 2023/11/17 11:53
     * @param syncTaskId
     */
    void sendWarnMsg(String syncTaskId);
}
