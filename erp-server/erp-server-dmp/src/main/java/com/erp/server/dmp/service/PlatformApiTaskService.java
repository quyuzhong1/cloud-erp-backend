package com.erp.server.dmp.service;

import com.common.business.service.SuperService;
import com.common.business.dto.JobTaskDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.dto.ThirdWarehouseTaskDTO;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface PlatformApiTaskService extends SuperService<PlatformApiTaskEntity> {

    /**
     * 修改任务下次执行
     *
     * @param jobTaskDTO jobTaskDTO
     * @param type
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/15 10:24
     **/
    Boolean updateTaskStateById(JobTaskDTO jobTaskDTO, Integer type);

    /**
     * 获取需要执行的任务
     * @param taskName
     * @return
     */
    PlatformApiTaskEntity getByApiCode(String taskName);

    /**
     * 根据店铺id创建平台任务
     * @param dto
     * @return
     */
    Boolean createPlatformTask(PlatformTaskDTO.AddDTO dto);

    /**
     * 根据平台和店铺id查询任务
     * @param dictPlatform
     * @param shopId
     * @return
     */
    List<PlatformApiTaskEntity> listByPlatformAndShop(String dictPlatform, String shopId);

    /**
     * 查询任务
     * @param localTime
     * @return
     */
    List<JobTaskDTO> listApiTask(LocalDateTime localTime,String operateType);

    /**
     * 批量插入任务
     * @param timeoutList
     * @param type
     */
    void updateTaskTypeState(List<JobTaskDTO> timeoutList, int type);

    /**
     * 根据店铺和平台删除平台任务
     * @param dto
     * @return
     */
    Boolean removePlatformTask(PlatformTaskDTO.AddDTO dto);

    /**
     * 开启关闭平台任务
     * @param disabledDTO
     * @return
     */
    Boolean disabledPlatformTask(PlatformTaskDTO.DisabledDTO disabledDTO);


    /**
     * 第三方仓创建平台任务
     * @param dto
     * @return
     */
    Boolean createThirdWarehouseTask(ThirdWarehouseTaskDTO.AddDTO dto);

    /**
     * 分组查询指定平台的groupId
     *
     * @param dictPlatform
     * @return List<String> groupIds
     */
    List<String> findGroupIdByPlatform(String dictPlatform);
}
