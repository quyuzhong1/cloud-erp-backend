package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.TemplatePhaseDTO;
import com.erp.model.plm.entity.TemplatePhaseEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @Entity entity..TemplatePhase
 */
@Mapper
public interface TemplatePhaseMapper extends BaseMapper<TemplatePhaseEntity> {
    /**
     * @description:
     * @author Will
     * @date: 2022/11/17 20:24
     * @param templateId
     * @return List<TemplatePhaseDTO>
     */
    List<TemplatePhaseDTO> getTemplatePhaseByTemplateId(@Param("templateId") String templateId);
}




