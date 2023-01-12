package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.dto.CfgApiAuthDTO;
import com.erp.model.dmp.entity.CfgApiAuthEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 11:38
 */
public interface CfgApiAuthService extends IService<CfgApiAuthEntity> {
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/1/11 17:02
     * @param dto
     * @return Boolean
     */
    Boolean insert(CfgApiAuthDTO dto);
    /**
     * @description: 编辑
     * @author Will
     * @date: 2023/1/11 17:20
     * @param dto
     */
    void update(CfgApiAuthDTO dto);
}
