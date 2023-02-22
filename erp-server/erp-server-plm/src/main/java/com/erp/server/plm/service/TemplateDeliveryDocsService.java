package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.TemplateDeliveryDocsEntity;

import java.util.List;


/**
 *
 */
public interface TemplateDeliveryDocsService extends IService<TemplateDeliveryDocsEntity> {

    void saveTemplateDeliveryDocs(String templateId, String productId);

    List<CopySourceDTO> copyTemplateDeliveryDocs(String templateId, String productId, List<CopySourceDTO> taskSourceList, List<CopySourceDTO> docsNameSourceList);
    /**
     * @description: 根据任务id和模板id删除
     * @author Will
     * @date: 2022/11/14 16:56
     * @param taskId
     * @param templateId

     */
    void removeByTaskIdAndTemplateId(String taskId, String templateId);
    /**
     * @description: 根据模板id删除
     * @author Will
     * @date: 2022/11/14 16:56
     * @param templateId

     */
    void removeByTemplateId(String templateId);
    /**
     * @description: 模板输出物列表查询
     * @author Will
     * @date: 2022/11/14 17:53
     * @param dto
     * @return PagingVO<List<TemplateDeliveryDocsShowDTO>>
     */
    PagingVO<List<TemplateDeliveryDocsShowDTO>> paging(PagingDTO<TemplateSearchDTO> dto);
    /**
     * @description: 新增或者修改
     * @author Will
     * @date: 2022/11/15 10:35
     * @param dto
     * @return Boolean
     */
    Boolean saveOrUpdate(TemplateDeliveryDocsDTO dto);
    /**
     * @description: 根据id和模板id删除
     * @author Will
     * @date: 2022/11/15 10:51
     * @param dto
     * @return Boolean
     */
    Boolean deleteTemplateDeliveryDocs(TemplateDeliveryDocsDeleteDTO dto);
    /**
     * @description: 新增交付文档
     * @author Will
     * @date: 2022/11/16 10:16
     * @param taskId
     * @param templateId
     * @param deliveryDocsList

     */
    void saveTemplateDeliveryDocsList(String taskId, String templateId, List<DocsDTO> deliveryDocsList);
    /**
     * @description: 查询模板下面所有输出物
     * @author Will
     * @date: 2022/11/16 15:31
     * @param templateId
     * @return List<TemplateDeliveryDocsEntity>
     */
    List<TemplateDeliveryDocsEntity> getAllDeliveryDocsForTemplate(String templateId);
    /**
     * @description: 修改状态
     * @author Will
     * @date: 2022/11/17 9:45
     * @param dto
     * @return Boolean
     */
    Boolean updateStatus(TemplateDeliveryDocsUpdateStatusDTO dto);

    /**
     * @description: 根据输出物id和模板id查询
     * @author Will
     * @date: 2022/11/18 9:12
     * @param deliveryDocsId
     * @param templateId
     * @return TemplateDeliveryDocsEntity
     */
    TemplateDeliveryDocsEntity getByIdAndTemplateId(String deliveryDocsId, String templateId);
    /**
     * @description: 根据任务id和模板id查询
     * @author Will
     * @date: 2022/11/18 11:45
     * @param id
     * @param templateId
     * @return List<DocsDTO>
     */
    List<DocsDTO> getDocsByTaskIdAndTemplateId(String id, String templateId);
}
