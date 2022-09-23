package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProductCertificateDTO;
import com.erp.model.plm.dto.ProductCertificateShowDTO;
import com.erp.model.plm.dto.ProductPackDTO;
import com.erp.model.plm.dto.ProductPackShowDTO;
import com.erp.model.plm.entity.ProductCertificateEntity;

import java.util.List;

/**
 * @Description 产品证书信息服务类
 * @Author Luo_WG
 * @Date 2022/9/23 15:35
 **/
public interface ProductCertificateService extends IService<ProductCertificateEntity> {
    /**
     * @Description 产品证书信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductCertificateShowDTO>
     **/
    List<ProductCertificateShowDTO> list(String productId);

    /**
     * @Description 保存/修改产品证书信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productCertificateDTO 产品证书信息表
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdate(ProductCertificateDTO productCertificateDTO);
}
