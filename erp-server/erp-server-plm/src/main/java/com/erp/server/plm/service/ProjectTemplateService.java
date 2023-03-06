package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectTemplateEntity;
import com.erp.model.plm.vo.PreTaskListVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 项目模板信息 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface ProjectTemplateService extends IService<ProjectTemplateEntity> {

    String saveTemplate(String templateName, String productId, Integer templateType);

    List<StartItemSourceDTO> startItemSource(Integer sourceType);

    /**
     * @param dto
     * @return PagingVO<ProjectTemplateDTO>
     * @description: 模板管理列表查询
     * @author Will
     * @date: 2022/11/11 12:01
     */
    PagingVO<ProjectTemplateDTO> paging(PagingDTO<BaseSearchDTO> dto);

    /**
     * @param dto
     * @return Boolean
     * @description: 新增或修改模板
     * @author Will
     * @date: 2022/11/11 14:57
     */
    Boolean saveOrUpdate(ProjectTemplateSaveOrUpdateDTO dto);

    /**
     * @param dto
     * @return Boolean
     * @description: 更新模板状态
     * @author Will
     * @date: 2022/11/11 15:38
     */
    Boolean updateTemplateStatus(ProjectTemplateUpdateStatusDTO dto);

    /**
     * @param templateId
     * @return List<SysRoleDTO>
     * @description: 根据模板id查询模板成员角色
     * @author Will
     * @date: 2023/1/9 15:33
     */
    List<SysRoleDTO> listTemplateRole(String templateId);



    List<Map<String, Object>> getProductPropertyList();


    /**
     * 根据任务id查询模板前置任务
     *
     * @param dto
     * @return
     */
    List<PreTaskListVO> ListPreTaskByTaskId(TemplatePreTaskDTO dto);

    /**
     * 根据任务id更新关系
     *
     * @param dto
     * @return
     */
    Boolean updatePreTask(PreTemplateTaskUpdateDTO dto);
}
