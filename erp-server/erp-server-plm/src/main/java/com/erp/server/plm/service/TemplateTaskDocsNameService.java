package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.dto.DocsDTO;
import com.erp.model.plm.dto.TmeplateDocsNameDTO;
import com.erp.model.plm.entity.TemplateTaskDocsNameEntity;

import java.util.List;


/**
 *
 */
public interface TemplateTaskDocsNameService extends IService<TemplateTaskDocsNameEntity> {

    List<CopySourceDTO>  saveTemplateDocsName(String templateId, String productId);

    List<CopySourceDTO> copyTemplateDocsName(String flagId, String productId, String projectId);
    /**
     * @description: 保存模板交付文件名
     * @author Will
     * @date: 2022/11/16 12:25
     * @param dto
     * @return Boolean
     */
    Boolean saveDocsName(TmeplateDocsNameDTO dto);
    /**
     * @description: 获取交付文件名
     * @author Will
     * @date: 2022/11/16 13:01
     * @param templateId
     * @return List<DocsDTO>
     */
    List<DocsDTO> getDocsNameList(String templateId);

    /**
     * @description: 根据交付文档名称id和模板id查询
     * @author Will
     * @date: 2022/11/16 19:58
     * @param docsNameId
     * @param templateId
     * @return TemplateTaskDocsNameEntity
     */
    TemplateTaskDocsNameEntity getByIdAndTemplateId(String docsNameId, String templateId);
    /**
     * @description: 修改交付文档
     * @author Will
     * @date: 2022/11/18 9:06
     * @param dto
     * @return Boolean
     */
    Boolean updateDocsName(TmeplateDocsNameDTO dto);

    
    /**
     * 根据模板id 集合获取模板文档名
     * @author yl
     * @date 2023-03-08 10:32
     * @param templateIds
     * @return java.util.List<com.erp.model.plm.entity.TemplateTaskDocsNameEntity>
     */
    List<TemplateTaskDocsNameEntity> getByTemplateIds(List<String> templateIds);
}
