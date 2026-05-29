package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortParamDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.dmp.dto.BiSettlementExchangeRateDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.ListDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.PagingParamDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.TabListDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.mapper.SmallBagCostAllocationMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
/**
 * <p>
 * 小包费用分摊 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-12-02
 */
@Slf4j
@Service
public class SmallBagCostAllocationServiceImpl extends SuperServiceImpl<SmallBagCostAllocationMapper, SmallBagCostAllocationEntity> implements SmallBagCostAllocationService {

    private static final Map<String, Pattern> PAGING_JOIN_ALIAS_PATTERNS;

    static {
        Map<String, Pattern> patterns = new HashMap<>(4);
        for (String alias : Arrays.asList("j", "k", "l", "n")) {
            // j.% 形式：别名 + 点 + 列名，避免子串 contains 误判
            patterns.put(alias, Pattern.compile("(?i)(?:^|[^a-zA-Z0-9_])" + alias + "\\.[a-zA-Z_][a-zA-Z0-9_]*"));
        }
        PAGING_JOIN_ALIAS_PATTERNS = Collections.unmodifiableMap(patterns);
    }

    @Autowired
    private OperateLogService operateLogService;
    
    @Resource
    private LogisticsSupplierService logisticsSupplierService;
    @Resource
    private LogisticsChannelService logisticsChannelService;
    @Resource
    private SmallBagCostAllocationDetailService smallBagCostAllocationDetailService;
    @Resource
    private SmallBagCostAllocationMainService smallBagCostAllocationMainService;
    @Resource
    private LogisticsBillCostService logisticsBillCostService;
    @Resource
    private LogisticsBillService logisticsBillService;
    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;

