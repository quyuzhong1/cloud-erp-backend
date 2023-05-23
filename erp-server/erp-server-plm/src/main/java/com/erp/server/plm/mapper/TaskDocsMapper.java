package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseSearchDTO;
import com.erp.model.plm.dto.DeliveryDocsDTO;
import com.erp.model.plm.dto.CountDTO;
import com.erp.model.plm.dto.DocsDTO;
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

    List<CountDTO> getTaskDocsCount(@Param("taskIds") List<String> taskIds);

    IPage<DeliveryDocsDTO> paging(Page query, @Param("params") BaseSearchDTO params,@Param("ids")List<String>  ids);

    List<DeliveryDocsDTO> list(@Param("params") BaseSearchDTO params,@Param("ids") List<String> ids);

    List<DeliveryDocsDTO> getByTaskId(@Param("taskId") String taskId);

    List<CountDTO> getTaskDocsCountByProductId();

    List<DocsDTO> getDocsByTaskId(@Param("taskId") String taskId);


    IPage<DeliveryDocsDTO> containPaging(Page query, BaseSearchDTO params, @Param("ids")List<String> containDocsPowerList);

    IPage<DeliveryDocsDTO> noContainPaging(Page query, BaseSearchDTO params, @Param("ids")List<String> noContainDocsPowerList);
}
