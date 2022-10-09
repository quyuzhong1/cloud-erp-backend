package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.SysTaskDTO;
import com.erp.model.plm.dto.SysTaskPagingDTO;
import com.erp.model.plm.entity.ProjectTaskSysEntity;

import java.util.List;
import java.util.Map;

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


    PagingVO<SysTaskPagingDTO> paging(PagingDTO<BaseSearchDTO> dto);

    Boolean removeTask(String taskId);

    List<ProjectTaskSysEntity> getListByProperty(Integer state);

    List<String> getSysTaskNames();

    List<Map<String, Object>> taskList();
}
