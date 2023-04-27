package com.erp.server.bi.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.bi.entity.BiProductInfoEntity;
import com.erp.server.bi.mapper.BiProductInfoMapper;
import com.erp.server.bi.service.BiProductInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 产品信息表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-21
 */
@Service
public class BiProductInfoServiceImpl extends SuperServiceImpl<BiProductInfoMapper, BiProductInfoEntity> implements BiProductInfoService {

    /**
     * 获取品牌信息
     *
     * @param brandList
     * @return
     */
    @Override
    public List<BiProductInfoEntity> getbrandList(List<String> brandList) {
        if (CollectionUtils.isEmpty(brandList)) {
            return this.list();
        }
        return this.lambdaQuery().in(BiProductInfoEntity::getBrandName,brandList).list();
    }
}
