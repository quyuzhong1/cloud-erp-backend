package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DataIdempotent;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.*;
import com.common.business.dto.base.BaseResultDTO.AddDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.DynamicDataSourceThreadLocal;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.message.constant.DistributeKeyConstant;
import com.common.core.utils.*;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.oms.entity.*;
import com.erp.model.plm.entity.ProductPackEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.*;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.dto.CfgSettingValueDTO.AllocationSettingDTO;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.dto.LogisticsBillCostDTO.*;
import com.erp.model.tms.dto.TmsCostDetailDTO.CostViewDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO.UpdateDTO;
import com.erp.model.tms.dto.excel.LogisticsBillCostExcelDTO;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.entity.DictBasicEntity;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.entity.DictBasicEntity;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.entity.*;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.sys.feign.UserInfoFeign;
import com.erp.server.tms.handler.asynctask.LogisticsSmallBagPushBatchPushHandlerFactory;
import com.erp.server.tms.handler.asynctask.LogisticsUpdateReconciliationBatchPushHandler;
import com.erp.server.tms.listener.LogisticsBillCostExcelListener;
import com.erp.server.tms.util.LogisticsBillPlatformCodeUtil;
import com.erp.server.tms.mapper.LogisticsBillCostMapper;
import com.erp.server.tms.query.LogisticsBillCostQueryHandler;
import com.erp.server.tms.query.LogisticsLastMileCostQueryHandler;
import com.erp.server.tms.handler.asynctask.LogisticsSmallBagPushBatchPushHandlerFactory;
import com.erp.server.tms.handler.asynctask.LogisticsUpdateReconciliationBatchPushHandler;
import com.erp.server.tms.service.*;
import com.erp.server.tms.service.asynctask.LogisticsBillCostAsyncTaskDelegate;
import com.erp.server.tms.service.support.LogisticsOrderWeightSupport;
import com.erp.server.tms.service.support.TmsAsyncTaskBatchConsumerSupport;
import com.erp.server.tms.util.LogisticsBillPlatformCodeUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_BILL_COST;
import static com.common.business.enums.FileTaskEventEnum.IMPORT_TMS_LOGISTICS_BILL_COST;

/**
 * <p>
 * 尾程费用(自发货) 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-11-06
 */
