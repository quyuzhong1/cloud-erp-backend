package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.common.core.utils.ValidatorUtil;
import org.apache.commons.math3.util.Pair;
import cn.hutool.core.util.StrUtil;
import com.common.business.service.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.TransferOutDetailDTO;
import com.erp.model.wms.entity.TransferOutDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.TransferOutDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.TransferOutDetailService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 分布式调出单明细 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Slf4j
@Service
public class TransferOutDetailServiceImpl extends SuperServiceImpl<TransferOutDetailMapper, TransferOutDetailEntity> implements TransferOutDetailService {

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private OperateLogService operateLogService;

    @Override
    public List<TransferOutDetailEntity> listSourceDetailIds(List<String> sourceDetailIds) {
        return baseMapper.listSourceDetailIds(sourceDetailIds);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(List<TransferOutDetailDTO.AddDTO> detailList, String mainId) {
        if(CollUtil.isEmpty(detailList)) {
            log.info("分步式调出单明细为空");
            return;
        }
        List<TransferOutDetailEntity> list = BeanMapperUtils.copyList(TransferOutDetailEntity.class, detailList);
        handleDetails(list, mainId, Boolean.FALSE);
        log.info("开始保存分步式调出单明细信息");
        super.saveBatch(list);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(List<TransferOutDetailDTO.UpdateDTO> detailList, String mainId) {
        // 查询原明细数据
        List<TransferOutDetailEntity> originMembers = this.listByMainId(mainId);
        // 查询被删除的明细id（即新上传的id集合没有包含原始id的）
        List<String> originIds = originMembers.stream().map(TransferOutDetailEntity::getId).collect(Collectors.toList());
        // 新上送的明细id集合（不包括空的）
        List<String> nowIds = detailList.stream().filter(r->StrUtils.isNotEmpty(r.getId())).map(TransferOutDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        // 需要删除的id集合
        List<String> deleteIds = originIds.stream().filter(id->!nowIds.contains(id)).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(deleteIds)) {
            // 记录删除日志
            List<TransferOutDetailEntity> deleteMembers = originMembers.stream().filter(r->deleteIds.contains(r.getId())).collect(Collectors.toList());
            List<Pair<String, String>> pairList = deleteMembers.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.TRANSFER_OUT.getCode(),pairList,"编辑操作");
            // 删除明细数据
            super.removeByIds(deleteIds);
        }
        // 新增或修改的明细数据
        List<TransferOutDetailEntity> newList = BeanMapperUtils.copyList(TransferOutDetailEntity.class, detailList);
        // TODO 下推数量验证
        // 记录新增或修改日志
        handleDetails(newList, mainId, Boolean.TRUE);
        //新增或修改明细
        boolean save = this.saveOrUpdateBatch(newList);
        ValidatorUtil.isTrue(save, ()->new ServiceException("分步式调出单明细保存失败"));
    }

    @Override
    public List<TransferOutDetailEntity> listByMainId(String mainId) {
        return lambdaQuery()
                .eq(TransferOutDetailEntity::getMainId,mainId)
                .orderByDesc(TransferOutDetailEntity::getId)
                .list();
    }

    private void handleDetails(List<TransferOutDetailEntity> newList, String mainId, Boolean isUpdate) {
        // 新增的明细
        List<TransferOutDetailEntity> addList = newList.stream().filter(c -> StrUtils.isEmpty(c.getId())).collect(Collectors.toList());
        // 需要修改的数据
        List<String> updateIds = newList.stream().filter(c -> StrUtils.isNotEmpty(c.getId())).map(TransferOutDetailEntity::getId).collect(Collectors.toList());
        List<TransferOutDetailEntity> updateList = Lists.newArrayList();
        if(CollUtil.isNotEmpty(updateIds)) {
            updateList = super.listByIds(updateIds);
        }

        List<String> skuIds = newList.stream().map(TransferOutDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuInfos = plmTaskFeign.getSkuInfoByIds(skuIds);
        Map<String,SkuVO> skuMap =  skuInfos.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        for(int i = 0, length = newList.size();i < length;i++) {
            TransferOutDetailEntity data = newList.get(i);
            SkuVO skuVO = skuMap.get(data.getSkuId());
            if(Objects.isNull(skuVO)) {
                throw new ServiceException(StrUtil.format("SKU【{}】错误", data.getSkuNo()));
            }
            // 单位
            String unit = StrUtils.null2EmptyWithTrim(skuVO.getUnitName());
            data.setUnit(unit);
            data.setMainId(mainId);
            data.setSkuNo(skuMap.get(data.getSkuId()).getSkuNo());// 填充sku编号
            data.setOutWarehouseLocation(StrUtils.null2EmptyWithTrim(data.getOutWarehouseLocation()));
            // 修改时添加日志
            if(StrUtils.isNotEmpty(data.getId())) {
                TransferOutDetailEntity old = updateList.stream().filter(r -> Objects.equals(data.getId(), r.getId())).findFirst().orElse(null);
                if (Objects.isNull(old)) {
                    throw new ServiceException("未找到分步式调出明细数据");
                }
                operateLogService.addModuleOperateLogByObj(old,data, ModuleTypeEnum.TRANSFER_OUT.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
        if (CollUtil.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.TRANSFER_OUT.getCode(), addPairList, "编辑操作");
        }
    }

}
