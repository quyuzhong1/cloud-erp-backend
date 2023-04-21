package com.erp.server.workflow.service;

import com.erp.model.workflow.dto.DictBasicDTO;
import com.erp.model.workflow.entity.DictBasicEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 字典表 服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
public interface DictBasicService extends SuperService<DictBasicEntity> {

    /**
     * 下拉列表
     * @param type
     * @param remark
     * @return
     */
    List<DictBasicDTO.DropDownDTO> listByType(String type, String remark);
}
