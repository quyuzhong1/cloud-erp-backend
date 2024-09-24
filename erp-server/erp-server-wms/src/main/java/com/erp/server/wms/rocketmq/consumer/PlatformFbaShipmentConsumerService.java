package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.business.dto.PlatformFbaShipmentReceiveDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.dto.MongoDBUpdateDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.convert.FbaShipmentConsumerConverter;
import com.erp.server.wms.service.CfgAmzFulfillmentCenterService;
import com.erp.server.wms.service.FbaShipmentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 下载FBA货件消费服务
 *
 * @author Jim
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_fba_shipment_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_fba_shipment_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformFbaShipmentConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private FbaShipmentService fbaShipmentService;
    @Resource
    private SkuMappingFeign skuMappingFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private CfgAmzFulfillmentCenterService cfgAmzFulfillmentCenterService;
    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;


    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        dmpTaskFeign.updateSyncInfo(paramDTO);
    }

    @Override
    public void updateMongodbData(String platform,String uniqueId, Integer isClean){
        if (StringUtils.isEmpty(uniqueId) || StringUtils.isEmpty(platform) || Objects.isNull(isClean)){
            return;
        }
        MongoDBUpdateDTO dto = MongoDBUpdateDTO.builder()
                .tableName(getTableName(platform))
                .uniqueId(uniqueId)
                .isClean(isClean)
                .build();
        dmpMongoDbFeign.updateMongoDbData(dto);
    }

    /**
     * 根据平台组装表名
     */
    private String getTableName(String platform){
        return StrUtil.format("{}_{}_{}", PlatformCategoryEnum.THIRD_SYSTEM.getCode(),
                platform, BusinessTypeEnum.FBA_SHIPMENT.getCode());
    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        dmpTaskFeign.sendWarnMsg(syncTaskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {
        PlatformFbaShipmentDTO dto = JSONUtil.toBean(ext.toString(), PlatformFbaShipmentDTO.class);
        log.info("[Fba货件] 消费:dto={}", JSONUtil.toJsonStr(dto));
        // 组合信息
        FbaShipmentEntity entity = FbaShipmentConsumerConverter.INSTANCE.fbaShipmentToEntity(dto);

        // 签收信息
        List<PlatformFbaShipmentReceiveDTO> receiveDTOList = dto.getReceiveDTOList();

        // 填充最新签收时间
        if (!CollectionUtils.isEmpty(dto.getReceiveDTOList())){
            List<PlatformFbaShipmentReceiveDTO> receiveTimeDTO = receiveDTOList.stream().filter(e -> e.getReceiveQty() > 0).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(receiveTimeDTO)) {
                LocalDateTime maxReceiveTime = receiveTimeDTO.stream()
                        .map(PlatformFbaShipmentReceiveDTO::getReceiveDate)
                        .max(LocalDateTime::compareTo)
                        .get();
                entity.setShipmentReceiveTime(maxReceiveTime);
            }
        }

        // 卖家SKU列表
        List<String> sellerSkuList = dto.getDetailList().stream().map(PlatformFbaShipmentReceiveDTO::getSellerSku).distinct().collect(Collectors.toList());
        // SKU绑定的信息
        Map<String, ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingDTOMap = new HashMap<>();
        // sku是否是组合类型
        List<String> hasChildrenSkuIds = new ArrayList<>();

        // 查询当前店铺
        ShopInfoEntity currentShopEntity = shopInfoFeign.getShopInfoById(entity.getShopId());

        // 查询仓库中心对应国家并设置对应店铺
        checkAndSetCountryWithShop(entity, dto, currentShopEntity);

        // 根据仓储中心店铺查询对应sku映射
        if (!CollectionUtils.isEmpty(sellerSkuList)) {
            ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
            paramDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
            paramDTO.setPlatformSkuNoList(sellerSkuList);
            paramDTO.setShopIdList(Collections.singletonList(entity.getShopId()));
            paramDTO.setType(RuleTypeEnum.PLATFORM.getCode());
            paramDTO.setMatchResult(true);
            paramDTO.setIsExpire(false);
            // 查询ListingInfo和skuMapping的关系
            List<ListingInfoWithSkuMappingDTO> listingedInfoWithSkuMappingList = skuMappingFeign.listingInfoWithSkuMappingList(paramDTO);

            // SKU相关信息
            listingInfoWithSkuMappingDTOMap = listingedInfoWithSkuMappingList
                    .stream()
                    .collect(Collectors.toMap(ListingInfoWithSkuMappingDTO::getPlatformSkuNo, Function.identity()));

            List<String> erpSkuIds = listingedInfoWithSkuMappingList
                    .stream()
                    .map(ListingInfoWithSkuMappingDTO::getProductSkuId)
                    .filter(e -> !StringUtils.isEmpty(e))
                    .distinct()
                    .collect(Collectors.toList());

            // 组合类型skuIds
            hasChildrenSkuIds = plmTaskFeign.listBomChildBySkuIds(erpSkuIds)
                    .stream()
                    .map(BomChildrenSkuDTO::getParentSkuId)
                    .collect(Collectors.toList());
        }

        // 查询国家信息
        DictCountryEntity countryEntity = sysUserFeign.getCountryById(dto.getCountryId());
        entity.setCountryName(null != countryEntity ? countryEntity.getNameCn() : "");

        // 新增或更新
        FbaShipmentEntity oldEntity = fbaShipmentService.getByFbaShipmentId(entity.getFbaShipmentId());
        if (null == oldEntity) {
            // 新增
            fbaShipmentService.checkAndSaveAll(entity, listingInfoWithSkuMappingDTOMap, hasChildrenSkuIds, receiveDTOList, dto.checkAndGetDetailList());
        } else {
            // 修改
            fbaShipmentService.checkAndUpdateAll(oldEntity, entity, listingInfoWithSkuMappingDTOMap, hasChildrenSkuIds, receiveDTOList, dto.checkAndGetDetailList());
        }

        return ApiResult.success();
    }

    /**
     * 查询仓库中心对应国家并设置对应店铺
     *
     * @param entity            来源实体
     * @param dto
     * @param currentShopEntity
     */
    private void checkAndSetCountryWithShop(FbaShipmentEntity entity, PlatformFbaShipmentDTO dto, ShopInfoEntity currentShopEntity) {
        // 查询仓库中心对应国家
        String country = cfgAmzFulfillmentCenterService.findCountryByCode(entity.getFulfillmentCenter());
        // 没有配置处理
        if (StringUtils.isEmpty(country)) {
            log.error("未找到系统仓储中心:{}", entity.getFulfillmentCenter());
            try {
                // 未找到系统仓储中心发送预警, 不影响主流程
                dmpTaskFeign.sendWarnMsg(dto.getDmpSyncTaskId());
            } catch (Exception e) {
                log.error("未找到系统仓储中心,发送预警失败:code={}, error={}", entity.getFulfillmentCenter(), ExceptionUtil.stacktraceToString(e, 2000));
            }
            return;
        }
        if (StringUtils.isEmpty(currentShopEntity.getDictCountryCode())){
            throw new ServiceException("店铺数据异常:国家为空，shopId=" +  currentShopEntity.getId());
        }

        // 国家一致
        if (currentShopEntity.getDictCountryCode().equalsIgnoreCase(country)) {
            return;
        }
        // 查询对应sellerId的国家店铺
        ShopInfoDTO.RelatedDTO requestDTO = new ShopInfoDTO.RelatedDTO();
        requestDTO.setCountry(country);
        requestDTO.setShopId(entity.getShopId());
        ShopInfoEntity shopInfoEntity = shopInfoFeign.getRelatedShopByIdAndCountry(requestDTO);
        if (null == shopInfoEntity){
            String msg = StrUtil.format("仓库中心对应国家的店铺未授权, shopId={}, country={}", entity.getShopId(), country);
            throw new ServiceException(msg);
        }
        entity.setShopId(shopInfoEntity.getId());
        entity.setShopName(shopInfoEntity.getName());
    }
}
