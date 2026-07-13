package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.PlatformReturnInstockDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractRestCloudPlatformConsumerHandler;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.oms.enums.ListingMatchResultEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cReturnStatusEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoB2cReturnEntity;
import com.erp.model.oms.entity.SoB2cReturnDetailEntity;
import com.erp.model.wms.dto.SoReturnPrestockDTO;
import com.erp.model.wms.dto.SoReturnPrestockDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.service.*;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 下载平台退货入库数据消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.RESTCLOUD_PLATFORM_RETURN_INSTOCK_TO_WMS_TOPIC,
selectorExpression = RocketMqNewTag.RESTCLOUD_PLATFORM_RETURN_INSTOCK_TO_WMS_TAG,
consumerGroup = RocketMqNewConsumerGroup.RESTCLOUD_PLATFORM_RETURN_INSTOCK_TO_WMS_GROUP,
consumeMode = ConsumeMode.ORDERLY)
public class RestCloudPlatformNewReturnInstockConsumerService extends AbstractRestCloudPlatformConsumerHandler {

	@Resource
	private SoReturnInstockService soReturnInstockService;

	@Resource
	private SkuMappingFeign skuMappingFeign;

	@Resource
	private WarehouseService warehouseService;

	@Resource
	private SysUserFeign sysUserFeign;

	@Resource
	private CustomerFeign customerFeign;

	@Resource
	private ShopInfoFeign shopInfoFeign;

	@Resource
	private SoOutstockService soOutstockService;

	@Resource
	private OverseasProviderWarehouseService overseasProviderWarehouseService;

	@Resource
	private SoB2cFeign soB2cFeign;

	@Resource
	private SoInfoFeign soInfoFeign;

	@Resource
	private SoOutstockDetailService soOutstockDetailService;

	@Resource
	private InventoryClosedRecordService inventoryClosedRecordService;

    @Resource
    private SoReturnInstockDetailService soReturnInstockDetailService;

	@Resource
	private SoReturnPrestockService soReturnPrestockService;

	@Resource
	private SoB2cReturnFeign soB2cReturnFeign;

	@Resource
	private ThirdWarehouseDeliveryService thirdWarehouseDeliveryService;

	@Resource
	private B2bThirdDeliveryService b2bThirdDeliveryService;

	@Resource
	private DmpTaskFeign dmpTaskFeign;

	@Resource
	private SoReturnInstockSalesMatchService soReturnInstockSalesMatchService;


	@Override
	public String getBizName() {
		return "平台退货入库";
	}

