package com.erp.server.dmp.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.dmp.entity.PlatformApiEntity;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.erp.model.plm.entity.PlatformApi
 */
@Mapper
public interface PlatformApiMapper extends BaseMapper<PlatformApiEntity> {

    /**
     * 查询未生成任务的api
     * @Author Luo_WG
     * @Date 2022/11/8 15:33
     * @return java.util.List<com.erp.server.entity.PlatformApiEntity>
     **/
    List<PlatformApiEntity> selectPlatformApiNoTask();

    /**
     * 修改api表状态为已生成任务
     * @Author Luo_WG
     * @Date 2022/11/9 14:38
     * @return void
     **/
    void updatePlatformApiState(@Param("taskEntityList") List<PlatformApiTaskEntity> taskEntityList);
}




