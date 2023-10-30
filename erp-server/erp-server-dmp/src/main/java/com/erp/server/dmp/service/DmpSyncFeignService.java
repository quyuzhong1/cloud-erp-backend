package com.erp.server.dmp.service;

import com.erp.model.dmp.dto.DmpSyncKingdeeDTO;

import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: 金蝶同步feign接口
 * @date 2023/10/17 18:19
 */
public interface DmpSyncFeignService {
    /**
     * @description: 查询金蝶数据
     * @author Will
     * @date: 2023/10/17 18:20
     * @param paramDTO
     * @return List<Map<Object>>
     */
    List<Map<String, Object>> listKingdeeData(DmpSyncKingdeeDTO.ParamDTO paramDTO);
}
