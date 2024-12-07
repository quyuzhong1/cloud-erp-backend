package com.erp.server.tms.service.impl;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

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
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO.ListDTO;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO.PagingParamDTO;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO.TabListDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.TransferDeclareCostAllocationDetailEntity;
import com.erp.model.tms.entity.TransferDeclareCostAllocationEntity;
import com.erp.model.tms.entity.TransferDeclareCostAllocationMainEntity;
import com.erp.model.tms.enums.AllocationFeeTypeEnum;
import com.erp.model.tms.enums.CostAllocationEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.model.tms.enums.SmallBagCostAllocationBigTableStatusEnum;
import com.erp.model.tms.enums.SmallBagCostAllocationMainFeeSourceEnum;
import com.erp.model.tms.enums.SmallBagCostAllocationReportStatusEnum;
import com.erp.model.tms.enums.TmsB2cDeclareReconciliationStatusEnum;
import com.erp.model.tms.enums.TransferDeclareCostAllocationReportStatusEnum;
import com.erp.model.tms.enums.WeightAllocationSmallBagEnum;
import com.erp.server.tms.mapper.TransferDeclareCostAllocationMapper;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TransferDeclareCostAllocationDetailService;
import com.erp.server.tms.service.TransferDeclareCostAllocationMainService;
import com.erp.server.tms.service.TransferDeclareCostAllocationService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
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

    @GlobalTransactional(rollbackFor = Exception.class)
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
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中转费用分摊"));
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
		List<TransferDeclareCostAllocationMainEntity> list = transferDeclareCostAllocationMainService.list();
		TransferDeclareCostAllocationReportStatusEnum[] values = TransferDeclareCostAllocationReportStatusEnum.values();
        for (TransferDeclareCostAllocationReportStatusEnum statusEnum : values) {
            LogisticsBillCostDTO.PagingParamDTO pagingParamDTO = new LogisticsBillCostDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            TransferDeclareCostAllocationDTO.TabListDTO resultDTO = new TransferDeclareCostAllocationDTO.TabListDTO();
            String code = statusEnum.getCode();
			Integer count = (int)list.stream().filter(l -> l.getReportStatus().equals(code)).count();
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
		Map<String, String> currencyIdSymbolMap = FeignQuery.getByIds(DictCurrencyEntity.class , records.stream().map(ListDTO::getCurrency).collect(Collectors.toList()))
			.stream().collect(Collectors.toMap(DictCurrencyEntity::getId, DictCurrencyEntity::getSymbol));
		Map<String, LogisticsChannelEntity> channelIdMaps = logisticsChannelService.listByIds(records.stream().map(ListDTO::getChannelId).collect(Collectors.toList())).stream().collect(Collectors.toMap(LogisticsChannelEntity::getId, l -> l));
		List<String> shopIds = records.stream().map(ListDTO::getShopId).collect(Collectors.toList());
		List<ShopInfoEntity> shopInfoEntityList = FeignQuery.create(ShopInfoEntity.class).in(ShopInfoEntity::getId, 
				shopIds).list();
		Map<String, String> shopIdNameMap = shopInfoEntityList.stream().collect(Collectors.toMap(ShopInfoEntity::getId, ShopInfoEntity::getName));
		List<String> countryIds = records.stream().map(ListDTO::getToCountry).collect(Collectors.toList());
		List<DictCountryEntity> dictCountryEntityList = FeignQuery.create(DictCountryEntity.class).in(DictCountryEntity::getId, 
				countryIds).list();
		Map<String, String> countryIdNameMap = dictCountryEntityList.stream().collect(Collectors.toMap(DictCountryEntity::getId, DictCountryEntity::getNameCn));
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
			BigDecimal unitCost = dto.getUnitCost();
			if(unitCost != null) {
				dto.setTotalCost(unitCost.multiply(new BigDecimal(dto.getDeliveryQty())));
			}
			dto.setFeeTypeName(AllocationFeeTypeEnum.getName(dto.getFeeType()));
			dto.setFeeAllocationTypeName(CostAllocationEnum.getName(dto.getFeeAllocationType()));
			dto.setWeightAllocationTypeName(WeightAllocationSmallBagEnum.getName(dto.getWeightAllocationType()));
			dto.setCurrencySymbol(currencyIdSymbolMap.get(dto.getCurrency()));
		}
	}

	@Override
	public BatchResultDTO updateReportStatus(String id, String reportDate, String reportStatus) {
		TransferDeclareCostAllocationMainEntity transferDeclareCostAllocationMainEntity = transferDeclareCostAllocationMainService.getById(id);
		if(StringUtils.isBlank(reportDate) && StringUtils.isBlank(reportStatus)) {
			throw new ServiceException("会计期间和核算状态不能同时为空");
		}
		if(StringUtils.isNotBlank(reportStatus)) {
			if(reportStatus.equals(transferDeclareCostAllocationMainEntity.getReportStatus())) {
				throw new ServiceException("更新前后核算状态一致");
			}
			if(reportStatus.equals(SmallBagCostAllocationReportStatusEnum.TOBECONFIRM.getCode()) 
					&& SmallBagCostAllocationBigTableStatusEnum.DONE.getCode().equals(transferDeclareCostAllocationMainEntity.getBigTableStatus())) {
				throw new ServiceException("物流大表已生成，无法从已确认更新为待确认");
			}
		}
		transferDeclareCostAllocationMainService.lambdaUpdate().eq(TransferDeclareCostAllocationMainEntity::getId, id)
			.set(StringUtils.isNotBlank(reportDate) , TransferDeclareCostAllocationMainEntity::getAccountDate, reportDate)
			.set(StringUtils.isNotBlank(reportStatus) , TransferDeclareCostAllocationMainEntity::getReportStatus, reportStatus)
			.update();
		return BatchResultDTO.success(id, id, "更新核算状态成功");
	}

	@Override
	public BatchResultDTO reAllocation(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public BatchResultDTO delete(String id) {
		TransferDeclareCostAllocationMainEntity transferDeclareCostAllocationMainEntity = transferDeclareCostAllocationMainService.getById(id);
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
}
