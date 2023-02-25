package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductAttestationEntity;
import com.erp.server.plm.mapper.ProductAttestationMapper;
import com.erp.server.plm.service.ProductAttestationService;
import org.springframework.stereotype.Service;

/**
 * 产品认证信息表(ProductAttestation)表服务实现类
 *
 * @author yl
 * @since 2023-02-25 12:56:17
 */
@Service
public class ProductAttestationServiceImpl extends ServiceImpl<ProductAttestationMapper, ProductAttestationEntity> implements ProductAttestationService {

}
