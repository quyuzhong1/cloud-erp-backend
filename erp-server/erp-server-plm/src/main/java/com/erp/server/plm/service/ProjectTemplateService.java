package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectTemplateEntity;
import com.erp.model.sys.dto.UserDTO;

import java.util.List;

/**
 * <p>
 * 项目模板信息 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface ProjectTemplateService extends IService<ProjectTemplateEntity> {
    
    String saveTemplate(String templateName , String productId , Integer templateType);

    List<StartItemSourceDTO> startItemSource(Integer sourceType);
    /**
     * @description: 模板管理列表查询
     * @author Will
     * @date: 2022/11/11 12:01
     * @param dto
     * @return PagingVO<ProjectTemplateDTO>
     */
    PagingVO<ProjectTemplateDTO> paging(PagingDTO<BaseSearchDTO> dto);
    /**
     * @description: 新增或修改模板
     * @author Will
     * @date: 2022/11/11 14:57
     * @param dto
     * @return Boolean
     */
    Boolean saveOrUpdate(ProjectTemplateSaveOrUpdateDTO dto);
    /**
     * @description: 更新模板状态
     * @author Will
     * @date: 2022/11/11 15:38
     * @param dto
     * @return Boolean
     */
    Boolean updateTemplateStatus(ProjectTemplateUpdateStatusDTO dto);
    /**
     * @description: 根据负责人类型及id查询所有上级
     * @author Will
     * @date: 2023/1/9 10:31
     * @param id
     * @param type
     * @return List<UserDTO>
     */
    List<UserDTO> listSuperior(String id, Integer type);
    /**
     * @description: 根据模板id查询模板成员角色
     * @author Will
     * @date: 2023/1/9 15:33
     * @param templateId
     * @return List<SysRoleDTO>
     */
    List<SysRoleDTO> listTemplateRole(String templateId);
}
