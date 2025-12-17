package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.QcNoticeDTO;
import com.erp.model.wms.entity.QcNoticeDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.QcNoticeDetailMapper;
import com.erp.server.wms.service.QcNoticeDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 质检通知单明细 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-04-21
 */
@Slf4j
@Service
public class QcNoticeDetailServiceImpl extends SuperServiceImpl<QcNoticeDetailMapper, QcNoticeDetailEntity> implements QcNoticeDetailService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(QcNoticeDTO.AddDTO addDTO,String mainId) {
        //新增明细
        List<QcNoticeDetailEntity> qcNoticeDetailList = BeanMapperUtils.copyList(QcNoticeDetailEntity.class, addDTO.getDetailList());

        handleData(qcNoticeDetailList, mainId,Boolean.FALSE);

        saveBatch(qcNoticeDetailList);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(QcNoticeDTO.UpdateDTO addOrUpdateDTO,String mainId) {
        //原明细数据
        List<QcNoticeDetailEntity>  oldList = listByMainIds(Collections.singletonList(mainId));

        List<QcNoticeDetailEntity> qcNoticeDetailList = BeanMapperUtils.copyList(QcNoticeDetailEntity.class, addOrUpdateDTO.getDetailList());
        List<String> deleteIds = getDeleteIds(qcNoticeDetailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<QcNoticeDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.QC_NOTICE.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }

        handleData(qcNoticeDetailList, mainId,Boolean.TRUE);
        //新增或更新明细
        saveOrUpdateBatch(qcNoticeDetailList);
    }

    @Override
    public List<QcNoticeDetailEntity> listByMainIds(List<String> mainIds) {
        if(CollUtil.isEmpty(mainIds)){
            return Collections.emptyList();
        }
        return lambdaQuery().in(QcNoticeDetailEntity::getMainId,mainIds).list();
    }

    @Override
    public void deleteByMainId(String id) {
        if(StringUtils.isBlank(id)){
            return ;
        }
        lambdaUpdate().eq(QcNoticeDetailEntity::getMainId,id).remove();
    }

    private List<String> getDeleteIds(List<QcNoticeDetailEntity> newList, List<QcNoticeDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getId())).map(QcNoticeDetailEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(QcNoticeDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(List<QcNoticeDetailEntity> qcNoticeDetailList,String mainId, Boolean isUpdate) {
        //原明细数据
        List<QcNoticeDetailEntity>  oldList = listByMainIds(Collections.singletonList(mainId));

        List<String> skuIds = qcNoticeDetailList.stream().map(QcNoticeDetailEntity::getSkuId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<ProductDetailEntity> skuList = plmTaskFeign.getByIdList(skuIds);
        Map<String, ProductDetailEntity> skuMap = skuList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, t -> t, (o1, o2) -> o1));

        qcNoticeDetailList.stream().forEach(e -> {
            e.setMainId(mainId);

            ProductDetailEntity productDetailEntity = skuMap.get(e.getSkuId());
            if (Objects.nonNull(productDetailEntity)) {
                e.setSkuNo(productDetailEntity.getSkuNo());
                e.setProductName(productDetailEntity.getName());
            }

            //校验是否是修改，如果是就新增修改日志
            if (CharSequenceUtil.isNotBlank(e.getId())) {
                QcNoticeDetailEntity old = oldList.stream().filter(obj -> obj.getId().equals(e.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_NOT_REQUISITION_APPLICATION);
                }
                operateLogService.addModuleOperateLogByObj(old, e, ModuleTypeEnum.QC_NOTICE.getCode(), mainId,"", String.format("【%s】", old.getSkuNo()));
            }
        });

        //需要新增的数据
        List<QcNoticeDetailEntity> addList = qcNoticeDetailList.stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.QC_NOTICE.getCode(), addPairList, "编辑操作");
        }
    }
}
