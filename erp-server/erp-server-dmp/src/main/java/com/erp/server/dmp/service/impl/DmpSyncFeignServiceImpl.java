package com.erp.server.dmp.service.impl;

import com.erp.model.dmp.dto.DmpSyncKingdeeDTO;
import com.erp.server.dmp.service.DmpSyncFeignService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: 金蝶同步feign实现
 * @date 2023/10/17 18:19
 */
@Service
public class DmpSyncFeignServiceImpl implements DmpSyncFeignService {


    @Override
    public List<Map<String, Object>> listKingdeeData(DmpSyncKingdeeDTO.ParamDTO paramDTO) {
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(paramDTO.getFormId());
        List<Map<String, Object>> queryList = apiUtils.queryList(paramDTO.getFilterString(), paramDTO.getFieldKeys(), paramDTO.getLimit(), paramDTO.getStartRow(), paramDTO.getTopRowCount());
        return queryList;
    }
}
