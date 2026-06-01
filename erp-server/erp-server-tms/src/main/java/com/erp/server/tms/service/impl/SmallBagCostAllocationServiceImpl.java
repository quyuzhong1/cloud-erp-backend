package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
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
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
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
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
public class SmallBagCostAllocationServiceImpl extends SuperServiceImpl<SmallBagCostAllocationMapper, SmallBagCostAllocationEntity> implements SmallBagCostAllocationService {
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
    private MQProducerService mQProducerService;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private RedissonClient redissonClient;

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
		List<String> supplierIds = channelIdMaps.values().stream().map(LogisticsChannelEntity::getMainId).collect(Collectors.toList());
		Map<String, String> supplierIdNameMap = new HashMap<>();
		if(CollUtil.isNotEmpty(supplierIds)) {
			supplierIdNameMap = logisticsSupplierService.listByIds(supplierIds).stream().collect(Collectors.toMap(LogisticsSupplierEntity::getId, LogisticsSupplierEntity::getShortName));
		}
		
		Map<String, BigDecimal> rateMap = new HashMap<>();
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
		// 目标状态为待确认时，需排除物流大表已生成的记录（等价于 Service 兜底校验，提前过滤减少无效子任务）
		boolean excludeBigTableDone = SmallBagCostAllocationReportStatusEnum.TOBECONFIRM.getCode().equals(reportStatus);
		String bigTableDoneCode = SmallBagCostAllocationBigTableStatusEnum.DONE.getCode();

		int total = smallBagCostAllocationMainService.countMainByReportPeriodStr(dto.getReportPeriodStr(), reportStatus, excludeBigTableDone, bigTableDoneCode);
		if (total == 0) {
			throw new ServiceException("没有可更新核算状态的数据");
		}

		TmsAsyncTaskRecordDTO.PushParamsDTO params = new TmsAsyncTaskRecordDTO.PushParamsDTO();
		params.setBusinessType(SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode());
		params.setMethodType(TmsAsyncTaskMethodTypeEnum.UPDATE_REPORT_STATUS.getCode());
		params.setReportPeriodStr(dto.getReportPeriodStr());
		params.setReportStatus(reportStatus);
		params.setReportDate(dto.getReportDate());
		String jsonStr = JSONUtil.toJsonStr(params);

		// 创建任务时直接写入预期明细数量，返回任务实体
		TmsAsyncTaskRecordEntity taskRecord = asyncTaskRecordService.addManualTask(params.getBusinessType(), params.getMethodType(), total, jsonStr);
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
		if (!claimed) {
			throw new ServiceException(ApiError.LOGISTICS_ASYNC_TASK_CREATE_ERROR, jsonStr);
		}

