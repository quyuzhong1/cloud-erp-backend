package com.erp.server.tms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.FirstMileSkuCostAllocationDTO;
import com.erp.model.tms.entity.FirstMileSkuCostAllocationEntity;
import com.erp.server.tms.mapper.FirstMileSkuCostAllocationMapper;
import com.erp.server.tms.service.FirstMileSkuCostAllocationService;
import com.erp.server.tms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 头程费用SKU分摊 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
 */
@Slf4j
@Service
public class FirstMileSkuCostAllocationServiceImpl extends SuperServiceImpl<FirstMileSkuCostAllocationMapper, FirstMileSkuCostAllocationEntity> implements FirstMileSkuCostAllocationService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FirstMileSkuCostAllocationDTO.AddDTO addDTO) {
        FirstMileSkuCostAllocationEntity firstMileSkuCostAllocationEntity = new FirstMileSkuCostAllocationEntity();
        BeanMapperUtils.copy(addDTO, firstMileSkuCostAllocationEntity);

        // 数据处理
        handleData(firstMileSkuCostAllocationEntity);

        log.info("开始新增头程费用SKU分摊");
        boolean save = super.save(firstMileSkuCostAllocationEntity);
        if(!save) {
            throw new ServiceException("头程费用SKU分摊保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "头程费用SKU分摊" , firstMileSkuCostAllocationEntity.getId());
        
        operateLogService.addModuleOperateLog(msg, null, firstMileSkuCostAllocationEntity.getId(), "新增操作");
        

        return new BaseResultDTO.AddDTO(firstMileSkuCostAllocationEntity.getId(), firstMileSkuCostAllocationEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FirstMileSkuCostAllocationDTO.UpdateDTO updateDTO) {
        FirstMileSkuCostAllocationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "头程费用SKU分摊"));
        FirstMileSkuCostAllocationEntity firstMileSkuCostAllocationEntity =  BeanMapperUtils.map(FirstMileSkuCostAllocationEntity.class, updateDTO);

        // 数据处理
        handleData(firstMileSkuCostAllocationEntity);
        log.info("编辑 开始修改头程费用SKU分摊数据，id：【{}】", old.getId());
        boolean save = super.updateById(firstMileSkuCostAllocationEntity);
        if(!save) {
            throw new ServiceException("头程费用SKU分摊保存失败");
        }
        

        // 记录主单操作日志
            log.info("编辑 开始记录头程费用SKU分摊日志数据，id：【{}】", firstMileSkuCostAllocationEntity.getId());
            String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), firstMileSkuCostAllocationEntity.getId(), "头程费用SKU分摊");
        
        operateLogService.addModuleOperateLogByObj(old, firstMileSkuCostAllocationEntity, null, firstMileSkuCostAllocationEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void removeByMainId(String id) {
        if (!CharSequenceUtil.isBlank(id)) {
            this.lambdaUpdate().eq(FirstMileSkuCostAllocationEntity::getMainId, id).remove();
        }
    }

    @Override
    public List<FirstMileSkuCostAllocationEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(FirstMileSkuCostAllocationEntity::getMainId,mainIds).list();
    }

    @Override
    public List<FirstMileSkuCostAllocationEntity> listByInitFirstMileDetailIds(List<String> initFirstMileDetailIds) {
        if (CollectionUtils.isEmpty(initFirstMileDetailIds)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(FirstMileSkuCostAllocationEntity::getInitFirstMileDetailId, initFirstMileDetailIds).list();
    }

    @Override
    public List<FirstMileSkuCostAllocationEntity> listByReconciliationDetailIds(List<String> detailIds) {
        if (CollectionUtils.isEmpty(detailIds)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(FirstMileSkuCostAllocationEntity::getReconciliationDetailId,detailIds).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(FirstMileSkuCostAllocationEntity firstMileSkuCostAllocationEntity) {
    
    }
}
