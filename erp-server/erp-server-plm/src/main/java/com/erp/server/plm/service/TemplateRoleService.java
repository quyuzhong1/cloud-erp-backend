package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.dto.TemplateRoleDTO;
import com.erp.model.plm.dto.TemplateRoleShowDTO;
import com.erp.model.plm.dto.TemplateSearchDTO;
import com.erp.model.plm.entity.TemplateRoleEntity;

import java.util.List;


/**
 *
 */
public interface TemplateRoleService extends IService<TemplateRoleEntity> {

    void saveTemplateRole(String templateId, String productId);

    List<CopySourceDTO> copyTemplateRole(String flagId, String productId, String projectId);
    /**
     * @description: 根据模板id删除
     * @author Will
     * @date: 2022/11/14 17:14
     * @param templateId
     */
    void removeByTemplateId(String templateId);
    /**
     * @description: 角色成员列表查询
     * @author Will
     * @date: 2022/11/15 12:07
     * @param dto
     * @return PagingVO<List<TemplateRoleShowDTO>>
     */
    PagingVO<List<TemplateRoleShowDTO>> paging(PagingDTO<TemplateSearchDTO> dto);
    /**
     * @description: 新增角色
     * @author Will
     * @date: 2022/11/15 14:12
     * @param dto
     * @return Boolean
     */
    Boolean saveTemplateRole(TemplateRoleDTO dto);
    /**
     * @description: 根据成员id和模板id删除
     * @author Will
     * @date: 2022/11/16 11:39
     * @param roleId
     * @param templateId
     * @return Boolean
     */
    Boolean removeByIdAndTemplateId(String roleId, String templateId);
    /**
     * @description: 查询模板下所有角色
     * @author Will
     * @date: 2022/11/16 12:05
     * @param templateId
     * @return List<TemplateRoleEntity>
     */
    List<TemplateRoleEntity> getAllRoles(String templateId);
    /**
     * @description: 根据模板id查询
     * @author Will
     * @date: 2022/11/21 16:32
     * @param templateId

     */
    List<TemplateRoleEntity> getByTemplateId(String templateId);
}
