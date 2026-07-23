package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.enums.BillApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.dto.KolSubB2cApplicationDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.CfgKolOptionTypeEnum;
import com.erp.model.oms.enums.KolSubB2cApplicationDeliveryStatusEnum;
import com.erp.model.oms.enums.KolSubB2cApplicationOrderStatusEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.service.CfgKolOptionService;
import com.erp.server.oms.service.KolSubB2cApplicationDetailService;
import com.erp.server.oms.mapper.KolSubB2cApplicationMapper;
import com.erp.server.oms.service.KolSubB2cApplicationService;
import com.erp.server.oms.service.SoB2cLogisticsService;
import com.erp.server.oms.service.SoB2cService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Resource;

/**
 * <p>
 * B2C寄样申请单拆分单 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-12-04
 */
@Slf4j
@Service
public class KolSubB2cApplicationServiceImpl extends SuperServiceImpl<KolSubB2cApplicationMapper, KolSubB2cApplicationEntity> implements KolSubB2cApplicationService {
    @Lazy
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;
    @Resource
    private KolSubB2cApplicationDetailService kolSubB2cApplicationDetailService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private CfgKolOptionService cfgKolOptionService;


    @Override
    public List<KolSubB2cApplicationDTO.ListDTO> listSubBySourceId(String sourceId) {
        if(StringUtils.isBlank(sourceId)){
            return Collections.emptyList();
        }
        List<KolSubB2cApplicationDTO.ListDTO> list = this.baseMapper.listSubBySourceId(sourceId);
        // 数据处理
        fillList(list);
        return list;
    }

    private void fillList(List<KolSubB2cApplicationDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }

