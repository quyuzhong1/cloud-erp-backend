package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.dto.TemplateMembersAddOrUpdateDTO;
import com.erp.model.plm.dto.TemplateRoleMembersDeleteDTO;
import com.erp.model.plm.entity.TemplateMembersEntity;

import java.util.List;


/**
 *
 */
public interface TemplateMembersService extends IService<TemplateMembersEntity> {


    /**
     * 产品保存模板 保存成员信息
     * @author yl
     * @date 2023-03-08 18:33
     * @param templateId
     * @param productId
     * @return void
     */
    List<CopySourceDTO> saveMember(String templateId, String productId);

    List<CopySourceDTO> copyTemplateMembers(String id, String productId, String projectId);
    /**
     * @description: 根据模板id删除成员
     * @author Will
     * @date: 2022/11/14 17:11
     * @param id
     * @param templateId
     * @return Boolean
     */
    Boolean removeByIdAndTemplateId(String id,String templateId);
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
    Boolean saveTemplateMembers(TemplateMembersAddOrUpdateDTO dto);
    /**
     * @description: 成员编辑
     * @author Will
     * @date: 2022/11/15 15:06
     * @return Boolean
     */
    Boolean updateTemplateMembers(TemplateMembersAddOrUpdateDTO dto);
    /**
     * @description: 删除成员
     * @author Will
     * @date: 2022/11/15 15:58
     * @param dto
     * @return Boolean
     */
    Boolean deleteTemplateMembers(TemplateRoleMembersDeleteDTO dto);
    /**
     * @description: 根据模板id查询
     * @author Will
     * @date: 2022/11/21 16:42
     * @param templateId
     * @return List<TemplateMembersEntity>
     */
    List<TemplateMembersEntity> getByTemplateId(String templateId);
    /**
     * @description: 根据角色ids和模板id查询人员
     * @author Will
     * @date: 2023/1/14 9:56
     * @param roleIds
     * @param templateId
     * @return List<TemplateMembersEntity>
     */
    List<TemplateMembersEntity> listByRoleIds(List<String> roleIds,String templateId);
    /**
     * @description: 根据角色名称和模板id查询人员
     * @author Will
     * @date: 2023/2/11 17:31
     * @param roleNames
     * @param templateId
     * @return List<TemplateMembersEntity>
     */
    List<TemplateMembersEntity> listByRoleNames(List<String> roleNames, String templateId);
}
