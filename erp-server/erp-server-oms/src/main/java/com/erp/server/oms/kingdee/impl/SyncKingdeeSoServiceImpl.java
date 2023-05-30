package com.erp.server.oms.kingdee.impl;

import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.server.oms.kingdee.SyncKingdeeSoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lambda
 * @Classname SyncKingdeeSoServiceImpl
 * @Description TODO
 * @Date 2023-05-30 11:46
 * @Created by yl
 */
@Slf4j
@Service
public class SyncKingdeeSoServiceImpl implements SyncKingdeeSoService {


    
    /**
     * 销售订单同步金碟
     * @author yl
     * @date 2023-05-30 11:50
     * @param entity
     * @param operate
     * @return void
     */
    @Override
    public void syncDataToKingdee(SoInfoEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //编码
        resultMap.put("code",entity.getCode());

    }
}
