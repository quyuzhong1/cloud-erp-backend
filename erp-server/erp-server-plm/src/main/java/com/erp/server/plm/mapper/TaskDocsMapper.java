package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.model.plm.dto.DeliveryDocsDTO;
import com.erp.model.plm.dto.TaskDocsCountDTO;
import com.erp.model.plm.entity.TaskDeliveryDocsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Classname TaskDocs
 * @Description TODO
 * @Date 2022-09-22 9:44
 * @Created by yl
 */
@Mapper
public interface TaskDocsMapper  extends BaseMapper<TaskDeliveryDocsEntity> {

    List<TaskDocsCountDTO> getTaskDocsCount(@Param("taskIds") List<String> taskIds);

    IPage paging(Page query, @Param("params") BaseSearchDTO params,@Param("ids") List<String> ids);

    List<DeliveryDocsDTO> getByTaskId(@Param("taskId") String taskId);

    List<TaskDocsCountDTO> getTaskDocsCountByProductId();
}
