package com.erp.server.tms.service.impl;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import cn.hutool.core.text.CharSequenceUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.ListDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.PagingParamDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.TabListDTO;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.entity.SmallBagCostAllocationDetailEntity;
import com.erp.model.tms.entity.SmallBagCostAllocationEntity;
import com.erp.model.tms.entity.SmallBagCostAllocationMainEntity;
import com.erp.model.tms.enums.AllocationFeeTypeEnum;
import com.erp.model.tms.enums.CostAllocationEnum;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.model.tms.enums.LogisticsBillCostCheckStatusEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.model.tms.enums.SmallBagCostAllocationBigTableStatusEnum;
import com.erp.model.tms.enums.SmallBagCostAllocationMainFeeSourceEnum;
import com.erp.model.tms.enums.SmallBagCostAllocationReportStatusEnum;
import com.erp.model.tms.enums.WeightAllocationSmallBagEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.mapper.SmallBagCostAllocationMapper;
import com.erp.server.tms.service.LogisticsBillCostService;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.LogisticsSupplierService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.SmallBagCostAllocationDetailService;
import com.erp.server.tms.service.SmallBagCostAllocationMainService;
import com.erp.server.tms.service.SmallBagCostAllocationService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
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

    @GlobalTransactional(rollbackFor = Exception.class)
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
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "小包费用分摊"));
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