        List<String> skuIds = list.stream().map(KolSubB2cApplicationDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, String> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuName));

        List<CfgKolOptionEntity> cfgKolOptionEntities = cfgKolOptionService.lambdaQuery().in(CfgKolOptionEntity::getType, Arrays.asList(CfgKolOptionTypeEnum.KOL_SAMPLE_TYPE.getCode(), CfgKolOptionTypeEnum.PROJECT_TAG.getCode())).list();
        Map<String, String> map = cfgKolOptionEntities.stream().collect(Collectors.toMap(CfgKolOptionEntity::getId, CfgKolOptionEntity::getName));


        // 属性赋值
        for(KolSubB2cApplicationDTO.ListDTO data : list) {
            //平台
            data.setDictPlatformName(DmpBasicSystemCodeEnum.getName(data.getDictPlatform()));
            //订单状态
            data.setOrderStatusName(KolSubB2cApplicationOrderStatusEnum.getName(data.getOrderStatus()));
            //发货状态
            data.setDeliveryStatusName(KolSubB2cApplicationDeliveryStatusEnum.getName(data.getDeliveryStatus()));
            //SKU名称
            data.setProductName(skuMap.get(data.getSkuId()));
            //项目名称
            if(StringUtils.isNotBlank(data.getProjectTag())){
                String projectTagName = Arrays.stream(data.getProjectTag().split(",")).map(map::get).collect(Collectors.joining(","));
                data.setProjectTagName(projectTagName);
            }
        }
    }

    @Override
    public List<KolSubB2cApplicationDTO.PushDTO> listPushByIds(List<String> ids){
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<KolSubB2cApplicationDTO.PushDTO> result = new ArrayList<>();
        List<KolSubB2cApplicationEntity> list = listByIds(ids);
        List<KolSubB2cApplicationDetailEntity> detailList = kolSubB2cApplicationDetailService.lambdaQuery().in(KolSubB2cApplicationDetailEntity::getMainId, ids).list();
        for (KolSubB2cApplicationEntity entity : list) {
            KolSubB2cApplicationDTO.PushDTO pushDTO = new KolSubB2cApplicationDTO.PushDTO();
            pushDTO.setEntity(entity);
            List<KolSubB2cApplicationDetailEntity> detailEntities = detailList.stream().filter(e -> e.getMainId().equals(entity.getId())).collect(Collectors.toList());
            pushDTO.setDetailList(detailEntities);
            result.add(pushDTO);
        }
        return result;
    }

    /**
     * 根据B2C寄样申请生成拆分单
     * 按达人维度生成拆分单和拆分单明细
     * 根据业务类型生成 国外=B2C订单  国内=旺店通销售订单
     * @author jack
     * @date: 2025-12-09
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<KolSubB2cApplicationDTO.PushDTO> generateSplitOrder(KolB2cApplicationEntity entity, List<KolB2cApplicationDetailEntity> list) {
        return generateSplitOrder(entity, list, 0);
    }

    private List<KolSubB2cApplicationDTO.PushDTO> generateSplitOrder(KolB2cApplicationEntity entity, List<KolB2cApplicationDetailEntity> list, int startIndex) {
        List<KolSubB2cApplicationDTO.PushDTO> result = new ArrayList<>();
        //明细按达人分组
        Map<String, List<KolB2cApplicationDetailEntity>> partnerGroup = list.stream().collect(Collectors.groupingBy(KolB2cApplicationDetailEntity::getPartnerId));

        int index = startIndex;
        for (Map.Entry<String, List<KolB2cApplicationDetailEntity>> entry : partnerGroup.entrySet()) {
            KolSubB2cApplicationDTO.PushDTO pushDTO = new KolSubB2cApplicationDTO.PushDTO();

            //------------按达人维度生成拆分单和拆分单明细------------
            KolSubB2cApplicationEntity kolSubB2cApplicationEntity = new KolSubB2cApplicationEntity();
            kolSubB2cApplicationEntity.setSourceId(entity.getId());
            kolSubB2cApplicationEntity.setDictPlatform(getDictPlatform(entity.getIsInternational()));
            kolSubB2cApplicationEntity.setDeliveryStatus(KolSubB2cApplicationDeliveryStatusEnum.WAITSHIPPED.getCode());
            kolSubB2cApplicationEntity.setOrderStatus(KolSubB2cApplicationOrderStatusEnum.NOTAPPROVE.getCode());
            kolSubB2cApplicationEntity.setRemark(entity.getRemark());
            if(index == 0){
                kolSubB2cApplicationEntity.setCode(entity.getCode());
            }else {
                kolSubB2cApplicationEntity.setCode(entity.getCode()+"_"+index);
            }
            kolSubB2cApplicationEntity.setPartnerId(entry.getKey());
            kolSubB2cApplicationEntity.setNickname(entry.getValue().get(0).getNickname());

            boolean save = super.save(kolSubB2cApplicationEntity);
            if(!save) {
                throw new ServiceException("B2C寄样申请单拆分单保存失败");
            }
            String id = kolSubB2cApplicationEntity.getId();
            //------------根据生成拆分单明细，以及生成B2C明细------------
            List<KolB2cApplicationDetailEntity> value = entry.getValue();
            List<KolSubB2cApplicationDetailEntity> detailList = new ArrayList<>();
            for (KolB2cApplicationDetailEntity b2cApplicationDetailEntity : value) {
                KolSubB2cApplicationDetailEntity subB2cApplicationDetailEntity = new KolSubB2cApplicationDetailEntity();
                String idStr = IdWorker.getIdStr();
                subB2cApplicationDetailEntity.setId(idStr);
                subB2cApplicationDetailEntity.setSourceDetailId(b2cApplicationDetailEntity.getId());
                subB2cApplicationDetailEntity.setSkuId(b2cApplicationDetailEntity.getSkuId());
                subB2cApplicationDetailEntity.setSkuNo(b2cApplicationDetailEntity.getSkuNo());
                subB2cApplicationDetailEntity.setApplyQty(b2cApplicationDetailEntity.getApplyQty());
                subB2cApplicationDetailEntity.setRemark(b2cApplicationDetailEntity.getRemark());
                subB2cApplicationDetailEntity.setProjectTag(b2cApplicationDetailEntity.getProjectTag());
                subB2cApplicationDetailEntity.setMainId(id);
                detailList.add(subB2cApplicationDetailEntity);
            }
            //保存明细
            kolSubB2cApplicationDetailService.saveBatch(detailList);

            pushDTO.setEntity(kolSubB2cApplicationEntity);
            pushDTO.setDetailList(detailList);
            result.add(pushDTO);
            //序号+1
            index+=1;
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<KolSubB2cApplicationDTO.PushDTO> generateSplitOrderIdempotent(KolB2cApplicationEntity entity, List<KolB2cApplicationDetailEntity> list) {
        if (Objects.isNull(entity) || StringUtils.isBlank(entity.getId())) {
            return Collections.emptyList();
        }
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<KolSubB2cApplicationEntity> existList = lambdaQuery()
                .eq(KolSubB2cApplicationEntity::getSourceId, entity.getId())
                .eq(KolSubB2cApplicationEntity::getIsDeleted, false)
                .list();
        if (CollUtil.isNotEmpty(existList)) {
            checkSplitOrderComplete(entity, list, existList);
            Map<String, KolSubB2cApplicationEntity> existPartnerMap = existList.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getPartnerId()))
                    .collect(Collectors.toMap(KolSubB2cApplicationEntity::getPartnerId, e -> e, (o1, o2) -> o1));
            List<KolB2cApplicationDetailEntity> missingPartnerDetails = list.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getPartnerId()))
                    .filter(e -> !existPartnerMap.containsKey(e.getPartnerId()))
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(missingPartnerDetails)) {
                generateSplitOrder(entity, missingPartnerDetails, getNextSplitOrderIndex(entity, existList));
                existList = lambdaQuery()
                        .eq(KolSubB2cApplicationEntity::getSourceId, entity.getId())
                        .eq(KolSubB2cApplicationEntity::getIsDeleted, false)
                        .list();
            }
            List<String> ids = existList.stream().map(KolSubB2cApplicationEntity::getId).collect(Collectors.toList());
            return listPushByIds(ids);
        }
        return generateSplitOrder(entity, list);
    }

    private void checkSplitOrderComplete(KolB2cApplicationEntity entity, List<KolB2cApplicationDetailEntity> sourceDetails, List<KolSubB2cApplicationEntity> existList) {
        Map<String, String> subPartnerMap = existList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getPartnerId()))
                .collect(Collectors.toMap(KolSubB2cApplicationEntity::getPartnerId, KolSubB2cApplicationEntity::getId, (o1, o2) -> o1));
        Set<String> existingSubIds = new HashSet<>(subPartnerMap.values());
        List<KolSubB2cApplicationDetailEntity> subDetails = CollUtil.isEmpty(existingSubIds)
                ? Collections.emptyList()
                : kolSubB2cApplicationDetailService.lambdaQuery()
                        .in(KolSubB2cApplicationDetailEntity::getMainId, existingSubIds)
                        .eq(KolSubB2cApplicationDetailEntity::getIsDeleted, false)
                        .list();
        Map<String, Set<String>> sourceDetailIdsBySubId = CollUtil.emptyIfNull(subDetails).stream()
                .filter(e -> StringUtils.isNotBlank(e.getSourceDetailId()))
                .collect(Collectors.groupingBy(KolSubB2cApplicationDetailEntity::getMainId,
                        Collectors.mapping(KolSubB2cApplicationDetailEntity::getSourceDetailId, Collectors.toSet())));
        for (KolB2cApplicationDetailEntity detail : sourceDetails) {
            String subId = subPartnerMap.get(detail.getPartnerId());
            if (StringUtils.isBlank(subId)) {
                continue;
            }
            Set<String> sourceDetailIds = sourceDetailIdsBySubId.getOrDefault(subId, Collections.emptySet());
            if (!sourceDetailIds.contains(detail.getId())) {
                throw new ServiceException(ApiError.WF_KOL_B2C_SPLIT_DETAIL_INCOMPLETE, entity.getCode(), detail.getNickname());
            }
        }
    }

    private int getNextSplitOrderIndex(KolB2cApplicationEntity entity, List<KolSubB2cApplicationEntity> existList) {
        int maxIndex = -1;
        String baseCode = entity.getCode();
        for (KolSubB2cApplicationEntity subEntity : CollUtil.emptyIfNull(existList)) {
            String code = subEntity.getCode();
            if (StringUtils.equals(code, baseCode)) {
                maxIndex = Math.max(maxIndex, 0);
            } else if (StringUtils.startsWith(code, baseCode + "_")) {
                String suffix = StringUtils.substringAfter(code, baseCode + "_");
                if (StringUtils.isNumeric(suffix)) {
                    maxIndex = Math.max(maxIndex, Integer.parseInt(suffix));
                }
            }
        }
        return maxIndex + 1;
    }

    /**
     *  根据业务类型判断是哪个平台
     */
    private String getDictPlatform(Boolean isInternational) {
        return Boolean.TRUE.equals(isInternational) ? DmpBasicSystemCodeEnum.ERP.getCode() : DmpBasicSystemCodeEnum.WDT.getCode();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshDeliveryAndTrackBySoB2c(String kolSubId) {
        if (StringUtils.isBlank(kolSubId)) {
            return;
        }
        KolSubB2cApplicationEntity subEntity = getById(kolSubId);
        if (subEntity == null) {
            return;
        }
        List<SoB2cEntity> soList = soB2cService.lambdaQuery()
                .eq(SoB2cEntity::getSourceType, SourceTypeEnum.KOL_B2C_APPLICATION.getCode())
                .eq(SoB2cEntity::getSourceId, kolSubId)
                .eq(SoB2cEntity::getInvalidStatus, Boolean.FALSE)
                .list();
        if (CollUtil.isEmpty(soList)) {
            return;
        }
        long shippedCount = soList.stream()
                .filter(e -> SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(e.getBillStatus()))
                .count();
        String deliveryStatus;
        if (shippedCount <= 0) {
            deliveryStatus = KolSubB2cApplicationDeliveryStatusEnum.WAITSHIPPED.getCode();
        } else if (shippedCount >= soList.size()) {
            deliveryStatus = KolSubB2cApplicationDeliveryStatusEnum.SHIPPED.getCode();
        } else {
            deliveryStatus = KolSubB2cApplicationDeliveryStatusEnum.PARTIAL_SHIPPED.getCode();
        }
        // 跟踪号：三方仓场景在 shipping_order_no；自营/手动发货一般在 so_b2c_logistics.track_no
        Set<String> trackNoSet = new LinkedHashSet<>();
        for (SoB2cEntity so : soList) {
            if (StringUtils.isNotBlank(so.getShippingOrderNo())) {
                Arrays.stream(so.getShippingOrderNo().split(","))
                        .map(String::trim)
                        .filter(StringUtils::isNotBlank)
                        .forEach(trackNoSet::add);
            }
        }
        List<String> soIds = soList.stream().map(SoB2cEntity::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(soIds)) {
            List<SoB2cLogisticsEntity> logisticsList = soB2cLogisticsService.listByMainIds(soIds);
            if (CollUtil.isNotEmpty(logisticsList)) {
                for (SoB2cLogisticsEntity logistics : logisticsList) {
                    if (StringUtils.isNotBlank(logistics.getTrackNo())) {
                        Arrays.stream(logistics.getTrackNo().split(","))
                                .map(String::trim)
                                .filter(StringUtils::isNotBlank)
                                .forEach(trackNoSet::add);
                    }
                }
            }
        }
        String trackNo = String.join(",", trackNoSet);
        // 订单关联状态：过滤已作废后，全部已审核则为已审核，否则未审核
        boolean allApproved = soList.stream().allMatch(e ->
                e.getApproveStatus() != null
                        && BillApproveStatusEnum.APPROVE.getStatus().equals(e.getApproveStatus().getStatus()));
        String orderStatus = allApproved
                ? KolSubB2cApplicationOrderStatusEnum.APPROVE.getCode()
                : KolSubB2cApplicationOrderStatusEnum.NOTAPPROVE.getCode();

        lambdaUpdate()
                .set(KolSubB2cApplicationEntity::getDeliveryStatus, deliveryStatus)
                .set(KolSubB2cApplicationEntity::getTrackNo, trackNo)
                .set(KolSubB2cApplicationEntity::getOrderStatus, orderStatus)
                .eq(KolSubB2cApplicationEntity::getId, kolSubId)
                .update();
    }

}
