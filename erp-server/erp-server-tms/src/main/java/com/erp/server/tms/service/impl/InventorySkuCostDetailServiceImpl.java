package com.erp.server.tms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.tms.dto.InventorySkuCostDTO;
import com.erp.model.tms.dto.InventorySkuCostDetailDTO;
import com.erp.model.tms.entity.InventorySkuCostDetailEntity;
import com.erp.model.tms.entity.InventorySkuCostEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.tms.mapper.InventorySkuCostDetailMapper;
import com.erp.server.tms.service.InventorySkuCostDetailService;
import com.erp.server.tms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * SKU成本明细 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-08-16
 */
@Slf4j
@Service
public class InventorySkuCostDetailServiceImpl extends SuperServiceImpl<InventorySkuCostDetailMapper, InventorySkuCostDetailEntity> implements InventorySkuCostDetailService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(InventorySkuCostDetailDTO.AddDTO addDTO) {
        InventorySkuCostDetailEntity inventorySkuCostDetailEntity = new InventorySkuCostDetailEntity();
        BeanMapperUtils.copy(addDTO, inventorySkuCostDetailEntity);

        // 数据处理
        handleData(inventorySkuCostDetailEntity);

        log.info("开始新增SKU成本明细");
        boolean save = super.save(inventorySkuCostDetailEntity);
        if (!save) {
            throw new ServiceException("SKU成本明细保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "SKU成本明细", inventorySkuCostDetailEntity.getId());
        
        operateLogService.addModuleOperateLog(msg, null, inventorySkuCostDetailEntity.getId(), "新增操作");
        

        return new BaseResultDTO.AddDTO(inventorySkuCostDetailEntity.getId(), inventorySkuCostDetailEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(InventorySkuCostDetailDTO.UpdateDTO updateDTO) {
        InventorySkuCostDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "SKU成本明细"));
        InventorySkuCostDetailEntity inventorySkuCostDetailEntity = BeanMapperUtils.map(InventorySkuCostDetailEntity.class, updateDTO);

        // 数据处理
        handleData(inventorySkuCostDetailEntity);
        log.info("编辑 开始修改SKU成本明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(inventorySkuCostDetailEntity);
        if (!save) {
            throw new ServiceException("SKU成本明细保存失败");
        }
        

        // 记录主单操作日志
        log.info("编辑 开始记录SKU成本明细日志数据，id：【{}】", inventorySkuCostDetailEntity.getId());
        String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), inventorySkuCostDetailEntity.getId(), "SKU成本明细");
        
        operateLogService.addModuleOperateLogByObj(old, inventorySkuCostDetailEntity, null, inventorySkuCostDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void removeByMainId(String id) {
        if (!CharSequenceUtil.isBlank(id)) {
            this.lambdaUpdate().eq(InventorySkuCostDetailEntity::getMainId, id).remove();
        }
    }

    @Override
    public List<InventorySkuCostDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(InventorySkuCostDetailEntity::getMainId, mainIds).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void buildDetail(List<InventorySkuCostDetailEntity> detailEntityList, InventorySkuCostEntity entity) {
        if (CollectionUtils.isEmpty(detailEntityList)) {
            //明细为空则清空
            lambdaUpdate().eq(InventorySkuCostDetailEntity::getMainId, entity.getId()).remove();
            return;
        }
        List<String> newDetailIds = detailEntityList.stream().filter(e -> Objects.nonNull(e) && CharSequenceUtil.isNotBlank(e.getId())).map(InventorySkuCostDetailEntity::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(newDetailIds)) {
            //明细为空则清空
            lambdaUpdate().eq(InventorySkuCostDetailEntity::getMainId, entity.getId()).remove();
        }
        //检查数据是否已存在
        List<String> skuIds = detailEntityList.stream().map(InventorySkuCostDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<InventorySkuCostDTO.PagingVO> existDetailEntityList = this.listDetailBySkuIds(skuIds);
        detailEntityList.forEach(detailEntity -> {
            if (!CollectionUtils.isEmpty(existDetailEntityList)){
                InventorySkuCostDTO.PagingVO pagingVO = existDetailEntityList.stream().filter(e -> Objects.nonNull(e)
                        && !Objects.equals(entity.getId(), e.getId())
                        && Objects.equals(e.getCompanyId(), entity.getCompanyId())
                        && Objects.equals(e.getAllocatedMonth(), entity.getAllocatedMonth())
                        && Objects.equals(e.getSkuId(), detailEntity.getSkuId())
                        && Objects.equals(e.getSkuNo(), detailEntity.getSkuNo())).findFirst().orElse(null);
                if (Objects.nonNull(pagingVO)){
                    throw new ServiceException(CharSequenceUtil.format("SKU成本中【{}】成本组织【{}】SKU【{}】分摊月份【{}】已存在", pagingVO.getCode(), entity.getCompanyName(),pagingVO.getSkuNo(),pagingVO.getAllocatedMonthStr()));
                }
            }
        });
        List<InventorySkuCostDetailEntity> oldDetailEntityList = this.listByMainIds(Collections.singletonList(entity.getId()));
        if (!CollectionUtils.isEmpty(oldDetailEntityList)) {
            List<String> oldDetailIds = oldDetailEntityList.stream().map(InventorySkuCostDetailEntity::getId).distinct().collect(Collectors.toList());
            List<String> notExistDetailIds = oldDetailIds.stream().filter(e -> !newDetailIds.contains(e)).distinct().collect(Collectors.toList());
            //清空不存在的明细记录
            this.removeByIds(notExistDetailIds);
        }
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);
        detailEntityList.forEach(inventorySkuCostDetailEntity -> {
            inventorySkuCostDetailEntity.setMainId(entity.getId());
            if (CharSequenceUtil.isBlank(inventorySkuCostDetailEntity.getUnit())){
                inventorySkuCostDetailEntity.setUnit("Pcs");
            }
            SkuVO skuVO = skuVOList.stream().filter(e -> e.getSkuId().equals(inventorySkuCostDetailEntity.getSkuId())).findFirst().orElse(null);
            if (Objects.nonNull(skuVO)){
                inventorySkuCostDetailEntity.setProductName(skuVO.getSkuName());
            }
        });
        this.saveOrUpdateBatch(detailEntityList);
    }

    @Override
    public List<InventorySkuCostDTO.PagingVO> listDetailBySkuIds(List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)){
            return Collections.emptyList();
        }
        return baseMapper.listDetailBySkuIds(skuIds);
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(InventorySkuCostDetailEntity inventorySkuCostDetailEntity) {
        
    }
}
