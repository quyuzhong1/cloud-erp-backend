package com.erp.server.sys.service.impl;


import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.sys.entity.ThirdpartyRefBusinessEntity;
import com.erp.server.sys.mapper.ThirdpartyRefBusinessMapper;
import com.erp.server.sys.service.ThirdpartyRefBusinessService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
/**
 * <p>
 * 第三方平台与业务对接关联表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-19
 */
@Slf4j
@Service
public class ThirdpartyRefBusinessServiceImpl extends SuperServiceImpl<ThirdpartyRefBusinessMapper, ThirdpartyRefBusinessEntity> implements ThirdpartyRefBusinessService {


    @Override
    public List<ThirdpartyRefBusinessEntity> listByBusinessIds(List<String> idList , String businessType) {
        if (CollectionUtils.isEmpty(idList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(ThirdpartyRefBusinessEntity::getBusinessType, businessType).in(ThirdpartyRefBusinessEntity::getBusinessId, idList).list();
    }

    @Override
    public ThirdpartyRefBusinessEntity getByBusinessId(String businessId , String businessType) {
        if (StringUtils.isBlank(businessId)) {
            return null;
        }
        return this.lambdaQuery().eq(ThirdpartyRefBusinessEntity::getBusinessType, businessType).eq(ThirdpartyRefBusinessEntity::getBusinessId, businessId).last("LIMIT 1").one();
    }

    @Override
    public void removeByBusinessId(String businessId , String businessType) {
        if (StringUtils.isNotBlank(businessId)) {
            this.lambdaUpdate().eq(ThirdpartyRefBusinessEntity::getBusinessType, businessType).eq(ThirdpartyRefBusinessEntity::getBusinessId, businessId).remove();
        }
    }
}
