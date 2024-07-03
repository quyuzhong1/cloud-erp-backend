package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.BiSkuInfoEntity;
import com.erp.server.bi.mapper.DmpSkuInfoMapper;
import com.erp.server.bi.service.DmpSkuInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class DmpSkuInfoServiceImpl extends ServiceImpl<DmpSkuInfoMapper, BiSkuInfoEntity>
    implements DmpSkuInfoService {


    /**
     * 根据sku查询商品信息
     *
     * @param skuNo     商品sku
     * @param companyId
     * @return com.erp.model.dmp.entity.DmpSkuInfoEntity
     **/
    @Override
    public BiSkuInfoEntity getBySkuNo(String skuNo, String companyId) {
        return lambdaQuery()
                .eq(BiSkuInfoEntity::getSkuNo, skuNo)
                .eq(StringUtils.isNotBlank(companyId), BiSkuInfoEntity::getCompanyId, companyId)
                .last(" LIMIT 1")
                .one();
    }
}




