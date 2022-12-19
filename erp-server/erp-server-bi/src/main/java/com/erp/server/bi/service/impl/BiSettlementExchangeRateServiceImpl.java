package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.bi.dto.BiSettlementExchangeRateDTO;
import com.erp.model.dmp.entity.BiSettlementExchangeRateEntity;
import com.erp.server.bi.mapper.BiSettlementExchangeRateMapper;
import com.erp.server.bi.service.BiSettlementExchangeRateService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/19 10:00
 */
@Service
public class BiSettlementExchangeRateServiceImpl extends ServiceImpl<BiSettlementExchangeRateMapper, BiSettlementExchangeRateEntity>
        implements BiSettlementExchangeRateService {


    @Override
    public Boolean batchAddSettlementExchangeRate(List<BiSettlementExchangeRateDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.Default);
        }
        for (BiSettlementExchangeRateDTO dto:list) {
            //验证同币别、日期是否已存在汇率
            checkExchangeRate(dto);
            BiSettlementExchangeRateEntity entity = new BiSettlementExchangeRateEntity();
            BeanMapperUtils.copy(dto,entity);
            boolean flag = this.save(entity);
            if (flag) {
                //同步订单、退款、退货中的结算汇率
            }
        }
        return true;
    }

    /**
     * 新增验证是否重复
     */
    private void checkExchangeRate(BiSettlementExchangeRateDTO dto) {
        BiSettlementExchangeRateEntity entity = getOneByParams(dto);
        if (ObjectUtils.isNotEmpty(entity)) {
            throw new ServiceException(500,String.format("源币种[%s],目标币种[%s],结算日期[%s]汇率已存在",dto.getTargetCurrencyCode(),dto.getTargetCurrencyCode(),dto.getSettlementDate()));
        }
    }

    /**
     * 根据目标币种、源币种、日期查询
     */
    private BiSettlementExchangeRateEntity getOneByParams(BiSettlementExchangeRateDTO dto) {
        LambdaQueryWrapper<BiSettlementExchangeRateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiSettlementExchangeRateEntity::getSourceCurrencyCode,dto.getSourceCurrencyCode());
        queryWrapper.eq(BiSettlementExchangeRateEntity::getTargetCurrencyCode,dto.getSourceCurrencyCode());
        queryWrapper.eq(BiSettlementExchangeRateEntity::getSettlementDate,dto.getSettlementDate());
        queryWrapper.last("limit 1");
        return  this.getOne(queryWrapper);
    }
}
