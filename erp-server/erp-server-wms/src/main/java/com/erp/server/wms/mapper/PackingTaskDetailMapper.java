package com.erp.server.wms.mapper;

import com.erp.model.wms.dto.PackingTaskDTO;
import com.erp.model.wms.entity.PackingTaskDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 装箱任务明细表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-07-02
 */
@Mapper
public interface PackingTaskDetailMapper extends BaseMapper<PackingTaskDetailEntity> {
    /**
     * 根据主表id进行sku分组统计
     * @param mainIds
     * @return
     */
    List<PackingTaskDTO.DetailDTO> listDetailByMainIds(@Param("mainIds") List<String> mainIds);

    /**
     * 统计发货数量总和
     * @param taskId
     */
    void countDeliveryQty(@Param("taskId") String taskId);
}
