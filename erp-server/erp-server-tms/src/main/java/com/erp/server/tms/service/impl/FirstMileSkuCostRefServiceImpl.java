package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.FirstMileSkuCostAllocationEntity;
import com.erp.model.tms.entity.FirstMileSkuCostRefEntity;
import com.erp.server.tms.mapper.FirstMileSkuCostRefMapper;
import com.erp.server.tms.service.FirstMileSkuCostRefService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.FirstMileSkuCostRefDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.util.CollectionUtils;

/**
 * <p>
 * sku成本关系记录 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-08-24
 */
@Slf4j
@Service
public class FirstMileSkuCostRefServiceImpl extends SuperServiceImpl<FirstMileSkuCostRefMapper, FirstMileSkuCostRefEntity> implements FirstMileSkuCostRefService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FirstMileSkuCostRefDTO.AddDTO addDTO) {
        FirstMileSkuCostRefEntity firstMileSkuCostRefEntity = new FirstMileSkuCostRefEntity();
        BeanMapperUtils.copy(addDTO, firstMileSkuCostRefEntity);

        // 数据处理
        handleData(firstMileSkuCostRefEntity);

        log.info("开始新增sku成本关系记录");
        boolean save = super.save(firstMileSkuCostRefEntity);
        if(!save) {
            throw new ServiceException("sku成本关系记录保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "sku成本关系记录" , firstMileSkuCostRefEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, firstMileSkuCostRefEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(firstMileSkuCostRefEntity.getId(), firstMileSkuCostRefEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FirstMileSkuCostRefDTO.UpdateDTO updateDTO) {
        FirstMileSkuCostRefEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "sku成本关系记录"));
        FirstMileSkuCostRefEntity firstMileSkuCostRefEntity =  BeanMapperUtils.map(FirstMileSkuCostRefEntity.class, updateDTO);

        // 数据处理
        handleData(firstMileSkuCostRefEntity);
        log.info("编辑 开始修改sku成本关系记录数据，id：【{}】", old.getId());
        boolean save = super.updateById(firstMileSkuCostRefEntity);
        if(!save) {
            throw new ServiceException("sku成本关系记录保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录sku成本关系记录日志数据，id：【{}】", firstMileSkuCostRefEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), firstMileSkuCostRefEntity.getId(), "sku成本关系记录");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, firstMileSkuCostRefEntity, null, firstMileSkuCostRefEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateRef(List<String> skuCostDetailIds, String id) {
        if(CollectionUtils.isEmpty(skuCostDetailIds) || StrUtil.isBlank(id)){
            return;
        }
        List<FirstMileSkuCostRefEntity> list = this.lambdaQuery().eq(FirstMileSkuCostRefEntity::getFirstMileSkuAllocationId, id).in(FirstMileSkuCostRefEntity::getSkuCostDetailId, skuCostDetailIds).list();
        List<FirstMileSkuCostRefEntity> createList = new ArrayList<>();
        for (String skuCostDetailId : skuCostDetailIds){
            //存在已记录数据就排除
            FirstMileSkuCostRefEntity exist = list.stream().filter(e -> Objects.nonNull(e) && Objects.equals(skuCostDetailId, e.getSkuCostDetailId())).findFirst().orElse(null);
            if (Objects.nonNull(exist)){
                continue;
            }
            createList.add(new FirstMileSkuCostRefEntity().setSkuCostDetailId(skuCostDetailId).setFirstMileSkuAllocationId(id));
        }
        if (!CollectionUtils.isEmpty(createList)){
            this.saveBatch(createList);
        }
    }

    @Override
    public void removeBySkuCostAllocation(List<FirstMileSkuCostAllocationEntity> skuCostAllocationEntityList) {
        if (CollectionUtils.isEmpty(skuCostAllocationEntityList)){
            return;
        }
        List<String> ids = skuCostAllocationEntityList.stream().map(FirstMileSkuCostAllocationEntity::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)){
            return;
        }
        this.lambdaUpdate().in(FirstMileSkuCostRefEntity::getFirstMileSkuAllocationId, ids).remove();
    }

    @Override
    public List<FirstMileSkuCostRefEntity> listBySkuCostDetailIds(List<String> detailIds) {
        if (CollectionUtils.isEmpty(detailIds)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(FirstMileSkuCostRefEntity::getSkuCostDetailId, detailIds).list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(FirstMileSkuCostRefEntity firstMileSkuCostRefEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
