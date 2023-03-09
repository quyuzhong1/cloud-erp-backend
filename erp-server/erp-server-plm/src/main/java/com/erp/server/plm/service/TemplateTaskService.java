package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.TaskChargeDistributionEntity;
import com.erp.model.plm.entity.TemplateTaskEntity;
import com.erp.model.plm.vo.TemplateTaskVO;

import java.util.List;
import java.util.Map;

/**
 * @Classname TemplateTaskService
 * @Description TODO
 * @Date 2022-09-20 15:34
 * @Created by yl
 */
public interface TemplateTaskService extends IService<TemplateTaskEntity> {

    /**
     * 产品保存模板 保存任务
     * @param templateId
     * @param productId
     * @return
     */
    List<CopySourceDTO> saveTemplateTask(String templateId, String productId, List<CopySourceDTO>  phaseSourceList);

    List<TemplateTaskEntity> getTaskByTemplateId(String flagTemplateId);

    List<CopySourceDTO> copyTemplateTask(String flagId, String productId, String projectId, List<CopySourceDTO> phaseSourceList);

    /**
     * @param dto
     * @return PagingVO<TemplateTaskShowDTO>
     * @description: 模板任务列表查询
     * @author Will
     * @date: 2022/11/14 9:26
     */
    PagingVO<TemplateTaskShowDTO> paging(PagingDTO<TemplateSearchDTO> dto);

    /**
     * @param dto
     * @return Boolean
     * @description: 模板任务新增或修改
     * @author Will
     * @date: 2022/11/14 14:01
     */
    Boolean saveOrUpdate(TemplateTaskDTO dto);

    /**
     * @param templateId
     * @return List<TemplateTaskEntity>
     * @description: 获取模板下面所有的任务
     * @author Will
     * @date: 2022/11/14 14:34
     */
    List<TemplateTaskEntity> getAllTaskByTemplateId(String templateId);

    /**
     * @param id
     * @return Boolean
     * @description: 删除任务
     * @author Will
     * @date: 2022/11/14 14:44
     */
    Boolean removeTask(String id, String templateId);

    /**
     * @param templateId
     * @description: 根据模板id删除
     * @author Will
     * @date: 2022/11/14 16:53
     */
    void removeByTemplateId(String templateId);

    /**
     * @param templateId
     * @return List<Map < Object>>
     * @description: 获取前置任务列表
     * @author Will
     * @date: 2022/11/18 11:33
     */
    List<Map<String, Object>> getTaskListByTemplateId(String templateId);

    /**
     * @param dto
     * @return TemplateTaskDTO
     * @description: 任务详情数据
     * @author Will
     * @date: 2022/11/18 11:39
     */
    TemplateTaskVO taskDetails(TemplateTaskParamDTO dto);


    List<TemplateTaskEntity> listByRoleId(String roleId);
    /**
     * 设置审核人信息
     */
    void setTaskChargeDistribution(List<TaskChargeDistributionEntity> taskChargeDistributionList, List<String> ids, String templateId, String taskId, Integer source);
}
