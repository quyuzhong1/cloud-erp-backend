package com.erp.server.mrp.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.PurchaseSuggestDTO;
import com.erp.model.mrp.entity.DeliverySuggestEntity;
import com.erp.model.mrp.entity.PurchaseSuggestEntity;
import com.erp.server.mrp.mapper.PurchaseSuggestMapper;
import com.erp.server.mrp.service.DeliverySuggestService;
import com.erp.server.mrp.service.OperateLogService;
import com.erp.server.mrp.service.PurchaseSuggestMergeService;
import com.erp.server.mrp.service.PurchaseSuggestService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 建议采购 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-29
 */
@Slf4j
@Service
public class PurchaseSuggestServiceImpl extends SuperServiceImpl<PurchaseSuggestMapper, PurchaseSuggestEntity> implements PurchaseSuggestService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private DeliverySuggestService deliverySuggestService;

    @Resource
    private PurchaseSuggestMergeService purchaseSuggestMergeService;



    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PurchaseSuggestDTO.AddDTO addDTO) {
        PurchaseSuggestEntity purchaseSuggestEntity = new PurchaseSuggestEntity();
        BeanMapperUtils.copy(addDTO, purchaseSuggestEntity);

        //计划修正值默认给建议发货量
        purchaseSuggestEntity.setPlanPurchaseQty(purchaseSuggestEntity.getSuggestPurchaseQty());
        // 数据处理
        handleData(purchaseSuggestEntity);

        log.info("开始新增建议采购");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_P);
        purchaseSuggestEntity.setCode(code);
        boolean save = super.save(purchaseSuggestEntity);
        if(!save) {
            throw new ServiceException("建议采购保存失败");
        }
        //添加采购建议合并数据
        purchaseSuggestMergeService.generatePurchaseSuggestMerge(purchaseSuggestEntity.getId());

        return new BaseResultDTO.AddDTO(purchaseSuggestEntity.getId(), code);
    }


    @Override
    public List<PurchaseSuggestEntity> listGeneratePurchaseSuggestMerge(PurchaseSuggestEntity purchaseSuggestEntity) {
        return baseMapper.listGeneratePurchaseSuggestMerge(purchaseSuggestEntity);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(PurchaseSuggestEntity purchaseSuggestEntity) {
        //发货建议
        DeliverySuggestEntity deliverySuggestEntity = deliverySuggestService.getById(purchaseSuggestEntity.getId());
        if (ObjectUtil.isEmpty(deliverySuggestEntity)) {
            throw new ServiceException("发货建议不能为空");
        }
        purchaseSuggestEntity.setParentSkuId(CharSequenceUtil.equals(purchaseSuggestEntity.getSkuId(),deliverySuggestEntity.getSkuId()) ? "" : deliverySuggestEntity.getSkuId());
        purchaseSuggestEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
        purchaseSuggestEntity.setShopId(deliverySuggestEntity.getShopId());
        purchaseSuggestEntity.setPlatformType(deliverySuggestEntity.getPlatformType());
        purchaseSuggestEntity.setPlatform(deliverySuggestEntity.getPlatform());
    }

}
