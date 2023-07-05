package com.erp.server.plm.mapper;
import com.erp.model.plm.dto.DocHistoryDTO;
import com.erp.model.plm.entity.TaskDocHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 任务文档历史表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
 */
@Mapper
public interface TaskDocHistoryMapper extends BaseMapper<TaskDocHistoryEntity> {


    List<DocHistoryDTO.InfoDTO> historyList(@Param("finishDocsId") String finishDocsId);
}
