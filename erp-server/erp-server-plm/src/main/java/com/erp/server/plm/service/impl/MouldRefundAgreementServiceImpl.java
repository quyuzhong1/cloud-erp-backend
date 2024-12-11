package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.plm.entity.MouldRefundAgreementEntity;
import com.erp.server.plm.mapper.MouldRefundAgreementMapper;
import com.erp.server.plm.service.MouldRefundAgreementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 合同返还约定 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
@Slf4j
@Service
public class MouldRefundAgreementServiceImpl extends SuperServiceImpl<MouldRefundAgreementMapper, MouldRefundAgreementEntity> implements MouldRefundAgreementService {

    @Override
    public List<MouldRefundAgreementEntity> listByMouldDetailIdList(List<String> detailIds) {
        if (CollectionUtils.isEmpty(detailIds)) {
            return Collections.emptyList();
        }
        return list(Wrappers.<MouldRefundAgreementEntity>lambdaQuery().in(MouldRefundAgreementEntity::getMouldDetailId, detailIds));
    }
}
