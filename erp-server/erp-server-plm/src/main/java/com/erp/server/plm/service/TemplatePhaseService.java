package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.TemplatePhaseEntity;

import java.util.List;


/**
 *
 */
public interface TemplatePhaseService extends IService<TemplatePhaseEntity> {

    List<CopySourceDTO> saveTemplatePhase(String templateId, String productId);

    List<CopySourceDTO> copyTemplatePhase(String flagId, String productId, String projectId);

    /**
     * @description: 批量新增或修改
     * @author Will
     * @date: 2022/11/17 10:08
     * @param dto

     */
    void batchSaveOrUpdate(BatchTemplatePhaseDTO dto);
    /**
     * @description: 模板阶段删除
     * @author Will
     * @date: 2022/11/17 10:18
     * @param id
     * @param templateId
     * @return Boolean
     */
    Boolean removeTemplatePhase(String id,String templateId);
    /**
     * @description: 根据模板id查询模板阶段
     * @author Will
     * @date: 2022/11/17 10:47
     * @param dto
     * @return List<TemplatePhaseDTO>
     */
    List<TemplatePhaseDTO> findList(BasicTemplateIdDTO dto);

    /**
     * @description: 根据阶段id和模板id查询
     * @author Will
     * @date: 2022/11/17 11:00
     * @param phaseId
     * @param templateId
     * @return TemplatePhaseEntity
     */
    TemplatePhaseEntity getByIdAndTemplateId(String phaseId, String templateId);

    List<TemplatePhaseEntity> getByTemplateIds(List<String> templateIds);
}
