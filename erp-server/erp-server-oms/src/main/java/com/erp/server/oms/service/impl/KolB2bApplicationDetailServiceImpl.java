package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.KolB2bApplicationDetailDTO;
import com.erp.model.oms.entity.KolB2bApplicationDetailEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.KolB2bApplicationDetailMapper;
import com.erp.server.oms.service.KolB2bApplicationDetailService;
import com.erp.server.oms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * B2B寄样申请明细表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-12-01
 */
@Slf4j
@Service
public class KolB2bApplicationDetailServiceImpl extends SuperServiceImpl<KolB2bApplicationDetailMapper, KolB2bApplicationDetailEntity> implements KolB2bApplicationDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(List<KolB2bApplicationDetailDTO.AddDTO> detailList,String mainId) {
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.BILL_PARAM_SELECTION_REQUIRED, "B2B寄样申请明细单");
        }
        List<KolB2bApplicationDetailEntity> list = BeanMapperUtils.copyList(KolB2bApplicationDetailEntity.class, detailList);

        // 数据处理
        handleData(list,mainId);

        log.info("开始新增B2B寄样申请明细单");
        boolean save = super.saveBatch(list);
        if(!save) {
            throw new ServiceException("B2B寄样申请明细单保存失败");
        }
        return save;
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<KolB2bApplicationDetailDTO.UpdateDTO> detailList,String mainId) {
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.BILL_PARAM_SELECTION_REQUIRED, "B2B寄样申请明细单");
        }
        List<KolB2bApplicationDetailEntity> list = BeanMapperUtils.copyList(KolB2bApplicationDetailEntity.class, detailList);

        //原明细数据
        List<KolB2bApplicationDetailEntity> oldList = this.listByMainIdList(Collections.singletonList(mainId));
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<KolB2bApplicationDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.KOL_B2B_APPLICATION.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }

        // 数据处理
        handleData(list,mainId);
        log.info("编辑 开始修改B2B寄样申请明细单数据，mainId：【{}】", mainId);
        boolean save = this.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("B2B寄样申请明细单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean deleteByMainId(String mainId) {
        return lambdaUpdate().eq(KolB2bApplicationDetailEntity::getMainId,mainId).remove();
    }

    @Override
    public List<KolB2bApplicationDetailEntity> listByMainIdList(List<String> mainIdList) {
        if (CollUtil.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(KolB2bApplicationDetailEntity::getMainId,mainIdList).orderByDesc(KolB2bApplicationDetailEntity::getId).list();
    }



    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<KolB2bApplicationDetailEntity> newList, List<KolB2bApplicationDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getId())).
                map(KolB2bApplicationDetailEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(KolB2bApplicationDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<KolB2bApplicationDetailEntity> list,String mainId ) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<String> skuIdList = list.stream().map(KolB2bApplicationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);
        Map<String, String> skuMap = CollUtil.isEmpty(productDetailList) ? new HashMap<>() : productDetailList.stream()
                .collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getSkuNo));
        for (KolB2bApplicationDetailEntity entity : list ) {
            //赋值sku编码
            entity.setSkuNo(skuMap.get(entity.getSkuId()));
            entity.setMainId(mainId);
            //操作日志
            if (CharSequenceUtil.isNotBlank(entity.getId())) {
                KolB2bApplicationDetailEntity old = this.getById(entity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_NOT_FOUND,"B2B寄样申请明细单");
                }
                operateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.KOL_B2B_APPLICATION.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
        //添加操作日志
        List<KolB2bApplicationDetailEntity> addList = list.stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).collect(Collectors.toList());
        //新增不需要添加新增SKU的日志
        if (CollectionUtils.isNotEmpty(addList) && addList.size() != list.size()) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.KOL_B2B_APPLICATION.getCode(), addPairList, "编辑操作");
        }
    }
}
