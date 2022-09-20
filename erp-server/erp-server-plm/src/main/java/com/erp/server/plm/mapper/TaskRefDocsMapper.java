package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.TaskRefDocsEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 任务文档关系表 Mapper 接口
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Mapper
public interface TaskRefDocsMapper extends BaseMapper<TaskRefDocsEntity> {

}