	@Override
	public void handle(String data) {
		PlatformReturnInstockDTO dto = JSONUtil.toBean(data, PlatformReturnInstockDTO.class);
		if (Objects.isNull(dto.getPutawayTime())) {
			// putawayTime缺失将由getPutawayLocalDate()兜底createTime/当前日期，此处打日志便于后续核对单据日期是否与真实业务日期一致
			log.warn("[平台退货入库]上架完成时间(putawayTime)缺失，将兜底createTime/当前日期：platform={}，平台单号={}，平台退货单号={}",
					dto.getPlatform(), dto.getPlatformOrderNo(), dto.getPlatformReturnOrderNo());
		}
		if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(dto.getPlatform()) || PlatformDictEnum.NASDAQ_JD.getCode().equalsIgnoreCase(dto.getPlatform())){
			String thisPlatform = PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(dto.getPlatform()) ?  PlatformDictEnum.AMAZON.getCode() : PlatformDictEnum.NASDAQ_JD.getCode();
			dto.setPlatform(thisPlatform);
			// 平台仓入库处理
			platformWarehouseHandle(dto);
		} else {
			// 海外仓入库处理
			overseasWarehouseHandle(dto);
		}
	}

    /**
     * 海外仓入库平台处理
     */
    public void overseasWarehouseHandle(PlatformReturnInstockDTO dto) {
        if(Objects.isNull(dto) || CharSequenceUtil.isBlank(dto.getAuthId())){
            return;
        }
        //根据产品条码查询sku
        handleThirdBarcode(dto);

        if (PlatformDictEnum.GOOD_CANG.getCode().equalsIgnoreCase(dto.getPlatform())){
            if (CollectionUtils.isEmpty(dto.getProductDetailList())){
                ServiceException.runError("谷仓退货入库明细流水为空");
            }
            String thirdId = dto.getProductDetailList().get(0).getThirdId();
            if (StringUtils.isBlank(thirdId)){
                ServiceException.runError("谷仓退货入库明细流水thirdId不能为空");
            }
            // 谷仓按明细ID判断
            Integer count = soReturnInstockDetailService.lambdaQuery()
                    .eq(SoReturnInstockDetailEntity::getSourceDetailId, thirdId)
                    .eq(SoReturnInstockDetailEntity::getCreateUserId, dto.getAuthId())
                    .count();
            if (count > 0) {
                return;
            }
        }else if (PlatformDictEnum.DA_MAI.getCode().equalsIgnoreCase(dto.getPlatform())){
            SoReturnInstockEntity exist = soReturnInstockService.getBySourceId(dto.getSourceId());
            if(Objects.nonNull(exist)){
                return;
            }
        } else {
            SoReturnInstockEntity existEntity = soReturnInstockService.getByThirdCode(dto.getPlatformReturnOrderNo());
            if (Objects.nonNull(existEntity)) {
                return;
            }
        }

		if (isLegacySpecialWarehousePlatform(dto.getPlatform())) {
			// 众包/极兔/艾姆勒/通邮：金额计算、B2B三方发货单匹配等能力为统一匹配流程未覆盖的历史专属逻辑，独立分支处理，不进入下方统一匹配
			handleLegacySpecialWarehouse(dto);
			return;
		}

		// 仓库统一走 overseasProviderWarehouseService 解析
		// （艾姆勒 IML / 统佑历史上依赖订单出库明细解析仓库，此处暂不支持，解析不到时记录 error 并跳过，
		// 属于已知限制，留给后续实现「留空」分支时一并重新设计）
		OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = overseasProviderWarehouseService.getByPlatform(dto.getAuthId(), dto.getWarehouseCode());
		if (Objects.isNull(overseasProviderWarehouseEntity) || CharSequenceUtil.isBlank(overseasProviderWarehouseEntity.getWarehouseId())) {
			log.error("[海外仓退货入库-无头件] 仓库信息缺失，跳过生成预入库单：platform={}, warehouseCode={}, thirdCode={}",
					dto.getPlatform(), dto.getWarehouseCode(), dto.getPlatformReturnOrderNo());
			// TODO 已知限制：艾姆勒IML/统佑等平台的仓库解析依赖订单出库明细，当前未支持，消息将被跳过且不重试；
			// 后续补充「留空仓库」分支后需要重新评估是否需要落地补偿表/告警，而不是仅记录日志
			return;
		}
		WarehouseEntity warehouseEntity = warehouseService.getById(overseasProviderWarehouseEntity.getWarehouseId());

		if (CharSequenceUtil.isNotBlank(dto.getReturnLogisticCode())) {
			// 按退货物流单号匹配B2B/B2C售后单：命中部分直接生成已审核退货入库单，未匹配的剩余数量带着继续走后续分支
			List<PlatformReturnInstockDTO.Detail> remaining = soReturnInstockService.matchAndCreateByReturnLogisticCode(dto, warehouseEntity);
			if (CollectionUtils.isEmpty(remaining)) {
				return;
			}
			dto.setProductDetailList(remaining);
		}
		if (CharSequenceUtil.isNotBlank(dto.getOrderReferenceNo())) {
			// 先按参考单号匹配售后单/退货单/销售订单，匹配不到再尝试按三方仓发货单号匹配，两者均无匹配则生成无关联预入库单
			if (handleReferenceNoAfterSaleMatch(dto, warehouseEntity)) {
				return;
			}
			if (handleWfhdReferenceNo(dto, warehouseEntity)) {
				return;
			}
		}
		// 参考单号无匹配或无参考单号：生成预入库单
		this.createSoReturnPrestockHeadless(dto, warehouseEntity);
	}

	/**
	 * 按三方仓发货单号查询对应B2C销售订单，命中则生成已审核退货入库单并返回 true，否则返回 false
	 */
	private boolean handleWfhdReferenceNo(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity) {
		ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = thirdWarehouseDeliveryService.getLatestByCode(dto.getOrderReferenceNo());
		if (Objects.nonNull(thirdWarehouseDeliveryEntity) && CharSequenceUtil.isNotBlank(thirdWarehouseDeliveryEntity.getSoCode())) {
			SoB2cEntity soB2cEntity = soB2cFeign.getSoCode(thirdWarehouseDeliveryEntity.getSoCode());
			if (Objects.nonNull(soB2cEntity)) {
				generateInstockBySo(dto, warehouseEntity, soB2cEntity);
				return true;
			}
		}
		return false;
	}

	/**
	 * 按参考单号依次匹配《B2C售后单-退货单》、《B2B/B2C销售订单》，命中则生成退货入库单并返回 true，均未匹配返回 false
	 */
	private boolean handleReferenceNoAfterSaleMatch(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity) {
		List<SoB2cReturnEntity> candidates = matchB2cReturnCandidates(dto.getOrderReferenceNo());
		SoB2cReturnEntity matchedReturn = CollectionUtils.isEmpty(candidates) ? null : pickReturnBySkuMatch(candidates, dto);
		if (Objects.nonNull(matchedReturn)) {
			if (generateInstockByMatchedReturn(dto, warehouseEntity, matchedReturn)) {
				return true;
			}
		}
		Set<String> incomingSkuIds = resolveSkuIds(dto);
		SoB2cEntity soB2cEntity = matchB2cSoByReferenceNo(dto.getOrderReferenceNo(), incomingSkuIds);
		if (Objects.nonNull(soB2cEntity)) {
			generateInstockBySo(dto, warehouseEntity, soB2cEntity);
			return true;
		}
		SoInfoEntity soInfoEntity = matchB2bSoByReferenceNo(dto.getOrderReferenceNo(), incomingSkuIds);
		if (Objects.nonNull(soInfoEntity)) {
			generateInstockBySoInfo(dto, warehouseEntity, soInfoEntity);
			return true;
		}
		return false;
	}

	/**
	 * 按参考单号匹配《B2C售后单-退货单》候选列表：命中平台订单号、平台退货单号、销售单号、退货单号任一字段即算候选。
	 * 服务端一次查询完成四字段 OR 匹配，避免多次 Feign 往返。
	 */
	private List<SoB2cReturnEntity> matchB2cReturnCandidates(String orderReferenceNo) {
		List<SoB2cReturnEntity> candidates = soB2cReturnFeign.listByAnyReferenceNo(orderReferenceNo);
		return CollectionUtils.isEmpty(candidates) ? new ArrayList<>() : candidates;
	}

	/**
	 * 按SKU维度从候选退货单中挑选可匹配的一条：优先"待退货"状态，同状态内取第一个SKU命中的
	 */
	private SoB2cReturnEntity pickReturnBySkuMatch(List<SoB2cReturnEntity> candidates, PlatformReturnInstockDTO dto) {
		Set<String> incomingSkuIds = resolveSkuIds(dto);
		if (CollectionUtils.isEmpty(incomingSkuIds)) {
			return null;
		}
		List<SoB2cReturnEntity> sorted = candidates.stream()
				.sorted(Comparator.comparing(v -> SoB2cReturnStatusEnum.TO_BE_RETURNED.getCode().equals(v.getStatus()) ? 0 : 1))
				.collect(Collectors.toList());
		for (SoB2cReturnEntity candidate : sorted) {
			List<SoB2cReturnDetailEntity> detailList = FeignQuery.create(SoB2cReturnDetailEntity.class)
					.eq(SoB2cReturnDetailEntity::getMainId, candidate.getId()).list();
			boolean skuMatched = detailList.stream()
					.anyMatch(d -> StringUtils.isNotBlank(d.getSkuId()) && incomingSkuIds.contains(d.getSkuId()));
			if (skuMatched) {
				return candidate;
			}
		}
		return null;
	}

	/**
	 * 解析退货入库明细对应的ERP SKU ID集合（仅保留已成功映射的）
	 */
	private Set<String> resolveSkuIds(PlatformReturnInstockDTO dto) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		if (CollectionUtils.isEmpty(details)) {
			return Collections.emptySet();
		}
		List<String> platformSkuNoList = details.stream().map(PlatformReturnInstockDTO.Detail::getProductSku).collect(Collectors.toList());
		ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
		listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
		listingInfoParamDTO.setAuthId(dto.getAuthId());
		listingInfoParamDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
		List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
		return mappingSkuViewDTOList.stream()
				.map(SkuMappingDTO.MappingSkuViewDTO::getProductSkuId)
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.toSet());
	}

	/**
	 * 命中退货单后生成《已审核-退货入库单》，并将本次入库与该退货单及其SKU匹配明细关联
	 *
	 * @return 是否生成成功；售后单/店铺数据不一致导致无法生成时返回 false，交由调用方回退到下一步匹配
	 */
	private boolean generateInstockByMatchedReturn(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoB2cReturnEntity matchedReturn) {
		SoB2cEntity soB2cEntity = soB2cFeign.getById(matchedReturn.getSoId());
		if (Objects.isNull(soB2cEntity)) {
			log.warn("[海外仓退货入库] 匹配到退货单{}但对应销售订单不存在，回退到销售订单匹配：soId={}", matchedReturn.getCode(), matchedReturn.getSoId());
			return false;
		}
		ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(soB2cEntity.getShopId());
		if (Objects.isNull(shopInfoEntity)) {
			log.warn("[海外仓退货入库] 匹配到退货单{}但对应店铺不存在，回退到销售订单匹配：shopId={}", matchedReturn.getCode(), soB2cEntity.getShopId());
			return false;
		}
		List<SoReturnInstockDetailEntity> detailEntityList = this.buildPlatformSoReturnInstockDetail(dto, warehouseEntity, soB2cEntity);
		if (CollectionUtils.isEmpty(detailEntityList)) {
			ServiceException.runError("【海外仓退货入库】来源明细未匹配到映射");
		}
		SoReturnInstockEntity soReturnInstockEntity = this.buildPlatformSoReturnInstockEntity(dto, warehouseEntity, soB2cEntity, shopInfoEntity);

		// 关联已匹配到的退货单：按SKU回写明细的退货单明细ID，退货单状态由待退货流转为已退货
		List<SoB2cReturnDetailEntity> matchedDetailList = FeignQuery.create(SoB2cReturnDetailEntity.class)
				.eq(SoB2cReturnDetailEntity::getMainId, matchedReturn.getId()).list();
		for (SoReturnInstockDetailEntity soReturnInstockDetailEntity : detailEntityList) {
			matchedDetailList.stream()
					.filter(v -> v.getSkuId().equals(soReturnInstockDetailEntity.getSkuId()))
					.findFirst()
					.ifPresent(v -> soReturnInstockDetailEntity.setSoReturnDetailId(v.getId()));
		}
		soReturnInstockEntity.setSoReturnId(matchedReturn.getId());
		soReturnInstockEntity.setSoReturnCode(matchedReturn.getCode());
		if (SoB2cReturnStatusEnum.TO_BE_RETURNED.getCode().equals(matchedReturn.getStatus())) {
			matchedReturn.setStatus(SoB2cReturnStatusEnum.RETURNED.getCode());
			soB2cReturnFeign.updateBatch(Collections.singletonList(matchedReturn));
		}

		soReturnInstockService.addByThirdWarehouse(soReturnInstockEntity, detailEntityList);
		return true;
	}

	/**
	 * 按参考单号匹配《B2C销售订单》：平台订单号或销售单号=参考单号，且需状态为"已发货"、SKU 命中其一。
	 * 可匹配数据范围：B2C销售订单-已发货
	 */
	private SoB2cEntity matchB2cSoByReferenceNo(String orderReferenceNo, Set<String> incomingSkuIds) {
		List<SoB2cEntity> candidates = new ArrayList<>();
		List<SoB2cEntity> byPlatformCode = soB2cFeign.getSoB2cByPlatformCode(orderReferenceNo);
		if (CollectionUtils.isNotEmpty(byPlatformCode)) {
			candidates.addAll(byPlatformCode);
		}
		SoB2cEntity bySoCode = soB2cFeign.getSoCode(orderReferenceNo);
		if (Objects.nonNull(bySoCode)) {
			candidates.add(bySoCode);
		}
		return candidates.stream()
				.filter(v -> !Boolean.TRUE.equals(v.getInvalidStatus()))
				.filter(v -> SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(v.getBillStatus()))
				.filter(v -> soB2cHasMatchingSku(v.getId(), incomingSkuIds))
				.findFirst()
				.orElse(null);
	}

	/**
	 * 校验B2C销售订单明细是否存在命中的ERP SKU
	 */
	private boolean soB2cHasMatchingSku(String soId, Set<String> incomingSkuIds) {
		if (CollectionUtils.isEmpty(incomingSkuIds)) {
			return false;
		}
		List<SoB2cDetailEntity> detailList = soB2cFeign.listDetailByMainIds(Collections.singletonList(soId));
		return CollectionUtils.isNotEmpty(detailList) && detailList.stream()
				.anyMatch(d -> StringUtils.isNotBlank(d.getSkuId()) && incomingSkuIds.contains(d.getSkuId()));
	}

		if (isLegacySpecialWarehousePlatform(dto.getPlatform())) {
			// 众包/极兔/艾姆勒/通邮：金额计算、B2B三方发货单匹配等能力为统一匹配流程未覆盖的历史专属逻辑，独立分支处理，不进入下方统一匹配
			handleLegacySpecialWarehouse(dto);
			return;
		}

		// 仓库统一走 overseasProviderWarehouseService 解析
		// （艾姆勒 IML / 统佑历史上依赖订单出库明细解析仓库，此处暂不支持，解析不到时记录 error 并跳过，
		// 属于已知限制，留给后续实现「留空」分支时一并重新设计）
		OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = overseasProviderWarehouseService.getByPlatform(dto.getAuthId(), dto.getWarehouseCode());
		if (Objects.isNull(overseasProviderWarehouseEntity) || CharSequenceUtil.isBlank(overseasProviderWarehouseEntity.getWarehouseId())) {
			log.error("[海外仓退货入库-无头件] 仓库信息缺失，跳过生成预入库单：platform={}, warehouseCode={}, thirdCode={}",
					dto.getPlatform(), dto.getWarehouseCode(), dto.getPlatformReturnOrderNo());
			// TODO 已知限制：艾姆勒IML/统佑等平台的仓库解析依赖订单出库明细，当前未支持，消息将被跳过且不重试；
			// 后续补充「留空仓库」分支后需要重新评估是否需要落地补偿表/告警，而不是仅记录日志
			return;
		}
		WarehouseEntity warehouseEntity = warehouseService.getById(overseasProviderWarehouseEntity.getWarehouseId());

		if (CharSequenceUtil.isNotBlank(dto.getReturnLogisticCode())) {
			// 按退货物流单号匹配B2B/B2C售后单：命中部分直接生成已审核退货入库单，未匹配的剩余数量带着继续走后续分支
			List<PlatformReturnInstockDTO.Detail> remaining = soReturnInstockService.matchAndCreateByReturnLogisticCode(dto, warehouseEntity);
			if (CollectionUtils.isEmpty(remaining)) {
				return;
			}
			dto.setProductDetailList(remaining);
		}
		if (CharSequenceUtil.isNotBlank(dto.getOrderReferenceNo())) {
			// 先按参考单号匹配售后单/退货单/销售订单，匹配不到再尝试按三方仓发货单号匹配，两者均无匹配则生成无关联预入库单
			if (handleReferenceNoAfterSaleMatch(dto, warehouseEntity)) {
				return;
			}
			if (handleWfhdReferenceNo(dto, warehouseEntity)) {
				return;
			}
		}
		// 参考单号无匹配或无参考单号：生成预入库单
		this.createSoReturnPrestockHeadless(dto, warehouseEntity);
	}

	/**
	 * 按三方仓发货单号查询对应B2C销售订单，命中则生成已审核退货入库单并返回 true，否则返回 false
	 */
	private boolean handleWfhdReferenceNo(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity) {
		ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = thirdWarehouseDeliveryService.getLatestByCode(dto.getOrderReferenceNo());
		if (Objects.nonNull(thirdWarehouseDeliveryEntity) && CharSequenceUtil.isNotBlank(thirdWarehouseDeliveryEntity.getSoCode())) {
			SoB2cEntity soB2cEntity = soB2cFeign.getSoCode(thirdWarehouseDeliveryEntity.getSoCode());
			if (Objects.nonNull(soB2cEntity)) {
				generateInstockBySo(dto, warehouseEntity, soB2cEntity);
				return true;
			}
		}
		return false;
	}

	/**
	 * 按参考单号依次匹配《B2C售后单-退货单》、《B2B/B2C销售订单》，命中则生成退货入库单并返回 true，均未匹配返回 false
	 */
	private boolean handleReferenceNoAfterSaleMatch(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity) {
		List<SoB2cReturnEntity> candidates = matchB2cReturnCandidates(dto.getOrderReferenceNo());
		SoB2cReturnEntity matchedReturn = CollectionUtils.isEmpty(candidates) ? null : pickReturnBySkuMatch(candidates, dto);
		if (Objects.nonNull(matchedReturn)) {
			if (generateInstockByMatchedReturn(dto, warehouseEntity, matchedReturn)) {
				return true;
			}
		}
		Set<String> incomingSkuIds = resolveSkuIds(dto);
		SoB2cEntity soB2cEntity = matchB2cSoByReferenceNo(dto.getOrderReferenceNo(), incomingSkuIds);
		if (Objects.nonNull(soB2cEntity)) {
			generateInstockBySo(dto, warehouseEntity, soB2cEntity);
			return true;
		}
		SoInfoEntity soInfoEntity = matchB2bSoByReferenceNo(dto.getOrderReferenceNo(), incomingSkuIds);
		if (Objects.nonNull(soInfoEntity)) {
			generateInstockBySoInfo(dto, warehouseEntity, soInfoEntity);
			return true;
		}
		return false;
	}

	/**
	 * 按参考单号匹配《B2C售后单-退货单》候选列表：命中平台订单号、平台退货单号、销售单号、退货单号任一字段即算候选。
	 * 服务端一次查询完成四字段 OR 匹配，避免多次 Feign 往返。
	 */
	private List<SoB2cReturnEntity> matchB2cReturnCandidates(String orderReferenceNo) {
		List<SoB2cReturnEntity> candidates = soB2cReturnFeign.listByAnyReferenceNo(orderReferenceNo);
		return CollectionUtils.isEmpty(candidates) ? new ArrayList<>() : candidates;
	}

	/**
	 * 按SKU维度从候选退货单中挑选可匹配的一条：优先"待退货"状态，同状态内取第一个SKU命中的
	 */
	private SoB2cReturnEntity pickReturnBySkuMatch(List<SoB2cReturnEntity> candidates, PlatformReturnInstockDTO dto) {
		Set<String> incomingSkuIds = resolveSkuIds(dto);
		if (CollectionUtils.isEmpty(incomingSkuIds)) {
			return null;
		}
		List<SoB2cReturnEntity> sorted = candidates.stream()
				.sorted(Comparator.comparing(v -> SoB2cReturnStatusEnum.TO_BE_RETURNED.getCode().equals(v.getStatus()) ? 0 : 1))
				.collect(Collectors.toList());
		for (SoB2cReturnEntity candidate : sorted) {
			List<SoB2cReturnDetailEntity> detailList = FeignQuery.create(SoB2cReturnDetailEntity.class)
					.eq(SoB2cReturnDetailEntity::getMainId, candidate.getId()).list();
			boolean skuMatched = detailList.stream()
					.anyMatch(d -> StringUtils.isNotBlank(d.getSkuId()) && incomingSkuIds.contains(d.getSkuId()));
			if (skuMatched) {
				return candidate;
			}
		}
		return null;
	}

	/**
	 * 解析退货入库明细对应的ERP SKU ID集合（仅保留已成功映射的）
	 */
	private Set<String> resolveSkuIds(PlatformReturnInstockDTO dto) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		if (CollectionUtils.isEmpty(details)) {
			return Collections.emptySet();
		}
		List<String> platformSkuNoList = details.stream().map(PlatformReturnInstockDTO.Detail::getProductSku).collect(Collectors.toList());
		ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
		listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
		listingInfoParamDTO.setAuthId(dto.getAuthId());
		listingInfoParamDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
		List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
		return mappingSkuViewDTOList.stream()
				.map(SkuMappingDTO.MappingSkuViewDTO::getProductSkuId)
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.toSet());
	}

	/**
	 * 命中退货单后生成《已审核-退货入库单》，并将本次入库与该退货单及其SKU匹配明细关联
	 *
	 * @return 是否生成成功；售后单/店铺数据不一致导致无法生成时返回 false，交由调用方回退到下一步匹配
	 */
	private boolean generateInstockByMatchedReturn(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoB2cReturnEntity matchedReturn) {
		SoB2cEntity soB2cEntity = soB2cFeign.getById(matchedReturn.getSoId());
		if (Objects.isNull(soB2cEntity)) {
			log.warn("[海外仓退货入库] 匹配到退货单{}但对应销售订单不存在，回退到销售订单匹配：soId={}", matchedReturn.getCode(), matchedReturn.getSoId());
			return false;
		}
		ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(soB2cEntity.getShopId());
		if (Objects.isNull(shopInfoEntity)) {
			log.warn("[海外仓退货入库] 匹配到退货单{}但对应店铺不存在，回退到销售订单匹配：shopId={}", matchedReturn.getCode(), soB2cEntity.getShopId());
			return false;
		}
		List<SoReturnInstockDetailEntity> detailEntityList = this.buildPlatformSoReturnInstockDetail(dto, warehouseEntity, soB2cEntity);
		if (CollectionUtils.isEmpty(detailEntityList)) {
			ServiceException.runError("【海外仓退货入库】来源明细未匹配到映射");
		}
		SoReturnInstockEntity soReturnInstockEntity = this.buildPlatformSoReturnInstockEntity(dto, warehouseEntity, soB2cEntity, shopInfoEntity);

		// 关联已匹配到的退货单：按SKU回写明细的退货单明细ID，退货单状态由待退货流转为已退货
		List<SoB2cReturnDetailEntity> matchedDetailList = FeignQuery.create(SoB2cReturnDetailEntity.class)
				.eq(SoB2cReturnDetailEntity::getMainId, matchedReturn.getId()).list();
		for (SoReturnInstockDetailEntity soReturnInstockDetailEntity : detailEntityList) {
			matchedDetailList.stream()
					.filter(v -> v.getSkuId().equals(soReturnInstockDetailEntity.getSkuId()))
					.findFirst()
					.ifPresent(v -> soReturnInstockDetailEntity.setSoReturnDetailId(v.getId()));
		}
		soReturnInstockEntity.setSoReturnId(matchedReturn.getId());
		soReturnInstockEntity.setSoReturnCode(matchedReturn.getCode());
		if (SoB2cReturnStatusEnum.TO_BE_RETURNED.getCode().equals(matchedReturn.getStatus())) {
			matchedReturn.setStatus(SoB2cReturnStatusEnum.RETURNED.getCode());
			soB2cReturnFeign.updateBatch(Collections.singletonList(matchedReturn));
		}

		soReturnInstockService.addByThirdWarehouse(soReturnInstockEntity, detailEntityList);
		return true;
	}

	/**
	 * 按参考单号匹配《B2C销售订单》：平台订单号或销售单号=参考单号，且需状态为"已发货"、SKU 命中其一。
	 * 可匹配数据范围：B2C销售订单-已发货
	 */
	private SoB2cEntity matchB2cSoByReferenceNo(String orderReferenceNo, Set<String> incomingSkuIds) {
		List<SoB2cEntity> candidates = new ArrayList<>();
		List<SoB2cEntity> byPlatformCode = soB2cFeign.getSoB2cByPlatformCode(orderReferenceNo);
		if (CollectionUtils.isNotEmpty(byPlatformCode)) {
			candidates.addAll(byPlatformCode);
		}
		SoB2cEntity bySoCode = soB2cFeign.getSoCode(orderReferenceNo);
		if (Objects.nonNull(bySoCode)) {
			candidates.add(bySoCode);
		}
		return candidates.stream()
				.filter(v -> !Boolean.TRUE.equals(v.getInvalidStatus()))
				.filter(v -> SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(v.getBillStatus()))
				.filter(v -> soB2cHasMatchingSku(v.getId(), incomingSkuIds))
				.findFirst()
				.orElse(null);
	}

	/**
	 * 校验B2C销售订单明细是否存在命中的ERP SKU
	 */
	private boolean soB2cHasMatchingSku(String soId, Set<String> incomingSkuIds) {
		if (CollectionUtils.isEmpty(incomingSkuIds)) {
			return false;
		}
		List<SoB2cDetailEntity> detailList = soB2cFeign.listDetailByMainIds(Collections.singletonList(soId));
		return CollectionUtils.isNotEmpty(detailList) && detailList.stream()
				.anyMatch(d -> StringUtils.isNotBlank(d.getSkuId()) && incomingSkuIds.contains(d.getSkuId()));
	}

	/**
	 * 按参考单号匹配《B2B销售订单》：平台订单号或销售单号=参考单号，且需未作废、SKU 命中其一。
	 * 可匹配数据范围：B2B销售订单-作废状态：未作废
	 */
	private SoInfoEntity matchB2bSoByReferenceNo(String orderReferenceNo, Set<String> incomingSkuIds) {
		List<SoInfoEntity> candidates = new ArrayList<>();
		List<SoInfoEntity> byPlatformOrderCode = soInfoFeign.getByPlatformOrderCode(orderReferenceNo);
		if (CollectionUtils.isNotEmpty(byPlatformOrderCode)) {
			candidates.addAll(byPlatformOrderCode);
		}
		SoInfoEntity byCode = soInfoFeign.getByCode(orderReferenceNo);
		if (Objects.nonNull(byCode)) {
			candidates.add(byCode);
		}
		return candidates.stream()
				.filter(v -> !Boolean.TRUE.equals(v.getInvalidStatus()))
				.filter(v -> soInfoHasMatchingSku(v.getId(), incomingSkuIds))
				.findFirst()
				.orElse(null);
	}

	/**
	 * 校验B2B销售订单明细是否存在命中的ERP SKU
	 */
	private boolean soInfoHasMatchingSku(String soInfoId, Set<String> incomingSkuIds) {
		if (CollectionUtils.isEmpty(incomingSkuIds)) {
			return false;
		}
		List<SoDetailEntity> detailList = soInfoFeign.listSoDetailByMainIds(Collections.singletonList(soInfoId));
		return CollectionUtils.isNotEmpty(detailList) && detailList.stream()
				.anyMatch(d -> StringUtils.isNotBlank(d.getSkuId()) && incomingSkuIds.contains(d.getSkuId()));
	}

	/**
	 * 命中销售订单后生成《已审核-退货入库单》，字段映射与已存在销售订单场景（{@link #createByExistSoB2c}）保持一致，
	 * 仅仓库改用海外仓映射的 warehouseEntity
	 */
	private void generateInstockBySo(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoB2cEntity soB2cEntity) {
		ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(soB2cEntity.getShopId());
		if (Objects.isNull(shopInfoEntity)) {
			log.warn("[海外仓退货入库] 参考单号匹配到销售订单{}但对应店铺不存在，生成无关联预入库单：shopId={}", soB2cEntity.getCode(), soB2cEntity.getShopId());
			this.createSoReturnPrestockHeadless(dto, warehouseEntity);
			return;
		}
		List<SoReturnInstockDetailEntity> detailEntityList = this.buildPlatformSoReturnInstockDetail(dto, warehouseEntity, soB2cEntity);
		if (CollectionUtils.isEmpty(detailEntityList)) {
			ServiceException.runError("【海外仓退货入库】来源明细未匹配到映射");
		}
		SoReturnInstockEntity soReturnInstockEntity = this.buildPlatformSoReturnInstockEntity(dto, warehouseEntity, soB2cEntity, shopInfoEntity);

		// 顺带尝试关联退货单（与既有销售订单流程一致）
		this.matchSoReturn(soReturnInstockEntity, detailEntityList, dto, soB2cEntity);
		soReturnInstockService.addByThirdWarehouse(soReturnInstockEntity, detailEntityList);
	}

	/**
	 * 命中B2B销售订单后生成《已审核-退货入库单》：B2B无"店铺"概念，客户直接取销售订单上的客户
	 */
	private void generateInstockBySoInfo(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoInfoEntity soInfoEntity) {
		List<SoReturnInstockDetailEntity> detailEntityList = this.buildPlatformSoReturnInstockDetailForSoInfo(dto, warehouseEntity, soInfoEntity);
		if (CollectionUtils.isEmpty(detailEntityList)) {
			ServiceException.runError("【海外仓退货入库】来源明细未匹配到映射");
		}
		SoReturnInstockEntity soReturnInstockEntity = this.buildPlatformSoReturnInstockEntityForSoInfo(dto, warehouseEntity, soInfoEntity);

		soReturnInstockService.addByThirdWarehouse(soReturnInstockEntity, detailEntityList);
	}

	/**
	 * B2B销售订单明细按平台SKU直接映射入库SKU（B2B无出库单就近匹配逻辑，字段映射与B2C保持一致）
	 */
	private List<SoReturnInstockDetailEntity> buildPlatformSoReturnInstockDetailForSoInfo(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoInfoEntity soInfoEntity) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		List<SoDetailEntity> soDetailEntityList = soInfoFeign.listSoDetailByMainIds(Collections.singletonList(soInfoEntity.getId()));
		if (CollectionUtils.isEmpty(soDetailEntityList)) {
			ServiceException.runError("【平台退货入库】对应B2B订单明细为空：{}", dto.getPlatformOrderNo());
		}
		List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();
		for (PlatformReturnInstockDTO.Detail detail : details) {
			SoDetailEntity detailEntity = soDetailEntityList.stream()
					.filter(e -> StringUtils.isNotBlank(e.getPlatformSkuNo()) && e.getPlatformSkuNo().equalsIgnoreCase(detail.getProductSku()))
					.findFirst().orElse(null);
			if (Objects.isNull(detailEntity)) {
				ServiceException.runError("找不到B2B销售订单明细:订单={}, 平台SKU={}", dto.getPlatformOrderNo(), detail.getProductSku());
			}
			SoReturnInstockDetailEntity soReturnInstockDetailEntity = new SoReturnInstockDetailEntity();
			soReturnInstockDetailEntity.setSkuId(detailEntity.getSkuId());
			soReturnInstockDetailEntity.setSkuNo(detailEntity.getSkuNo());
			soReturnInstockDetailEntity.setMustQty(detail.getMustQty());
			soReturnInstockDetailEntity.setReceiveQty(detail.getReceiveQty());
			soReturnInstockDetailEntity.setRealQty(detail.getRealQty());
			soReturnInstockDetailEntity.setWarehouseId(warehouseEntity.getId());
			soReturnInstockDetailEntity.setWarehouseName(warehouseEntity.getName());
			soReturnInstockDetailEntity.setRemark(dto.getReason());
			soReturnInstockDetailEntity.setReturnTypeDict(dto.getReturnType());
			soReturnInstockDetailEntity.setDefectiveProductFlag(detail.getDefectiveProductFlag());
			detailEntityList.add(soReturnInstockDetailEntity);
		}
		return detailEntityList;
	}

	/**
	 * B2B销售订单场景字段映射：客户直接取销售订单上的 customerId（B2B无"店铺-客户关联"环节）
	 */
	private SoReturnInstockEntity buildPlatformSoReturnInstockEntityForSoInfo(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoInfoEntity soInfoEntity) {
		SoReturnInstockEntity soReturnInstockEntity = new SoReturnInstockEntity();
		soReturnInstockEntity.setApproveTime(LocalDateTime.now());
		soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
		soReturnInstockEntity.setBillDate(dto.getPutawayLocalDate());
		soReturnInstockEntity.setInventoryOrgId(warehouseEntity.getOrgId());
		SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
		soReturnInstockEntity.setInventoryOrgName(company.getCompanyName());
		soReturnInstockEntity.setWarehouseKeeperId(warehouseEntity.getChargeId());
		soReturnInstockEntity.setApproveUserName("system");
		soReturnInstockEntity.setSourceCode(dto.getUniqueId());
		soReturnInstockEntity.setSourceType(SourceTypeEnum.PLATFORM_RETURN_INSTOCK.getCode());
		soReturnInstockEntity.setThirdCode(dto.getPlatformReturnOrderNo());
		soReturnInstockEntity.setPlatformOrderCode(dto.getPlatformOrderNo());
		soReturnInstockEntity.setCreated(dto.getCreateTime());
		soReturnInstockEntity.setType(BillTypeEnum.B2B.getCode());
		soReturnInstockEntity.setReturnLogisticCode(dto.getReturnLogisticCode());
		soReturnInstockEntity.setSoId(soInfoEntity.getId());
		soReturnInstockEntity.setSoCode(soInfoEntity.getCode());
		soReturnInstockEntity.setCurrency(soInfoEntity.getCurrency());
		if (StringUtils.isBlank(soInfoEntity.getCustomerId())) {
			ServiceException.runError("B2B销售订单对应客户信息为空:{}", soInfoEntity.getCode());
		}
		CustomerInfoEntity customerInfo = customerFeign.getCustomerById(soInfoEntity.getCustomerId());
		if (null == customerInfo) {
			ServiceException.runError("B2B销售订单对应客户信息不存在:客户ID={}", soInfoEntity.getCustomerId());
		}
		soReturnInstockEntity.setSalesOrgId(customerInfo.getUseOrgId());
		soReturnInstockEntity.setSalesOrgName(customerInfo.getUseOrgName());
		soReturnInstockEntity.setCustomerId(soInfoEntity.getCustomerId());
		soReturnInstockEntity.setCustomerName(customerInfo.getName());
		if (StringUtils.isNotBlank(customerInfo.getSalesDeptId())) {
			SysDepartmentDTO department = sysUserFeign.getUserDeptById(customerInfo.getSalesDeptId());
			if (null != department) {
				soReturnInstockEntity.setSalesDeptId(customerInfo.getSalesDeptId());
				soReturnInstockEntity.setSalesDeptName(department.getName());
			}
		}
		soReturnInstockEntity.setSellerId(customerInfo.getSellerId());
		soReturnInstockEntity.setSellerName(customerInfo.getSellerName());
		return soReturnInstockEntity;
	}

	/**
	 * 既无退货物流单号又无参考单号：无法关联到任何单据，生成预入库单交由运营人工关联
	 */
	private void createSoReturnPrestockHeadless(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity) {
		List<SoReturnPrestockDetailDTO.Add> detailList = buildPrestockDetailList(dto);
		if (CollectionUtils.isEmpty(detailList)) {
			log.warn("[海外仓退货入库-无头件] 明细为空，跳过：thirdCode={}", dto.getPlatformReturnOrderNo());
			return;
		}
		SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
		SoReturnPrestockDTO.Add addDTO = new SoReturnPrestockDTO.Add();
		addDTO.setType(BillTypeEnum.B2C.getCode());
		addDTO.setReturnLogisticCode("");
		addDTO.setReturnTypeDict(dto.getReturnType());
		addDTO.setInventoryOrgId(warehouseEntity.getOrgId());
		addDTO.setInventoryOrgName(Objects.nonNull(company) ? company.getCompanyName() : "");
		addDTO.setWarehouseId(warehouseEntity.getId());
		addDTO.setWarehouseName(warehouseEntity.getName());
		addDTO.setThirdCode(dto.getPlatformReturnOrderNo());
		addDTO.setRemark(dto.getReason());
		addDTO.setDetailList(detailList);
		soReturnPrestockService.createFromOverseasWhHeadless(addDTO);
	}

	/**
	 * 由平台退货入库明细构建预入库单明细行；未匹配到 SKU 映射时保留原始平台 SKU，不丢弃、不中断，交给运营人工核对
	 */
	private List<SoReturnPrestockDetailDTO.Add> buildPrestockDetailList(PlatformReturnInstockDTO dto) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		if (CollectionUtils.isEmpty(details)) {
			return Collections.emptyList();
		}
		List<String> platformSkuNoList = details.stream().map(PlatformReturnInstockDTO.Detail::getProductSku).collect(Collectors.toList());
		ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
		listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
		listingInfoParamDTO.setAuthId(dto.getAuthId());
		listingInfoParamDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
		List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);

		return details.stream().map(detail -> {
			SkuMappingDTO.MappingSkuViewDTO skuViewDTO = mappingSkuViewDTOList.stream()
					.filter(v -> v.getPlatformSkuNo().equals(detail.getProductSku()))
					.findFirst().orElse(null);
			SoReturnPrestockDetailDTO.Add detailDTO = new SoReturnPrestockDetailDTO.Add();
			detailDTO.setSkuId(Objects.nonNull(skuViewDTO) ? skuViewDTO.getProductSkuId() : "");
			// 未匹配到映射时，仍用平台SKU占位落库，不丢消息，等运营人工在预入库单里核对
			detailDTO.setSkuNo(Objects.nonNull(skuViewDTO) ? skuViewDTO.getProductSkuNo() : detail.getProductSku());
			detailDTO.setProductName(Objects.nonNull(skuViewDTO) ? skuViewDTO.getProductName() : "");
			detailDTO.setReturnQty(detail.getMustQty());
			detailDTO.setReceiveQty(detail.getReceiveQty());
			// 平台订单号、平台字典值均为【关联】相关字段，不代表本行数据来源渠道，此处无头件尚未关联，不写入
			detailDTO.setRemark(dto.getReason());
			return detailDTO;
		}).collect(Collectors.toList());
	}

	/**
	 * 匹配退货订单
	 * @param soReturnInstockEntity
	 * @param detailEntityList
	 * @param dto
	 * @param soB2cEntity
	 */
	private void matchSoReturn(SoReturnInstockEntity soReturnInstockEntity, List<SoReturnInstockDetailEntity> detailEntityList, PlatformReturnInstockDTO dto,SoB2cEntity soB2cEntity) {
		if(Objects.isNull(soB2cEntity) || CharSequenceUtil.isBlank(soB2cEntity.getPlatformCode())){
			return;
		}
		List<SoB2cReturnEntity> soB2cReturnEntityList = FeignQuery.create(SoB2cReturnEntity.class).eq(SoB2cReturnEntity::getPlatformOrderNo,soB2cEntity.getPlatformCode()).list();
		if(CollectionUtils.isEmpty(soB2cReturnEntityList)){
			return;
		}
		List<String> soB2cReturnIds = soB2cReturnEntityList.stream().map(v->v.getId()).collect(Collectors.toList());
		List<SoB2cReturnDetailEntity> soB2cReturnDetailEntityList = FeignQuery.create(SoB2cReturnDetailEntity.class).in(SoB2cReturnDetailEntity::getMainId,soB2cReturnIds).list();
		if(CollectionUtils.isEmpty(soB2cReturnDetailEntityList)){
			return;
		}
		for (SoB2cReturnEntity soB2cReturnEntity : soB2cReturnEntityList) {
			List<SoB2cReturnDetailEntity> currentDetailList = soB2cReturnDetailEntityList.stream().filter(v->v.getMainId().equals(soB2cReturnEntity.getId())).collect(Collectors.toList());
			if(CollectionUtils.isEmpty(currentDetailList)){
				continue;
			}
			boolean isMatch = false;
			for (SoReturnInstockDetailEntity soReturnInstockDetailEntity : detailEntityList) {
				SoB2cReturnDetailEntity soB2cReturnDetailEntity = currentDetailList.stream().filter(v->v.getSkuId().equals(soReturnInstockDetailEntity.getSkuId())).findFirst().orElse(null);
				if(Objects.isNull(soB2cReturnDetailEntity)){
					continue;
				}
				soReturnInstockDetailEntity.setSoReturnDetailId(soB2cReturnDetailEntity.getId());
				isMatch = true;
			}
			if(isMatch){
				soReturnInstockEntity.setSoReturnId(soB2cReturnEntity.getId());
				soReturnInstockEntity.setSoReturnCode(soB2cReturnEntity.getCode());
				soReturnInstockEntity.setPlatformOrderCode(Objects.nonNull(soB2cEntity) ? soB2cEntity.getPlatformCode() : "");
				return;
			}
		}
	}

	/**
	 * 众包(ZHONG_BAO_WAREHOUSE)/极兔(JI_TU_WAREHOUSE)/艾姆勒(IML)/通邮(TONG_YOU_WAREHOUSE)：
	 * 历史遗留专属仓库解析、来源匹配（B2B三方发货单/B2C发货单号）与金额、含税金额、汇率计算逻辑，
	 * 与下方统一匹配流程（{@link #handleReferenceNoAfterSaleMatch}）相互独立，不进入统一匹配
	 */
	private boolean isLegacySpecialWarehousePlatform(String platform) {
		return PlatformDictEnum.ZHONG_BAO_WAREHOUSE.getCode().equalsIgnoreCase(platform)
				|| PlatformDictEnum.JI_TU_WAREHOUSE.getCode().equalsIgnoreCase(platform)
				|| PlatformDictEnum.IML.getCode().equalsIgnoreCase(platform)
				|| PlatformDictEnum.TONG_YOU_WAREHOUSE.getCode().equalsIgnoreCase(platform);
	}

	/**
	 * 众包/极兔/艾姆勒/通邮专属处理：来源单号解析（含WFHD三方仓发货单）、众包极兔的B2B三方发货单/B2C发货单号匹配、
	 * 仓库解析（艾姆勒/通邮取订单仓库，众包/极兔走海外仓服务商映射）、按平台构建对应实体明细并落库
	 */
	private void handleLegacySpecialWarehouse(PlatformReturnInstockDTO dto) {
	/**
	 * 按参考单号匹配《B2B销售订单》：平台订单号或销售单号=参考单号，且需未作废、SKU 命中其一。
	 * 可匹配数据范围：B2B销售订单-作废状态：未作废
	 */
	private SoInfoEntity matchB2bSoByReferenceNo(String orderReferenceNo, Set<String> incomingSkuIds) {
		List<SoInfoEntity> candidates = new ArrayList<>();
		List<SoInfoEntity> byPlatformOrderCode = soInfoFeign.getByPlatformOrderCode(orderReferenceNo);
		if (CollectionUtils.isNotEmpty(byPlatformOrderCode)) {
			candidates.addAll(byPlatformOrderCode);
		}
		SoInfoEntity byCode = soInfoFeign.getByCode(orderReferenceNo);
		if (Objects.nonNull(byCode)) {
			candidates.add(byCode);
		}
		return candidates.stream()
				.filter(v -> !Boolean.TRUE.equals(v.getInvalidStatus()))
				.filter(v -> soInfoHasMatchingSku(v.getId(), incomingSkuIds))
				.findFirst()
				.orElse(null);
	}

	/**
	 * 校验B2B销售订单明细是否存在命中的ERP SKU
	 */
	private boolean soInfoHasMatchingSku(String soInfoId, Set<String> incomingSkuIds) {
		if (CollectionUtils.isEmpty(incomingSkuIds)) {
			return false;
		}
		List<SoDetailEntity> detailList = soInfoFeign.listSoDetailByMainIds(Collections.singletonList(soInfoId));
		return CollectionUtils.isNotEmpty(detailList) && detailList.stream()
				.anyMatch(d -> StringUtils.isNotBlank(d.getSkuId()) && incomingSkuIds.contains(d.getSkuId()));
	}

	/**
	 * 命中销售订单后生成《已审核-退货入库单》，字段映射与已存在销售订单场景（{@link #createByExistSoB2c}）保持一致，
	 * 仅仓库改用海外仓映射的 warehouseEntity
	 */
	private void generateInstockBySo(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoB2cEntity soB2cEntity) {
		ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(soB2cEntity.getShopId());
		if (Objects.isNull(shopInfoEntity)) {
			log.warn("[海外仓退货入库] 参考单号匹配到销售订单{}但对应店铺不存在，生成无关联预入库单：shopId={}", soB2cEntity.getCode(), soB2cEntity.getShopId());
			this.createSoReturnPrestockHeadless(dto, warehouseEntity);
			return;
		}
		List<SoReturnInstockDetailEntity> detailEntityList = this.buildPlatformSoReturnInstockDetail(dto, warehouseEntity, soB2cEntity);
		if (CollectionUtils.isEmpty(detailEntityList)) {
			ServiceException.runError("【海外仓退货入库】来源明细未匹配到映射");
		}
		SoReturnInstockEntity soReturnInstockEntity = this.buildPlatformSoReturnInstockEntity(dto, warehouseEntity, soB2cEntity, shopInfoEntity);

		// 顺带尝试关联退货单（与既有销售订单流程一致）
		this.matchSoReturn(soReturnInstockEntity, detailEntityList, dto, soB2cEntity);
		soReturnInstockService.addByThirdWarehouse(soReturnInstockEntity, detailEntityList);
	}

	/**
	 * 命中B2B销售订单后生成《已审核-退货入库单》：B2B无"店铺"概念，客户直接取销售订单上的客户
	 */
	private void generateInstockBySoInfo(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoInfoEntity soInfoEntity) {
		List<SoReturnInstockDetailEntity> detailEntityList = this.buildPlatformSoReturnInstockDetailForSoInfo(dto, warehouseEntity, soInfoEntity);
		if (CollectionUtils.isEmpty(detailEntityList)) {
			ServiceException.runError("【海外仓退货入库】来源明细未匹配到映射");
		}
		SoReturnInstockEntity soReturnInstockEntity = this.buildPlatformSoReturnInstockEntityForSoInfo(dto, warehouseEntity, soInfoEntity);

		soReturnInstockService.addByThirdWarehouse(soReturnInstockEntity, detailEntityList);
	}

	/**
	 * B2B销售订单明细按平台SKU直接映射入库SKU（B2B无出库单就近匹配逻辑，字段映射与B2C保持一致）
	 */
	private List<SoReturnInstockDetailEntity> buildPlatformSoReturnInstockDetailForSoInfo(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoInfoEntity soInfoEntity) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		List<SoDetailEntity> soDetailEntityList = soInfoFeign.listSoDetailByMainIds(Collections.singletonList(soInfoEntity.getId()));
		if (CollectionUtils.isEmpty(soDetailEntityList)) {
			ServiceException.runError("【平台退货入库】对应B2B订单明细为空：{}", dto.getPlatformOrderNo());
		}
		List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();
		for (PlatformReturnInstockDTO.Detail detail : details) {
			SoDetailEntity detailEntity = soDetailEntityList.stream()
					.filter(e -> StringUtils.isNotBlank(e.getPlatformSkuNo()) && e.getPlatformSkuNo().equalsIgnoreCase(detail.getProductSku()))
					.findFirst().orElse(null);
			if (Objects.isNull(detailEntity)) {
				ServiceException.runError("找不到B2B销售订单明细:订单={}, 平台SKU={}", dto.getPlatformOrderNo(), detail.getProductSku());
			}
			SoReturnInstockDetailEntity soReturnInstockDetailEntity = new SoReturnInstockDetailEntity();
			soReturnInstockDetailEntity.setSkuId(detailEntity.getSkuId());
			soReturnInstockDetailEntity.setSkuNo(detailEntity.getSkuNo());
			soReturnInstockDetailEntity.setMustQty(detail.getMustQty());
			soReturnInstockDetailEntity.setReceiveQty(detail.getReceiveQty());
			soReturnInstockDetailEntity.setRealQty(detail.getRealQty());
			soReturnInstockDetailEntity.setWarehouseId(warehouseEntity.getId());
			soReturnInstockDetailEntity.setWarehouseName(warehouseEntity.getName());
			soReturnInstockDetailEntity.setRemark(dto.getReason());
			soReturnInstockDetailEntity.setReturnTypeDict(dto.getReturnType());
			soReturnInstockDetailEntity.setDefectiveProductFlag(detail.getDefectiveProductFlag());
			detailEntityList.add(soReturnInstockDetailEntity);
		}
		return detailEntityList;
	}

	/**
	 * B2B销售订单场景字段映射：客户直接取销售订单上的 customerId（B2B无"店铺-客户关联"环节）
	 */
	private SoReturnInstockEntity buildPlatformSoReturnInstockEntityForSoInfo(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoInfoEntity soInfoEntity) {
		SoReturnInstockEntity soReturnInstockEntity = new SoReturnInstockEntity();
		soReturnInstockEntity.setApproveTime(LocalDateTime.now());
		soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
		soReturnInstockEntity.setBillDate(dto.getPutawayLocalDate());
		soReturnInstockEntity.setInventoryOrgId(warehouseEntity.getOrgId());
		SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
		soReturnInstockEntity.setInventoryOrgName(company.getCompanyName());
		soReturnInstockEntity.setWarehouseKeeperId(warehouseEntity.getChargeId());
		soReturnInstockEntity.setApproveUserName("system");
		soReturnInstockEntity.setSourceCode(dto.getUniqueId());
		soReturnInstockEntity.setSourceType(SourceTypeEnum.PLATFORM_RETURN_INSTOCK.getCode());
		soReturnInstockEntity.setThirdCode(dto.getPlatformReturnOrderNo());
		soReturnInstockEntity.setPlatformOrderCode(dto.getPlatformOrderNo());
		soReturnInstockEntity.setCreated(dto.getCreateTime());
		soReturnInstockEntity.setType(BillTypeEnum.B2B.getCode());
		soReturnInstockEntity.setReturnLogisticCode(dto.getReturnLogisticCode());
		soReturnInstockEntity.setSoId(soInfoEntity.getId());
		soReturnInstockEntity.setSoCode(soInfoEntity.getCode());
		soReturnInstockEntity.setCurrency(soInfoEntity.getCurrency());
		if (StringUtils.isBlank(soInfoEntity.getCustomerId())) {
			ServiceException.runError("B2B销售订单对应客户信息为空:{}", soInfoEntity.getCode());
		}
		CustomerInfoEntity customerInfo = customerFeign.getCustomerById(soInfoEntity.getCustomerId());
		if (null == customerInfo) {
			ServiceException.runError("B2B销售订单对应客户信息不存在:客户ID={}", soInfoEntity.getCustomerId());
		}
		soReturnInstockEntity.setSalesOrgId(customerInfo.getUseOrgId());
		soReturnInstockEntity.setSalesOrgName(customerInfo.getUseOrgName());
		soReturnInstockEntity.setCustomerId(soInfoEntity.getCustomerId());
		soReturnInstockEntity.setCustomerName(customerInfo.getName());
		if (StringUtils.isNotBlank(customerInfo.getSalesDeptId())) {
			SysDepartmentDTO department = sysUserFeign.getUserDeptById(customerInfo.getSalesDeptId());
			if (null != department) {
				soReturnInstockEntity.setSalesDeptId(customerInfo.getSalesDeptId());
				soReturnInstockEntity.setSalesDeptName(department.getName());
			}
		}
		soReturnInstockEntity.setSellerId(customerInfo.getSellerId());
		soReturnInstockEntity.setSellerName(customerInfo.getSellerName());
		return soReturnInstockEntity;
	}

	/**
	 * 既无退货物流单号又无参考单号：无法关联到任何单据，生成预入库单交由运营人工关联
	 */
	private void createSoReturnPrestockHeadless(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity) {
		List<SoReturnPrestockDetailDTO.Add> detailList = buildPrestockDetailList(dto);
		if (CollectionUtils.isEmpty(detailList)) {
			log.warn("[海外仓退货入库-无头件] 明细为空，跳过：thirdCode={}", dto.getPlatformReturnOrderNo());
			return;
		}
		SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
		SoReturnPrestockDTO.Add addDTO = new SoReturnPrestockDTO.Add();
		addDTO.setType(BillTypeEnum.B2C.getCode());
		addDTO.setReturnLogisticCode("");
		addDTO.setReturnTypeDict(dto.getReturnType());
		addDTO.setInventoryOrgId(warehouseEntity.getOrgId());
		addDTO.setInventoryOrgName(Objects.nonNull(company) ? company.getCompanyName() : "");
		addDTO.setWarehouseId(warehouseEntity.getId());
		addDTO.setWarehouseName(warehouseEntity.getName());
		addDTO.setThirdCode(dto.getPlatformReturnOrderNo());
		addDTO.setRemark(dto.getReason());
		addDTO.setDetailList(detailList);
		soReturnPrestockService.createFromOverseasWhHeadless(addDTO);
	}

	/**
	 * 由平台退货入库明细构建预入库单明细行；未匹配到 SKU 映射时保留原始平台 SKU，不丢弃、不中断，交给运营人工核对
	 */
	private List<SoReturnPrestockDetailDTO.Add> buildPrestockDetailList(PlatformReturnInstockDTO dto) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		if (CollectionUtils.isEmpty(details)) {
			return Collections.emptyList();
		}
		List<String> platformSkuNoList = details.stream().map(PlatformReturnInstockDTO.Detail::getProductSku).collect(Collectors.toList());
		ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
		listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
		listingInfoParamDTO.setAuthId(dto.getAuthId());
		listingInfoParamDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
		List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);

		return details.stream().map(detail -> {
			SkuMappingDTO.MappingSkuViewDTO skuViewDTO = mappingSkuViewDTOList.stream()
					.filter(v -> v.getPlatformSkuNo().equals(detail.getProductSku()))
					.findFirst().orElse(null);
			SoReturnPrestockDetailDTO.Add detailDTO = new SoReturnPrestockDetailDTO.Add();
			detailDTO.setSkuId(Objects.nonNull(skuViewDTO) ? skuViewDTO.getProductSkuId() : "");
			// 未匹配到映射时，仍用平台SKU占位落库，不丢消息，等运营人工在预入库单里核对
			detailDTO.setSkuNo(Objects.nonNull(skuViewDTO) ? skuViewDTO.getProductSkuNo() : detail.getProductSku());
			detailDTO.setProductName(Objects.nonNull(skuViewDTO) ? skuViewDTO.getProductName() : "");
			detailDTO.setReturnQty(detail.getMustQty());
			detailDTO.setReceiveQty(detail.getReceiveQty());
			// 平台订单号、平台字典值均为【关联】相关字段，不代表本行数据来源渠道，此处无头件尚未关联，不写入
			detailDTO.setRemark(dto.getReason());
			return detailDTO;
		}).collect(Collectors.toList());
	}


	/**
	 * 众包(ZHONG_BAO_WAREHOUSE)/极兔(JI_TU_WAREHOUSE)/艾姆勒(IML)/通邮(TONG_YOU_WAREHOUSE)：
	 * 历史遗留专属仓库解析、来源匹配（B2B三方发货单/B2C发货单号）与金额、含税金额、汇率计算逻辑，
	 * 与下方统一匹配流程（{@link #handleReferenceNoAfterSaleMatch}）相互独立，不进入统一匹配
	 */
	private boolean isLegacySpecialWarehousePlatform(String platform) {
		return PlatformDictEnum.ZHONG_BAO_WAREHOUSE.getCode().equalsIgnoreCase(platform)
				|| PlatformDictEnum.JI_TU_WAREHOUSE.getCode().equalsIgnoreCase(platform)
				|| PlatformDictEnum.IML.getCode().equalsIgnoreCase(platform)
				|| PlatformDictEnum.TONG_YOU_WAREHOUSE.getCode().equalsIgnoreCase(platform);
	}

	/**
	 * 众包/极兔/艾姆勒/通邮专属处理：来源单号解析（含WFHD三方仓发货单）、众包极兔的B2B三方发货单/B2C发货单号匹配、
	 * 仓库解析（艾姆勒/通邮取订单仓库，众包/极兔走海外仓服务商映射）、按平台构建对应实体明细并落库
	 */
	private void handleLegacySpecialWarehouse(PlatformReturnInstockDTO dto) {
		SoB2cEntity soB2cEntity = null;
		SoInfoEntity soInfoEntity = null;
		SoOutstockEntity soOutstock = null;
		SoReturnInstockEntity soReturnInstockEntity;
		List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();

		if (CharSequenceUtil.isNotBlank(dto.getOrderReferenceNo()) && dto.getOrderReferenceNo().contains(BusinessNoConstant.WFHD)) {
			// 参考单号为三方仓发货单号：查三方仓发货单取对应销售订单
			ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = thirdWarehouseDeliveryService.getLatestByCode(dto.getOrderReferenceNo());
			if (Objects.nonNull(thirdWarehouseDeliveryEntity)) {
				soB2cEntity = soB2cFeign.getSoCode(thirdWarehouseDeliveryEntity.getSoCode());
			}
			if (Objects.nonNull(soB2cEntity)) {
				soOutstock = soOutstockService.getBySoId(soB2cEntity.getId());
			}
		}

		if ((Objects.equals(dto.getPlatform(), PlatformDictEnum.ZHONG_BAO_WAREHOUSE.getCode())
				|| Objects.equals(dto.getPlatform(), PlatformDictEnum.JI_TU_WAREHOUSE.getCode())
				&& CharSequenceUtil.isNotBlank(dto.getPlatformOrderNo()))) {
			List<B2bThirdDeliveryEntity> list = b2bThirdDeliveryService.lambdaQuery()
					.eq(B2bThirdDeliveryEntity::getPlatformOrderCode, dto.getPlatformOrderNo())
					.list();
			if (!list.isEmpty()) {
				SoInfoEntity soInfo = soInfoFeign.getSoInfoById(list.get(0).getSoId());
				if (Objects.nonNull(soInfo)) {
					soInfoEntity = soInfo;
				}
			}

			List<SoB2cEntity> soB2CList = soB2cFeign.getByShippingOrderNo(dto.getPlatformOrderNo());
			if (!soB2CList.isEmpty()) {
				soB2cEntity = soB2CList.get(0);
			}
		}

		WarehouseEntity warehouseEntity = new WarehouseEntity();
		// 艾姆勒/通邮没有仓库映射，拿订单的仓库
		if (PlatformDictEnum.IML.getCode().equalsIgnoreCase(dto.getPlatform()) || PlatformDictEnum.TONG_YOU_WAREHOUSE.getCode().equalsIgnoreCase(dto.getPlatform())) {
			if (Objects.nonNull(soB2cEntity)) {
				soOutstock = soOutstockService.getBySoId(soB2cEntity.getId());
				List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cFeign.listDetailByMainIds(Collections.singletonList(soB2cEntity.getId()));
				if (CollectionUtils.isNotEmpty(soB2cDetailEntityList)) {
					String warehouseId = soB2cDetailEntityList.get(0).getWarehouseId();
					if (StringUtils.isNotBlank(warehouseId)) {
						warehouseEntity = warehouseService.getById(warehouseId);
					}
				}
			}
		} else {
			OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = overseasProviderWarehouseService.getByPlatform(dto.getAuthId(), dto.getWarehouseCode());
			if (Objects.isNull(overseasProviderWarehouseEntity) || CharSequenceUtil.isBlank(overseasProviderWarehouseEntity.getWarehouseId())) {
				throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "仓库信息");
			}
			warehouseEntity = warehouseService.getById(overseasProviderWarehouseEntity.getWarehouseId());
		}

		if (Objects.equals(dto.getPlatform(), PlatformDictEnum.ZHONG_BAO_WAREHOUSE.getCode())) {
			soReturnInstockEntity = this.buildZhongBaoSoReturnInstockEntity(dto, warehouseEntity, soB2cEntity, soInfoEntity, soOutstock);
			detailEntityList.addAll(this.buildZhongBaoSoReturnInstockDetail(dto, soB2cEntity, soInfoEntity, warehouseEntity));
		} else if (Objects.equals(dto.getPlatform(), PlatformDictEnum.JI_TU_WAREHOUSE.getCode())) {
			soReturnInstockEntity = this.buildJiTuSoReturnInstockEntity(dto, warehouseEntity, soB2cEntity, soInfoEntity, soOutstock);
			detailEntityList.addAll(this.buildJiTuSoReturnInstockDetail(dto, soB2cEntity, soInfoEntity, warehouseEntity));
		} else {
			soReturnInstockEntity = this.buildSoReturnInstockEntity(dto, warehouseEntity, soB2cEntity, soOutstock);
			detailEntityList.addAll(this.buildSoReturnInstockDetail(dto, soReturnInstockEntity, warehouseEntity));
		}

		if (CollectionUtils.isEmpty(detailEntityList)) {
			throw new ServiceException("没有映射");
		}

		this.matchSoReturn(soReturnInstockEntity, detailEntityList, dto, soB2cEntity);

		soReturnInstockService.addByThirdWarehouse(soReturnInstockEntity, detailEntityList);
	}

	/**
	 * 众包/极兔/艾姆勒/通邮 旧版默认明细构建（艾姆勒/通邮走此分支）：按平台SKU全局映射（不区分订单来源），
	 * 命中来源单据时补充 sourceDetailId/createUserId/platformSkuNo/defectiveProductFlag
	 */
	private void matchSoReturn(SoReturnInstockEntity soReturnInstockEntity, List<SoReturnInstockDetailEntity> detailEntityList, PlatformReturnInstockDTO dto,SoB2cEntity soB2cEntity) {
		if(Objects.isNull(soB2cEntity) || CharSequenceUtil.isBlank(soB2cEntity.getPlatformCode())){
			return;
		}
		List<SoB2cReturnEntity> soB2cReturnEntityList = FeignQuery.create(SoB2cReturnEntity.class).eq(SoB2cReturnEntity::getPlatformOrderNo,soB2cEntity.getPlatformCode()).list();
		if(CollectionUtils.isEmpty(soB2cReturnEntityList)){
			return;
		}
		List<String> soB2cReturnIds = soB2cReturnEntityList.stream().map(v->v.getId()).collect(Collectors.toList());
		List<SoB2cReturnDetailEntity> soB2cReturnDetailEntityList = FeignQuery.create(SoB2cReturnDetailEntity.class).in(SoB2cReturnDetailEntity::getMainId,soB2cReturnIds).list();
		if(CollectionUtils.isEmpty(soB2cReturnDetailEntityList)){
			return;
		}
		for (SoB2cReturnEntity soB2cReturnEntity : soB2cReturnEntityList) {
			List<SoB2cReturnDetailEntity> currentDetailList = soB2cReturnDetailEntityList.stream().filter(v->v.getMainId().equals(soB2cReturnEntity.getId())).collect(Collectors.toList());
			if(CollectionUtils.isEmpty(currentDetailList)){
				continue;
			}
			boolean isMatch = false;
			for (SoReturnInstockDetailEntity soReturnInstockDetailEntity : detailEntityList) {
				SoB2cReturnDetailEntity soB2cReturnDetailEntity = currentDetailList.stream().filter(v->v.getSkuId().equals(soReturnInstockDetailEntity.getSkuId())).findFirst().orElse(null);
				if(Objects.isNull(soB2cReturnDetailEntity)){
					continue;
				}
				soReturnInstockDetailEntity.setSoReturnDetailId(soB2cReturnDetailEntity.getId());
				isMatch = true;
			}
			if(isMatch){
				soReturnInstockEntity.setSoReturnId(soB2cReturnEntity.getId());
				soReturnInstockEntity.setSoReturnCode(soB2cReturnEntity.getCode());
				soReturnInstockEntity.setPlatformOrderCode(Objects.nonNull(soB2cEntity) ? soB2cEntity.getPlatformCode() : "");
				return;
			}
		}
	}

    /**
     * 众包/极兔/艾姆勒/通邮 旧版默认明细构建（艾姆勒/通邮走此分支）：按平台SKU全局映射（不区分订单来源），
     * 命中来源单据时补充 sourceDetailId/createUserId/platformSkuNo/defectiveProductFlag
     */
	private List<SoReturnInstockDetailEntity> buildSoReturnInstockDetail(PlatformReturnInstockDTO dto, SoReturnInstockEntity soReturnInstockEntity, WarehouseEntity warehouseEntity) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		if (CollectionUtils.isEmpty(details)) {
			throw new ServiceException("明细为空");
		}
		List<String> platformSkuNoList = dto.getProductDetailList().stream().map(v -> v.getProductSku()).collect(Collectors.toList());
		ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
		listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
		listingInfoParamDTO.setAuthId(dto.getAuthId());
		listingInfoParamDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
		List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
		List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();
		for (PlatformReturnInstockDTO.Detail detail : details) {
			SkuMappingDTO.MappingSkuViewDTO skuViewDTO = mappingSkuViewDTOList.stream().filter(v -> v.getPlatformSkuNo().equals(detail.getProductSku())).findFirst().orElse(null);
			if (Objects.isNull(skuViewDTO)) {
				continue;
			}
			SoReturnInstockDetailEntity soReturnInstockDetailEntity = new SoReturnInstockDetailEntity();
			soReturnInstockDetailEntity.setSkuId(skuViewDTO.getProductSkuId());
			soReturnInstockDetailEntity.setSkuNo(skuViewDTO.getProductSkuNo());
			soReturnInstockDetailEntity.setMustQty(detail.getMustQty());
			soReturnInstockDetailEntity.setReceiveQty(detail.getReceiveQty());
			soReturnInstockDetailEntity.setRealQty(detail.getRealQty());
			soReturnInstockDetailEntity.setWarehouseId(warehouseEntity.getId());
			soReturnInstockDetailEntity.setWarehouseName(warehouseEntity.getName());
			soReturnInstockDetailEntity.setRemark(dto.getReason());
			soReturnInstockDetailEntity.setReturnTypeDict(dto.getReturnType());
			soReturnInstockDetailEntity.setSourceDetailId(detail.getThirdId());
			soReturnInstockDetailEntity.setCreateUserId(dto.getAuthId());
			soReturnInstockDetailEntity.setPlatformSkuNo(detail.getProductSku());
			soReturnInstockDetailEntity.setDefectiveProductFlag(detail.getDefectiveProductFlag());
			detailEntityList.add(soReturnInstockDetailEntity);
		}
		return detailEntityList;
	}

	/**
	 * 众包退货入库明细：按销售订单/退货订单明细价格计算退货金额与含税退货金额
	 */
	private List<SoReturnInstockDetailEntity> buildZhongBaoSoReturnInstockDetail(PlatformReturnInstockDTO dto, SoB2cEntity soB2cEntity, SoInfoEntity soInfoEntity, WarehouseEntity warehouseEntity) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		List<SoB2cDetailEntity> soB2cDetails = new ArrayList<>();
		List<SoDetailEntity> soDetails = new ArrayList<>();
		if (CollectionUtils.isEmpty(details)) {
			throw new ServiceException("明细为空");
		}
		List<String> platformSkuNoList = dto.getProductDetailList().stream().map(v -> v.getProductSku()).collect(Collectors.toList());
		ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
		listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
		listingInfoParamDTO.setAuthId(dto.getAuthId());
		listingInfoParamDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
		List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
		List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();
		if (Objects.nonNull(soB2cEntity)) {
			soB2cDetails.addAll(soB2cFeign.listDetailByMainIds(Collections.singletonList(soB2cEntity.getId())));
		} else if (Objects.nonNull(soInfoEntity)) {
			soDetails.addAll(soInfoFeign.listSoDetailByMainId(soInfoEntity.getId()));
		}
		for (PlatformReturnInstockDTO.Detail detail : details) {
			SkuMappingDTO.MappingSkuViewDTO skuViewDTO = mappingSkuViewDTOList.stream().filter(v -> v.getPlatformSkuNo().equals(detail.getProductSku())).findFirst().orElse(null);
			if (Objects.isNull(skuViewDTO)) {
				continue;
			}
			SoReturnInstockDetailEntity soReturnInstockDetailEntity = new SoReturnInstockDetailEntity();
			soReturnInstockDetailEntity.setSkuId(skuViewDTO.getProductSkuId());
			soReturnInstockDetailEntity.setSkuNo(skuViewDTO.getProductSkuNo());
			soReturnInstockDetailEntity.setMustQty(0);
			soReturnInstockDetailEntity.setReceiveQty(detail.getReceiveQty());
			soReturnInstockDetailEntity.setRealQty(detail.getRealQty());
			if (!soB2cDetails.isEmpty()) {
				SoB2cDetailEntity soB2cDetailEntity = soB2cDetails.stream()
						.filter(item -> Objects.equals(item.getSkuId(), skuViewDTO.getPlatformSkuNo()))
						.findFirst()
						.orElse(null);
				if (Objects.nonNull(soB2cDetailEntity)) {
					BigDecimal amount = MathUtil.multiplyWithTwo(soB2cDetailEntity.getPrice(), detail.getReceiveQty());
					soReturnInstockDetailEntity.setAmount(amount);
					soReturnInstockDetailEntity.setTaxReturnAmount(amount);
				} else {
					soReturnInstockDetailEntity.setAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setTaxReturnAmount(BigDecimal.ZERO);
				}
			} else if (!soDetails.isEmpty()) {
				SoDetailEntity soDetailEntity = soDetails.stream()
						.filter(item -> Objects.equals(item.getSkuId(), skuViewDTO.getPlatformSkuNo()))
						.findFirst()
						.orElse(null);
				if (Objects.nonNull(soDetailEntity)) {
					soReturnInstockDetailEntity.setReturnAmount(MathUtil.multiplyWithTwo(soDetailEntity.getPrice(), detail.getReceiveQty()));
					soReturnInstockDetailEntity.setTaxReturnAmount(MathUtil.multiplyWithTwo(soDetailEntity.getTaxPrice(), detail.getReceiveQty()));
				} else {
					soReturnInstockDetailEntity.setAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setTaxReturnAmount(BigDecimal.ZERO);
				}
			} else {
				soReturnInstockDetailEntity.setAmount(BigDecimal.ZERO);
				soReturnInstockDetailEntity.setTaxReturnAmount(BigDecimal.ZERO);
			}
			soReturnInstockDetailEntity.setWarehouseId(warehouseEntity.getId());
			soReturnInstockDetailEntity.setWarehouseName(warehouseEntity.getName());
			soReturnInstockDetailEntity.setRemark(dto.getReason());
			soReturnInstockDetailEntity.setReturnTypeDict(dto.getReturnType());
			soReturnInstockDetailEntity.setSourceDetailId(detail.getThirdId());
			soReturnInstockDetailEntity.setCreateUserId(dto.getAuthId());
			soReturnInstockDetailEntity.setPlatformSkuNo(detail.getProductSku());
			soReturnInstockDetailEntity.setDefectiveProductFlag(detail.getDefectiveProductFlag());
			detailEntityList.add(soReturnInstockDetailEntity);
		}
		return detailEntityList;
	}

	/**
	 * 旧版默认退货入库单实体构建（艾姆勒/通邮走此分支）
	 */
	private SoReturnInstockEntity buildSoReturnInstockEntity(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoB2cEntity soB2cEntity, SoOutstockEntity soOutstock) {
		SoReturnInstockEntity soReturnInstockEntity = new SoReturnInstockEntity();
		if (StringUtils.isNotBlank(warehouseEntity.getId())) {
			soReturnInstockEntity.setApproveTime(LocalDateTime.now());
			soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
		}
		soReturnInstockEntity.setBillDate(dto.getPutawayLocalDate());
		soReturnInstockEntity.setInventoryOrgId(warehouseEntity.getOrgId());
		soReturnInstockEntity.setReturnLogisticCode(dto.getReturnLogisticCode());
		soReturnInstockEntity.setSourceId(dto.getSourceId());
		if (StringUtils.isNotBlank(warehouseEntity.getId())) {
			SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
			soReturnInstockEntity.setInventoryOrgName(company.getCompanyName());
		}
		soReturnInstockEntity.setWarehouseKeeperId(warehouseEntity.getChargeId());
		soReturnInstockEntity.setApproveUserName("system");
		soReturnInstockEntity.setSourceCode(Objects.nonNull(soB2cEntity) ? soB2cEntity.getCode() : dto.getOrderReferenceNo());
		soReturnInstockEntity.setSourceType(SourceTypeEnum.THIRD_WAREHOUSE_RETURN_INSTOCK.getCode());
		soReturnInstockEntity.setThirdCode(dto.getPlatformReturnOrderNo());
		soReturnInstockEntity.setCreated(dto.getCreateTime());
		soReturnInstockEntity.setType("B2C");
		if (Objects.nonNull(soB2cEntity)) {
			soReturnInstockEntity.setSalesOrgId(soB2cEntity.getOrgId());
			soReturnInstockEntity.setSalesOrgName(soB2cEntity.getOrgName());
			ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(soB2cEntity.getShopId());
			CustomerInfoEntity customerInfo = customerFeign.getCustomerById(shopInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerId(shopInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerName(customerInfo.getName());
			soReturnInstockEntity.setSoCode(soB2cEntity.getCode());
			soReturnInstockEntity.setSoId(soB2cEntity.getId());
			soReturnInstockEntity.setShopId(soB2cEntity.getShopId());
			soReturnInstockEntity.setCurrency(soB2cEntity.getCurrency());
			if (CharSequenceUtil.isNotBlank(soB2cEntity.getPlatformCode())) {
				soReturnInstockEntity.setPlatformOrderCode(soB2cEntity.getPlatformCode());
			}
		} else {
			soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
			soReturnInstockEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
			soReturnInstockEntity.setCurrencySymbol("¥");
		}
		if (Objects.nonNull(soOutstock)) {
			if (StringUtils.isNotBlank(soOutstock.getSalesDeptId())) {
				SysDepartmentDTO department = sysUserFeign.getUserDeptById(soOutstock.getSalesDeptId());
				if (null != department) {
					soReturnInstockEntity.setSalesDeptId(soOutstock.getSalesDeptId());
					soReturnInstockEntity.setSalesDeptName(department.getName());
				}
			}
			soReturnInstockEntity.setSellerId(soOutstock.getSellerId());
			soReturnInstockEntity.setSellerName(soOutstock.getSellerName());
		}
		return soReturnInstockEntity;
	}

	/**
	 * 众包退货入库单实体构建
	 */
	private SoReturnInstockEntity buildZhongBaoSoReturnInstockEntity(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoB2cEntity soB2cEntity, SoInfoEntity soInfoEntity, SoOutstockEntity soOutstock) {
		SoReturnInstockEntity soReturnInstockEntity = new SoReturnInstockEntity();
		CustomerInfoEntity customerInfo = null;
		if (StringUtils.isNotBlank(warehouseEntity.getId())) {
			soReturnInstockEntity.setApproveTime(LocalDateTime.now());
			soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
		}
		soReturnInstockEntity.setBillDate(dto.getPutawayLocalDate());
		soReturnInstockEntity.setInventoryOrgId(warehouseEntity.getOrgId());
		soReturnInstockEntity.setReturnLogisticCode(dto.getReturnLogisticCode());
		soReturnInstockEntity.setSourceId(dto.getSourceId());
		if (StringUtils.isNotBlank(warehouseEntity.getId())) {
			SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
			soReturnInstockEntity.setInventoryOrgName(company.getCompanyName());
		}
		soReturnInstockEntity.setWarehouseKeeperId(warehouseEntity.getChargeId());
		soReturnInstockEntity.setApproveUserName("system");
		soReturnInstockEntity.setSourceType(SourceTypeEnum.THIRD_WAREHOUSE_RETURN_INSTOCK.getCode());
		soReturnInstockEntity.setThirdCode(dto.getPlatformReturnOrderNo());
		soReturnInstockEntity.setCreated(dto.getCreateTime());
		if (Objects.nonNull(soB2cEntity)) {
			soReturnInstockEntity.setSourceCode(soB2cEntity.getCode());
			soReturnInstockEntity.setType("B2C");
			soReturnInstockEntity.setSalesOrgId(soB2cEntity.getOrgId());
			soReturnInstockEntity.setSalesOrgName(soB2cEntity.getOrgName());
			ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(soB2cEntity.getShopId());
			customerInfo = customerFeign.getCustomerById(shopInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerId(shopInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerName(customerInfo.getName());
			soReturnInstockEntity.setSoCode(soB2cEntity.getCode());
			soReturnInstockEntity.setSourceCode(soB2cEntity.getCode());
			soReturnInstockEntity.setSoId(soB2cEntity.getId());
			soReturnInstockEntity.setShopId(soB2cEntity.getShopId());
			soReturnInstockEntity.setCurrency(soB2cEntity.getCurrency());
		} else if (Objects.nonNull(soInfoEntity)) {
			soReturnInstockEntity.setSourceCode(soInfoEntity.getCode());
			soReturnInstockEntity.setType("B2B");
			soReturnInstockEntity.setSalesOrgId(soInfoEntity.getSalesOrgId());
			soReturnInstockEntity.setSalesOrgName(soInfoEntity.getSalesOrgName());
			customerInfo = customerFeign.getCustomerById(soInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerId(soInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerName(customerInfo.getName());
			soReturnInstockEntity.setSoCode(soInfoEntity.getCode());
			soReturnInstockEntity.setSourceCode(soInfoEntity.getCode());
			soReturnInstockEntity.setSoId(soInfoEntity.getId());
			soReturnInstockEntity.setCurrency(soInfoEntity.getCurrency());
		} else {
			soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
			soReturnInstockEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
			soReturnInstockEntity.setCurrencySymbol("¥");
		}
		if (Objects.nonNull(soOutstock)) {
			if (StringUtils.isNotBlank(soOutstock.getSalesDeptId())) {
				SysDepartmentDTO department = sysUserFeign.getUserDeptById(soOutstock.getSalesDeptId());
				if (null != department) {
					soReturnInstockEntity.setSalesDeptId(soOutstock.getSalesDeptId());
					soReturnInstockEntity.setSalesDeptName(department.getName());
				}
			}
		}
		if (Objects.nonNull(customerInfo)) {
			soReturnInstockEntity.setSellerId(customerInfo.getSellerId());
			soReturnInstockEntity.setSellerName(customerInfo.getSellerName());
		}
		return soReturnInstockEntity;
	}

	/**
	 * 极兔退货入库单实体构建
	 */
	private SoReturnInstockEntity buildJiTuSoReturnInstockEntity(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoB2cEntity soB2cEntity, SoInfoEntity soInfoEntity, SoOutstockEntity soOutstock) {
		SoReturnInstockEntity soReturnInstockEntity = new SoReturnInstockEntity();
		CustomerInfoEntity customerInfo = null;

		// 默认自动提交并审核通过
		if (StringUtils.isNotBlank(warehouseEntity.getId())) {
			soReturnInstockEntity.setApproveTime(LocalDateTime.now());
			soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
		}
		// 入库日期取关单时间，为空兜底当前日期
		soReturnInstockEntity.setBillDate(dto.getPutawayLocalDate());
		// 库存组织
		soReturnInstockEntity.setInventoryOrgId(warehouseEntity.getOrgId());
		// 组织信息
		if (StringUtils.isNotBlank(warehouseEntity.getId())) {
			SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
			soReturnInstockEntity.setInventoryOrgName(company.getCompanyName());
		}
		// 退货物流单号
		soReturnInstockEntity.setReturnLogisticCode(dto.getReturnLogisticCode());
		// 第三方单据编号
		soReturnInstockEntity.setThirdCode(dto.getPlatformReturnOrderNo());
		// 来源类型
		soReturnInstockEntity.setSourceType(SourceTypeEnum.THIRD_WAREHOUSE_RETURN_INSTOCK.getCode());
		// 创建时间
		soReturnInstockEntity.setCreated(dto.getCreateTime());
		// 仓库信息
		soReturnInstockEntity.setWarehouseKeeperId(warehouseEntity.getChargeId());
		if (Objects.nonNull(soB2cEntity)) {
			// B2C订单
			soReturnInstockEntity.setType("B2C");
			//取客户订单号，匹配数大臣的B2C三方仓发货单的三方仓订单号，匹配到后将发货单的销售单号作为退货入库单的来源订单号
			if (Objects.equals(soB2cEntity.getShippingOrderNo(), dto.getOrderReferenceNo())) {
				soReturnInstockEntity.setSourceId(soB2cEntity.getId());
				soReturnInstockEntity.setSourceCode(soB2cEntity.getCode());
			}
			soReturnInstockEntity.setSalesOrgId(soB2cEntity.getOrgId());
			soReturnInstockEntity.setSalesOrgName(soB2cEntity.getOrgName());
			// 退货客户
			ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(soB2cEntity.getShopId());
			customerInfo = customerFeign.getCustomerById(shopInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerId(shopInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerName(customerInfo.getName());
			// 销售订单信息
			soReturnInstockEntity.setSoCode(soB2cEntity.getCode());
			soReturnInstockEntity.setSoId(soB2cEntity.getId());
			soReturnInstockEntity.setShopId(soB2cEntity.getShopId());
			// 平台订单编号
			soReturnInstockEntity.setPlatformOrderCode(soB2cEntity.getPlatformCode());
			// 币种
			soReturnInstockEntity.setCurrency(soB2cEntity.getCurrency());
		} else if (Objects.nonNull(soInfoEntity)) {
			// B2B订单
			soReturnInstockEntity.setType("B2B");
			//未匹配到时匹配B2B三方发货单的三方仓订单号，匹配到后将发货单的销售单号作为退货入库单的来源订单号
			if (StringUtils.isNotBlank(dto.getOrderReferenceNo())) {
				B2bThirdDeliveryEntity b2bThirdDelivery = b2bThirdDeliveryService.lambdaQuery()
						.eq(B2bThirdDeliveryEntity::getPlatformOrderCode, dto.getOrderReferenceNo())
						.one();
				if (Objects.nonNull(b2bThirdDelivery)) {
					soReturnInstockEntity.setSourceId(b2bThirdDelivery.getId());
					soReturnInstockEntity.setSourceCode(b2bThirdDelivery.getSoCode());
				}
			}
			soReturnInstockEntity.setSalesOrgId(soInfoEntity.getSalesOrgId());
			soReturnInstockEntity.setSalesOrgName(soInfoEntity.getSalesOrgName());
			// 退货客户
			customerInfo = customerFeign.getCustomerById(soInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerId(soInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerName(customerInfo.getName());
			// 销售订单信息
			soReturnInstockEntity.setSoCode(soInfoEntity.getCode());
			soReturnInstockEntity.setSoId(soInfoEntity.getId());
			// 平台订单编号，与B2C分支保持一致：销售订单平台单号为空时用平台消息单号兜底
			soReturnInstockEntity.setPlatformOrderCode(CharSequenceUtil.isNotBlank(soInfoEntity.getPlatformOrderCode()) ? soInfoEntity.getPlatformOrderCode() : dto.getPlatformOrderNo());
			// 币种
			soReturnInstockEntity.setCurrency(soInfoEntity.getCurrency());
			soReturnInstockEntity.setCurrencySymbol(soInfoEntity.getCurrencySymbol());
		} else {
			// 未关联到订单
			soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
			soReturnInstockEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
			soReturnInstockEntity.setCurrencySymbol("¥");
		}

		// 销售部门和销售员
		if (Objects.nonNull(soOutstock)) {
			if (StringUtils.isNotBlank(soOutstock.getSalesDeptId())) {
				SysDepartmentDTO department = sysUserFeign.getUserDeptById(soOutstock.getSalesDeptId());
				if (null != department) {
					soReturnInstockEntity.setSalesDeptId(soOutstock.getSalesDeptId());
					soReturnInstockEntity.setSalesDeptName(department.getName());
				}
			}
			soReturnInstockEntity.setSellerId(soOutstock.getSellerId());
			soReturnInstockEntity.setSellerName(soOutstock.getSellerName());
		}

		if (Objects.nonNull(customerInfo)) {
			soReturnInstockEntity.setSellerId(customerInfo.getSellerId());
			soReturnInstockEntity.setSellerName(customerInfo.getSellerName());
		}

		return soReturnInstockEntity;
	}

	/**
	 * 极兔退货入库单明细构建：应退数量取已存在退货单明细汇总，退货/含税退货金额按销售订单明细价格计算并折算本位币
	 */
	private List<SoReturnInstockDetailEntity> buildJiTuSoReturnInstockDetail(PlatformReturnInstockDTO dto, SoB2cEntity soB2cEntity, SoInfoEntity soInfoEntity, WarehouseEntity warehouseEntity) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		List<SoB2cDetailEntity> soB2cDetails = new ArrayList<>();
		List<SoDetailEntity> soDetails = new ArrayList<>();
		List<SoB2cReturnDetailEntity> soB2cReturnDetails = new ArrayList<>();
		List<SoReturnDetailEntity> soReturnDetails = new ArrayList<>();

		if (CollectionUtils.isEmpty(details)) {
			throw new ServiceException("明细为空");
		}

		// 获取SKU映射
		List<String> platformSkuNoList = dto.getProductDetailList().stream().map(v -> v.getProductSku()).collect(Collectors.toList());
		ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
		listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
		listingInfoParamDTO.setAuthId(dto.getAuthId());
		listingInfoParamDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
		List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);

		if (mappingSkuViewDTOList.isEmpty()) {
			throw new ServiceException("没有找到sku映射");
		}

		// 获取订单明细和关联数据
		BigDecimal rate = new BigDecimal("1");
		if (Objects.nonNull(soB2cEntity)) {
			try {
				rate = dmpTaskFeign.getRate(soB2cEntity.getBillDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), soB2cEntity.getCurrency());
			} catch (Exception e) {
				throw new ServiceException(soB2cEntity.getCurrency() + "获取汇率失败");
			}
			// B2C订单
			soB2cDetails.addAll(soB2cFeign.listDetailByMainIds(Collections.singletonList(soB2cEntity.getId())));

			// 获取退货订单明细
			List<SoB2cReturnEntity> soB2cReturnEntities = FeignQuery.create(SoB2cReturnEntity.class)
					.eq(SoB2cReturnEntity::getSoId, soB2cEntity.getId())
					.list();
			if (CollectionUtils.isNotEmpty(soB2cReturnEntities)) {
				List<String> returnIds = soB2cReturnEntities.stream().map(SoB2cReturnEntity::getId).collect(Collectors.toList());
				soB2cReturnDetails.addAll(FeignQuery.create(SoB2cReturnDetailEntity.class)
						.in(SoB2cReturnDetailEntity::getMainId, returnIds)
						.list());
			}
		} else if (Objects.nonNull(soInfoEntity)) {
			try {
				rate = dmpTaskFeign.getRate(soInfoEntity.getBillDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), soInfoEntity.getCurrency());
			} catch (Exception e) {
				throw new ServiceException(soInfoEntity.getCurrency() + "获取汇率失败");
			}
			// B2B订单
			soDetails.addAll(soInfoFeign.listSoDetailByMainId(soInfoEntity.getId()));

			// 获取退货订单明细
			List<SoReturnEntity> soReturnEntities = FeignQuery.create(SoReturnEntity.class)
					.eq(SoReturnEntity::getSourceId, soInfoEntity.getId())
					.list();
			if (CollectionUtils.isNotEmpty(soReturnEntities)) {
				List<String> returnIds = soReturnEntities.stream().map(SoReturnEntity::getId).collect(Collectors.toList());
				soReturnDetails.addAll(FeignQuery.create(SoReturnDetailEntity.class)
						.in(SoReturnDetailEntity::getMainId, returnIds)
						.list());
			}
		}

		List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();

		for (PlatformReturnInstockDTO.Detail detail : details) {
			// 匹配SKU
			SkuMappingDTO.MappingSkuViewDTO skuViewDTO = mappingSkuViewDTOList.stream()
					.filter(v -> v.getPlatformSkuNo().equals(detail.getProductSku()))
					.findFirst()
					.orElse(null);

			if (Objects.isNull(skuViewDTO)) {
				continue;
			}

			SoReturnInstockDetailEntity soReturnInstockDetailEntity = new SoReturnInstockDetailEntity();
			soReturnInstockDetailEntity.setSkuId(skuViewDTO.getProductSkuId());
			soReturnInstockDetailEntity.setSkuNo(skuViewDTO.getProductSkuNo());
			// 仓库信息
			soReturnInstockDetailEntity.setWarehouseId(warehouseEntity.getId());
			soReturnInstockDetailEntity.setWarehouseName(warehouseEntity.getName());
			// 备注和退货类型
			soReturnInstockDetailEntity.setRemark(dto.getReason());
			soReturnInstockDetailEntity.setReturnTypeDict(dto.getReturnType());
			// 来源明细ID
			soReturnInstockDetailEntity.setSourceDetailId(detail.getThirdId());
			soReturnInstockDetailEntity.setCreateUserId(dto.getAuthId());
			soReturnInstockDetailEntity.setPlatformSkuNo(detail.getProductSku());

			// 计算数量
			int shouldReturnQty = 0; // 应退数量
			int actualQty = detail.getReceiveQty() != null ? detail.getReceiveQty() : 0;

			if (!soB2cDetails.isEmpty()) {
				// B2C订单
				SoB2cDetailEntity soB2cDetailEntity = soB2cDetails.stream()
						.filter(item -> Objects.equals(item.getSkuId(), skuViewDTO.getPlatformSkuNo()))
						.findFirst()
						.orElse(null);
				if (Objects.nonNull(soB2cDetailEntity)) {

					// 应退数量
					shouldReturnQty = soB2cReturnDetails.stream()
							.filter(item -> Objects.equals(item.getSkuId(), skuViewDTO.getProductSkuId()))
							.mapToInt(item -> item.getReturnQty() != null ? item.getReturnQty() : 0)
							.sum();

					// 计算退货金额和含税退货金额
					// 取销售订单的订单金额*订单明细的真实售价占比*（上架数量/销售数量）
					BigDecimal amount = MathUtil.multiplyWithTwo(soB2cDetailEntity.getPrice(), actualQty);
					soReturnInstockDetailEntity.setAmount(amount);
					soReturnInstockDetailEntity.setTaxReturnAmount(amount);
					soReturnInstockDetailEntity.setTaxReturnAmountLocalCurrency(MathUtil.multiplyWithTwo(amount, rate));
				} else {
					soReturnInstockDetailEntity.setAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setTaxReturnAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setTaxReturnAmountLocalCurrency(BigDecimal.ZERO);
				}
			} else if (!soDetails.isEmpty()) {
				// B2B订单
				SoDetailEntity soDetailEntity = soDetails.stream()
						.filter(item -> Objects.equals(item.getSkuId(), skuViewDTO.getPlatformSkuNo()))
						.findFirst()
						.orElse(null);
				if (Objects.nonNull(soDetailEntity)) {

					// 应退数量
					shouldReturnQty = soReturnDetails.stream()
							.filter(item -> Objects.equals(item.getSkuId(), skuViewDTO.getProductSkuId()))
							.mapToInt(item -> item.getReturnQty() != null ? item.getReturnQty() : 0)
							.sum();

					// 计算退货金额和含税退货金额
					// 取销售订单的明细销售单价*上架数量
					soReturnInstockDetailEntity.setReturnAmount(MathUtil.multiplyWithTwo(soDetailEntity.getPrice(), actualQty));
					// 取销售订单的含税单价*退货数量
					BigDecimal amount = MathUtil.multiplyWithTwo(soDetailEntity.getTaxPrice(), actualQty);
					soReturnInstockDetailEntity.setTaxReturnAmount(amount);
					soReturnInstockDetailEntity.setTaxReturnAmountLocalCurrency(MathUtil.multiplyWithTwo(amount, rate));
				} else {
					soReturnInstockDetailEntity.setAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setTaxReturnAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setTaxReturnAmountLocalCurrency(BigDecimal.ZERO);
				}
			} else {
				// 未匹配到订单
				soReturnInstockDetailEntity.setAmount(BigDecimal.ZERO);
				soReturnInstockDetailEntity.setTaxReturnAmount(BigDecimal.ZERO);
				soReturnInstockDetailEntity.setTaxReturnAmountLocalCurrency(BigDecimal.ZERO);
			}

			soReturnInstockDetailEntity.setMustQty(shouldReturnQty);
			soReturnInstockDetailEntity.setReceiveQty(actualQty);
			soReturnInstockDetailEntity.setRealQty(actualQty);
			soReturnInstockDetailEntity.setDefectiveProductFlag(detail.getDefectiveProductFlag());
			detailEntityList.add(soReturnInstockDetailEntity);
		}

		return detailEntityList;
	}

	/**
	 * 平台仓入库平台处理
	 */
	public void platformWarehouseHandle(PlatformReturnInstockDTO dto) {
		// 根据
		List<SoReturnInstockEntity> soReturnList = soReturnInstockService.lambdaQuery()
				.in(SoReturnInstockEntity::getSourceCode, dto.getUniqueId())
				.list();
		if(CollectionUtils.isNotEmpty(soReturnList)){
			log.warn("【平台退货入库】退货入库单已存在:{}", dto.getUniqueId());
			return;
		}
		// 查询对应店铺
		String platformShopCode = dto.getAuthId();
		List<ShopInfoEntity> shopList = shopInfoFeign.listByParams(new ShopInfoDTO.ListParamDTO(AuthStatusEnum.ALREADY.getCode(),dto.getPlatform(), null));
		List<String> shopIds ;
		//京东取的是店铺ID
		if (CharSequenceUtil.equals(dto.getPlatform(),PlatformDictEnum.NASDAQ_JD.getCode()) ) {
			shopIds = shopList.stream()
					.map(BaseEntity::getId)
					.filter(id -> id.equals(platformShopCode))
					.collect(Collectors.toList());
		} else {
			shopIds = shopList.stream()
					.filter(e -> e.getPlatformShopCode().equalsIgnoreCase(platformShopCode))
					.map(BaseEntity::getId)
					.collect(Collectors.toList());
		}

		if (CollectionUtils.isEmpty(shopIds)){
			log.warn("【平台退货入库】店铺不存在:店铺代号{}", dto.getAuthId());
			ServiceException.runError("【平台退货入库】店铺不存在:店铺代号{}", dto.getAuthId());
		}
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		if(CollectionUtils.isEmpty(details)){
			ServiceException.runError("【平台退货入库】来源明细为空");
		}
		SoB2cEntity soB2cEntity = soReturnInstockSalesMatchService.matchOriginalSoB2c(dto);
		if (Objects.isNull(soB2cEntity)) {
			List<SoB2cEntity> soB2cEntityList = soB2cFeign.getByPlatformCode(
					Collections.singletonList(dto.getPlatformOrderNo()),
					dto.getPlatform(),
					"",
					SourceTypeEnum.SO_B2C.getCode()
			);
			soB2cEntity = soB2cEntityList.stream().filter(e -> shopIds.contains(e.getShopId())).findFirst().orElse(null);
		}
		if (null == soB2cEntity){
			log.warn("【平台退货入库】匹配销售订单不存在:{}", dto.getPlatformOrderNo());
			// 不存在按当前映射关系生成
			createByNotExistSoB2c(dto, shopList, shopIds);
		} else {
			// 存在按销售订单映射生成
			createByExistSoB2c(dto, shopList, soB2cEntity);
		}

	}


	private List<SoReturnInstockDetailEntity> buildPlatformSoReturnInstockDetail(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoB2cEntity soB2cEntity) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		// 销售订单明细
		List<SoB2cDetailEntity> soDetailEntityList = soB2cFeign.listDetailByMainIds(Collections.singletonList(soB2cEntity.getId()));
		if (CollectionUtils.isEmpty(soDetailEntityList)){
			ServiceException.runError("【平台退货入库】对应订单明细为空：{}", dto.getPlatformOrderNo());
		}
		List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();
		for (PlatformReturnInstockDTO.Detail detail : details) {
			// 优先匹配 platformSkuNo（平台/销售渠道SKU），未命中时按 warehouseSkuNo（仓库SKU）兜底——
			// 部分海外仓来源平台回传的 productSku 实际是仓库侧SKU而非平台SKU
			SoB2cDetailEntity detailEntity = soDetailEntityList.stream()
					.filter(e -> StringUtils.isNotBlank(e.getPlatformSkuNo()) && e.getPlatformSkuNo().equalsIgnoreCase(detail.getProductSku()))
					.findFirst()
					.orElseGet(() -> soDetailEntityList.stream()
							.filter(e -> StringUtils.isNotBlank(e.getWarehouseSkuNo()) && e.getWarehouseSkuNo().equalsIgnoreCase(detail.getProductSku()))
							.findFirst()
							.orElse(null));
			if(null == detailEntity){
				ServiceException.runError("找不到销售订单明细:{}", dto.getPlatformOrderNo());
			}
			SoReturnInstockDetailEntity soReturnInstockDetailEntity = new SoReturnInstockDetailEntity();
			soReturnInstockDetailEntity.setSkuId(detailEntity.getSkuId());
			soReturnInstockDetailEntity.setSkuNo(detailEntity.getSkuNo());
			soReturnInstockDetailEntity.setMustQty(detail.getMustQty());
			soReturnInstockDetailEntity.setReceiveQty(detail.getReceiveQty());
			soReturnInstockDetailEntity.setRealQty(detail.getRealQty());
			soReturnInstockDetailEntity.setWarehouseId(warehouseEntity.getId());
			soReturnInstockDetailEntity.setWarehouseName(warehouseEntity.getName());
			soReturnInstockDetailEntity.setRemark(dto.getReason());
			soReturnInstockDetailEntity.setReturnTypeDict(dto.getReturnType());
			soReturnInstockDetailEntity.setDefectiveProductFlag(detail.getDefectiveProductFlag());
			detailEntityList.add(soReturnInstockDetailEntity);
		}
		return detailEntityList;
	}

	private SoReturnInstockEntity buildPlatformSoReturnInstockEntity(PlatformReturnInstockDTO dto,
															 WarehouseEntity warehouseEntity,
															 SoB2cEntity soB2cEntity,
															 ShopInfoEntity shopInfoEntity
	){
		SoReturnInstockEntity soReturnInstockEntity = new SoReturnInstockEntity();
		soReturnInstockEntity.setApproveTime(LocalDateTime.now());
		soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
		soReturnInstockEntity.setBillDate(dto.getPutawayLocalDate());
		soReturnInstockEntity.setInventoryOrgId(warehouseEntity.getOrgId());
		//组织信息
		SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
		soReturnInstockEntity.setInventoryOrgName(company.getCompanyName());
		soReturnInstockEntity.setWarehouseKeeperId(warehouseEntity.getChargeId());
		soReturnInstockEntity.setApproveUserName("system");
		soReturnInstockEntity.setSourceCode(dto.getUniqueId());
		soReturnInstockEntity.setSourceType(SourceTypeEnum.PLATFORM_RETURN_INSTOCK.getCode());
		soReturnInstockEntity.setThirdCode(dto.getPlatformReturnOrderNo());
		soReturnInstockEntity.setPlatformOrderCode(dto.getPlatformOrderNo());
		soReturnInstockEntity.setCreated(dto.getCreateTime());
		soReturnInstockEntity.setType("B2C");
		soReturnInstockEntity.setReturnLogisticCode(dto.getReturnLogisticCode());
		ShopInfoEntity customerShop = shopInfoEntity;
		if (Objects.nonNull(soB2cEntity)) {
			soReturnInstockEntity.setSoId(soB2cEntity.getId());
			soReturnInstockEntity.setSoCode(soB2cEntity.getCode());
			soReturnInstockEntity.setCurrency(soB2cEntity.getCurrency());
			soReturnInstockEntity.setShopId(soB2cEntity.getShopId());
			if (CharSequenceUtil.isNotBlank(soB2cEntity.getPlatformCode())) {
				soReturnInstockEntity.setPlatformOrderCode(soB2cEntity.getPlatformCode());
			}
			ShopInfoEntity originalShop = shopInfoFeign.getShopInfoById(soB2cEntity.getShopId());
			if (Objects.nonNull(originalShop)) {
				customerShop = originalShop;
			}
		} else {
			soReturnInstockEntity.setCurrency(CharSequenceUtil.isBlank(shopInfoEntity.getSettlementCurrency()) ? CurrencyEnum.CNY.getCurrencyCode() : shopInfoEntity.getSettlementCurrency());
		}
		if (StringUtils.isBlank(customerShop.getCustomerId())){
			ServiceException.runError("店铺对应客户信息为空:{}", customerShop.getName());
		}
		CustomerInfoEntity customerInfo = customerFeign.getCustomerById(customerShop.getCustomerId());
		if (null == customerInfo){
			ServiceException.runError("店铺对应客户信息不存在:客户ID={}", customerShop.getCustomerId());
		}
		soReturnInstockEntity.setSalesOrgId(customerInfo.getUseOrgId());
		soReturnInstockEntity.setSalesOrgName(customerInfo.getUseOrgName());
		soReturnInstockEntity.setCustomerId(customerShop.getCustomerId());
		soReturnInstockEntity.setCustomerName(customerInfo.getName());
		if (StringUtils.isNotBlank(customerInfo.getSalesDeptId())){
			SysDepartmentDTO department = sysUserFeign.getUserDeptById(customerInfo.getSalesDeptId());
			if( null != department){
				soReturnInstockEntity.setSalesDeptId(customerInfo.getSalesDeptId());
				soReturnInstockEntity.setSalesDeptName(department.getName());
			}
		}
		soReturnInstockEntity.setSellerId(customerInfo.getSellerId());
		soReturnInstockEntity.setSellerName(customerInfo.getSellerName());
		return soReturnInstockEntity;
	}

	/**
	 * 不存在销售订单创建退货入库单
	 */
	private void createByNotExistSoB2c(PlatformReturnInstockDTO dto, List<ShopInfoEntity> shopList, List<String> shopIds) {
		List<ShopInfoEntity> allAccnountShopList = shopList.stream().filter(e -> shopIds.contains(e.getId())).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(allAccnountShopList)){
			log.warn("【平台退货入库】店铺列表不存在:{}", dto.getPlatformOrderNo());
			ServiceException.runError("【平台退货入库】店铺列表不存在:店铺代号{}", dto.getAuthId());
		}
		// TODO 临时指定站点
		ShopInfoEntity shopInfoEntity;
		if (allAccnountShopList.size() > 2){
			shopInfoEntity = allAccnountShopList.stream()
					.filter(e-> "DE".equalsIgnoreCase(e.getDictCountryCode()) || "US".equalsIgnoreCase(e.getDictCountryCode()))
					.findFirst()
					.orElse(allAccnountShopList.get(0));
		} else {
			shopInfoEntity = allAccnountShopList.get(0);
		}
		if (null == shopInfoEntity){
			log.warn("【平台退货入库】历史销售订单对应店铺不存在:{}", dto.getPlatformOrderNo());
			ServiceException.runError("【平台退货入库】历史销售订单对应店铺不存在:店铺代号{}", dto.getAuthId());
		}
		WarehouseEntity warehouseEntity = checkAndGetWarehouseByShopInfo(shopInfoEntity);
		// 查询是否已关账
		LocalDate closedLocalDate = inventoryClosedRecordService.checkClosed(warehouseEntity.getOrgId(), dto.getPutawayLocalDate());
		if (null != closedLocalDate) {
			// 已关账
			log.warn("[退货入库单消费]:当前退货入库单消费日期【{}】因关账【{}】停止生成：单号={}", dto.getPutawayLocalDate(), closedLocalDate, dto.getPlatformOrderNo());
			return;
		}
		List<SoReturnInstockDetailEntity> detailEntityList = this.buildPlatformSoReturnInstockDetailWithoutSoB2c(dto, warehouseEntity, shopInfoEntity, shopIds);
		if(CollectionUtils.isEmpty(detailEntityList)){
			ServiceException.runError("【平台退货入库】来源明细未匹配到映射");
		}
		SoReturnInstockEntity soReturnInstockEntity = this.buildPlatformSoReturnInstockEntity(dto, warehouseEntity, null, shopInfoEntity);


		soReturnInstockService.addByThirdWarehouse(soReturnInstockEntity,detailEntityList);
	}


	/**
	 * 已存在销售订单创建退货入库单
	 */
	private void createByExistSoB2c(PlatformReturnInstockDTO dto, List<ShopInfoEntity> shopList, SoB2cEntity soB2cEntity) {
		ShopInfoEntity shopInfoEntity = resolveSalesShopInfo(shopList, soB2cEntity, dto);
		WarehouseEntity warehouseEntity = checkAndGetWarehouseByShopInfo(shopInfoEntity);
		// 查询是否已关账
		LocalDate closedLocalDate = inventoryClosedRecordService.checkClosed(warehouseEntity.getOrgId(), dto.getPutawayLocalDate());
		if (null != closedLocalDate) {
			// 已关账
			log.warn("[退货入库单消费]:当前退货入库单消费日期【{}】因关账【{}】停止生成：单号={}", dto.getPutawayLocalDate(), closedLocalDate, dto.getPlatformOrderNo());
			return;
		}

		List<SoReturnInstockDetailEntity> detailEntityList = this.buildPlatformSoReturnInstockDetail(dto, warehouseEntity, soB2cEntity);
		if(CollectionUtils.isEmpty(detailEntityList)){
			ServiceException.runError("【平台退货入库】来源明细未匹配到映射");
		}
		SoReturnInstockEntity soReturnInstockEntity = this.buildPlatformSoReturnInstockEntity(dto, warehouseEntity, soB2cEntity, shopInfoEntity);

		//关联销售退货单
		this.matchSoReturn(soReturnInstockEntity,detailEntityList,dto, soB2cEntity);

		soReturnInstockService.addByThirdWarehouse(soReturnInstockEntity,detailEntityList);
	}


	private ShopInfoEntity resolveSalesShopInfo(List<ShopInfoEntity> shopList, SoB2cEntity soB2cEntity, PlatformReturnInstockDTO dto) {
		ShopInfoEntity shopInfoEntity = shopList.stream()
				.filter(e -> e.getId().equalsIgnoreCase(soB2cEntity.getShopId()))
				.findFirst()
				.orElse(null);
		if (Objects.isNull(shopInfoEntity)) {
			shopInfoEntity = shopInfoFeign.getShopInfoById(soB2cEntity.getShopId());
		}
		if (Objects.isNull(shopInfoEntity)) {
			log.warn("【平台退货入库】店铺不存在:{}", dto.getPlatformOrderNo());
			ServiceException.runError("【平台退货入库】店铺不存在:店铺代号{}", dto.getAuthId());
		}
		return shopInfoEntity;
	}

	/**
	 * 平台退货入库
	 */
	private WarehouseEntity checkAndGetWarehouseByShopInfo(ShopInfoEntity shopInfoEntity) {
		String returnWarehouse = StringUtils.isBlank(shopInfoEntity.getReturnWarehouse()) ? shopInfoEntity.getWarehouseId() : shopInfoEntity.getReturnWarehouse();
		WarehouseEntity warehouseEntity = warehouseService.getById(returnWarehouse);
		if (null == warehouseEntity){
			ServiceException.runError("未找到店铺配置的退货仓库或关联店铺:{}", shopInfoEntity.getName());
		}
		return warehouseEntity;
	}


	private List<SoReturnInstockDetailEntity> buildPlatformSoReturnInstockDetailWithoutSoB2c(PlatformReturnInstockDTO dto,
																							 WarehouseEntity warehouseEntity,
																							 ShopInfoEntity shopInfoEntity,
																							 List<String> shopIds
	) {
		List<String> platformSkuList = dto.getProductDetailList().stream().map(PlatformReturnInstockDTO.Detail::getProductSku).distinct().collect(Collectors.toList());
		// 查询店铺映射:
		ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
		paramDTO.setPlatformSkuNoList(platformSkuList);
		paramDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
		paramDTO.setIsExpire(false);
		//传参调整
		if  (CharSequenceUtil.equals(dto.getPlatform(), PlatformDictEnum.NASDAQ_JD.getCode())) {
			paramDTO.setType(RuleTypeEnum.WAREHOUSE.getCode());
			paramDTO.setWarehouseIdList(Collections.singletonList(warehouseEntity.getId()));
		} else {
			paramDTO.setPlatform(dto.getPlatform());
			paramDTO.setShopIdList(shopIds);
			paramDTO.setType(RuleTypeEnum.B2C_PLATFORM.getCode());
		}
		// 查询ListingInfo和skuMapping的关系
		List<ListingInfoWithSkuMappingDTO> listingedInfoWithSkuMappingList = skuMappingFeign.listingInfoWithSkuMappingList(paramDTO);
		Map<String, List<ListingInfoWithSkuMappingDTO>> mappingRelationMap = listingedInfoWithSkuMappingList.stream()
				.collect(Collectors.groupingBy(ListingInfoWithSkuMappingDTO::getPlatformSkuNo));

		List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();
		for (PlatformReturnInstockDTO.Detail detail : dto.getProductDetailList()) {
			List<ListingInfoWithSkuMappingDTO> mappingDTOList = mappingRelationMap.get(detail.getProductSku());
			if(CollectionUtils.isEmpty(mappingDTOList)){
				ServiceException.runError("未找到sku映射记录:{}", dto.getPlatformOrderNo());
			}
			ListingInfoWithSkuMappingDTO mappingDTO = checkAndMappingDTO(mappingDTOList, shopInfoEntity.getId());
			SoReturnInstockDetailEntity soReturnInstockDetailEntity = new SoReturnInstockDetailEntity();
			soReturnInstockDetailEntity.setSkuId(mappingDTO.getProductSkuId());
			soReturnInstockDetailEntity.setSkuNo(mappingDTO.getProductSkuNo());
			soReturnInstockDetailEntity.setPlatformSkuNo(detail.getProductSku());
			soReturnInstockDetailEntity.setMustQty(detail.getMustQty());
			soReturnInstockDetailEntity.setReceiveQty(detail.getReceiveQty());
			soReturnInstockDetailEntity.setRealQty(detail.getRealQty());
			soReturnInstockDetailEntity.setWarehouseId(warehouseEntity.getId());
			soReturnInstockDetailEntity.setWarehouseName(warehouseEntity.getName());
			soReturnInstockDetailEntity.setRemark(dto.getReason());
			soReturnInstockDetailEntity.setReturnTypeDict(dto.getReturnType());
			detailEntityList.add(soReturnInstockDetailEntity);
		}
		return detailEntityList;
	}

	/**
	 * 检查或获取映射关系
	 */
	public ListingInfoWithSkuMappingDTO checkAndMappingDTO(List<ListingInfoWithSkuMappingDTO> mappingDTOList,String shopId) {
		if (1 == mappingDTOList.size()){
			return mappingDTOList.get(0);
		}
		return mappingDTOList.stream()
				.filter(e->e.getShopId().equalsIgnoreCase(shopId))
				.findFirst()
				.orElse(mappingDTOList.get(0));
	}

	/**
	 * 根据产品条码查询sku
	 * @author will
	 * @date 2025/12/4 11:50
	 * @param dto
	 * @return void
	 */
	private void handleThirdBarcode (PlatformReturnInstockDTO dto) {
		List<String> thirdBarcodeList = dto.getProductDetailList().stream().map(PlatformReturnInstockDTO.Detail::getThirdBarcode).distinct().collect(Collectors.toList());
		if (CollUtil.isEmpty(thirdBarcodeList) || !CharSequenceUtil.equals(dto.getPlatform(), PlatformDictEnum.TONG_YOU_WAREHOUSE.getCode())) {
			return;
		}
		List<ListingInfoEntity> list = FeignQuery.create(ListingInfoEntity.class)
				.eq(ListingInfoEntity::getPlatform, dto.getPlatform())
				.eq(ListingInfoEntity::getAuthId, dto.getAuthId())
				.in(ListingInfoEntity::getThirdBarcode, thirdBarcodeList)
				.list();
		if (CollUtil.isEmpty(list)) {
			return;
		}
		for (PlatformReturnInstockDTO.Detail item : dto.getProductDetailList()) {
			list.stream().filter(obj -> CharSequenceUtil.equals(dto.getAuthId(),obj.getAuthId()) && CharSequenceUtil.equals(dto.getPlatform(),obj.getPlatform()) && CharSequenceUtil.equals(item.getThirdBarcode(),obj.getThirdBarcode()))
					.findFirst()
					.ifPresent(obj -> item.setProductSku(obj.getPlatformSkuNo()));
		}
	}
	
	/**
	 * 构建极兔退货入库单实体
	 */
	private SoReturnInstockEntity buildJiTuSoReturnInstockEntity(PlatformReturnInstockDTO dto,WarehouseEntity warehouseEntity,SoB2cEntity soB2cEntity,SoInfoEntity soInfoEntity,SoOutstockEntity soOutstock){
		SoReturnInstockEntity soReturnInstockEntity = new SoReturnInstockEntity();
		CustomerInfoEntity customerInfo = null;

		// 默认自动提交并审核通过
		if(StringUtils.isNotBlank(warehouseEntity.getId())){
			soReturnInstockEntity.setApproveTime(LocalDateTime.now());
			soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
		}
		// 入库日期取关单时间
		if (dto.getPutawayTime() != null) {
			soReturnInstockEntity.setBillDate(dto.getPutawayTime().toLocalDate());
		}
		// 库存组织
		soReturnInstockEntity.setInventoryOrgId(warehouseEntity.getOrgId());
		// 组织信息
		if(StringUtils.isNotBlank(warehouseEntity.getId())){
			SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
			soReturnInstockEntity.setInventoryOrgName(company.getCompanyName());
		}
		// 退货物流单号
		soReturnInstockEntity.setReturnLogisticCode(dto.getReturnLogisticCode());
		// 第三方单据编号
		soReturnInstockEntity.setThirdCode(dto.getPlatformReturnOrderNo());
		// 来源类型
		soReturnInstockEntity.setSourceType(SourceTypeEnum.THIRD_WAREHOUSE_RETURN_INSTOCK.getCode());
		// 创建时间
		soReturnInstockEntity.setCreated(dto.getCreateTime());
		// 仓库信息
		soReturnInstockEntity.setWarehouseKeeperId(warehouseEntity.getChargeId());
		if (Objects.nonNull(soB2cEntity)) {
			// B2C订单
			soReturnInstockEntity.setType("B2C");
			//取客户订单号，匹配数大臣的B2C三方仓发货单的三方仓订单号，匹配到后将发货单的销售单号作为退货入库单的来源订单号
			if (Objects.equals(soB2cEntity.getShippingOrderNo(),dto.getOrderReferenceNo())) {
				soReturnInstockEntity.setSourceId(soB2cEntity.getId());
				soReturnInstockEntity.setSourceCode(soB2cEntity.getCode());
			}
			soReturnInstockEntity.setSalesOrgId(soB2cEntity.getOrgId());
			soReturnInstockEntity.setSalesOrgName(soB2cEntity.getOrgName());
			// 退货客户
			ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(soB2cEntity.getShopId());
			customerInfo = customerFeign.getCustomerById(shopInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerId(shopInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerName(customerInfo.getName());
			// 销售订单信息
			soReturnInstockEntity.setSoCode(soB2cEntity.getCode());
			soReturnInstockEntity.setSoId(soB2cEntity.getId());
			soReturnInstockEntity.setShopId(soB2cEntity.getShopId());
			// 平台订单编号：销售订单平台单号为空时用平台消息单号兜底
			soReturnInstockEntity.setPlatformOrderCode(CharSequenceUtil.isNotBlank(soB2cEntity.getPlatformCode()) ? soB2cEntity.getPlatformCode() : dto.getPlatformOrderNo());
			// 币种
			soReturnInstockEntity.setCurrency(soB2cEntity.getCurrency());
		} else if (Objects.nonNull(soInfoEntity)){
			// B2B订单
			soReturnInstockEntity.setType("B2B");
			//未匹配到时匹配B2B三方发货单的三方仓订单号，匹配到后将发货单的销售单号作为退货入库单的来源订单号
			if (StringUtils.isNotBlank(dto.getOrderReferenceNo())) {
				B2bThirdDeliveryEntity b2bThirdDelivery = b2bThirdDeliveryService.lambdaQuery()
						.eq(B2bThirdDeliveryEntity::getPlatformOrderCode, dto.getOrderReferenceNo())
						.one();
				if (Objects.nonNull(b2bThirdDelivery)) {
					soReturnInstockEntity.setSourceId(b2bThirdDelivery.getId());
					soReturnInstockEntity.setSourceCode(b2bThirdDelivery.getSoCode());
				}
			}
			soReturnInstockEntity.setSalesOrgId(soInfoEntity.getSalesOrgId());
			soReturnInstockEntity.setSalesOrgName(soInfoEntity.getSalesOrgName());
			// 退货客户
			customerInfo = customerFeign.getCustomerById(soInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerId(soInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerName(customerInfo.getName());
			// 销售订单信息
			soReturnInstockEntity.setSoCode(soInfoEntity.getCode());
			soReturnInstockEntity.setSoId(soInfoEntity.getId());
			// 平台订单编号，与B2C分支保持一致：销售订单平台单号为空时用平台消息单号兜底
			soReturnInstockEntity.setPlatformOrderCode(CharSequenceUtil.isNotBlank(soInfoEntity.getPlatformOrderCode()) ? soInfoEntity.getPlatformOrderCode() : dto.getPlatformOrderNo());
			// 币种
			soReturnInstockEntity.setCurrency(soInfoEntity.getCurrency());
			soReturnInstockEntity.setCurrencySymbol(soInfoEntity.getCurrencySymbol());
		} else {
			// 未关联到订单
			soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
			soReturnInstockEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
			soReturnInstockEntity.setCurrencySymbol("¥");
		}

		// 销售部门和销售员
		if(Objects.nonNull(soOutstock)){
			if (StringUtils.isNotBlank(soOutstock.getSalesDeptId())){
				SysDepartmentDTO department = sysUserFeign.getUserDeptById(soOutstock.getSalesDeptId());
				if( null != department){
					soReturnInstockEntity.setSalesDeptId(soOutstock.getSalesDeptId());
					soReturnInstockEntity.setSalesDeptName(department.getName());
				}
			}
			soReturnInstockEntity.setSellerId(soOutstock.getSellerId());
			soReturnInstockEntity.setSellerName(soOutstock.getSellerName());
		}

		if (Objects.nonNull(customerInfo)) {
			soReturnInstockEntity.setSellerId(customerInfo.getSellerId());
			soReturnInstockEntity.setSellerName(customerInfo.getSellerName());
		}

		return soReturnInstockEntity;
	}

	/**
	 * 构建极兔退货入库单明细
	 */
	private List<SoReturnInstockDetailEntity> buildJiTuSoReturnInstockDetail(PlatformReturnInstockDTO dto, SoB2cEntity soB2cEntity, SoInfoEntity soInfoEntity, WarehouseEntity warehouseEntity) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		List<SoB2cDetailEntity> soB2cDetails = new ArrayList<>();
		List<SoDetailEntity> soDetails = new ArrayList<>();
		List<SoB2cReturnDetailEntity> soB2cReturnDetails = new ArrayList<>();
		List<SoReturnDetailEntity> soReturnDetails = new ArrayList<>();

		if(CollectionUtils.isEmpty(details)){
			throw new ServiceException("明细为空");
		}

		// 获取SKU映射
		List<String> platformSkuNoList = dto.getProductDetailList().stream().map(v->v.getProductSku()).collect(Collectors.toList());
		ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
		listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
		listingInfoParamDTO.setAuthId(dto.getAuthId());
		listingInfoParamDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
		List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);

		if (mappingSkuViewDTOList.isEmpty()) {
			throw new ServiceException("没有找到sku映射");
		}

		// 获取订单明细和关联数据
		BigDecimal rate = new BigDecimal("1");
		if (Objects.nonNull(soB2cEntity)) {
			try {
				rate = dmpTaskFeign.getRate(soB2cEntity.getBillDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), soB2cEntity.getCurrency());
			}catch (Exception e){
				throw new ServiceException(soB2cEntity.getCurrency() + "获取汇率失败");
			}
			// B2C订单
			soB2cDetails.addAll(soB2cFeign.listDetailByMainIds(Collections.singletonList(soB2cEntity.getId())));

			// 获取退货订单明细
			List<SoB2cReturnEntity> soB2cReturnEntities = FeignQuery.create(SoB2cReturnEntity.class)
					.eq(SoB2cReturnEntity::getSoId, soB2cEntity.getId())
					.list();
			if (CollectionUtils.isNotEmpty(soB2cReturnEntities)) {
				List<String> returnIds = soB2cReturnEntities.stream().map(SoB2cReturnEntity::getId).collect(Collectors.toList());
				soB2cReturnDetails.addAll(FeignQuery.create(SoB2cReturnDetailEntity.class)
						.in(SoB2cReturnDetailEntity::getMainId, returnIds)
						.list());
			}
		} else if (Objects.nonNull(soInfoEntity)){
			try {
				rate = dmpTaskFeign.getRate(soInfoEntity.getBillDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), soInfoEntity.getCurrency());
			}catch (Exception e){
				throw new ServiceException(soInfoEntity.getCurrency() + "获取汇率失败");
			}
			// B2B订单
			soDetails.addAll(soInfoFeign.listSoDetailByMainId(soInfoEntity.getId()));

			// 获取退货订单明细
			List<SoReturnEntity> soReturnEntities = FeignQuery.create(SoReturnEntity.class)
					.eq(SoReturnEntity::getSourceId, soInfoEntity.getId())
					.list();
			if (CollectionUtils.isNotEmpty(soReturnEntities)) {
				List<String> returnIds = soReturnEntities.stream().map(SoReturnEntity::getId).collect(Collectors.toList());
				soReturnDetails.addAll(FeignQuery.create(SoReturnDetailEntity.class)
						.in(SoReturnDetailEntity::getMainId, returnIds)
						.list());
			}
		}

		List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();

		for (PlatformReturnInstockDTO.Detail detail : details) {
			// 匹配SKU
			SkuMappingDTO.MappingSkuViewDTO skuViewDTO = mappingSkuViewDTOList.stream()
					.filter(v->v.getPlatformSkuNo().equals(detail.getProductSku()))
					.findFirst()
					.orElse(null);

			if(Objects.isNull(skuViewDTO)){
				continue;
			}

			SoReturnInstockDetailEntity soReturnInstockDetailEntity = new SoReturnInstockDetailEntity();
			soReturnInstockDetailEntity.setSkuId(skuViewDTO.getProductSkuId());
			soReturnInstockDetailEntity.setSkuNo(skuViewDTO.getProductSkuNo());
			// 仓库信息
			soReturnInstockDetailEntity.setWarehouseId(warehouseEntity.getId());
			soReturnInstockDetailEntity.setWarehouseName(warehouseEntity.getName());
			// 备注和退货类型
			soReturnInstockDetailEntity.setRemark(dto.getReason());
			soReturnInstockDetailEntity.setReturnTypeDict(dto.getReturnType());
			// 来源明细ID
			soReturnInstockDetailEntity.setSourceDetailId(detail.getThirdId());
			soReturnInstockDetailEntity.setCreateUserId(dto.getAuthId());
			soReturnInstockDetailEntity.setPlatformSkuNo(detail.getProductSku());

			// 计算数量
			int shouldReturnQty = 0; // 应退数量
			int actualQty = detail.getReceiveQty() != null ? detail.getReceiveQty() : 0;

			if (!soB2cDetails.isEmpty()) {
				// B2C订单
				SoB2cDetailEntity soB2cDetailEntity = soB2cDetails.stream()
						.filter(item -> Objects.equals(item.getSkuId(), skuViewDTO.getPlatformSkuNo()))
						.findFirst()
						.orElse(null);
				if (Objects.nonNull(soB2cDetailEntity)) {

					// 应退数量
					shouldReturnQty = soB2cReturnDetails.stream()
							.filter(item -> Objects.equals(item.getSkuId(), skuViewDTO.getProductSkuId()))
							.mapToInt(item -> item.getReturnQty() != null ? item.getReturnQty() : 0)
							.sum();

					// 计算退货金额和含税退货金额
					// 取销售订单的订单金额*订单明细的真实售价占比*（上架数量/销售数量）
					BigDecimal amount = MathUtil.multiplyWithTwo(soB2cDetailEntity.getPrice(), actualQty);
					soReturnInstockDetailEntity.setAmount(amount);
					soReturnInstockDetailEntity.setReturnAmount(amount);
					soReturnInstockDetailEntity.setTaxReturnAmount(amount);
					soReturnInstockDetailEntity.setReturnAmountLocalCurrency(MathUtil.multiplyWithSix(amount, rate, BigDecimal.ROUND_DOWN));
					soReturnInstockDetailEntity.setTaxReturnAmountLocalCurrency(MathUtil.multiplyWithSix(amount, rate, BigDecimal.ROUND_DOWN));
				} else {
					soReturnInstockDetailEntity.setAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setReturnAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setTaxReturnAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setReturnAmountLocalCurrency(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setTaxReturnAmountLocalCurrency(BigDecimal.ZERO);
				}
			} else if (!soDetails.isEmpty()){
				// B2B订单
				SoDetailEntity soDetailEntity = soDetails.stream()
						.filter(item -> Objects.equals(item.getSkuId(), skuViewDTO.getPlatformSkuNo()))
						.findFirst()
						.orElse(null);
				if (Objects.nonNull(soDetailEntity)) {

					// 应退数量
					shouldReturnQty = soReturnDetails.stream()
							.filter(item -> Objects.equals(item.getSkuId(), skuViewDTO.getProductSkuId()))
							.mapToInt(item -> item.getReturnQty() != null ? item.getReturnQty() : 0)
							.sum();

					BigDecimal returnAmount = MathUtil.multiplyWithTwo(soDetailEntity.getPrice(), actualQty);
					soReturnInstockDetailEntity.setReturnAmount(returnAmount);
					// 取销售订单的含税单价*退货数量
					BigDecimal amount = MathUtil.multiplyWithTwo(soDetailEntity.getTaxPrice(), actualQty);
					soReturnInstockDetailEntity.setTaxReturnAmount(amount);
					soReturnInstockDetailEntity.setReturnAmountLocalCurrency(MathUtil.multiplyWithSix(returnAmount, rate, BigDecimal.ROUND_DOWN));
					soReturnInstockDetailEntity.setTaxReturnAmountLocalCurrency(MathUtil.multiplyWithSix(amount, rate, BigDecimal.ROUND_DOWN));
				} else {
					soReturnInstockDetailEntity.setAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setReturnAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setTaxReturnAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setReturnAmountLocalCurrency(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setTaxReturnAmountLocalCurrency(BigDecimal.ZERO);
				}
			} else {
				// 未匹配到订单
				soReturnInstockDetailEntity.setAmount(BigDecimal.ZERO);
				soReturnInstockDetailEntity.setReturnAmount(BigDecimal.ZERO);
				soReturnInstockDetailEntity.setTaxReturnAmount(BigDecimal.ZERO);
				soReturnInstockDetailEntity.setReturnAmountLocalCurrency(BigDecimal.ZERO);
				soReturnInstockDetailEntity.setTaxReturnAmountLocalCurrency(BigDecimal.ZERO);
			}

			soReturnInstockDetailEntity.setMustQty(shouldReturnQty);
			soReturnInstockDetailEntity.setReceiveQty(actualQty);
			soReturnInstockDetailEntity.setRealQty(actualQty);
			soReturnInstockDetailEntity.setDefectiveProductFlag(detail.getDefectiveProductFlag());
			detailEntityList.add(soReturnInstockDetailEntity);
		}

		return detailEntityList;
	}

	/**
	 * WEGO 退货入库：按参考单号从 so_b2c_return 中查找匹配记录。
	 * <p>
	 * 单次 Feign 调用，DB 层 SQL 以 OR 条件匹配 code / platform_return_no / platform_order_no / so_code，
	 * 并按以上优先级排序取首条，避免多次远程调用。
	 */
	private SoB2cReturnEntity findSoB2cReturnByRef(String referenceNo) {
		if (CharSequenceUtil.isBlank(referenceNo)) {
			return null;
		}
		SoB2cReturnEntity result = soB2cReturnFeign.findFirstByReferenceNo(referenceNo);
		if (Objects.nonNull(result)) {
			log.info("[WEGO退货入库] 参考单号 {} 命中 so_b2c_return[{}]", referenceNo, result.getId());
		} else {
			log.info("[WEGO退货入库] 参考单号 {} 在 so_b2c_return 中未查到匹配记录", referenceNo);
		}
		return result;
	}
}