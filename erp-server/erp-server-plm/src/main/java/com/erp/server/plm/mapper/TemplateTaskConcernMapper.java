package com.erp.server.plm.mapper;
import com.erp.model.plm.entity.TemplateTaskConcernEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 任务关注的人 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-06-20
 */
@Mapper
public interface TemplateTaskConcernMapper extends BaseMapper<TemplateTaskConcernEntity> {

    Boolean deleteConcernUser(@Param("templateId") String templateId, @Param("templateTaskId") String templateTaskId);
}
