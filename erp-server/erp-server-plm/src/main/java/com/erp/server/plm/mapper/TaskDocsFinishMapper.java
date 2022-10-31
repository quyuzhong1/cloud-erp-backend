package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.CountDTO;
import com.erp.model.plm.entity.TaskDocsFinishEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 任务文档交付表 Mapper 接口
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Mapper
public interface TaskDocsFinishMapper extends BaseMapper<TaskDocsFinishEntity> {

    List<CountDTO> getTaskDocsCountByProductId();
}
