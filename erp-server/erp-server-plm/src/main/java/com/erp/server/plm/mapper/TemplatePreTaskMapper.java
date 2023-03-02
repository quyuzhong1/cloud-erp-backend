package com.erp.server.plm.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.TemplatePreTaskEntity;
import com.erp.model.plm.vo.PreTaskListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity entity..TemplatePreTask
 */
@Mapper
public interface TemplatePreTaskMapper extends BaseMapper<TemplatePreTaskEntity> {

    /**
     * 根据模板任务id查询前置任务信息任务名
     * @param taskId
     * @return
     */
    List<PreTaskListVO> getPreAndNameByTaskId(@Param("taskId") String taskId);
}




