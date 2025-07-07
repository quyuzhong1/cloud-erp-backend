package com.erp.server.workflow.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.workflow.dto.ApproveTaskDetailDTO;
import com.erp.model.workflow.entity.ApproveTaskDetailEntity;

import java.util.List;

/**
 * <p>
 * 三方生成查询明细 服务类
 * </p>
 *
 * @author will
 * @since 2025-05-27
 */
public interface ApproveTaskDetailService extends SuperService<ApproveTaskDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-05-27
    * @param detailList
    * @return
    */
    BaseResultDTO.AddDTO add(List<ApproveTaskDetailDTO.AddDTO> detailList,String mainId);

    /**
    * 修改
    * @author will
    * @date: 2025-05-27
    * @param detailList
    * @param mainId 主表id
    * @return
    */
    Boolean update(List<ApproveTaskDetailDTO.UpdateDTO> detailList, String mainId);

    /**
     * 根据主表id查询
     * @author will
     * @date 2025/5/27 12:05
     * @param id
     * @return List<ApproveTaskDetailEntity>
     */
    List<ApproveTaskDetailEntity> listByMainId(String id);
    /**
     * 根据主表id删除
     * @author will
     * @date 2025/7/7 14:57
     * @param mainId
     * @return Boolean
     */
    Boolean removeByMainId(String mainId);
}