		try {
			SendResult sendResult = mQProducerService.syncClassMsg(RocketMqTopic.TMS_ASYNC_TASK_RECORD_TOPIC, RocketMqNewTag.TMS_ASYNC_TASK_RECORD_TAG, params, taskId);
			if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
				log.error("小包核算状态变更 MQ消息发送失败：{}", sendResult);
				throw new ServiceException(ApiError.LOGISTICS_ASYNC_TASK_CREATE_ERROR, "MQ消息发送失败");
			}
			log.info("小包核算状态变更 MQ消息发送成功，taskId: {}, 预计处理数据量: {}", taskId, total);
		} catch (Exception e) {
			log.error("小包核算状态变更 MQ消息发送异常，taskId: {}", taskId, e);
			asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
				StringUtils.substring(e.getMessage(), 0, 1000));
			throw e;
		}

		return BatchResultDTO.success(taskId, taskRecord.getCode());
	}

	@Override
	public void pushUpdateReportStatus(TmsAsyncTaskRecordDTO.PushParamsDTO dto) {
		String taskId = dto.getTaskId();
		if (StringUtils.isBlank(taskId)) {
			log.error("小包核算状态变更异步任务ID为空");
			return;
		}

		RLock taskLock = redissonClient.getLock(DistributeKeyConstant.TMS_ASYNC_TASK_EXEC_KEY + ":" + taskId);
		boolean locked = false;
		try {
			// 同一 taskId 只允许一个消费者执行，防 MQ 重投并发重复执行
			locked = taskLock.tryLock(0, TimeUnit.SECONDS);
			if (!locked) {
				log.warn("小包核算状态变更异步任务正在执行，跳过重复消费，taskId: {}", taskId);
				return;
			}

			CfgSettingValueDTO.BillBatchParamsDTO billBatchParamsDTO = loadBillBatchParams(taskId);
			if (billBatchParamsDTO == null) {
				return;
			}

			// 1. 校验任务存在性与状态
			TmsAsyncTaskRecordEntity taskRecord = asyncTaskRecordService.getById(taskId);
			if (Objects.isNull(taskRecord)) {
				log.error("任务记录不存在，taskId: {}", taskId);
				return;
			}
			if (Objects.equals(taskRecord.getStatus(), TmsAsyncTaskRecordStatusEnum.FINISH.getCode())) {
				log.warn("小包核算状态变更异步任务已完成，跳过重复消费，taskId: {}", taskId);
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
					log.warn("小包核算状态变更异步任务已被其他消费者认领，taskId: {}", taskId);
					return;
				}
			} else if (!Objects.equals(taskRecord.getStatus(), TmsAsyncTaskRecordStatusEnum.ING.getCode())) {
				log.warn("小包核算状态变更异步任务状态不可执行，taskId: {}, status: {}", taskId, taskRecord.getStatus());
				return;
			}

			// 2. 初始化批次配置（轻量 UPDATE，复用小包批次大小）
			int batchSize = resolveSmallBagBatchSize(billBatchParamsDTO.getSmallBagBatch());
			String lastId = "";
			int totalProcessed = 0;
			int totalSuccess = 0;
			int totalFailed = 0;
			int batchNumber = 0;
			LocalDateTime taskStartTime = taskRecord.getStartTime();
			Integer taskExecTimeout = taskRecord.getExecTimeout();

			boolean excludeBigTableDone = SmallBagCostAllocationReportStatusEnum.TOBECONFIRM.getCode().equals(dto.getReportStatus());
			String bigTableDoneCode = SmallBagCostAllocationBigTableStatusEnum.DONE.getCode();

			log.info("开始分批处理核算状态变更任务，taskId: {}, 批次大小: {}, 预计总数: {}", taskId, batchSize, taskRecord.getDetailCount());

			// 3. 游标分批循环处理
			while (true) {
				batchNumber++;

				if (batchNumber == 1 || batchNumber % 10 == 0) {
					TmsAsyncTaskRecordEntity currentTask = asyncTaskRecordService.getById(taskId);
					if (Objects.equals(currentTask.getStatus(), TmsAsyncTaskRecordStatusEnum.FINISH.getCode())) {
						log.warn("循环过程中，任务状态显示已完成，taskId: {}", taskId);
						break;
					}
					taskExecTimeout = currentTask.getExecTimeout();
				}

				if (taskExecTimeout != null && taskExecTimeout > 0 && taskStartTime != null) {
					long elapsedSeconds = Duration.between(taskStartTime, LocalDateTime.now()).getSeconds();
					if (elapsedSeconds > taskExecTimeout) {
						log.error("任务执行超时，taskId: {}, 已耗时: {}秒", taskId, elapsedSeconds);
						asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), "任务执行超时，已耗时" + elapsedSeconds + "秒");
						break;
					}
				}

				List<String> batchIds;
				try {
					batchIds = smallBagCostAllocationMainService.pageMainIdsByReportPeriodStr(dto.getReportPeriodStr(), dto.getReportStatus(), excludeBigTableDone, bigTableDoneCode, lastId, batchSize);
				} catch (Exception e) {
					log.error("第{}批查询失败，taskId: {}", batchNumber, taskId, e);
					asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), "第" + batchNumber + "批查询失败: " + e.getMessage());
					break;
				}

				if (CollUtil.isEmpty(batchIds)) {
					log.info("所有数据处理完成，taskId: {}, 总批次: {}, 总处理: {}/成功: {}/失败: {}", taskId, batchNumber - 1, totalProcessed, totalSuccess, totalFailed);
					break;
				}

				int successCount = 0;
				int failedCount = 0;
				Map<String, SmallBagCostAllocationMainEntity> entityMap = smallBagCostAllocationMainService.listByIds(batchIds).stream()
					.collect(Collectors.toMap(SmallBagCostAllocationMainEntity::getId, Function.identity(), (a, b) -> a));
				for (String id : batchIds) {
					SmallBagCostAllocationMainEntity entity = entityMap.get(id);
					if (ObjectUtil.isEmpty(entity)) {
						failedCount++;
						log.error("小包核算状态变更失败，主表记录不存在，taskId: {}, id: {}", taskId, id);
						continue;
					}
					try {
						updateReportStatus(entity, dto.getReportDate(), dto.getReportStatus());
						successCount++;
					} catch (Exception e) {
						failedCount++;
						log.error("小包核算状态变更失败，taskId: {}, id: {}", taskId, id, e);
					}
				}

				totalProcessed += batchIds.size();
				totalSuccess += successCount;
				totalFailed += failedCount;

				try {
					asyncTaskRecordService.lambdaUpdate()
						.set(TmsAsyncTaskRecordEntity::getErrorCount, totalFailed)
						.eq(TmsAsyncTaskRecordEntity::getId, taskId)
						.update();
				} catch (Exception e) {
					log.error("更新任务进度失败，taskId: {}", taskId, e);
				}

				// 游标推进（取本批最后一条ID）
				lastId = batchIds.get(batchIds.size() - 1);
			}

			// 4. 最终更新任务状态
			try {
				asyncTaskRecordService.finishTaskOnMainRecord(taskId);
				log.info("核算状态变更任务最终状态更新完成，taskId: {}", taskId);
			} catch (Exception e) {
				log.error("更新任务最终状态失败，taskId: {}", taskId, e);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("小包核算状态变更异步任务获取锁被中断，taskId: {}", taskId, e);
		} catch (Exception e) {
			log.error("小包核算状态变更异步任务执行失败，taskId: {}", taskId, e);
			asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), StringUtils.substring(e.getMessage(), 0, 1000));
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
		String reportStatus = SmallBagCostAllocationMainReportStatusEnum.TOBECONFIRM.getCode();
		int total = smallBagCostAllocationMainService.countMainForReAllocation(dto.getReportPeriodStr(), reportStatus);
		if (total == 0) {
			throw new ServiceException("没有可重新分摊的数据");
		}

		TmsAsyncTaskRecordDTO.PushParamsDTO params = new TmsAsyncTaskRecordDTO.PushParamsDTO();
		params.setBusinessType(SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode());
		params.setMethodType(TmsAsyncTaskMethodTypeEnum.RE_ALLOCATION.getCode());
		params.setReportPeriodStr(dto.getReportPeriodStr());
		params.setReportStatus(reportStatus);
		String jsonStr = JSONUtil.toJsonStr(params);

		TmsAsyncTaskRecordEntity taskRecord = asyncTaskRecordService.addManualTask(params.getBusinessType(), params.getMethodType(), total, jsonStr);
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
		if (!claimed) {
			throw new ServiceException(ApiError.LOGISTICS_ASYNC_TASK_CREATE_ERROR, jsonStr);
		}

		try {
			SendResult sendResult = mQProducerService.syncClassMsg(RocketMqTopic.TMS_ASYNC_TASK_RECORD_TOPIC, RocketMqNewTag.TMS_ASYNC_TASK_RECORD_TAG, params, taskId);
			if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
				log.error("小包重新分摊 MQ消息发送失败：{}", sendResult);
				throw new ServiceException(ApiError.LOGISTICS_ASYNC_TASK_CREATE_ERROR, "MQ消息发送失败");
			}
			log.info("小包重新分摊 MQ消息发送成功，taskId: {}, 预计处理数据量: {}", taskId, total);
		} catch (Exception e) {
			log.error("小包重新分摊 MQ消息发送异常，taskId: {}", taskId, e);
			asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
				StringUtils.substring(e.getMessage(), 0, 1000));
			throw e;
		}

		return BatchResultDTO.success(taskId, taskRecord.getCode());
	}

	@Override
	public void pushReAllocation(TmsAsyncTaskRecordDTO.PushParamsDTO dto) {
		String taskId = dto.getTaskId();
		if (StringUtils.isBlank(taskId)) {
			log.error("小包重新分摊异步任务ID为空");
			return;
		}

		RLock taskLock = redissonClient.getLock(DistributeKeyConstant.TMS_ASYNC_TASK_EXEC_KEY + ":" + taskId);
		boolean locked = false;
		try {
			locked = taskLock.tryLock(0, TimeUnit.SECONDS);
			if (!locked) {
				log.warn("小包重新分摊异步任务正在执行，跳过重复消费，taskId: {}", taskId);
				return;
			}

			CfgSettingValueDTO.BillBatchParamsDTO billBatchParamsDTO = loadBillBatchParams(taskId);
			if (billBatchParamsDTO == null) {
				return;
			}

			TmsAsyncTaskRecordEntity taskRecord = asyncTaskRecordService.getById(taskId);
			if (Objects.isNull(taskRecord)) {
				log.error("任务记录不存在，taskId: {}", taskId);
				return;
			}
			if (Objects.equals(taskRecord.getStatus(), TmsAsyncTaskRecordStatusEnum.FINISH.getCode())) {
				log.warn("小包重新分摊异步任务已完成，跳过重复消费，taskId: {}", taskId);
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
					log.warn("小包重新分摊异步任务已被其他消费者认领，taskId: {}", taskId);
					return;
				}
			} else if (!Objects.equals(taskRecord.getStatus(), TmsAsyncTaskRecordStatusEnum.ING.getCode())) {
				log.warn("小包重新分摊异步任务状态不可执行，taskId: {}, status: {}", taskId, taskRecord.getStatus());
				return;
			}

			int batchSize = resolveSmallBagBatchSize(billBatchParamsDTO.getSmallBagBatch());
			String lastId = "";
			int totalProcessed = 0;
			int totalSuccess = 0;
			int totalFailed = 0;
			int batchNumber = 0;
			LocalDateTime taskStartTime = taskRecord.getStartTime();
			Integer taskExecTimeout = taskRecord.getExecTimeout();
			String reportStatus = SmallBagCostAllocationMainReportStatusEnum.TOBECONFIRM.getCode();

			log.info("开始分批处理小包重新分摊任务，taskId: {}, 批次大小: {}, 预计总数: {}", taskId, batchSize, taskRecord.getDetailCount());

			SmallBagCostAllocationService self = ApplicationContextUtils.getBean(SmallBagCostAllocationService.class);

			while (true) {
				batchNumber++;

				if (batchNumber == 1 || batchNumber % 10 == 0) {
					TmsAsyncTaskRecordEntity currentTask = asyncTaskRecordService.getById(taskId);
					if (Objects.equals(currentTask.getStatus(), TmsAsyncTaskRecordStatusEnum.FINISH.getCode())) {
						log.warn("循环过程中，任务状态显示已完成，taskId: {}", taskId);
						break;
					}
					taskExecTimeout = currentTask.getExecTimeout();
				}

				if (taskExecTimeout != null && taskExecTimeout > 0 && taskStartTime != null) {
					long elapsedSeconds = Duration.between(taskStartTime, LocalDateTime.now()).getSeconds();
					if (elapsedSeconds > taskExecTimeout) {
						log.error("任务执行超时，taskId: {}, 已耗时: {}秒", taskId, elapsedSeconds);
						asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), "任务执行超时，已耗时" + elapsedSeconds + "秒");
						break;
					}
				}

				List<String> batchIds;
				try {
					batchIds = smallBagCostAllocationMainService.pageMainIdsForReAllocation(dto.getReportPeriodStr(), reportStatus, lastId, batchSize);
				} catch (Exception e) {
					log.error("第{}批查询失败，taskId: {}", batchNumber, taskId, e);
					asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), "第" + batchNumber + "批查询失败: " + e.getMessage());
					break;
				}

				if (CollUtil.isEmpty(batchIds)) {
					log.info("所有数据处理完成，taskId: {}, 总批次: {}, 总处理: {}/成功: {}/失败: {}", taskId, batchNumber - 1, totalProcessed, totalSuccess, totalFailed);
					break;
				}

				int successCount = 0;
				int failedCount = 0;
				for (String id : batchIds) {
					try {
						self.reAllocation(id);
						successCount++;
					} catch (Exception e) {
						failedCount++;
						log.error("小包重新分摊失败，taskId: {}, id: {}", taskId, id, e);
					}
				}

				totalProcessed += batchIds.size();
				totalSuccess += successCount;
				totalFailed += failedCount;

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
				asyncTaskRecordService.finishTaskOnMainRecord(taskId);
				log.info("小包重新分摊任务最终状态更新完成，taskId: {}", taskId);
			} catch (Exception e) {
				log.error("更新任务最终状态失败，taskId: {}", taskId, e);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("小包重新分摊异步任务获取锁被中断，taskId: {}", taskId, e);
		} catch (Exception e) {
			log.error("小包重新分摊异步任务执行失败，taskId: {}", taskId, e);
			asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), StringUtils.substring(e.getMessage(), 0, 1000));
		} finally {
			if (locked && taskLock.isHeldByCurrentThread()) {
				taskLock.unlock();
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResultDTO reAllocation(String id) {
		SmallBagCostAllocationMainEntity smallBagCostAllocationMainEntity = smallBagCostAllocationMainService.getById(id);
		if (ObjectUtil.isEmpty(smallBagCostAllocationMainEntity)) {
			throw new ServiceException("小包分摊不存在");
		}
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

	private int resolveSmallBagBatchSize(String batchConfig) {
		int batchSize = NumberUtils.toInt(batchConfig, 500);
		return batchSize <= 0 ? 500 : batchSize;
	}

	private CfgSettingValueDTO.BillBatchParamsDTO loadBillBatchParams(String taskId) {
		CfgSettingEntity byKey = cfgSettingService.getByKey(CfgSettingEnum.BILL_BATCH_PARAMS.getCode());
		if (byKey == null || byKey.getDataJson() == null) {
			log.error("配置项 {} 不存在或 dataJson 为空，taskId: {}",
				CfgSettingEnum.BILL_BATCH_PARAMS.getCode(), taskId);
			asyncTaskRecordService.finishTaskWithError(taskId, "批次配置不存在");
			return null;
		}
		return JSON.parseObject(byKey.getDataJson().toJSONString(0), CfgSettingValueDTO.BillBatchParamsDTO.class);
	}
}
