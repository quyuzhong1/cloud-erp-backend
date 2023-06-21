package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.TaskDTO;
import com.erp.model.plm.dto.TaskPagingShowDTO;
import com.erp.model.plm.entity.ProjectTaskEntity;

import java.util.List;

/**
 * @author Lambda
 * @Classname TaskService
 * @Description TODO
 * @Date 2023-06-20 19:50
 * @Created by yl
 */
public interface TaskService extends IService<ProjectTaskEntity> {
    
    /**
     * 可执行的任务列表分页查询 可执行表示没有前置任务 或者 任务没有完成和取消
     * @author yl
     * @date 2023-06-21 9:04
     * @param searchParamDTO
     * @return com.common.business.vo.PagingVO<java.util.List<com.erp.model.plm.dto.TaskPagingShowDTO>>
     */
    PagingVO<List<TaskPagingShowDTO>> allExecutablePaging(PagingDTO<TaskDTO.TaskPagingParamDTO> searchParamDTO);


}
