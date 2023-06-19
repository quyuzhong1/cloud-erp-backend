package com.erp.server.scm.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.dto.SubcontractChangeDetailDTO;
import com.erp.model.scm.entity.SubcontractChangeDetailEntity;

import java.util.List;


/**
 * <p>
 * 委外变单明细 服务类
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
public interface SubcontractChangeDetailService extends SuperService<SubcontractChangeDetailEntity> {

    /**
     * @description: 新增变更明细
     * @author Will
     * @date: 2023/6/19 11:30
     * @param detailList
     * @param mainId
     */
    void add(List<SubcontractChangeDetailDTO.AddDTO> detailList, String mainId);
    /**
     * @description: 修改变更明细
     * @author Will
     * @date: 2023/6/19 11:31
     * @param detailList
     * @param mainId
     */
    void update(List<SubcontractChangeDetailDTO.UpdateDTO> detailList, String mainId);
    /**
     * @description: 根据主表明细ids删除
     * @author Will
     * @date: 2023/6/19 11:36
     * @param mainIds
     */
    void removeByMainIds(List<String> mainIds);
}
