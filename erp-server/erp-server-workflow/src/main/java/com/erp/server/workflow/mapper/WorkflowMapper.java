package com.erp.server.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @Classname WorkflowMapper

 * @Date 2022-08-16 17:26
 * @Created by yl
 */
@Mapper
public interface WorkflowMapper  extends BaseMapper<Objects> {


    void deleteTaskByIdArray(@Param("taskIdList") List<String> taskIdList);

    void deleteExecutionByProcInstIdAndActInstIdArray(@Param("procId") String procId, @Param("actInstIdList") List<String> actIdList);

    void updateHiTaskInstByIdArray(@Param("taskIdList") List<String> taskIdList, @Param("endTime") Date endTime);

    void updateHiActInstById(@Param("actInstIdList") List<String> actInstIdList, @Param("endTime") Date endTime);
}
