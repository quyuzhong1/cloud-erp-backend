package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.dto.BiSettlementExchangeRateDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.ListDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.PagingParamDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.TabListDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.handler.asynctask.SmallBagDeleteBatchPushHandler;
import com.erp.server.tms.handler.asynctask.SmallBagReAllocationBatchPushHandler;
import com.erp.server.tms.handler.asynctask.SmallBagUpdateReportStatusBatchPushHandler;
import com.erp.server.tms.mapper.SmallBagCostAllocationMapper;
import com.erp.server.tms.service.*;
import com.erp.server.tms.service.asynctask.SmallBagCostAllocationAsyncTaskDelegate;
import com.erp.server.tms.service.support.TmsAsyncTaskBatchConsumerSupport;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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
public class SmallBagCostAllocationServiceImpl extends SuperServiceImpl<SmallBagCostAllocationMapper, SmallBagCostAllocationEntity>
    implements SmallBagCostAllocationService, SmallBagCostAllocationAsyncTaskDelegate {

    @Resource
    private SmallBagUpdateReportStatusBatchPushHandler smallBagUpdateReportStatusBatchPushHandler;
    @Resource
    private SmallBagReAllocationBatchPushHandler smallBagReAllocationBatchPushHandler;
    @Resource
    private SmallBagDeleteBatchPushHandler smallBagDeleteBatchPushHandler;

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
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;
    @Resource
    private TmsAsyncTaskDetailService asyncTaskDetailRecordService;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private TmsAsyncTaskBatchConsumerSupport tmsAsyncTaskBatchConsumerSupport;
    @Lazy
    @Resource
    private SmallBagCostAllocationService self;

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
		long pagingStart = System.nanoTime();
		PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        long countStart = System.nanoTime();
        long totalCount = this.baseMapper.pagingCount(params);
        long countCostMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - countStart);
        log.info("小包费用分摊分页总数查询耗时:{}ms, currPage={}, pageSize={}, totalCount={}",
                countCostMs, dto.getCurrPage(), dto.getPageSize(), totalCount);
        if (totalCount == 0L) {
            log.info("小包费用分摊分页总耗时:{}ms, result=empty", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - pagingStart));
            return new PagingVO<>(Collections.emptyList(), 0, dto.getPageSize(), dto.getCurrPage());
        }

        long offset = ((long) dto.getCurrPage() - 1L) * dto.getPageSize();
        long idQueryStart = System.nanoTime();
        List<String> detailIds = this.baseMapper.pagingDetailIds(params, offset, dto.getPageSize());
        long idQueryCostMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - idQueryStart);
        log.info("小包费用分摊分页主键查询耗时:{}ms, offset={}, pageSize={}, idCount={}",
                idQueryCostMs, offset, dto.getPageSize(), detailIds.size());
        if (CollectionUtils.isEmpty(detailIds)) {
            log.info("小包费用分摊分页总耗时:{}ms, result=emptyIds", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - pagingStart));
            return new PagingVO<>(Collections.emptyList(), (int) totalCount, dto.getPageSize(), dto.getCurrPage());
        }

        long detailQueryStart = System.nanoTime();
        List<ListDTO> loaded = this.baseMapper.selectByDetailIds(detailIds);
        long detailQueryCostMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - detailQueryStart);
        log.info("小包费用分摊分页回表查询耗时:{}ms, detailIdCount={}, loadedCount={}",
                detailQueryCostMs, detailIds.size(), loaded.size());
        Map<String, ListDTO> byDetailId = loaded.stream()
                .collect(Collectors.toMap(ListDTO::getDetailId, Function.identity(), (first, ignored) -> first));

        List<ListDTO> records = detailIds.stream()
                .map(byDetailId::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(records)) {
            log.info("小包费用分摊分页总耗时:{}ms, result=emptyRecords", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - pagingStart));
            return new PagingVO<>(records, (int) totalCount, dto.getPageSize(), dto.getCurrPage());
        }

        long formatStart = System.nanoTime();
        handleDataPaging(records);
        long formatCostMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - formatStart);
        log.info("小包费用分摊分页数据处理耗时:{}ms, recordCount={}", formatCostMs, records.size());
        log.info("小包费用分摊分页总耗时:{}ms, currPage={}, pageSize={}, totalCount={}, recordCount={}",
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - pagingStart), dto.getCurrPage(), dto.getPageSize(),
                totalCount, records.size());
        return new PagingVO<>(records, (int) totalCount, dto.getPageSize(), dto.getCurrPage());
	}

	/**
	 * 批量加载当前分页记录需要的汇率。
	 *
	 * @param records 小包费用分摊分页记录
	 * @return 按查询日期和源币别拼接键缓存的汇率
	 */
	protected Map<String, BigDecimal> loadBatchRates(List<ListDTO> records) {
		List<BiSettlementExchangeRateDTO.BatchRateParamDTO> rateParams = records.stream()
				.filter(item -> StringUtils.isNotBlank(item.getUnitCurrency()))
				.filter(item -> !"CNY".equals(item.getUnitCurrency()))
				.map(item -> new BiSettlementExchangeRateDTO.BatchRateParamDTO(item.getReportDate() + "-01", item.getUnitCurrency()))
				.collect(Collectors.collectingAndThen(
						Collectors.toMap(item -> item.getDate() + "_" + item.getSourceCurrencyCode(),
								Function.identity(), (first, ignored) -> first, LinkedHashMap::new),
						map -> new ArrayList<>(map.values())));
		Map<String, BigDecimal> rateMap = Collections.emptyMap();
		if (CollectionUtils.isNotEmpty(rateParams)) {
			rateMap = new HashMap<>();
			List<BiSettlementExchangeRateDTO.BatchRateResultDTO> rateResults = dmpTaskFeign.getRates(rateParams);
			if (CollectionUtils.isNotEmpty(rateResults)) {
				for (BiSettlementExchangeRateDTO.BatchRateResultDTO rateResult : rateResults) {
					rateMap.put(rateResult.getDate() + "_" + rateResult.getSourceCurrencyCode(), rateResult.getExchangeRate());
				}
			}
		}
		return rateMap;
	}

	/**
	 * 从批量汇率结果中解析单条分页记录使用的汇率。
	 *
	 * @param reportDate 报告月份
	 * @param unitCurrency 源币别
	 * @param rateMap 已加载的批量汇率
	 * @return 解析后的汇率
	 */
	protected BigDecimal resolvePageRate(String reportDate, String unitCurrency, Map<String, BigDecimal> rateMap) {
		String key = reportDate + "-01_" + unitCurrency;
		BigDecimal rate = rateMap.get(key);
		if (rate == null) {
			log.error("币别 {} 未查询到有效汇率", unitCurrency);
			throw new ServiceException("汇率为空，请维护汇率后再查询");
		}
	return rate;
	}

	/**
	 * 按汇率换算单价并保留 6 位小数。
	 *
	 * @param unitCost 原始单价
	 * @param rate 汇率
	 * @return 乘法后的精确结果
	 */
	protected BigDecimal multiplyUnitCost(BigDecimal unitCost, BigDecimal rate) {
		if (unitCost == null || rate == null) {
			return unitCost;
		}
		return unitCost.multiply(rate).setScale(6, RoundingMode.HALF_UP);
	}

	/**
	 * 格式化分页记录的展示字段。
	 *
	 * @param records 小包费用分摊分页记录
	 */
	protected void handleDataPaging(List<ListDTO> records) {
		List<String> skuIds = records.stream().map(ListDTO::getSkuId).collect(Collectors.toList());
		List<ProductDetailEntity> productDetailEntityList = FeignQuery.create(ProductDetailEntity.class).in(ProductDetailEntity::getId, 
				skuIds).list();
		Map<String, String> skuIdNameMap = productDetailEntityList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getName));
		
		Map<String, LogisticsChannelEntity> channelIdMaps = logisticsChannelService.listByIds(records.stream().map(ListDTO::getChannelId).collect(Collectors.toList())).stream().collect(Collectors.toMap(LogisticsChannelEntity::getId, l -> l));
		List<String> supplierIds = channelIdMaps.values().stream().map(LogisticsChannelEntity::getMainId).collect(Collectors.toList());
		Map<String, String> supplierIdNameMap = new HashMap<>();
		if(CollUtil.isNotEmpty(supplierIds)) {
			supplierIdNameMap = logisticsSupplierService.listByIds(supplierIds).stream().collect(Collectors.toMap(LogisticsSupplierEntity::getId, LogisticsSupplierEntity::getShortName));
		}
		
		Map<String, BigDecimal> rateMap = loadBatchRates(records);
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
					BigDecimal rate = resolvePageRate(reportDate, unitCurrency, rateMap);
					unitCost = multiplyUnitCost(unitCost, rate);
				}
				dto.setUnitCost(df6.format(unitCost));
				dto.setTotalCost(df6.format(unitCost.multiply(new BigDecimal(deliveryQty)).setScale(6, RoundingMode.HALF_UP)));
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
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResultDTO updateReportStatus(String id, String reportDate, String reportStatus) {
		return updateReportStatus(smallBagCostAllocationMainService.getById(id), reportDate, reportStatus);
	}

	private BatchResultDTO updateReportStatus(SmallBagCostAllocationMainEntity smallBagCostAllocationMainEntity, String reportDate, String reportStatus) {
		// 兜底：游标查询到的主表行在处理前可能已被删除
		if (ObjectUtil.isEmpty(smallBagCostAllocationMainEntity)) {
			throw new ServiceException("小包分摊不存在, 核算状态");
		}
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
		String id = smallBagCostAllocationMainEntity.getId();
		smallBagCostAllocationMainService.lambdaUpdate().eq(SmallBagCostAllocationMainEntity::getId, id)
			.set(StringUtils.isNotBlank(reportDate) , SmallBagCostAllocationMainEntity::getAccountDate, reportDate)
			.set(StringUtils.isNotBlank(reportStatus) , SmallBagCostAllocationMainEntity::getReportStatus, reportStatus)
			.update();
		// 仅在变更核算状态时联动更新物流费用对账状态，避免 reportStatus 为空时 NPE
		if (StringUtils.isNotBlank(reportStatus)) {
			logisticsBillCostService.lambdaUpdate()
				.eq(LogisticsBillCostEntity::getId, smallBagCostAllocationMainEntity.getCostId())
				.set(LogisticsBillCostEntity::getCheckStatus, reportStatus.equals(SmallBagCostAllocationReportStatusEnum.TOBECONFIRM.getCode())
						? LogisticsBillCostCheckStatusEnum.CHECKED.getCode() : LogisticsBillCostCheckStatusEnum.CONFIRM.getCode())
				.update();
		}
		return BatchResultDTO.success(id, id, "更新核算状态成功");
	}

	@Override
	public BatchResultDTO asyncUpdateReportStatus(SmallBagCostAllocationDTO.UpdateStatusDTO dto) {
		if (CharSequenceUtil.isBlank(dto.getReportPeriodStr())) {
			throw new ServiceException("核算期间不能为空");
		}
		if (StringUtils.isBlank(dto.getReportDate()) && StringUtils.isBlank(dto.getReportStatus())) {
			throw new ServiceException("会计期间和核算状态不能同时为空");
		}
		String reportStatus = dto.getReportStatus();
		boolean excludeBigTableDone = SmallBagCostAllocationReportStatusEnum.TOBECONFIRM.getCode().equals(reportStatus);
		String bigTableDoneCode = SmallBagCostAllocationBigTableStatusEnum.DONE.getCode();

		int total = smallBagCostAllocationMainService.countMainByReportPeriodStr(dto.getReportPeriodStr(), reportStatus, excludeBigTableDone, bigTableDoneCode);
		if (total == 0) {
			throw new ServiceException("没有可更新核算状态的数据");
		}

		String businessType = SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode();
		String methodType = TmsAsyncTaskMethodTypeEnum.UPDATE_REPORT_STATUS.getCode();
		TmsAsyncTaskRecordDTO.SmallBagUpdateReportStatusPayloadDTO payload =
			new TmsAsyncTaskRecordDTO.SmallBagUpdateReportStatusPayloadDTO(
				dto.getReportPeriodStr(), reportStatus, dto.getReportDate());
		TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope =
			asyncTaskRecordService.buildEnvelope(businessType, methodType, null, null, payload);
		return asyncTaskRecordService.dispatchManualEnvelopeTask(
			businessType, methodType, total, envelope,
			"小包核算状态变更异步任务派发成功，taskId: {}, 预计处理数据量: {}");
	}

	/**
	 * 消费已迁移的小包核算状态变更异步任务。
	 */
	@Override
	public void pushUpdateReportStatus(TmsAsyncTaskRecordEntity taskRecord) {
		tmsAsyncTaskBatchConsumerSupport.execute(taskRecord, smallBagUpdateReportStatusBatchPushHandler);
	}

	@Override
	public BatchResultDTO asyncReAllocation(SmallBagCostAllocationDTO.ResetIdsDTO dto) {
		if (CharSequenceUtil.isBlank(dto.getReportPeriodStr())) {
			throw new ServiceException("核算期间不能为空");
		}
		String reportStatus = SmallBagCostAllocationMainReportStatusEnum.TOBECONFIRM.getCode();
		int total = smallBagCostAllocationMainService.countMainForReAllocation(dto.getReportPeriodStr(), reportStatus);
		if (total == 0) {
			throw new ServiceException("没有可重新分摊的数据");
		}

		String businessType = SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode();
		String methodType = TmsAsyncTaskMethodTypeEnum.RE_ALLOCATION.getCode();
		TmsAsyncTaskRecordDTO.SmallBagReportPeriodBatchPayloadDTO payload =
			new TmsAsyncTaskRecordDTO.SmallBagReportPeriodBatchPayloadDTO(dto.getReportPeriodStr(), reportStatus);
		TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope =
			asyncTaskRecordService.buildEnvelope(businessType, methodType, null, null, payload);
		return asyncTaskRecordService.dispatchManualEnvelopeTask(
			businessType, methodType, total, envelope,
			"小包重新分摊异步任务派发成功，taskId: {}, 预计处理数据量: {}");
	}

	/**
	 * 消费已迁移的小包重新分摊异步任务。
	 */
	@Override
	public void pushReAllocation(TmsAsyncTaskRecordEntity taskRecord) {
		tmsAsyncTaskBatchConsumerSupport.execute(taskRecord, smallBagReAllocationBatchPushHandler);
	}

	@Override
	public BatchResultDTO asyncDelete(SmallBagCostAllocationDTO.ResetIdsDTO dto) {
		if (CharSequenceUtil.isBlank(dto.getReportPeriodStr())) {
			throw new ServiceException("核算期间不能为空");
		}
		String reportStatus = SmallBagCostAllocationMainReportStatusEnum.TOBECONFIRM.getCode();
		int total = smallBagCostAllocationMainService.countMainForReAllocation(dto.getReportPeriodStr(), reportStatus);
		if (total == 0) {
			throw new ServiceException("没有可删除的数据");
		}

		String businessType = SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode();
		String methodType = TmsAsyncTaskMethodTypeEnum.DELETE.getCode();
		TmsAsyncTaskRecordDTO.SmallBagReportPeriodBatchPayloadDTO payload =
			new TmsAsyncTaskRecordDTO.SmallBagReportPeriodBatchPayloadDTO(dto.getReportPeriodStr(), reportStatus);
		TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope =
			asyncTaskRecordService.buildEnvelope(businessType, methodType, null, null, payload);
		return asyncTaskRecordService.dispatchManualEnvelopeTask(
			businessType, methodType, total, envelope,
			"小包批量删除异步任务派发成功，taskId: {}, 预计处理数据量: {}");
	}

	/**
	 * 消费已迁移的小包批量删除异步任务。
	 */
	@Override
	public void pushDelete(TmsAsyncTaskRecordEntity taskRecord) {
		tmsAsyncTaskBatchConsumerSupport.execute(taskRecord, smallBagDeleteBatchPushHandler);
	}

	/**
	 * 小包核算状态变更批次：为主表记录补齐任务明细并回写单条执行结果。
	 */
	@Override
	public TmsAsyncTaskRecordDTO.BatchProcessResult processUpdateStatusBatch(String taskId,
																					 List<String> batchIds,
																					 String reportDate,
																					 String reportStatus,
																					 int timeoutSeconds,
																					 int staleDetailSeconds,
																					 LoginUser operatorUser) {
		Map<String, SmallBagCostAllocationMainEntity> entityMap = smallBagCostAllocationMainService.listByIds(batchIds).stream()
			.collect(Collectors.toMap(SmallBagCostAllocationMainEntity::getId, Function.identity(), (a, b) -> a));
		List<TmsAsyncTaskDetailEntity> detailsToExecute = prepareSmallBagTaskDetails(taskId, batchIds, entityMap);
		return executeSmallBagBatchWithConcurrency(
			taskId, staleDetailSeconds, detailsToExecute, entityMap, timeoutSeconds, operatorUser,
			(taskDetailId, businessId, entity) -> self.updateReportStatus(businessId, reportDate, reportStatus));
	}

	/**
	 * 小包重新分摊批次：按任务明细维度执行，保证失败单据可单独重试。
	 */
	@Override
	public TmsAsyncTaskRecordDTO.BatchProcessResult processReAllocationBatch(String taskId,
																					 List<String> batchIds,
																					 int timeoutSeconds,
																					 int staleDetailSeconds,
																					 LoginUser operatorUser) {
		Map<String, SmallBagCostAllocationMainEntity> entityMap = smallBagCostAllocationMainService.listByIds(batchIds).stream()
			.collect(Collectors.toMap(SmallBagCostAllocationMainEntity::getId, Function.identity(), (a, b) -> a));
		List<TmsAsyncTaskDetailEntity> detailsToExecute = prepareSmallBagTaskDetails(taskId, batchIds, entityMap);
		return executeSmallBagBatchWithConcurrency(
			taskId, staleDetailSeconds, detailsToExecute, entityMap, timeoutSeconds, operatorUser,
			(taskDetailId, businessId, entity) -> self.reAllocation(entity));
	}

	/**
	 * 小包批量删除批次：将删除成功或失败逐条写入异步任务明细。
	 */
	@Override
	public TmsAsyncTaskRecordDTO.BatchProcessResult processDeleteBatch(String taskId,
																			   List<String> batchIds,
																			   int timeoutSeconds,
																			   int staleDetailSeconds,
																			   LoginUser operatorUser) {
		Map<String, SmallBagCostAllocationMainEntity> entityMap = smallBagCostAllocationMainService.listByIds(batchIds).stream()
			.collect(Collectors.toMap(SmallBagCostAllocationMainEntity::getId, Function.identity(), (a, b) -> a));
		List<TmsAsyncTaskDetailEntity> detailsToExecute = prepareSmallBagTaskDetails(taskId, batchIds, entityMap);
		return executeSmallBagBatchWithConcurrency(
			taskId, staleDetailSeconds, detailsToExecute, entityMap, timeoutSeconds, operatorUser,
			(taskDetailId, businessId, entity) -> self.delete(businessId));
	}

	/**
	 * 创建本批次缺失的任务明细；已有明细不重复创建，防止 MQ 重投重复删除或重复分摊。
	 */
	private List<TmsAsyncTaskDetailEntity> prepareSmallBagTaskDetails(String taskId,
																	  List<String> batchIds,
																	  Map<String, SmallBagCostAllocationMainEntity> entityMap) {
		List<String> distinctBatchIds = batchIds.stream()
			.filter(StringUtils::isNotBlank)
			.distinct()
			.collect(Collectors.toList());
		if (CollUtil.isEmpty(distinctBatchIds)) {
			return Collections.emptyList();
		}

		List<TmsAsyncTaskDetailEntity> existingDetails = asyncTaskDetailRecordService.lambdaQuery()
			.eq(TmsAsyncTaskDetailEntity::getMainId, taskId)
			.in(TmsAsyncTaskDetailEntity::getBusinessId, distinctBatchIds)
			.list();
		Set<String> existingIngBusinessIds = existingDetails.stream()
			.filter(d -> Objects.equals(d.getStatus(), TmsAsyncTaskRecordStatusEnum.ING.getCode()))
			.map(TmsAsyncTaskDetailEntity::getBusinessId)
			.filter(StringUtils::isNotBlank)
			.collect(Collectors.toSet());
		Map<String, TmsAsyncTaskDetailEntity> existingPendingByBusinessId = existingDetails.stream()
			.filter(d -> Objects.equals(d.getStatus(), TmsAsyncTaskRecordStatusEnum.PENDING.getCode()))
			.filter(d -> StringUtils.isNotBlank(d.getBusinessId()))
			.collect(Collectors.toMap(TmsAsyncTaskDetailEntity::getBusinessId, Function.identity(), (o1, o2) -> o1));
		Set<String> existingBusinessIds = existingDetails.stream()
			.map(TmsAsyncTaskDetailEntity::getBusinessId)
			.filter(StringUtils::isNotBlank)
			.collect(Collectors.toSet());

		List<TmsAsyncTaskDetailEntity> detailsToExecute = new ArrayList<>(existingPendingByBusinessId.values());
		LocalDateTime now = LocalDateTime.now();
		List<TmsAsyncTaskDetailEntity> details = new ArrayList<>();
		for (String businessId : distinctBatchIds) {
			if (existingIngBusinessIds.contains(businessId) || existingBusinessIds.contains(businessId)) {
				continue;
			}
			SmallBagCostAllocationMainEntity entity = entityMap.get(businessId);
			TmsAsyncTaskDetailEntity detail = new TmsAsyncTaskDetailEntity();
			detail.setMainId(taskId);
			detail.setBusinessType(SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode());
			detail.setBusinessId(businessId);
			if (Objects.nonNull(entity)) {
				detail.setBusinessCode(entity.getCostId());
			}
			detail.setStatus(TmsAsyncTaskRecordStatusEnum.PENDING.getCode());
			detail.setCreateTime(now);
			details.add(detail);
		}
		if (CollUtil.isNotEmpty(details)) {
			asyncTaskDetailRecordService.saveBatch(details);
			detailsToExecute.addAll(details);
		}
		return detailsToExecute;
	}

	/**
	 * 并发执行小包任务明细；当 {@code taskId} 与 {@code staleDetailSeconds} 有效时，先标记僵死 ING 明细为 FAILED。
	 */
	private TmsAsyncTaskRecordDTO.BatchProcessResult executeSmallBagBatchWithConcurrency(String taskId,
																						int staleDetailSeconds,
																						List<TmsAsyncTaskDetailEntity> batchDetails,
																						Map<String, SmallBagCostAllocationMainEntity> entityMap,
																						int timeoutSeconds,
																						LoginUser operatorUser,
																						SmallBagTaskExecutor executor) {
		if (CollUtil.isEmpty(batchDetails)) {
			return new TmsAsyncTaskRecordDTO.BatchProcessResult(0, 0);
		}

		int staleFailedCount = 0;
		if (StringUtils.isNotBlank(taskId) && staleDetailSeconds > 0) {
			List<String> businessIds = batchDetails.stream()
				.map(TmsAsyncTaskDetailEntity::getBusinessId)
				.filter(StringUtils::isNotBlank)
				.distinct()
				.collect(Collectors.toList());
			if (CollUtil.isNotEmpty(businessIds)) {
				LocalDateTime staleBefore = LocalDateTime.now().minusSeconds(staleDetailSeconds);
				List<TmsAsyncTaskDetailEntity> existingDetails = asyncTaskDetailRecordService.lambdaQuery()
					.eq(TmsAsyncTaskDetailEntity::getMainId, taskId)
					.in(TmsAsyncTaskDetailEntity::getBusinessId, businessIds)
					.list();
				Set<String> staleIngBusinessIds = existingDetails.stream()
					.filter(d -> Objects.equals(d.getStatus(), TmsAsyncTaskRecordStatusEnum.ING.getCode()))
					.filter(d -> asyncTaskRecordService.isStaleIngDetail(d, staleBefore))
					.map(TmsAsyncTaskDetailEntity::getBusinessId)
					.filter(StringUtils::isNotBlank)
					.collect(Collectors.toSet());
				staleFailedCount = asyncTaskDetailRecordService.markStaleIngDetailsFailed(
					taskId, staleIngBusinessIds, staleBefore, ApiError.ASYNC_TASK_DETAIL_TIMEOUT.getMsg());
				if (staleFailedCount > 0) {
					log.warn("小包费用分摊僵死ING明细已标记失败，taskId: {}, 数量: {}", taskId, staleFailedCount);
				}
			}
		}

		Map<String, TmsAsyncTaskDetailEntity> executableDetailMap = new LinkedHashMap<>();
		List<TmsAsyncTaskDetailEntity> duplicateDetails = new ArrayList<>();
		for (TmsAsyncTaskDetailEntity detail : batchDetails) {
			String businessId = detail.getBusinessId();
			if (StringUtils.isBlank(businessId)) {
				executableDetailMap.put(detail.getId(), detail);
				continue;
			}
			TmsAsyncTaskDetailEntity oldDetail = executableDetailMap.putIfAbsent(businessId, detail);
			if (Objects.nonNull(oldDetail)) {
				duplicateDetails.add(detail);
			}
		}

		CountDownLatch latch = new CountDownLatch(executableDetailMap.size());
		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failedCount = new AtomicInteger(duplicateDetails.size() + staleFailedCount);
		for (TmsAsyncTaskDetailEntity duplicateDetail : duplicateDetails) {
			asyncTaskDetailRecordService.updateDetail(
				duplicateDetail.getId(),
				TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),
				"同一任务下重复明细已跳过"
			);
		}

		for (TmsAsyncTaskDetailEntity detail : executableDetailMap.values()) {
			String taskDetailId = detail.getId();
			String businessId = detail.getBusinessId();
			try {
				costAllocationPool.execute(() -> {
					try {
						UserContext.setLoginUser(operatorUser);
						if (!asyncTaskDetailRecordService.tryClaimDetailForExecution(taskDetailId)) {
							log.debug("任务明细[{}]状态已变更，跳过", taskDetailId);
							return;
						}
						SmallBagCostAllocationMainEntity entity = entityMap.get(businessId);
						if (Objects.isNull(entity)) {
							asyncTaskDetailRecordService.updateDetail(taskDetailId,
								TmsAsyncTaskRecordStatusEnum.FAILED.getCode(), "主表记录不存在");
							failedCount.incrementAndGet();
							return;
						}

						BatchResultDTO result = executor.execute(taskDetailId, businessId, entity);
						if (Boolean.TRUE.equals(result.getSuccess())) {
							asyncTaskDetailRecordService.updateDetail(taskDetailId,
								TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), "");
							successCount.incrementAndGet();
						} else {
							String errorMsg = StringUtils.isNotBlank(result.getMsg())
								? StringUtils.substring(result.getMsg(), 0, 1000)
								: "未知错误";
							asyncTaskDetailRecordService.updateDetail(taskDetailId,
								TmsAsyncTaskRecordStatusEnum.FAILED.getCode(), errorMsg);
							failedCount.incrementAndGet();
						}
					} catch (Exception e) {
						log.error("处理小包费用分摊任务失败 taskDetailId: {}, businessId: {}", taskDetailId, businessId, e);
						asyncTaskRecordService.updateTaskDetailFailure(taskDetailId, e);
						failedCount.incrementAndGet();
					} finally {
						UserContext.clear();
						latch.countDown();
					}
				});
			} catch (RejectedExecutionException ex) {
				log.error("小包费用分摊任务提交失败 taskDetailId: {}, businessId: {}", taskDetailId, businessId, ex);
				asyncTaskDetailRecordService.updateDetail(taskDetailId,
					TmsAsyncTaskRecordStatusEnum.FAILED.getCode(), "线程池拒绝执行");
				failedCount.incrementAndGet();
				latch.countDown();
			}
		}

		List<TmsAsyncTaskDetailEntity> batchDetailSnapshot = new ArrayList<>(executableDetailMap.values());
		try {
			boolean completed = latch.await(timeoutSeconds, TimeUnit.SECONDS);
			if (!completed) {
				log.error("小包费用分摊批次处理超时，批次大小: {}, 超时时间: {}秒", batchDetails.size(), timeoutSeconds);
				failedCount.addAndGet(asyncTaskDetailRecordService.markUnfinishedBatchDetailsFailed(
					batchDetailSnapshot, "批次执行超时"));
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("小包费用分摊批次等待被中断", e);
			failedCount.addAndGet(asyncTaskDetailRecordService.markUnfinishedBatchDetailsFailed(
				batchDetailSnapshot, "任务等待中断"));
		}

		return new TmsAsyncTaskRecordDTO.BatchProcessResult(successCount.get(), failedCount.get());
	}

	private interface SmallBagTaskExecutor {
		BatchResultDTO execute(String taskDetailId, String businessId, SmallBagCostAllocationMainEntity entity);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResultDTO reAllocation(String id) {
		return reAllocation(smallBagCostAllocationMainService.getById(id));
	}

	@Transactional(rollbackFor = Exception.class)
	public BatchResultDTO reAllocation(SmallBagCostAllocationMainEntity smallBagCostAllocationMainEntity) {
		if (ObjectUtil.isEmpty(smallBagCostAllocationMainEntity)) {
			throw new ServiceException("小包分摊不存在");
		}
		String id = smallBagCostAllocationMainEntity.getId();
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
		logisticsBillCostService.pushAllocation(costId, smallBagCostAllocationMainEntity.getReportDate(), null);
		
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
		if (ObjectUtil.isEmpty(smallBagCostAllocationMainEntity)) {
			throw new ServiceException("小包分摊不存在");
		}
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