@Slf4j
@Service
public class LogisticsBillCostServiceImpl extends SuperServiceImpl<LogisticsBillCostMapper, LogisticsBillCostEntity>
    implements LogisticsBillCostService, LogisticsBillCostAsyncTaskDelegate {
    private static final int IMPORT_CONFIRM_BATCH_SIZE = 1000;
    private static final String LAST_MILE_FEE_ATTRIBUTION = DictCostAttributionEnum.LAST_MILE_DELIVERY.getCode();
    /**
     * 对账月份高级查询字段名。
     */
    private static final String RECONCILIATION_MONTH_QUERY_FIELD = "lbc.reconciliation_month";
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    @Lazy
    private LogisticsBillService logisticsBillService;

    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private UserInfoFeign userInfoFeign;

    @Resource
    private TmsCostDetailService tmsCostDetailService;

    @Resource
    private TmsCfgCostService tmsCfgCostService;

    @Resource
    @Lazy
    private LogisticsBillDetailService logisticsBillDetailService;

    @Resource
    private LogisticsBillCostQueryHandler logisticsBillCostQueryHandler;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private LogisticsLastMileCostQueryHandler logisticsLastMileCostQueryHandler;

    @Resource
    private LogisticsChannelService logisticsChannelService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Autowired
	protected IdentifierGenerator identifierGenerator;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private SmallBagCostAllocationMainService smallBagCostAllocationMainService;
    @Resource
    @Lazy
    private SmallBagCostAllocationService smallBagCostAllocationService;
    @Resource
    private SmallBagCostAllocationDetailService smallBagCostAllocationDetailService;
    @Resource
    private InventorySkuCostService inventorySkuCostService;
    @Resource
    private InventorySkuCostDetailService inventorySkuCostDetailService;
    @Resource
    @Lazy
    private LogisticsLargeService logisticsLargeService;
    @Resource
    private FileFeign fileFeign;
    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    @Resource
    @Lazy
    private LogisticsBillCostService service;
    @Resource
    @Lazy
    private LogisticsReconService logisticsReconService;
    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;
    @Resource
    private TmsAsyncTaskDetailService asyncTaskDetailRecordService;
    @Resource
    private TmsAsyncTaskBatchConsumerSupport tmsAsyncTaskBatchConsumerSupport;
    @Resource
    private LogisticsOrderWeightSupport logisticsOrderWeightSupport;

    @Resource
    private LogisticsUpdateReconciliationBatchPushHandler logisticsUpdateReconciliationBatchPushHandler;
    @Resource
    private LogisticsSmallBagPushBatchPushHandlerFactory logisticsSmallBagPushBatchPushHandlerFactory;
    @Autowired
    @Qualifier("costAllocationPool")
    private ExecutorService costAllocationPool;


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsBillCostDTO.AddDTO addDTO, Map<String, TmsCfgCostEntity> cfgCostCache) {
        LogisticsBillCostEntity logisticsBillCostEntity = new LogisticsBillCostEntity();
        BeanMapperUtils.copy(addDTO, logisticsBillCostEntity);

        logisticsBillCostEntity.setChannelId(addDTO.getChannelId());
        logisticsBillCostEntity.setDeptId(addDTO.getSalesDeptId());
        // 数据处理
        handleData(logisticsBillCostEntity);

        log.info("开始新增尾程费用(自发货)");
        boolean save = super.save(logisticsBillCostEntity);
        if(!save) {
            throw new ServiceException("尾程费用(自发货)保存失败");
        }
        //添加费用明细
        tmsCostDetailService.batchAdd(addDTO.getCostDetailList(),logisticsBillCostEntity.getId(), DictCostAttributionEnum.SELF_DELIVER);

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流费用" , logisticsBillCostEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), logisticsBillCostEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(logisticsBillCostEntity.getId(), logisticsBillCostEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.UpdateDTO update(LogisticsBillCostDTO.UpdateDTO updateDTO, Boolean isImport, Map<String, TmsCfgCostEntity> cfgCostCache) {
        LogisticsBillCostEntity old = null;
        if (Objects.nonNull(updateDTO.getId())){
            old = super.getById(updateDTO.getId());
        }
        String reconciliationStatus = old.getReconciliationStatus();
        if(old.getType().equals(DictCostAttributionEnum.SELF_DELIVER.getCode())
        		|| old.getType().equals(DictCostAttributionEnum.LAST_MILE.getCode())) {
        	if(reconciliationStatus.equals(ReconciliationStatusEnum.CONFIRMED.getCode())) {
        		throw new ServiceException("对账状态为账单确认，不能编辑");
        	}
        	boolean throwFlag = false;
        	if(reconciliationStatus.equals(ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode())) {
        		throwFlag = true;
        	}

    		BigDecimal billingWeight = old.getBillingWeight();
    		if(billingWeight == null) {
    			billingWeight = BigDecimal.ZERO;
    		}
    		BigDecimal updateBillingWeight = updateDTO.getBillingWeight();
    		if(updateBillingWeight == null) {
    			updateBillingWeight = billingWeight;
    		}
			if(throwFlag && billingWeight.compareTo(updateBillingWeight) != 0) {
				throw new ServiceException("核算状态为暂估确认，不能修改计费重[预估]");
    		}
			List<TmsCostDetailEntity> tmsCostDetailEntityList = tmsCostDetailService.lambdaQuery()
					.eq(TmsCostDetailEntity::getMainId, old.getId())
					.eq(TmsCostDetailEntity::getType, LogisticsBillCostTypeEnum.ESTIMATED.getCode())
					.list();
			List<TmsCostDetailDTO.UpdateDTO>  costDetailList = updateDTO.getCostDetailList();
			if(CollUtil.isNotEmpty(tmsCostDetailEntityList) && CollUtil.isNotEmpty(costDetailList)) {
				Map<String, UpdateDTO> cfgIdDtoMap = costDetailList.stream().filter(c -> LogisticsBillCostTypeEnum.ESTIMATED.getCode().equals(c.getType()))
						.collect(Collectors.toMap(UpdateDTO::getCfgCostId, t -> t));
				for(TmsCostDetailEntity tmsCostDetailEntity : tmsCostDetailEntityList) {
					UpdateDTO dbUpdateDto = cfgIdDtoMap.get(tmsCostDetailEntity.getCfgCostId());
					if(dbUpdateDto == null && isImport) {
						TmsCostDetailDTO.UpdateDTO dto = new TmsCostDetailDTO.UpdateDTO();
						dto.setCostValue(tmsCostDetailEntity.getCostValue());
						dto.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
						dto.setCfgCostId(tmsCostDetailEntity.getCfgCostId());
						dto.setSourceType(SourceTypeEnum.LOGISTICS_BILL_COST.getCode());
						costDetailList.add(dto);
						continue;
					}
					BigDecimal dbCostValue = tmsCostDetailEntity.getCostValue();
					if(dbCostValue == null) {
						dbCostValue = BigDecimal.ZERO;
					}
					BigDecimal costValue = BigDecimal.ZERO;
					if(dbUpdateDto != null && dbUpdateDto.getCostValue() != null) {
						costValue = dbUpdateDto.getCostValue();
					}

					if(throwFlag && dbCostValue.compareTo(costValue) != 0) {
						throw new ServiceException("核算状态为暂估确认，不能修改预估金额");
					}
					cfgIdDtoMap.remove(tmsCostDetailEntity.getCfgCostId());
				}
				if(throwFlag && !cfgIdDtoMap.isEmpty() && cfgIdDtoMap.values().stream().anyMatch(c -> c.getType().equals(LogisticsBillCostTypeEnum.ESTIMATED.getCode())
						&& c.getCostValue() != null && c.getCostValue().compareTo(BigDecimal.ZERO) != 0)) {
					throw new ServiceException("核算状态为暂估确认，不能新增预估金额");
				}
			}

        }
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "尾程费用(自发货)"));
        //赋值
        LogisticsBillCostEntity logisticsBillCostEntity =  BeanMapperUtils.map(LogisticsBillCostEntity.class, old);
        logisticsBillCostEntity.setBillingWeight(updateDTO.getBillingWeight());
        logisticsBillCostEntity.setBillingWeightLogistics(updateDTO.getBillingWeightLogistics());
        logisticsBillCostEntity.setRemark(updateDTO.getRemark());
        logisticsBillCostEntity.setCurrency(updateDTO.getCurrency());
        logisticsBillCostEntity.setPayType(CharSequenceUtil.blankToDefault(updateDTO.getPayType(), old.getPayType()));

        Optional.ofNullable(updateDTO.getActualWeight()).ifPresent(logisticsBillCostEntity::setActualWeight);
        Optional.ofNullable(updateDTO.getVolumeWeight()).ifPresent(logisticsBillCostEntity::setVolumeWeight);
        Optional.ofNullable(updateDTO.getVolumeWeightLogistics()).ifPresent(logisticsBillCostEntity::setVolumeWeightLogistics);
        Optional.ofNullable(updateDTO.getWeightLogistics()).ifPresent(logisticsBillCostEntity::setWeightLogistics);
        Optional.ofNullable(updateDTO.getLogisticsBillDetailId()).ifPresent(logisticsBillCostEntity::setLogisticsBillDetailId);

        Optional.ofNullable(updateDTO.getReconciliationMonth()).ifPresent(logisticsBillCostEntity::setReconciliationMonth);
        Optional.ofNullable(updateDTO.getThirdLength()).ifPresent(logisticsBillCostEntity::setThirdLength);
        Optional.ofNullable(updateDTO.getThirdWidth()).ifPresent(logisticsBillCostEntity::setThirdWidth);
        Optional.ofNullable(updateDTO.getThirdHeight()).ifPresent(logisticsBillCostEntity::setThirdHeight);
        Optional.ofNullable(updateDTO.getThirdActualWeight()).ifPresent(logisticsBillCostEntity::setThirdActualWeight);


        //物流单
        LogisticsBillEntity logisticsBillEntity = logisticsBillService.getById(logisticsBillCostEntity.getLogisticsBillId());
        if (ObjectUtil.isEmpty(logisticsBillEntity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE,"物流单");
        }
        logisticsBillCostEntity.setTransportNo(logisticsBillEntity.getTransportNo());
        logisticsBillCostEntity.setChannelId(logisticsBillEntity.getChannelId());
        // 数据处理
        handleData(logisticsBillCostEntity);
        log.info("编辑 开始修改尾程费用(自发货)数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsBillCostEntity);
        if(!save) {
            throw new ServiceException("尾程费用(自发货)保存失败：{}", JSONUtil.toJsonStr(logisticsBillCostEntity));
        }
        //更新费用明细
        tmsCostDetailService.batchUpdate(updateDTO.getCostDetailList(),logisticsBillCostEntity.getId(),DictCostAttributionEnum.SELF_DELIVER,isImport);

        // 记录主单操作日志
        log.info("编辑 开始记录尾程费用(自发货)日志数据，id：【{}】", logisticsBillCostEntity.getId());
        String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsBillCostEntity.getId(), "尾程费用(自发货)");
        operateLogService.addModuleOperateLogByObj(old, logisticsBillCostEntity, ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), logisticsBillCostEntity.getId(), msg);
        return new BaseResultDTO.UpdateDTO(logisticsBillCostEntity.getId(), logisticsBillCostEntity.getId());
    }

    @Override
    public List<LogisticsBillCostEntity> batchImportAdd(List<LogisticsBillEntity> logisticsBillList, List<LogisticsBillDetailEntity> logisticsBillDetailList,
                                                        List<LogisticsBillCostDTO.AddDTO> dtoList,String processingType) {
        if (CollUtil.isEmpty(dtoList)) {
            return Collections.emptyList();
        }
        List<LogisticsBillCostEntity> logisticsBillCostList = BeanUtil.copyToList(dtoList, LogisticsBillCostEntity.class);
        //导入数据批量处理
        handleImportData(logisticsBillList,logisticsBillDetailList,logisticsBillCostList);
        // saveOrUpdateBatch 对已存在 id 的行会走 UPDATE，与并发 batchImportUpdate 同源死锁；
        // 统一按 id 升序排序，消除"并发事务加锁顺序不一致"这一 PG 40P01 主因。
        logisticsBillCostList.sort(Comparator.nullsLast(Comparator.comparing(LogisticsBillCostEntity::getId, Comparator.nullsLast(String::compareTo))));
        log.info("开始新增尾程费用(自发货)");
        boolean save = super.saveOrUpdateBatch(logisticsBillCostList);
        if(!save) {
            throw new ServiceException("尾程费用(自发货)保存失败");
        }
        return logisticsBillCostList;
    }

    @Override
    public List<LogisticsBillCostEntity> batchImportUpdate(List<LogisticsBillEntity> logisticsBillList, List<LogisticsBillDetailEntity> logisticsBillDetailList,
                                                           List<LogisticsBillCostDTO.UpdateDTO> dtoList,String processingType) {
        if (CollUtil.isEmpty(dtoList)) {
            return Collections.emptyList();
        }
        List<LogisticsBillCostEntity> logisticsBillCostList = BeanUtil.copyToList(dtoList, LogisticsBillCostEntity.class);
        // updateBatchById 按 list 顺序逐条 UPDATE，并发事务 id 顺序不同必然死锁；
        // 提前按 id 升序排序，确保所有并发批次以一致顺序加锁。
        logisticsBillCostList.sort(Comparator.nullsLast(Comparator.comparing(LogisticsBillCostEntity::getId, Comparator.nullsLast(String::compareTo))));
        List<String> costIdList = logisticsBillCostList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getId())).map(LogisticsBillCostEntity::getId).distinct().collect(Collectors.toList());
        List<LogisticsBillCostEntity> oldLogisticsBillCostList = super.listByIds(costIdList);
        Map<String, LogisticsBillCostEntity> oldCostMap = CollUtil.isEmpty(oldLogisticsBillCostList) ? new HashMap<>() : oldLogisticsBillCostList.stream().collect(Collectors.toMap(LogisticsBillCostEntity::getId, obj -> obj));

        for (LogisticsBillCostEntity costEntity : logisticsBillCostList) {
            LogisticsBillCostEntity old = oldCostMap.get(costEntity.getId());
            String reconciliationStatus = old.getReconciliationStatus();
            if(old.getType().equals(DictCostAttributionEnum.SELF_DELIVER.getCode())
                    || old.getType().equals(DictCostAttributionEnum.LAST_MILE.getCode())) {
                if(reconciliationStatus.equals(ReconciliationStatusEnum.CONFIRMED.getCode())) {
                    throw new ServiceException("对账状态为账单确认，不能编辑");
                }
                boolean throwFlag = false;
                if(reconciliationStatus.equals(ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode())) {
                    throwFlag = true;
                }

                BigDecimal billingWeight = old.getBillingWeight();
                if(billingWeight == null) {
                    billingWeight = BigDecimal.ZERO;
                }
                BigDecimal updateBillingWeight = costEntity.getBillingWeight();
                if(updateBillingWeight == null) {
                    updateBillingWeight = billingWeight;
                }
                if(throwFlag && billingWeight.compareTo(updateBillingWeight) != 0) {
                    throw new ServiceException("核算状态为暂估确认，不能修改计费重[预估]");
                }
            }
            Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "尾程费用(自发货)"));
            //赋值
            LogisticsBillCostEntity logisticsBillCostEntity =  BeanMapperUtils.map(LogisticsBillCostEntity.class, old);
            logisticsBillCostEntity.setBillingWeight(costEntity.getBillingWeight());
            logisticsBillCostEntity.setBillingWeightLogistics(costEntity.getBillingWeightLogistics());
            logisticsBillCostEntity.setRemark(costEntity.getRemark());
            logisticsBillCostEntity.setCurrency(costEntity.getCurrency());
            logisticsBillCostEntity.setPayType(CharSequenceUtil.blankToDefault(costEntity.getPayType(), old.getPayType()));

            Optional.ofNullable(costEntity.getActualWeight()).ifPresent(logisticsBillCostEntity::setActualWeight);
            Optional.ofNullable(costEntity.getVolumeWeight()).ifPresent(logisticsBillCostEntity::setVolumeWeight);
            Optional.ofNullable(costEntity.getVolumeWeightLogistics()).ifPresent(logisticsBillCostEntity::setVolumeWeightLogistics);
            Optional.ofNullable(costEntity.getWeightLogistics()).ifPresent(logisticsBillCostEntity::setWeightLogistics);
            Optional.ofNullable(costEntity.getLogisticsBillDetailId()).ifPresent(logisticsBillCostEntity::setLogisticsBillDetailId);

            Optional.ofNullable(costEntity.getReconciliationMonth()).ifPresent(logisticsBillCostEntity::setReconciliationMonth);
            Optional.ofNullable(costEntity.getThirdLength()).ifPresent(logisticsBillCostEntity::setThirdLength);
            Optional.ofNullable(costEntity.getThirdWidth()).ifPresent(logisticsBillCostEntity::setThirdWidth);
            Optional.ofNullable(costEntity.getThirdHeight()).ifPresent(logisticsBillCostEntity::setThirdHeight);
            Optional.ofNullable(costEntity.getThirdActualWeight()).ifPresent(logisticsBillCostEntity::setThirdActualWeight);

        }
        //导入数据批量处理
        handleImportData(logisticsBillList,logisticsBillDetailList,logisticsBillCostList);

        //导入确认
        if (CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.CONFIRM_IMPORT.getCode(),processingType)) {
            //批量修改对账状态为已确认
            dtoList.forEach(obj -> obj.setImportConfirmDTO(new ImportHistoryRecordDTO.ImportConfirmDTO(obj.getId(),obj.getConfirmTime())));
        } else {
            logisticsBillCostList.forEach(obj -> obj.setConfirmTime(null));
        }

        log.info("编辑 开始修改尾程费用(自发货)数据");
        boolean save = super.updateBatchById(logisticsBillCostList);
        if(!save) {
            throw new ServiceException("尾程费用(自发货)保存失败：{}", JSONUtil.toJsonStr(logisticsBillCostList));
        }
        return logisticsBillCostList;
    }

    /**
     * 处理导入成功的数据
     * @author will
     * @date 2026/3/24 17:00
     * @param logisticsBillList 物流单数据列表
     * @param logisticsBillDetailList 物流单明细数据列表
     * @param logisticsBillCostList 导入的物流单费用数据列表
     */
    private void handleImportData(List<LogisticsBillEntity> logisticsBillList, List<LogisticsBillDetailEntity> logisticsBillDetailList,List<LogisticsBillCostEntity> logisticsBillCostList) {
        // 数据处理
        if (CollUtil.isEmpty(logisticsBillList) || CollUtil.isEmpty(logisticsBillDetailList) || CollUtil.isEmpty(logisticsBillCostList)) {
            return;
        }
        // 物流单转MAP，导入明细可能多行引用同一物流单，按id去重避免Collectors.toMap重复key异常
        Map<String, LogisticsBillEntity> logisticsBillMap = logisticsBillList.stream()
                .filter(Objects::nonNull)
                .filter(obj -> CharSequenceUtil.isNotBlank(obj.getId()))
                .collect(Collectors.toMap(LogisticsBillEntity::getId, Function.identity(), (first, second) -> first));
        //物流明细转MAP
        Map<String, List<LogisticsBillDetailEntity>> logisticsBillDetailMap = logisticsBillDetailList.stream().collect(Collectors.groupingBy(LogisticsBillDetailEntity::getMainId));
        // b2b销售订单id列表
        List<String> soIdList = logisticsBillList.stream().filter(obj -> CharSequenceUtil.equals(OrderTypeEnum.B2B.getCode(), obj.getOrderType())).map(LogisticsBillEntity::getSourceId).distinct().collect(Collectors.toList());
        List<SoInfoEntity> soInfoEntityList = FeignQuery.getByIds(SoInfoEntity.class, soIdList);
        Map<String, SoInfoEntity> soMap = CollUtil.isEmpty(soInfoEntityList) ? new HashMap<>() : soInfoEntityList.stream().collect(Collectors.toMap(SoInfoEntity::getId, e -> e));

        //b2c销售订单id列表
        List<String> soB2cIdList = logisticsBillList.stream().filter(obj -> CharSequenceUtil.equals(OrderTypeEnum.B2C.getCode(), obj.getOrderType())).map(LogisticsBillEntity::getSourceId).distinct().collect(Collectors.toList());
        List<SoB2cReceiverEntity> b2cReceiverList = FeignQuery.create(SoB2cReceiverEntity.class).in(SoB2cReceiverEntity::getMainId, soB2cIdList).list();
        Map<String, SoB2cReceiverEntity> soB2cReceiverMap = CollUtil.isEmpty(b2cReceiverList) ? new HashMap<>() : b2cReceiverList.stream().collect(Collectors.toMap(SoB2cReceiverEntity::getMainId, e -> e));


        List<SoB2cLogisticsEntity> soB2cLogisticsList = FeignQuery.create(SoB2cLogisticsEntity.class).in(SoB2cReceiverEntity::getMainId, soB2cIdList).list();
        Map<String, SoB2cLogisticsEntity> soB2cLogisticsMap = CollUtil.isEmpty(soB2cLogisticsList) ? new HashMap<>() : soB2cLogisticsList.stream().collect(Collectors.toMap(SoB2cLogisticsEntity::getMainId, e -> e));


        //销售出库单id列表
        List<String> outstockIdList = logisticsBillList.stream()
                .map(LogisticsBillEntity::getOutstockId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SoOutstockEntity> soOutstockEntityList = FeignQuery.getByIds(SoOutstockEntity.class,outstockIdList);
        Map<String, SoOutstockEntity> soOutstockMap = CollUtil.isEmpty(soOutstockEntityList) ? new HashMap<>() : soOutstockEntityList.stream().collect(Collectors.toMap(SoOutstockEntity::getId, e -> e));

        //店铺id列表
        List<String> shopIdList = logisticsBillList.stream().map(LogisticsBillEntity::getShopId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = FeignQuery.getByIds(ShopInfoEntity.class, shopIdList);
        Map<String, ShopInfoEntity> shopMap = CollUtil.isEmpty(shopInfoEntityList) ? new HashMap<>() : shopInfoEntityList.stream().collect(Collectors.toMap(ShopInfoEntity::getId, e -> e));

        //国家信息
        List<DictCountryEntity> countryList = FeignQuery.list(DictCountryEntity.class);
        Map<String, DictCountryEntity> countryMap = CollUtil.isEmpty(countryList) ? new HashMap<>() : countryList.stream().collect(Collectors.toMap(DictCountryEntity::getId, e -> e));

        //渠道id列表
        List<String> channelIdList = logisticsBillList.stream().map(LogisticsBillEntity::getChannelId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<LogisticsChannelEntity> channelList = FeignQuery.getByIds(LogisticsChannelEntity.class, channelIdList);
        Map<String, LogisticsChannelEntity> channelMap = CollUtil.isEmpty(channelList) ? new HashMap<>() : channelList.stream().collect(Collectors.toMap(LogisticsChannelEntity::getId, e -> e));

        //批量查询汇率
        List<String> currencyList = logisticsBillCostList.stream().map(LogisticsBillCostEntity::getCurrency).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        Map<String, BigDecimal> exchangeRateMap = new HashMap<>();
        if (CollUtil.isNotEmpty(currencyList)) {
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            for (String currency : currencyList) {
                if (CurrencyEnum.CNY.getCurrencyCode().equals(currency)) {
                    exchangeRateMap.put(currency, BigDecimal.ONE);
                } else {
                    BigDecimal rate = dmpTaskFeign.getRate(currentDate, currency);
                    if (Objects.isNull(rate)) {
                        throw new ServiceException(ApiError.COMMON_EXCHANGE_RATE_NOT_EXIST, LocalDate.now(), currency);
                    }
                    exchangeRateMap.put(currency, rate);
                }
            }
        }

        for  (LogisticsBillCostEntity entity : logisticsBillCostList) {
            //物流单号
            LogisticsBillEntity logisticsBillEntity = logisticsBillMap.get(entity.getLogisticsBillId());
            if (ObjectUtil.isEmpty(logisticsBillEntity)) {
                throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC,"物流订单");
            }
            entity.setTransportNo(logisticsBillEntity.getTransportNo());
            entity.setChannelId(logisticsBillEntity.getChannelId());

            //查询销售订单
            if (CharSequenceUtil.equals(OrderTypeEnum.B2B.getCode(),logisticsBillEntity.getOrderType())) {
                SoInfoEntity soInfoEntity = soMap.get(logisticsBillEntity.getSourceId());
                if (ObjectUtil.isNotEmpty(soInfoEntity)) {
                    if (CharSequenceUtil.isNotBlank(soInfoEntity.getCountryId())) {
                        // 查询国家区域
                        DictCountryEntity dictCountryEntity = countryMap.get( soInfoEntity.getCountryId());
                        entity.setSubregionCode(ObjectUtil.isEmpty(dictCountryEntity) ? "" : dictCountryEntity.getRegionCode());
                    }
                    entity.setDeptId(soInfoEntity.getSalesDeptId());
                    entity.setPartitionId(soInfoEntity.getPartitionId());
                }
            } else if (CharSequenceUtil.equals(OrderTypeEnum.B2C.getCode(),logisticsBillEntity.getOrderType())) {
                SoB2cReceiverEntity receiverEntity = soB2cReceiverMap.get(logisticsBillEntity.getSourceId()) ;
                if (ObjectUtil.isNotEmpty(receiverEntity)) {
                    if (CharSequenceUtil.isNotBlank(receiverEntity.getCountry())) {
                        // 查询国家区域
                        DictCountryEntity dictCountryEntity =  countryMap.get( receiverEntity.getCountry());
                        entity.setSubregionCode(ObjectUtil.isEmpty(dictCountryEntity) ? "" : dictCountryEntity.getRegionCode());
                    }
                    entity.setPartitionId(receiverEntity.getPartitionId());
                }
                //查询销售出库单
                if (CharSequenceUtil.isNotBlank(logisticsBillEntity.getOutstockId())) {
                    SoOutstockEntity soOutstockEntity = soOutstockMap.get(logisticsBillEntity.getOutstockId()) ;
                    if (ObjectUtil.isNotEmpty(soOutstockEntity)) {
                        entity.setDeptId(soOutstockEntity.getSalesDeptId());
                    }
                }
            }

            BigDecimal billingWeight = entity.getBillingWeight();
            if(StringUtils.isBlank(entity.getId()) && billingWeight == null) {
                billingWeight = MathUtil.compareTo(entity.getActualWeight(),entity.getVolumeWeight()) > MathUtil.ZERO
                        ? entity.getActualWeight() : entity.getVolumeWeight();
            }
            //计费重
            entity.setBillingWeight(billingWeight);

            //默认kg
            entity.setWeightUnit(UnitEnum.WeightUnitEnum.KG.getCode());

            //店铺负责人
            ShopInfoEntity shopInfoEntity = shopMap.get(logisticsBillEntity.getShopId()) ;
            if (ObjectUtil.isNotEmpty(shopInfoEntity)) {
                entity.setShopChargeId(shopInfoEntity.getChargeId());
                entity.setShopChargeName(shopInfoEntity.getChargeName());
            }

            //判断物流费用类型
            String type;
            if (CharSequenceUtil.equals(logisticsBillEntity.getOrderType(), OrderTypeEnum.FIRST_MILE.getCode())) {
                type = DictCostAttributionEnum.FIRST_MILE.getCode();
            } else {
                type = CharSequenceUtil.equals(logisticsBillEntity.getShipmentType(), ShipmentTypeEnum.SELF_DELIVER.getCode())
                        ? DictCostAttributionEnum.SELF_DELIVER.getCode() : DictCostAttributionEnum.LAST_MILE.getCode();
            }
            entity.setType(type);
            if (Objects.equals(DictCostAttributionEnum.SELF_DELIVER.getCode(), entity.getType()) || Objects.equals(DictCostAttributionEnum.FIRST_MILE.getCode(), entity.getType())){
                //根据渠道设置计费规则
                if (CharSequenceUtil.isNotBlank(entity.getChannelId())){
                    LogisticsChannelEntity channelEntity = channelMap.get(entity.getChannelId());
                    entity.setFeeRule(Objects.nonNull(channelEntity)? channelEntity.getFeeRule() : "");
                }
            }else if (Objects.equals(DictCostAttributionEnum.LAST_MILE.getCode(), entity.getType())){
                entity.setFeeRule(ShippingFeeRuleEnum.BILLING_WEIGHT.getCode());
            }
            if (CharSequenceUtil.isBlank(entity.getLogisticsBillDetailId())){
                List<LogisticsBillDetailEntity> logisticsBillDetailEntityList = logisticsBillDetailMap.get(logisticsBillEntity.getId());
                if (CollectionUtils.isNotEmpty(logisticsBillDetailEntityList)){
                    //现在正常情况下物流主表和物流明细是1：1关系
                    entity.setLogisticsBillDetailId(logisticsBillDetailEntityList.get(0).getId());
                }
            }
            //币别汇率
            if (CharSequenceUtil.isNotBlank(entity.getCurrency())){
                BigDecimal rate = exchangeRateMap.get(entity.getCurrency());
                if (Objects.isNull(rate)){
                    throw new ServiceException(ApiError.COMMON_EXCHANGE_RATE_NOT_EXIST, LocalDate.now(), entity.getCurrency());
                }
                entity.setExchangeRate(rate);
            }
            String sourceId = logisticsBillEntity.getSourceId();
            if(StringUtils.isNotBlank(sourceId)) {
                SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsMap.get(sourceId);
                if(ObjectUtil.isNotEmpty(soB2cLogisticsEntity)) {
                    BigDecimal length = soB2cLogisticsEntity.getLength();
                    if(length != null) {
                        length = length.setScale(1, RoundingMode.HALF_UP);
                    }
                    BigDecimal width = soB2cLogisticsEntity.getWidth();
                    if(width != null) {
                        width = width.setScale(1, RoundingMode.HALF_UP);
                    }
                    BigDecimal height = soB2cLogisticsEntity.getHeight();
                    if(height != null) {
                        height = height.setScale(1, RoundingMode.HALF_UP);
                    }
                    entity.setVolume(length + "*" + width + "*" + height);
                }
            }
        }
    }

    @Override
    public List<LogisticsBillCostDTO.TabListDTO> tabList(PermissionsDTO dto, DictCostAttributionEnum attribution) {
        LogisticsBillCostDTO.PagingParamDTO pagingParamDTO = new LogisticsBillCostDTO.PagingParamDTO();
        pagingParamDTO.setPermissionSql(dto.getPermissionSql());
        List<LogisticsBillCostDTO.TabCountDTO> tabCountDTOS = this.baseMapper.listCount(pagingParamDTO,attribution.getCode());
        //根据实体的三个字段拼接汇总数量
        Map<String, Integer> map = tabCountDTOS.stream().collect(Collectors.toMap(
                tab -> tab.getReconciliationStatus() + "_" + tab.getPayType(),
                LogisticsBillCostDTO.TabCountDTO::getCount,
                Integer::sum
        ));
        List<LogisticsBillCostDTO.TabListDTO> resultList = new ArrayList<>();
        ReconciliationTabStatusEnum[] values = ReconciliationTabStatusEnum.values();
        for (ReconciliationTabStatusEnum statusEnum : values) {
            LogisticsBillCostDTO.TabListDTO resultDTO = new LogisticsBillCostDTO.TabListDTO();
            Integer count = getCountByMap(statusEnum,map);
            resultDTO.setCount(count);
            resultDTO.setTabFlag(statusEnum.getCode());
            resultDTO.setTabFlagName(statusEnum.getName());
            resultList.add(resultDTO);
        }

        return resultList;
    }

    private Integer getCountByMap(ReconciliationTabStatusEnum statusEnum, Map<String, Integer> map) {
        Integer count = 0;
        if (ReconciliationTabStatusEnum.PAY_CONFIRM.getCode().equals(statusEnum.getCode())) {
            String key = ReconciliationStatusEnum.TO_BE_CONFIRM.getCode() + "_" + LogisticsBillCostPayTypeEnum.PAY.getCode();
            count = map.getOrDefault(key, 0);
        }else if (ReconciliationTabStatusEnum.PAY_CONFIRMED.getCode().equals(statusEnum.getCode())) {
            String key1 = ReconciliationStatusEnum.CONFIRMED.getCode() + "_" + LogisticsBillCostPayTypeEnum.PAY.getCode();
            Integer count1 = map.getOrDefault(key1, 0);
            String key2 = ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode() + "_" + LogisticsBillCostPayTypeEnum.PAY.getCode();
            Integer count2 = map.getOrDefault(key2, 0);
            count = count1 + count2;
        }else if (ReconciliationTabStatusEnum.REFUND_CONFIRM.getCode().equals(statusEnum.getCode())) {
            String key = ReconciliationStatusEnum.TO_BE_CONFIRM.getCode() + "_" + LogisticsBillCostPayTypeEnum.REFUND.getCode();
            count = map.getOrDefault(key, 0);
        }else if (ReconciliationTabStatusEnum.REFUND_CONFIRMED.getCode().equals(statusEnum.getCode())) {
            String key1 = ReconciliationStatusEnum.CONFIRMED.getCode() + "_" + LogisticsBillCostPayTypeEnum.REFUND.getCode();
            Integer count1 = map.getOrDefault(key1, 0);
            String key2 = ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode() + "_" + LogisticsBillCostPayTypeEnum.REFUND.getCode();
            Integer count2 = map.getOrDefault(key2, 0);
            count = count1 + count2;
        }
        return count;
    }

    @Override
    public PagingVO<LogisticsBillCostDTO.ListDTO> paging(PagingDTO<LogisticsBillCostDTO.PagingParamDTO> pagingDTO) {
        LogisticsBillCostDTO.PagingParamDTO params = pagingDTO.getParams();
        DynamicDataSourceTypeEnum dynamicDataSourceTypeEnum = DynamicDataSourceThreadLocal.get();
        if(dynamicDataSourceTypeEnum == null) {
            dynamicDataSourceTypeEnum = DynamicDataSourceTypeEnum.POSTGRES;
        }
        params.setDynamicDataSource(dynamicDataSourceTypeEnum.getCode());
        params.setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<LogisticsBillCostDTO.ListDTO> pageData = this.baseMapper.paging(query, params);
        List<LogisticsBillCostDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据赋值处理
        handleDataPaging(records);
        return new PagingVO(pageData);
    }

    @Override
    public BatchResultDTO updateReconciliationStatus(String id, String reconciliationStatus , LocalDateTime confirmTime) {
        if(org.apache.commons.lang3.StringUtils.isBlank(reconciliationStatus)) {
        	throw new ServiceException("对账状态不能为空");
        }
        boolean confirmFlag = (ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode().equals(reconciliationStatus)
    			|| ReconciliationStatusEnum.CONFIRMED.getCode().equals(reconciliationStatus));
    	if(confirmFlag && confirmTime == null) {
    		throw new ServiceException("对账状态修改为" + reconciliationStatus + "时，对账确认时间不能为空");
        }
    	LogisticsBillCostEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "尾程费用(自发货)"));
        //自发货/尾程费用：状态变更【已确认/已作废】可以修改为其他状态【待确认/已确认】【现有功能优化】
//        if (ReconciliationStatusEnum.INVALID.getCode().equals(entity.getReconciliationStatus()) || ReconciliationStatusEnum.CONFIRMED.getCode().equals(entity.getReconciliationStatus())) {
//            throw new ServiceException(ApiError.ERROR_LOGISTICS_BILL_COST_RECONCILIATION_STATUS);
//        }
        String beforeReconciliationStatus = entity.getReconciliationStatus();
        if(beforeReconciliationStatus.equals(reconciliationStatus)) {
        	throw new ServiceException("修改后对账状态不能和当前对账状态一样");
        }
        if(ReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equals(beforeReconciliationStatus)) {
        	if(!ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode().equals(reconciliationStatus)
        			&& !ReconciliationStatusEnum.CONFIRMED.getCode().equals(reconciliationStatus)
        			&& !ReconciliationStatusEnum.INVALID.getCode().equals(reconciliationStatus)) {
        		throw new ServiceException("当前对账状态为待确认，只能修改为暂估确认/账单确认/作废");
        	}
        }else if(ReconciliationStatusEnum.INVALID.getCode().equals(beforeReconciliationStatus)) {
        	if(!confirmFlag) {
        		throw new ServiceException("当前对账状态为已作废，只能修改为暂估确认/账单确认");
        	}
        }else if(ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode().equals(beforeReconciliationStatus)) {
        	if(ReconciliationStatusEnum.CONFIRMED.getCode().equals(reconciliationStatus)) {
        		if(LogisticsBillCostCheckStatusEnum.CHECKED.getCode().equals(entity.getCheckStatus())) {
        			throw new ServiceException("当前对账状态为暂估确认，核算状态为已生成不能修改为账单确认");
        		}
        	}else {
        		if(!(LogisticsBillCostCheckStatusEnum.CHECKING.getCode().equals(entity.getCheckStatus())
            			&& LogisticsBillCostPayStateEnum.PAYMENT.getCode().equals(entity.getPayStatus()))) {
            		String p = entity.getPayType().equals("pay") ? "付" : "退";
            		throw new ServiceException("当前对账状态为暂估确认，只有核算状态为待生成且支付状态为待" + p + "款时才能修改为非账单确认状态");
            	}
        	}
        }else if(ReconciliationStatusEnum.CONFIRMED.getCode().equals(beforeReconciliationStatus)) {
        	if(!(LogisticsBillCostCheckStatusEnum.CHECKING.getCode().equals(entity.getCheckStatus())
        			&& LogisticsBillCostPayStateEnum.PAYMENT.getCode().equals(entity.getPayStatus()))) {
        		String p = entity.getPayType().equals("pay") ? "付" : "退";
        		throw new ServiceException("当前对账状态为账单确认，只有核算状态为待生成且支付状态为待" + p + "款时才能修改为其他状态");
        	}
        }

        if("refund".equals(entity.getPayType()) && ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode().equals(reconciliationStatus)) {
        	throw new ServiceException("退款费用类型不能修改为暂估确认");
        }
        validateConfirmAmount(Collections.singletonList(id), reconciliationStatus);

        //状态变更
        lambdaUpdate().eq(LogisticsBillCostEntity::getId, id)
        		.set(LogisticsBillCostEntity::getReconciliationStatus, reconciliationStatus)
                .set(confirmFlag,LogisticsBillCostEntity::getConfirmTime, confirmTime)
                .set(confirmFlag,LogisticsBillCostEntity::getConfirmUserId, UserContext.getDefaultLoginUser().getUid())
                .set(confirmFlag,LogisticsBillCostEntity::getConfirmUserName, UserContext.getDefaultLoginUser().getUserName())
                .set(!confirmFlag,LogisticsBillCostEntity::getConfirmTime, null)
                .set(!confirmFlag,LogisticsBillCostEntity::getConfirmUserId, "")
                .set(!confirmFlag,LogisticsBillCostEntity::getConfirmUserName, "")
                .update();
        // 费用单对账状态变更后，反向同步对账单 ref 快照与 detail_sub 聚合状态
        syncReconStatusQuietly(Collections.singletonList(id));
        // 状态变更日志
        log.info("状态变更日志数据，id集合：【{}】", id);
        String msg = CharSequenceUtil.format("状态更新为【{}】 ",  ReconciliationStatusEnum.getName(reconciliationStatus));
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), entity.getTransportNo(), "状态更新");
        return BatchResultDTO.success(entity.getId(), entity.getTrackNo(), OperationTypeEnum.UPDATE_STATUS);
    }

    private static final int RECONCILIATION_STATUS_BATCH_SIZE = 1000;

    @Override
    public int batchUpdateReconciliationStatus(List<String> ids, String reconciliationStatus, LocalDateTime confirmTime) {
        return batchUpdateReconciliationStatus(ids, reconciliationStatus, confirmTime, false);
    }

    @Override
    public int batchUpdateReconciliationStatus(List<String> ids, String reconciliationStatus, LocalDateTime confirmTime,
                                               boolean skipSync) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(reconciliationStatus)) {
            throw new ServiceException("对账状态不能为空");
        }
        boolean confirmFlag = ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode().equals(reconciliationStatus)
                || ReconciliationStatusEnum.CONFIRMED.getCode().equals(reconciliationStatus);
        if (confirmFlag && confirmTime == null) {
            throw new ServiceException("对账状态修改为" + reconciliationStatus + "时，对账确认时间不能为空");
        }
        List<String> distinctIds = ids.stream()
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(distinctIds)) {
            return 0;
        }
        String confirmUserId = UserContext.getDefaultLoginUser().getUid();
        String confirmUserName = UserContext.getDefaultLoginUser().getUserName();
        int totalUpdated = 0;
        for (int i = 0; i < distinctIds.size(); i += RECONCILIATION_STATUS_BATCH_SIZE) {
            List<String> batch = distinctIds.subList(i,
                    Math.min(distinctIds.size(), i + RECONCILIATION_STATUS_BATCH_SIZE));
            validateConfirmAmount(batch, reconciliationStatus);
            LambdaUpdateChainWrapper<LogisticsBillCostEntity> updateChain = lambdaUpdate()
                    .in(LogisticsBillCostEntity::getId, batch);
            if (ReconciliationStatusEnum.CONFIRMED.getCode().equals(reconciliationStatus)) {
                updateChain.eq(LogisticsBillCostEntity::getReconciliationStatus,
                        ReconciliationStatusEnum.TO_BE_CONFIRM.getCode());
            } else if (ReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equals(reconciliationStatus)) {
                updateChain.eq(LogisticsBillCostEntity::getReconciliationStatus,
                        ReconciliationStatusEnum.CONFIRMED.getCode())
                        .eq(LogisticsBillCostEntity::getCheckStatus, LogisticsBillCostCheckStatusEnum.CHECKING.getCode())
                        .eq(LogisticsBillCostEntity::getPayStatus, LogisticsBillCostPayStateEnum.PAYMENT.getCode());
            }
            updateChain
                    .set(LogisticsBillCostEntity::getReconciliationStatus, reconciliationStatus)
                    .set(confirmFlag, LogisticsBillCostEntity::getConfirmTime, confirmTime)
                    .set(confirmFlag, LogisticsBillCostEntity::getConfirmUserId, confirmUserId)
                    .set(confirmFlag, LogisticsBillCostEntity::getConfirmUserName, confirmUserName)
                    .set(!confirmFlag, LogisticsBillCostEntity::getConfirmTime, null)
                    .set(!confirmFlag, LogisticsBillCostEntity::getConfirmUserId, "")
                    .set(!confirmFlag, LogisticsBillCostEntity::getConfirmUserName, "");
            totalUpdated += getBaseMapper().update(null, updateChain.getWrapper());
        }
        // 费用单对账状态变更后，反向同步对账单 ref 快照与 detail_sub 聚合状态（幂等，重复刷新结果一致）；
        // skipSync=true 时由调用方在批处理结束后统一同步一次，避免逐批全单刷新导致的 O(n^2) 开销。
        if (!skipSync) {
            syncReconStatusQuietly(distinctIds);
        }
        log.info("批量更新对账状态完成，更新={}，期望={}，状态={}，skipSync={}", totalUpdated, distinctIds.size(),
                reconciliationStatus, skipSync);
        return totalUpdated;
    }

    /**
     * 反向同步对账单状态（失败仅告警，不影响费用单状态主流程）。
     */
    private void syncReconStatusQuietly(java.util.Collection<String> logisticsBillCostIds) {
        if (CollUtil.isEmpty(logisticsBillCostIds)) {
            return;
        }
        try {
            logisticsReconService.syncReconStatusByCostIds(logisticsBillCostIds);
        } catch (Exception e) {
            log.warn("[syncReconStatusQuietly] 对账单状态反向同步失败 costIds={}", logisticsBillCostIds, e);
        }
    }

    /**
     * 导入确认前校验目标费用单合并导入明细后的费用分类确认金额是否不全部为 0。
     * <p>仅在对账状态为账单确认（CONFIRMED）或暂估确认（ESTIMATE_CONFIRM）时生效；
     * 账单确认校验实际金额，暂估确认校验暂估金额。供物流商模板导入（confirmImport）及标准导入勾选确认场景使用，
     * 返回错误文案供行级收集，不抛异常。</p>
     * <p>业务规则：只校验当前费用单中实际存在费用明细的费用分类；没有费用明细的费用分类不参与校验。
     * 只要任一存在的费用分类金额不等于 0 即通过，只有全部存在分类金额均等于 0 才返回错误。</p>
     *
     * @param logisticsCostId      目标物流费用单 ID
     * @param importList           本次导入待合并的费用明细，可为 null（仅校验库内已有明细）
     * @param reconciliationStatus 目标对账状态，非确认类状态直接返回 null
     * @return 不满足时返回错误文案，否则返回 null
     */
    @Override
    public String validateImportConfirmAmountMsg(String logisticsCostId, List<TmsCostDetailDTO.UpdateDTO> importList, String reconciliationStatus) {
        return validateImportConfirmAmountMsg(logisticsCostId, importList, reconciliationStatus, null);
    }

    /**
     * 导入确认前校验目标费用单合并导入明细后的费用分类确认金额是否不全部为 0。
     * <p>传入 {@code existingDetailMap} 时复用预查询明细，避免导入批量处理场景按单据循环查库；
     * 未传入时方法内部按目标费用单查询库内明细。</p>
     *
     * @param logisticsCostId      目标物流费用单 ID
     * @param importList           本次导入待合并的费用明细，可为 null
     * @param reconciliationStatus 目标对账状态，非确认类状态直接返回 null
     * @param existingDetailMap    预查询的费用明细，key 为费用单 ID；可为 null
     * @return 全部存在分类金额均为 0 时返回错误文案，否则返回 null
     */
    @Override
    public String validateImportConfirmAmountMsg(String logisticsCostId, List<TmsCostDetailDTO.UpdateDTO> importList,
                                                 String reconciliationStatus,
                                                 Map<String, List<TmsCostDetailEntity>> existingDetailMap) {
        // 非确认类状态不进入后续确认流程，无需校验确认金额。
        if (!ReconciliationStatusEnum.CONFIRMED.getCode().equals(reconciliationStatus)
                && !ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode().equals(reconciliationStatus)) {
            return null;
        }
        if (CharSequenceUtil.isBlank(logisticsCostId)) {
            return null;
        }
        String costType = ReconciliationStatusEnum.CONFIRMED.getCode().equals(reconciliationStatus)
                ? LogisticsBillCostTypeEnum.ACTUAL.getCode()
                : LogisticsBillCostTypeEnum.ESTIMATED.getCode();
        ApiError confirmAmountError = validateProjectedConfirmAmountByCategory(
                logisticsCostId, importList, costType, existingDetailMap, reconciliationStatus);
        return buildConfirmAmountCategoryMsg(confirmAmountError);
    }

    /**
     * 状态变更确认前校验目标状态对应费用分类金额是否不全部为 0。
     * <p>该方法用于同步 ID 分支，也会被异步任务明细执行链路复用；
     * 校验失败时抛出 {@link ServiceException}，由同步批量结果或异步明细失败原因承接。</p>
     *
     * @param logisticsCostIdList  待变更状态的物流费用单 ID 集合
     * @param reconciliationStatus 目标对账状态，非确认类状态直接跳过
     */
    private void validateConfirmAmount(List<String> logisticsCostIdList, String reconciliationStatus) {
        // 只有账单确认和暂估确认会进入确认金额校验。
        if (!ReconciliationStatusEnum.CONFIRMED.getCode().equals(reconciliationStatus)
                && !ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode().equals(reconciliationStatus)) {
            return;
        }
        if (CollUtil.isEmpty(logisticsCostIdList)) {
            return;
        }
        String costType = ReconciliationStatusEnum.CONFIRMED.getCode().equals(reconciliationStatus)
                ? LogisticsBillCostTypeEnum.ACTUAL.getCode()
                : LogisticsBillCostTypeEnum.ESTIMATED.getCode();
        // 批量预取目标类型明细，避免逐单据重复查询。
        List<TmsCostDetailEntity> detailList = tmsCostDetailService.lambdaQuery()
                .in(TmsCostDetailEntity::getMainId, logisticsCostIdList)
                .eq(TmsCostDetailEntity::getType, costType)
                .list();
        Map<String, List<TmsCostDetailEntity>> existingDetailMap = CollUtil.isEmpty(detailList)
                ? Collections.emptyMap()
                : detailList.stream().collect(Collectors.groupingBy(TmsCostDetailEntity::getMainId));
        for (String logisticsCostId : logisticsCostIdList) {
            ApiError confirmAmountError = validateProjectedConfirmAmountByCategory(
                    logisticsCostId, null, costType, existingDetailMap, reconciliationStatus);
            if (confirmAmountError != null) {
                // 同步分支返回批量失败原因；异步分支写入任务明细失败原因。
                throw new ServiceException(confirmAmountError);
            }
        }
    }

    /**
     * 校验目标费用单在合并导入明细后的费用分类确认金额是否不全部为 0。
     * <p>方法先以费用配置 ID 为维度得到预计金额，再解析每个费用配置所属的费用分类，
     * 最后按费用分类汇总并校验。这样可以覆盖同一费用分类下存在多个费用名称的场景。</p>
     * <p>业务规则：只校验当前费用单中实际存在费用明细的费用分类；没有费用明细的费用分类不参与校验。
     * 只要任一存在的费用分类金额不等于 0 即通过，只有全部存在分类金额均等于 0 才返回错误。</p>
     *
     * @param logisticsCostId      目标物流费用单 ID
     * @param importList           本次导入待合并的费用明细，可为空
     * @param costType             确认状态对应的费用类型
     * @param existingDetailMap    预查询的费用明细，key 为费用单 ID；可为空
     * @param reconciliationStatus 目标对账状态
     * @return 全部存在分类金额均为 0 时返回标准错误信息，否则返回 null
     */
    private ApiError validateProjectedConfirmAmountByCategory(String logisticsCostId,
                                                              List<TmsCostDetailDTO.UpdateDTO> importList,
                                                              String costType,
                                                              Map<String, List<TmsCostDetailEntity>> existingDetailMap,
                                                              String reconciliationStatus) {
        Map<String, BigDecimal> cfgAmountMap = buildProjectedConfirmCfgAmountMap(
                logisticsCostId, importList, costType, existingDetailMap);
        if (CollUtil.isEmpty(cfgAmountMap)) {
            // 当前单据没有目标类型明细时，不校验不存在的费用分类。
            return null;
        }
        Map<String, String> cfgCategoryMap = resolveCfgCostCategoryMap(cfgAmountMap.keySet(), importList, costType);
        Map<String, BigDecimal> categoryAmountMap = new LinkedHashMap<>();
        for (Map.Entry<String, BigDecimal> entry : cfgAmountMap.entrySet()) {
            String dictCostCategory = cfgCategoryMap.get(entry.getKey());
            if (CharSequenceUtil.isBlank(dictCostCategory)) {
                // 费用配置缺少分类时保留 cfgCostId，避免金额明细被静默丢弃。
                dictCostCategory = entry.getKey();
            }
            BigDecimal costValue = ObjectUtil.defaultIfNull(entry.getValue(), BigDecimal.ZERO);
            // 同一费用分类下可能包含多个费用名称，需要按分类汇总后再判断。
            categoryAmountMap.merge(dictCostCategory, costValue, BigDecimal::add);
        }
        for (Map.Entry<String, BigDecimal> entry : categoryAmountMap.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) != 0) {
                // 任一存在的费用分类金额不等于 0，即满足确认条件。
                return null;
            }
        }
        // 走到这里说明存在目标类型明细，但所有存在的费用分类金额都等于 0。
        return resolveConfirmAmountCategoryApiError(reconciliationStatus);
    }

    /**
     * 构建费用配置维度的预计确认金额。
     * <p>库内明细代表当前已保存金额；本次导入明细代表即将覆盖保存的金额。
     * 当二者存在相同费用配置 ID 时，以导入明细金额为准，模拟导入落库后的确认金额。</p>
     *
     * @param logisticsCostId   目标物流费用单 ID
     * @param importList        本次导入待合并的费用明细，可为空
     * @param costType          确认状态对应的费用类型
     * @param existingDetailMap 预查询的费用明细，key 为费用单 ID；可为空
     * @return key 为费用配置 ID、value 为预计确认金额的有序映射
     */
    private Map<String, BigDecimal> buildProjectedConfirmCfgAmountMap(String logisticsCostId,
                                                                      List<TmsCostDetailDTO.UpdateDTO> importList,
                                                                      String costType,
                                                                      Map<String, List<TmsCostDetailEntity>> existingDetailMap) {
        Map<String, BigDecimal> cfgAmountMap = new LinkedHashMap<>();
        List<TmsCostDetailEntity> existingList;
        if (existingDetailMap != null) {
            // 批量导入场景优先使用调用方预查询结果，避免循环查库。
            existingList = existingDetailMap.getOrDefault(logisticsCostId, Collections.emptyList());
        } else {
            existingList = tmsCostDetailService.lambdaQuery()
                    .eq(TmsCostDetailEntity::getMainId, logisticsCostId)
                    .eq(TmsCostDetailEntity::getType, costType)
                    .list();
        }
        if (CollUtil.isNotEmpty(existingList)) {
            for (TmsCostDetailEntity detailEntity : existingList) {
                if (!CharSequenceUtil.equals(costType, detailEntity.getType())) {
                    // 预查询结果可能包含其他类型明细，目标确认类型以 costType 为准。
                    continue;
                }
                cfgAmountMap.put(detailEntity.getCfgCostId(),
                        ObjectUtil.defaultIfNull(detailEntity.getCostValue(), BigDecimal.ZERO));
            }
        }
        if (CollUtil.isNotEmpty(importList)) {
            for (TmsCostDetailDTO.UpdateDTO updateDTO : importList) {
                if (CharSequenceUtil.equals(costType, updateDTO.getType())) {
                    // 导入明细覆盖同费用配置的库内金额，用于提前校验导入后的状态。
                    cfgAmountMap.put(updateDTO.getCfgCostId(),
                            ObjectUtil.defaultIfNull(updateDTO.getCostValue(), BigDecimal.ZERO));
                }
            }
        }
        return cfgAmountMap;
    }

    /**
     * 解析费用配置对应的费用分类。
     * <p>导入明细自带费用分类时优先使用导入值；其余费用配置 ID 从费用配置表补齐。
     * 该优先级可以保证导入场景在落库前也能按本次导入识别出的费用分类校验。</p>
     *
     * @param cfgCostIds 费用配置 ID 集合
     * @param importList 本次导入待合并的费用明细，可为空
     * @param costType   确认状态对应的费用类型
     * @return key 为费用配置 ID、value 为费用分类 code 的映射
     */
    private Map<String, String> resolveCfgCostCategoryMap(Collection<String> cfgCostIds,
                                                          List<TmsCostDetailDTO.UpdateDTO> importList,
                                                          String costType) {
        Map<String, String> cfgCategoryMap = new HashMap<>();
        if (CollUtil.isNotEmpty(importList)) {
            for (TmsCostDetailDTO.UpdateDTO updateDTO : importList) {
                if (CharSequenceUtil.equals(costType, updateDTO.getType())
                        && CharSequenceUtil.isNotBlank(updateDTO.getCfgCostId())
                        && CharSequenceUtil.isNotBlank(updateDTO.getDictCostCategory())) {
                    // 导入场景可能尚未落库，优先采用本次解析出的费用分类。
                    cfgCategoryMap.put(updateDTO.getCfgCostId(), updateDTO.getDictCostCategory());
                }
            }
        }
        // 仅查询导入明细无法提供分类的费用配置，减少不必要的配置表访问。
        List<String> missingCategoryCfgIds = cfgCostIds.stream()
                .filter(CharSequenceUtil::isNotBlank)
                .filter(cfgCostId -> CharSequenceUtil.isBlank(cfgCategoryMap.get(cfgCostId)))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(missingCategoryCfgIds)) {
            return cfgCategoryMap;
        }
        List<TmsCfgCostEntity> cfgCostList = tmsCfgCostService.listByIds(missingCategoryCfgIds);
        if (CollUtil.isNotEmpty(cfgCostList)) {
            for (TmsCfgCostEntity cfgCostEntity : cfgCostList) {
                if (CharSequenceUtil.isNotBlank(cfgCostEntity.getId())
                        && CharSequenceUtil.isNotBlank(cfgCostEntity.getDictCostCategory())) {
                    cfgCategoryMap.put(cfgCostEntity.getId(), cfgCostEntity.getDictCostCategory());
                }
            }
        }
        return cfgCategoryMap;
    }

    /**
     * 根据目标对账状态选择确认金额校验错误码。
     *
     * @param reconciliationStatus 目标对账状态
     * @return 账单确认或暂估确认对应的标准错误码
     */
    private ApiError resolveConfirmAmountCategoryApiError(String reconciliationStatus) {
        if (ReconciliationStatusEnum.CONFIRMED.getCode().equals(reconciliationStatus)) {
            return ApiError.LOGISTICS_BILL_COST_ACTUAL_CONFIRM_AMOUNT_CATEGORIES_ALL_ZERO;
        }
        return ApiError.LOGISTICS_BILL_COST_ESTIMATED_CONFIRM_AMOUNT_CATEGORIES_ALL_ZERO;
    }

    /**
     * 生成费用分类确认金额校验提示。
     * <p>用于导入行级校验结果收集；同步状态变更分支直接使用 {@link ServiceException} 承载同一错误码。</p>
     *
     * @param confirmAmountError 标准错误码
     * @return 校验提示；无错误时返回 null
     */
    private String buildConfirmAmountCategoryMsg(ApiError confirmAmountError) {
        if (confirmAmountError == null) {
            return null;
        }
        return MessageUtils.getMessage(confirmAmountError);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/logisticsBillCostTemplate.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.FILE_IMPORT_TEMPLATE_DOWNLOAD_FAILED);
        }
    }

    @Override
    public Boolean exportExcel(LogisticsBillCostDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("尾程费用(自发货)", EXPORT_TMS_LOGISTICS_BILL_COST.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public Boolean invalidByLogisticsBillId(String logisticsBillId) {
        LogisticsBillCostEntity entity = this.lambdaQuery().eq(LogisticsBillCostEntity::getLogisticsBillId,logisticsBillId).one();
        if(Objects.isNull(entity)){
            return true;
        }
        entity.setReconciliationStatus(ReconciliationStatusEnum.INVALID.getCode());
        boolean updated = this.updateById(entity);
        // 作废后费用单不再参与对账确认，反向同步对账单 ref 快照与 detail_sub 聚合状态
        syncReconStatusQuietly(Collections.singletonList(entity.getId()));
        return updated;
    }

    @Override
    public List<LogisticsBillCostEntity> getByLogisticsBillIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)){
            return Collections.emptyList();
        }
        return lambdaQuery().in(LogisticsBillCostEntity::getLogisticsBillId, ids).list();
    }

    @Override
    public LogisticsBillCostDTO.ViewDTO view(String id) {
        LogisticsBillCostEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到自发货物流费用数据"));
        LogisticsBillCostDTO.ViewDTO data = BeanMapperUtils.map(LogisticsBillCostDTO.ViewDTO.class, entity);
        data.setFeeRuleName(ShippingFeeRuleEnum.getName(data.getFeeRule()));
        //查询实际明细
        List<TmsCostDetailEntity> costDetailList = tmsCostDetailService.listByMainIdList(Arrays.asList(data.getId()));
        costDetailList = costDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), LogisticsBillCostTypeEnum.ACTUAL.getCode())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(costDetailList)) {
            data.setCostDetailList(BeanMapperUtils.copyList(TmsCostDetailDTO.ViewDTO.class,costDetailList));
        }
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByLogisticsBillDetailIdList(List<String> logisticsBillDetailIdList) {
        if (CollectionUtils.isEmpty(logisticsBillDetailIdList)) {
            return;
        }
        List<LogisticsBillCostEntity> logisticsBillCostList = this.listByLogisticsBillDetailIdList(logisticsBillDetailIdList);
        if (CollectionUtils.isEmpty(logisticsBillCostList)) {
            return;
        }
        //删除费用明细
        List<String> idList = logisticsBillCostList.stream().map(LogisticsBillCostEntity::getId).distinct().collect(Collectors.toList());
        tmsCostDetailService.deleteByMainIdList(idList);
        //删除费用
        this.removeByIds(idList);
    }

    @Override
    public List<LogisticsBillCostDTO.OutStockDTO> listBillCostByOutstockIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)){
            return Collections.emptyList();
        }
        return baseMapper.listBillCostByOutstockIds(ids);
    }

    @Override
    public List<LogisticsBillCostDTO.ListDTO> listLogisticsLastMileCostExport(LogisticsBillCostDTO.PagingParamDTO dto) {

        return baseMapper.listByExportExcel(dto);
    }


    /**
     * @description: 根据物流单id集合查询
     * @author Will
     * @date: 2023/11/14 19:49
     * @param logisticsBillIdList
     * @return List<LogisticsBillCostEntity>
     */
    @Override
    public List<LogisticsBillCostEntity> listByLogisticsBillIdList (List<String> logisticsBillIdList) {
        if (CollectionUtils.isEmpty(logisticsBillIdList)) {
            return Collections.EMPTY_LIST;
        }
       return this.lambdaQuery().in(LogisticsBillCostEntity::getLogisticsBillId,logisticsBillIdList).list();
    }

    @Override
    public List<LogisticsBillCostEntity> listByLogisticsBillIdList (List<String> logisticsBillIdList,String reconciliationMonth) {
        if (CollectionUtils.isEmpty(logisticsBillIdList)) {
            return Collections.EMPTY_LIST;
        }
        return this.lambdaQuery().in(LogisticsBillCostEntity::getLogisticsBillId,logisticsBillIdList).list();
    }

    /**
     * @description: 根据物流单明细id集合查询
     * @author Will
     * @date: 2024/3/25 14:23
     * @param logisticsBillDetailIdList
     * @return List<LogisticsBillCostEntity>
     */
    @Override
    public List<LogisticsBillCostEntity> listByLogisticsBillDetailIdList (List<String> logisticsBillDetailIdList) {
        if (CollectionUtils.isEmpty(logisticsBillDetailIdList)) {
            return Collections.EMPTY_LIST;
        }
        return this.lambdaQuery().in(LogisticsBillCostEntity::getLogisticsBillDetailId,logisticsBillDetailIdList).list();
    }

    /**
    * 新增修改处理数据
    */
    @Override
    public void handleData(LogisticsBillCostEntity entity) {

        //物流单号
        LogisticsBillEntity logisticsBillEntity = logisticsBillService.getById(entity.getLogisticsBillId());
        if (ObjectUtil.isEmpty(logisticsBillEntity)) {
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC,"物流订单");
        }
        entity.setTransportNo(logisticsBillEntity.getTransportNo());
        entity.setChannelId(logisticsBillEntity.getChannelId());

        //查询销售订单
        if (CharSequenceUtil.equals(OrderTypeEnum.B2B.getCode(),logisticsBillEntity.getOrderType())) {
            SoInfoEntity soInfoEntity = FeignQuery.getById(SoInfoEntity.class, logisticsBillEntity.getSourceId());
            if (ObjectUtil.isNotEmpty(soInfoEntity)) {
                if (CharSequenceUtil.isNotBlank(soInfoEntity.getCountryId())) {
                    // 查询国家区域
                    DictCountryEntity dictCountryEntity = FeignQuery.getById(DictCountryEntity.class, soInfoEntity.getCountryId());
                    entity.setSubregionCode(ObjectUtil.isEmpty(dictCountryEntity) ? "" : dictCountryEntity.getRegionCode());
                }
                entity.setDeptId(soInfoEntity.getSalesDeptId());
                entity.setPartitionId(soInfoEntity.getPartitionId());
            }
        } else if (CharSequenceUtil.equals(OrderTypeEnum.B2C.getCode(),logisticsBillEntity.getOrderType())) {
            List<SoB2cReceiverEntity> soB2cReceiverEntityList = FeignQuery.create(SoB2cReceiverEntity.class).eq(SoB2cReceiverEntity::getMainId,logisticsBillEntity.getSourceId()).list();
           if (CollUtil.isNotEmpty(soB2cReceiverEntityList)) {
               SoB2cReceiverEntity receiverEntity = soB2cReceiverEntityList.get(0);
               if (CharSequenceUtil.isNotBlank(receiverEntity.getCountry())) {
                   // 查询国家区域
                   DictCountryEntity dictCountryEntity = FeignQuery.getById(DictCountryEntity.class, receiverEntity.getCountry());
                   entity.setSubregionCode(ObjectUtil.isEmpty(dictCountryEntity) ? "" : dictCountryEntity.getRegionCode());
               }
               entity.setPartitionId(receiverEntity.getPartitionId());
           }
           //查询销售出库单
            if (CharSequenceUtil.isNotBlank(logisticsBillEntity.getOutstockId())) {
                SoOutstockEntity soOutstockEntity = FeignQuery.getById(SoOutstockEntity.class, logisticsBillEntity.getOutstockId());
                if (ObjectUtil.isNotEmpty(soOutstockEntity)) {
                    entity.setDeptId(soOutstockEntity.getSalesDeptId());
                }
            }
        }

        BigDecimal billingWeight = entity.getBillingWeight();
        if(StringUtils.isBlank(entity.getId()) && billingWeight == null) {
        	billingWeight = MathUtil.compareTo(entity.getActualWeight(),entity.getVolumeWeight()) > MathUtil.ZERO
                    ? entity.getActualWeight() : entity.getVolumeWeight();
        }
        //计费重
        entity.setBillingWeight(billingWeight);

        //默认kg
        entity.setWeightUnit(UnitEnum.WeightUnitEnum.KG.getCode());

        //店铺负责人
        ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(logisticsBillEntity.getShopId());
        if (ObjectUtil.isNotEmpty(shopInfoEntity)) {
            entity.setShopChargeId(shopInfoEntity.getChargeId());
            entity.setShopChargeName(shopInfoEntity.getChargeName());
        }

        //判断物流费用类型
        String type;
        if (CharSequenceUtil.equals(logisticsBillEntity.getOrderType(), OrderTypeEnum.FIRST_MILE.getCode())) {
            type = DictCostAttributionEnum.FIRST_MILE.getCode();
        } else {
            type = CharSequenceUtil.equals(logisticsBillEntity.getShipmentType(), ShipmentTypeEnum.SELF_DELIVER.getCode())
                    ? DictCostAttributionEnum.SELF_DELIVER.getCode() : DictCostAttributionEnum.LAST_MILE.getCode();
        }
        entity.setType(type);
        if (Objects.equals(DictCostAttributionEnum.SELF_DELIVER.getCode(), entity.getType()) || Objects.equals(DictCostAttributionEnum.FIRST_MILE.getCode(), entity.getType())){
            //根据渠道设置计费规则
            if (CharSequenceUtil.isNotBlank(entity.getChannelId())){
                LogisticsChannelEntity channelEntity = logisticsChannelService.getById(entity.getChannelId());
                entity.setFeeRule(Objects.nonNull(channelEntity)? channelEntity.getFeeRule() : "");
            }
        }else if (Objects.equals(DictCostAttributionEnum.LAST_MILE.getCode(), entity.getType())){
            entity.setFeeRule(ShippingFeeRuleEnum.BILLING_WEIGHT.getCode());
        }
        if (CharSequenceUtil.isBlank(entity.getLogisticsBillDetailId())){
            List<LogisticsBillDetailEntity> logisticsBillDetailEntityList = logisticsBillDetailService.listByMainIds(Collections.singletonList(logisticsBillEntity.getId()));
            if (CollectionUtils.isNotEmpty(logisticsBillDetailEntityList)){
                //现在正常情况下物流主表和物流明细是1：1关系
                entity.setLogisticsBillDetailId(logisticsBillDetailEntityList.get(0).getId());
            }
        }
        if (CharSequenceUtil.isNotBlank(entity.getCurrency())){
            if (CurrencyEnum.CNY.getCurrencyCode().equals(entity.getCurrency())){
                entity.setExchangeRate(BigDecimal.ONE);
            }else {
                String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                BigDecimal rate = dmpTaskFeign.getRate(currentDate, entity.getCurrency());
                if (Objects.isNull(rate)){
                    throw new ServiceException(ApiError.COMMON_EXCHANGE_RATE_NOT_EXIST, LocalDate.now(), entity.getCurrency());
                }
                entity.setExchangeRate(rate);
            }
        }
        String sourceId = logisticsBillEntity.getSourceId();
        if(StringUtils.isNotBlank(sourceId)) {
        	List<SoB2cLogisticsEntity> soB2cLogisticsList = FeignQuery.create(SoB2cLogisticsEntity.class).eq(SoB2cLogisticsEntity::getMainId, sourceId).list();
        	if(CollUtil.isNotEmpty(soB2cLogisticsList)) {
        		SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsList.get(0);
				BigDecimal length = soB2cLogisticsEntity.getLength();
				if(length != null) {
					length = length.setScale(1, RoundingMode.HALF_UP);
				}
				BigDecimal width = soB2cLogisticsEntity.getWidth();
				if(width != null) {
					width = width.setScale(1, RoundingMode.HALF_UP);
				}
				BigDecimal height = soB2cLogisticsEntity.getHeight();
				if(height != null) {
					height = height.setScale(1, RoundingMode.HALF_UP);
				}
				entity.setVolume(length + "*" + width + "*" + height);
        	}
        }
    }




    /**
     * @description: 分页查询数据格式化
     * @author Will
     * @date: 2023/11/13 16:04
     * @param records
     */
    @Override
    public void handleDataPaging( List<LogisticsBillCostDTO.ListDTO> records)  {
        //运输状态
        List<DictBasicEntity> transportStatusList = dictBasicService.getByKey(DictBasicEnum.LOGISTIC_TRACK_STATUS.getType());
        Map<String, String> transportStatusMap = transportStatusList.stream()
                .collect(Collectors.toMap(DictBasicEntity::getCode, DictBasicEntity::getName, (first, second) -> first));
        //区域信息
        Map<String, String> regionNameMap = FeignQuery.list(DictGlobalAreaEntity.class).stream().collect(Collectors.toMap(DictGlobalAreaEntity::getId, DictGlobalAreaEntity::getRegionName));

        //部门信息
        Map<String, String> deptNameMap = sysUserFeign.getDeptList().stream().collect(Collectors.toMap(SysDepartmentDTO::getId, SysDepartmentDTO::getName));

        //军区信息
        Map<String, String> militaryAreaNameMap = FeignQuery.list(DictPartitionEntity.class).stream().collect(Collectors.toMap(DictPartitionEntity::getId, DictPartitionEntity::getName));

        //币别信息
        Map<String, String> currencySymbolMap = FeignQuery.list(DictCurrencyEntity.class).stream().collect(Collectors.toMap(DictCurrencyEntity::getId, DictCurrencyEntity::getSymbol));
        //物流商信息
        Map<String, String> logisticsSupplierNameMap = FeignQuery.list(LogisticsSupplierEntity.class).stream().collect(Collectors.toMap(LogisticsSupplierEntity::getId, LogisticsSupplierEntity::getSupplierName));
        //实际金额
        List<String> mainIdList = records.stream().map(LogisticsBillCostDTO.ListDTO::getId).collect(Collectors.toList());
        Map<String, String> detailIdStatus = new HashMap<>();
        List<String> logisticsBillDetailIdList = records.stream().map(LogisticsBillCostDTO.ListDTO::getLogisticsBillDetailId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(logisticsBillDetailIdList)) {
        	detailIdStatus = logisticsBillDetailService.listByIds(logisticsBillDetailIdList).stream().collect(Collectors.toMap(LogisticsBillDetailEntity::getId, LogisticsBillDetailEntity::getTrackStatus));
        }
        Map<String, List<CostViewDTO>> costListMap = tmsCostDetailService.listCostByMainIdList(mainIdList).stream()
        		.collect(Collectors.groupingBy(l -> l.getMainId() + "_" + l.getDictCostCategory() + "_" + l.getType()));

        Map<String, String> payStatusNameMap = new HashMap<>();
        payStatusNameMap.put("pay_payment", "待付款");
        payStatusNameMap.put("pay_paid", "已付款");
        payStatusNameMap.put("refund_payment", "待退款");
        payStatusNameMap.put("refund_paid", "已退款");

        Map<String, BigDecimal> rateMap = new HashMap<>();
        rateMap.put("CNY", BigDecimal.ONE);
        for (LogisticsBillCostDTO.ListDTO listDTO : records) {
            listDTO.setLogisticsSupplierName(logisticsSupplierNameMap.getOrDefault(listDTO.getLogisticsSupplierId(), ""));
        	String payType = listDTO.getPayType();
        	String payStatus = listDTO.getPayStatus();
        	if(StringUtils.isNotBlank(payType) && StringUtils.isNotBlank(payStatus)) {
        		listDTO.setPayStatusName(payStatusNameMap.get(payType + "_" + payStatus));
        	}
            listDTO.setThirdPackSize(CharSequenceUtil.format("{}*{}*{}", listDTO.getThirdLength().stripTrailingZeros().toPlainString(), listDTO.getThirdWidth().stripTrailingZeros().toPlainString(), listDTO.getThirdHeight().stripTrailingZeros().toPlainString()));
        	listDTO.setCheckStatusName(LogisticsBillCostCheckStatusEnum.getName(listDTO.getCheckStatus()));
            listDTO.setOrderTypeName(CostBillTypeEnum.getName(listDTO.getOrderType()));
            listDTO.setSourceTypeName(SourceTypeEnum.getName(listDTO.getSourceType()));
            listDTO.setReconciliationStatusName(ReconciliationStatusEnum.getName(listDTO.getReconciliationStatus()));
            listDTO.setTransportStatus(detailIdStatus.get(listDTO.getLogisticsBillDetailId()));
            //运输状态
            listDTO.setTransportStatusName(transportStatusMap.getOrDefault(listDTO.getTransportStatus(), ""));
            //平台名称
            PlatformDictEnum platformDictEnum = PlatformDictEnum.getByCode(listDTO.getSalesPlatform());
            if (ObjectUtil.isNotEmpty(platformDictEnum)) {
                listDTO.setSalesPlatformName(platformDictEnum.getName());
            }
            //币别符号
            listDTO.setCurrencySymbol(currencySymbolMap.getOrDefault(listDTO.getCurrency() , "¥"));

            LocalDateTime deliveryTime = listDTO.getDeliveryTime();
            if(deliveryTime == null) {
            	deliveryTime = LocalDateTime.now();
            }
            //区域名称
            listDTO.setRegionName(regionNameMap.get(listDTO.getSubregionCode()));
            //部门名称
            listDTO.setDeptName(deptNameMap.getOrDefault(listDTO.getDeptId(), ""));
            //军区名称
            listDTO.setPartitionName(militaryAreaNameMap.getOrDefault(listDTO.getPartitionId(),""));
            //是否分摊
            listDTO.setIsAllocateRequiredName(listDTO.getIsAllocateRequired() ? "是" : "否");
            //预估运费
            BigDecimal exchangeEstimatedShippingCost = BigDecimal.ZERO;
            List<CostViewDTO> costList = costListMap.get(listDTO.getId() + "_" + DictCostCategoryEnum.SHIPPING_COST.getCode() + "_" + LogisticsBillCostTypeEnum.ESTIMATED.getCode());
            if(CollUtil.isNotEmpty(costList)) {
            	BigDecimal estimatedShippingCost = costList.stream().map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
                listDTO.setEstimatedShippingCost(estimatedShippingCost);
                String currency = costList.get(0).getCurrency();
                if(org.apache.commons.lang3.StringUtils.isBlank(currency)) {
                	currency = "CNY";
                }
				listDTO.setEstimatedShippingCostCurrencySymbol(currencySymbolMap.getOrDefault(currency , "¥"));
                BigDecimal rate = rateMap.get(currency);
                if(rate == null) {
                	rate = dmpTaskFeign.getRate(deliveryTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), currency);
                    if(ObjectUtil.isEmpty(rate)){
                        log.error("币别【{}】,汇率为空，请维护汇率后再提交",currency);
                        throw new ServiceException("汇率为空，请维护汇率后再提交");
                    }
                    rateMap.put(currency, rate);
                }
                exchangeEstimatedShippingCost = estimatedShippingCost.multiply(rate);
            }else {
            	listDTO.setEstimatedShippingCost(BigDecimal.ZERO);
                listDTO.setEstimatedShippingCostCurrencySymbol("¥");
            }
            listDTO.setEstimatedShippingCostStr(listDTO.getEstimatedShippingCostCurrencySymbol() + listDTO.getEstimatedShippingCost());

            //预估关税费用
            costList = costListMap.get(listDTO.getId() + "_" + DictCostCategoryEnum.DECLARE_COST.getCode() + "_" + LogisticsBillCostTypeEnum.ESTIMATED.getCode());
            if(CollUtil.isNotEmpty(costList)) {
            	BigDecimal estimatedDeclareCost = costList.stream().map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            	listDTO.setEstimatedDeclareCost(estimatedDeclareCost);
                listDTO.setEstimatedDeclareCostCurrencySymbol(currencySymbolMap.getOrDefault(costList.get(0).getCurrency() , "¥"));
            }else {
            	listDTO.setEstimatedDeclareCost(BigDecimal.ZERO);
                listDTO.setEstimatedDeclareCostCurrencySymbol("¥");
            }
            listDTO.setEstimatedDeclareCostStr(listDTO.getEstimatedDeclareCostCurrencySymbol() + listDTO.getEstimatedDeclareCost());

            //预估其他费用
            costList = costListMap.get(listDTO.getId() + "_" + DictCostCategoryEnum.OTHER_COST.getCode() + "_" + LogisticsBillCostTypeEnum.ESTIMATED.getCode());
            if(CollUtil.isNotEmpty(costList)) {
            	BigDecimal estimatedOtherCost = costList.stream().map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            	listDTO.setEstimatedOtherCost(estimatedOtherCost);
                listDTO.setEstimatedOtherCostCurrencySymbol(currencySymbolMap.getOrDefault(costList.get(0).getCurrency() , "¥"));
            }else {
            	listDTO.setEstimatedOtherCost(BigDecimal.ZERO);
                listDTO.setEstimatedOtherCostCurrencySymbol("¥");
            }
            listDTO.setEstimatedOtherCostStr(listDTO.getEstimatedOtherCostCurrencySymbol() + listDTO.getEstimatedOtherCost());

            //预估可抵扣税金
            costList = costListMap.get(listDTO.getId() + "_" + DictCostCategoryEnum.DEDUCTIBLE_TAX.getCode() + "_" + LogisticsBillCostTypeEnum.ESTIMATED.getCode());
            if(CollUtil.isNotEmpty(costList)) {
            	BigDecimal estimatedDeductibleTax = costList.stream().map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            	listDTO.setEstimatedDeductibleTax(estimatedDeductibleTax);
                listDTO.setEstimatedDeductibleTaxCurrencySymbol(currencySymbolMap.getOrDefault(costList.get(0).getCurrency() , "¥"));
            }else {
            	listDTO.setEstimatedDeductibleTax(BigDecimal.ZERO);
                listDTO.setEstimatedDeductibleTaxCurrencySymbol("¥");
            }
            listDTO.setEstimatedDeductibleTaxStr(listDTO.getEstimatedDeductibleTaxCurrencySymbol() + listDTO.getEstimatedDeductibleTax());

            //实际运费
            BigDecimal exchangeActualShippingCost = BigDecimal.ZERO;
            costList = costListMap.get(listDTO.getId() + "_" + DictCostCategoryEnum.SHIPPING_COST.getCode() + "_" + LogisticsBillCostTypeEnum.ACTUAL.getCode());
            if(CollUtil.isNotEmpty(costList)) {
            	BigDecimal actualShippingCost = costList.stream().map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            	listDTO.setActualShippingCost(actualShippingCost);
                String currency = costList.get(0).getCurrency();
                if(org.apache.commons.lang3.StringUtils.isBlank(currency)) {
                	currency = "CNY";
                }
				listDTO.setActualShippingCostCurrencySymbol(currencySymbolMap.getOrDefault(currency , "¥"));
				BigDecimal rate = rateMap.get(currency);
                if(rate == null) {
                	rate = dmpTaskFeign.getRate(deliveryTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), currency);
                    if(ObjectUtil.isEmpty(rate)){
                        log.error("币别【{}】,汇率为空，请维护汇率后再提交",currency);
                        throw new ServiceException("汇率为空，请维护汇率后再提交");
                    }
                    rateMap.put(currency, rate);
                }
                exchangeActualShippingCost = actualShippingCost.multiply(rate);
            }else {
            	listDTO.setActualShippingCost(BigDecimal.ZERO);
                listDTO.setActualShippingCostCurrencySymbol("¥");
            }
            listDTO.setActualShippingCostStr(listDTO.getActualShippingCostCurrencySymbol() + listDTO.getActualShippingCost());

            //运费差异
            listDTO.setDiffShippingCost(MathUtil.scaleToSix(MathUtil.subtract(exchangeActualShippingCost, exchangeEstimatedShippingCost), BigDecimal.ROUND_DOWN));
            listDTO.setDiffShippingCostStr(listDTO.getDiffShippingCostCurrencySymbol() + listDTO.getDiffShippingCost());

            //实际报关费
            costList = costListMap.get(listDTO.getId() + "_" + DictCostCategoryEnum.DECLARE_COST.getCode() + "_" + LogisticsBillCostTypeEnum.ACTUAL.getCode());
            if(CollUtil.isNotEmpty(costList)) {
            	BigDecimal actualDeclareCost = costList.stream().map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            	listDTO.setActualDeclareCost(actualDeclareCost);
                listDTO.setActualDeclareCostCurrencySymbol(currencySymbolMap.getOrDefault(costList.get(0).getCurrency() , "¥"));
            }else {
            	listDTO.setActualDeclareCost(BigDecimal.ZERO);
                listDTO.setActualDeclareCostCurrencySymbol("¥");
            }
            listDTO.setActualDeclareCostStr(listDTO.getActualDeclareCostCurrencySymbol() + listDTO.getActualDeclareCost());

            //实际其他费用
            costList = costListMap.get(listDTO.getId() + "_" + DictCostCategoryEnum.OTHER_COST.getCode() + "_" + LogisticsBillCostTypeEnum.ACTUAL.getCode());
            if(CollUtil.isNotEmpty(costList)) {
            	BigDecimal actualOtherCost = costList.stream().map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            	listDTO.setActualOtherCost(actualOtherCost);
                listDTO.setActualOtherCostCurrencySymbol(currencySymbolMap.getOrDefault(costList.get(0).getCurrency() , "¥"));
            }else {
            	listDTO.setActualOtherCost(BigDecimal.ZERO);
                listDTO.setActualOtherCostCurrencySymbol("¥");
            }
            listDTO.setActualOtherCostStr(listDTO.getActualOtherCostCurrencySymbol() + listDTO.getActualOtherCost());

            //实际可抵扣税金
            costList = costListMap.get(listDTO.getId() + "_" + DictCostCategoryEnum.DEDUCTIBLE_TAX.getCode() + "_" + LogisticsBillCostTypeEnum.ACTUAL.getCode());
            if(CollUtil.isNotEmpty(costList)) {
            	BigDecimal actualDeductibleTax = costList.stream().map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            	listDTO.setActualDeductibleTax(actualDeductibleTax);
                listDTO.setActualDeductibleTaxCurrencySymbol(currencySymbolMap.getOrDefault(costList.get(0).getCurrency() , "¥"));
            }else {
            	listDTO.setActualDeductibleTax(BigDecimal.ZERO);
                listDTO.setActualDeductibleTaxCurrencySymbol("¥");
            }
            listDTO.setActualDeductibleTaxStr(listDTO.getActualDeductibleTaxCurrencySymbol() + listDTO.getActualDeductibleTax());

            //费用规则
            listDTO.setFeeRuleName(ShippingFeeRuleEnum.getName(listDTO.getFeeRule()));
        }
    }

    /**
     * @param successList
     * @param errorList
     * @param importType
     * @description: 导入数据处理
     * @author Will
     * @date: 2023/11/14 20:06
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    @Override
    public void handleImportSuccessList (List<LogisticsBillCostExcelDTO> successList, List<LogisticsBillCostExcelDTO > errorList, String dictCostAttribution, String importType, Map<String,Object> extMap) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //对账月份
        String reconciliationMonth = (String)extMap.get("reconciliationMonth");
        if (StrUtil.isBlank(reconciliationMonth)) {
            throw new ServiceException(ApiError.LOGISTICS_BILL_COST_IMPORT_NOT_EXIST_RECONCILIATION_MONTH);
        }
        //是否确认
        Boolean confirmStatus = (Boolean)extMap.get("confirmStatus");

        //平台订单号
        List<String> platformCodeList = successList.stream().map(LogisticsBillCostExcelDTO::getPlatformCode).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        //销售订单号编码
        List<String> soCodeList = successList.stream().map(LogisticsBillCostExcelDTO::getSoCode).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        //发货单号
        List<String> soDeliveryCodeList = successList.stream().map(LogisticsBillCostExcelDTO::getSoDeliveryCode).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        //物流单
        List<String> trackNoList = successList.stream().map(LogisticsBillCostExcelDTO::getTrackNo).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos = logisticsBillService.listLogisticsBillVoByData(platformCodeList,soCodeList,soDeliveryCodeList,trackNoList);

        //物流单费用
        List<String> logisticsBillIdList = logisticsBillVos.stream().map(LogisticsBillDTO.LogisticsBillVo::getId).distinct().collect(Collectors.toList());
        List<LogisticsBillCostEntity> logisticsBillCostList = this.listByLogisticsBillIdList(logisticsBillIdList);

        //费用配置
        List<String> costNameList = successList.stream().map(LogisticsBillCostExcelDTO::getCostName).distinct().collect(Collectors.toList());
        List<TmsCfgCostEntity> tmsCfgCostList = tmsCfgCostService.listByCostNameList(costNameList);
        Map<String, TmsCfgCostEntity> cfgCostMap = CollUtil.isEmpty(tmsCfgCostList) ? Collections.emptyMap()
                : tmsCfgCostList.stream().collect(Collectors.toMap(TmsCfgCostEntity::getId, obj -> obj, (a, b) -> a));

        Map<String, List<LogisticsBillDTO.LogisticsBillVo>> billVoIndex = buildBillVoIndex(logisticsBillVos);
        Map<String, List<LogisticsBillCostExcelDTO>> map = successList.stream().collect(
                Collectors.groupingBy(
                        obj -> CharSequenceUtil.format("{}_{}_{}_{}_{}",
                                obj.getPlatformCode(), obj.getSoCode(), obj.getSoDeliveryCode(), obj.getTrackNo(), obj.getPayType()),
                        LinkedHashMap::new,
                        Collectors.toList()));
        Map<String, List<LogisticsBillDTO.LogisticsBillVo>> groupLogisticsBillVoMap = new LinkedHashMap<>();
        map.forEach((key, rows) ->
                groupLogisticsBillVoMap.put(key, matchImportLogisticsBillVos(rows.get(0), logisticsBillVos, billVoIndex)));

        OutstockWeightPreloadDTO outstockWeightPreload = preloadOutstockWeightDataForAllocation(groupLogisticsBillVoMap);

        Map<String, List<TmsCostDetailEntity>> mainIdListMap = new HashMap<>();
        if(CollUtil.isNotEmpty(logisticsBillVos)) {
            List<String> logisticsBillCostIdList = logisticsBillVos.stream()
                    .map(LogisticsBillDTO.LogisticsBillVo::getLogisticsBillCostId)
                    .filter(CharSequenceUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            logisticsBillCostIdList.forEach(id -> mainIdListMap.put(id, Collections.emptyList()));
            if (CollUtil.isNotEmpty(logisticsBillCostIdList)) {
                List<TmsCostDetailEntity> listByMainIdList = tmsCostDetailService.listByMainIdList(logisticsBillCostIdList);
                if (CollUtil.isNotEmpty(listByMainIdList)) {
                    mainIdListMap.putAll(listByMainIdList.stream().collect(Collectors.groupingBy(TmsCostDetailEntity::getMainId)));
                }
            }
        }

        for ( Map.Entry<String, List<LogisticsBillCostExcelDTO>> entry : map.entrySet()) {
            List<LogisticsBillCostExcelDTO> value = entry.getValue();
            LogisticsBillCostExcelDTO billCostExcelDTO = value.get(0);

            List<String> errorMsgList = new ArrayList<>();
            List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList = groupLogisticsBillVoMap.getOrDefault(entry.getKey(), Collections.emptyList());
            boolean importAddNew =Objects.equals("import_add_new",importType);
            if (CollUtil.isEmpty(logisticsBillVoList) && !importAddNew) {
                errorMsgList.add("未找到对应物流单");
            }
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                value.forEach(excelDTO -> excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList)));
                errorList.addAll(value);
                continue;
            }

            List<TmsCostDetailDTO.UpdateDTO> updateDetailList = new ArrayList<>();
            for (LogisticsBillCostExcelDTO excelDTO : value) {
                List<String> costErrorMsgList = new ArrayList<>();
                TmsCfgCostEntity tmsCfgCostEntity = tmsCfgCostList.stream()
                        .filter(obj -> CharSequenceUtil.equals(obj.getCostName(), excelDTO.getCostName())
                                && CharSequenceUtil.equals(obj.getDictCostAttribution(), LAST_MILE_FEE_ATTRIBUTION))
                        .findFirst().orElse(null);
                if (ObjectUtil.isEmpty(tmsCfgCostEntity)) {
                    // 自发货尾程导入费用项统一从“尾程发货”归属查找，主单 type 仍由 dictCostAttribution 校验。
                    costErrorMsgList.add("费用管理尾程发货未找到该费用名称");
                }
                String estimatedCostValue = excelDTO.getEstimatedCostValue();
                String estimatedCurrency = excelDTO.getEstimatedCurrency();
                if(StringUtils.isNotBlank(estimatedCostValue) && StringUtils.isBlank(estimatedCurrency)) {
                	costErrorMsgList.add("预估金额不为空，预估币种必填");
                }
                if (CollectionUtils.isNotEmpty(costErrorMsgList)) {
                    excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(costErrorMsgList));
                    errorList.add(excelDTO);
                    continue;
                }
                TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
                updateDTO.setCostValue(new BigDecimal(excelDTO.getCostValue()));
                updateDTO.setType(LogisticsBillCostTypeEnum.ACTUAL.getCode());
                updateDTO.setCfgCostId(tmsCfgCostEntity.getId());
                updateDTO.setSourceType(SourceTypeEnum.LOGISTICS_BILL_COST.getCode());
                updateDTO.setCurrency(excelDTO.getCurrency());
                updateDTO.setDictCostCategory(tmsCfgCostEntity.getDictCostCategory());
                updateDetailList.add(updateDTO);
                if(StringUtils.isNotBlank(estimatedCostValue)) {
                	updateDTO = new TmsCostDetailDTO.UpdateDTO();
                    updateDTO.setCostValue(new BigDecimal(estimatedCostValue));
                    updateDTO.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
                    updateDTO.setCfgCostId(tmsCfgCostEntity.getId());
                    updateDTO.setSourceType(SourceTypeEnum.LOGISTICS_BILL_COST.getCode());
					updateDTO.setCurrency(estimatedCurrency);
					updateDTO.setDictCostCategory(tmsCfgCostEntity.getDictCostCategory());
                    updateDetailList.add(updateDTO);
                }
            }
            List<LogisticsBillCostExcelDTO> importSuccessList = value.stream()
                    .filter(excelDTO -> CharSequenceUtil.isBlank(excelDTO.getErrorMsg()))
                    .collect(Collectors.toList());
            updateDetailList = mergeImportUpdateDetails(updateDetailList);
            if (CollectionUtils.isEmpty(updateDetailList)) {
                continue;
            }
            validateSameCostCurrency(updateDetailList, tmsCfgCostList, errorMsgList);
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                importSuccessList.forEach(excelDTO -> excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList)));
                errorList.addAll(importSuccessList);
                continue;
            }

            LogisticsBillCostEntity logisticsBillCostEntity;
            //对账确认日期
            List<Pair<String,LocalDateTime>> pairList = new ArrayList<>();
            LocalDateTime confirmTime = CharSequenceUtil.isBlank(billCostExcelDTO.getConfirmTimeStr()) ? LocalDateTime.now() : LocalDateUtil.stringToLocalDateTime(billCostExcelDTO.getConfirmTimeStr());

            if (CollUtil.isNotEmpty(logisticsBillVoList)) {
                Map<String, List<TmsCostDetailDTO.UpdateDTO>> allocatedCostMap = new HashMap<>();
                if (logisticsBillVoList.size() > 1) {
                    Map<String, BigDecimal> weightMap = buildOrderWeightMap(logisticsBillVoList, errorMsgList, outstockWeightPreload);
                    if (CollectionUtils.isNotEmpty(errorMsgList)) {
                        importSuccessList.forEach(excelDTO -> excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList)));
                        errorList.addAll(importSuccessList);
                        continue;
                    }
                    allocatedCostMap = allocateCostDetailByWeight(updateDetailList, logisticsBillVoList, weightMap);
                }
                List<Pair<LogisticsBillDTO.LogisticsBillVo, LogisticsBillCostEntity>> targetPairList = new ArrayList<>();
                Map<String, List<TmsCostDetailDTO.UpdateDTO>> targetUpdateMap = new HashMap<>();
                for (LogisticsBillDTO.LogisticsBillVo logisticsBillVo : logisticsBillVoList) {
                    logisticsBillVo.setReconciliationMonth(reconciliationMonth);
                    List<TmsCostDetailDTO.UpdateDTO> currentUpdateList = logisticsBillVoList.size() > 1
                            ? allocatedCostMap.getOrDefault(logisticsBillVo.getDetailId(), Collections.emptyList())
                            : updateDetailList;
                    //数据验证
                    checkImportData(billCostExcelDTO,logisticsBillVo,logisticsBillCostList,dictCostAttribution,importType,errorMsgList);
                    if (CollectionUtils.isNotEmpty(errorMsgList)) {
                        break;
                    }
                    //物流费用单
                    logisticsBillCostEntity = logisticsBillCostList.stream().filter(obj -> obj.getLogisticsBillId().equals(logisticsBillVo.getId())
                                    && CharSequenceUtil.equals(obj.getLogisticsBillDetailId(),logisticsBillVo.getDetailId())
                                    && (CharSequenceUtil.equals(obj.getReconciliationStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode()) || (CharSequenceUtil.equals(ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode(),obj.getReconciliationStatus())
                                    && CharSequenceUtil.equals(obj.getCheckStatus(),LogisticsBillCostCheckStatusEnum.CHECKING.getCode())))
                                    && CharSequenceUtil.equals(obj.getPayType(),billCostExcelDTO.getPayType()))
                            .findFirst().orElse(null);
                    if (CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode().equals(importType) && Objects.isNull(logisticsBillCostEntity)){
                        logisticsBillCostEntity = logisticsBillCostList.stream().filter(obj -> obj.getLogisticsBillId().equals(logisticsBillVo.getId())
                                        && CharSequenceUtil.equals(obj.getLogisticsBillDetailId(),logisticsBillVo.getDetailId()))
                                .findFirst().orElse(null);
                    }
                    if (Objects.isNull(logisticsBillCostEntity)){
                        errorMsgList.add("未找到对应物流费用单");
                        break;
                    }
                    //校验分类币别
                    checkCategoryCurrency(currentUpdateList,logisticsBillCostEntity,tmsCfgCostList,mainIdListMap,importSuccessList,errorList);
                    if (CollUtil.isEmpty(currentUpdateList)) {
                        errorMsgList.add("费用分类币别校验未通过");
                        break;
                    }
                    targetPairList.add(new Pair<>(logisticsBillVo, logisticsBillCostEntity));
                    targetUpdateMap.put(logisticsBillVo.getDetailId(), currentUpdateList);
                }
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    importSuccessList.forEach(excelDTO -> excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList)));
                    errorList.addAll(importSuccessList);
                    continue;
                }
                // 勾选导入确认时，校验合并明细后实际金额合计大于 0
                appendImportConfirmAmountErrors(confirmStatus, targetPairList, targetUpdateMap, mainIdListMap, errorMsgList);
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    importSuccessList.forEach(excelDTO -> excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList)));
                    errorList.addAll(importSuccessList);
                    continue;
                }
                for (Pair<LogisticsBillDTO.LogisticsBillVo, LogisticsBillCostEntity> targetPair : targetPairList) {
                    LogisticsBillDTO.LogisticsBillVo logisticsBillVo = targetPair.getKey();
                    logisticsBillCostEntity = targetPair.getValue();
                    List<TmsCostDetailDTO.UpdateDTO> currentUpdateList = targetUpdateMap.get(logisticsBillVo.getDetailId());
                    //生成物流费用对象
                    LogisticsBillCostDTO.UpdateDTO updateDataDTO = handleLogisticsBillCostImportData(logisticsBillCostEntity, billCostExcelDTO, reconciliationMonth);
                    if (CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode().equals(importType)){
                        List<LogisticsBillCostDTO.AddDataDTO> dtoList = buildAddDTO(updateDataDTO,currentUpdateList);
                        List<AddDTO> addDTOS = service.addPayAndRefund(dtoList, cfgCostMap);
                        pairList.addAll(addDTOS.stream()
                                .map(obj -> new Pair<String, LocalDateTime>(obj.getId(), confirmTime))
                                .collect(Collectors.toList()));
                    }else {
                        updateDataDTO.setCostDetailList(currentUpdateList);
                        BaseResultDTO.UpdateDTO update = service.update(updateDataDTO, Boolean.TRUE, cfgCostMap);
                        pairList.add(new Pair<>(update.getId(),confirmTime));
                    }
                }
            } else {
                LogisticsBillEntity logisticsBillEntity = addImportLogisticBill(billCostExcelDTO);
                //查询物流费用加下更新
                List<LogisticsBillCostEntity> logisticsBillCost = this.getByLogisticsBillIds(Collections.singletonList(logisticsBillEntity.getId()));
                if (CollUtil.isEmpty(logisticsBillCost)) {
                    throw new ServiceException("新增物流单后未找到对应的物流费用数据，无法进行后续处理");
                }
                logisticsBillCostEntity = logisticsBillCost.get(0);

                // 新增物流单分支：勾选导入确认时按费用分类校验实际金额。
                if (Boolean.TRUE.equals(confirmStatus)) {
                    String confirmMsg = validateImportConfirmAmountMsg(logisticsBillCostEntity.getId(), updateDetailList,
                            ReconciliationStatusEnum.CONFIRMED.getCode(), Collections.emptyMap());
                    if (CharSequenceUtil.isNotBlank(confirmMsg)) {
                        importSuccessList.forEach(excelDTO -> excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(Collections.singletonList(confirmMsg))));
                        errorList.addAll(importSuccessList);
                        continue;
                    }
                }
                LogisticsBillCostDTO.UpdateDTO updateDataDTO = handleLogisticsBillCostImportData(logisticsBillCostEntity, billCostExcelDTO, reconciliationMonth);
                updateDataDTO.setCostDetailList(updateDetailList);
                BaseResultDTO.UpdateDTO update = service.update(updateDataDTO, Boolean.TRUE, cfgCostMap);
                pairList.add(new Pair<>(update.getId(),confirmTime));
            }
            //确认
            if (confirmStatus) {
                pairList.forEach(obj -> this.updateReconciliationStatus(obj.getKey(), ReconciliationStatusEnum.CONFIRMED.getCode(), obj.getValue()));
            }
        }
    }

    /**
     * 标准导入勾选确认时，对匹配到的多张物流费用单逐单校验账单确认金额。
     * <p>未勾选确认（confirmStatus=false）时直接跳过。</p>
     */
    private void appendImportConfirmAmountErrors(Boolean confirmStatus,
                                                 List<Pair<LogisticsBillDTO.LogisticsBillVo, LogisticsBillCostEntity>> targetPairList,
                                                 Map<String, List<TmsCostDetailDTO.UpdateDTO>> targetUpdateMap,
                                                 Map<String, List<TmsCostDetailEntity>> mainIdListMap,
                                                 List<String> errorMsgList) {
        if (!Boolean.TRUE.equals(confirmStatus) || CollUtil.isEmpty(targetPairList)) {
            return;
        }
        for (Pair<LogisticsBillDTO.LogisticsBillVo, LogisticsBillCostEntity> targetPair : targetPairList) {
            // 合并库内已有明细与本次导入明细后，按费用分类校验实际金额。
            String confirmMsg = validateImportConfirmAmountMsg(targetPair.getValue().getId(),
                    targetUpdateMap.get(targetPair.getKey().getDetailId()),
                    ReconciliationStatusEnum.CONFIRMED.getCode(), mainIdListMap);
            if (CharSequenceUtil.isNotBlank(confirmMsg)) {
                errorMsgList.add(confirmMsg);
                return;
            }
        }
    }

    /**
     * 标准导入同识别分组允许同费用项多行录入，落库前按费用项、费用类型和币种汇总。
     */
    private List<TmsCostDetailDTO.UpdateDTO> mergeImportUpdateDetails(List<TmsCostDetailDTO.UpdateDTO> updateDetailList) {
        if (CollUtil.isEmpty(updateDetailList)) {
            return updateDetailList;
        }
        Map<String, TmsCostDetailDTO.UpdateDTO> updateDetailMap = new LinkedHashMap<>();
        for (TmsCostDetailDTO.UpdateDTO updateDTO : updateDetailList) {
            String key = CharSequenceUtil.format("{}_{}_{}", updateDTO.getCfgCostId(), updateDTO.getType(), updateDTO.getCurrency());
            TmsCostDetailDTO.UpdateDTO existsDTO = updateDetailMap.get(key);
            if (ObjectUtil.isNull(existsDTO)) {
                updateDetailMap.put(key, updateDTO);
                continue;
            }
            existsDTO.setCostValue(ObjectUtil.defaultIfNull(existsDTO.getCostValue(), BigDecimal.ZERO).add(ObjectUtil.defaultIfNull(updateDTO.getCostValue(), BigDecimal.ZERO)));
        }
        return new ArrayList<>(updateDetailMap.values());
    }

    private List<LogisticsBillDTO.LogisticsBillVo> matchImportLogisticsBillVos(LogisticsBillCostExcelDTO excelDTO,
                                                                               List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos,
                                                                               Map<String, List<LogisticsBillDTO.LogisticsBillVo>> billVoIndex) {
        if (CharSequenceUtil.isAllBlank(excelDTO.getPlatformCode(), excelDTO.getSoDeliveryCode(), excelDTO.getSoCode(), excelDTO.getTrackNo())) {
            return Collections.emptyList();
        }
        if (CollUtil.isEmpty(logisticsBillVos)) {
            return Collections.emptyList();
        }
        // 按选择性最高的非空字段从索引中缩小候选集，避免全量 O(n×m) 扫描
        List<LogisticsBillDTO.LogisticsBillVo> candidates;
        if (CharSequenceUtil.isNotBlank(excelDTO.getTrackNo())) {
            candidates = billVoIndex.getOrDefault("trackNo:" + excelDTO.getTrackNo(), Collections.emptyList());
        } else if (CharSequenceUtil.isNotBlank(excelDTO.getSoDeliveryCode())) {
            candidates = billVoIndex.getOrDefault("soDelivery:" + excelDTO.getSoDeliveryCode(), Collections.emptyList());
        } else if (CharSequenceUtil.isNotBlank(excelDTO.getSoCode())) {
            candidates = billVoIndex.getOrDefault("soCode:" + excelDTO.getSoCode(), Collections.emptyList());
        } else {
            candidates = billVoIndex.getOrDefault("platform:" + excelDTO.getPlatformCode(), Collections.emptyList());
        }
        return candidates.stream()
                .filter(obj -> CharSequenceUtil.isBlank(excelDTO.getPlatformCode())
                        || LogisticsBillPlatformCodeUtil.matches(excelDTO.getPlatformCode(), obj.getPlatformCode()))
                .filter(obj -> CharSequenceUtil.isBlank(excelDTO.getSoDeliveryCode()) || CharSequenceUtil.equals(obj.getSoDeliveryCode(), excelDTO.getSoDeliveryCode()))
                .filter(obj -> CharSequenceUtil.isBlank(excelDTO.getSoCode()) || CharSequenceUtil.equals(obj.getSourceCode(), excelDTO.getSoCode()))
                .filter(obj -> CharSequenceUtil.isBlank(excelDTO.getTrackNo()) || CharSequenceUtil.equals(obj.getTrackNo(), excelDTO.getTrackNo()))
                .collect(Collectors.toList());
    }

    private Map<String, List<LogisticsBillDTO.LogisticsBillVo>> buildBillVoIndex(List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos) {
        Map<String, List<LogisticsBillDTO.LogisticsBillVo>> index = new HashMap<>();
        if (CollUtil.isEmpty(logisticsBillVos)) {
            return index;
        }
        for (LogisticsBillDTO.LogisticsBillVo vo : logisticsBillVos) {
            if (CharSequenceUtil.isNotBlank(vo.getTrackNo())) {
                index.computeIfAbsent("trackNo:" + vo.getTrackNo(), k -> new ArrayList<>()).add(vo);
            }
            if (CharSequenceUtil.isNotBlank(vo.getSoDeliveryCode())) {
                index.computeIfAbsent("soDelivery:" + vo.getSoDeliveryCode(), k -> new ArrayList<>()).add(vo);
            }
            if (CharSequenceUtil.isNotBlank(vo.getSourceCode())) {
                index.computeIfAbsent("soCode:" + vo.getSourceCode(), k -> new ArrayList<>()).add(vo);
            }
            LogisticsBillPlatformCodeUtil.splitPlatformCodes(vo.getPlatformCode())
                    .forEach(platformCode -> index.computeIfAbsent("platform:" + platformCode, k -> new ArrayList<>()).add(vo));
        }
        return index;
    }

    private void validateSameCostCurrency(List<TmsCostDetailDTO.UpdateDTO> updateDetailList,
                                          List<TmsCfgCostEntity> tmsCfgCostList,
                                          List<String> errorMsgList) {
        Map<String, List<TmsCostDetailDTO.UpdateDTO>> costTypeMap = updateDetailList.stream()
                .collect(Collectors.groupingBy(updateDTO -> updateDTO.getCfgCostId() + "_" + updateDTO.getType()));
        for (Map.Entry<String, List<TmsCostDetailDTO.UpdateDTO>> entry : costTypeMap.entrySet()) {
            List<String> currencyList = entry.getValue().stream()
                    .map(updateDTO -> CharSequenceUtil.blankToDefault(updateDTO.getCurrency(), ""))
                    .distinct()
                    .collect(Collectors.toList());
            if (currencyList.size() <= 1) {
                continue;
            }
            TmsCostDetailDTO.UpdateDTO updateDTO = entry.getValue().get(0);
            String costName = tmsCfgCostList.stream()
                    .filter(cfgCost -> CharSequenceUtil.equals(cfgCost.getId(), updateDTO.getCfgCostId()))
                    .map(TmsCfgCostEntity::getCostName)
                    .findFirst()
                    .orElse(updateDTO.getCfgCostId());
            errorMsgList.add("【" + costName + "】相同费用类型存在不同币别");
        }
    }

    @Override
    public OutstockWeightPreloadDTO preloadOutstockWeightDataForAllocation(
            Map<String, List<LogisticsBillDTO.LogisticsBillVo>> groupLogisticsBillVoMap) {
        OutstockWeightPreloadDTO preloadData = new OutstockWeightPreloadDTO();
        List<String> allocationOutstockIdList = groupLogisticsBillVoMap.values().stream()
                .filter(list -> list.size() > 1)
                .flatMap(Collection::stream)
                .map(LogisticsBillDTO.LogisticsBillVo::getOutstockId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(allocationOutstockIdList)) {
            return preloadData;
        }
        List<SoOutstockDetailEntity> outstockDetailList = FeignQuery.create(SoOutstockDetailEntity.class)
                .in(SoOutstockDetailEntity::getMainId, allocationOutstockIdList).list();
        if (CollUtil.isEmpty(outstockDetailList)) {
            return preloadData;
        }
        preloadData.setOutstockDetailMap(outstockDetailList.stream()
                .collect(Collectors.groupingBy(SoOutstockDetailEntity::getMainId)));
        return preloadData;
    }

    @Override
    public Map<String, BigDecimal> buildOrderWeightMap(List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList,
                                                       List<String> errorMsgList,
                                                       OutstockWeightPreloadDTO preloadData) {
        Map<String, List<SoOutstockDetailEntity>> outstockDetailMap = preloadData == null
                ? Collections.emptyMap() : ObjectUtil.defaultIfNull(preloadData.getOutstockDetailMap(), Collections.emptyMap());
        return logisticsOrderWeightSupport.buildOrderWeightMap(logisticsBillVoList, outstockDetailMap, errorMsgList);
    }

    @Override
    public Map<String, List<TmsCostDetailDTO.UpdateDTO>> allocateCostDetailByWeight(List<TmsCostDetailDTO.UpdateDTO> updateList,
                                                                                    List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList,
                                                                                    Map<String, BigDecimal> weightMap) {
        Map<String, List<TmsCostDetailDTO.UpdateDTO>> resultMap = new LinkedHashMap<>();
        BigDecimal totalWeight = logisticsBillVoList.stream().map(vo -> weightMap.getOrDefault(vo.getDetailId(), BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add);
        for (TmsCostDetailDTO.UpdateDTO updateDTO : updateList) {
            BigDecimal allocatedSum = BigDecimal.ZERO;
            BigDecimal cost = ObjectUtil.defaultIfNull(updateDTO.getCostValue(), BigDecimal.ZERO);
            for (int i = 0; i < logisticsBillVoList.size(); i++) {
                LogisticsBillDTO.LogisticsBillVo logisticsBillVo = logisticsBillVoList.get(i);
                BigDecimal allocatedCost = i == logisticsBillVoList.size() - 1
                        ? cost.subtract(allocatedSum)
                        : cost.multiply(weightMap.getOrDefault(logisticsBillVo.getDetailId(), BigDecimal.ZERO)).divide(totalWeight, 4, RoundingMode.HALF_UP);
                allocatedSum = allocatedSum.add(allocatedCost);
                TmsCostDetailDTO.UpdateDTO copyDTO = copyUpdateCostDetail(updateDTO);
                copyDTO.setCostValue(allocatedCost);
                resultMap.computeIfAbsent(logisticsBillVo.getDetailId(), key -> new ArrayList<>()).add(copyDTO);
            }
        }
        return resultMap;
    }

    private TmsCostDetailDTO.UpdateDTO copyUpdateCostDetail(TmsCostDetailDTO.UpdateDTO source) {
        TmsCostDetailDTO.UpdateDTO copyDTO = new TmsCostDetailDTO.UpdateDTO();
        copyDTO.setId(source.getId());
        copyDTO.setDictCostCategory(source.getDictCostCategory());
        copyDTO.setSourceType(source.getSourceType());
        copyDTO.setMainId(source.getMainId());
        copyDTO.setCostValue(source.getCostValue());
        copyDTO.setCfgCostId(source.getCfgCostId());
        copyDTO.setType(source.getType());
        copyDTO.setCurrency(source.getCurrency());
        return copyDTO;
    }

    /**
     * 新增物流单
     * @author will
     * @date 2026/1/22 20:04
     * @param excelDTO
     * @return com.erp.model.tms.entity.LogisticsBillEntity
     */
    private LogisticsBillEntity addImportLogisticBill (LogisticsBillCostExcelDTO excelDTO) {
        //新增物流单，格式化物流费用
        LogisticsBillDTO.AddDTO addDTO = new LogisticsBillDTO.AddDTO();

        List<LogisticsSupplierEntity> logisticsSupplierList = logisticsSupplierService.listByName(Collections.singletonList(excelDTO.getLogisticsSupplierName()));
        if (CollUtil.isEmpty(logisticsSupplierList)) {
            throw new ServiceException(ApiError.LOGISTICS_SUPPLIER_NAME_NOT_FOUND,excelDTO.getLogisticsSupplierName());
        }
        addDTO.setLogisticsSupplierId(logisticsSupplierList.get(0).getId());
        //发货单信息
        if (CharSequenceUtil.isNotBlank(excelDTO.getSoDeliveryCode())) {
            if (excelDTO.getSoDeliveryCode().startsWith("FHTZ")) {
                List<SoDeliveryNoticeEntity> list = FeignQuery.create(SoDeliveryNoticeEntity.class).eq(SoDeliveryNoticeEntity::getCode, excelDTO.getSoDeliveryCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addDTO.setSoDeliveryCode(list.get(0).getId());
                    addDTO.setSourceCode(list.get(0).getSourceCode());
                    addDTO.setSourceType(SourceTypeEnum.SO_INFO.getCode());
                    addDTO.setOrderType(OrderTypeEnum.B2B.getCode());
                }
            } else if (excelDTO.getSoDeliveryCode().startsWith("FHDC")) {
                List<SoB2cDeliveryEntity> list = FeignQuery.create(SoB2cDeliveryEntity.class).eq(SoB2cDeliveryEntity::getCode, excelDTO.getSoDeliveryCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addDTO.setSoDeliveryCode(list.get(0).getId());
                    addDTO.setSourceCode(list.get(0).getSourceCode());
                    addDTO.setOrderType(OrderTypeEnum.B2C.getCode());
                    addDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());
                }
            }else {
                addDTO.setOrderType(OrderTypeEnum.OTHER.getCode());
            }
        }
        //销售订单信息
        if (CharSequenceUtil.isNotBlank(excelDTO.getSoCode())) {
            if (CharSequenceUtil.isNotBlank(addDTO.getSourceCode()) && !CharSequenceUtil.equals(addDTO.getSourceCode(),excelDTO.getSoCode())) {
                throw new ServiceException("发货单对应的销售订单与导入的销售订单不匹配，请核查");
            }
            if (excelDTO.getSoCode().startsWith("XSD")) {
                List<SoInfoEntity> list = FeignQuery.create(SoInfoEntity.class).eq(SoInfoEntity::getCode, excelDTO.getSoCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addDTO.setSourceId(list.get(0).getId());
                    addDTO.setSourceType(SourceTypeEnum.SO_INFO.getCode());
                    addDTO.setOrderType(OrderTypeEnum.B2B.getCode());
                }
            } else if (excelDTO.getSoCode().startsWith("XSDS")) {
                List<SoB2cEntity> list = FeignQuery.create(SoB2cEntity.class).eq(SoB2cEntity::getCode, excelDTO.getSoCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addDTO.setSourceId(list.get(0).getId());
                    addDTO.setOrderType(OrderTypeEnum.B2C.getCode());
                    addDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());
                }
            }else {
                addDTO.setOrderType(OrderTypeEnum.OTHER.getCode());
            }
        }

        //订单类型默认其他
        if (CharSequenceUtil.isBlank(addDTO.getOrderType())) {
            addDTO.setOrderType(OrderTypeEnum.OTHER.getCode());
        }

        addDTO.setShipmentType(ShipmentTypeEnum.SELF_DELIVER.getCode());
        addDTO.setSourceCode(excelDTO.getSoCode());
        addDTO.setPlatformCode(excelDTO.getPlatformCode());
        addDTO.setSoDeliveryCode(excelDTO.getSoDeliveryCode());

        LogisticsBillDetailDTO.AddDTO addDetailDTO = new LogisticsBillDetailDTO.AddDTO();
        addDetailDTO.setTrackNo(excelDTO.getTrackNo());
        addDetailDTO.setTrackEnable(Boolean.FALSE);
        addDTO.setDetailList(Collections.singletonList(addDetailDTO));
        return logisticsBillService.add(addDTO);
    }

    private void checkCategoryCurrency (List<TmsCostDetailDTO.UpdateDTO> updateDetailList,LogisticsBillCostEntity logisticsBillCostEntity,
                                        List<TmsCfgCostEntity> tmsCfgCostList,
                                        Map<String, List<TmsCostDetailEntity>> mainIdListMap,
                                        List<LogisticsBillCostExcelDTO> value,
                                        List<LogisticsBillCostExcelDTO> errorList) {
        List<TmsCostDetailEntity> validateList = BeanMapperUtils.copyList(TmsCostDetailEntity.class, updateDetailList);
        List<TmsCostDetailEntity> tmsCostDetailEntityList = mainIdListMap.get(logisticsBillCostEntity.getId());
        if(CollUtil.isNotEmpty(tmsCostDetailEntityList)) {
            List<String> cfgCostIds = validateList.stream().map(TmsCostDetailEntity::getCfgCostId).collect(Collectors.toList());
            validateList.addAll(tmsCostDetailEntityList.stream().filter(t -> !cfgCostIds.contains(t.getCfgCostId())).collect(Collectors.toList()));
        }
        Set<String> validateCategoryCurrency = tmsCostDetailService.validateCategoryCurrency(validateList);
        if(!validateCategoryCurrency.isEmpty()) {
            Map<String, Set<String>> costIdTypeListMap = new HashMap<>();
            for(String validateCategory : validateCategoryCurrency) {
                String[] split = validateCategory.split("_");
                List<UpdateDTO> removeList = updateDetailList.stream().filter(u -> u.getDictCostCategory().equals(split[0]) && u.getType().equals(split[1])).collect(Collectors.toList());
                for(UpdateDTO remove : removeList) {
                    String costName = tmsCfgCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), remove.getCfgCostId())).findFirst().orElse(null).getCostName();
                    Set<String> set = costIdTypeListMap.get(costName);
                    if(CollUtil.isEmpty(set)) {
                        set = new HashSet<>();
                    }
                    set.add(AllocationFeeTypeEnum.getName(split[0]) + "-" + LogisticsBillCostTypeEnum.getName(split[1]) + "分类下所有一级费用币种必须一致");
                    costIdTypeListMap.put(costName, set);
                }
                updateDetailList.removeIf(u -> u.getDictCostCategory().equals(split[0]) && u.getType().equals(split[1]));
            }
            if(!costIdTypeListMap.isEmpty()) {
                for(LogisticsBillCostExcelDTO excelDTO : value) {
                    Set<String> set = costIdTypeListMap.get(excelDTO.getCostName());
                    if(CollUtil.isNotEmpty(set)) {
                        excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(new ArrayList<>(set)));
                        errorList.add(excelDTO);
                    }
                }
            }
        }
    }

    private LogisticsBillCostDTO.UpdateDTO handleLogisticsBillCostImportData( LogisticsBillCostEntity logisticsBillCostEntity,LogisticsBillCostExcelDTO billCostExcelDTO,String reconciliationMonth) {
        //数据赋值
        LogisticsBillCostDTO.UpdateDTO updateDataDTO = new LogisticsBillCostDTO.UpdateDTO();
        updateDataDTO.setId(logisticsBillCostEntity.getId());
        updateDataDTO.setPayType(billCostExcelDTO.getPayType());
        String billingWeight = billCostExcelDTO.getBillingWeight();
        if(StringUtils.isNotBlank(billingWeight)) {
            updateDataDTO.setBillingWeight(new BigDecimal(billingWeight));
        }
        String billingWeightLogistics = billCostExcelDTO.getBillingWeightLogistics();
        if(StringUtils.isNotBlank(billingWeightLogistics)) {
            updateDataDTO.setBillingWeightLogistics(new BigDecimal(billingWeightLogistics));
        }
        updateDataDTO.setCurrency(CharSequenceUtil.isBlank(billCostExcelDTO.getCurrency()) ? CurrencyEnum.CNY.getCurrencyCode() : billCostExcelDTO.getCurrency());
        //对账月份
        updateDataDTO.setReconciliationMonth(reconciliationMonth);
        //尺寸
        String thirdHeight = billCostExcelDTO.getThirdHeight();
        if(StringUtils.isNotBlank(thirdHeight)) {
            updateDataDTO.setThirdHeight(new BigDecimal(thirdHeight));
        }
        String thirdWidth = billCostExcelDTO.getThirdWidth();
        if(StringUtils.isNotBlank(thirdWidth)) {
            updateDataDTO.setThirdWidth(new BigDecimal(thirdWidth));
        }
        String thirdLength = billCostExcelDTO.getThirdLength();
        if(StringUtils.isNotBlank(thirdHeight)) {
            updateDataDTO.setThirdLength(new BigDecimal(thirdLength));
        }
        //实重
        String thirdActualWeight = billCostExcelDTO.getThirdActualWeight();
        if(StringUtils.isNotBlank(thirdActualWeight)) {
            updateDataDTO.setThirdActualWeight(new BigDecimal(thirdActualWeight));
        }
        return updateDataDTO;
    }

    private List<AddDataDTO> buildAddDTO(LogisticsBillCostDTO.UpdateDTO updateDataDTO, List<TmsCostDetailDTO.UpdateDTO> updateDetailList) {
        Map<String, AddDataDTO> dtoMap = new LinkedHashMap<>();
        for (TmsCostDetailDTO.UpdateDTO updateDTO : updateDetailList) {
            AddDataDTO addDataDTO = dtoMap.computeIfAbsent(updateDTO.getCfgCostId(), cfgCostId -> {
                AddDataDTO dto = new AddDataDTO();
                dto.setPayType(updateDataDTO.getPayType());
                dto.setSourceId(updateDataDTO.getId());
                dto.setBillingWeight(updateDataDTO.getBillingWeight());
                dto.setBillingWeightLogistics(updateDataDTO.getBillingWeightLogistics());
                dto.setReconciliationMonth(updateDataDTO.getReconciliationMonth());
                dto.setThirdLength(updateDataDTO.getThirdLength());
                dto.setThirdHeight(updateDataDTO.getThirdHeight());
                dto.setThirdWidth(updateDataDTO.getThirdWidth());
                dto.setThirdActualWeight(updateDataDTO.getThirdActualWeight());
                dto.setCfgCostId(updateDTO.getCfgCostId());
                dto.setCostValue(BigDecimal.ZERO);
                return dto;
            });

            BigDecimal costValue = ObjectUtil.defaultIfNull(updateDTO.getCostValue(), BigDecimal.ZERO);
            if (LogisticsBillCostTypeEnum.ESTIMATED.getCode().equals(updateDTO.getType())) {
                addDataDTO.setEstimatedValue(ObjectUtil.defaultIfNull(addDataDTO.getEstimatedValue(), BigDecimal.ZERO).add(costValue));
                addDataDTO.setEstimatedCurrency(updateDTO.getCurrency());
            } else {
                addDataDTO.setCostValue(addDataDTO.getCostValue().add(costValue));
                addDataDTO.setCurrency(updateDTO.getCurrency());
            }
        }
        return new ArrayList<>(dtoMap.values());
    }

    @Override
    public Boolean updateShopCharge(LogisticsBillCostDTO.UpdateShopChargeDTO dto) {
        SysUserInfoEntity user = userInfoFeign.info(dto.getShopChargeId());
        if (ObjectUtil.isEmpty(user)) {
            throw new ServiceException("未找到店铺负责人");
        }
        baseMapper.updateShopChargeId(dto.getShopId(),dto.getShopChargeId(),user.getUserName());
        return Boolean.TRUE;
    }

    @Override
    public BigDecimal getActualLogisticCost(String soId) {
        if(StringUtils.isBlank(soId)){
            return BigDecimal.ZERO;
        }
        return baseMapper.getActualLogisticCost(soId);
    }

    @Override
    public void updateStatusByLogisticsBillIdsAndReconciliationId(List<String> logisticsBillIds, String reconciliationId, String status) {
        if (CollectionUtils.isEmpty(logisticsBillIds) && CharSequenceUtil.isBlank(reconciliationId)){
            return;
        }
        this.lambdaUpdate().eq(CharSequenceUtil.isNotBlank(reconciliationId), LogisticsBillCostEntity::getReconciliationId, reconciliationId)
                .in(CollectionUtils.isNotEmpty(logisticsBillIds), LogisticsBillCostEntity::getLogisticsBillId, logisticsBillIds)
                .set(LogisticsBillCostEntity::getReconciliationStatus,status).update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLogisticsBillCost(TmsFirstMileReconciliationEntity mainEntity, List<TmsFirstMileReconciliationDetailEntity> detailEntityList, Map<String, TmsFirstMileReconciliationDetailEntity> actualMap) {
        // 需要变动的物流单
        List<String> billIds = detailEntityList.stream()
                .filter(e -> e.getType().equalsIgnoreCase(DetailReconciliationTypeEnum.ESTIMATED.getCode()))
                .map(TmsFirstMileReconciliationDetailEntity::getSourceId)
                .distinct()
                .collect(Collectors.toList());
        //对应物流单费用记录
        List<LogisticsBillCostEntity> billEntityList = this.listByLogisticsBillIdList(billIds);
        //物流单记录
        List<LogisticsBillEntity> logisticsBillEntityList = logisticsBillService.listByIds(billIds);
        Map<String, TmsFirstMileReconciliationDetailEntity> estimatedMap = detailEntityList.stream()
                .filter(e -> Objects.nonNull(e)
                        && DetailReconciliationTypeEnum.ESTIMATED.getCode().equalsIgnoreCase(e.getType())
                        && CharSequenceUtil.isNotBlank(e.getSourceId()))
                .collect(Collectors.toMap(TmsFirstMileReconciliationDetailEntity::getSourceId, e -> e, (first, second) -> first));
        List<LogisticsBillCostEntity> updateBillList = new ArrayList<>();
        //根据物流单进行处理
        List<TmsFirstMileReconciliationDetailEntity> detailEntityList1 = detailEntityList.stream().filter(e -> DetailReconciliationTypeEnum.ACTUAL.getCode().equals(e.getType())).collect(Collectors.toList());
        for (TmsFirstMileReconciliationDetailEntity detailEntity : detailEntityList1){
            //获取费用记录
            LogisticsBillCostEntity entity = billEntityList.stream().filter(e -> Objects.nonNull(e) && Objects.equals(detailEntity.getSourceId(), e.getLogisticsBillId())
                        && Objects.equals(mainEntity.getId(), e.getReconciliationId())).findFirst().orElse(null);
            //当费用为null时再看下是否有空对账单记录
            if (Objects.isNull(entity)){
                entity = billEntityList.stream().filter(e -> Objects.nonNull(e) && Objects.equals(detailEntity.getSourceId(), e.getLogisticsBillId())
                        && CharSequenceUtil.isBlank(e.getReconciliationId())).findFirst().orElse(null);
            }
            //重置费用表记录
            if (Objects.isNull(entity)){
                entity = new LogisticsBillCostEntity();
                LogisticsBillEntity logisticsBillEntity = logisticsBillEntityList.stream().filter(e -> Objects.nonNull(e) && Objects.equals(detailEntity.getSourceId(), e.getId())).findFirst().orElse(null);
                if (Objects.isNull(logisticsBillEntity)){
                    continue;
                }
                entity.setLogisticsBillId(detailEntity.getSourceId());
            }
            entity.setCurrency(mainEntity.getCurrency());
            backfillEstimatedWeight(entity, estimatedMap.get(detailEntity.getSourceId()));
            //填充信息
            this.handleData(entity);
            TmsFirstMileReconciliationDetailEntity actualDetailEntity = actualMap.get(detailEntity.getSourceId());
            if (null == actualDetailEntity) {
                // 移除
                entity.setReconciliationStatus(ReconciliationStatusEnum.TO_BE_GENERATED.getCode());
                continue;
            }
            // 更新实际重量和体积重, 计费重
            entity.setVolumeWeightLogistics(defaultWeight(actualDetailEntity.getVolumeWeight()));
            entity.setWeightLogistics(defaultWeight(actualDetailEntity.getActualWeight()));
            entity.setBillingWeightLogistics(maxWeight(actualDetailEntity.getVolumeWeight(), actualDetailEntity.getActualWeight()));
            // 设置实际费用明细
            if (!CollectionUtils.isEmpty(actualDetailEntity.getUpdateList())) {
                List<TmsCostDetailDTO.UpdateDTO> updateList = BeanMapperUtils.copyList(TmsCostDetailDTO.UpdateDTO.class, actualDetailEntity.getUpdateList());
                updateList.forEach(e -> e.setSourceType(SourceTypeEnum.FIRST_MILE_LOGISTICS_BILL_COST.getCode()));
                entity.setUpdateList(updateList);
            }else {
                //如果费用明细为空，则判断对账单次数是否大于1 大于1则创建费用明细
                if (Objects.equals(DetailReconciliationTypeEnum.ACTUAL.getCode(), detailEntity.getType())){
                	List<TmsCostDetailEntity> dbActualList = tmsCostDetailService.lambdaQuery().eq(TmsCostDetailEntity::getMainId, entity.getId()).eq(TmsCostDetailEntity::getType, DetailReconciliationTypeEnum.ACTUAL.getCode()).list();
                    if(CollUtil.isEmpty(dbActualList)) {
                    	buildFirstMileCostDetail(detailEntity,entity);
                    }else {
                    	entity.setUpdateList(BeanUtil.copyToList(dbActualList, TmsCostDetailDTO.UpdateDTO.class));
                    }
                }
            }
            entity.setReconciliationStatus(actualDetailEntity.getStatus());
            //填充下推对账单id
            entity.setReconciliationId(mainEntity.getId());
            updateBillList.add(entity);
        }
        // 批量更新物流单状态
        if (!CollectionUtils.isEmpty(updateBillList)) {
            // 更新费用信息
            this.saveOrUpdateBatch(updateBillList);
            // 更新明细信息
            List<String> delActualCostIds = new LinkedList<>();
            List<String> delActualCostDetailIds = new LinkedList<>();
            for (LogisticsBillCostEntity costEntity : updateBillList) {
                // 检查历史明细是否需要移除
                if (ReconciliationStatusEnum.TO_BE_GENERATED.getCode().equalsIgnoreCase(costEntity.getReconciliationStatus())){
                    delActualCostIds.add(costEntity.getId());
                    continue;
                }
                List<TmsCostDetailDTO.UpdateDTO> updateList = costEntity.getUpdateList();
                if (CollectionUtils.isEmpty(updateList)) {
                    delActualCostDetailIds.add(costEntity.getId());
                    continue;
                }
                tmsCostDetailService.batchUpdate(updateList, costEntity.getId(), DictCostAttributionEnum.FIRST_MILE,Boolean.FALSE);
            }
            // 移除实际费用
            if (!CollectionUtils.isEmpty(delActualCostIds)){
                tmsCostDetailService.updateActual0ByMainId(delActualCostIds);
            }
            //移除实际费用明细
            if (!CollectionUtils.isEmpty(delActualCostDetailIds)){
                tmsCostDetailService.updateActual0ByMainId(delActualCostDetailIds);
            }
        }
    }

    /**
     * 使用预估明细补齐费用记录的预估重量。
     *
     * @param entity 物流费用记录
     * @param estimatedDetailEntity 预估对账明细
     */
    private void backfillEstimatedWeight(LogisticsBillCostEntity entity, TmsFirstMileReconciliationDetailEntity estimatedDetailEntity) {
        if (Objects.isNull(entity) || Objects.isNull(estimatedDetailEntity)) {
            return;
        }
        if (isBlankWeight(entity.getActualWeight()) && Objects.nonNull(estimatedDetailEntity.getActualWeight())) {
            entity.setActualWeight(estimatedDetailEntity.getActualWeight());
        }
        if (isBlankWeight(entity.getVolumeWeight()) && Objects.nonNull(estimatedDetailEntity.getVolumeWeight())) {
            entity.setVolumeWeight(estimatedDetailEntity.getVolumeWeight());
        }
        if (isBlankWeight(entity.getBillingWeight()) && Objects.nonNull(estimatedDetailEntity.getBillingWeight())) {
            entity.setBillingWeight(estimatedDetailEntity.getBillingWeight());
        }
    }

    /**
     * 判断重量是否为空白值。
     *
     * @param value 重量
     * @return 为空或零时返回true
     */
    private boolean isBlankWeight(BigDecimal value) {
        return Objects.isNull(value) || BigDecimal.ZERO.compareTo(value) == 0;
    }

    /**
     * 获取重量默认值。
     *
     * @param value 重量
     * @return 非空重量或零
     */
    private BigDecimal defaultWeight(BigDecimal value) {
        return Objects.nonNull(value) ? value : BigDecimal.ZERO;
    }

    /**
     * 获取物流商计费重。
     *
     * @param volumeWeight 体积重
     * @param actualWeight 实重
     * @return 体积重和实重的较大值
     */
    private BigDecimal maxWeight(BigDecimal volumeWeight, BigDecimal actualWeight) {
        return defaultWeight(volumeWeight).max(defaultWeight(actualWeight));
    }

    /**
     * 构建头程费用明细
     * @param detailEntity
     * @param entity
     */
    private void buildFirstMileCostDetail(TmsFirstMileReconciliationDetailEntity detailEntity, LogisticsBillCostEntity entity) {
        List<TmsCfgCostEntity> tmsCfgCostList = tmsCfgCostService.listByCostAttribution(DictCostAttributionEnum.FIRST_MILE.getCode());
        if (CollectionUtils.isEmpty(tmsCfgCostList) || Objects.isNull(detailEntity)){
            return;
        }
        List<TmsCostDetailEntity> tmsCostDetailEntities = null;
        if (CharSequenceUtil.isNotBlank(entity.getId())){
            tmsCostDetailEntities = tmsCostDetailService.listByMainIdList(Collections.singletonList(entity.getId()));
        }
        List<TmsCostDetailDTO.UpdateDTO> updateList = new ArrayList<>();
        //物流费用
        BigDecimal shippingCost = Objects.nonNull(detailEntity.getShippingCost()) ? detailEntity.getShippingCost() : BigDecimal.ZERO;
        if (BigDecimal.ZERO.compareTo(shippingCost) != 0){
            TmsCfgCostEntity tmsCfgCostEntity = tmsCfgCostList.stream().filter(e -> Objects.equals(DictCostAttributionEnum.FIRST_MILE.getCode(), e.getDictCostAttribution())
                    && Objects.equals(DictCostCategoryEnum.SHIPPING_COST.getCode(), e.getDictCostCategory()) && e.getIsDefault()).findFirst().orElse(null);
            //不存在默认配置时，取第一个配置
            if (Objects.isNull(tmsCfgCostEntity)){
                tmsCfgCostEntity = tmsCfgCostList.stream().filter(e -> Objects.equals(DictCostAttributionEnum.FIRST_MILE.getCode(), e.getDictCostAttribution())
                        && Objects.equals(DictCostCategoryEnum.SHIPPING_COST.getCode(), e.getDictCostCategory())).findFirst().orElse(null);
            }
            if (Objects.nonNull(tmsCfgCostEntity)){
                TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
                String cfgCostEntityId = tmsCfgCostEntity.getId();
                //检查是否存在记录
                if (CollectionUtils.isNotEmpty(tmsCostDetailEntities)){
                    TmsCostDetailEntity tmsCostDetailEntity = tmsCostDetailEntities.stream().filter(e -> detailEntity.getType().equals(e.getType())
                            && SourceTypeEnum.FIRST_MILE_LOGISTICS_BILL_COST.getCode().equals(e.getSourceType())
                            && cfgCostEntityId.equals(e.getCfgCostId())).findFirst().orElse(null);
                    if (Objects.nonNull(tmsCostDetailEntity)){
                        updateDTO.setId(tmsCostDetailEntity.getId());
                    }
                }
                updateDTO.setType(detailEntity.getType());
                updateDTO.setCfgCostId(cfgCostEntityId);
                updateDTO.setCostValue(shippingCost);
                updateDTO.setSourceType(SourceTypeEnum.FIRST_MILE_LOGISTICS_BILL_COST.getCode());
                updateDTO.setDictCostCategory(DictCostCategoryEnum.SHIPPING_COST.getCode());
                updateList.add(updateDTO);
            }
        }
        //报关费用
        BigDecimal declareCost = Objects.nonNull(detailEntity.getDeclareCost()) ? detailEntity.getDeclareCost() : BigDecimal.ZERO;
        if (BigDecimal.ZERO.compareTo(declareCost) != 0){
            TmsCfgCostEntity tmsCfgCostEntity = tmsCfgCostList.stream().filter(e -> Objects.equals(DictCostAttributionEnum.FIRST_MILE.getCode(), e.getDictCostAttribution())
                    && Objects.equals(DictCostCategoryEnum.DECLARE_COST.getCode(), e.getDictCostCategory()) && e.getIsDefault()).findFirst().orElse(null);
            //不存在默认配置时，取第一个配置
            if (Objects.isNull(tmsCfgCostEntity)){
                tmsCfgCostEntity = tmsCfgCostList.stream().filter(e -> Objects.equals(DictCostAttributionEnum.FIRST_MILE.getCode(), e.getDictCostAttribution())
                        && Objects.equals(DictCostCategoryEnum.DECLARE_COST.getCode(), e.getDictCostCategory())).findFirst().orElse(null);
            }
            if (Objects.nonNull(tmsCfgCostEntity)){
                TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
                String cfgCostEntityId = tmsCfgCostEntity.getId();
                //检查是否存在记录
                if (CollectionUtils.isNotEmpty(tmsCostDetailEntities)){
                    TmsCostDetailEntity tmsCostDetailEntity = tmsCostDetailEntities.stream().filter(e -> detailEntity.getType().equals(e.getType())
                            && SourceTypeEnum.FIRST_MILE_LOGISTICS_BILL_COST.getCode().equals(e.getSourceType())
                            && cfgCostEntityId.equals(e.getCfgCostId())).findFirst().orElse(null);
                    if (Objects.nonNull(tmsCostDetailEntity)){
                        updateDTO.setId(tmsCostDetailEntity.getId());
                    }
                }
                updateDTO.setType(detailEntity.getType());
                updateDTO.setCfgCostId(cfgCostEntityId);
                updateDTO.setCostValue(declareCost);
                updateDTO.setSourceType(SourceTypeEnum.FIRST_MILE_LOGISTICS_BILL_COST.getCode());
                updateDTO.setDictCostCategory(DictCostCategoryEnum.DECLARE_COST.getCode());
                updateList.add(updateDTO);
            }
        }
        //其他费用
        BigDecimal otherCost = Objects.nonNull(detailEntity.getOtherCost()) ? detailEntity.getOtherCost() : BigDecimal.ZERO;
        if (BigDecimal.ZERO.compareTo(otherCost) != 0){
            TmsCfgCostEntity tmsCfgCostEntity = tmsCfgCostList.stream().filter(e -> Objects.equals(DictCostAttributionEnum.FIRST_MILE.getCode(), e.getDictCostAttribution())
                    && Objects.equals(DictCostCategoryEnum.OTHER_COST.getCode(), e.getDictCostCategory()) && e.getIsDefault()).findFirst().orElse(null);
            //不存在默认配置时，取第一个配置
            if (Objects.isNull(tmsCfgCostEntity)){
                tmsCfgCostEntity = tmsCfgCostList.stream().filter(e -> Objects.equals(DictCostAttributionEnum.FIRST_MILE.getCode(), e.getDictCostAttribution())
                        && Objects.equals(DictCostCategoryEnum.OTHER_COST.getCode(), e.getDictCostCategory())).findFirst().orElse(null);
            }
            if (Objects.nonNull(tmsCfgCostEntity)){
                TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
                String cfgCostEntityId = tmsCfgCostEntity.getId();
                //检查是否存在记录
                if (CollectionUtils.isNotEmpty(tmsCostDetailEntities)){
                    TmsCostDetailEntity tmsCostDetailEntity = tmsCostDetailEntities.stream().filter(e -> detailEntity.getType().equals(e.getType())
                            && SourceTypeEnum.FIRST_MILE_LOGISTICS_BILL_COST.getCode().equals(e.getSourceType())
                            && cfgCostEntityId.equals(e.getCfgCostId())).findFirst().orElse(null);
                    if (Objects.nonNull(tmsCostDetailEntity)){
                        updateDTO.setId(tmsCostDetailEntity.getId());
                    }
                }
                updateDTO.setType(detailEntity.getType());
                updateDTO.setCfgCostId(cfgCostEntityId);
                updateDTO.setCostValue(otherCost);
                updateDTO.setSourceType(SourceTypeEnum.FIRST_MILE_LOGISTICS_BILL_COST.getCode());
                updateDTO.setDictCostCategory(DictCostCategoryEnum.OTHER_COST.getCode());
                updateList.add(updateDTO);
            }
        }
        //其他税费
        BigDecimal otherTaxCost = Objects.nonNull(detailEntity.getOtherTaxCost()) ? detailEntity.getOtherTaxCost() : BigDecimal.ZERO;
        if (BigDecimal.ZERO.compareTo(otherTaxCost) != 0){
            TmsCfgCostEntity tmsCfgCostEntity = tmsCfgCostList.stream().filter(e -> Objects.equals(DictCostAttributionEnum.FIRST_MILE.getCode(), e.getDictCostAttribution())
                    && Objects.equals(DictCostCategoryEnum.OTHER_TAX_FEE.getCode(), e.getDictCostCategory()) && e.getIsDefault()).findFirst().orElse(null);
            //不存在默认配置时，取第一个配置
            if (Objects.isNull(tmsCfgCostEntity)){
                tmsCfgCostEntity = tmsCfgCostList.stream().filter(e -> Objects.equals(DictCostAttributionEnum.FIRST_MILE.getCode(), e.getDictCostAttribution())
                        && Objects.equals(DictCostCategoryEnum.OTHER_TAX_FEE.getCode(), e.getDictCostCategory())).findFirst().orElse(null);
            }
            if (Objects.nonNull(tmsCfgCostEntity)){
                TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
                String cfgCostEntityId = tmsCfgCostEntity.getId();
                //检查是否存在记录
                if (CollectionUtils.isNotEmpty(tmsCostDetailEntities)){
                    TmsCostDetailEntity tmsCostDetailEntity = tmsCostDetailEntities.stream().filter(e -> detailEntity.getType().equals(e.getType())
                            && SourceTypeEnum.FIRST_MILE_LOGISTICS_BILL_COST.getCode().equals(e.getSourceType())
                            && cfgCostEntityId.equals(e.getCfgCostId())).findFirst().orElse(null);
                    if (Objects.nonNull(tmsCostDetailEntity)){
                        updateDTO.setId(tmsCostDetailEntity.getId());
                    }
                }
                updateDTO.setType(detailEntity.getType());
                updateDTO.setCfgCostId(cfgCostEntityId);
                updateDTO.setCostValue(otherTaxCost);
                updateDTO.setSourceType(SourceTypeEnum.FIRST_MILE_LOGISTICS_BILL_COST.getCode());
                updateDTO.setDictCostCategory(DictCostCategoryEnum.OTHER_TAX_FEE.getCode());
                updateList.add(updateDTO);
            }
        }
        if (CollectionUtils.isNotEmpty(updateList)){
            entity.setUpdateList(updateList);
        }
    }

    @Override
    public PagingVO<LogisticsBillCostDTO.ListDTO> exportLogisticsBillCost(PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<LogisticsBillCostDTO.ListDTO> page = this.baseMapper.listByExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            //数据赋值处理
            handleDataPaging(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public void removeByReconciliationIds(String reconciliationId, List<String> logisticsBillIds) {
        if (CollectionUtils.isEmpty(logisticsBillIds) || CharSequenceUtil.isBlank(reconciliationId)){
            return;
        }
        List<LogisticsBillCostEntity> list = this.lambdaQuery().eq(LogisticsBillCostEntity::getReconciliationId, reconciliationId).in(LogisticsBillCostEntity::getLogisticsBillId,logisticsBillIds).list();
        if (CollectionUtils.isNotEmpty(list)){
            List<String> costIds = list.stream().map(LogisticsBillCostEntity::getId).distinct().collect(Collectors.toList());
            this.tmsCostDetailService.removeByMainIds(costIds);
            this.removeByIds(costIds);
        }
    }

    /**
     * @param excelDTO
     * @param logisticsBillVo
     * @param importType
     * @return List<String>
     * @description: 数据验证
     * @author Will
     * @date: 2023/11/14 20:05
     */
    private void checkImportData (LogisticsBillCostExcelDTO excelDTO, LogisticsBillDTO.LogisticsBillVo logisticsBillVo,List<LogisticsBillCostEntity> logisticsBillCostList
            ,  String dictCostAttribution, String importType,List<String> errorMsgList) {
        if (CharSequenceUtil.isBlank(excelDTO.getPlatformCode()) && CharSequenceUtil.isBlank(excelDTO.getSoCode())
                && CharSequenceUtil.isBlank(excelDTO.getSoDeliveryCode()) && CharSequenceUtil.isBlank(excelDTO.getTrackNo())) {
            throw new ServiceException(ApiError.LOGISTICS_BILL_COST_IMPORT_NOT_EXIST_BILL);
        }
        //物流单明细
        if (CharSequenceUtil.isBlank(logisticsBillVo.getDetailId())) {
            errorMsgList.add("未找到对应的物流单明细");
        }
        //物流费用单
        List<LogisticsBillCostEntity> logisticsBillCostEntityList = logisticsBillCostList.stream()
                .filter(obj -> obj.getLogisticsBillId().equals(logisticsBillVo.getId())
                        && CharSequenceUtil.equals(logisticsBillVo.getTrackNo(),obj.getTrackNo())
                        && CharSequenceUtil.equals(excelDTO.getPayType(),obj.getPayType()))
                .collect(Collectors.toList());
        LogisticsBillCostEntity logisticsBillCostEntity = null;
        if (CollUtil.isEmpty(logisticsBillCostEntityList)) {
             errorMsgList.add("未找到对应对账类型的物流费用单");
        } else {
            if(logisticsBillCostEntityList.size() > 1) {
                if (CfgLogisticsCostImportImportTypeEnum.IMPORT_UPDATE.getCode().equals(importType)) {
                    long count = logisticsBillCostEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getReconciliationStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())
                                    || (CharSequenceUtil.equals(ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode(),obj.getReconciliationStatus())
                                    && CharSequenceUtil.equals(obj.getCheckStatus(),LogisticsBillCostCheckStatusEnum.CHECKING.getCode())))
                            .count();
                    if (count > 1) {
                        errorMsgList.add("出库单和运输单号对应对账类型的物流费用单有多条，请在页面编辑指定物流费用单");
                    }
                    if (count == 0) {
                        errorMsgList.add("出库单和运输单号对应对账类型的物流费用单无可更新的数据，请核查");
                    }
                } else {
                    //新增时判断是否已存在相同对账月份
                    long hasCount = logisticsBillCostEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getReconciliationMonth(), logisticsBillVo.getReconciliationMonth())).count();
                    if (hasCount > 0) {
                        errorMsgList.add("已存在相同对账月份的物流费用单，不支持新增");
                    }
                    long confirmCount = logisticsBillCostEntityList.stream().filter(obj -> !CharSequenceUtil.equals(obj.getReconciliationMonth(), logisticsBillVo.getReconciliationMonth()) && CharSequenceUtil.equals(obj.getReconciliationStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())).count();
                    if (confirmCount > 0) {
                        errorMsgList.add("已存在未确认的物流费用单，不支持新增");
                    }
                }
            }else {
                logisticsBillCostEntity = logisticsBillCostEntityList.get(0);
                if (!CharSequenceUtil.equals(logisticsBillCostEntity.getType(),dictCostAttribution)) {
                    errorMsgList.add(CharSequenceUtil.format("需要导入【{}】物流单费用信息",DictCostAttributionEnum.getName(dictCostAttribution)));
                }
                if (CfgLogisticsCostImportImportTypeEnum.IMPORT_UPDATE.getCode().equals(importType) ) {
                    if (!CharSequenceUtil.equals(ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(),logisticsBillCostEntity.getReconciliationStatus())
                            && !CharSequenceUtil.equals(ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode(),logisticsBillCostEntity.getReconciliationStatus())) {
                        errorMsgList.add("物流费用单非待确认不支持更新");
                    }
                    if (CharSequenceUtil.equals(ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode(),logisticsBillCostEntity.getReconciliationStatus())
                            && !CharSequenceUtil.equals(logisticsBillCostEntity.getCheckStatus(),LogisticsBillCostCheckStatusEnum.CHECKING.getCode())) {
                        errorMsgList.add("暂估确认物流费用单已下推费用分摊，不支持更新");
                    }
                } else{
                    if (CharSequenceUtil.equals(logisticsBillVo.getReconciliationMonth(),logisticsBillCostEntity.getReconciliationMonth())) {
                        errorMsgList.add("已存在相同对账月份的物流费用单，不支持新增");
                    }
                    if (!CharSequenceUtil.equals(logisticsBillVo.getReconciliationMonth(), logisticsBillCostEntity.getReconciliationMonth()) && CharSequenceUtil.equals(logisticsBillCostEntity.getReconciliationStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())) {
                        errorMsgList.add("已存在未确认的物流费用单，不支持新增");
                    }
                }
            }
        }
        if (ObjectUtil.isNotEmpty(logisticsBillCostEntity)){
            String currency = CharSequenceUtil.isBlank(excelDTO.getCurrency()) ? logisticsBillCostEntity.getCurrency() : excelDTO.getCurrency();
            excelDTO.setCurrency(currency);
        }
    }


    @Override
    public void removeRefByReconciliationIds(String reconciliationId, List<String> logisticsBillIds) {
        if (CollectionUtils.isEmpty(logisticsBillIds) || CharSequenceUtil.isBlank(reconciliationId)){
            return;
        }
        List<LogisticsBillCostEntity> list = this.lambdaQuery().eq(LogisticsBillCostEntity::getReconciliationId, reconciliationId).in(LogisticsBillCostEntity::getLogisticsBillId,logisticsBillIds).list();
        //移除对账单id记录
        this.lambdaUpdate().eq(LogisticsBillCostEntity::getReconciliationId, reconciliationId).in(LogisticsBillCostEntity::getLogisticsBillId,logisticsBillIds)
                .set(LogisticsBillCostEntity::getReconciliationId, "")
                .set(LogisticsBillCostEntity::getReconciliationStatus, ReconciliationStatusEnum.TO_BE_GENERATED.getCode())
                .update();

        if (CollectionUtils.isNotEmpty(list)){
            List<String> costIds = list.stream().map(LogisticsBillCostEntity::getId).distinct().collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(costIds)){
                List<TmsCostDetailEntity> tmsCostDetailEntities = tmsCostDetailService.listByMainIdList(costIds);
                if (CollectionUtils.isNotEmpty(tmsCostDetailEntities)){
                    List<String> ids = tmsCostDetailEntities.stream().filter(e -> LogisticsBillCostTypeEnum.ACTUAL.getCode().equals(e.getType())).map(TmsCostDetailEntity::getId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(ids)){
                        tmsCostDetailService.removeByIds(ids);
                    }
                }
            }
        }
    }

    @Override
    public void initExchangeRate() {
        List<LogisticsBillCostDTO.ExchangeRateDTO> dtoList = baseMapper.listBillCostByExchangeRate();
        if (CollUtil.isEmpty(dtoList)){
            return;
        }
        List<List<LogisticsBillCostDTO.ExchangeRateDTO>> partition = ListUtil.partition(dtoList, 100);
        for (List<LogisticsBillCostDTO.ExchangeRateDTO> list : partition){
            if (CollUtil.isEmpty(list)){
                return;
            }
            for (LogisticsBillCostDTO.ExchangeRateDTO entity : list){
                if (CurrencyEnum.CNY.getCurrencyCode().equals(entity.getCurrency()) || CharSequenceUtil.isBlank(entity.getCurrency())){
                    this.lambdaUpdate().set(LogisticsBillCostEntity::getExchangeRate, BigDecimal.ONE)
                            .set(CharSequenceUtil.isBlank(entity.getCurrency()), LogisticsBillCostEntity::getCurrency, CurrencyEnum.CNY.getCurrencyCode())
                            .eq(LogisticsBillCostEntity::getId, entity.getId()).update();
                }else {
                    String currentDate = entity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    BigDecimal rate = dmpTaskFeign.getRate(currentDate, entity.getCurrency());
                    if (Objects.nonNull(rate)){
                        this.lambdaUpdate().set(LogisticsBillCostEntity::getExchangeRate, rate).eq(LogisticsBillCostEntity::getId, entity.getId()).update();
                    }
                }
            }
        }
    }
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
	@Override
	public List<BaseResultDTO.AddDTO> addPayAndRefund(List<LogisticsBillCostDTO.AddDataDTO> dtoList, Map<String, TmsCfgCostEntity> cfgCostCache) {
    	List<BaseResultDTO.AddDTO> addList = new ArrayList<>();
    	Map<String, List<AddDataDTO>> sourceIdDtoMaps = dtoList.stream().collect(Collectors.groupingBy(LogisticsBillCostDTO.AddDataDTO::getSourceId));
    	for(Map.Entry<String, List<AddDataDTO>> sourceIdDtoMap : sourceIdDtoMaps.entrySet()) {
    		List<AddDataDTO> value = sourceIdDtoMap.getValue();

    		value.forEach(v -> {
    			if(CharSequenceUtil.isBlank(v.getCurrency())) {
    				v.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
    			}
    			if(CharSequenceUtil.isBlank(v.getEstimatedCurrency())) {
    				v.setEstimatedCurrency(CurrencyEnum.CNY.getCurrencyCode());
    			}
    		});

    		Map<String, List<AddDataDTO>> cfgCostIdMaps = value.stream().collect(Collectors.groupingBy(AddDataDTO::getCfgCostId));
    		value = new ArrayList<>();
    		for(Map.Entry<String, List<AddDataDTO>> cfgCostIdMap : cfgCostIdMaps.entrySet()) {
    			List<AddDataDTO> groupValue = cfgCostIdMap.getValue();
    			AddDataDTO v = groupValue.get(0);
				String estimatedCurrency = v.getEstimatedCurrency();
				String currency = v.getCurrency();
    			if(groupValue.stream().anyMatch(g -> !estimatedCurrency.equals(g.getEstimatedCurrency()))) {
    				throw new ServiceException("【" + tmsCfgCostService.getById(cfgCostIdMap.getKey()).getCostName() + "】相同费用类型预估金额存在不同币别");
    			}
    			if(groupValue.stream().anyMatch(g -> !currency.equals(g.getCurrency()))) {
    				throw new ServiceException("【" + tmsCfgCostService.getById(cfgCostIdMap.getKey()).getCostName() + "】相同费用类型实际金额存在不同币别");
    			}
    			v.setEstimatedValue(groupValue.stream().filter(g -> g.getEstimatedValue() != null).map(AddDataDTO::getEstimatedValue).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
    			v.setCostValue(groupValue.stream().filter(g -> g.getCostValue() != null).map(AddDataDTO::getCostValue).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
    			value.add(v);
    		}

			AddDataDTO dto = value.get(0);
    		String sourceId = dto.getSourceId();
    		LogisticsBillCostEntity logisticsBillCostEntity = getById(sourceId);
    		LogisticsBillCostDTO.AddDTO addDTO = new LogisticsBillCostDTO.AddDTO();
    		addDTO.setPayType(dto.getPayType());
    		addDTO.setReconciliationStatus(ReconciliationStatusEnum.TO_BE_CONFIRM.getCode());
    		addDTO.setLogisticsBillId(logisticsBillCostEntity.getLogisticsBillId());
    		addDTO.setLogisticsBillDetailId(logisticsBillCostEntity.getLogisticsBillDetailId());
    		addDTO.setActualWeight(logisticsBillCostEntity.getActualWeight());
    		addDTO.setVolumeWeight(logisticsBillCostEntity.getVolumeWeight());
    		addDTO.setBillingWeightLogistics(logisticsBillCostEntity.getBillingWeightLogistics());
    		String currency = dto.getCurrency();
    		if(org.apache.commons.lang3.StringUtils.isBlank(currency)) {
    			currency = CurrencyEnum.CNY.getCurrencyCode();
    		}

    		addDTO.setCurrency(currency);

    		addDTO.setTrackNo(logisticsBillCostEntity.getTrackNo());
    		addDTO.setChannelId(logisticsBillCostEntity.getChannelId());
    		addDTO.setWeightLogistics(logisticsBillCostEntity.getWeightLogistics());
    		addDTO.setVolumeWeightLogistics(logisticsBillCostEntity.getVolumeWeightLogistics());

            addDTO.setReconciliationMonth(dto.getReconciliationMonth());
            addDTO.setThirdHeight(dto.getThirdHeight());
            addDTO.setThirdWidth(dto.getThirdWidth());
            addDTO.setThirdLength(dto.getThirdLength());
            addDTO.setThirdActualWeight(dto.getThirdActualWeight());

    		List<TmsCostDetailDTO.AddDTO>  costDetailList = new ArrayList<>();
    		for(AddDataDTO detailDTO : value) {
    			TmsCostDetailDTO.AddDTO add = new TmsCostDetailDTO.AddDTO();
    			add.setCfgCostId(detailDTO.getCfgCostId());
    			add.setCostValue(detailDTO.getCostValue());
    			add.setType(LogisticsBillCostTypeEnum.ACTUAL.getCode());
    			add.setSourceType(SourceTypeEnum.LOGISTICS_BILL_COST.getCode());
    			add.setCurrency(detailDTO.getCurrency());
    			costDetailList.add(add);

    			BigDecimal estimatedValue = detailDTO.getEstimatedValue();
    			if(estimatedValue != null && estimatedValue.compareTo(BigDecimal.ZERO) != 0) {
    				add = new TmsCostDetailDTO.AddDTO();
    				add.setCfgCostId(detailDTO.getCfgCostId());
    				add.setCostValue(estimatedValue);
    				add.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
    				add.setSourceType(SourceTypeEnum.LOGISTICS_BILL_COST.getCode());
    				add.setCurrency(detailDTO.getEstimatedCurrency());
    				costDetailList.add(add);
    			}
    		}
    		addDTO.setCostDetailList(costDetailList);
    		try {
				addList.add(service.add(addDTO, cfgCostCache));
			} catch (ServiceException e) {
				throw new ServiceException("物流运单号：" + logisticsBillCostEntity.getTransportNo() + e.getMessage());
			}
    	}

		return addList;
	}

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
	@Override
	public void addPayAndRefundConfirm(ConfirmAddDataDTO dto) {
    	if(dto.getConfirmTime() == null) {
    		dto.setConfirmTime(LocalDateTime.now());
    	}
    	LocalDateTime confirmTime = dto.getConfirmTime();
		List<AddDTO> dtoList = service.addPayAndRefund(dto.getAddDataDTOList(), null);
		dtoList.forEach(addDTO -> service.updateReconciliationStatus(addDTO.getId(), ReconciliationStatusEnum.CONFIRMED.getCode(), confirmTime));
	}

	@Override
	public BatchResultDTO updatePayStatus(String id, String payStatus, LocalDateTime payTime) {
        LogisticsBillCostEntity entity = Optional.ofNullable(super.getById(id)).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "尾程费用(自发货)"));
        if(payStatus.equals(entity.getPayStatus())) {
        	throw new ServiceException("修改前后支付状态一致");
        }
        if(payStatus.equals("paid")) {
        	if(payTime == null) {
        		throw new ServiceException("支付状态修改为已付款/已退款，付款/退款时间不能为空");
        	}
        	if(!ReconciliationStatusEnum.CONFIRMED.getCode().equals(entity.getReconciliationStatus())) {
        		throw new ServiceException("支付状态修改为已付款/已退款，对账状态必须为账单确认");
        	}
        }else {
        	payTime = null;
        }
        lambdaUpdate().eq(LogisticsBillCostEntity::getId, id)
			        .set(LogisticsBillCostEntity::getPayStatus, payStatus)
			        .set(LogisticsBillCostEntity::getPayTime, payTime)
			        .update();


        // 修改物流大表的支付状态
        List<SmallBagCostAllocationMainEntity> costAllocationMainEntities = smallBagCostAllocationMainService.lambdaQuery().eq(SmallBagCostAllocationMainEntity::getCostId, id).list();
        List<String> ids = costAllocationMainEntities.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(ids)) {
            logisticsLargeService.updatePayStatusBySourceId(ids, payStatus, payTime);
        }
        //添加日志
        operateLogService.addModuleOperateLog(CharSequenceUtil.format("修改支付状态【{}】",TmsB2cDeclareReconciliationPayStatusEnum.getName(payStatus)), ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), id, "修改支付状态");

		return BatchResultDTO.success(entity.getId(), entity.getTrackNo(), OperationTypeEnum.UPDATE_STATUS);
	}

	@Override
	public List<EditViewDTO> editView(String id) {
		List<EditViewDTO> costDetailList = new ArrayList<>();
		LogisticsBillCostEntity entity = super.getById(id);
		List<TmsCostDetailEntity> tmsCostDetailEntityList = tmsCostDetailService.lambdaQuery().eq(TmsCostDetailEntity::getMainId, id).list();
		Map<String, List<TmsCostDetailEntity>> costIdMaps = tmsCostDetailEntityList.stream().collect(Collectors.groupingBy(TmsCostDetailEntity::getCfgCostId));

		Map<String, String> idCategoryMap = new HashMap<>();
		if(CollUtil.isNotEmpty(tmsCostDetailEntityList)) {
			idCategoryMap = tmsCfgCostService.listByIds(tmsCostDetailEntityList.stream().map(TmsCostDetailEntity::getCfgCostId).collect(Collectors.toList()))
					.stream().collect(Collectors.toMap(TmsCfgCostEntity::getId, TmsCfgCostEntity::getDictCostCategory));
		}
		for(Map.Entry<String, List<TmsCostDetailEntity>> costIdMap : costIdMaps.entrySet()) {
			EditViewDTO editViewDTO = BeanUtil.copyProperties(entity, EditViewDTO.class);
			String payType = entity.getPayType();
			editViewDTO.setPayTypeName(payType.equals("pay") ? "付款" : "退款");

			List<TmsCostDetailEntity> value = costIdMap.getValue();
			String key = costIdMap.getKey();
			editViewDTO.setCfgCostId(key);
			editViewDTO.setDictCostCategory(idCategoryMap.get(key));

			List<TmsCostDetailEntity> actualList = value.stream().filter(v -> LogisticsBillCostTypeEnum.ACTUAL.getCode().equals(v.getType())).collect(Collectors.toList());
			List<TmsCostDetailEntity> estimatedList = value.stream().filter(v -> LogisticsBillCostTypeEnum.ESTIMATED.getCode().equals(v.getType())).collect(Collectors.toList());
			editViewDTO.setCostValue(actualList.stream().map(TmsCostDetailEntity::getCostValue).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
			editViewDTO.setEstimatedValue(estimatedList.stream().map(TmsCostDetailEntity::getCostValue).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
			if(CollUtil.isNotEmpty(actualList)) {
				editViewDTO.setCurrency(actualList.get(0).getCurrency());
			}else {
				editViewDTO.setCurrency("CNY");
			}
			if(CollUtil.isNotEmpty(estimatedList)) {
				editViewDTO.setEstimatedCurrency(estimatedList.get(0).getCurrency());
			}else {
				editViewDTO.setEstimatedCurrency("CNY");
			}
			costDetailList.add(editViewDTO);
		}
		if(CollUtil.isEmpty(costDetailList)) {
			EditViewDTO editViewDTO = BeanUtil.copyProperties(entity, EditViewDTO.class);
			String payType = entity.getPayType();
			editViewDTO.setPayTypeName(payType.equals("pay") ? "付款" : "退款");
			costDetailList.add(editViewDTO);
		}
		return costDetailList;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void edit(List<EditDataDTO> dtoList) {
		if(CollUtil.isEmpty(dtoList)) {
			throw new ServiceException("不能移除所有费用");
		}

		dtoList.forEach(d -> {
			if(CharSequenceUtil.isBlank(d.getCurrency())) {
				d.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
			}
			if(CharSequenceUtil.isBlank(d.getEstimatedCurrency())) {
				d.setEstimatedCurrency(CurrencyEnum.CNY.getCurrencyCode());
			}
		});

		Map<String, List<EditDataDTO>> cfgCostIdMaps = dtoList.stream().collect(Collectors.groupingBy(EditDataDTO::getCfgCostId));
		dtoList = new ArrayList<>();
		for(Map.Entry<String, List<EditDataDTO>> cfgCostIdMap : cfgCostIdMaps.entrySet()) {
			List<EditDataDTO> groupValue = cfgCostIdMap.getValue();
			EditDataDTO v = groupValue.get(0);
			String estimatedCurrency = v.getEstimatedCurrency();
			String currency = v.getCurrency();
			if(groupValue.stream().anyMatch(g -> !estimatedCurrency.equals(g.getEstimatedCurrency()))) {
				throw new ServiceException("【" + tmsCfgCostService.getById(cfgCostIdMap.getKey()).getCostName() + "】相同费用类型预估金额存在不同币别");
			}
			if(groupValue.stream().anyMatch(g -> !currency.equals(g.getCurrency()))) {
				throw new ServiceException("【" + tmsCfgCostService.getById(cfgCostIdMap.getKey()).getCostName() + "】相同费用类型实际金额存在不同币别");
			}
			v.setEstimatedValue(groupValue.stream().filter(g -> g.getEstimatedValue() != null).map(EditDataDTO::getEstimatedValue).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
			v.setCostValue(groupValue.stream().filter(g -> g.getCostValue() != null).map(EditDataDTO::getCostValue).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
			dtoList.add(v);
		}

		LogisticsBillCostDTO.UpdateDTO updateDataDTO = new LogisticsBillCostDTO.UpdateDTO();
		EditDataDTO dto = dtoList.get(0);
		updateDataDTO.setId(dto.getId());
        updateDataDTO.setBillingWeight(dto.getBillingWeight());
        updateDataDTO.setBillingWeightLogistics(dto.getBillingWeightLogistics());
        updateDataDTO.setCurrency(CharSequenceUtil.isBlank(dto.getCurrency()) ? CurrencyEnum.CNY.getCurrencyCode() : dto.getCurrency());

        List<TmsCostDetailDTO.UpdateDTO> updateDetailList = new ArrayList<>();
        for(EditDataDTO detailDTO : dtoList) {
        	TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
            updateDTO.setCostValue(detailDTO.getCostValue());
            updateDTO.setType(LogisticsBillCostTypeEnum.ACTUAL.getCode());
            updateDTO.setCfgCostId(detailDTO.getCfgCostId());
            updateDTO.setSourceType(SourceTypeEnum.LOGISTICS_BILL_COST.getCode());
            updateDTO.setCurrency(detailDTO.getCurrency());
            updateDetailList.add(updateDTO);
            BigDecimal estimatedCostValue = detailDTO.getEstimatedValue();
            if(estimatedCostValue != null) {
            	updateDTO = new TmsCostDetailDTO.UpdateDTO();
                updateDTO.setCostValue(estimatedCostValue);
                updateDTO.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
                updateDTO.setCfgCostId(detailDTO.getCfgCostId());
                updateDTO.setSourceType(SourceTypeEnum.LOGISTICS_BILL_COST.getCode());
                updateDTO.setCurrency(detailDTO.getEstimatedCurrency());
                updateDetailList.add(updateDTO);
            }
        }
        updateDataDTO.setCostDetailList(updateDetailList);
		service.update(updateDataDTO, false, null);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void generateLogisticsBill(SoReturnInstockEntity entity) {
		LogisticsBillDTO.AddDTO addDTO = new LogisticsBillDTO.AddDTO();
		addDTO.setSourceType(SourceTypeEnum.SO_RETURN_INSTOCK.getCode());
		String returnLogisticCode = entity.getReturnLogisticCode();
		addDTO.setTransportNo(returnLogisticCode);

		String customerId = entity.getCustomerId();
		CustomerInfoEntity customerInfoEntity = FeignQuery.getById(CustomerInfoEntity.class, customerId);
		addDTO.setSalesPlatform(customerInfoEntity.getPlatformType());
		List<ShopInfoEntity> shopInfoEntityList = FeignQuery.create(ShopInfoEntity.class).eq(ShopInfoEntity::getCustomerId, customerId).list();
		if(CollUtil.isNotEmpty(shopInfoEntityList)) {
			ShopInfoEntity shopInfoEntity = shopInfoEntityList.get(0);
			addDTO.setShopId(shopInfoEntity.getId());
			addDTO.setShopName(shopInfoEntity.getName());
		}
		addDTO.setSourceId(entity.getSoId());
		addDTO.setSourceCode(entity.getSoCode());
		addDTO.setOutstockId(entity.getId());
		addDTO.setOutstockCode(entity.getCode());
		addDTO.setDeliveryTime(entity.getApproveTime());
		addDTO.setOrderType(OrderTypeEnum.SORETURN_INSTOCK.getCode());
		addDTO.setShipmentType(ShipmentTypeEnum.SELF_DELIVER.getCode());

		List<LogisticsBillDetailDTO.AddDTO> detailList = new ArrayList<>();
		LogisticsBillDetailDTO.AddDTO detailAdd = new LogisticsBillDetailDTO.AddDTO();
		detailAdd.setTrackNo(returnLogisticCode);
		detailAdd.setTrackStatus(LogisticTrackStatusEnum.SIGN.getCode());
		detailList.add(detailAdd);
		addDTO.setDetailList(detailList);

		logisticsBillService.add(addDTO);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
    public BatchResultDTO delete(String id) {
        LogisticsBillCostEntity entity = this.getById(id);
        if(!entity.getCheckStatus().equals(LogisticsBillCostCheckStatusEnum.CHECKING.getCode())) {
        	return BatchResultDTO.fail(entity.getId(), entity.getTrackNo(), "仅未下推分摊的数据删除，下推费用分摊后不可删除");
        }
        this.removeById(id);
        Integer count = lambdaQuery().eq(LogisticsBillCostEntity::getLogisticsBillId, entity.getLogisticsBillId()).count();
		if(count == null || count == 0) {
	        tmsCostDetailService.lambdaUpdate()
	        	.eq(TmsCostDetailEntity::getMainId, id)
	        	.set(TmsCostDetailEntity::getIsDeleted, true)
	        	.update();
	        logisticsBillDetailService.removeById(entity.getLogisticsBillDetailId());
	        logisticsBillService.removeById(entity.getLogisticsBillId());
        }

        return BatchResultDTO.success(entity.getId(), entity.getTrackNo(), OperationTypeEnum.DELETE);
    }



    @Override
    public LogisticsBillCostDTO.PushAllocatedCostCountDTO pushAllocationCount(LogisticsBillCostDTO.PushDTO dto) {
        LogisticsBillCostDTO.CanPushAllocationPageQueryDTO query =new LogisticsBillCostDTO.CanPushAllocationPageQueryDTO();
        query.setIds(dto.getIds());
        query.setReportDate(dto.getReportDate());
        query.setType(dto.getType());
        query.setSalesPlatformList(dto.getSalesPlatformList());
        int count = countByCanPushAllocation(query);
        LogisticsBillCostDTO.PushAllocatedCostCountDTO pushAllocatedCostCountDTO = new LogisticsBillCostDTO.PushAllocatedCostCountDTO();
        pushAllocatedCostCountDTO.setCount(count);
        return pushAllocatedCostCountDTO;
    }

    @Override
    public void batchConfirmImport(List<ImportHistoryRecordDTO.ImportConfirmDTO> confirmList, String reconciliationStatus) {
        batchConfirmImport(confirmList, reconciliationStatus, false);
    }

    @Override
    public void batchConfirmImport(List<ImportHistoryRecordDTO.ImportConfirmDTO> confirmList, String reconciliationStatus,
                                   boolean skipSync) {
        if (CollUtil.isEmpty(confirmList)) {
            return;
        }
        if (CharSequenceUtil.isBlank(reconciliationStatus)) {
            throw new ServiceException("对账状态不能为空");
        }

        LinkedHashMap<String, ImportHistoryRecordDTO.ImportConfirmDTO> confirmMap = new LinkedHashMap<>();
        for (ImportHistoryRecordDTO.ImportConfirmDTO confirmDTO : confirmList) {
            if (ObjectUtil.isNull(confirmDTO) || CharSequenceUtil.isBlank(confirmDTO.getLogisticsCostId())) {
                continue;
            }
            confirmMap.put(confirmDTO.getLogisticsCostId(), confirmDTO);
        }
        if (CollUtil.isEmpty(confirmMap)) {
            return;
        }

        String confirmUserId = UserContext.getDefaultLoginUser().getUid();
        String confirmUserName = UserContext.getDefaultLoginUser().getUserName();
        List<ImportHistoryRecordDTO.ImportConfirmDTO> distinctConfirmList = new ArrayList<>(confirmMap.values());
        // confirmImport 场景下行级已通过 validateImportConfirmAmountMsg 校验，此处不再 validateConfirmAmount，避免整批抛异常
        int updateCount = 0;
        for (List<ImportHistoryRecordDTO.ImportConfirmDTO> batch : ListUtil.partition(distinctConfirmList, IMPORT_CONFIRM_BATCH_SIZE)) {
            updateCount += baseMapper.batchConfirmImport(batch, reconciliationStatus, confirmUserId, confirmUserName);
        }
        // 导入确认变更费用单对账状态后，反向同步对账单 ref 快照与 detail_sub 聚合状态；
        // skipSync=true（对账匹配确认路径）时跳过：ref 已由 buildReconBillRefs 直接写 confirmed，
        // detail_sub 由 doMatchSubsChunk 按分片 scope 刷新，避免逐分片全单刷新导致的 O(n^2) 与并发全表更新竞争。
        if (!skipSync) {
            syncReconStatusQuietly(new ArrayList<>(confirmMap.keySet()));
        }
        log.info("导入确认批量更新完成，入参条数：{}，去重后条数：{}，更新条数：{}，skipSync={}",
                confirmList.size(), distinctConfirmList.size(), updateCount, skipSync);
    }

    @Override
    public List<String> pageByCanPushAllocation(LogisticsBillCostDTO.CanPushAllocationPageQueryDTO query) {
        return baseMapper.pageByCanPushAllocation(query);
    }

    /**
     * 统计可下推分摊的费用总条数
     */
    @Override
    public int countByCanPushAllocation(LogisticsBillCostDTO.CanPushAllocationPageQueryDTO query) {
        Integer count = baseMapper.countByCanPushAllocation(query);
        return count == null ? 0 : count;
    }

    @Override
    public void batchAsyncPushAllocation(TmsAsyncTaskRecordDTO.SmallBagPushAllocationPayloadDTO payload) {
        if (Objects.isNull(payload)) {
            throw new ServiceException("下推分摊参数不能为空");
        }
        if (CollUtil.isEmpty(payload.getSalesPlatformList())) {
            throw new ServiceException(ApiError.LOGISTICS_SALES_PLATFORM_REQUIRED);
        }
        if (StringUtils.isBlank(payload.getReportDate())) {
            throw new ServiceException("核算日期不能为空");
        }
        if (StringUtils.isBlank(payload.getType())) {
            throw new ServiceException("费用类型不能为空");
        }
        if (!Objects.equals(payload.getType(), DictCostAttributionEnum.SELF_DELIVER.getCode())
            && !Objects.equals(payload.getType(), DictCostAttributionEnum.LAST_MILE.getCode())) {
            throw new ServiceException("仅支持自发货/尾程费用下推分摊");
        }
        String businessType = SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode();
        String methodType = resolveSmallBagPushAllocationMethodType(payload.getType());

        LogisticsBillCostDTO.CanPushAllocationPageQueryDTO query = new LogisticsBillCostDTO.CanPushAllocationPageQueryDTO();
        query.setReportDate(payload.getReportDate());
        query.setType(payload.getType());
        query.setSalesPlatformList(payload.getSalesPlatformList());
        int totalCount = countByCanPushAllocation(query);
        if (totalCount == 0) {
            throw new ServiceException("没有可下推分摊的数据");
        }

        TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope = asyncTaskRecordService.buildEnvelope(
            businessType, methodType, null, null, payload);
        asyncTaskRecordService.dispatchManualEnvelopeTask(
            businessType, methodType, totalCount, envelope,
            "MQ消息发送成功，taskId: {}, 预计处理数据量: {}");
    }

    private String resolveSmallBagPushAllocationMethodType(String type) {
        if (Objects.equals(type, DictCostAttributionEnum.SELF_DELIVER.getCode())) {
            return TmsAsyncTaskMethodTypeEnum.SELFDELIVER_PUSH_ALLOCATION.getCode();
        }
        if (Objects.equals(type, DictCostAttributionEnum.LAST_MILE.getCode())) {
            return TmsAsyncTaskMethodTypeEnum.LASTMILE_PUSH_ALLOCATION.getCode();
        }
        throw new ServiceException("仅支持自发货/尾程费用下推分摊");
    }

    @Override
    public void pushSmallBagCostAllocation(TmsAsyncTaskRecordEntity taskRecord) {
        tmsAsyncTaskBatchConsumerSupport.execute(taskRecord,
            logisticsSmallBagPushBatchPushHandlerFactory.create(taskRecord));
    }

    /**
     * 处理单个批次：创建任务明细 + 并发执行
     *
     * @param taskId       主任务ID
     * @param businessType 业务类型
     * @param batchIds     本批次的费用ID列表
     * @param reportDate   报告日期
     * @return 处理结果（成功数/失败数）
     * @author jack
     * @date 2026-04-22
     */
    @Override
    public TmsAsyncTaskRecordDTO.BatchProcessResult processPushAllocationBatch(String taskId, String businessType,
                                             List<String> batchIds, String reportDate, int timeoutSeconds, int staleDetailSeconds,
                                                                  LogisticsBillCostDTO.SmallBagPushAllocationContext pushContext) {

        List<String> distinctBatchIds = batchIds.stream()
            .filter(StringUtils::isNotBlank)
            .distinct()
            .collect(Collectors.toList());
        if (CollUtil.isEmpty(distinctBatchIds)) {
            log.warn("批次中无有效费用ID，batchIds: {}", batchIds);
            return new TmsAsyncTaskRecordDTO.BatchProcessResult(0, 0);
        }

        // 1. 批量查询物流费用实体
        List<LogisticsBillCostEntity> costList = listByIds(distinctBatchIds);
        if (CollUtil.isEmpty(costList)) {
            log.warn("批次中无有效费用数据，batchIds: {}", distinctBatchIds);
            return new TmsAsyncTaskRecordDTO.BatchProcessResult(0, 0);
        }

        List<String> costIds = costList.stream()
            .map(LogisticsBillCostEntity::getId)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime staleBefore = now.minusSeconds(staleDetailSeconds);
        List<TmsAsyncTaskDetailEntity> existingDetails = asyncTaskDetailRecordService.lambdaQuery()
            .eq(TmsAsyncTaskDetailEntity::getMainId, taskId)
            .in(TmsAsyncTaskDetailEntity::getBusinessId, costIds)
            .list();
        Set<String> staleIngBusinessIds = existingDetails.stream()
            .filter(d -> Objects.equals(d.getStatus(), TmsAsyncTaskRecordStatusEnum.ING.getCode()))
            .filter(d -> asyncTaskRecordService.isStaleIngDetail(d, staleBefore))
            .map(TmsAsyncTaskDetailEntity::getBusinessId)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toSet());
        int staleFailedCount = asyncTaskDetailRecordService.markStaleIngDetailsFailed(
            taskId, staleIngBusinessIds, staleBefore, ApiError.ASYNC_TASK_DETAIL_TIMEOUT.getMsg());
        Set<String> existingIngBusinessIds = existingDetails.stream()
            .filter(d -> Objects.equals(d.getStatus(), TmsAsyncTaskRecordStatusEnum.ING.getCode()))
            .filter(d -> !staleIngBusinessIds.contains(d.getBusinessId()))
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
        if (staleFailedCount > 0) {
            log.warn("小包分摊僵死ING明细已标记失败，taskId: {}, 数量: {}", taskId, staleFailedCount);
        }
        if (CollUtil.isNotEmpty(existingIngBusinessIds)) {
            log.warn("跳过近期执行中的任务明细，taskId: {}, ING数量: {}", taskId, existingIngBusinessIds.size());
        }
        if (CollUtil.isNotEmpty(existingPendingByBusinessId)) {
            log.info("纳入已存在的PENDING任务明细执行，taskId: {}, 数量: {}", taskId, existingPendingByBusinessId.size());
        }

        List<TmsAsyncTaskDetailEntity> detailsToExecute = new ArrayList<>(existingPendingByBusinessId.values());

        // 2. 批量查询关联的物流单
        List<String> billIds = costList.stream()
            .map(LogisticsBillCostEntity::getLogisticsBillId)
            .filter(StringUtils::isNotBlank)
            .distinct()
            .collect(Collectors.toList());

        Map<String, LogisticsBillEntity> billMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(billIds)) {
            try {
                billMap = logisticsBillService.listByIds(billIds).stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(LogisticsBillEntity::getId, Function.identity(), (o1, o2) -> o1));
            } catch (Exception e) {
                log.error("查询物流单失败，billIds数量: {}", billIds.size(), e);
            }
        }

        // 3. 构建任务明细列表
        List<TmsAsyncTaskDetailEntity> details = new ArrayList<>();

        for (LogisticsBillCostEntity cost : costList) {
            if (existingIngBusinessIds.contains(cost.getId())) {
                continue;
            }
            if (existingBusinessIds.contains(cost.getId())) {
                continue;
            }
            LogisticsBillEntity bill = billMap.get(cost.getLogisticsBillId());
            TmsAsyncTaskDetailEntity detail = new TmsAsyncTaskDetailEntity();
            detail.setMainId(taskId);
            detail.setBusinessType(businessType);
            detail.setBusinessId(cost.getId());
            detail.setStatus(TmsAsyncTaskRecordStatusEnum.PENDING.getCode());
            detail.setStartTime(now);
            if (Objects.nonNull(bill)){
                detail.setBusinessCode(bill.getOutstockCode());
            }
            // 过滤无效数据
            if (Objects.isNull(bill)||Objects.isNull(bill.getIsAllocateCostRequired()) || !bill.getIsAllocateCostRequired()) {
                log.warn("费用单【{}】关联的物流单不存在，跳过", cost.getId());
                detail.setStatus(TmsAsyncTaskRecordStatusEnum.FAILED.getCode());
                detail.setEndTime(now);
                detail.setErrorData(StrUtil.format("费用单【{}】关联的物流单不存在",cost.getId()));
            }
            details.add(detail);
        }

        // 4. 批量保存任务明细
        if (CollUtil.isNotEmpty(details)) {
            try {
                asyncTaskDetailRecordService.saveBatch(details);
                log.info("本批次任务明细保存成功，数量: {}", details.size());
                detailsToExecute.addAll(details);
            } catch (Exception e) {
                log.error("保存任务明细失败，批次大小: {}", details.size(), e);
                return new TmsAsyncTaskRecordDTO.BatchProcessResult(0, details.size());
            }
        }

        if (CollUtil.isEmpty(detailsToExecute)) {
            log.warn("本批次无可执行任务明细，taskId: {}", taskId);
            return new TmsAsyncTaskRecordDTO.BatchProcessResult(0, staleFailedCount);
        }

        // 5. 并发执行本批次（含重投时已存在且仍为 PENDING 的明细）
        TmsAsyncTaskRecordDTO.BatchProcessResult result = executeBatchWithConcurrency(detailsToExecute, reportDate, timeoutSeconds, pushContext);
        return new TmsAsyncTaskRecordDTO.BatchProcessResult(
            result.getSuccessCount(), result.getFailedCount() + staleFailedCount);
    }

    /**
     * 并发执行批次任务
     *
     * @param batchDetails 本批次的任务明细列表
     * @param reportDate   报告日期
     * @return 处理结果
     * @author jack
     * @date 2026-04-22
     */
    private TmsAsyncTaskRecordDTO.BatchProcessResult executeBatchWithConcurrency(List<TmsAsyncTaskDetailEntity> batchDetails,
                                                            String reportDate, int timeoutSeconds,
                                                                                 LogisticsBillCostDTO.SmallBagPushAllocationContext pushContext) {

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
            if (!Objects.equals(duplicateDetail.getStatus(), TmsAsyncTaskRecordStatusEnum.FAILED.getCode())) {
                asyncTaskDetailRecordService.updateDetail(
                    duplicateDetail.getId(),
                    TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),
                    "同一任务下重复明细已跳过"
                );
            }
        }

        for (TmsAsyncTaskDetailEntity detail : executableDetailMap.values()) {
            if(Objects.equals(detail.getStatus(),TmsAsyncTaskRecordStatusEnum.FAILED.getCode())){
                failedCount.incrementAndGet();
                latch.countDown();
                continue;
            }
            String taskDetailId = detail.getId();
            String businessId = detail.getBusinessId();

            try {
                costAllocationPool.execute(() -> {
                    try {
                        if (!asyncTaskDetailRecordService.tryClaimDetailForExecution(taskDetailId)) {
                            log.debug("任务明细[{}]状态已变更，跳过", taskDetailId);
                            return;
                        }
                        BatchResultDTO result = service.pushAllocation(businessId, reportDate, pushContext);

                        if (result.getSuccess()) {
                            asyncTaskDetailRecordService.updateDetail(
                                taskDetailId,
                                TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),
                                ""
                            );
                            successCount.incrementAndGet();
                        } else {
                            String errorMsg = StringUtils.isNotBlank(result.getMsg())
                                ? org.apache.commons.lang3.StringUtils.substring(result.getMsg(), 0, 1000)
                                : "未知错误";
                            asyncTaskDetailRecordService.updateDetail(
                                taskDetailId,
                                TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),
                                errorMsg
                            );
                            failedCount.incrementAndGet();
                        }

                    } catch (Exception e) {
                        log.error("处理任务失败 taskDetailId: {}, businessId: {}", taskDetailId, businessId, e);
                        asyncTaskRecordService.updateTaskDetailFailure(taskDetailId, e);
                        failedCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            } catch (RejectedExecutionException ex) {
                log.error("小包分摊任务提交失败 taskDetailId: {}, businessId: {}", taskDetailId, businessId, ex);
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
                log.error("批次处理超时，批次大小: {}, 超时时间: {}秒", batchDetails.size(), timeoutSeconds);
                failedCount.addAndGet(asyncTaskDetailRecordService.markUnfinishedBatchDetailsFailed(
                    batchDetailSnapshot, "批次执行超时"));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("批次等待被中断", e);
            failedCount.addAndGet(asyncTaskDetailRecordService.markUnfinishedBatchDetailsFailed(
                batchDetailSnapshot, "任务等待中断"));
        }
        return new TmsAsyncTaskRecordDTO.BatchProcessResult(successCount.get(), failedCount.get());
    }



    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(businessType = DistributeKeyConstant.TMS_PUSH_ALLOCATION_KEY, keyName = "id", unlockAfterTx = true)
    @Override
    public BatchResultDTO pushAllocation(String id, String reportDate, LogisticsBillCostDTO.SmallBagPushAllocationContext pushContext) {
        if (pushContext == null) {
            pushContext = buildSmallBagPushAllocationContext();
        }
		LogisticsBillCostEntity entity = getById(id);
        if(Objects.isNull(entity)){
            throw new ServiceException("物流费用单不存在");
        }
		String reconciliationStatus = entity.getReconciliationStatus();
        if(StringUtils.isBlank(reconciliationStatus)){
            throw new ServiceException("对账状态不能为空");
        }
		if(!(ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode().equals(reconciliationStatus)
				|| ReconciliationStatusEnum.CONFIRMED.getCode().equals(reconciliationStatus))) {
			throw new ServiceException("只支持对账状态为暂估确认或账单确认下推分摊");
		}
        LogisticsBillCostTypeEnum costType = ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode().equals(reconciliationStatus)
                ? LogisticsBillCostTypeEnum.ESTIMATED : LogisticsBillCostTypeEnum.ACTUAL;
		List<SmallBagCostAllocationMainEntity> smallBagCostAllocationMainEntityList = smallBagCostAllocationMainService.lambdaQuery()
				.eq(SmallBagCostAllocationMainEntity::getCostId, id).list();
		if(CollUtil.isNotEmpty(smallBagCostAllocationMainEntityList)) {
            if(smallBagCostAllocationMainEntityList.stream().anyMatch(s -> SmallBagCostAllocationMainFeeSourceEnum.CONFIRMED.getCode().equals(s.getFeeSource())) && costType.equals(LogisticsBillCostTypeEnum.ACTUAL)) {
                throw new ServiceException("一个费用单只能下推一次费用分摊，不可重复下推分摊");
            }
			if(smallBagCostAllocationMainEntityList.stream().anyMatch(s -> s.getReportDate().equals(reportDate))) {
				throw new ServiceException(reportDate + "已存在下推分摊数据，不可下推分摊");
			}
			if(smallBagCostAllocationMainEntityList.stream().anyMatch(s -> !SmallBagCostAllocationReportStatusEnum.CONFIRMED.getCode().equals(s.getReportStatus()))) {
				throw new ServiceException("存在历史未确认分摊数据，不可下推分摊");
			}
		}

		LogisticsBillEntity logisticsBillEntity = logisticsBillService.getById(entity.getLogisticsBillId());
		String outstockId = logisticsBillEntity.getOutstockId();
		if(org.apache.commons.lang3.StringUtils.isBlank(outstockId)) {
			throw new ServiceException("销售出库单id不存在");
		}
        //小包分摊设置未开启
        if (!logisticsBillEntity.getIsAllocateCostRequired()) {
            throw new  ServiceException(ApiError.LOGISTICS_SMALL_BAG_NOT_CAN_Allocate);
        }

		List<SoOutstockDetailEntity> soOutstockDetailEntityList = new ArrayList<>();
		if(SourceTypeEnum.SO_RETURN_INSTOCK.getCode().equals(logisticsBillEntity.getSourceType())) {
			List<SoReturnInstockDetailEntity> soReturnInstockDetailEntityList = FeignQuery.create(SoReturnInstockDetailEntity.class)
					.eq(SoReturnInstockDetailEntity::getMainId, outstockId).list();
			if(CollUtil.isEmpty(soReturnInstockDetailEntityList)) {
				throw new ServiceException("退货入库明细不存在");
			}
			for(SoReturnInstockDetailEntity soReturnInstockDetailEntity : soReturnInstockDetailEntityList) {
				SoOutstockDetailEntity soOutstockDetailEntity = new SoOutstockDetailEntity();
				soOutstockDetailEntity.setId(soReturnInstockDetailEntity.getId());
				soOutstockDetailEntity.setSkuId(soReturnInstockDetailEntity.getSkuId());
				soOutstockDetailEntity.setSkuNo(soReturnInstockDetailEntity.getSkuNo());
				soOutstockDetailEntity.setActualQty(soReturnInstockDetailEntity.getRealQty());
				soOutstockDetailEntity.setWarehouseId(soReturnInstockDetailEntity.getWarehouseId());
				soOutstockDetailEntityList.add(soOutstockDetailEntity);
			}
		}else {
			soOutstockDetailEntityList = FeignQuery.create(SoOutstockDetailEntity.class)
					.eq(SoOutstockDetailEntity::getMainId, outstockId).list();
			if(CollUtil.isEmpty(soOutstockDetailEntityList)) {
				throw new ServiceException("销售出库单明细不存在");
			}
            SoOutstockEntity soOutstockEntity = FeignQuery.getById(SoOutstockEntity.class, outstockId);
            if (Objects.isNull(soOutstockEntity)) {
                throw new ServiceException("销售出库单不存在");
            }
			String warehouseId = soOutstockEntity.getWarehouseId();
			soOutstockDetailEntityList.forEach(s -> s.setWarehouseId(warehouseId));
		}

		List<TmsCostDetailDTO.CostViewDTO> costList = tmsCostDetailService.listCostByMainIdList(Collections.singletonList(id));
		Map<String, List<CostViewDTO>> costCategoryMaps = new HashMap<>();
		if(CollUtil.isNotEmpty(costList)) {
			costList = costList.stream().filter(c -> costType.getCode().equals(c.getType()) && c.getIsAllocate()).collect(Collectors.toList());
			costCategoryMaps = costList.stream().collect(Collectors.groupingBy(TmsCostDetailDTO.CostViewDTO::getDictCostCategory));
		}

		List<SmallBagCostAllocationEntity> addSmallBagCostAllocationEntityList = new ArrayList<>();
		List<SmallBagCostAllocationDetailEntity> addSmallBagCostAllocationDetailEntityList = new ArrayList<>();

		Map<String, String> feeTypeSettingMaps = pushContext.getFeeTypeSettingMaps();
		String weightPackageAllocation = pushContext.getWeightPackageAllocation();
        String packageOrgId = pushContext.getPackageOrgId();
        String packageWarehouseId = pushContext.getPackageWarehouseId();

        List<String> warehouseIds1 = soOutstockDetailEntityList.stream().map(SoOutstockDetailEntity::getWarehouseId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CharSequenceUtil.isNotBlank(packageWarehouseId) && !warehouseIds1.contains(packageWarehouseId)){
            warehouseIds1 = Stream.concat(warehouseIds1.stream(), Stream.of(packageWarehouseId)).collect(Collectors.toList());
        }
        Map<String, String> wareIdOrgIdMaps = FeignQuery.getByIds(WarehouseEntity.class, warehouseIds1)
				.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getOrgId));
		Map<String, InventorySkuCostDetailEntity> unInventorySkuCostMap = new HashMap<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate parse = LocalDate.parse(reportDate + "-01", formatter);
		List<InventorySkuCostEntity> inventorySkuCostEntityList = inventorySkuCostService.lambdaQuery().eq(InventorySkuCostEntity::getAllocatedMonth, parse)
				.eq(InventorySkuCostEntity::getStatus, "approve")
				.in(CharSequenceUtil.isBlank(packageOrgId),InventorySkuCostEntity::getCompanyId, wareIdOrgIdMaps.values())
				.eq(CharSequenceUtil.isNotBlank(packageOrgId),InventorySkuCostEntity::getCompanyId, packageOrgId)
				.list();
		Map<String, InventorySkuCostEntity> idEntityMaps = new HashMap<>();
		if(CollUtil.isNotEmpty(inventorySkuCostEntityList)) {
            List<String> warehouseIds = soOutstockDetailEntityList.stream().map(SoOutstockDetailEntity::getWarehouseId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            List<String> skuIds = soOutstockDetailEntityList.stream().map(SoOutstockDetailEntity::getSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            idEntityMaps = inventorySkuCostEntityList.stream().collect(Collectors.toMap(InventorySkuCostEntity::getId, i -> i));
            List<InventorySkuCostDetailEntity> inventorySkuCostDetailEntityList = inventorySkuCostDetailService.lambdaQuery()
                    .in(InventorySkuCostDetailEntity::getMainId, idEntityMaps.keySet())
                    .in(CharSequenceUtil.isBlank(packageWarehouseId) && CollUtil.isNotEmpty(warehouseIds),InventorySkuCostDetailEntity::getWarehouseId, warehouseIds)
                    .eq(CharSequenceUtil.isNotBlank(packageWarehouseId), InventorySkuCostDetailEntity::getWarehouseId,packageWarehouseId)
                    .in(InventorySkuCostDetailEntity::getSkuId , skuIds).list();
			if(CollUtil.isNotEmpty(inventorySkuCostDetailEntityList)) {
				for(InventorySkuCostDetailEntity i : inventorySkuCostDetailEntityList) {
					InventorySkuCostEntity inventorySkuCostEntity = idEntityMaps.get(i.getMainId());
                    String companyId = CharSequenceUtil.isBlank(packageOrgId) ? inventorySkuCostEntity.getCompanyId() : packageOrgId;
                    String warehouseId = CharSequenceUtil.isBlank(packageWarehouseId) ? i.getWarehouseId() : packageWarehouseId;
					String skuId = i.getSkuId();
					unInventorySkuCostMap.put(companyId + "_" + warehouseId + "_" + skuId, i);
				}
			}
		}

		BigDecimal totalSkuCost = BigDecimal.ZERO;
		List<ProductPackEntity> productPackEntityList = FeignQuery.create(ProductPackEntity.class)
				.in(ProductPackEntity::getSkuId, soOutstockDetailEntityList.stream().map(SoOutstockDetailEntity::getSkuId).collect(Collectors.toList()))
				.list();
		Map<String, BigDecimal> skuWeightCostMaps = productPackEntityList.stream().collect(Collectors.toMap(ProductPackEntity::getSkuId, ProductPackEntity::getGrossWeight));
		BigDecimal totalSkuWeightCost = BigDecimal.ZERO;
		for(SoOutstockDetailEntity soOutstockDetailEntity : soOutstockDetailEntityList) {
			String skuId = soOutstockDetailEntity.getSkuId();
			Integer actualQty = soOutstockDetailEntity.getActualQty();
            String orgId = CharSequenceUtil.isBlank(packageOrgId) ? wareIdOrgIdMaps.get(soOutstockDetailEntity.getWarehouseId()) : packageOrgId;
            String warehouseId = CharSequenceUtil.isBlank(packageWarehouseId) ? soOutstockDetailEntity.getWarehouseId() : packageWarehouseId;
            InventorySkuCostDetailEntity inventorySkuCostDetailEntity = unInventorySkuCostMap.get(orgId + "_" + warehouseId + "_" + skuId);
			if(inventorySkuCostDetailEntity != null) {
				totalSkuCost = totalSkuCost.add(inventorySkuCostDetailEntity.getProductCost().multiply(new BigDecimal(actualQty)));
			}
			BigDecimal skuWeightCost = skuWeightCostMaps.get(skuId);
			if(skuWeightCost != null) {
				totalSkuWeightCost = totalSkuWeightCost.add(skuWeightCost.multiply(new BigDecimal(actualQty)));
			}
		}

		SmallBagCostAllocationMainEntity smallBagCostAllocationMainEntity = new SmallBagCostAllocationMainEntity();
		String smallBagCostAllocationMainId = identifierGenerator.nextId(smallBagCostAllocationMainEntity).toString();
		smallBagCostAllocationMainEntity.setId(smallBagCostAllocationMainId);
		smallBagCostAllocationMainEntity.setCostId(id);
		smallBagCostAllocationMainEntity.setReportDate(reportDate);
		smallBagCostAllocationMainEntity.setReportStatus(SmallBagCostAllocationReportStatusEnum.TOBECONFIRM.getCode());
		smallBagCostAllocationMainEntity.setBigTableStatus(SmallBagCostAllocationBigTableStatusEnum.TODO.getCode());
		smallBagCostAllocationMainEntity.setFeeSource(reconciliationStatus);

		soOutstockDetailEntityList.sort((s1 , s2) -> s1.getActualQty().compareTo(s2.getActualQty()));
		int i = 0;

        Map<String, String> orgIdNameMaps = pushContext.getOrgIdNameMaps();

		String feeRule = entity.getFeeRule();
		if(StringUtils.isNotBlank(feeRule) && !ShippingFeeRuleEnum.BILLING_WEIGHT.getCode().equals(feeRule)) {
			if(WeightAllocationEnum.OUTSTOCK_CHARGED_WEIGHT.getCode().equals(weightPackageAllocation)) {
				weightPackageAllocation = feeRule;
			}
		}

        //物流组织
        String logisticsSupplierId = "";
        LogisticsChannelEntity logisticsChannelEntity = logisticsChannelService.getById(entity.getChannelId());
        if(Objects.nonNull(logisticsChannelEntity) && StringUtils.isNotBlank(logisticsChannelEntity.getMainId())){
            LogisticsSupplierEntity LogisticsSupplierEntity = logisticsSupplierService.getById(logisticsChannelEntity.getMainId());
            if(Objects.nonNull(LogisticsSupplierEntity)){
                logisticsSupplierId = LogisticsSupplierEntity.getOrgId();
            }
        }

		Map<String, BigDecimal> rateMap = new HashMap<>();
		boolean skuCostFlag = false;
		for(SoOutstockDetailEntity soOutstockDetailEntity : soOutstockDetailEntityList) {
			i = i + 1;
			String skuId = soOutstockDetailEntity.getSkuId();
			String skuNo = soOutstockDetailEntity.getSkuNo();
			SmallBagCostAllocationEntity smallBagCostAllocationEntity = new SmallBagCostAllocationEntity();
			String mainId = identifierGenerator.nextId(smallBagCostAllocationEntity).toString();
			smallBagCostAllocationEntity.setId(mainId);
			smallBagCostAllocationEntity.setMainId(smallBagCostAllocationMainId);
			smallBagCostAllocationEntity.setSkuId(skuId);
			smallBagCostAllocationEntity.setSkuNo(skuNo);
			smallBagCostAllocationEntity.setOutstockDetailId(soOutstockDetailEntity.getId());

            //组织id
            String orgId = "";
            if(CharSequenceUtil.isBlank(packageOrgId) || Objects.equals(CostAllocationOrgTypeEnum.BILL_ORG.getCode(),packageOrgId)){
                //单据成本组织
                orgId =  wareIdOrgIdMaps.get(soOutstockDetailEntity.getWarehouseId());
            }else if(Objects.equals(CostAllocationOrgTypeEnum.LOGISTICS_SUPPLIER_ORG.getCode(),packageOrgId)){
                orgId = logisticsSupplierId;
            }else {
                orgId = packageOrgId;
            }
            String orgName = orgIdNameMaps.get(orgId);

			Integer actualQty = soOutstockDetailEntity.getActualQty();
            if (actualQty == null || actualQty <= 0) {
                throw new ServiceException("出库明细实际数量不能为0或空");
            }
			BigDecimal skuCostPre = BigDecimal.ZERO;
            String warehouseId = CharSequenceUtil.isBlank(packageWarehouseId) ? soOutstockDetailEntity.getWarehouseId() : packageWarehouseId;
            InventorySkuCostDetailEntity inventorySkuCostDetailEntity = unInventorySkuCostMap.get(orgId + "_" + warehouseId + "_" + skuId);
			if(inventorySkuCostDetailEntity != null) {
				BigDecimal skuCost = inventorySkuCostDetailEntity.getProductCost();
				if(totalSkuCost.compareTo(BigDecimal.ZERO) != 0 && skuCost != null) {
					skuCostPre = skuCost.multiply(new BigDecimal(actualQty)).divide(totalSkuCost, 8, RoundingMode.HALF_UP);
				}
				smallBagCostAllocationEntity.setUnitCost(skuCost);
				smallBagCostAllocationEntity.setUnitCurrency(idEntityMaps.get(inventorySkuCostDetailEntity.getMainId()).getCurrency());
			}else {
				skuCostFlag = true;
				smallBagCostAllocationEntity.setUnitCost(BigDecimal.ZERO);
				smallBagCostAllocationEntity.setUnitCurrency("CNY");
			}
			BigDecimal skuWeightCostPre = BigDecimal.ZERO;
			BigDecimal skuWeightCost = skuWeightCostMaps.get(skuId);
			if(totalSkuWeightCost.compareTo(BigDecimal.ZERO) != 0 && skuWeightCost != null) {
				skuWeightCostPre = skuWeightCost.multiply(new BigDecimal(actualQty)).divide(totalSkuWeightCost, 8, RoundingMode.HALF_UP);
			}

			smallBagCostAllocationEntity.setDeliveryQty(actualQty);
			BigDecimal billingWeight = entity.getBillingWeight();
			BigDecimal skuWeight = null;
			if(entity.getType().equals(DictCostAttributionEnum.LAST_MILE.getCode())) {
				billingWeight = BigDecimal.ZERO;
			}else {
				if(ShippingFeeRuleEnum.BILLING_WEIGHT.getCode().equals(feeRule)) {
					billingWeight = entity.getBillingWeight();
				}else if(ShippingFeeRuleEnum.NET_WEIGHT.getCode().equals(feeRule)) {
					billingWeight = entity.getActualWeight();
				}else if(ShippingFeeRuleEnum.VOLUME_WEIGHT.getCode().equals(feeRule)) {
					billingWeight = entity.getVolumeWeight();
				}
			}
			if(WeightAllocationSmallBagEnum.OUTSTOCK_CHARGED_WEIGHT.getCode().equals(weightPackageAllocation)
					|| WeightAllocationSmallBagEnum.NETWEIGHT.getCode().equals(weightPackageAllocation)
					|| WeightAllocationSmallBagEnum.VOLUMEWEIGHT.getCode().equals(weightPackageAllocation)) {
				skuWeight = billingWeight.multiply(skuWeightCostPre).divide(new BigDecimal(actualQty), 4 , RoundingMode.HALF_UP);
				if(BigDecimal.ZERO.compareTo(billingWeight) != 0) {
					skuWeightCostPre = skuWeight.multiply(new BigDecimal(actualQty)).divide(billingWeight, 8, RoundingMode.HALF_UP);
				}
			}else if(WeightAllocationSmallBagEnum.SUPPLIER_CHARGED_WEIGHT.getCode().equals(weightPackageAllocation)) {
				skuWeight = entity.getBillingWeightLogistics().multiply(skuWeightCostPre).divide(new BigDecimal(actualQty), 4 , RoundingMode.HALF_UP);
			}else if(WeightAllocationSmallBagEnum.SINGLE_PRODUCT_WEIGHT.getCode().equals(weightPackageAllocation)) {
				skuWeight = skuWeightCostMaps.get(skuId).divide(new BigDecimal("1000"), 4 , RoundingMode.HALF_UP);
			}
			if(skuWeight == null) {
				skuWeight = BigDecimal.ZERO;
			}
			smallBagCostAllocationEntity.setBillingWeight(billingWeight);
			smallBagCostAllocationEntity.setSkuWeight(skuWeight);
			addSmallBagCostAllocationEntityList.add(smallBagCostAllocationEntity);

			for(Map.Entry<String, String> feeTypeSettingMap : feeTypeSettingMaps.entrySet()) {
				SmallBagCostAllocationDetailEntity smallBagCostAllocationDetailEntity = new SmallBagCostAllocationDetailEntity();
				smallBagCostAllocationDetailEntity.setMainId(mainId);
				String feeType = feeTypeSettingMap.getKey();
				List<CostViewDTO> costViewDTOList = costCategoryMaps.get(feeType);
				if(CollUtil.isEmpty(costViewDTOList)) {
					costViewDTOList = new ArrayList<>();
				}
				BigDecimal costValueSum = costViewDTOList.stream().map(CostViewDTO::getCostValue).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
				String allocatedCurrency = "CNY";
				if(CollUtil.isNotEmpty(costViewDTOList)) {
					allocatedCurrency = costViewDTOList.get(0).getCurrency();
				}
				String key = reportDate + "_" + allocatedCurrency;
				BigDecimal rate = rateMap.get(key);
				if(rate == null) {
                    LocalDate localDate = LocalDate.parse(reportDate + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
					rate = dmpTaskFeign.getRate(localDate.withDayOfMonth(localDate.lengthOfMonth()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), allocatedCurrency);
					if(ObjectUtil.isEmpty(rate)){
			            log.error("币别【{}】,汇率为空，请维护汇率后再查询",allocatedCurrency);
			            throw new ServiceException("汇率为空，请维护汇率后再查询");
			        }
					rateMap.put(key, rate);
				}
				smallBagCostAllocationDetailEntity.setBillAmount(costValueSum);
				BigDecimal billAmountExchange = MathUtil.multiplyWithSix(smallBagCostAllocationDetailEntity.getBillAmount(), rate, BigDecimal.ROUND_DOWN);
				smallBagCostAllocationDetailEntity.setBillAmountExchange(billAmountExchange);
				smallBagCostAllocationDetailEntity.setFeeType(feeType);
				String feeAllocationType = feeTypeSettingMap.getValue();
				if(org.apache.commons.lang3.StringUtils.isBlank(feeAllocationType)) {
					feeAllocationType = CostAllocationEnum.WEIGHT_ALLOCATION.getCode();
				}
				smallBagCostAllocationDetailEntity.setFeeAllocationType(feeAllocationType);
				if(i < soOutstockDetailEntityList.size()) {
					if(CostAllocationEnum.WEIGHT_ALLOCATION.getCode().equals(feeAllocationType)) {
						smallBagCostAllocationDetailEntity.setAllocatedAmount(costValueSum.multiply(skuWeightCostPre).setScale(2, RoundingMode.DOWN));
						smallBagCostAllocationDetailEntity.setAllocatedAmountExchange(billAmountExchange.multiply(skuWeightCostPre).setScale(2, RoundingMode.DOWN));
					}else {
						smallBagCostAllocationDetailEntity.setAllocatedAmount(costValueSum.multiply(skuCostPre).setScale(2, RoundingMode.DOWN));
						smallBagCostAllocationDetailEntity.setAllocatedAmountExchange(billAmountExchange.multiply(skuCostPre).setScale(2, RoundingMode.DOWN));
					}
				}else {
					smallBagCostAllocationDetailEntity.setAllocatedAmount(costValueSum.subtract(addSmallBagCostAllocationDetailEntityList.stream()
							.filter(a -> a.getFeeType().equals(feeType)).map(SmallBagCostAllocationDetailEntity::getAllocatedAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO)).setScale(2, RoundingMode.DOWN));
					smallBagCostAllocationDetailEntity.setAllocatedAmountExchange(billAmountExchange.subtract(addSmallBagCostAllocationDetailEntityList.stream()
							.filter(a -> a.getFeeType().equals(feeType)).map(SmallBagCostAllocationDetailEntity::getAllocatedAmountExchange).reduce(BigDecimal::add).orElse(BigDecimal.ZERO)).setScale(2, RoundingMode.DOWN));
				}
				smallBagCostAllocationDetailEntity.setAllocatedCurrency(allocatedCurrency);
				smallBagCostAllocationDetailEntity.setProductAllocatedAmount(smallBagCostAllocationDetailEntity.getAllocatedAmount()
						.divide(new BigDecimal(actualQty), 6, RoundingMode.HALF_UP));
				smallBagCostAllocationDetailEntity.setProductAllocatedAmountExchange(smallBagCostAllocationDetailEntity.getAllocatedAmountExchange()
						.divide(new BigDecimal(actualQty), 6, RoundingMode.HALF_UP));
				smallBagCostAllocationDetailEntity.setWeightAllocationType(weightPackageAllocation);
				smallBagCostAllocationDetailEntity.setOrgId(orgId);
				smallBagCostAllocationDetailEntity.setOrgName(orgName);
				addSmallBagCostAllocationDetailEntityList.add(smallBagCostAllocationDetailEntity);
			}
		}

		smallBagCostAllocationMainService.save(smallBagCostAllocationMainEntity);
		if(CollUtil.isNotEmpty(addSmallBagCostAllocationEntityList)) {
			smallBagCostAllocationService.saveBatch(addSmallBagCostAllocationEntityList);
		}
		if(CollUtil.isNotEmpty(addSmallBagCostAllocationDetailEntityList)) {
			if(skuCostFlag) {
				for(Map.Entry<String, String> feeTypeSettingMap : feeTypeSettingMaps.entrySet()) {
					if(CostAllocationEnum.COST_ALLOCATION.getCode().equals(feeTypeSettingMap.getValue())) {
						addSmallBagCostAllocationDetailEntityList.forEach(a -> {
							if(a.getFeeType().equals(feeTypeSettingMap.getKey())) {
								a.setAllocatedAmount(BigDecimal.ZERO);
								a.setAllocatedAmountExchange(BigDecimal.ZERO);
								a.setProductAllocatedAmount(BigDecimal.ZERO);
								a.setProductAllocatedAmountExchange(BigDecimal.ZERO);
							}
						});
					}
				}
			}
			smallBagCostAllocationDetailService.saveBatch(addSmallBagCostAllocationDetailEntityList);
		}

		lambdaUpdate().eq(LogisticsBillCostEntity::getId, entity.getId()).set(LogisticsBillCostEntity::getCheckStatus, LogisticsBillCostCheckStatusEnum.CHECKED.getCode()).update();

		return BatchResultDTO.success(entity.getId(), entity.getTrackNo(), "下推成功");
	}

    @Override
    public void deleteLogisticsBillCostNoBill() {
        List<LogisticsBillCostDTO.BillCostNoBillDTO> dtos = baseMapper.selectLogisticsBillCostNoBill();
        if (CollUtil.isEmpty(dtos)){
            return;
        }
        List<List<LogisticsBillCostDTO.BillCostNoBillDTO>> partition = ListUtil.partition(dtos, 100);
        partition.forEach(list -> {
            List<String> ids = list.stream().map(LogisticsBillCostDTO.BillCostNoBillDTO::getId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            tmsCostDetailService.deleteByMainIdList(ids);
            this.removeByIds(ids);
        });
    }

    @Override
    public Boolean importExcel(BaseDTO.ImportDTO dto) {
        downloadTaskFeign.saveImportTask("尾程费用(自发货)导入", IMPORT_TMS_LOGISTICS_BILL_COST.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importLogisticsBillCost(BaseDTO.ImportDTO dto) {
        LogisticsBillCostExcelListener excelListenerUtil = new LogisticsBillCostExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount(),dto.getExtMap());
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), LogisticsBillCostExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        //导出错误数据
        List<LogisticsBillCostExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "尾程费用(自发货)错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, LogisticsBillCostExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    @Override
    public List<LogisticsBillCostDTO.CostDetailDTO> listCostDetailByBillAndReconciliationIds(List<String> billIds, List<String> mainIds, String type) {
        if (CollUtil.isEmpty(billIds) && CollUtil.isEmpty(mainIds)){
            return Collections.emptyList();
        }
        return baseMapper.listCostDetailByBillAndReconciliationIds(billIds, mainIds,type);
    }

    @Override
    public List<String> listLogisticsBillCostId(LogisticsBillCostDTO.ListParamDTO dto) {
        return this.baseMapper.listLogisticsBillCostId(dto);
    }

    /**
     * 创建对账状态变更异步任务。
     * 全量高级查询不再把几万ID返回前端，任务参数只保存查询条件和目标状态。
     */
    @Override
    public BatchResultDTO batchAsyncUpdateReconciliationStatus(LogisticsBillCostDTO.UpdateStatusDTO dto) {
        validateUpdateReconciliationStatusParams(dto.getReconciliationStatus(), dto.getConfirmTime());
        validateUpdateReconciliationStatusScope(dto);
        String costTypeName = resolveCostAttributionName(dto.getType());
        String businessType = SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode();
        String methodType = resolveUpdateReconciliationStatusMethodType(dto.getType());

        int total = countByUpdateReconciliationStatus(buildUpdateReconciliationStatusCountQuery(dto));
        if (total == 0) {
            throw new ServiceException("没有可更新对账状态的数据");
        }

        TmsAsyncTaskRecordDTO.UpdateReconciliationStatusPayloadDTO payload =
            new TmsAsyncTaskRecordDTO.UpdateReconciliationStatusPayloadDTO(
                dto.getReconciliationStatus(), dto.getConfirmTime(), dto.getSqlMap(), dto.getPermissionSql());
        TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope =
            asyncTaskRecordService.buildEnvelope(businessType, methodType, null, null, payload);
        BatchResultDTO result = asyncTaskRecordService.dispatchManualEnvelopeTask(
            businessType, methodType, total, envelope,
            costTypeName + "对账状态变更异步任务派发成功，taskId: {}, 预计处理数据量: {}");
        log.info("{}对账状态变更异步任务派发成功，taskId: {}, 预计处理数据量: {}",
            costTypeName, result.getId(), total);
        return result;
    }

    /**
     * 创建任务前校验目标状态，避免明显无效的异步任务进入队列。
     */
    private void validateUpdateReconciliationStatusParams(String reconciliationStatus, LocalDateTime confirmTime) {
        if (org.apache.commons.lang3.StringUtils.isBlank(reconciliationStatus)) {
            throw new ServiceException("对账状态不能为空");
        }
        boolean validStatus = Arrays.asList(
                ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(),
                ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode(),
                ReconciliationStatusEnum.CONFIRMED.getCode(),
                ReconciliationStatusEnum.INVALID.getCode()
        ).contains(reconciliationStatus);
        if (!validStatus) {
            throw new ServiceException("不支持的对账状态");
        }
        boolean confirmFlag = ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode().equals(reconciliationStatus)
                || ReconciliationStatusEnum.CONFIRMED.getCode().equals(reconciliationStatus);
        if (confirmFlag && confirmTime == null) {
            throw new ServiceException("对账状态修改为" + reconciliationStatus + "时，对账确认时间不能为空");
        }
    }

    /**
     * 批量任务必须由服务端入口写入费用归属类型，且全量异步更新必须带查询条件。
     */
    private void validateUpdateReconciliationStatusScope(LogisticsBillCostDTO.UpdateStatusDTO dto) {
        if (!Objects.equals(dto.getType(), DictCostAttributionEnum.SELF_DELIVER.getCode())
                && !Objects.equals(dto.getType(), DictCostAttributionEnum.LAST_MILE.getCode())) {
            throw new ServiceException("费用归属类型不支持");
        }
        String querySql = dto.getSqlMap() == null ? null : dto.getSqlMap().get("default");
        if (CollUtil.isEmpty(dto.getIds())) {
            validateAsyncUpdateReconciliationMonth(dto.getAdvanceQueryDTOList());
        }
        if (CollUtil.isEmpty(dto.getIds()) && StringUtils.isBlank(querySql)) {
            throw new ServiceException("请先筛选要更新的费用数据");
        }
    }

    /**
     * 校验全量异步更新对账状态时必须选择对账月份。
     * <p>
     * 全量异步任务按高级查询条件创建任务，不回传具体 ID。为了避免任务范围过大，
     * 必须包含 lbc.reconciliation_month 条件，且比较符只能为等于、值不能为空、格式必须为 yyyy-MM。
     *
     * @param advanceQueryDTOList 页面高级查询条件
     */
    private void validateAsyncUpdateReconciliationMonth(List<AdvanceQueryDTO> advanceQueryDTOList) {
        List<AdvanceQueryDTO> reconciliationMonthQueries = Optional.ofNullable(advanceQueryDTOList)
                .orElse(Collections.emptyList())
                .stream()
                .filter(query -> Objects.equals(RECONCILIATION_MONTH_QUERY_FIELD, query.getField()))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(reconciliationMonthQueries)) {
            throw new ServiceException("异步更新必须选择【对账月份】，格式为yyyy-MM，且不能为空，比较符必须为等于");
        }
        boolean invalid = reconciliationMonthQueries.stream().anyMatch(query ->
                !Objects.equals(QueryConditionEnum.EQ.getCompareCode(), query.getCompare())
                        || isBlankAdvanceQueryValue(query.getValue())
                        || !isValidReconciliationMonthValue(query.getValue()));
        if (invalid) {
            throw new ServiceException("异步更新必须选择【对账月份】，格式为yyyy-MM，且不能为空，比较符必须为等于");
        }
    }

    /**
     * 判断对账月份高级查询值格式是否合法。
     *
     * @param value 对账月份高级查询条件值
     * @return true 表示值符合 yyyy-MM 格式
     */
    private boolean isValidReconciliationMonthValue(Object value) {
        return value != null && value.toString().trim().matches("\\d{4}-(0[1-9]|1[0-2])");
    }

    /**
     * 判断高级查询值是否为空。
     *
     * @param value 高级查询条件值
     * @return true 表示值为空
     */
    private boolean isBlankAdvanceQueryValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof CharSequence) {
            return StrUtil.isBlank((CharSequence) value);
        }
        if (value instanceof Collection) {
            return CollUtil.isEmpty((Collection<?>) value);
        }
        return false;
    }

    private String resolveCostAttributionName(String type) {
        if (Objects.equals(type, DictCostAttributionEnum.SELF_DELIVER.getCode())) {
            return "尾程费用(自发货)";
        }
        if (Objects.equals(type, DictCostAttributionEnum.LAST_MILE.getCode())) {
            return "尾程费用(平台发货)";
        }
        return "尾程费用";
    }

    private void fillUpdateReconciliationStatusCodes(LogisticsBillCostDTO.UpdateReconciliationStatusPageQueryDTO query) {
        query.setEstimateConfirmStatus(ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode());
        query.setConfirmedStatus(ReconciliationStatusEnum.CONFIRMED.getCode());
        query.setInvalidStatus(ReconciliationStatusEnum.INVALID.getCode());
        query.setToBeConfirmStatus(ReconciliationStatusEnum.TO_BE_CONFIRM.getCode());
        query.setCheckedCheckStatus(LogisticsBillCostCheckStatusEnum.CHECKED.getCode());
        query.setCheckingCheckStatus(LogisticsBillCostCheckStatusEnum.CHECKING.getCode());
        query.setRefundPayType(LogisticsBillCostPayTypeEnum.REFUND.getCode());
        query.setPaymentPayStatus(TmsB2cDeclareReconciliationPayStatusEnum.PAYMENT.getCode());
    }

    /**
     * 同步ID分支改用LIST权限注入查询SQL，这里补充按ID权限校验。
     */
    @Override
    public void checkUpdateReconciliationStatusPermission(LogisticsBillCostDTO.UpdateStatusDTO dto) {
        validateUpdateReconciliationStatusScope(dto);
        if (CollUtil.isEmpty(dto.getIds())) {
            return;
        }
        long inputCount = dto.getIds().stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .count();
        Integer permittedCount = baseMapper.countPermittedUpdateReconciliationStatusIds(dto);
        if (permittedCount == null || permittedCount < inputCount) {
            throw new ServiceException(ApiError.HTTP_FORBIDDEN);
        }
    }

    @Override
    public List<String> pageByUpdateReconciliationStatus(LogisticsBillCostDTO.UpdateReconciliationStatusPageQueryDTO query) {
        fillUpdateReconciliationStatusCodes(query);
        return baseMapper.pageByUpdateReconciliationStatus(query);
    }

    @Override
    public int countByUpdateReconciliationStatus(LogisticsBillCostDTO.UpdateReconciliationStatusPageQueryDTO query) {
        fillUpdateReconciliationStatusCodes(query);
        Integer count = baseMapper.countByUpdateReconciliationStatus(query);
        return count == null ? 0 : count;
    }

    @Override
    public LogisticsBillCostDTO.UpdateReconciliationStatusPageQueryDTO buildUpdateReconciliationStatusPageQuery(
            TmsAsyncTaskRecordDTO.UpdateReconciliationStatusPayloadDTO payload, String costType) {
        LogisticsBillCostDTO.UpdateReconciliationStatusPageQueryDTO query =
            new LogisticsBillCostDTO.UpdateReconciliationStatusPageQueryDTO();
        query.setType(costType);
        query.setReconciliationStatus(payload.getReconciliationStatus());
        query.setConfirmTime(payload.getConfirmTime());
        query.setSqlMap(payload.getSqlMap());
        query.setPermissionSql(payload.getPermissionSql());
        fillUpdateReconciliationStatusCodes(query);
        return query;
    }

    /**
     * 消费对账状态变更任务。
     */
    @Override
    public void pushUpdateReconciliationStatus(TmsAsyncTaskRecordEntity taskRecord) {
        tmsAsyncTaskBatchConsumerSupport.execute(taskRecord, logisticsUpdateReconciliationBatchPushHandler);
    }

    private String resolveUpdateReconciliationStatusMethodType(String type) {
        if (Objects.equals(type, DictCostAttributionEnum.SELF_DELIVER.getCode())) {
            return TmsAsyncTaskMethodTypeEnum.SELFDELIVER_UPDATE_RECONCILIATION_STATUS.getCode();
        }
        if (Objects.equals(type, DictCostAttributionEnum.LAST_MILE.getCode())) {
            return TmsAsyncTaskMethodTypeEnum.LASTMILE_UPDATE_RECONCILIATION_STATUS.getCode();
        }
        throw new ServiceException("费用归属类型不支持");
    }

    @Override
    public String resolveCostTypeFromUpdateReconciliationMethodType(String methodType) {
        if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.SELFDELIVER_UPDATE_RECONCILIATION_STATUS.getCode())) {
            return DictCostAttributionEnum.SELF_DELIVER.getCode();
        }
        if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.LASTMILE_UPDATE_RECONCILIATION_STATUS.getCode())) {
            return DictCostAttributionEnum.LAST_MILE.getCode();
        }
        throw new ServiceException("对账状态变更异步任务方法类型不支持: " + methodType);
    }

    private LogisticsBillCostDTO.UpdateReconciliationStatusPageQueryDTO buildUpdateReconciliationStatusCountQuery(
            LogisticsBillCostDTO.UpdateStatusDTO dto) {
        return buildUpdateReconciliationStatusPageQuery(
            new TmsAsyncTaskRecordDTO.UpdateReconciliationStatusPayloadDTO(
                dto.getReconciliationStatus(), dto.getConfirmTime(), dto.getSqlMap(), dto.getPermissionSql()),
            dto.getType());
    }

    /**
     * 执行单批对账状态变更，并把每条成功或失败写入任务明细。
     */
    @Override
    public TmsAsyncTaskRecordDTO.BatchProcessResult processUpdateReconciliationStatusBatch(String taskId,
                                                                                           List<String> batchIds,
                                                                                           String reconciliationStatus,
                                                                                           LocalDateTime confirmTime,
                                                                                           String costType,
                                                                                           LoginUser operatorUser,
                                                                                           int timeoutSeconds,
                                                                                           int staleDetailSeconds) {
        List<String> distinctBatchIds = batchIds.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(distinctBatchIds)) {
            return new TmsAsyncTaskRecordDTO.BatchProcessResult(0, 0);
        }

        List<LogisticsBillCostEntity> costList = listByIds(distinctBatchIds);
        Map<String, LogisticsBillCostEntity> costMap = costList.stream()
                .collect(Collectors.toMap(LogisticsBillCostEntity::getId, Function.identity(), (o1, o2) -> o1));
        List<TmsAsyncTaskDetailEntity> details =
            buildUpdateReconciliationStatusDetails(taskId, distinctBatchIds, costMap, staleDetailSeconds);
        if (CollUtil.isEmpty(details)) {
            return new TmsAsyncTaskRecordDTO.BatchProcessResult(0, 0);
        }

        return executeUpdateReconciliationStatusBatchWithConcurrency(
            details, reconciliationStatus, confirmTime, costType, operatorUser, taskId, timeoutSeconds);
    }

    /**
     * 并发执行单批对账状态变更，与下推分摊批次处理模式保持一致。
     */
    private TmsAsyncTaskRecordDTO.BatchProcessResult executeUpdateReconciliationStatusBatchWithConcurrency(
            List<TmsAsyncTaskDetailEntity> batchDetails,
            String reconciliationStatus,
            LocalDateTime confirmTime,
            String costType,
            LoginUser operatorUser,
            String taskId,
            int timeoutSeconds) {
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
        String costTypeName = resolveCostAttributionName(costType);

        for (TmsAsyncTaskDetailEntity duplicateDetail : duplicateDetails) {
            asyncTaskDetailRecordService.updateDetail(
                duplicateDetail.getId(),
                TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),
                "同一任务下重复明细已跳过"
            );
        }

        for (TmsAsyncTaskDetailEntity detail : executableDetailMap.values()) {
            if (Objects.equals(detail.getStatus(), TmsAsyncTaskRecordStatusEnum.FAILED.getCode())) {
                failedCount.incrementAndGet();
                latch.countDown();
                continue;
            }
            String taskDetailId = detail.getId();
            String businessId = detail.getBusinessId();
            try {
                costAllocationPool.execute(() -> {
                    try {
                        if (!asyncTaskDetailRecordService.tryClaimDetailForExecution(taskDetailId)) {
                            log.debug("任务明细[{}]状态已变更，跳过", taskDetailId);
                            return;
                        }
                        UserContext.setLoginUser(operatorUser);
                        BatchResultDTO result = service.updateReconciliationStatus(
                            businessId, reconciliationStatus, confirmTime);
                        if (Boolean.TRUE.equals(result.getSuccess())) {
                            asyncTaskDetailRecordService.updateDetail(taskDetailId,
                                TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), "");
                            successCount.incrementAndGet();
                        } else {
                            asyncTaskDetailRecordService.updateDetail(taskDetailId,
                                TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),
                                org.apache.commons.lang3.StringUtils.substring(result.getMsg(), 0, 1000));
                            failedCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        log.error("{}对账状态变更明细执行失败 taskDetailId: {}, businessId: {}",
                            costTypeName, taskDetailId, businessId, e);
                        asyncTaskRecordService.updateTaskDetailFailure(taskDetailId, e);
                        failedCount.incrementAndGet();
                    } finally {
                        UserContext.clear();
                        latch.countDown();
                    }
                });
            } catch (RejectedExecutionException ex) {
                log.error("{}对账状态变更任务提交失败 taskDetailId: {}, businessId: {}", costTypeName, taskDetailId, businessId, ex);
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
                log.error("{}对账状态变更批次处理超时，批次大小: {}, 超时时间: {}秒",
                    costTypeName, batchDetails.size(), timeoutSeconds);
                failedCount.addAndGet(asyncTaskDetailRecordService.markUnfinishedBatchDetailsFailed(
                    batchDetailSnapshot, "批次执行超时"));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("{}对账状态变更批次等待被中断", costTypeName, e);
            failedCount.addAndGet(asyncTaskDetailRecordService.markUnfinishedBatchDetailsFailed(
                batchDetailSnapshot, "任务等待中断"));
        }
        return new TmsAsyncTaskRecordDTO.BatchProcessResult(successCount.get(), failedCount.get());
    }

    /**
     * 补齐本批任务明细；已存在的明细不重复创建，支持MQ重投和失败重试。
     */
    private List<TmsAsyncTaskDetailEntity> buildUpdateReconciliationStatusDetails(String taskId,
                                                                                  List<String> batchIds,
                                                                                  Map<String, LogisticsBillCostEntity> costMap,
                                                                                  int staleDetailSeconds) {
        List<TmsAsyncTaskDetailEntity> existingDetails = asyncTaskDetailRecordService.lambdaQuery()
                .eq(TmsAsyncTaskDetailEntity::getMainId, taskId)
                .in(TmsAsyncTaskDetailEntity::getBusinessId, batchIds)
                .list();
        LocalDateTime staleBefore = LocalDateTime.now().minusSeconds(staleDetailSeconds);
        Set<String> staleIngBusinessIds = existingDetails.stream()
                .filter(d -> Objects.equals(d.getStatus(), TmsAsyncTaskRecordStatusEnum.ING.getCode()))
                .filter(d -> asyncTaskRecordService.isStaleIngDetail(d, staleBefore))
                .map(TmsAsyncTaskDetailEntity::getBusinessId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (CollUtil.isNotEmpty(staleIngBusinessIds)) {
            int staleFailedCount = asyncTaskDetailRecordService.markStaleIngDetailsFailed(
                taskId, staleIngBusinessIds, staleBefore, ApiError.ASYNC_TASK_DETAIL_TIMEOUT.getMsg());
            if (staleFailedCount > 0) {
                log.warn("对账状态变更僵死ING明细已标记失败，taskId: {}, 数量: {}", taskId, staleFailedCount);
            }
        }
        Set<String> existingIngBusinessIds = existingDetails.stream()
                .filter(d -> Objects.equals(d.getStatus(), TmsAsyncTaskRecordStatusEnum.ING.getCode()))
                .filter(d -> !staleIngBusinessIds.contains(d.getBusinessId()))
                .map(TmsAsyncTaskDetailEntity::getBusinessId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        Set<String> existingBusinessIds = existingDetails.stream()
                .map(TmsAsyncTaskDetailEntity::getBusinessId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        List<TmsAsyncTaskDetailEntity> pendingDetails = existingDetails.stream()
                .filter(d -> Objects.equals(d.getStatus(), TmsAsyncTaskRecordStatusEnum.PENDING.getCode()))
                .collect(Collectors.toList());
        LocalDateTime now = LocalDateTime.now();
        List<TmsAsyncTaskDetailEntity> addDetails = new ArrayList<>();
        for (String businessId : batchIds) {
            if (existingIngBusinessIds.contains(businessId) || existingBusinessIds.contains(businessId)) {
                continue;
            }
            LogisticsBillCostEntity entity = costMap.get(businessId);
            TmsAsyncTaskDetailEntity detail = new TmsAsyncTaskDetailEntity();
            detail.setMainId(taskId);
            detail.setBusinessType(SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode());
            detail.setBusinessId(businessId);
            detail.setBusinessCode(Objects.nonNull(entity) ? entity.getTrackNo() : businessId);
            detail.setStatus(TmsAsyncTaskRecordStatusEnum.PENDING.getCode());
            detail.setCreateTime(now);
            addDetails.add(detail);
        }
        if (CollUtil.isNotEmpty(addDetails)) {
            asyncTaskDetailRecordService.saveBatch(addDetails);
            pendingDetails.addAll(addDetails);
        }
        return pendingDetails;
    }

    @Override
    public LogisticsBillCostDTO.TotalCountDTO listTotalCount(LogisticsBillCostDTO.PagingParamDTO dto) {
        //查询费用项信息
        LogisticsBillCostDTO.TotalCountDTO totalCountDTO = baseMapper.listTotalCostValueCount(dto);
        if (ObjectUtil.isEmpty(totalCountDTO)){
            totalCountDTO = new LogisticsBillCostDTO.TotalCountDTO();
        }
        //查询计费重合计
        BigDecimal totalBillingWeightLogistics = this.baseMapper.listTotalBillingWeightLogisticsCount(dto);
        totalCountDTO.setTotalBillingWeightLogistics(totalBillingWeightLogistics);
        return totalCountDTO;
    }

    @Override
    public LogisticsBillCostDTO.SmallBagPushAllocationContext buildSmallBagPushAllocationContext() {
        CfgSettingEntity byKey = cfgSettingService.getByKey(CfgSettingEnum.ALLOCATION_SETTING.getCode());
        if (byKey == null || byKey.getDataJson() == null) {
            throw new ServiceException("分摊配置不存在，请检查系统配置");
        }
        AllocationSettingDTO allocationSettingDTO = JSON.parseObject(byKey.getDataJson().toJSONString(0), AllocationSettingDTO.class);
        Map<String, String> feeTypeSettingMaps = buildFeeTypeSettingMaps(allocationSettingDTO);
        Map<String, String> orgIdNameMaps = new HashMap<>();
        List<BaseIdDTO> accountingCompanies = sysUserFeign.listAccountingCompany();
        if (CollUtil.isNotEmpty(accountingCompanies)) {
            orgIdNameMaps = accountingCompanies.stream()
                .collect(Collectors.toMap(BaseIdDTO::getId, BaseIdDTO::getName, (o1, o2) -> o1));
        }
        return new LogisticsBillCostDTO.SmallBagPushAllocationContext(
            allocationSettingDTO,
            feeTypeSettingMaps,
            orgIdNameMaps,
            allocationSettingDTO.getWeightPackageAllocation(),
            allocationSettingDTO.getPackageOrgId(),
            allocationSettingDTO.getPackageWarehouseId()
        );
    }

    private Map<String, String> buildFeeTypeSettingMaps(AllocationSettingDTO allocationSettingDTO) {
        Map<String, String> feeTypeSettingMaps = new HashMap<>();
        for (AllocationFeeTypeEnum allocationFeeTypeEnum : AllocationFeeTypeEnum.values()) {
            if (AllocationFeeTypeEnum.SHIPPING_COST == allocationFeeTypeEnum) {
                feeTypeSettingMaps.put(allocationFeeTypeEnum.getCode(), allocationSettingDTO.getPackageShippingCost());
            } else if (AllocationFeeTypeEnum.DECLARE_COST == allocationFeeTypeEnum) {
                feeTypeSettingMaps.put(allocationFeeTypeEnum.getCode(), allocationSettingDTO.getPackageTariffFee());
            } else if (AllocationFeeTypeEnum.OTHER_COST == allocationFeeTypeEnum) {
                feeTypeSettingMaps.put(allocationFeeTypeEnum.getCode(), allocationSettingDTO.getPackageOtherFee());
            } else if (AllocationFeeTypeEnum.DEDUCTIBLE_TAX == allocationFeeTypeEnum) {
                feeTypeSettingMaps.put(allocationFeeTypeEnum.getCode(), allocationSettingDTO.getPackageDeductibleTax());
            }
        }
        return feeTypeSettingMaps;
    }

}
