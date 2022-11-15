package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.dto.TemplateTaskDTO;
import com.erp.model.plm.entity.TemplateTaskEntity;

import java.util.List;

/**
 * @Classname TemplateTaskService
 * @Description TODO
 * @Date 2022-09-20 15:34
 * @Created by yl
 */
public interface TemplateTaskService extends IService<TemplateTaskEntity> {

    void saveTemplateTask(String templateId, String productId);

    List<TemplateTaskEntity> getTaskByTemplateId(String flagTemplateId);

    List<CopySourceDTO> copyTemplateTask(String flagId, String productId, String projectId, List<CopySourceDTO> phaseSourceList);
    /**
     * @description: 模板任务列表查询
     * @author Will
     * @date: 2022/11/14 9:26
     * @param dto
     * @return PagingVO<TemplateTaskDTO>
     */
    PagingVO<TemplateTaskDTO> paging(PagingDTO<TemplateTaskDTO> dto);
    /**
     * @description: 模板任务新增或修改
     * @author Will
     * @date: 2022/11/14 14:01
     * @param dto
     * @return Boolean
     */
    Boolean saveOrUpdate(TemplateTaskDTO dto);
    /**
     * @description: 获取模板下面所有的任务
     * @author Will
     * @date: 2022/11/14 14:34
     * @param templateId
     * @return List<TemplateTaskEntity>
     */
    List<TemplateTaskEntity> getAllTaskByTemplateId(String templateId);
    /**
     * @description: 删除任务
     * @author Will
     * @date: 2022/11/14 14:44
     * @param id
     * @return Boolean
     */
    Boolean removeTask(String id , String templateId);
    /**
     * @description: 根据模板id删除
     * @author Will
     * @date: 2022/11/14 16:53
     * @param templateId

     */
    void removeByTemplateId(String templateId);
}
