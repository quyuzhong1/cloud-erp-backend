package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderFinanceDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ReflectUtils;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cFinanceDTO;
import com.erp.model.oms.entity.*;
import com.erp.server.oms.convert.B2cOrderConsumerConverter;
import com.erp.server.oms.mapper.SoB2cFinanceMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cFinanceService;
import com.erp.server.oms.service.SoB2cService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * B2C销售订单财务信息表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-09-08
 */
@Slf4j
@Service
public class SoB2cFinanceServiceImpl extends SuperServiceImpl<SoB2cFinanceMapper, SoB2cFinanceEntity> implements SoB2cFinanceService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private SoB2cService soB2cService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(SoB2cFinanceDTO.AddDTO addDTO) {
        SoB2cFinanceEntity soB2cFinanceEntity = new SoB2cFinanceEntity();
        BeanMapperUtils.copy(addDTO, soB2cFinanceEntity);

        // 数据处理
        handleData(soB2cFinanceEntity);

        log.info("开始新增B2C销售订单财务信息单");
        boolean save = super.save(soB2cFinanceEntity);
        if(!save) {
            throw new ServiceException("B2C销售订单财务信息单保存失败");
        }
        return soB2cFinanceEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoB2cFinanceDTO.UpdateDTO updateDTO) {
        SoB2cFinanceEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单财务信息单"));
        SoB2cFinanceEntity soB2cFinanceEntity =  BeanMapperUtils.map(SoB2cFinanceEntity.class, updateDTO);

        // 数据处理
        handleData(soB2cFinanceEntity);
        log.info("编辑 开始修改B2C销售订单财务信息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(soB2cFinanceEntity);
        if(!save) {
            throw new ServiceException("B2C销售订单财务信息单保存失败");
        }

        return Boolean.TRUE;
    }

    @Override
    public SoB2cFinanceEntity getByMainId(String mainId) {
        return  lambdaQuery().eq(SoB2cFinanceEntity::getMainId,mainId).one();
    }

    @Override
    public List<SoB2cFinanceEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(SoB2cFinanceEntity::getMainId,mainIds).list();
    }


    @Override
    public Boolean deleteByMainIds(List<String> mainIds) {
        return lambdaUpdate().in(SoB2cFinanceEntity::getMainId,mainIds).remove();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @GlobalTransactional(rollbackFor = Exception.class)
    public void saveOrUpdateEntity(PlatformOrderDTO dto, SoB2cEntity mainEntity, SoB2cLogisticsEntity logisticsEntity, List<SoB2cDetailEntity> detailList) {
        if (Objects.isNull(mainEntity) || StrUtil.isBlank(mainEntity.getId())) return;
        PlatformOrderFinanceDTO financeDTO = dto.getFinances();

        if (null == financeDTO){
            //获取主表下物流记录
            SoB2cFinanceEntity oldEntity = getByMainId(mainEntity.getId());
            if( null == oldEntity ){
                SoB2cFinanceEntity entity = B2cOrderConsumerConverter.INSTANCE.convertNewFinance(null, mainEntity.getId());
                // 无信息新增空表
                if (!this.save(entity)){
                    throw new ServiceException("[SoB2cLogisticsEntity] 保存失败");
                }
            }
            return;
        }
        //获取主表下物流记录
        SoB2cFinanceEntity oldEntity = getByMainId(mainEntity.getId());
        if (null == oldEntity){
            //新增
            SoB2cFinanceEntity entity = B2cOrderConsumerConverter.INSTANCE.convertNewFinance(financeDTO, mainEntity.getId());
            // 补充数据
            fillAndHandleData(mainEntity, logisticsEntity, detailList, financeDTO, entity, true);
            log.info("消费:开始新增B2C销售订单财务信息单");
            if (!this.save(entity)){
                throw new ServiceException("[SoB2cFinanceEntity] 保存失败");
            }
        }else {
            // 更新
            SoB2cFinanceEntity newEntity = B2cOrderConsumerConverter.INSTANCE.convertUpdateFinance(oldEntity, financeDTO);
            // 历史数据修复
//            if (!BusinessCommonConstants.hasProfile("prod")){
//                fillAndHandleData(mainEntity, logisticsEntity, detailList, financeDTO, newEntity, false);
//            }
            // 指定字段有值不更新
            ReflectUtils.updateSpecifiedFieldsIfNotValue(newEntity, oldEntity, SoB2cFinanceEntity.fieldsExistNotUpdate());
            if (!this.updateById(newEntity)){
                throw new ServiceException("[SoB2cFinanceEntity] 更新失败");
            }
        }
    }

    private void fillAndHandleData(SoB2cEntity mainEntity, SoB2cLogisticsEntity logisticsEntity, List<SoB2cDetailEntity> detailList, PlatformOrderFinanceDTO financeDTO, SoB2cFinanceEntity newEntity, Boolean isAdd) {
        SoB2cDTO.FinancialParamDTO paramDTO = new SoB2cDTO.FinancialParamDTO();
        paramDTO.setId(mainEntity.getId());
        paramDTO.setIsCny(CurrencyEnum.CNY.getCurrencyCode().equalsIgnoreCase(financeDTO.getCurrency()));
        paramDTO.setSoB2cEntity(mainEntity);
        paramDTO.setSoB2cLogisticsEntity(logisticsEntity);
        paramDTO.setSoB2cDetailList(detailList);
        paramDTO.setSoB2cFinanceEntity(newEntity);
        SoB2cDTO.FinancialInfoDTO financialInfoDTO = soB2cService.getFinancialInfo(paramDTO, isAdd);
        SoB2cFinanceDTO.AddDTO addDTO = BeanMapperUtils.map(SoB2cFinanceDTO.AddDTO.class, financialInfoDTO);
        addDTO.setMainId(mainEntity.getId());
        BeanMapperUtils.copy(addDTO, newEntity);
        // 平台参数优先
        if (null != financeDTO.getLogisticsCost() && financeDTO.getLogisticsCost().compareTo(BigDecimal.ZERO) > 0){
            newEntity.setLogisticsCost(financeDTO.getLogisticsCost());
        }
        if (null != financeDTO.getPlatformRate() && financeDTO.getPlatformRate().compareTo(BigDecimal.ZERO) > 0){
            newEntity.setPlatformRate(financeDTO.getPlatformRate());
        }
        if (null != financeDTO.getTransferRate() && financeDTO.getTransferRate().compareTo(BigDecimal.ZERO) > 0){
            newEntity.setTransferRate(financeDTO.getTransferRate());
        }
        if (null != financeDTO.getVatRate() && financeDTO.getVatRate().compareTo(BigDecimal.ZERO) > 0){
            newEntity.setVatRate(financeDTO.getTransferRate());
        }
        if (null != financeDTO.getPlatformCost() && financeDTO.getPlatformCost().compareTo(BigDecimal.ZERO) > 0){
            newEntity.setPlatformCost(financeDTO.getPlatformCost());
        }
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cFinanceEntity soB2cFinanceEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
