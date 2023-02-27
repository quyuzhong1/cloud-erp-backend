package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.SysTaskDTO;
import com.erp.model.plm.dto.SysTaskPagingDTO;
import com.erp.model.plm.dto.SysTaskPagingSearchDTO;
import com.erp.model.plm.entity.ProjectTaskSysEntity;
import com.erp.model.plm.vo.SysTaskVO;

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


    PagingVO<SysTaskPagingDTO> paging(PagingDTO<SysTaskPagingSearchDTO> dto);

    Boolean removeTask(String taskId);

    List<ProjectTaskSysEntity> getListByProperty(Integer state,String templateId);

    List<String> getSysTaskNames();

    List<Map<String, Object>> taskList();

    SysTaskVO taskDetails(String taskId);

    void checkQuotePhase(String id);
}
