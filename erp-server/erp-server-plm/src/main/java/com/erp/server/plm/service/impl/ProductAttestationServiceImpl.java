package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.ProductAttestationDTO;
import com.erp.model.plm.entity.ProductAttestationEntity;
import com.erp.server.plm.mapper.ProductAttestationMapper;
import com.erp.server.plm.service.ProductAttestationService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 产品认证信息表(ProductAttestation)表服务实现类
 *
 * @author yl
 * @since 2023-02-25 12:56:17
 */
@Service
public class ProductAttestationServiceImpl extends ServiceImpl<ProductAttestationMapper, ProductAttestationEntity> implements ProductAttestationService {


    /**
     * 保存或者修改产品认证信息
     *
     * @param productAttestationList
     * @return
     */
    @Override
    public Boolean saveOrUpdateBatchAttestation(List<ProductAttestationDTO> productAttestationList) {
        if (CollectionUtils.isNotEmpty(productAttestationList)) {
            List<ProductAttestationEntity> list = BeanMapper.copyList(productAttestationList, ProductAttestationEntity.class);
            return this.saveBatch(list);
        }
        return true;
    }
}
