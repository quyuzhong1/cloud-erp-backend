package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.plm.entity.MouldStoreLocationEntity;
import com.erp.server.plm.mapper.MouldStoreLocationMapper;
import com.erp.server.plm.service.MouldStoreLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 模具存放位置 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
@Slf4j
@Service
public class MouldStoreLocationServiceImpl extends SuperServiceImpl<MouldStoreLocationMapper, MouldStoreLocationEntity> implements MouldStoreLocationService {

    @Override
    public List<MouldStoreLocationEntity> listByMouldDetailIdList(List<String> detailIdList) {
        return list(Wrappers.<MouldStoreLocationEntity>lambdaQuery().in(MouldStoreLocationEntity::getMouldDetailId, detailIdList));
    }

    @Override
    public MouldStoreLocationEntity getByMouldDetailId(String detailId) {
        return getOne(Wrappers.<MouldStoreLocationEntity>lambdaQuery().eq(MouldStoreLocationEntity::getMouldDetailId, detailId));
    }
}
