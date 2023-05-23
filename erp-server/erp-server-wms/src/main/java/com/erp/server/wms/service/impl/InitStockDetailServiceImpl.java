package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.inventory.InitStockDetailDTO;
import com.erp.model.wms.entity.InitStockDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.InitStockDetailMapper;
import com.erp.server.wms.service.InitStockDetailService;
import com.erp.server.wms.service.OperateLogService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Classname: InitStockDetailServiceImpl
 * @Description: 期初库存明细服务实现类
 * @CreateTime: 2023-05-11  10:31
 * @Author: zhangchunlin
 */
@Service
public class InitStockDetailServiceImpl extends SuperServiceImpl<InitStockDetailMapper, InitStockDetailEntity> implements InitStockDetailService {

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Override
    public List<InitStockDetailEntity> findList(String mainId) {
        List<InitStockDetailEntity> members = lambdaQuery().eq(InitStockDetailEntity::getMainId,mainId).list();
        if(CollUtil.isNotEmpty(members)) {
            members.stream().sorted(Comparator.comparing(InitStockDetailEntity::getId)).collect(Collectors.toList());
        }
        return members;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(List<InitStockDetailDTO.AddDTO> details, String mainId) {
        List<InitStockDetailEntity> list = BeanMapperUtils.copyList(InitStockDetailEntity.class, details);
        handleDetails(list, mainId, Boolean.FALSE);
        // 批量保存
        boolean save = this.saveBatch(list);
        ValidatorUtil.isTrue(save, ()->new ServiceException("期初库存明细保存失败"));
    }

    @Override
    public InitStockDetailEntity findDetail(String mainId, String skuId) {
        return lambdaQuery()
                .eq(InitStockDetailEntity::getMainId,mainId)
                .eq(InitStockDetailEntity::getSkuId,skuId)
                .one();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(List<InitStockDetailDTO.UpdateDTO> details, String mainId) {
        // 查询原明细数据
        List<InitStockDetailEntity> originMembers = this.findList(mainId);
        // 查询被删除的明细id（即新上传的id集合没有包含原始id的）
        List<String> originIds = originMembers.stream().map(InitStockDetailEntity::getId).collect(Collectors.toList());
        // 新上送的明细id集合（不包括空的）
        List<String> nowIds = details.stream().filter(r->StrUtils.isNotEmpty(r.getId())).map(InitStockDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        // 需要删除的id集合
        List<String> deleteIds = originIds.stream().filter(id->!nowIds.contains(id)).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(deleteIds)) {
            // 记录删除日志
            List<InitStockDetailEntity> deleteMembers = originMembers.stream().filter(r->deleteIds.contains(r.getId())).collect(Collectors.toList());
            List<Pair<String, String>> pairList = deleteMembers.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.INIT_STOCK.getCode(),pairList,"编辑操作");
            // 删除明细数据
            super.removeByIds(deleteIds);
        }
        // 新增或修改的明细数据
        List<InitStockDetailEntity> newList = BeanMapperUtils.copyList(InitStockDetailEntity.class, details);
        // 记录新增或修改日志
        handleDetails(newList, mainId, Boolean.TRUE);
        //新增或修改期初库存明细
        boolean save = this.saveOrUpdateBatch(newList);
        ValidatorUtil.isTrue(save, ()->new ServiceException("期初库存明细保存失败"));
    }

    @Override
    public Integer countCondition(String warehouseId, String warehouseLocation, String skuId, String mainId) {
        return this.baseMapper.countCondition(warehouseId, warehouseLocation, skuId, mainId);
    }

    @Override
    public Map<String, List<InitStockDetailEntity>> findListByIds(List<String> mainIds) {
        List<InitStockDetailEntity> details =  lambdaQuery().in(InitStockDetailEntity::getMainId, mainIds).list();
        // 按主单id分组
        Map<String, List<InitStockDetailEntity>> initStockDetailEntityMap = details.stream().collect(Collectors.groupingBy(InitStockDetailEntity::getMainId));
        return initStockDetailEntityMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByMainIds(List<String> mainIds) {
        lambdaUpdate().in(InitStockDetailEntity::getMainId, mainIds).remove();
    }


    public void handleDetails(List<InitStockDetailEntity> list, String mainId, Boolean isUpdate) {
        //添加操作日志
        List<InitStockDetailEntity> addList = list.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        List<String> skuIds = list.stream().map(InitStockDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuInfos = plmTaskFeign.getSkuInfoByIds(skuIds);
        Map<String,SkuVO> skuMap =  skuInfos.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        for(int i = 0, length = list.size();i < length;i++) {
            InitStockDetailEntity data = list.get(i);
            SkuVO skuVO = skuMap.get(data.getSkuId());
            if(Objects.isNull(skuVO)) {
                throw new ServiceException(StrUtil.format("第{}行SKU【{}】错误", (i + 1), data.getSkuNo()));
            }
            // 验证产品是否审核通过
            Integer skuStatus = skuVO.getStatus();
            if(!Objects.equals(skuStatus, ProductDetailStatusEnum.APPROVAL_PASS.getCode())) {
                throw new ServiceException(StrUtil.format("第{}行SKU【{}】未审核通过", (i + 1), data.getSkuNo()));
            }
            data.setMainId(mainId);
            data.setSkuNo(skuMap.get(data.getSkuId()).getSkuNo());// 填充真实的sku no
            data.setWarehouseLocation(StrUtils.null2EmptyWithTrim(data.getWarehouseLocation()));
            // 修改时添加日志
            if(StrUtils.isNotEmpty(data.getId())) {
                InitStockDetailEntity initStockDetailOld = super.getById(data.getId());
                if (ObjectUtils.isEmpty(initStockDetailOld)) {
                    throw new ServiceException("未找到期初库存明细数据");
                }
                operateLogService.addModuleOperateLogByObj(initStockDetailOld,data, ModuleTypeEnum.INIT_STOCK.getCode(),mainId,"",String.format("【%s】",initStockDetailOld.getSkuNo()));
            }
        }
        if (CollUtil.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.INIT_STOCK.getCode(), addPairList, "编辑操作");
        }
    }

}