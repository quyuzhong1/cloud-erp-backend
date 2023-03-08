package com.erp.server.plm.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.TemplatePropertyDTO;
import com.erp.model.plm.entity.TemplateRefPropertyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 模板属性关系表 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2023-03-06
 */
@Mapper
public interface TemplateRefPropertyMapper extends BaseMapper<TemplateRefPropertyEntity> {

    List<TemplatePropertyDTO> getByTemplateIds(@Param("templateIdList") List<String> templateIdList);
}
