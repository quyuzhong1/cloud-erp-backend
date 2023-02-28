package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProductAttestationDTO;
import com.erp.model.plm.entity.ProductAttestationEntity;

import java.util.List;

/**
 * 产品认证信息表(ProductAttestation)表服务接口
 *
 * @author yl
 * @since 2023-02-25 12:56:16
 */
public interface ProductAttestationService  extends IService<ProductAttestationEntity> {

    /**
     * 保存或者修改 产品认证信息
     * @author yl
     * @date 2023-02-27 9:13
     * @param productAttestationList
     * @return java.lang.Boolean
     */
    Boolean saveOrUpdateBatchAttestation(List<ProductAttestationDTO> productAttestationList);

    /**
     * 根据产品id 获取 产品认证信息
     * @author yl
     * @date 2023-02-27 10:55
     * @param productId
     * @return java.util.List<com.erp.model.plm.dto.ProductAttestationDTO>
     */
    List<ProductAttestationDTO> getByProductId(String productId);

    List<ProductAttestationEntity> getListByIds(List<String> ids);

    List<ProductAttestationEntity> getBySkuIds(List<String> skuIds);
}
