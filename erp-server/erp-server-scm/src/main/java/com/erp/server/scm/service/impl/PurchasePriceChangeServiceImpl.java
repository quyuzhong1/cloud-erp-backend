package com.erp.server.scm.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.server.scm.mapper.PurchasePriceChangeMapper;
import com.erp.server.scm.service.PurchasePriceChangeService;
import com.erp.server.scm.service.PurchasePriceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * <p>
 * 采购价变更表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class PurchasePriceChangeServiceImpl extends SuperServiceImpl<PurchasePriceChangeMapper, PurchasePriceChangeEntity> implements PurchasePriceChangeService {

    @Resource
    private PurchasePriceService purchasePriceService;

    /**
     * 添加采购价目变更
     *
     * @param dto
     * @return com.erp.model.scm.entity.PurchasePriceChangeEntity
     * @author yl
     * @date 2023-03-28 11:49
     */
    @Override
    public PurchasePriceChangeEntity add(PurchasePriceChangeDTO.AddDTO dto) {
        //采购价目表的id
        String priceId = dto.getPurchasePriceId();
        PurchasePriceEntity purchasePrice = purchasePriceService.getById(priceId);
        if (Objects.isNull(purchasePrice)) {

        }

        return null;
    }
}
