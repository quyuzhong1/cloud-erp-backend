package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.TemplateRoleDTO;
import com.erp.model.plm.entity.TemplateMembersEntity;



/**
 *
 */
public interface TemplateMembersService extends IService<TemplateMembersEntity> {

    void saveMember(String templateId, String productId);

    void copyTemplateMembers(String id, String productId, String projectId);
    /**
     * @description: 根据模板id删除成员
     * @author Will
     * @date: 2022/11/14 17:11
     * @param templateId

     */
    void removeByTemplateId(String templateId);
    /**
     * @description: 判断是否是模板成员
     * @author Will
     * @date: 2022/11/14 18:42
     * @param userId
     * @param templateId
     * @return Boolean
     */
    Boolean ifTemplateMember(String userId, String templateId);
    /**
     * @description: 成员新增
     * @author Will
     * @date: 2022/11/15 14:21
     * @param dto
     * @return Boolean
     */
    Boolean saveTemplateMembers(TemplateRoleDTO dto);
    /**
     * @description: 成员编辑
     * @author Will
     * @date: 2022/11/15 15:06
     * @return Boolean
     */
    Boolean updateTemplateMembers(TemplateRoleDTO dto);
    /**
     * @description: 删除成员
     * @author Will
     * @date: 2022/11/15 15:58
     * @param dto
     * @return Boolean
     */
    Boolean deleteTemplateMembers(TemplateRoleDTO dto);
}
