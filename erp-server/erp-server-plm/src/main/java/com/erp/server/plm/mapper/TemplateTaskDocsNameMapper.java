package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.TemplateTaskDocsNameEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @Entity entity..TemplateTaskDocsName
 */
@Mapper
public interface TemplateTaskDocsNameMapper extends BaseMapper<TemplateTaskDocsNameEntity> {

    List<TemplateTaskDocsNameEntity> getDocsNamesById(@Param("ids") List<String> ids);
}




