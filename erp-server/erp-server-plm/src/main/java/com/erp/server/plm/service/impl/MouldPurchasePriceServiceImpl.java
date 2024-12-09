package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.plm.entity.MouldPurchasePriceEntity;
import com.erp.server.plm.mapper.MouldPurchasePriceMapper;
import com.erp.server.plm.service.MouldPurchasePriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 模具价目表 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
@Slf4j
@Service
public class MouldPurchasePriceServiceImpl extends SuperServiceImpl<MouldPurchasePriceMapper, MouldPurchasePriceEntity> implements MouldPurchasePriceService {

    @Override
    public List<MouldPurchasePriceEntity> listByMouldDetailIdList(List<String> detailIds) {
        if (CollectionUtils.isEmpty(detailIds)) {
            return Collections.emptyList();
        }
        return list(Wrappers.<MouldPurchasePriceEntity>lambdaQuery().in(MouldPurchasePriceEntity::getMouldDetailId, detailIds));
    }
}
