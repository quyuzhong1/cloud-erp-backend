package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.BasicProductIdDTO;
import com.erp.model.plm.dto.BatchTaskPhaseDTO;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.dto.TaskPhaseDTO;
import com.erp.model.plm.entity.ProjectPhaseEntity;

import java.util.List;

/**
 * <p>
 * 任务阶段表 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface ProjectPhaseService extends IService<ProjectPhaseEntity> {

    /**
     * 获取项目任务下的 阶段名
     * @author yl
     * @date 2022-09-13 17:38
     * @param dto
     * @return java.util.List<com.erp.model.plm.dto.TaskPhaseDTO>
     */
    List<TaskPhaseDTO> findList(BasicProductIdDTO dto);

    void batchSaveOrUpdate(BatchTaskPhaseDTO list);


    String saveTaskPhase(String productId, String phaseName,Integer isSourceSys);

    Boolean removeTaskPhaseById(String id);



    List<String> getPhaseNameName(String productId);

    List<CopySourceDTO> saveSysPhase(String productId);

    List<ProjectPhaseEntity> getByProductId(String productId);
}
