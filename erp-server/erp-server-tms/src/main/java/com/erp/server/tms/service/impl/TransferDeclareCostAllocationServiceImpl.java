package com.erp.server.tms.service.impl;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.message.constant.DistributeKeyConstant;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqTopic;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO.ListDTO;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO.PagingParamDTO;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO.TabListDTO;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationDetailEntity;
import com.erp.model.tms.entity.TransferDeclareCostAllocationDetailEntity;
import com.erp.model.tms.entity.TransferDeclareCostAllocationEntity;
import com.erp.model.tms.entity.TransferDeclareCostAllocationMainEntity;
import com.erp.model.tms.enums.AllocationFeeTypeEnum;
import com.erp.model.tms.enums.CfgSettingEnum;
import com.erp.model.tms.enums.CostAllocationEnum;
import com.erp.model.tms.enums.SmallBagCostAllocationBigTableStatusEnum;
import com.erp.model.tms.enums.SmallBagCostAllocationReportStatusEnum;
import com.erp.model.tms.enums.TmsAsyncTaskMethodTypeEnum;
import com.erp.model.tms.enums.TmsAsyncTaskRecordStatusEnum;
import com.erp.model.tms.enums.TmsB2cDeclareReconciliationStatusEnum;
import com.erp.model.tms.enums.TransferDeclareCostAllocationBigTableStatusEnum;
import com.erp.model.tms.enums.TransferDeclareCostAllocationMainReportStatusEnum;
import com.erp.model.tms.enums.TransferDeclareCostAllocationReportStatusEnum;
import com.erp.model.tms.enums.WeightAllocationSmallBagEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.mapper.TransferDeclareCostAllocationMapper;
import com.erp.server.tms.service.CfgSettingService;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TmsAsyncTaskDetailService;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.erp.server.tms.service.TmsB2cDeclareReconciliationDetailService;
import com.erp.server.tms.service.TransferDeclareCostAllocationDetailService;
import com.erp.server.tms.service.TransferDeclareCostAllocationMainService;
import com.erp.server.tms.service.TransferDeclareCostAllocationService;
import com.erp.server.tms.service.TransferDeclareService;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.time.LocalDateTime;
/**
 * <p>
 * 中转费用分摊 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-12-03
 */
