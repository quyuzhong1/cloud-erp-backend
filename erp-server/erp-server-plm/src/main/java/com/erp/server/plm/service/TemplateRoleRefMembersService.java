package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.entity.TemplateRoleRefMembersEntity;

import java.util.List;


/**
 *
 */
public interface TemplateRoleRefMembersService extends IService<TemplateRoleRefMembersEntity> {

    
    /**
     * 产品存模板 保存角色与成员表 关系
     * @author yl
     * @date 2023-03-09 9:38
     * @param templateId
     * @param productId
     * @param sourceMembersList
     * @param sourceRoleList
     * @return void
     */
    void saveRoleRefMembers(String templateId, String productId,List<CopySourceDTO> sourceMembersList, List<CopySourceDTO>  sourceRoleList);

    void copyTemplateRoleRefMembers(String flagId, String productId, String projectId, List<CopySourceDTO> copyRoleSourceList, List<CopySourceDTO> copyMembersSourceList);
    /**
     * @description: 根据模板id删除
     * @author Will
     * @date: 2022/11/14 17:16
     * @param templateId
     */
    void removeByTemplateId(String templateId);
    /**
     * @description: 根据用户id和模板id查询
     * @author Will
     * @date: 2022/11/14 18:16
     * @param userId
     * @param templateId
     * @return List<String>
     */
    List<String> getUserRole(String userId, String templateId);
    /**
     * @description: 根据id和模板id查询中间表数据
     * @author Will
     * @date: 2022/11/15 15:28
     * @param id
     * @param templateId
     * @return TemplateRoleRefMembersEntity
     */
    TemplateRoleRefMembersEntity getByIdAndTemplateId(String id, String templateId);
    /**
     * @description: 根据模板id和表id更新
     * @author Will
     * @date: 2022/11/15 15:36
     * @param templateRoleRefMembersEntity

     */
    Boolean updateByTemplateId(TemplateRoleRefMembersEntity templateRoleRefMembersEntity);
    /**
     * @description: 根据角色id和模板id查询
     * @author Will
     * @date: 2022/11/16 11:49
     * @param roleId
     * @param templateId
     * @return List<TemplateRoleRefMembersEntity>
     */
    List<TemplateRoleRefMembersEntity> getByRoleIdAndTemplateId(String roleId, String templateId);
    /**
     * @description: 根据角色ids和模板id查询
     * @author Will
     * @date: 2022/11/16 11:49
     * @param roleIds
     * @param templateId
     * @return List<TemplateRoleRefMembersEntity>
     */
    List<TemplateRoleRefMembersEntity> getByRoleIdsAndTemplateId(List<String> roleIds, String templateId);
    /**
     * @description: 根据id和模板id删除
     * @author Will
     * @date: 2022/11/17 13:27
     * @param roleRefMembersId
     * @param templateId
     * @return Boolean
     */
    Boolean removeByIdAndTemplateId(String roleRefMembersId, String templateId);
    /**
     * @description: 根据模板id查询
     * @author Will
     * @date: 2022/11/21 16:47
     * @param templateId
     * @return List<TemplateRoleRefMembersEntity>
     */
    List<TemplateRoleRefMembersEntity> getByTemplateId(String templateId);


}
