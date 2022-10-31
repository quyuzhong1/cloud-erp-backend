package com.erp.server.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @Classname WorkflowMapper
 * @Description TODO
 * @Date 2022-08-16 17:26
 * @Created by yl
 */
@Mapper
public interface WorkflowMapper  extends BaseMapper {


    void deleteTaskByIdArray(@Param("taskIdList") List<String> taskIdList);

    void deleteExecutionByProcInstIdAndActInstIdArray(@Param("procId") String procId, @Param("actInstIdList") List<String> actIdList);

    void updateHiTaskInstByIdArray(@Param("taskIdLit") List<String> taskIdLit, @Param("endTime") Date endTime);

    void updateHiActInstById(@Param("actInstIdList") List<String> actInstIdList, @Param("endTime") Date endTime);
}
