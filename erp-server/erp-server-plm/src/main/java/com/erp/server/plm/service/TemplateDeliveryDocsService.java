package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.dto.TemplateDeliveryDocsDTO;
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
     * @return PagingVO<List<TemplateDeliveryDocsEntity>>
     */
    PagingVO<List<TemplateDeliveryDocsEntity>> paging(PagingDTO<TemplateDeliveryDocsDTO> dto);
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
     * @param id
     * @param templateId
     * @return Boolean
     */
    Boolean deleteByTempalteId(String id, String templateId);
}