	@Autowired
	@Qualifier("costAllocationPool")
	private ExecutorService costAllocationPool;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SmallBagCostAllocationDTO.AddDTO addDTO) {
        SmallBagCostAllocationEntity smallBagCostAllocationEntity = new SmallBagCostAllocationEntity();
        BeanMapperUtils.copy(addDTO, smallBagCostAllocationEntity);

        // 数据处理
        handleData(smallBagCostAllocationEntity);

        log.info("开始新增小包费用分摊");
        boolean save = super.save(smallBagCostAllocationEntity);
        if(!save) {
            throw new ServiceException("小包费用分摊保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "小包费用分摊" , smallBagCostAllocationEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, smallBagCostAllocationEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(smallBagCostAllocationEntity.getId(), smallBagCostAllocationEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SmallBagCostAllocationDTO.UpdateDTO updateDTO) {
        SmallBagCostAllocationEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "小包费用分摊"));
        SmallBagCostAllocationEntity smallBagCostAllocationEntity =  BeanMapperUtils.map(SmallBagCostAllocationEntity.class, updateDTO);

        // 数据处理
        handleData(smallBagCostAllocationEntity);
        log.info("编辑 开始修改小包费用分摊数据，id：【{}】", old.getId());
        boolean save = super.updateById(smallBagCostAllocationEntity);
        if(!save) {
            throw new ServiceException("小包费用分摊保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录小包费用分摊日志数据，id：【{}】", smallBagCostAllocationEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), smallBagCostAllocationEntity.getId(), "小包费用分摊");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, smallBagCostAllocationEntity, null, smallBagCostAllocationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SmallBagCostAllocationEntity smallBagCostAllocationEntity) {
    // TODO 验证数据 & 数据赋值
    }

	@Override
	public List<TabListDTO> tabList(PermissionsDTO dto) {
		List<SmallBagCostAllocationDTO.TabListDTO> resultList = new ArrayList<>();
		Map<String, Integer> flagCountMap = this.getBaseMapper().tabList(dto).stream().collect(Collectors.toMap(TabListDTO::getTabFlag, TabListDTO::getCount));
		SmallBagCostAllocationReportStatusEnum[] values = SmallBagCostAllocationReportStatusEnum.values();
        for (SmallBagCostAllocationReportStatusEnum statusEnum : values) {
            LogisticsBillCostDTO.PagingParamDTO pagingParamDTO = new LogisticsBillCostDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            SmallBagCostAllocationDTO.TabListDTO resultDTO = new SmallBagCostAllocationDTO.TabListDTO();
            String code = statusEnum.getCode();
			Integer count = flagCountMap.get(code);
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(code);
            resultDTO.setTabFlagName(statusEnum.getName());
            resultList.add(resultDTO);
        }
        
        return resultList;
	}

	@Override
	public PagingVO<ListDTO> paging(PagingDTO<PagingParamDTO> dto) {
		PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        fillPagingJoinFlags(params);
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<ListDTO> pageData = this.baseMapper.paging(query, params);
        List<ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        // 物流展示字段后置补查，分页 SQL 不再固定 JOIN j/k/l
        enrichLogisticsFields(records);
        handleDataPaging(records);
        return new PagingVO(pageData);
	}

	/**
	 * 根据高级查询、排序、权限 SQL 判断分页是否需要 JOIN 物流/字典表，避免默认关联千万级大表
	 */
	private void fillPagingJoinFlags(PagingParamDTO params) {
		boolean needJoinJ = false;
		boolean needJoinK = false;
		boolean needJoinL = false;
		boolean needJoinN = false;
		if (CollUtil.isNotEmpty(params.getAdvanceQueryDTOList())) {
			for (AdvanceQueryDTO queryDTO : params.getAdvanceQueryDTOList()) {
				if (queryDTO == null || CharSequenceUtil.isBlank(queryDTO.getField())) {
					continue;
				}
				needJoinJ |= containsAlias(queryDTO.getField(), "j");
				needJoinK |= containsAlias(queryDTO.getField(), "k");
				needJoinL |= containsAlias(queryDTO.getField(), "l");
				needJoinN |= containsAlias(queryDTO.getField(), "n");
			}
		}
		if (CollUtil.isNotEmpty(params.getSortList())) {
			for (SortParamDTO sortParam : params.getSortList()) {
				if (sortParam == null || CharSequenceUtil.isBlank(sortParam.getField())) {
					continue;
				}
				needJoinJ |= containsAlias(sortParam.getField(), "j");
				needJoinK |= containsAlias(sortParam.getField(), "k");
				needJoinL |= containsAlias(sortParam.getField(), "l");
				needJoinN |= containsAlias(sortParam.getField(), "n");
			}
		}
		needJoinJ |= containsAlias(params.getPermissionSql(), "j");
		needJoinK |= containsAlias(params.getPermissionSql(), "k");
		needJoinL |= containsAlias(params.getPermissionSql(), "l");
		needJoinN |= containsAlias(params.getPermissionSql(), "n");
		if (params.getSqlMap() != null) {
			for (String sql : params.getSqlMap().values()) {
				needJoinJ |= containsAlias(sql, "j");
				needJoinK |= containsAlias(sql, "k");
				needJoinL |= containsAlias(sql, "l");
				needJoinN |= containsAlias(sql, "n");
			}
		}
		// l -> k -> j 存在外键依赖，子表 JOIN 时必须带上父表
		if (needJoinL) {
			needJoinK = true;
		}
		if (needJoinK) {
			needJoinJ = true;
		}
		params.setNeedJoinJ(needJoinJ);
		params.setNeedJoinK(needJoinK);
		params.setNeedJoinL(needJoinL);
		params.setNeedJoinN(needJoinN);
	}

	private boolean containsAlias(String text, String alias) {
		if (CharSequenceUtil.isBlank(text) || CharSequenceUtil.isBlank(alias)) {
			return false;
		}
		Pattern pattern = PAGING_JOIN_ALIAS_PATTERNS.get(alias.toLowerCase());
		return pattern != null && pattern.matcher(text).find();
	}

	/**
	 * 按当前页 costId 批量补全物流字段，替代 SQL 固定 JOIN j/k/l
	 */
	private void enrichLogisticsFields(List<ListDTO> records) {
		List<String> costIds = records.stream().map(ListDTO::getCostId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
		if (CollUtil.isEmpty(costIds)) {
			return;
		}
		Map<String, LogisticsBillCostEntity> costMap = logisticsBillCostService.listByIds(costIds).stream()
				.collect(Collectors.toMap(LogisticsBillCostEntity::getId, e -> e, (a, b) -> a));
		List<String> billIds = costMap.values().stream().map(LogisticsBillCostEntity::getLogisticsBillId)
				.filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
		List<String> billDetailIds = costMap.values().stream().map(LogisticsBillCostEntity::getLogisticsBillDetailId)
				.filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());

		// bill / detail 查询互不依赖，并行发起减少串行 IO
		CompletableFuture<Map<String, LogisticsBillEntity>> billFuture = CollUtil.isEmpty(billIds)
				? CompletableFuture.completedFuture(Collections.emptyMap())
				: CompletableFuture.supplyAsync(() -> logisticsBillService.listByIds(billIds).stream()
						.collect(Collectors.toMap(LogisticsBillEntity::getId, e -> e, (a, b) -> a)),costAllocationPool);
		CompletableFuture<Map<String, LogisticsBillDetailEntity>> detailByIdFuture = CollUtil.isEmpty(billDetailIds)
				? CompletableFuture.completedFuture(Collections.emptyMap())
				: CompletableFuture.supplyAsync(() -> logisticsBillDetailService.listByIds(billDetailIds).stream()
						.collect(Collectors.toMap(LogisticsBillDetailEntity::getId, e -> e, (a, b) -> a)),costAllocationPool);
		CompletableFuture<Map<String, LogisticsBillDetailEntity>> detailByBillIdFuture = CollUtil.isEmpty(billIds)
				? CompletableFuture.completedFuture(Collections.emptyMap())
				: CompletableFuture.supplyAsync(() -> logisticsBillDetailService.listByMainIds(billIds).stream()
						.collect(Collectors.toMap(LogisticsBillDetailEntity::getMainId, e -> e, (a, b) -> a)),costAllocationPool);

		Map<String, LogisticsBillEntity> billMap = billFuture.join();
		Map<String, LogisticsBillDetailEntity> detailByIdMap = detailByIdFuture.join();
		Map<String, LogisticsBillDetailEntity> detailByBillIdMap = detailByBillIdFuture.join();

		for (ListDTO record : records) {
			LogisticsBillCostEntity cost = costMap.get(record.getCostId());
			if (cost == null) {
				continue;
			}
			record.setReconciliationStatus(cost.getReconciliationStatus());
			record.setChannelId(cost.getChannelId());
			record.setTransportNo(cost.getTransportNo());
			record.setTrackNo(cost.getTrackNo());
			record.setConfirmTime(cost.getConfirmTime());
			record.setPayType(cost.getPayType());
			if (cost.getBillingWeightLogistics() != null) {
				record.setBillingWeightLogistics(cost.getBillingWeightLogistics().toPlainString());
			}
			LogisticsBillEntity bill = billMap.get(cost.getLogisticsBillId());
			if (bill != null) {
				record.setOutstockCode(bill.getOutstockCode());
				record.setDeliveryTime(bill.getDeliveryTime());
				record.setShopName(bill.getShopName());
				record.setToCountry(bill.getToCountry());
				record.setPlatformCode(bill.getPlatformCode());
			}
			LogisticsBillDetailEntity detail = detailByIdMap.get(cost.getLogisticsBillDetailId());
			// detailId 缺失时按 billId 兜底，与原 SQL l.main_id JOIN 逻辑一致
			if (detail == null && CharSequenceUtil.isNotBlank(cost.getLogisticsBillId())) {
				detail = detailByBillIdMap.get(cost.getLogisticsBillId());
			}
			if (detail != null) {
				record.setSignTime(detail.getSignTime());
				record.setTrackStatus(detail.getTrackStatus());
			}
		}
	}

	private void handleDataPaging(List<ListDTO> records) {
		// 去空去重，避免 IN 查询带入 null 或重复 ID
		List<String> skuIds = records.stream().map(ListDTO::getSkuId)
				.filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
		Map<String, String> skuIdNameMap = Collections.emptyMap();
		if (CollUtil.isNotEmpty(skuIds)) {
			List<ProductDetailEntity> productDetailEntityList = FeignQuery.create(ProductDetailEntity.class)
					.in(ProductDetailEntity::getId, skuIds).list();
			skuIdNameMap = productDetailEntityList.stream()
					.collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getName));
		}

		// channelId 由 enrichLogisticsFields 回填，空值不参与 listByIds
		List<String> channelIds = records.stream().map(ListDTO::getChannelId)
				.filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
		Map<String, LogisticsChannelEntity> channelIdMaps = CollUtil.isEmpty(channelIds) ? Collections.emptyMap()
				: logisticsChannelService.listByIds(channelIds).stream()
						.collect(Collectors.toMap(LogisticsChannelEntity::getId, l -> l));
		List<String> supplierIds = channelIdMaps.values().stream().map(LogisticsChannelEntity::getMainId)
				.filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
		Map<String, String> supplierIdNameMap = CollUtil.isEmpty(supplierIds) ? Collections.emptyMap()
				: logisticsSupplierService.listByIds(supplierIds).stream()
						.collect(Collectors.toMap(LogisticsSupplierEntity::getId, LogisticsSupplierEntity::getShortName));

		Map<String, BigDecimal> rateMap = prefetchExchangeRates(records);
		DecimalFormat df2 = new DecimalFormat("0.00");
		DecimalFormat df4 = new DecimalFormat("0.0000");
		DecimalFormat df6 = new DecimalFormat("0.000000");
		for(ListDTO dto : records) {
			LogisticsChannelEntity logisticsChannelEntity = channelIdMaps.get(dto.getChannelId());
			if(logisticsChannelEntity != null) {
				dto.setSupplierName(supplierIdNameMap.get(logisticsChannelEntity.getMainId()) + "-" + logisticsChannelEntity.getName());
			}
			dto.setReportStatusName(SmallBagCostAllocationReportStatusEnum.getName(dto.getReportStatus()));
			String reconciliationStatus = dto.getReconciliationStatus();
			dto.setReconciliationStatusName(ReconciliationStatusEnum.getName(reconciliationStatus));
			dto.setBigTableStatusName(SmallBagCostAllocationBigTableStatusEnum.getName(dto.getBigTableStatus()));
			String skuId = dto.getSkuId();
			dto.setSkuName(skuIdNameMap.get(skuId));
			BigDecimal unitCost = new BigDecimal(dto.getUnitCost());
			Integer deliveryQty = dto.getDeliveryQty();
			String reportDate = dto.getReportDate();
			if(unitCost != null) {
				String unitCurrency = dto.getUnitCurrency();
				if(StringUtils.isNotBlank(unitCurrency) && !"CNY".equals(unitCurrency)) {
					// 与 prefetchExchangeRates / listRate 共用 rateKey：reportDate_currency
					String key = reportDate + "_" + unitCurrency;
					BigDecimal rate = rateMap.get(key);
					if (ObjectUtil.isEmpty(rate)) {
						log.error("币别【{}】,汇率为空，请维护汇率后再查询", unitCurrency);
						throw new ServiceException("汇率为空，请维护汇率后再查询");
					}
					unitCost = unitCost.multiply(rate).setScale(6);
				}
				dto.setUnitCost(df6.format(unitCost));
				dto.setTotalCost(df6.format(unitCost.multiply(new BigDecimal(deliveryQty)).setScale(6)));
			}
			
			dto.setFeeSource(SmallBagCostAllocationMainFeeSourceEnum.getName(dto.getFeeSource()));
			if(LogisticTrackStatusEnum.SIGN.getCode().equals(dto.getTrackStatus())
					|| LogisticTrackStatusEnum.MANUAL_COMPLETE.getCode().equals(dto.getTrackStatus())
					|| LogisticTrackStatusEnum.SYSTEM_COMPLETE.getCode().equals(dto.getTrackStatus())) {
				dto.setDeliveryStatusName("已签收");
			}else {
				dto.setDeliveryStatusName("未签收");
			}
			dto.setFeeTypeName(AllocationFeeTypeEnum.getName(dto.getFeeType()));
			dto.setFeeAllocationTypeName(CostAllocationEnum.getName(dto.getFeeAllocationType()));
			dto.setWeightAllocationTypeName(WeightAllocationSmallBagEnum.getName(dto.getWeightAllocationType()));
			dto.setCurrencySymbol("¥");
			
			
			BigDecimal refund = BigDecimal.ONE;
			if("refund".equals(dto.getPayType())) {
				refund = new BigDecimal("-1");
			}
			String billAmount = dto.getBillAmount();
			if(billAmount != null) {
				dto.setBillAmount(df4.format(new BigDecimal(billAmount).multiply(refund).setScale(4, RoundingMode.HALF_UP)));
			}
			String allocatedAmount = dto.getAllocatedAmount();
			if(allocatedAmount != null) {
				dto.setAllocatedAmount(df2.format(new BigDecimal(allocatedAmount).multiply(refund).setScale(2, RoundingMode.HALF_UP)));
			}
			String productAllocatedAmount = dto.getProductAllocatedAmount();
			if(productAllocatedAmount != null) {
				dto.setProductAllocatedAmount(df6.format(new BigDecimal(productAllocatedAmount).multiply(refund).setScale(6, RoundingMode.HALF_UP)));
			}
		}
	}

	/**
	 * 批量预取汇率；构造的 rateKey 须符合 {@link BiSettlementExchangeRateDTO.ListRateParamDTO} 协议。
	 *
	 * @param records 分页记录，reportDate 与 unitCurrency 拼为 {@code reportDate_currency}（如 {@code 2024-01_USD}）
	 * @return key 与 DMP listRate 入参/出参 rateKey 一致
	 */
	private Map<String, BigDecimal> prefetchExchangeRates(List<ListDTO> records) {
		Set<String> rateKeys = records.stream()
				.filter(dto -> StringUtils.isNotBlank(dto.getUnitCurrency()) && !"CNY".equals(dto.getUnitCurrency()))
				.filter(dto -> StringUtils.isNotBlank(dto.getReportDate()))
				// rateKey = reportDate + "_" + unitCurrency，reportDate 勿含 '_'，币种勿含 '_'
				.map(dto -> dto.getReportDate() + "_" + dto.getUnitCurrency())
				.collect(Collectors.toSet());
		if (CollUtil.isEmpty(rateKeys)) {
			return Collections.emptyMap();
		}
		// 构建汇率查询参数并调用远程服务批量获取汇率
		BiSettlementExchangeRateDTO.ListRateParamDTO listRateParamDTO = new BiSettlementExchangeRateDTO.ListRateParamDTO();
		listRateParamDTO.setRateKeys(rateKeys);
		Map<String, BigDecimal> rateMap = dmpTaskFeign.listRate(listRateParamDTO);
		if (rateMap == null) {
			rateMap = Collections.emptyMap();
		}
		// 校验所有请求的汇率数据是否完整，存在缺失则记录错误并抛出异常
		for (String key : rateKeys) {
			BigDecimal rate = rateMap.get(key);
			if (ObjectUtil.isEmpty(rate)) {
				int sep = key.indexOf('_');
				String unitCurrency = key.substring(sep + 1);
				log.error("币别【{}】,汇率为空，请维护汇率后再查询", unitCurrency);
				throw new ServiceException("汇率为空，请维护汇率后再查询");
			}
		}
		return rateMap;
	}
	
	@Override
	public BatchResultDTO updateReportStatus(String id, String reportDate, String reportStatus) {
		SmallBagCostAllocationMainEntity smallBagCostAllocationMainEntity = smallBagCostAllocationMainService.getById(id);
		if(StringUtils.isBlank(reportDate) && StringUtils.isBlank(reportStatus)) {
			throw new ServiceException("会计期间和核算状态不能同时为空");
		}
		if(StringUtils.isNotBlank(reportStatus)) {
			if(reportStatus.equals(smallBagCostAllocationMainEntity.getReportStatus())) {
				throw new ServiceException("更新前后核算状态一致");
			}
			if(reportStatus.equals(SmallBagCostAllocationReportStatusEnum.TOBECONFIRM.getCode()) 
					&& SmallBagCostAllocationBigTableStatusEnum.DONE.getCode().equals(smallBagCostAllocationMainEntity.getBigTableStatus())) {
				throw new ServiceException("物流大表已生成，无法从已确认更新为待确认");
			}
		}
		smallBagCostAllocationMainService.lambdaUpdate().eq(SmallBagCostAllocationMainEntity::getId, id)
			.set(StringUtils.isNotBlank(reportDate) , SmallBagCostAllocationMainEntity::getAccountDate, reportDate)
			.set(StringUtils.isNotBlank(reportStatus) , SmallBagCostAllocationMainEntity::getReportStatus, reportStatus)
			.update();
		logisticsBillCostService.lambdaUpdate()
			.eq(LogisticsBillCostEntity::getId, smallBagCostAllocationMainEntity.getCostId())
			.set(LogisticsBillCostEntity::getCheckStatus, reportStatus.equals(SmallBagCostAllocationReportStatusEnum.TOBECONFIRM.getCode()) 
					? LogisticsBillCostCheckStatusEnum.CHECKED.getCode() : LogisticsBillCostCheckStatusEnum.CONFIRM.getCode())
			.update();
		return BatchResultDTO.success(id, id, "更新核算状态成功");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResultDTO reAllocation(String id) {
		SmallBagCostAllocationMainEntity smallBagCostAllocationMainEntity = smallBagCostAllocationMainService.getById(id);
		String costId = smallBagCostAllocationMainEntity.getCostId();
		if(SmallBagCostAllocationReportStatusEnum.CONFIRMED.getCode().equals(smallBagCostAllocationMainEntity.getReportStatus())) {
			throw new ServiceException("所选分摊费用核算状态必须为【待确认】才可重新分摊");
		}
		smallBagCostAllocationMainService.removeById(id);
		List<String> ids = lambdaQuery().eq(SmallBagCostAllocationEntity::getMainId, id).list().stream().map(SmallBagCostAllocationEntity::getId).collect(Collectors.toList());
		removeByIds(ids);
		smallBagCostAllocationDetailService.lambdaUpdate()
			.in(SmallBagCostAllocationDetailEntity::getMainId, ids)
			.set(SmallBagCostAllocationDetailEntity::getIsDeleted, true)
			.update();
		logisticsBillCostService.pushAllocation(costId, smallBagCostAllocationMainEntity.getReportDate());
		
		smallBagCostAllocationMainService.lambdaUpdate().eq(SmallBagCostAllocationMainEntity::getCostId, costId)
			.set(SmallBagCostAllocationMainEntity::getCreateTime, smallBagCostAllocationMainEntity.getCreateTime())
			.set(SmallBagCostAllocationMainEntity::getCreateUserId, smallBagCostAllocationMainEntity.getCreateUserId())
			.set(SmallBagCostAllocationMainEntity::getCreateUserName, smallBagCostAllocationMainEntity.getCreateUserName())
			.update();
		
		return BatchResultDTO.success(id, id, "重新分摊成功");
	}

	@Override
	public BatchResultDTO pushBigTable(String id) {
		return null;
	}

    @Override
    public List<SmallBagCostAllocationEntity> listByReportPeriodStr(String reportPeriodStr, String reportStatus) {
		if (CharSequenceUtil.isBlank(reportPeriodStr)) {
			return Collections.emptyList();
		}
        return baseMapper.listByReportPeriodStr(reportPeriodStr,reportStatus);
    }

	@Override
	public List<SmallBagCostAllocationDTO.SmallBagCostDTO> listSmallBagCost(SmallBagCostAllocationDTO.SmallBagCostParamDTO paramDTO) {
		return baseMapper.listSmallBagCost(paramDTO);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResultDTO delete(String id) {
		SmallBagCostAllocationMainEntity smallBagCostAllocationMainEntity = smallBagCostAllocationMainService.getById(id);
		String costId = smallBagCostAllocationMainEntity.getCostId();
		if(SmallBagCostAllocationReportStatusEnum.CONFIRMED.getCode().equals(smallBagCostAllocationMainEntity.getReportStatus())) {
			throw new ServiceException("所选分摊费用核算状态必须为【待确认】才可删除");
		}
		smallBagCostAllocationMainService.removeById(id);
		List<String> ids = lambdaQuery().eq(SmallBagCostAllocationEntity::getMainId, id).list().stream().map(SmallBagCostAllocationEntity::getId).collect(Collectors.toList());
		removeByIds(ids);
		smallBagCostAllocationDetailService.lambdaUpdate()
			.in(SmallBagCostAllocationDetailEntity::getMainId, ids)
			.set(SmallBagCostAllocationDetailEntity::getIsDeleted, true)
			.update();
		logisticsBillCostService.lambdaUpdate().eq(LogisticsBillCostEntity::getId, costId).set(LogisticsBillCostEntity::getCheckStatus , LogisticsBillCostCheckStatusEnum.CHECKING.getCode()).update();
		return BatchResultDTO.success(id, id, "删除成功");
	}

	@Override
	public Boolean exportExcel(PagingParamDTO dto) {
		downloadTaskFeign.saveDownloadTask("小包费用分摊列表", FileTaskEventEnum.EXPORT_SMALL_BAG_COST_ALLOCATION.getCode(), dto);
        return Boolean.TRUE;
	}
}
