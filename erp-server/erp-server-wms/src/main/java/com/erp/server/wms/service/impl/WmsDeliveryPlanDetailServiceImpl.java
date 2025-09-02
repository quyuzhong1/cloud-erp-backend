package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WmsDeliveryPlanDTO;
import com.erp.model.wms.dto.WmsDeliveryPlanDetailDTO;
import com.erp.model.wms.entity.WmsDeliveryPlanDetailEntity;
import com.erp.model.wms.enums.DeliveryPlanTypeEnum;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.convert.DeliveryPlanConverter;
import com.erp.server.wms.mapper.WmsDeliveryPlanDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.OverseasProviderWarehouseService;
import com.erp.server.wms.service.WmsDeliveryPlanDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 发货计划详情表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class WmsDeliveryPlanDetailServiceImpl extends SuperServiceImpl<WmsDeliveryPlanDetailMapper, WmsDeliveryPlanDetailEntity> implements WmsDeliveryPlanDetailService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private OmsListingInfoFeign omsListingInfoFeign;
    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;
    @Resource
    private SkuMappingFeign skuMappingFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(WmsDeliveryPlanDTO.AddDTO addDTO, String mainId) {
        List<WmsDeliveryPlanDetailEntity> list = DeliveryPlanConverter.INSTANCE.generateDeliveryDetailAdd(addDTO.getDetailList());
        // 数据处理
        handleData(list, mainId, Boolean.FALSE, addDTO.getToWarehouseId());

        //校验是否重复
        String type = addDTO.getType();
        if (DeliveryPlanTypeEnum.FBA.getCode().equals(type)) {
            // 分组并检查 FBA 类型的唯一性
            Map<String, List<WmsDeliveryPlanDetailEntity>> fbaGroup = list.stream()
                    .collect(Collectors.groupingBy(detail -> detail.getPlatformSku() + detail.getPlatformFnSku() + detail.getSkuNo()));

            for (Map.Entry<String, List<WmsDeliveryPlanDetailEntity>> entry : fbaGroup.entrySet()) {
                if (entry.getValue().size() > 1) {
                    String duplicateSkus = entry.getValue().stream()
                            .map(detail -> detail.getPlatformSku() + "+" + detail.getPlatformFnSku() + "+" + detail.getSkuNo())
                            .distinct()
                            .collect(Collectors.joining(", "));
                    throw new ServiceException("FBA 类型的 MSKU+FNSKU+SKU 必须唯一 ,重复的组合:" + duplicateSkus);
                }
            }
        } else if (DeliveryPlanTypeEnum.THIRD_WAREHOUSE.getCode().equals(type)) {
            Map<String, List<WmsDeliveryPlanDetailEntity>> thirdPartyGroup = list.stream()
                    .collect(Collectors.groupingBy(detail -> detail.getPlatformSku() + detail.getSkuNo()));

            for (Map.Entry<String, List<WmsDeliveryPlanDetailEntity>> entry : thirdPartyGroup.entrySet()) {
                if (entry.getValue().size() > 1) {
                    String duplicateSkus = entry.getValue().stream()
                            .map(detail -> detail.getPlatformSku() + "+" + detail.getSkuNo())
                            .distinct()
                            .collect(Collectors.joining(", "));
                    throw new ServiceException("三方仓类型的 三方仓SKU+SKU 必须唯一，重复的组合: " + duplicateSkus);
                }
            }
        } else {
            throw new ServiceException("未知的类型: " + type);
        }

        log.info("开始新增发货计划详情单");
        boolean save = super.saveBatch(list);
        if(!save) {
            throw new ServiceException("发货计划详情单保存失败");
        }

    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(WmsDeliveryPlanDTO.UpdateDTO updateDTO, String mainId) {
        List<WmsDeliveryPlanDetailDTO.UpdateDTO> detailList = updateDTO.getDetailList();
        //原明细数据
        List<WmsDeliveryPlanDetailEntity> oldList = this.listByMainIds(Collections.singletonList(mainId));
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<WmsDeliveryPlanDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.DELIVERY_PLAN.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }

        //映射字段
        List<WmsDeliveryPlanDetailEntity> list = DeliveryPlanConverter.INSTANCE.generateDeliveryDetailUpdate(updateDTO.getDetailList());
        // 数据处理
        handleData(list, mainId, Boolean.TRUE, updateDTO.getToWarehouseId());


        //校验是否重复
        String type = updateDTO.getType();
        if (DeliveryPlanTypeEnum.FBA.getCode().equals(type)) {
            // 分组并检查 FBA 类型的唯一性
            Map<String, List<WmsDeliveryPlanDetailEntity>> fbaGroup = list.stream()
                    .collect(Collectors.groupingBy(detail -> detail.getPlatformSku() + detail.getPlatformFnSku() + detail.getSkuNo()));

            for (Map.Entry<String, List<WmsDeliveryPlanDetailEntity>> entry : fbaGroup.entrySet()) {
                if (entry.getValue().size() > 1) {
                    String duplicateSkus = entry.getValue().stream()
                            .map(detail -> detail.getPlatformSku() + "+" + detail.getPlatformFnSku() + "+" + detail.getSkuNo())
                            .distinct()
                            .collect(Collectors.joining(", "));
                    throw new ServiceException("FBA 类型的 MSKU+FNSKU+SKU 必须唯一 ,重复的组合:" + duplicateSkus);
                }
            }
        } else if (DeliveryPlanTypeEnum.THIRD_WAREHOUSE.getCode().equals(type)) {
            Map<String, List<WmsDeliveryPlanDetailEntity>> thirdPartyGroup = list.stream()
                    .collect(Collectors.groupingBy(detail -> detail.getPlatformSku() + detail.getSkuNo()));

            for (Map.Entry<String, List<WmsDeliveryPlanDetailEntity>> entry : thirdPartyGroup.entrySet()) {
                if (entry.getValue().size() > 1) {
                    String duplicateSkus = entry.getValue().stream()
                            .map(detail -> detail.getPlatformSku() + "+" + detail.getSkuNo())
                            .distinct()
                            .collect(Collectors.joining(", "));
                    throw new ServiceException("三方仓类型的 三方仓SKU+SKU 必须唯一，重复的组合: " + duplicateSkus);
                }
            }
        } else {
            throw new ServiceException("未知的类型: " + type);
        }
        log.info("开始修改发货计划详情单");
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("修改发货计划详情失败");
        }
    }

    @Override
    public List<WmsDeliveryPlanDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(WmsDeliveryPlanDetailEntity::getMainId, mainIds).list();
    }

    @Override
    public Boolean removeByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Boolean.FALSE;
        }
        return lambdaUpdate().in(WmsDeliveryPlanDetailEntity::getMainId,mainIds).remove();
    }

    @Override
    public List<WmsDeliveryPlanDetailEntity> listBySourceIdList(List<String> idList) {
        return baseMapper.listBySourceIdList(idList);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<WmsDeliveryPlanDetailEntity> list, String mainId, Boolean isUpdate, String toWarehouseId) {
        //需要新增的数据
        List<WmsDeliveryPlanDetailEntity> addList = list.stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).collect(Collectors.toList());

        //根据skuId查询拥有的子sku
        List<String> skuIds = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);

        //查询skuId产品信息
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);

        //设置详情字段
        for (WmsDeliveryPlanDetailEntity detailEntity : list) {
            detailEntity.setMainId(mainId);
            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(detailEntity.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                detailEntity.setIsCombination(Boolean.TRUE);
            } else {
                detailEntity.setIsCombination(Boolean.FALSE);
            }
            //设置产品编号
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(new SkuVO());
            detailEntity.setSkuNo(skuVO.getSkuNo());
            //来源信息
            detailEntity.setSourceJson(CollectionUtils.isEmpty(detailEntity.getSourceJsonList()) ? new JSONArray() : JSONUtil.parseArray(detailEntity.getSourceJsonList()));

            //校验是否是修改，如果是就新增修改日志
            if (CharSequenceUtil.isNotBlank(detailEntity.getId())) {
                WmsDeliveryPlanDetailEntity old = list.stream().filter(obj -> obj.getId().equals(detailEntity.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_NOT_OVERSEAS_DELIVERY_PLAN);
                }
                operateLogService.addModuleOperateLogByObj(old, detailEntity, ModuleTypeEnum.DELIVERY_PLAN.getCode(), detailEntity.getId(),"", String.format("【%s】", old.getSkuNo()));
            }
        }

        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.DELIVERY_PLAN.getCode(), addPairList, "编辑操作");
        }
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<WmsDeliveryPlanDetailDTO.UpdateDTO> newList, List<WmsDeliveryPlanDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getId())).
                map(WmsDeliveryPlanDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(WmsDeliveryPlanDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

}
