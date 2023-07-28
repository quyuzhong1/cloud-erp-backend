package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.dto.CfgApiAuthDTO;
import com.erp.model.dmp.entity.CfgApiAuthEntity;

/**
 * @author Will
 * @version 1.0

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
    Boolean insert(CfgApiAuthDTO.ParamDTO dto);
    /**
     * @description: 编辑
     * @author Will
     * @date: 2023/1/11 17:20
     * @param dto
     */
    void update(CfgApiAuthDTO.ParamDTO dto);

    /**
     * @description: 根据key值查询
     * @author Will
     * @date: 2023/7/3 9:30
     * @param key
     * @param apiGroup
     * @param apiPlatformId
     * @return CfgApiAuthEntity
     */
    CfgApiAuthEntity getByKey (String key ,String apiGroup,String apiPlatformId);

    void saveMongoTest(String type);
    void saveMongoTest(String type, String id);

}
