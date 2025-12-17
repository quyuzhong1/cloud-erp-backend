package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.FirstMileSkuCostAllocationDetailDTO;
import com.erp.model.tms.entity.FirstMileSkuCostAllocationDetailEntity;
import com.erp.model.tms.entity.FirstMileWeightAllocationEntity;
import com.erp.server.tms.mapper.FirstMileSkuCostAllocationDetailMapper;
import com.erp.server.tms.service.FirstMileSkuCostAllocationDetailService;
import com.erp.server.tms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
 * 头程费用SKU分摊明细 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
 */
@Slf4j
@Service
public class FirstMileSkuCostAllocationDetailServiceImpl extends SuperServiceImpl<FirstMileSkuCostAllocationDetailMapper, FirstMileSkuCostAllocationDetailEntity> implements FirstMileSkuCostAllocationDetailService {
    public static final String MSG = "头程费用SKU分摊明细";
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FirstMileSkuCostAllocationDetailDTO.AddDTO addDTO) {
        FirstMileSkuCostAllocationDetailEntity firstMileSkuCostAllocationDetailEntity = new FirstMileSkuCostAllocationDetailEntity();
        BeanMapperUtils.copy(addDTO, firstMileSkuCostAllocationDetailEntity);


        log.info("开始新增头程费用SKU分摊明细");
        boolean save = super.save(firstMileSkuCostAllocationDetailEntity);
        if(!save) {
            throw new ServiceException("头程费用SKU分摊明细保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), MSG, firstMileSkuCostAllocationDetailEntity.getId());

        operateLogService.addModuleOperateLog(msg, null, firstMileSkuCostAllocationDetailEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(firstMileSkuCostAllocationDetailEntity.getId(), firstMileSkuCostAllocationDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FirstMileSkuCostAllocationDetailDTO.UpdateDTO updateDTO) {
        FirstMileSkuCostAllocationDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, MSG));
        FirstMileSkuCostAllocationDetailEntity firstMileSkuCostAllocationDetailEntity =  BeanMapperUtils.map(FirstMileSkuCostAllocationDetailEntity.class, updateDTO);

        log.info("编辑 开始修改头程费用SKU分摊明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(firstMileSkuCostAllocationDetailEntity);
        if(!save) {
            throw new ServiceException("头程费用SKU分摊明细保存失败");
        }

        // 记录主单操作日志
            log.info("编辑 开始记录头程费用SKU分摊明细日志数据，id：【{}】", firstMileSkuCostAllocationDetailEntity.getId());
            String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), firstMileSkuCostAllocationDetailEntity.getId(), MSG);

        operateLogService.addModuleOperateLogByObj(old, firstMileSkuCostAllocationDetailEntity, null, firstMileSkuCostAllocationDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void removeByMainId(String id) {
        if (!CharSequenceUtil.isBlank(id)) {
            this.lambdaUpdate().eq(FirstMileSkuCostAllocationDetailEntity::getMainId, id).remove();
        }
    }

    @Override
    public List<FirstMileSkuCostAllocationDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)){
            return Collections.emptyList();
        }
        return baseMapper.listByMainIds(mainIds);
    }

    @Override
    public void updateEndPeriodTransitCost(String detailId, String newEndPeriodTransitCost) {
        if (CharSequenceUtil.isBlank(detailId) || Objects.isNull(newEndPeriodTransitCost)){
            return;
        }
        this.lambdaUpdate().set(FirstMileSkuCostAllocationDetailEntity::getEndPeriodTransitCost, new BigDecimal(newEndPeriodTransitCost)).eq(FirstMileSkuCostAllocationDetailEntity::getId, detailId).update();
    }

    @Override
    public void updateEndPeriodEstimatedCost(String detailId, String newEndPeriodEstimatedCost) {
        if (CharSequenceUtil.isBlank(detailId) || Objects.isNull(newEndPeriodEstimatedCost)){
            return;
        }
        this.lambdaUpdate().set(FirstMileSkuCostAllocationDetailEntity::getEndPeriodEstimatedCost, new BigDecimal(newEndPeriodEstimatedCost)).eq(FirstMileSkuCostAllocationDetailEntity::getId, detailId).update();
    }

    @Override
    public List<FirstMileSkuCostAllocationDetailEntity> listBySourceCodeList(List<String> businessCodeList, List<String> sourceCodeList, List<String> transportNoList) {
        if (CollUtil.isEmpty(businessCodeList) && CollUtil.isEmpty(sourceCodeList) && CollUtil.isEmpty(transportNoList)){
            return Collections.emptyList();
        }
        return baseMapper.listBySourceCodeList(businessCodeList,sourceCodeList,transportNoList);
    }

    @Override
    public void updateDetailRemark(String detailId, String newDetailRemark) {
        if (CharSequenceUtil.isBlank(detailId) || Objects.isNull(newDetailRemark)){
            return;
        }
        this.lambdaUpdate().set(FirstMileSkuCostAllocationDetailEntity::getRemark, newDetailRemark).eq(FirstMileSkuCostAllocationDetailEntity::getId, detailId).update();

    }

    @Override
    public List<FirstMileSkuCostAllocationDetailEntity> listByReportMonth(String sourceId, String businessCode, String transportNo, String skuId, String platformSkuNo, String reportPeriodId) {
        return baseMapper.listByReportMonth(sourceId,businessCode,transportNo,skuId,platformSkuNo,reportPeriodId);
    }

    @Override
    public List<FirstMileSkuCostAllocationDetailEntity> listByCostIdList(List<String> costIdList) {
        if (CollUtil.isEmpty(costIdList)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(FirstMileSkuCostAllocationDetailEntity::getCostMainId,costIdList).list();
    }

}
