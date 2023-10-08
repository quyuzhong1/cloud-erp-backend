package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.server.bi.mapper.DmpSkuInfoMapper;
import com.erp.server.bi.service.DmpSkuInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class DmpSkuInfoServiceImpl extends ServiceImpl<DmpSkuInfoMapper, DmpSkuInfoEntity>
    implements DmpSkuInfoService {


    /**
     * 根据sku查询商品信息
     *
     * @param skuNo     商品sku
     * @param companyId
     * @return com.erp.model.dmp.entity.DmpSkuInfoEntity
     **/
    @Override
    public DmpSkuInfoEntity getBySkuNo(String skuNo, String companyId) {
        return lambdaQuery()
                .eq(DmpSkuInfoEntity::getSkuNo, skuNo)
                .eq(StringUtils.isNotBlank(companyId), DmpSkuInfoEntity::getCompanyId, companyId)
                .last(" LIMIT 1")
                .one();
    }
}




