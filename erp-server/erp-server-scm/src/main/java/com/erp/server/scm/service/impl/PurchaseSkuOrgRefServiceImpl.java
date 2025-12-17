package com.erp.server.scm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.model.scm.entity.PurchaseSkuOrgRefEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.scm.mapper.PurchaseSkuOrgRefMapper;
import com.erp.server.scm.service.DictBasicService;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.PurchasePriceDetailService;
import com.erp.server.scm.service.PurchaseSkuOrgRefService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.scm.dto.PurchaseSkuOrgRefDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * SKU与采购组织关系 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-05-28
 */
@Slf4j
@Service
public class PurchaseSkuOrgRefServiceImpl extends SuperServiceImpl<PurchaseSkuOrgRefMapper, PurchaseSkuOrgRefEntity> implements PurchaseSkuOrgRefService {
    @Resource
    private ModuleOperateLogService moduleOperateLogService;
    @Lazy
    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;
    @Resource
    private DictBasicService dictBasicService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PurchaseSkuOrgRefDTO.AddDTO addDTO) {
        PurchaseSkuOrgRefEntity purchaseSkuOrgRefEntity = new PurchaseSkuOrgRefEntity();
        BeanMapperUtils.copy(addDTO, purchaseSkuOrgRefEntity);

        // 数据处理
        handleData(purchaseSkuOrgRefEntity);

        log.info("开始新增SKU与采购组织关系");
        boolean save = super.save(purchaseSkuOrgRefEntity);
        if(!save) {
            throw new ServiceException("SKU与采购组织关系保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "SKU与采购组织关系" , purchaseSkuOrgRefEntity.getId());
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SKU_ORG_REF.getCode(), purchaseSkuOrgRefEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(purchaseSkuOrgRefEntity.getId(), purchaseSkuOrgRefEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PurchaseSkuOrgRefDTO.UpdateDTO addOrUpdateDTO) {
        PurchaseSkuOrgRefEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "SKU与采购组织关系"));
        PurchaseSkuOrgRefEntity purchaseSkuOrgRefEntity =  BeanMapperUtils.map(PurchaseSkuOrgRefEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(purchaseSkuOrgRefEntity);
        log.info("编辑 开始修改SKU与采购组织关系数据，id：【{}】", old.getId());
        boolean save = super.updateById(purchaseSkuOrgRefEntity);
        if(!save) {
            throw new ServiceException("SKU与采购组织关系保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录SKU与采购组织关系日志数据，id：【{}】", purchaseSkuOrgRefEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), purchaseSkuOrgRefEntity.getId(), "SKU与采购组织关系");
        moduleOperateLogService.addModuleOperateLogByObj(old, purchaseSkuOrgRefEntity, ModuleTypeEnum.SKU_ORG_REF.getCode(), purchaseSkuOrgRefEntity.getId(), "", msg);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addByPurchasePrice(PurchasePriceEntity entity) {
        if (!ApproveStatusEnum.APPROVE.equals(entity.getApproveStatus())){
            return;
        }
        //获取采购价目表明细
        List<PurchasePriceDetailEntity> detailEntityList = purchasePriceDetailService.listDetailByMainId(entity.getId());
        if (CollUtil.isEmpty(detailEntityList)){
            return;
        }
        List<String> skuIdList = detailEntityList.stream().map(PurchasePriceDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(skuIdList)){
            return;
        }
        List<PurchaseSkuOrgRefEntity> addList = new ArrayList<>();
        //清空sku对应的关联关系
         this.lambdaUpdate().in(PurchaseSkuOrgRefEntity::getSkuId, skuIdList).remove();
        //遍历采购价目表明细
        for (PurchasePriceDetailEntity detailEntity : detailEntityList) {
            //新增采购价目表明细
            PurchaseSkuOrgRefEntity purchaseSkuOrgRefEntity = new PurchaseSkuOrgRefEntity();
            purchaseSkuOrgRefEntity.setSkuId(detailEntity.getSkuId());
            purchaseSkuOrgRefEntity.setSkuNo(detailEntity.getSkuNo());
            purchaseSkuOrgRefEntity.setPurchaseOrgId(entity.getPurchaseOrgId());
            purchaseSkuOrgRefEntity.setPurchaseOrgName(entity.getPurchaseOrgName());
            addList.add(purchaseSkuOrgRefEntity);
        }
        if (CollUtil.isNotEmpty(addList)){
            this.saveBatch(addList);
        }
    }

    @Override
    public List<PurchaseSkuOrgRefEntity> getBySkuIdList(List<String> skuIdList) {
        if (CollUtil.isEmpty(skuIdList)){
            return Collections.emptyList();
        }
        List<PurchaseSkuOrgRefEntity> list = this.lambdaQuery().in(PurchaseSkuOrgRefEntity::getSkuId, skuIdList).list();
        //为空默认取值简拍组织
        List<DictBasicDTO> dictList = dictBasicService.getByKey("skuDefaultOrg");
        List<PurchaseSkuOrgRefEntity> resultList = new ArrayList<>();
        skuIdList.forEach(e ->{
            PurchaseSkuOrgRefEntity purchaseSkuOrgRefEntity = list.stream().filter(f -> f.getSkuId().equals(e)).findFirst().orElse(null);
            if (Objects.nonNull(purchaseSkuOrgRefEntity)){
                resultList.add(purchaseSkuOrgRefEntity);
            }
            if (Objects.isNull(purchaseSkuOrgRefEntity) && CollUtil.isNotEmpty(dictList)){
                purchaseSkuOrgRefEntity = new PurchaseSkuOrgRefEntity();
                purchaseSkuOrgRefEntity.setSkuId(e);
                purchaseSkuOrgRefEntity.setPurchaseOrgId(dictList.get(0).getValue());
                purchaseSkuOrgRefEntity.setPurchaseOrgName(dictList.get(0).getName());
                resultList.add(purchaseSkuOrgRefEntity);
            }
        });
        return resultList;
    }

    @Override
    public void removeByPrice(PurchasePriceEntity entity) {
        if (Objects.isNull(entity) || CharSequenceUtil.isBlank(entity.getPurchaseOrgId())){
            return;
        }
        //获取采购价目表明细
        List<PurchasePriceDetailEntity> detailEntityList = purchasePriceDetailService.listDetailByMainId(entity.getId());
        if (CollUtil.isEmpty(detailEntityList)){
            return;
        }
        List<String> skuIdList = detailEntityList.stream().map(PurchasePriceDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(skuIdList)){
            return;
        }
        //清空sku对应的关联关系
        this.lambdaUpdate().in(PurchaseSkuOrgRefEntity::getSkuId, skuIdList).eq(PurchaseSkuOrgRefEntity::getPurchaseOrgId, entity.getPurchaseOrgId()).remove();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(PurchaseSkuOrgRefEntity purchaseSkuOrgRefEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
