package com.erp.server.sys.service.impl;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.sys.entity.DictThirdCity;
import com.erp.server.sys.mapper.DictThirdCityMapper;
import com.erp.server.sys.service.DictThirdCityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 第三方城市字典表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2023-11-23
 */
@Slf4j
@Service
public class DictThirdCityServiceImpl extends SuperServiceImpl<DictThirdCityMapper, DictThirdCity> implements DictThirdCityService {

    @Override
    public boolean saveOrUpdateByRegionId(DictThirdCity entity) {
        LambdaUpdateWrapper<DictThirdCity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DictThirdCity::getRegionId, entity.getRegionId());
        return this.saveOrUpdate(entity,updateWrapper);
    }

    @Override
    public List<DictThirdCity> listByDictIdList(List<String> dictIds) {
        if (CollectionUtils.isEmpty(dictIds)){
            return Collections.emptyList();
        }
        return lambdaQuery()
                .in(DictThirdCity::getDictCityId, dictIds)
                .list();
    }
}
