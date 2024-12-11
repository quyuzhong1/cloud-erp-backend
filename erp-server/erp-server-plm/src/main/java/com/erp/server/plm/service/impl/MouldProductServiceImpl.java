package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.plm.entity.MouldProductEntity;
import com.erp.server.plm.mapper.MouldProductMapper;
import com.erp.server.plm.service.MouldProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 模具 产品 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
@Slf4j
@Service
public class MouldProductServiceImpl extends SuperServiceImpl<MouldProductMapper, MouldProductEntity> implements MouldProductService {

    @Override
    public List<MouldProductEntity> listByMouldDetailIdList(List<String> detailIdList) {
        return list(Wrappers.<MouldProductEntity>lambdaQuery().in(MouldProductEntity::getMouldDetailId, detailIdList));
    }
}
