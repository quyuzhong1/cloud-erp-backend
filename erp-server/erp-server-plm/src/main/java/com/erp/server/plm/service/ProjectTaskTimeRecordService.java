package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProjectTaskTimeRecordDTO;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.ProjectTaskTimeRecordEntity;
import com.erp.model.plm.vo.ProjectTaskTimeRecordPageVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-02-23
 */
public interface ProjectTaskTimeRecordService extends IService<ProjectTaskTimeRecordEntity> {

    /**
     * 任务工时列表分页查询
     * @param dto
     * @return
     */
    PagingVO<ProjectTaskTimeRecordPageVO> pageRecord(PagingDTO<ProjectTaskTimeRecordDTO.PageRecordDto> dto);

    /**
     * 导出任务工时列表
     *
     * @param dto
     * @return
     */
    Boolean exportTaskTimeList(ProjectTaskTimeRecordDTO.PageRecordDto dto);

    /**
     * 通过任务列表保存或更新工时
     * @param taskList
     */
    Boolean saveOrUpdateByProjectTaskList(List<ProjectTaskEntity> taskList);

    
    /**
     * 根据产品id 获取到工时信息
     * @author yl
     * @date 2023-06-12 18:28
     * @param productIds
     * @return java.util.List<com.erp.model.plm.dto.ProjectTaskTimeRecordDTO.TaskWorkTimeDTO>
     */
    List<ProjectTaskTimeRecordDTO.TaskWorkTimeDTO> listByProductIds(List<String> productIds);

    PagingVO<ProjectTaskTimeRecordPageVO> exportTaskTimeRecord(PagingDTO<ProjectTaskTimeRecordDTO.PageRecordDto> dto);
}
