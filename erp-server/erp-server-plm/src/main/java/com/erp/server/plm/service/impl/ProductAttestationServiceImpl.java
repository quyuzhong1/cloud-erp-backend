package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.AttestationDTO;
import com.erp.model.plm.dto.ProductAttestationDTO;
import com.erp.model.plm.entity.ProductAttestationEntity;
import com.erp.server.plm.constant.ProductManyDetailConstant;
import com.erp.server.plm.mapper.ProductAttestationMapper;
import com.erp.server.plm.service.ProductAttestationService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            List<AttestationDTO> attestationList = new ArrayList<>(10);
            for (ProductAttestationDTO item : productAttestationList) {
                //产品认证
                List<AttestationDTO> productList = item.getProductList();
                for (AttestationDTO product : productList) {
                    product.setSkuId(item.getSkuId());
                    product.setType(ProductManyDetailConstant.PRODUCT_ATTESTATION);

                }
                attestationList.addAll(productList);
                //其它认证
                List<AttestationDTO> otherList = item.getOtherList();
                for (AttestationDTO other : otherList) {
                    other.setSkuId(item.getSkuId());
                    other.setType(ProductManyDetailConstant.OTHER_ATTESTATION);
                }
                attestationList.addAll(otherList);

                //运输认证
                List<AttestationDTO> transportList = item.getTransportList();
                for (AttestationDTO transport : transportList) {
                    transport.setSkuId(item.getSkuId());
                    transport.setType(ProductManyDetailConstant.TRANSPORT_ATTESTATION);
                }
                attestationList.addAll(transportList);
            }

            List<ProductAttestationEntity> list = BeanMapper.copyList(attestationList, ProductAttestationEntity.class);
            return this.saveBatch(list);
        }
        return true;
    }

    @Override
    public List<ProductAttestationDTO> getByProductId(String productId) {
        List<ProductAttestationDTO> resultList = new ArrayList<>(10);
        List<ProductAttestationEntity> list = baseMapper.getByProductId(productId);
        if (CollectionUtils.isNotEmpty(list)) {
            //其它认证
            String other = ProductManyDetailConstant.OTHER_ATTESTATION;
            //产品认证
            String product = ProductManyDetailConstant.PRODUCT_ATTESTATION;
            //运输认证
            String transport = ProductManyDetailConstant.TRANSPORT_ATTESTATION;

            Map<String, List<ProductAttestationEntity>> map = list.parallelStream().
                    collect(Collectors.groupingBy(ProductAttestationEntity::getSkuId));
            for (Map.Entry<String, List<ProductAttestationEntity>> item : map.entrySet()) {
                String skuId = item.getKey();
                List<ProductAttestationEntity> valueList = item.getValue();
                ProductAttestationDTO attestation = new ProductAttestationDTO();
                attestation.setSkuId(skuId);
                //产品
                List<ProductAttestationEntity> productList = valueList.stream().filter(v -> product.equals(v.getType())).
                        collect(Collectors.toList());
                attestation.setProductList(BeanMapper.copyList(productList, AttestationDTO.class));
                //运输
                List<ProductAttestationEntity> transportList = valueList.stream().filter(v -> transport.equals(v.getType())).
                        collect(Collectors.toList());
                attestation.setTransportList(BeanMapper.copyList(transportList, AttestationDTO.class));

                //其它
                List<ProductAttestationEntity> otherList = valueList.stream().filter(v -> other.equals(v.getType())).
                        collect(Collectors.toList());
                attestation.setTransportList(BeanMapper.copyList(otherList, AttestationDTO.class));
                resultList.add(attestation);
            }

        }

        return resultList;
    }
}
