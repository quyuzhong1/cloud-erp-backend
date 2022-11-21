package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.DocsDTO;
import com.erp.model.plm.dto.TemplateDeliveryDocsShowDTO;
import com.erp.model.plm.dto.TemplateSearchDTO;
import com.erp.model.plm.entity.TemplateDeliveryDocsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @Entity entity..TemplateDeliveryDocs
 */
@Mapper
public interface TemplateDeliveryDocsMapper extends BaseMapper<TemplateDeliveryDocsEntity> {
    /**
     * @description: 模板输出物列表查询
     * @author Will
     * @date: 2022/11/14 18:47
     * @param query
     * @param params
     * @return IPage<TemplateDeliveryDocsShowDTO>
     */
    IPage<TemplateDeliveryDocsShowDTO> paging(Page query, @Param("params") TemplateSearchDTO params);
    /**
     * @description: 根据任务id和模板id查询
     * @author Will
     * @date: 2022/11/18 11:47
     * @param taskId
     * @param templateId
     * @return List<DocsDTO>
     */
    List<DocsDTO> getDocsByTaskIdAndTemplateId(@Param("taskId") String taskId,@Param("templateId") String templateId);
}




