package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.SysTaskDTO;
import com.erp.model.plm.entity.ProjectTaskSysEntity;

/**
 * <p>
 * 系统任务 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface ProjectTaskSysService extends IService<ProjectTaskSysEntity> {
    Boolean saveOrUpdateSysTask(SysTaskDTO dto);


    PagingVO paging(PagingDTO<BaseSearchDTO> dto);

    Boolean removeTask(String taskId);
}