@Slf4j
@Service
public class TransferDeclareCostAllocationServiceImpl extends SuperServiceImpl<TransferDeclareCostAllocationMapper, TransferDeclareCostAllocationEntity> implements TransferDeclareCostAllocationService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private TransferDeclareCostAllocationMainService transferDeclareCostAllocationMainService;
    @Autowired
    private TransferDeclareCostAllocationDetailService transferDeclareCostAllocationDetailService;
    @Resource
    private LogisticsChannelService logisticsChannelService;
    @Resource
    private TmsB2cDeclareReconciliationDetailService tmsB2cDeclareReconciliationDetailService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;
    @Resource
    private TmsAsyncTaskDetailService asyncTaskDetailRecordService;
    @Resource
    private MQProducerService mQProducerService;
    @Lazy
    @Resource
    private TransferDeclareService transferDeclareService;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private RedissonClient redissonClient;
    @Lazy
    @Resource
    private TransferDeclareCostAllocationService self;

    @Autowired
    @Qualifier("costAllocationPool")
    private ExecutorService costAllocationPool;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TransferDeclareCostAllocationDTO.AddDTO addDTO) {
        TransferDeclareCostAllocationEntity transferDeclareCostAllocationEntity = new TransferDeclareCostAllocationEntity();
        BeanMapperUtils.copy(addDTO, transferDeclareCostAllocationEntity);

        // 数据处理
        handleData(transferDeclareCostAllocationEntity);

        log.info("开始新增中转费用分摊");
        boolean save = super.save(transferDeclareCostAllocationEntity);
        if(!save) {
            throw new ServiceException("中转费用分摊保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中转费用分摊" , transferDeclareCostAllocationEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, transferDeclareCostAllocationEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(transferDeclareCostAllocationEntity.getId(), transferDeclareCostAllocationEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TransferDeclareCostAllocationDTO.UpdateDTO updateDTO) {
        TransferDeclareCostAllocationEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "中转费用分摊"));
        TransferDeclareCostAllocationEntity transferDeclareCostAllocationEntity =  BeanMapperUtils.map(TransferDeclareCostAllocationEntity.class, updateDTO);

        // 数据处理
        handleData(transferDeclareCostAllocationEntity);
        log.info("编辑 开始修改中转费用分摊数据，id：【{}】", old.getId());
        boolean save = super.updateById(transferDeclareCostAllocationEntity);
        if(!save) {
            throw new ServiceException("中转费用分摊保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中转费用分摊日志数据，id：【{}】", transferDeclareCostAllocationEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), transferDeclareCostAllocationEntity.getId(), "中转费用分摊");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, transferDeclareCostAllocationEntity, null, transferDeclareCostAllocationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TransferDeclareCostAllocationEntity transferDeclareCostAllocationEntity) {
    // TODO 验证数据 & 数据赋值
    }

	@Override
	public List<TabListDTO> tabList(PermissionsDTO dto) {
		List<TransferDeclareCostAllocationDTO.TabListDTO> resultList = new ArrayList<>();
		Map<String, Integer> flagCountMap = this.getBaseMapper().tabList(dto).stream().collect(Collectors.toMap(TabListDTO::getTabFlag, TabListDTO::getCount));
		TransferDeclareCostAllocationReportStatusEnum[] values = TransferDeclareCostAllocationReportStatusEnum.values();
        for (TransferDeclareCostAllocationReportStatusEnum statusEnum : values) {
            LogisticsBillCostDTO.PagingParamDTO pagingParamDTO = new LogisticsBillCostDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            TransferDeclareCostAllocationDTO.TabListDTO resultDTO = new TransferDeclareCostAllocationDTO.TabListDTO();
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
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<ListDTO> pageData = this.baseMapper.paging(query, params);
        List<ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据赋值处理
        handleDataPaging(records);
        return new PagingVO(pageData);
	}
	
	private void handleDataPaging(List<ListDTO> records) {
		List<String> skuIds = records.stream().map(ListDTO::getSkuId).collect(Collectors.toList());
		List<ProductDetailEntity> productDetailEntityList = FeignQuery.create(ProductDetailEntity.class).in(ProductDetailEntity::getId, 
				skuIds).list();
		Map<String, String> skuIdNameMap = productDetailEntityList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getName));
		Map<String, LogisticsChannelEntity> channelIdMaps = logisticsChannelService.listByIds(records.stream().map(ListDTO::getChannelId).collect(Collectors.toList())).stream().collect(Collectors.toMap(LogisticsChannelEntity::getId, l -> l));
		List<String> shopIds = records.stream().map(ListDTO::getShopId).collect(Collectors.toList());
		List<ShopInfoEntity> shopInfoEntityList = FeignQuery.create(ShopInfoEntity.class).in(ShopInfoEntity::getId, 
				shopIds).list();
		Map<String, String> shopIdNameMap = shopInfoEntityList.stream().collect(Collectors.toMap(ShopInfoEntity::getId, ShopInfoEntity::getName));
		List<String> countryIds = records.stream().map(ListDTO::getToCountry).collect(Collectors.toList());
		List<DictCountryEntity> dictCountryEntityList = FeignQuery.create(DictCountryEntity.class).in(DictCountryEntity::getId, 
				countryIds).list();
		Map<String, String> countryIdNameMap = dictCountryEntityList.stream().collect(Collectors.toMap(DictCountryEntity::getId, DictCountryEntity::getNameCn));
		Map<String, BigDecimal> rateMap = new HashMap<>();
		DecimalFormat df6 = new DecimalFormat("0.000000");
		DecimalFormat df4 = new DecimalFormat("0.0000");
		for(ListDTO dto : records) {
			LogisticsChannelEntity logisticsChannelEntity = channelIdMaps.get(dto.getChannelId());
			if(logisticsChannelEntity != null) {
				dto.setSupplierName(logisticsChannelEntity.getName());
			}
			dto.setReportStatusName(SmallBagCostAllocationReportStatusEnum.getName(dto.getReportStatus()));
			String reconciliationStatus = dto.getReconciliationStatus();
			dto.setReconciliationStatusName(TmsB2cDeclareReconciliationStatusEnum.getName(reconciliationStatus));
			dto.setBigTableStatusName(SmallBagCostAllocationBigTableStatusEnum.getName(dto.getBigTableStatus()));
			String skuId = dto.getSkuId();
			dto.setSkuName(skuIdNameMap.get(skuId));
			String shopId = dto.getShopId();
			if(StringUtils.isNotBlank(shopId)) {
				dto.setShopName(shopIdNameMap.get(shopId));
			}
			String toCountry = dto.getToCountry();
			if(StringUtils.isNotBlank(toCountry)) {
				dto.setToCountry(countryIdNameMap.get(dto.getToCountry()));
			}
			
			Integer deliveryQty = dto.getDeliveryQty();
			String reportDate = dto.getReportDate();
			
			String billingWeight = dto.getBillingWeight();
			String estimateWeightUnit = dto.getEstimateWeightUnit();
			if("g".equals(estimateWeightUnit)) {
				dto.setBillingWeight(df4.format(new BigDecimal(billingWeight).divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP)));
			}else {
				dto.setBillingWeight(df4.format(new BigDecimal(billingWeight).setScale(4)));
			}
			
			BigDecimal unitCost = new BigDecimal(dto.getUnitCost());
			if(unitCost != null) {
				String unitCurrency = dto.getUnitCurrency();
				if(StringUtils.isNotBlank(unitCurrency) && !"CNY".equals(unitCurrency)) {
					String key = reportDate + "_" + unitCurrency;
					BigDecimal rate = rateMap.get(key);
					if(rate == null) {
						rate = dmpTaskFeign.getRate(reportDate + "-01", unitCurrency);
						if(ObjectUtil.isEmpty(rate)){
				            log.error("币别【{}】,汇率为空，请维护汇率后再查询",unitCurrency);
				            throw new ServiceException("汇率为空，请维护汇率后再查询");
				        }
						rateMap.put(key, rate);
					}
					unitCost = unitCost.multiply(rate).setScale(6);
				}
				dto.setUnitCost(df6.format(unitCost));
				dto.setTotalCost(df6.format(unitCost.multiply(new BigDecimal(deliveryQty)).setScale(6)));
			}
			
			dto.setFeeTypeName(AllocationFeeTypeEnum.getName(dto.getFeeType()));
			dto.setFeeAllocationTypeName(CostAllocationEnum.getName(dto.getFeeAllocationType()));
			if(dto.getWeightAllocationType() != null) {
				dto.setWeightAllocationTypeName(WeightAllocationSmallBagEnum.getName(dto.getWeightAllocationType()));
			}
			dto.setCurrencySymbol("¥");
			
		}
	}

	@Override
	public BatchResultDTO updateReportStatus(String id, String reportDate, String reportStatus) {
		return updateReportStatus(transferDeclareCostAllocationMainService.getById(id), reportDate, reportStatus);
	}

	private BatchResultDTO updateReportStatus(TransferDeclareCostAllocationMainEntity transferDeclareCostAllocationMainEntity, String reportDate, String reportStatus) {
		if (ObjectUtil.isEmpty(transferDeclareCostAllocationMainEntity)) {
			throw new ServiceException("中转分摊不存在");
		}
		if(StringUtils.isBlank(reportDate) && StringUtils.isBlank(reportStatus)) {
			throw new ServiceException("会计期间和核算状态不能同时为空");
		}
		if(StringUtils.isNotBlank(reportStatus)) {
			if(reportStatus.equals(transferDeclareCostAllocationMainEntity.getReportStatus())) {
				throw new ServiceException("更新前后核算状态一致");
			}
			if(reportStatus.equals(TransferDeclareCostAllocationMainReportStatusEnum.TOBECONFIRM.getCode())
					&& TransferDeclareCostAllocationBigTableStatusEnum.DONE.getCode().equals(transferDeclareCostAllocationMainEntity.getBigTableStatus())) {
				throw new ServiceException("物流大表已生成，无法从已确认更新为待确认");
			}
		}
		String id = transferDeclareCostAllocationMainEntity.getId();
		transferDeclareCostAllocationMainService.lambdaUpdate().eq(TransferDeclareCostAllocationMainEntity::getId, id)
			.set(StringUtils.isNotBlank(reportDate) , TransferDeclareCostAllocationMainEntity::getAccountDate, reportDate)
			.set(StringUtils.isNotBlank(reportStatus) , TransferDeclareCostAllocationMainEntity::getReportStatus, reportStatus)
			.update();
		return BatchResultDTO.success(id, id, "更新核算状态成功");
	}

	@Override
	public BatchResultDTO asyncUpdateReportStatus(TransferDeclareCostAllocationDTO.UpdateStatusDTO dto) {
		if (CharSequenceUtil.isBlank(dto.getReportPeriodStr())) {
			throw new ServiceException("核算期间不能为空");
		}
		if (StringUtils.isBlank(dto.getReportDate()) && StringUtils.isBlank(dto.getReportStatus())) {
			throw new ServiceException("会计期间和核算状态不能同时为空");
		}
		String reportStatus = dto.getReportStatus();
		boolean excludeBigTableDone = TransferDeclareCostAllocationMainReportStatusEnum.TOBECONFIRM.getCode().equals(reportStatus);
		String bigTableDoneCode = TransferDeclareCostAllocationBigTableStatusEnum.DONE.getCode();

		int total = transferDeclareCostAllocationMainService.countMainByReportPeriodStr(
			dto.getReportPeriodStr(), reportStatus, excludeBigTableDone, bigTableDoneCode);
		if (total == 0) {
			throw new ServiceException("没有可更新核算状态的数据");
		}

		TmsAsyncTaskRecordDTO.PushParamsDTO params = new TmsAsyncTaskRecordDTO.PushParamsDTO();
		params.setBusinessType(SourceTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode());
		params.setMethodType(TmsAsyncTaskMethodTypeEnum.UPDATE_REPORT_STATUS.getCode());
		params.setReportPeriodStr(dto.getReportPeriodStr());
		params.setReportStatus(reportStatus);
		params.setReportDate(dto.getReportDate());
		String jsonStr = JSONUtil.toJsonStr(params);

		TmsAsyncTaskRecordEntity taskRecord = asyncTaskRecordService.addManualTask(
			params.getBusinessType(), params.getMethodType(), total, jsonStr);
		if (Objects.isNull(taskRecord)) {
			throw new ServiceException(ApiError.LOGISTICS_ASYNC_TASK_CREATE_ERROR, jsonStr);
		}
		String taskId = taskRecord.getId();
		params.setTaskId(taskId);

		boolean claimed = asyncTaskRecordService.lambdaUpdate()
			.set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.ING.getCode())
			.set(TmsAsyncTaskRecordEntity::getErrorData, "任务已派发")
			.eq(TmsAsyncTaskRecordEntity::getId, taskId)
			.eq(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.PENDING.getCode())
			.update();
		BatchResultDTO claimResult = asyncTaskRecordService.resolveDispatchClaimOrThrow(taskId, claimed, taskRecord.getCode(), jsonStr);
		if (claimResult != null) {
			return claimResult;
		}

		try {
			SendResult sendResult = mQProducerService.syncClassMsg(
				RocketMqTopic.TMS_ASYNC_TASK_RECORD_TOPIC, RocketMqNewTag.TMS_ASYNC_TASK_RECORD_TAG, params, taskId);
			if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
				log.error("中转核算状态变更 MQ消息发送失败：{}", sendResult);
				throw new ServiceException(ApiError.LOGISTICS_ASYNC_TASK_CREATE_ERROR, "MQ消息发送失败");
			}
			log.info("中转核算状态变更 MQ消息发送成功，taskId: {}, 预计处理数据量: {}", taskId, total);
		} catch (Exception e) {
			log.error("中转核算状态变更 MQ消息发送异常，taskId: {}", taskId, e);
			asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
				asyncTaskRecordService.formatTaskErrorMessage(e));
			throw e;
		}

		return BatchResultDTO.success(taskId, taskRecord.getCode());
	}

	@Override
	public void pushUpdateReportStatus(TmsAsyncTaskRecordDTO.PushParamsDTO dto) {
		String taskId = dto.getTaskId();
		if (StringUtils.isBlank(taskId)) {
			log.error("中转核算状态变更异步任务ID为空");
			return;
		}

		RLock taskLock = redissonClient.getLock(DistributeKeyConstant.TMS_ASYNC_TASK_EXEC_KEY + ":" + taskId);
		boolean locked = false;
		try {
			locked = taskLock.tryLock(0, TimeUnit.SECONDS);
			if (!locked) {
				log.warn("中转核算状态变更异步任务正在执行，跳过重复消费，taskId: {}", taskId);
				return;
			}

			CfgSettingValueDTO.BillBatchParamsDTO billBatchParamsDTO = asyncTaskRecordService.loadBillBatchParams(taskId);
			if (billBatchParamsDTO == null) {
				return;
			}

			TmsAsyncTaskRecordEntity taskRecord = asyncTaskRecordService.getById(taskId);
			if (Objects.isNull(taskRecord)) {
				log.error("任务记录不存在，taskId: {}", taskId);
				return;
			}
			if (Objects.equals(taskRecord.getStatus(), TmsAsyncTaskRecordStatusEnum.FINISH.getCode())) {
				log.warn("中转核算状态变更异步任务已完成，跳过重复消费，taskId: {}", taskId);
				return;
			}
			if (Objects.equals(taskRecord.getStatus(), TmsAsyncTaskRecordStatusEnum.PENDING.getCode())) {
				boolean claimed = asyncTaskRecordService.lambdaUpdate()
					.set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.ING.getCode())
					.set(TmsAsyncTaskRecordEntity::getErrorData, "分批处理中")
					.eq(TmsAsyncTaskRecordEntity::getId, taskId)
					.eq(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.PENDING.getCode())
					.update();
				if (!claimed) {
					log.warn("中转核算状态变更异步任务已被其他消费者认领，taskId: {}", taskId);
					return;
				}
			} else if (!Objects.equals(taskRecord.getStatus(), TmsAsyncTaskRecordStatusEnum.ING.getCode())) {
			log.warn("中转核算状态变更异步任务状态不可执行，taskId: {}, status: {}", taskId, taskRecord.getStatus());
			return;
		}

		int batchSize = asyncTaskRecordService.resolveBatchSize(billBatchParamsDTO.getTransferBatch(), 500);
			int timeoutSeconds = asyncTaskRecordService.resolveBatchSize(billBatchParamsDTO.getTransferTimeoutSeconds(), 5000);
			String lastId = "";
			int totalProcessed = 0;
			int totalSuccess = 0;
			int totalFailed = 0;
			int batchNumber = 0;
			LocalDateTime taskStartTime = taskRecord.getStartTime();
			Integer taskExecTimeout = taskRecord.getExecTimeout();

			boolean excludeBigTableDone = TransferDeclareCostAllocationMainReportStatusEnum.TOBECONFIRM.getCode().equals(dto.getReportStatus());
			String bigTableDoneCode = TransferDeclareCostAllocationBigTableStatusEnum.DONE.getCode();

			log.info("开始分批处理中转核算状态变更任务，taskId: {}, 批次大小: {}, 预计总数: {}", taskId, batchSize, taskRecord.getDetailCount());

			while (true) {
				batchNumber++;

				if (batchNumber == 1 || batchNumber % 10 == 0) {
					TmsAsyncTaskRecordEntity currentTask = asyncTaskRecordService.getById(taskId);
					if (asyncTaskRecordService.shouldStopLoopTask(taskId, currentTask)) {
						break;
					}
					taskExecTimeout = currentTask.getExecTimeout();
				}

				if (taskExecTimeout != null && taskExecTimeout > 0 && taskStartTime != null) {
					long elapsedSeconds = Duration.between(taskStartTime, LocalDateTime.now()).getSeconds();
					if (elapsedSeconds > taskExecTimeout) {
						log.error("任务执行超时，taskId: {}, 已耗时: {}秒", taskId, elapsedSeconds);
						asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
							"任务执行超时，已耗时" + elapsedSeconds + "秒");
						break;
					}
				}

				List<String> batchIds;
				try {
					String cursor = lastId;
					batchIds = pageTransferCostAllocationIds(dto.getIds(), cursor, batchSize,
						() -> transferDeclareCostAllocationMainService.pageMainIdsByReportPeriodStr(
							dto.getReportPeriodStr(), dto.getReportStatus(), excludeBigTableDone, bigTableDoneCode, cursor, batchSize));
				} catch (Exception e) {
					log.error("第{}批查询失败，taskId: {}", batchNumber, taskId, e);
					asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
						"第" + batchNumber + "批查询失败: " + asyncTaskRecordService.formatTaskErrorMessage(e));
					break;
				}

				if (CollUtil.isEmpty(batchIds)) {
					log.info("所有数据处理完成，taskId: {}, 总批次: {}, 总处理: {}/成功: {}/失败: {}",
						taskId, batchNumber - 1, totalProcessed, totalSuccess, totalFailed);
					break;
				}

				// 先写入任务明细再变更状态，保证中转分摊任务可按单据追踪失败原因。
				TmsAsyncTaskRecordDTO.BatchProcessResult result = processTransferUpdateStatusBatch(taskId, batchIds, dto, timeoutSeconds);

				totalProcessed += batchIds.size();
				totalSuccess += result.getSuccessCount();
				totalFailed += result.getFailedCount();

				try {
					asyncTaskRecordService.lambdaUpdate()
						.set(TmsAsyncTaskRecordEntity::getErrorCount, totalFailed)
						.eq(TmsAsyncTaskRecordEntity::getId, taskId)
						.update();
				} catch (Exception e) {
					log.error("更新任务进度失败，taskId: {}", taskId, e);
				}

				lastId = batchIds.get(batchIds.size() - 1);
			}

			try {
				asyncTaskRecordService.updateTaskFinally(taskId);
				log.info("中转核算状态变更任务最终状态更新完成，taskId: {}", taskId);
			} catch (Exception e) {
				log.error("更新任务最终状态失败，taskId: {}", taskId, e);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("中转核算状态变更异步任务获取锁被中断，taskId: {}", taskId, e);
		} catch (Exception e) {
			log.error("中转核算状态变更异步任务执行失败，taskId: {}", taskId, e);
			asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
				asyncTaskRecordService.formatTaskErrorMessage(e));
		} finally {
			if (locked && taskLock.isHeldByCurrentThread()) {
				taskLock.unlock();
			}
		}
	}

	@Override
	public BatchResultDTO asyncReAllocation(FirstMileCostAllocationDTO.ResetIdsDTO dto) {
		if (CharSequenceUtil.isBlank(dto.getReportPeriodStr())) {
			throw new ServiceException("核算期间不能为空");
		}
		String reportStatus = TransferDeclareCostAllocationMainReportStatusEnum.TOBECONFIRM.getCode();
		int total = transferDeclareCostAllocationMainService.countMainForReAllocation(dto.getReportPeriodStr(), reportStatus);
		if (total == 0) {
			throw new ServiceException("没有可重新分摊的数据");
		}

		TmsAsyncTaskRecordDTO.PushParamsDTO params = new TmsAsyncTaskRecordDTO.PushParamsDTO();
		params.setBusinessType(SourceTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode());
		params.setMethodType(TmsAsyncTaskMethodTypeEnum.RE_ALLOCATION.getCode());
		params.setReportPeriodStr(dto.getReportPeriodStr());
		params.setReportStatus(reportStatus);
		String jsonStr = JSONUtil.toJsonStr(params);

		TmsAsyncTaskRecordEntity taskRecord = asyncTaskRecordService.addManualTask(
			params.getBusinessType(), params.getMethodType(), total, jsonStr);
		if (Objects.isNull(taskRecord)) {
			throw new ServiceException(ApiError.LOGISTICS_ASYNC_TASK_CREATE_ERROR, jsonStr);
		}
		String taskId = taskRecord.getId();
		params.setTaskId(taskId);

		boolean claimed = asyncTaskRecordService.lambdaUpdate()
			.set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.ING.getCode())
			.set(TmsAsyncTaskRecordEntity::getErrorData, "任务已派发")
			.eq(TmsAsyncTaskRecordEntity::getId, taskId)
			.eq(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.PENDING.getCode())
			.update();
		BatchResultDTO claimResult = asyncTaskRecordService.resolveDispatchClaimOrThrow(taskId, claimed, taskRecord.getCode(), jsonStr);
		if (claimResult != null) {
			return claimResult;
		}

		try {
			SendResult sendResult = mQProducerService.syncClassMsg(
				RocketMqTopic.TMS_ASYNC_TASK_RECORD_TOPIC, RocketMqNewTag.TMS_ASYNC_TASK_RECORD_TAG, params, taskId);
			if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
				log.error("中转重新分摊 MQ消息发送失败：{}", sendResult);
				throw new ServiceException(ApiError.LOGISTICS_ASYNC_TASK_CREATE_ERROR, "MQ消息发送失败");
			}
			log.info("中转重新分摊 MQ消息发送成功，taskId: {}, 预计处理数据量: {}", taskId, total);
		} catch (Exception e) {
			log.error("中转重新分摊 MQ消息发送异常，taskId: {}", taskId, e);
			asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
				asyncTaskRecordService.formatTaskErrorMessage(e));
			throw e;
		}

		return BatchResultDTO.success(taskId, taskRecord.getCode());
	}

	@Override
	public void pushReAllocation(TmsAsyncTaskRecordDTO.PushParamsDTO dto) {
		String taskId = dto.getTaskId();
		if (StringUtils.isBlank(taskId)) {
			log.error("中转重新分摊异步任务ID为空");
			return;
		}

		RLock taskLock = redissonClient.getLock(DistributeKeyConstant.TMS_ASYNC_TASK_EXEC_KEY + ":" + taskId);
		boolean locked = false;
		try {
			locked = taskLock.tryLock(0, TimeUnit.SECONDS);
			if (!locked) {
				log.warn("中转重新分摊异步任务正在执行，跳过重复消费，taskId: {}", taskId);
				return;
			}

			CfgSettingValueDTO.BillBatchParamsDTO billBatchParamsDTO = asyncTaskRecordService.loadBillBatchParams(taskId);
			if (billBatchParamsDTO == null) {
				return;
			}

			TmsAsyncTaskRecordEntity taskRecord = asyncTaskRecordService.getById(taskId);
			if (Objects.isNull(taskRecord)) {
				log.error("任务记录不存在，taskId: {}", taskId);
				return;
			}
			if (Objects.equals(taskRecord.getStatus(), TmsAsyncTaskRecordStatusEnum.FINISH.getCode())) {
				log.warn("中转重新分摊异步任务已完成，跳过重复消费，taskId: {}", taskId);
				return;
			}
			if (Objects.equals(taskRecord.getStatus(), TmsAsyncTaskRecordStatusEnum.PENDING.getCode())) {
				boolean claimed = asyncTaskRecordService.lambdaUpdate()
					.set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.ING.getCode())
					.set(TmsAsyncTaskRecordEntity::getErrorData, "分批处理中")
					.eq(TmsAsyncTaskRecordEntity::getId, taskId)
					.eq(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.PENDING.getCode())
					.update();
				if (!claimed) {
					log.warn("中转重新分摊异步任务已被其他消费者认领，taskId: {}", taskId);
					return;
				}
			} else if (!Objects.equals(taskRecord.getStatus(), TmsAsyncTaskRecordStatusEnum.ING.getCode())) {
			log.warn("中转重新分摊异步任务状态不可执行，taskId: {}, status: {}", taskId, taskRecord.getStatus());
			return;
		}

		int batchSize = asyncTaskRecordService.resolveBatchSize(billBatchParamsDTO.getTransferBatch(), 500);
			int timeoutSeconds = asyncTaskRecordService.resolveBatchSize(billBatchParamsDTO.getTransferTimeoutSeconds(), 5000);
			String lastId = "";
			int totalProcessed = 0;
			int totalSuccess = 0;
			int totalFailed = 0;
			int batchNumber = 0;
			LocalDateTime taskStartTime = taskRecord.getStartTime();
			Integer taskExecTimeout = taskRecord.getExecTimeout();
			String reportStatus = TransferDeclareCostAllocationMainReportStatusEnum.TOBECONFIRM.getCode();

			log.info("开始分批处理中转重新分摊任务，taskId: {}, 批次大小: {}, 预计总数: {}", taskId, batchSize, taskRecord.getDetailCount());


			while (true) {
				batchNumber++;

				if (batchNumber == 1 || batchNumber % 10 == 0) {
					TmsAsyncTaskRecordEntity currentTask = asyncTaskRecordService.getById(taskId);
					if (asyncTaskRecordService.shouldStopLoopTask(taskId, currentTask)) {
						break;
					}
					taskExecTimeout = currentTask.getExecTimeout();
				}

				if (taskExecTimeout != null && taskExecTimeout > 0 && taskStartTime != null) {
					long elapsedSeconds = Duration.between(taskStartTime, LocalDateTime.now()).getSeconds();
					if (elapsedSeconds > taskExecTimeout) {
						log.error("任务执行超时，taskId: {}, 已耗时: {}秒", taskId, elapsedSeconds);
						asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
							"任务执行超时，已耗时" + elapsedSeconds + "秒");
						break;
					}
				}

				List<String> batchIds;
				try {
					String cursor = lastId;
					batchIds = pageTransferCostAllocationIds(dto.getIds(), cursor, batchSize,
						() -> transferDeclareCostAllocationMainService.pageMainIdsForReAllocation(
							dto.getReportPeriodStr(), reportStatus, cursor, batchSize));
				} catch (Exception e) {
					log.error("第{}批查询失败，taskId: {}", batchNumber, taskId, e);
					asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
						"第" + batchNumber + "批查询失败: " + asyncTaskRecordService.formatTaskErrorMessage(e));
					break;
				}

				if (CollUtil.isEmpty(batchIds)) {
					log.info("所有数据处理完成，taskId: {}, 总批次: {}, 总处理: {}/成功: {}/失败: {}",
						taskId, batchNumber - 1, totalProcessed, totalSuccess, totalFailed);
					break;
				}

				// 重新分摊可能部分单据失败，明细表用于后续只重试失败单据。
				TmsAsyncTaskRecordDTO.BatchProcessResult result = processTransferReAllocationBatch(taskId, batchIds, timeoutSeconds);

				totalProcessed += batchIds.size();
				totalSuccess += result.getSuccessCount();
				totalFailed += result.getFailedCount();

				try {
					asyncTaskRecordService.lambdaUpdate()
						.set(TmsAsyncTaskRecordEntity::getErrorCount, totalFailed)
						.eq(TmsAsyncTaskRecordEntity::getId, taskId)
						.update();
				} catch (Exception e) {
					log.error("更新任务进度失败，taskId: {}", taskId, e);
				}

				lastId = batchIds.get(batchIds.size() - 1);
			}

			try {
				asyncTaskRecordService.updateTaskFinally(taskId);
				log.info("中转重新分摊任务最终状态更新完成，taskId: {}", taskId);
			} catch (Exception e) {
				log.error("更新任务最终状态失败，taskId: {}", taskId, e);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("中转重新分摊异步任务获取锁被中断，taskId: {}", taskId, e);
		} catch (Exception e) {
			log.error("中转重新分摊异步任务执行失败，taskId: {}", taskId, e);
			asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
				asyncTaskRecordService.formatTaskErrorMessage(e));
		} finally {
			if (locked && taskLock.isHeldByCurrentThread()) {
				taskLock.unlock();
			}
		}
	}

	@Override
	public BatchResultDTO asyncDelete(FirstMileCostAllocationDTO.ResetIdsDTO dto) {
		if (CharSequenceUtil.isBlank(dto.getReportPeriodStr())) {
			throw new ServiceException("核算期间不能为空");
		}
		String reportStatus = TransferDeclareCostAllocationMainReportStatusEnum.TOBECONFIRM.getCode();
		int total = transferDeclareCostAllocationMainService.countMainForReAllocation(dto.getReportPeriodStr(), reportStatus);
		if (total == 0) {
			throw new ServiceException("没有可删除的数据");
		}

		TmsAsyncTaskRecordDTO.PushParamsDTO params = new TmsAsyncTaskRecordDTO.PushParamsDTO();
		params.setBusinessType(SourceTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode());
		params.setMethodType(TmsAsyncTaskMethodTypeEnum.DELETE.getCode());
		params.setReportPeriodStr(dto.getReportPeriodStr());
		params.setReportStatus(reportStatus);
		String jsonStr = JSONUtil.toJsonStr(params);

		TmsAsyncTaskRecordEntity taskRecord = asyncTaskRecordService.addManualTask(
			params.getBusinessType(), params.getMethodType(), total, jsonStr);
		if (Objects.isNull(taskRecord)) {
			throw new ServiceException(ApiError.LOGISTICS_ASYNC_TASK_CREATE_ERROR, jsonStr);
		}
		String taskId = taskRecord.getId();
		params.setTaskId(taskId);

		boolean claimed = asyncTaskRecordService.lambdaUpdate()
			.set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.ING.getCode())
			.set(TmsAsyncTaskRecordEntity::getErrorData, "任务已派发")
			.eq(TmsAsyncTaskRecordEntity::getId, taskId)
			.eq(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.PENDING.getCode())
			.update();
		BatchResultDTO claimResult = asyncTaskRecordService.resolveDispatchClaimOrThrow(taskId, claimed, taskRecord.getCode(), jsonStr);
		if (claimResult != null) {
			return claimResult;
		}

		try {
			SendResult sendResult = mQProducerService.syncClassMsg(
				RocketMqTopic.TMS_ASYNC_TASK_RECORD_TOPIC, RocketMqNewTag.TMS_ASYNC_TASK_RECORD_TAG, params, taskId);
			if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
				log.error("中转批量删除 MQ消息发送失败：{}", sendResult);
				throw new ServiceException(ApiError.LOGISTICS_ASYNC_TASK_CREATE_ERROR, "MQ消息发送失败");
			}
			log.info("中转批量删除 MQ消息发送成功，taskId: {}, 预计处理数据量: {}", taskId, total);
		} catch (Exception e) {
			log.error("中转批量删除 MQ消息发送异常，taskId: {}", taskId, e);
			asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
				asyncTaskRecordService.formatTaskErrorMessage(e));
			throw e;
		}

		return BatchResultDTO.success(taskId, taskRecord.getCode());
	}

	@Override
	public void pushDelete(TmsAsyncTaskRecordDTO.PushParamsDTO dto) {
		String taskId = dto.getTaskId();
		if (StringUtils.isBlank(taskId)) {
			log.error("中转批量删除异步任务ID为空");
			return;
		}

		RLock taskLock = redissonClient.getLock(DistributeKeyConstant.TMS_ASYNC_TASK_EXEC_KEY + ":" + taskId);
		boolean locked = false;
		try {
			locked = taskLock.tryLock(0, TimeUnit.SECONDS);
			if (!locked) {
				log.warn("中转批量删除异步任务正在执行，跳过重复消费，taskId: {}", taskId);
				return;
			}

			CfgSettingValueDTO.BillBatchParamsDTO billBatchParamsDTO = asyncTaskRecordService.loadBillBatchParams(taskId);
			if (billBatchParamsDTO == null) {
				return;
			}

			TmsAsyncTaskRecordEntity taskRecord = asyncTaskRecordService.getById(taskId);
			if (Objects.isNull(taskRecord)) {
				log.error("任务记录不存在，taskId: {}", taskId);
				return;
			}
			if (Objects.equals(taskRecord.getStatus(), TmsAsyncTaskRecordStatusEnum.FINISH.getCode())) {
				log.warn("中转批量删除异步任务已完成，跳过重复消费，taskId: {}", taskId);
				return;
			}
			if (Objects.equals(taskRecord.getStatus(), TmsAsyncTaskRecordStatusEnum.PENDING.getCode())) {
				boolean claimed = asyncTaskRecordService.lambdaUpdate()
					.set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.ING.getCode())
					.set(TmsAsyncTaskRecordEntity::getErrorData, "分批处理中")
					.eq(TmsAsyncTaskRecordEntity::getId, taskId)
					.eq(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.PENDING.getCode())
					.update();
				if (!claimed) {
					log.warn("中转批量删除异步任务已被其他消费者认领，taskId: {}", taskId);
					return;
				}
			} else if (!Objects.equals(taskRecord.getStatus(), TmsAsyncTaskRecordStatusEnum.ING.getCode())) {
			log.warn("中转批量删除异步任务状态不可执行，taskId: {}, status: {}", taskId, taskRecord.getStatus());
			return;
		}

		int batchSize = asyncTaskRecordService.resolveBatchSize(billBatchParamsDTO.getTransferBatch(), 500);
			int timeoutSeconds = asyncTaskRecordService.resolveBatchSize(billBatchParamsDTO.getTransferTimeoutSeconds(), 5000);
			String lastId = "";
			int totalProcessed = 0;
			int totalSuccess = 0;
			int totalFailed = 0;
			int batchNumber = 0;
			LocalDateTime taskStartTime = taskRecord.getStartTime();
			Integer taskExecTimeout = taskRecord.getExecTimeout();
			String reportStatus = TransferDeclareCostAllocationMainReportStatusEnum.TOBECONFIRM.getCode();

			log.info("开始分批处理中转批量删除任务，taskId: {}, 批次大小: {}, 预计总数: {}", taskId, batchSize, taskRecord.getDetailCount());


			while (true) {
				batchNumber++;

				if (batchNumber == 1 || batchNumber % 10 == 0) {
					TmsAsyncTaskRecordEntity currentTask = asyncTaskRecordService.getById(taskId);
					if (asyncTaskRecordService.shouldStopLoopTask(taskId, currentTask)) {
						break;
					}
					taskExecTimeout = currentTask.getExecTimeout();
				}

				if (taskExecTimeout != null && taskExecTimeout > 0 && taskStartTime != null) {
					long elapsedSeconds = Duration.between(taskStartTime, LocalDateTime.now()).getSeconds();
					if (elapsedSeconds > taskExecTimeout) {
						log.error("任务执行超时，taskId: {}, 已耗时: {}秒", taskId, elapsedSeconds);
						asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
							"任务执行超时，已耗时" + elapsedSeconds + "秒");
						break;
					}
				}

				List<String> batchIds;
				try {
					String cursor = lastId;
					batchIds = pageTransferCostAllocationIds(dto.getIds(), cursor, batchSize,
						() -> transferDeclareCostAllocationMainService.pageMainIdsForReAllocation(
							dto.getReportPeriodStr(), reportStatus, cursor, batchSize));
				} catch (Exception e) {
					log.error("第{}批查询失败，taskId: {}", batchNumber, taskId, e);
					asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
						"第" + batchNumber + "批查询失败: " + asyncTaskRecordService.formatTaskErrorMessage(e));
					break;
				}

				if (CollUtil.isEmpty(batchIds)) {
					log.info("所有数据处理完成，taskId: {}, 总批次: {}, 总处理: {}/成功: {}/失败: {}",
						taskId, batchNumber - 1, totalProcessed, totalSuccess, totalFailed);
					break;
				}

				// 删除任务同样按明细记录执行结果，避免 MQ 重投时重复处理已完成单据。
				TmsAsyncTaskRecordDTO.BatchProcessResult result = processTransferDeleteBatch(taskId, batchIds, timeoutSeconds);

				totalProcessed += batchIds.size();
				totalSuccess += result.getSuccessCount();
				totalFailed += result.getFailedCount();

				try {
					asyncTaskRecordService.lambdaUpdate()
						.set(TmsAsyncTaskRecordEntity::getErrorCount, totalFailed)
						.eq(TmsAsyncTaskRecordEntity::getId, taskId)
						.update();
				} catch (Exception e) {
					log.error("更新任务进度失败，taskId: {}", taskId, e);
				}

				lastId = batchIds.get(batchIds.size() - 1);
			}

			try {
				asyncTaskRecordService.updateTaskFinally(taskId);
				log.info("中转批量删除任务最终状态更新完成，taskId: {}", taskId);
			} catch (Exception e) {
				log.error("更新任务最终状态失败，taskId: {}", taskId, e);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("中转批量删除异步任务获取锁被中断，taskId: {}", taskId, e);
		} catch (Exception e) {
			log.error("中转批量删除异步任务执行失败，taskId: {}", taskId, e);
			asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
				asyncTaskRecordService.formatTaskErrorMessage(e));
		} finally {
			if (locked && taskLock.isHeldByCurrentThread()) {
				taskLock.unlock();
			}
		}
	}

	/**
	 * 普通任务按查询条件分页；失败重试任务只遍历失败明细中的 businessId。
	 */
	private List<String> pageTransferCostAllocationIds(List<String> retryIds, String lastId, int batchSize, Supplier<List<String>> querySupplier) {
		if (CollUtil.isEmpty(retryIds)) {
			return querySupplier.get();
		}
		List<String> distinctIds = retryIds.stream()
			.filter(StringUtils::isNotBlank)
			.distinct()
			.collect(Collectors.toList());
		int startIndex = 0;
		if (StringUtils.isNotBlank(lastId)) {
			int lastIndex = distinctIds.indexOf(lastId);
			if (lastIndex < 0 || lastIndex + 1 >= distinctIds.size()) {
				return Collections.emptyList();
			}
			startIndex = lastIndex + 1;
		}
		int endIndex = Math.min(startIndex + batchSize, distinctIds.size());
		return new ArrayList<>(distinctIds.subList(startIndex, endIndex));
	}

	/**
	 * 中转核算状态变更批次：为每条主表记录落明细并回写状态变更结果。
	 */
	private TmsAsyncTaskRecordDTO.BatchProcessResult processTransferUpdateStatusBatch(String taskId,
																					 List<String> batchIds,
																					 TmsAsyncTaskRecordDTO.PushParamsDTO dto,
																					 int timeoutSeconds) {
		Map<String, TransferDeclareCostAllocationMainEntity> entityMap = transferDeclareCostAllocationMainService.listByIds(batchIds).stream()
			.collect(Collectors.toMap(TransferDeclareCostAllocationMainEntity::getId, Function.identity(), (a, b) -> a));
		List<TmsAsyncTaskDetailEntity> detailsToExecute = prepareTransferTaskDetails(taskId, batchIds, entityMap);
		return executeTransferBatchWithConcurrency(detailsToExecute, entityMap, timeoutSeconds, (taskDetailId, businessId, entity) -> {
			updateReportStatus(entity, dto.getReportDate(), dto.getReportStatus());
			return BatchResultDTO.success(businessId, entity.getTransferDeclareId(), "核算状态变更成功");
		});
	}

	/**
	 * 中转重新分摊批次：按异步任务明细维度执行，支持单据级失败重试。
	 */
	private TmsAsyncTaskRecordDTO.BatchProcessResult processTransferReAllocationBatch(String taskId,
																					 List<String> batchIds,
																					 int timeoutSeconds) {
		Map<String, TransferDeclareCostAllocationMainEntity> entityMap = transferDeclareCostAllocationMainService.listByIds(batchIds).stream()
			.collect(Collectors.toMap(TransferDeclareCostAllocationMainEntity::getId, Function.identity(), (a, b) -> a));
		List<TmsAsyncTaskDetailEntity> detailsToExecute = prepareTransferTaskDetails(taskId, batchIds, entityMap);
		return executeTransferBatchWithConcurrency(detailsToExecute, entityMap, timeoutSeconds, (taskDetailId, businessId, entity) ->
			self.reAllocation(entity));
	}

	/**
	 * 中转批量删除批次：逐条记录删除结果，便于任务明细页定位失败单据。
	 */
	private TmsAsyncTaskRecordDTO.BatchProcessResult processTransferDeleteBatch(String taskId,
																			   List<String> batchIds,
																			   int timeoutSeconds) {
		Map<String, TransferDeclareCostAllocationMainEntity> entityMap = transferDeclareCostAllocationMainService.listByIds(batchIds).stream()
			.collect(Collectors.toMap(TransferDeclareCostAllocationMainEntity::getId, Function.identity(), (a, b) -> a));
		List<TmsAsyncTaskDetailEntity> detailsToExecute = prepareTransferTaskDetails(taskId, batchIds, entityMap);
		return executeTransferBatchWithConcurrency(detailsToExecute, entityMap, timeoutSeconds, (taskDetailId, businessId, entity) ->
			self.delete(businessId));
	}

	/**
	 * 创建本批次缺失的任务明细；已存在的明细不重复创建，防止重投重复执行。
	 */
	private List<TmsAsyncTaskDetailEntity> prepareTransferTaskDetails(String taskId,
																	  List<String> batchIds,
																	  Map<String, TransferDeclareCostAllocationMainEntity> entityMap) {
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
			TransferDeclareCostAllocationMainEntity entity = entityMap.get(businessId);
			TmsAsyncTaskDetailEntity detail = new TmsAsyncTaskDetailEntity();
			detail.setMainId(taskId);
			detail.setBusinessType(SourceTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode());
			detail.setBusinessId(businessId);
			if (Objects.nonNull(entity)) {
				detail.setBusinessCode(entity.getTransferDeclareId());
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
	 * 并发处理中转任务明细，通过 PENDING -> ING 认领控制同一明细只执行一次。
	 */
	private TmsAsyncTaskRecordDTO.BatchProcessResult executeTransferBatchWithConcurrency(List<TmsAsyncTaskDetailEntity> batchDetails,
																						Map<String, TransferDeclareCostAllocationMainEntity> entityMap,
																						int timeoutSeconds,
																						TransferTaskExecutor executor) {
		if (CollUtil.isEmpty(batchDetails)) {
			return new TmsAsyncTaskRecordDTO.BatchProcessResult(0, 0);
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
		AtomicInteger failedCount = new AtomicInteger(duplicateDetails.size());
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
			costAllocationPool.execute(() -> {
				try {
					if (!asyncTaskDetailRecordService.tryClaimDetailForExecution(taskDetailId)) {
						log.debug("任务明细[{}]状态已变更，跳过", taskDetailId);
						return;
					}
					TransferDeclareCostAllocationMainEntity entity = entityMap.get(businessId);
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
					log.error("处理中转费用分摊任务失败 taskDetailId: {}, businessId: {}", taskDetailId, businessId, e);
					asyncTaskRecordService.updateTaskDetailFailure(taskDetailId, e);
					failedCount.incrementAndGet();
				} finally {
					latch.countDown();
				}
			});
		}

		try {
			boolean completed = latch.await(timeoutSeconds, TimeUnit.SECONDS);
			if (!completed) {
				log.error("中转费用分摊批次处理超时，批次大小: {}, 超时时间: {}秒", batchDetails.size(), timeoutSeconds);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("中转费用分摊批次等待被中断", e);
		}

		return new TmsAsyncTaskRecordDTO.BatchProcessResult(successCount.get(), failedCount.get());
	}

	private interface TransferTaskExecutor {
		BatchResultDTO execute(String taskDetailId, String businessId, TransferDeclareCostAllocationMainEntity entity);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public BatchResultDTO reAllocation(String id) {
		return reAllocation(transferDeclareCostAllocationMainService.getById(id));
	}

	@Transactional(rollbackFor = Exception.class)
	public BatchResultDTO reAllocation(TransferDeclareCostAllocationMainEntity transferDeclareCostAllocationMainEntity) {
		if (ObjectUtil.isEmpty(transferDeclareCostAllocationMainEntity)) {
			throw new ServiceException("中转分摊不存在");
		}
		String id = transferDeclareCostAllocationMainEntity.getId();
		if(SmallBagCostAllocationReportStatusEnum.CONFIRMED.getCode().equals(transferDeclareCostAllocationMainEntity.getReportStatus())) {
			throw new ServiceException("所选分摊费用核算状态必须为【待确认】才可重新下推");
		}
		transferDeclareCostAllocationMainService.removeById(id);
		List<String> ids = lambdaQuery().eq(TransferDeclareCostAllocationEntity::getMainId, id).list().stream().map(TransferDeclareCostAllocationEntity::getId).collect(Collectors.toList());
		removeByIds(ids);
		transferDeclareCostAllocationDetailService.lambdaUpdate()
			.in(TransferDeclareCostAllocationDetailEntity::getMainId, ids)
			.set(TransferDeclareCostAllocationDetailEntity::getIsDeleted, true)
			.update();
		
		TmsB2cDeclareReconciliationDetailEntity tmsB2cDeclareReconciliationDetailEntity = tmsB2cDeclareReconciliationDetailService.getById(transferDeclareCostAllocationMainEntity.getDeclareReconciliationDetailId());
		ApplicationContextUtils.getBean(TransferDeclareServiceImpl.class).singPushAllocation(transferDeclareCostAllocationMainEntity.getTransferDeclareId(), 
				transferDeclareCostAllocationMainEntity.getReportDate(), Arrays.asList(tmsB2cDeclareReconciliationDetailEntity));
		transferDeclareCostAllocationMainService.lambdaUpdate().eq(TransferDeclareCostAllocationMainEntity::getDeclareReconciliationDetailId, transferDeclareCostAllocationMainEntity.getDeclareReconciliationDetailId())
			.set(TransferDeclareCostAllocationMainEntity::getCreateTime, transferDeclareCostAllocationMainEntity.getCreateTime())
			.set(TransferDeclareCostAllocationMainEntity::getCreateUserId, transferDeclareCostAllocationMainEntity.getCreateUserId())
			.set(TransferDeclareCostAllocationMainEntity::getCreateUserName, transferDeclareCostAllocationMainEntity.getCreateUserName())
			.update();
		return BatchResultDTO.success(id, id, "重新下推成功");
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public BatchResultDTO delete(String id) {
		TransferDeclareCostAllocationMainEntity transferDeclareCostAllocationMainEntity = transferDeclareCostAllocationMainService.getById(id);
		if (ObjectUtil.isEmpty(transferDeclareCostAllocationMainEntity)) {
			throw new ServiceException("中转分摊不存在");
		}
		if(SmallBagCostAllocationReportStatusEnum.CONFIRMED.getCode().equals(transferDeclareCostAllocationMainEntity.getReportStatus())) {
			throw new ServiceException("所选分摊费用核算状态必须为【待确认】才可删除");
		}
		transferDeclareCostAllocationMainService.removeById(id);
		List<String> ids = lambdaQuery().eq(TransferDeclareCostAllocationEntity::getMainId, id).list().stream().map(TransferDeclareCostAllocationEntity::getId).collect(Collectors.toList());
		removeByIds(ids);
		transferDeclareCostAllocationDetailService.lambdaUpdate()
			.in(TransferDeclareCostAllocationDetailEntity::getMainId, ids)
			.set(TransferDeclareCostAllocationDetailEntity::getIsDeleted, true)
			.update();
		
		return BatchResultDTO.success(id, id, "删除成功");
	}

	@Override
	public BatchResultDTO pushBigTable(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean exportExcel(PagingParamDTO dto) {
		downloadTaskFeign.saveDownloadTask("中转费用分摊列表", FileTaskEventEnum.EXPORT_TRANSFER_DECLARE_COST_ALLOCATION.getCode(), dto);
        return Boolean.TRUE;
	}

    @Override
    public List<TransferDeclareCostAllocationEntity> listByReportPeriodStr(String reportPeriodStr, String reportStatus) {
		if (CharSequenceUtil.isBlank(reportPeriodStr)){
			return Collections.emptyList();
		}
        return baseMapper.listByReportPeriodStr(reportPeriodStr,reportStatus);
    }

    @Override
    public void pushAllocation(TmsAsyncTaskRecordDTO.PushParamsDTO dto) {
        if (transferDeclareService.addTaskDetailByTransferDeclare(dto)) return;
        transferDeclareService.pushTransferDeclare(dto);
    }

}
