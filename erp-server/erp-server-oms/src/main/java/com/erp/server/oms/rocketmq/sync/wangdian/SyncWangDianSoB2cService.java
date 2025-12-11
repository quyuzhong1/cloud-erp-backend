package com.erp.server.oms.rocketmq.sync.wangdian;


import com.erp.model.oms.dto.KolSubB2cApplicationDTO;
import com.erp.model.oms.entity.KolB2cApplicationEntity;
import com.erp.model.oms.entity.KolSubB2cApplicationEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;

import java.util.List;
import java.util.Map;

public interface SyncWangDianSoB2cService {

    void syncDataToWangDian(KolSubB2cApplicationDTO.PushDTO pushDTO, Map<String, SkuVO> skuMap );
}
