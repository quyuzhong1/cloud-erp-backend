package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.wms.dto.QcProductDTO;
import com.erp.model.wms.entity.QcProductEntity;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.mapper.QcProductMapper;
import com.erp.server.wms.service.WmsAttachmentService;
import com.erp.server.wms.service.QcProductService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Service
public class QcProductServiceImpl extends SuperServiceImpl<QcProductMapper, QcProductEntity> implements QcProductService {

    @Resource
    private WmsAttachmentService wmsAttachmentService;

    /**
     * 质检产品信息 暂存
     *
     * @param billId
     * @param qcProduct
     * @return void
     * @author yl
     * @date 2023-04-19 9:45
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void draft(String billId, QcProductDTO.AddDTO qcProduct) {
        QcProductEntity qcProductEntity = new QcProductEntity();
        BeanMapper.copy(qcProduct, qcProductEntity);
        String id = qcProduct.getId();
        if (StringUtils.isBlank(id)) {
            id = IdWorker.getIdStr();
        }
        qcProductEntity.setMainId(billId);
        qcProductEntity.setId(id);
        //产品信息
        List<String> productImageNameList = qcProduct.getProductImageNameList();
        List<String> productImageUrlList = qcProduct.getProductImageUrlList();
        wmsAttachmentService.batchSave(productImageUrlList, productImageNameList, WmsConstant.QC_PRODUCT, id);
        //外箱信息
        List<String> boxImageNameList = qcProduct.getBoxImageNameList();
        List<String> boxImageUrlList = qcProduct.getBoxImageUrlList();
        wmsAttachmentService.batchSave(boxImageUrlList, boxImageNameList, WmsConstant.QC_BOX, id);
        this.saveOrUpdate(qcProductEntity);
    }
}

