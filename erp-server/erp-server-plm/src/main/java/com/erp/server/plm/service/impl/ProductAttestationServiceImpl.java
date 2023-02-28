package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.AttestationDTO;
import com.erp.model.plm.dto.ProductAttestationDTO;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.entity.ProductAttestationEntity;
import com.erp.server.plm.constant.ProductManyDetailConstant;
import com.erp.server.plm.mapper.ProductAttestationMapper;
import com.erp.server.plm.service.BasicDictService;
import com.erp.server.plm.service.ProductAttestationService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 产品认证信息表(ProductAttestation)表服务实现类
 *
 * @author yl
 * @since 2023-02-25 12:56:17
 */
@Service
public class ProductAttestationServiceImpl extends ServiceImpl<ProductAttestationMapper, ProductAttestationEntity> implements ProductAttestationService {


    @Resource
    private BasicDictService basicDictService;

    /**
     * 保存或者修改产品认证信息
     *
     * @param productAttestationList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateBatchAttestation(List<ProductAttestationDTO> productAttestationList) {
        if (CollectionUtils.isNotEmpty(productAttestationList)) {
            List<AttestationDTO> attestationList = new ArrayList<>(10);
            for (ProductAttestationDTO item : productAttestationList) {
                //产品认证
                List<String> productDictList = item.getProductList();
                for (String productDict : productDictList) {
                    AttestationDTO product = new AttestationDTO();
                    product.setSkuId(item.getSkuId());
                    product.setType(ProductManyDetailConstant.PRODUCT_ATTESTATION);
                    product.setDictId(productDict);
                    attestationList.add(product);
                }

                //其它认证
                List<String> otherDictList = item.getOtherList();
                for (String otherDict : otherDictList) {
                    AttestationDTO other = new AttestationDTO();
                    other.setDictId(otherDict);
                    other.setSkuId(item.getSkuId());
                    other.setType(ProductManyDetailConstant.OTHER_ATTESTATION);
                    attestationList.add(other);
                }

                //运输认证
                List<String> transportDictList = item.getTransportList();
                for (String transportDict : transportDictList) {
                    AttestationDTO transport = new AttestationDTO();
                    transport.setSkuId(item.getSkuId());
                    transport.setType(ProductManyDetailConstant.TRANSPORT_ATTESTATION);
                    transport.setDictId(transportDict);
                    attestationList.add(transport);
                }

            }

            //这个是skuid
            List<String> skuIdList = productAttestationList.stream().map(ProductAttestationDTO::getSkuId).collect(Collectors.toList());
            //先删除 去掉的 认证
            removeBySkuIds(skuIdList);
            if(CollectionUtils.isNotEmpty(attestationList)){
                //对应的字典表信息
                List<String> dictIdList = attestationList.stream().map(AttestationDTO::getDictId).collect(Collectors.toList());
                List<BasicDictEntity> dictList = basicDictService.listByIds(dictIdList);
                for (AttestationDTO item : attestationList) {
                    String value = dictList.stream().filter(d -> d.getId().equals(item.getDictId())).
                            findFirst().flatMap(obj -> Optional.ofNullable(obj.getValue())).orElse("");
                    item.setValue(value);
                }
                List<ProductAttestationEntity> list = BeanMapper.copyList(attestationList, ProductAttestationEntity.class);
                return this.saveBatch(list);
            }
        }
        return true;
    }

    /**
     * @param skuIdList
     */
    private void removeBySkuIds(List<String> skuIdList) {
        if (CollectionUtils.isNotEmpty(skuIdList)) {
            LambdaQueryWrapper<ProductAttestationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(ProductAttestationEntity::getSkuId, skuIdList);
            this.remove(queryWrapper);
        }
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
                List<String> productList = valueList.stream().filter(v -> product.equals(v.getType())).
                        map(ProductAttestationEntity::getDictId).collect(Collectors.toList());
                attestation.setProductList(productList);
                //运输
                List<String> transportList = valueList.stream().filter(v -> transport.equals(v.getType())).
                        map(ProductAttestationEntity::getDictId).collect(Collectors.toList());
                attestation.setTransportList(transportList);

                //其它
                List<String> otherList = valueList.stream().filter(v -> other.equals(v.getType())).
                        map(ProductAttestationEntity::getDictId).collect(Collectors.toList());
                attestation.setOtherList(otherList);
                resultList.add(attestation);
            }

        }

        return resultList;
    }

    @Override
    public List<ProductAttestationEntity> getListByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ProductAttestationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProductAttestationEntity::getId, ids);
        return this.list(queryWrapper);
    }

    
    /**
     * 获取根据sku id
     * @author yl
     * @date 2023-02-28 11:19
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.entity.ProductAttestationEntity>
     */
    @Override
    public List<ProductAttestationEntity> getBySkuIds(List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ProductAttestationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProductAttestationEntity::getSkuId, skuIds);
        return this.list(queryWrapper);
    }
}
