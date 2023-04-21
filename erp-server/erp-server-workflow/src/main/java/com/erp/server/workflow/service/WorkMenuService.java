package com.erp.server.workflow.service;

import com.erp.model.workflow.dto.DictBasicDTO;
import com.erp.model.workflow.entity.WorkMenuEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 工作台菜单基础表 服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
public interface WorkMenuService extends SuperService<WorkMenuEntity> {

    /**
     * 通过code获取下拉列表
     * @param code
     * @return
     */
    List<DictBasicDTO.DropDownDTO> listByCode(String code);
}
