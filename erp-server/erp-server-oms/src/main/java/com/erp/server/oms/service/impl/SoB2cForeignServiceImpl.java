package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.ShopifyServerSoB2cDTO;
import com.erp.model.oms.dto.SoB2cForeignDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cOptionTypeEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.oms.mapper.SoB2cMapper;
import com.erp.server.oms.service.*;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * B2C销售订单表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cForeignServiceImpl extends SuperServiceImpl<SoB2cMapper, SoB2cEntity> implements SoB2cForeignService {

    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private SoB2cRefService soB2cRefService;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;

    @Resource
    private SoB2cReceiverService soB2cReceiverService;
    @Autowired
    private SoB2cDetailService soB2cDetailService;

    @Override
    public PagingVO<SoB2cForeignDTO.OrderDeliveryResp> getOrderDeliveryInfo(PagingDTO<SoB2cForeignDTO.OrderDeliveryReq> pagingDTO) {
        SoB2cForeignDTO.OrderDeliveryReq orderDeliveryReq = pagingDTO.getParams();
        if(Objects.isNull(orderDeliveryReq)){
            throw new ServiceException("参数不能为空");
        }
        if(Objects.isNull(pagingDTO.getCurrPage()) || Objects.isNull(pagingDTO.getPageSize())){
            throw new ServiceException("分页参数不能为空");
        }
        if(pagingDTO.getPageSize() > 200){
            throw new ServiceException("每页查询数量不能超过200");
        }
        if(StringUtils.isBlank(orderDeliveryReq.getShopId())){
            throw new ServiceException("店铺Id不能为空");
        }
        List<ShopInfoEntity> shopInfoEntityList = shopInfoService.getRelatedShopById(orderDeliveryReq.getShopId());
        if(CollectionUtils.isEmpty(shopInfoEntityList)){
            throw new ServiceException("店铺不存在或已取消授权");
        }
        ShopInfoEntity shopInfoEntity = shopInfoEntityList.get(0);
        orderDeliveryReq.setErpShopId(shopInfoEntity.getId());
        if(StringUtils.isNotBlank(orderDeliveryReq.getPlatformOrderCode())){
            orderDeliveryReq.setPlatformOrderCodeList(Arrays.asList(StringUtil.split(orderDeliveryReq.getPlatformOrderCode(), ",")));
        }
        if(StringUtils.isNotBlank(orderDeliveryReq.getSellerOrderCode())){
            orderDeliveryReq.setSellerOrderCodeList(Arrays.asList(StringUtil.split(orderDeliveryReq.getSellerOrderCode(), ",")));
        }
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<SoB2cForeignDTO.OrderDeliveryResp> pageData = baseMapper.getForeignOrderDeliveryInfo(query,orderDeliveryReq);
        this.fillDeliveryOrderInfo(pageData.getRecords(),orderDeliveryReq);
        return new PagingVO(pageData);
    }

    private void fillDeliveryOrderInfo(List<SoB2cForeignDTO.OrderDeliveryResp> records,SoB2cForeignDTO.OrderDeliveryReq orderDeliveryReq) {
        if(CollectionUtils.isEmpty(records)){
            return;
        }
        List<String> ids = records.stream().map(SoB2cForeignDTO.OrderDeliveryResp::getId).collect(Collectors.toList());
        List<SoB2cRefEntity> soB2cRefList = soB2cRefService.listBySourceIdOrTargetId(ids);
        List<String> mergeIds = soB2cRefList.stream().filter(v->v.getType().equals(SoB2cOptionTypeEnum.ENUM_MERGE.getCode()) && ids.contains(v.getTargetId())).map(SoB2cRefEntity::getSourceId).collect(Collectors.toList());
        List<String> splitIds = soB2cRefList.stream().filter(v->v.getType().equals(SoB2cOptionTypeEnum.ENUM_SPLIT.getCode()) && ids.contains(v.getTargetId())).map(SoB2cRefEntity::getSourceId).collect(Collectors.toList());
        List<SoB2cEntity> allMergeSob2cList = CollectionUtils.isEmpty(mergeIds)?new ArrayList<>():this.listByIds(mergeIds);
        List<SoB2cEntity> allSplitSob2cList = CollectionUtils.isEmpty(splitIds)?new ArrayList<>():this.listByIds(splitIds);
        List<String> allChannelIds = records.stream().map(SoB2cForeignDTO.OrderDeliveryResp::getLogisticChannelId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<LogisticsChannelDTO.BaseDTO> logisticBaseDTOList = CollectionUtils.isEmpty(allChannelIds)?new ArrayList<>(): logisticsFeign.listChannelInfoById(allChannelIds);
        for (SoB2cForeignDTO.OrderDeliveryResp record : records) {
            record.setShopId(orderDeliveryReq.getShopId());
            if(record.getOrderSource().equals("soB2c")){
                record.setOrderSource("thirdPlatform");
            }

            LogisticsChannelDTO.BaseDTO logisticBaseDTO = logisticBaseDTOList.stream().filter(v->v.getId().equals(record.getLogisticChannelId())).findFirst().orElse(new LogisticsChannelDTO.BaseDTO());
            record.setLogisticChannelCode(logisticBaseDTO.getCode());
            record.setLogisticChannelName(logisticBaseDTO.getName());
            //处理拆分合并数据
            if (CollectionUtils.isNotEmpty(soB2cRefList)) {
                //合并
                long mergeCount = soB2cRefList.stream().filter(obj -> (obj.getTargetId().equals(record.getId()))
                        && SoB2cOptionTypeEnum.ENUM_MERGE.getCode().equals(obj.getType())
                        && InvalidStatusEnum.NOT_VOIDED.getStatus().equals(record.getInvalidStatus())
                ).count();
                record.setIsMergeOrder(mergeCount > 0);
                if(record.getIsMergeOrder()){
                    List<String> sourceIds = soB2cRefList.stream().filter(obj -> (obj.getTargetId().equals(record.getId()))
                            && SoB2cOptionTypeEnum.ENUM_MERGE.getCode().equals(obj.getType())).map(SoB2cRefEntity::getSourceId).collect(Collectors.toList());
                    List<SoB2cEntity> mergeSob2cList = allMergeSob2cList.stream().filter(v->sourceIds.contains(v.getId())).collect(Collectors.toList());
                    mergeSob2cList.forEach(v->{
                        SoB2cForeignDTO.MergeOrderInfo mergeOrderInfo = SoB2cForeignDTO.MergeOrderInfo.builder()
                                .mergeChildOrderErpOrderCode(v.getCode())
                                .mergeChildOrderPlatformOrderCode(v.getPlatformCode())
                                .mergeChildOrderSellerOrderCode(v.getSellerOrderCode())
                                .build();
                        if(CollectionUtils.isEmpty(record.getMergeOrderInfoList())){
                            List<SoB2cForeignDTO.MergeOrderInfo> mergeList = new ArrayList<>();
                            mergeList.add(mergeOrderInfo);
                            record.setMergeOrderInfoList(mergeList);
                        }else{
                            record.getMergeOrderInfoList().add(mergeOrderInfo);
                        }
                    });
                }
                //拆分
                long splitCount = soB2cRefList.stream().filter(obj -> (obj.getTargetId().equals(record.getId()))
                        && SoB2cOptionTypeEnum.ENUM_SPLIT.getCode().equals(obj.getType())
                        && InvalidStatusEnum.NOT_VOIDED.getStatus().equals(record.getInvalidStatus())
                ).count();
                record.setIsSplitOrder(splitCount > 0);
                if(record.getIsSplitOrder()){
                    List<String> sourceIds = soB2cRefList.stream().filter(obj -> (obj.getTargetId().equals(record.getId()))
                            && SoB2cOptionTypeEnum.ENUM_SPLIT.getCode().equals(obj.getType())).map(SoB2cRefEntity::getSourceId).collect(Collectors.toList());
                    List<SoB2cEntity> splitSob2cList = allSplitSob2cList.stream().filter(v->sourceIds.contains(v.getId())).collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(splitSob2cList)){
                        SoB2cForeignDTO.SplitOrderInfo splitOrderInfo = SoB2cForeignDTO.SplitOrderInfo.builder()
                                .splitParentOrderErpOrderCode(splitSob2cList.get(0).getCode())
                                .splitParentOrderPlatformOrderCode(splitSob2cList.get(0).getPlatformCode())
                                .splitParentOrderSellerOrderCode(splitSob2cList.get(0).getSellerOrderCode())
                                .build();
                        record.setSplitOrderInfo(splitOrderInfo);
                    }
                }
            }
        }
    }

    @Override
    public List<ShopifyServerSoB2cDTO.SoB2cLogisticInfoDTO> getShopifyLogisticInfo(ShopifyServerSoB2cDTO.SoB2cLogisticQueryDTO dto) {
        // 优化和抽取下面方法
        List<SoB2cEntity> soB2cList = new LinkedList<>();
        List<SoB2cLogisticsEntity> logisticsEntityList = new LinkedList<>();
        if (StringUtils.isNotBlank(dto.getOrderNumber())) {
            Pair<List<SoB2cEntity>, List<SoB2cLogisticsEntity>> result = queryByTrackingNumber(dto);
            soB2cList = result.getLeft();
            logisticsEntityList = result.getRight();
        } else if (dto.isOrderNumberQuery()) {
            Pair<List<SoB2cEntity>, List<SoB2cLogisticsEntity>> result = queryByOrderNumberAndContactInfo(dto);
            soB2cList = result.getLeft();
            logisticsEntityList = result.getRight();
        }
        if (CollectionUtils.isEmpty(logisticsEntityList) || CollectionUtils.isEmpty(soB2cList)) {
            return Collections.emptyList();
        }
        Map<String, SoB2cLogisticsEntity> logisticsEntityMap = logisticsEntityList.stream().collect(Collectors.toMap(SoB2cLogisticsEntity::getMainId, Function.identity()));
        // 查询订单明细
        List<String> mainIds = soB2cList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainIds(mainIds);
        Map<String, List<SoB2cDetailEntity>> detailMap = soB2cDetailEntityList.stream().collect(Collectors.groupingBy(SoB2cDetailEntity::getMainId));

        // 查询跟踪信息
        List<String> tradeNoList = logisticsEntityList.stream().map(SoB2cLogisticsEntity::getTrackNo).distinct().collect(Collectors.toList());
        List<LogisticsTrackEntity> list = FeignQuery.create(LogisticsTrackEntity.class)
                .eq(LogisticsTrackEntity::getTrackNo, tradeNoList)
                .list();
        Map<String, List<LogisticsTrackEntity>> trackMap = list.stream().collect(Collectors.groupingBy(LogisticsTrackEntity::getTrackNo));
        //根据soB2c的id关联SoB2cDetailEntity/SoB2cLogisticsEntity的mainId, 在根据SoB2cLogisticsEntity的TrackNo关联LogisticsTrackEntity组合成ShopifyServerSoB2cDTO.SoB2cLogisticInfoDTO
        return soB2cList.stream()
                .map(soB2c -> combineSoB2cLogisticInfoDTO(soB2c, logisticsEntityMap, trackMap, detailMap))
                .collect(Collectors.toList());
    }

    /**
     * 组合 SoB2cLogisticInfoDTO 对象。
     *
     * @param soB2c 当前的 SoB2cEntity 对象，包含订单的基本信息。
     * @param logisticsEntityMap 物流实体的映射表，key 为 SoB2cEntity 的主键 ID，value 为对应的 SoB2cLogisticsEntity。
     * @param trackMap 物流跟踪信息的映射表，key 为物流单号，value 为对应的 LogisticsTrackEntity 列表。
     * @param detailMap 订单明细的映射表，key 为 SoB2cEntity 的主键 ID，value 为对应的 SoB2cDetailEntity 列表。
     * @return 组合后的 ShopifyServerSoB2cDTO.SoB2cLogisticInfoDTO 对象，包含订单、物流和产品信息。
     */
    private ShopifyServerSoB2cDTO.SoB2cLogisticInfoDTO combineSoB2cLogisticInfoDTO(SoB2cEntity soB2c,
                                                                                   Map<String, SoB2cLogisticsEntity> logisticsEntityMap,
                                                                                   Map<String, List<LogisticsTrackEntity>> trackMap,
                                                                                   Map<String, List<SoB2cDetailEntity>> detailMap
    ) {
        ShopifyServerSoB2cDTO.SoB2cLogisticInfoDTO logisticInfoDTO = new ShopifyServerSoB2cDTO.SoB2cLogisticInfoDTO();
        logisticInfoDTO.setOrderNumber(soB2c.getSellerOrderCode());
        logisticInfoDTO.setPlatformCode(soB2c.getDictPlatform());
        logisticInfoDTO.setBillStatus(soB2c.getBillStatus());

        // 设置订单信息
        ShopifyServerSoB2cDTO.OrderInfo orderInfo = new ShopifyServerSoB2cDTO.OrderInfo();
        orderInfo.setCreatedAt(soB2c.getPlatformOrderCreateTime());
        // TODO 发货时间
        orderInfo.setPackagedAt(null);
        logisticInfoDTO.setOrderInfo(orderInfo);

        // 设置物流信息
        SoB2cLogisticsEntity logisticsEntity = logisticsEntityMap.get(soB2c.getId());
        if (logisticsEntity != null) {
            ShopifyServerSoB2cDTO.Logistics logistics = new ShopifyServerSoB2cDTO.Logistics();
            logistics.setCarrier(logisticsEntity.getLogisticsChannelName());
            logistics.setTrackingNumber(logisticsEntity.getTrackNo());

            // 设置物流跟踪信息
            List<LogisticsTrackEntity> trackEntities = trackMap.getOrDefault(logisticsEntity.getTrackNo(), Collections.emptyList());
            List<ShopifyServerSoB2cDTO.StatusUpdate> statusUpdates = trackEntities.stream()
                    .sorted(Comparator.comparing(LogisticsTrackEntity::getTrackTime))
                    .map(track -> {
                ShopifyServerSoB2cDTO.StatusUpdate statusUpdate = new ShopifyServerSoB2cDTO.StatusUpdate();
                statusUpdate.setTrackingId(track.getTrackNo());
                statusUpdate.setTimestamp(track.getUpdateTime());
                statusUpdate.setLocation(track.getAddress());
                statusUpdate.setDescription(track.getContent());
                return statusUpdate;
            }).collect(Collectors.toList());
            logistics.setStatusUpdates(statusUpdates);
            logisticInfoDTO.setLogistics(logistics);
        }

        // 设置产品信息
        List<SoB2cDetailEntity> relatedDetails = detailMap.getOrDefault(soB2c.getId(), Collections.emptyList());
        List<ShopifyServerSoB2cDTO.Product> products = relatedDetails.stream().map(detail -> {
            ShopifyServerSoB2cDTO.Product product = new ShopifyServerSoB2cDTO.Product();
            product.setDetailId(detail.getId());
            product.setPlatformDetailId(detail.getSourceDetailId());
            product.setPlatformSkuNo(detail.getPlatformSkuNo());
            product.setPlatformSpuNo(detail.getPlatformSpuNo());
            product.setProductName(null);
            product.setProductImage(null);
            return product;
        }).collect(Collectors.toList());
        logisticInfoDTO.setProducts(products);

        return logisticInfoDTO;
    }

    /**
     * 通过跟踪号查询订单信息
     */
    private Pair<List<SoB2cEntity>, List<SoB2cLogisticsEntity>> queryByTrackingNumber(ShopifyServerSoB2cDTO.SoB2cLogisticQueryDTO dto) {
        List<SoB2cLogisticsEntity> logisticsEntityList = soB2cLogisticsService.lambdaQuery()
                .eq(SoB2cLogisticsEntity::getTrackNo, dto.getTrackingNumber())
                .list();
        if (CollectionUtils.isNotEmpty(logisticsEntityList)) {
            List<String> mainIds = logisticsEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            List<SoB2cEntity> soB2cList = this.lambdaQuery()
                    .eq(SoB2cEntity::getId, mainIds)
                    .eq(SoB2cEntity::getDictPlatform, PlatformDictEnum.SHOPIFY.getCode())
                    .eq(SoB2cEntity::getBillStatus, SoB2cBillStatusEnum.ENUM_SHIPPED.getCode())
                    .list();
            return Pair.of(soB2cList, logisticsEntityList);
        }
        return Pair.of(Collections.emptyList(), Collections.emptyList());
    }

    /**
     * 通过订单号和联系信息查询订单信息
     */
    private Pair<List<SoB2cEntity>, List<SoB2cLogisticsEntity>> queryByOrderNumberAndContactInfo(ShopifyServerSoB2cDTO.SoB2cLogisticQueryDTO dto) {
        List<SoB2cEntity> soB2cList = this.lambdaQuery()
                .eq(SoB2cEntity::getSellerOrderCode, dto.getOrderNumber())
                .eq(SoB2cEntity::getDictPlatform, PlatformDictEnum.SHOPIFY.getCode())
                .eq(SoB2cEntity::getBillStatus, SoB2cBillStatusEnum.ENUM_SHIPPED.getCode())
                .list();
        List<SoB2cLogisticsEntity> logisticsEntityList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(soB2cList)) {
            List<String> mainIds = soB2cList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            List<SoB2cReceiverEntity> receiverEntityList = soB2cReceiverService.listByMainIds(mainIds);
            if (CollectionUtils.isNotEmpty(receiverEntityList)) {
                boolean contactMatch = receiverEntityList.stream()
                        .anyMatch(receiver -> receiver.getEmail().equals(dto.getContactInfo()) || receiver.getTelNumber().equals(dto.getContactInfo()));
                if (contactMatch) {
                    logisticsEntityList = soB2cLogisticsService.lambdaQuery()
                            .eq(SoB2cLogisticsEntity::getMainId, mainIds)
                            .list();
                }
            }
        }
        return Pair.of(soB2cList, logisticsEntityList);
    }

}
