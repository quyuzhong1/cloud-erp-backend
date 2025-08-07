package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_BILL_COST;
import static com.common.business.enums.FileTaskEventEnum.IMPORT_TMS_LOGISTICS_BILL_COST;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.core.utils.*;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.model.tms.entity.*;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.UserInfoFeign;
import com.erp.server.tms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DataIdempotent;
import com.common.business.dto.base.BaseIdDTO.CodeDTO;
import com.common.business.dto.base.BaseResultDTO.AddDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.plm.entity.ProductPackEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.tms.dto.CfgSettingValueDTO.AllocationSettingDTO;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO.AddDataDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO.ConfirmAddDataDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO.EditDataDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO.EditViewDTO;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO.CostViewDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO.UpdateDTO;
import com.erp.model.tms.dto.excel.LogisticsBillCostExcelDTO;
import com.erp.model.tms.enums.*;
import com.erp.model.tms.enums.AllocationFeeTypeEnum;
import com.erp.model.tms.enums.CfgSettingEnum;
import com.erp.model.tms.enums.CostAllocationEnum;
import com.erp.model.tms.enums.DetailReconciliationTypeEnum;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.model.tms.enums.DictCostAttributionEnum;
import com.erp.model.tms.enums.DictCostCategoryEnum;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.model.tms.enums.LogisticsBillCostCheckStatusEnum;
import com.erp.model.tms.enums.LogisticsBillCostTypeEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.model.tms.enums.ReconciliationTabStatusEnum;
import com.erp.model.tms.enums.ShipmentTypeEnum;
import com.erp.model.tms.enums.ShippingFeeRuleEnum;
import com.erp.model.tms.enums.SmallBagCostAllocationBigTableStatusEnum;
import com.erp.model.tms.enums.SmallBagCostAllocationReportStatusEnum;
import com.erp.model.tms.enums.WeightAllocationEnum;
import com.erp.model.tms.enums.WeightAllocationSmallBagEnum;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.tms.listener.LogisticsBillCostExcelListener;
import com.erp.server.tms.mapper.LogisticsBillCostMapper;
import com.erp.server.tms.query.LogisticsBillCostQueryHandler;
import com.erp.server.tms.query.LogisticsLastMileCostQueryHandler;

import cn.hutool.core.bean.BeanUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Stream;

/**
 * <p>
 * 自发货费用 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-11-06
 */
@Slf4j
@Service
public class LogisticsBillCostServiceImpl extends SuperServiceImpl<LogisticsBillCostMapper, LogisticsBillCostEntity> implements LogisticsBillCostService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
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
    private SmallBagCostAllocationService smallBagCostAllocationService;
    @Resource
    private SmallBagCostAllocationDetailService smallBagCostAllocationDetailService;
    @Resource
    private InventorySkuCostService inventorySkuCostService;
    @Resource
    private InventorySkuCostDetailService inventorySkuCostDetailService;
    @Resource
    private LogisticsLargeService logisticsLargeService;
    @Resource
    private FileFeign fileFeign;
    

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsBillCostDTO.AddDTO addDTO) {
        LogisticsBillCostEntity logisticsBillCostEntity = new LogisticsBillCostEntity();
        BeanMapperUtils.copy(addDTO, logisticsBillCostEntity);

        logisticsBillCostEntity.setChannelId(addDTO.getChannelId());

        // 数据处理
        handleData(logisticsBillCostEntity);

        log.info("开始新增自发货费用");
        boolean save = super.save(logisticsBillCostEntity);
        if(!save) {
            throw new ServiceException("自发货费用保存失败");
        }
        //添加费用明细
        tmsCostDetailService.batchAdd(addDTO.getCostDetailList(),logisticsBillCostEntity.getId(), DictCostAttributionEnum.SELF_DELIVER);

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "自发货费用" , logisticsBillCostEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), logisticsBillCostEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(logisticsBillCostEntity.getId(), logisticsBillCostEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsBillCostDTO.UpdateDTO updateDTO,Boolean isImport) {
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
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "自发货费用"));
        //赋值
        LogisticsBillCostEntity logisticsBillCostEntity =  BeanMapperUtils.map(LogisticsBillCostEntity.class, old);
        logisticsBillCostEntity.setBillingWeight(updateDTO.getBillingWeight());
        logisticsBillCostEntity.setBillingWeightLogistics(updateDTO.getBillingWeightLogistics());
        logisticsBillCostEntity.setRemark(updateDTO.getRemark());
        logisticsBillCostEntity.setCurrency(updateDTO.getCurrency());

        Optional.ofNullable(updateDTO.getActualWeight()).ifPresent(logisticsBillCostEntity::setActualWeight);
        Optional.ofNullable(updateDTO.getVolumeWeight()).ifPresent(logisticsBillCostEntity::setVolumeWeight);
        Optional.ofNullable(updateDTO.getVolumeWeightLogistics()).ifPresent(logisticsBillCostEntity::setVolumeWeightLogistics);
        Optional.ofNullable(updateDTO.getWeightLogistics()).ifPresent(logisticsBillCostEntity::setWeightLogistics);
        Optional.ofNullable(updateDTO.getLogisticsBillDetailId()).ifPresent(logisticsBillCostEntity::setLogisticsBillDetailId);
        //物流单
        LogisticsBillEntity logisticsBillEntity = logisticsBillService.getById(logisticsBillCostEntity.getLogisticsBillId());
        if (ObjectUtil.isEmpty(logisticsBillEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"物流单");
        }
        logisticsBillCostEntity.setTransportNo(logisticsBillEntity.getTransportNo());
        logisticsBillCostEntity.setChannelId(logisticsBillEntity.getChannelId());
        // 数据处理
        handleData(logisticsBillCostEntity);
        log.info("编辑 开始修改自发货费用数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsBillCostEntity);
        if(!save) {
            throw new ServiceException("自发货费用保存失败：{}", JSONUtil.toJsonStr(logisticsBillCostEntity));
        }
        //更新费用明细
        tmsCostDetailService.batchUpdate(updateDTO.getCostDetailList(),logisticsBillCostEntity.getId(),DictCostAttributionEnum.SELF_DELIVER,isImport);

        // 记录主单操作日志
        log.info("编辑 开始记录自发货费用日志数据，id：【{}】", logisticsBillCostEntity.getId());
        String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsBillCostEntity.getId(), "自发货费用");
        operateLogService.addModuleOperateLogByObj(old, logisticsBillCostEntity, ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), logisticsBillCostEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsBillCostDTO.TabListDTO> tabList(PermissionsDTO dto, DictCostAttributionEnum attribution) {
        List<LogisticsBillCostDTO.TabListDTO> resultList = new ArrayList<>();
        ReconciliationTabStatusEnum[] values = ReconciliationTabStatusEnum.values();
        for (ReconciliationTabStatusEnum statusEnum : values) {
            LogisticsBillCostDTO.PagingParamDTO pagingParamDTO = new LogisticsBillCostDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            LogisticsBillCostDTO.TabListDTO resultDTO = new LogisticsBillCostDTO.TabListDTO();
            //自发货费用
            String tabSql = logisticsBillCostQueryHandler.getTabSql(statusEnum.getCode());
            //尾程费用
            if (DictCostAttributionEnum.LAST_MILE.equals(attribution)) {
                tabSql = logisticsLastMileCostQueryHandler.getTabSql(statusEnum.getCode());
            }
            HashMap<String,String> map = new HashMap<>();
            map.put("default",tabSql);
            pagingParamDTO.setSqlMap(map);
            Integer count = this.baseMapper.listCount(pagingParamDTO);
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(statusEnum.getCode());
            resultDTO.setTabFlagName(statusEnum.getName());
            resultList.add(resultDTO);
        }
        
        return resultList;
    }

    @Override
    public PagingVO<LogisticsBillCostDTO.ListDTO> paging(PagingDTO<LogisticsBillCostDTO.PagingParamDTO> pagingDTO) {
        LogisticsBillCostDTO.PagingParamDTO params = pagingDTO.getParams();
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
        Optional.ofNullable(entity).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "自发货费用"));
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
            			&& "payment".equals(entity.getPayStatus()))) {
            		String p = entity.getPayType().equals("pay") ? "付" : "退";
            		throw new ServiceException("当前对账状态为暂估确认，只有核算状态为待生成且支付状态为待" + p + "款时才能修改为非账单确认状态");
            	}
        	}
        }else if(ReconciliationStatusEnum.CONFIRMED.getCode().equals(beforeReconciliationStatus)) {
        	if(!(LogisticsBillCostCheckStatusEnum.CHECKING.getCode().equals(entity.getCheckStatus()) 
        			&& "payment".equals(entity.getPayStatus()))) {
        		String p = entity.getPayType().equals("pay") ? "付" : "退";
        		throw new ServiceException("当前对账状态为账单确认，只有核算状态为待生成且支付状态为待" + p + "款时才能修改为其他状态");
        	}
        }
        
        if("refund".equals(entity.getPayType()) && ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode().equals(reconciliationStatus)) {
        	throw new ServiceException("退款费用类型不能修改为暂估确认");
        }
        
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
        // 状态变更日志
        log.info("状态变更日志数据，id集合：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】自发货费用【{}】的【{}】单据{}操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getTransportNo(), "自发货费用", ReconciliationStatusEnum.getName(reconciliationStatus));
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), entity.getTransportNo(), "启用/停用");
        return BatchResultDTO.success(entity.getId(), entity.getTrackNo(), OperationTypeEnum.UPDATE_STATUS);
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
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Override
    public Boolean exportExcel(LogisticsBillCostDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("自发货列表", EXPORT_TMS_LOGISTICS_BILL_COST.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public Boolean invalidByLogisticsBillId(String logisticsBillId) {
        LogisticsBillCostEntity entity = this.lambdaQuery().eq(LogisticsBillCostEntity::getLogisticsBillId,logisticsBillId).one();
        if(Objects.isNull(entity)){
            return true;
        }
        entity.setReconciliationStatus(ReconciliationStatusEnum.INVALID.getCode());
        return this.updateById(entity);
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
            throw new ServiceException(ApiError.NOT_EXIST,"物流订单");
        }
        entity.setTransportNo(logisticsBillEntity.getTransportNo());
        entity.setChannelId(logisticsBillEntity.getChannelId());
        
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
                    throw new ServiceException(ApiError.ERROR_EXCHANGE_RATE_NOT_EXIST, LocalDate.now(), entity.getCurrency());
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
        List<DictBasicDTO.ViewDTO> transportStatusList = dictBasicService.getByKey(DictBasicEnum.LOGISTIC_TRACK_STATUS.getType());

        //币别信息
        Map<String, String> currencySymbolMap = FeignQuery.list(DictCurrencyEntity.class).stream().collect(Collectors.toMap(DictCurrencyEntity::getId, DictCurrencyEntity::getSymbol));

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
        	String payType = listDTO.getPayType();
        	String payStatus = listDTO.getPayStatus();
        	if(StringUtils.isNotBlank(payType) && StringUtils.isNotBlank(payStatus)) {
        		listDTO.setPayStatusName(payStatusNameMap.get(payType + "_" + payStatus));
        	}
        	listDTO.setCheckStatusName(LogisticsBillCostCheckStatusEnum.getName(listDTO.getCheckStatus()));
            listDTO.setOrderTypeName(CostBillTypeEnum.getName(listDTO.getOrderType()));
            listDTO.setSourceTypeName(SourceTypeEnum.getName(listDTO.getSourceType()));
            listDTO.setReconciliationStatusName(ReconciliationStatusEnum.getName(listDTO.getReconciliationStatus()));
            listDTO.setTransportStatus(detailIdStatus.get(listDTO.getLogisticsBillDetailId()));
            //运输状态
            String name = transportStatusList.stream().filter(obj -> obj.getCode().equals(listDTO.getTransportStatus())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setTransportStatusName(name);
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
            listDTO.setDiffShippingCost(MathUtil.subtract(exchangeActualShippingCost,exchangeEstimatedShippingCost).setScale(4, RoundingMode.DOWN));
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
     * @description: 导入数据处理
     * @author Will
     * @date: 2023/11/14 20:06
     * @param successList
     * @param errorList
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    @Override
    public void handleImportSuccessList (List<LogisticsBillCostExcelDTO> successList,List<LogisticsBillCostExcelDTO > errorList,String dictCostAttribution) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //物流单
        List<String> trackNoList = successList.stream().map(LogisticsBillCostExcelDTO::getTrackNo).collect(Collectors.toList());
        List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos = logisticsBillService.listLogisticsBillVoByTrackNo(trackNoList);

        //物流单明细
        List<String> logisticsBillIdList = logisticsBillVos.stream().map(LogisticsBillDTO.LogisticsBillVo::getId).distinct().collect(Collectors.toList());
        List<LogisticsBillDetailEntity> logisticsBillDetailList = logisticsBillDetailService.listByMainIds(logisticsBillIdList);

        //物流单费用
        List<LogisticsBillCostEntity> logisticsBillCostList = this.listByLogisticsBillIdList(logisticsBillIdList);

        //费用配置
        List<String> costNameList = successList.stream().map(LogisticsBillCostExcelDTO::getCostName).distinct().collect(Collectors.toList());
        List<TmsCfgCostEntity> tmsCfgCostList = tmsCfgCostService.listByCostNameList(costNameList);

        Map<String, List<LogisticsBillCostExcelDTO>> map = successList.stream().collect(Collectors.groupingBy(obj -> obj.getTrackNo()+"_"+obj.getOutstockCode()+"_"+obj.getPayType()));

        Map<String, List<TmsCostDetailEntity>> mainIdListMap = new HashMap<>();
        if(CollUtil.isNotEmpty(logisticsBillCostList)) {
        	List<TmsCostDetailEntity> listByMainIdList = tmsCostDetailService.listByMainIdList(logisticsBillCostList.stream().map(LogisticsBillCostEntity::getId).collect(Collectors.toList()));
        	mainIdListMap = listByMainIdList.stream().collect(Collectors.groupingBy(TmsCostDetailEntity::getMainId));
        }
        
        for ( Map.Entry<String, List<LogisticsBillCostExcelDTO>> entry : map.entrySet()) {
            List<LogisticsBillCostExcelDTO> value = entry.getValue();
            LogisticsBillCostExcelDTO billCostExcelDTO = value.get(0);

            List<TmsCostDetailDTO.UpdateDTO> updateDetailList = new ArrayList<>();
            for (LogisticsBillCostExcelDTO excelDTO : value) {
                //数据验证
                List<String> errorMsgList = checkImportData(excelDTO,logisticsBillVos,logisticsBillCostList,logisticsBillDetailList,dictCostAttribution);
                //判断导入费用名称是否重复
                long count = value.stream().filter(obj -> CharSequenceUtil.equals(obj.getCostName(), excelDTO.getCostName())).count();
                if (count > MathUtil.ONE) {
                    errorMsgList.add("费用名称不能重复录入");
                }
                TmsCfgCostEntity tmsCfgCostEntity = tmsCfgCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCostName(), excelDTO.getCostName())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(tmsCfgCostEntity)) {
                    errorMsgList.add("费用管理未找到该费用名称");
                } else {
                   if (!CharSequenceUtil.equals(tmsCfgCostEntity.getDictCostAttribution(), dictCostAttribution)) {
                       errorMsgList.add("费用归属非自发货不支持导入");
                   }
                }
                String estimatedCostValue = excelDTO.getEstimatedCostValue();
                String estimatedCurrency = excelDTO.getEstimatedCurrency();
                if(StringUtils.isNotBlank(estimatedCostValue) && StringUtils.isBlank(estimatedCurrency)) {
                	errorMsgList.add("预估金额不为空，预估币种必填");
                }
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
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
            if (CollectionUtils.isEmpty(updateDetailList)) {
                continue;
            }

            //物流单
            LogisticsBillDTO.LogisticsBillVo logisticsBillVo = logisticsBillVos.stream().filter(obj -> obj.getTrackNo().equals(billCostExcelDTO.getTrackNo())).findFirst().orElse(new LogisticsBillDTO.LogisticsBillVo());
            //物流费用单
            LogisticsBillDetailEntity logisticsBillDetailEntity = logisticsBillDetailList.stream().filter(obj -> obj.getMainId().equals(logisticsBillVo.getId())
                    && CharSequenceUtil.equals(obj.getTrackNo(),billCostExcelDTO.getTrackNo()))
                    .findFirst().orElse(new LogisticsBillDetailEntity());
            //物流费用单
            LogisticsBillCostEntity logisticsBillCostEntity = logisticsBillCostList.stream().filter(obj -> obj.getLogisticsBillId().equals(logisticsBillVo.getId())
                    && CharSequenceUtil.equals(obj.getLogisticsBillDetailId(),logisticsBillDetailEntity.getId())
            		&& CharSequenceUtil.equals(obj.getPayType(),billCostExcelDTO.getPayType()))
                    .findFirst().orElse(new LogisticsBillCostEntity());
            String id = logisticsBillCostEntity.getId();
			if (Objects.isNull(id)){
                continue;
            }
            //数据赋值
            LogisticsBillCostDTO.UpdateDTO updateDataDTO = new LogisticsBillCostDTO.UpdateDTO();
            updateDataDTO.setId(id);
            String billingWeight = billCostExcelDTO.getBillingWeight();
            if(StringUtils.isNotBlank(billingWeight)) {
            	updateDataDTO.setBillingWeight(new BigDecimal(billingWeight));
            }
            String billingWeightLogistics = billCostExcelDTO.getBillingWeightLogistics();
            if(StringUtils.isNotBlank(billingWeightLogistics)) {
            	updateDataDTO.setBillingWeightLogistics(new BigDecimal(billingWeightLogistics));
            }
            updateDataDTO.setCurrency(CharSequenceUtil.isBlank(billCostExcelDTO.getCurrency()) ? CurrencyEnum.CNY.getCurrencyCode() : billCostExcelDTO.getCurrency());
            
            List<TmsCostDetailEntity> validateList = BeanMapperUtils.copyList(TmsCostDetailEntity.class, updateDetailList);
            List<TmsCostDetailEntity> tmsCostDetailEntityList = mainIdListMap.get(id);
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
            
            if(CollUtil.isEmpty(updateDetailList)) {
            	continue;
            }
            updateDataDTO.setCostDetailList(updateDetailList);
            this.update(updateDataDTO,Boolean.TRUE);
        }
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
            //填充信息
            this.handleData(entity);
            TmsFirstMileReconciliationDetailEntity actualDetailEntity = actualMap.get(detailEntity.getSourceId());
            if (null == actualDetailEntity) {
                // 移除
                entity.setReconciliationStatus(ReconciliationStatusEnum.TO_BE_GENERATED.getCode());
                continue;
            }
            // 更新实际重量和体积重, 计费重
            entity.setVolumeWeightLogistics(actualDetailEntity.getVolumeWeight());
            entity.setWeightLogistics(actualDetailEntity.getActualWeight());
            // 费用重取最大
            entity.setBillingWeight(actualDetailEntity.getVolumeWeight().max(actualDetailEntity.getActualWeight()));
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
     * @description: 数据验证
     * @author Will
     * @date: 2023/11/14 20:05
     * @param excelDTO
     * @param logisticsBillList
     * @param logisticsBillCostList
     * @return List<String>
     */
    private List<String> checkImportData (LogisticsBillCostExcelDTO excelDTO,List<LogisticsBillDTO.LogisticsBillVo> logisticsBillList
            ,List<LogisticsBillCostEntity> logisticsBillCostList,List<LogisticsBillDetailEntity> logisticsBillDetailList ,String dictCostAttribution) {
        List<String> errorMsgList = new ArrayList<>();
        LogisticsBillDTO.LogisticsBillVo logisticsBillVo = logisticsBillList.stream().filter(obj -> obj.getTrackNo().equals(excelDTO.getTrackNo())).findFirst().orElse(null);
        if (Objects.isNull(logisticsBillVo)) {
            errorMsgList.add("未找到对应物流单");
        } else {
            //物流单明细
            LogisticsBillDetailEntity detailEntity = logisticsBillDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(), logisticsBillVo.getId()) && CharSequenceUtil.equals(excelDTO.getTrackNo(), obj.getTrackNo())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(detailEntity)) {
                errorMsgList.add("未找到物流跟踪单号对应的物流单明细");
            }
            //物流费用单
            List<LogisticsBillCostEntity> logisticsBillCostEntityList = logisticsBillCostList.stream()
            		.filter(obj -> obj.getLogisticsBillId().equals(logisticsBillVo.getId()) 
            				&& CharSequenceUtil.equals(excelDTO.getTrackNo(),obj.getTrackNo()) 
            				&& CharSequenceUtil.equals(excelDTO.getPayType(),obj.getPayType()))
            		.collect(Collectors.toList());
            LogisticsBillCostEntity logisticsBillCostEntity = null;
            if (CollUtil.isEmpty(logisticsBillCostEntityList)) {
                errorMsgList.add("未找到出库单和运输单号对应对账类型的物流费用单");
            } else {
            	if(logisticsBillCostEntityList.size() > 1) {
            		errorMsgList.add("出库单和运输单号对应对账类型的物流费用单有多条，请在页面编辑指定物流费用单");
            	}else {
            		logisticsBillCostEntity = logisticsBillCostEntityList.get(0);
                    if (!CharSequenceUtil.equals(logisticsBillCostEntity.getType(),dictCostAttribution)) {
                        errorMsgList.add(CharSequenceUtil.format("需要导入【{}】物流单费用信息",DictCostAttributionEnum.getName(dictCostAttribution)));
                    }
            	}
            }

            if (ObjectUtil.isNotEmpty(logisticsBillCostEntity)){
                String currency = CharSequenceUtil.isBlank(excelDTO.getCurrency()) ? logisticsBillCostEntity.getCurrency() : excelDTO.getCurrency();
				excelDTO.setCurrency(currency);

//                if (ObjectUtil.isNotEmpty(logisticsBillCostEntity) && !CharSequenceUtil.equals(excelDTO.getCurrency(),logisticsBillCostEntity.getCurrency())) {
//                    errorMsgList.add("导入币别与物流费用单币别不一致");
//                }
                if (ReconciliationStatusEnum.CONFIRMED.getCode().equals(logisticsBillCostEntity.getReconciliationStatus())
                        || ReconciliationStatusEnum.INVALID.getCode().equals(logisticsBillCostEntity.getReconciliationStatus())) {
                    errorMsgList.add("物流费用单已确认或已作废不支持更新");
                }
            }


        }
        return errorMsgList;
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
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
	@Override
	public List<BaseResultDTO.AddDTO> addPayAndRefund(List<LogisticsBillCostDTO.AddDataDTO> dtoList) {
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
				addList.add(this.add(addDTO));
			} catch (ServiceException e) {
				throw new ServiceException("物流运单号：" + logisticsBillCostEntity.getTransportNo() + e.getMessage());
			}
    	}
    	
		return addList;
	}

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
	@Override
	public void addPayAndRefundConfirm(ConfirmAddDataDTO dto) {
    	if(dto.getConfirmTime() == null) {
    		dto.setConfirmTime(LocalDateTime.now());
    	}
    	LocalDateTime confirmTime = dto.getConfirmTime();
		List<AddDTO> dtoList = this.addPayAndRefund(dto.getAddDataDTOList());
		dtoList.forEach(addDTO -> this.updateReconciliationStatus(addDTO.getId(), ReconciliationStatusEnum.CONFIRMED.getCode(), confirmTime));
	}

	@Override
	public BatchResultDTO updatePayStatus(String id, String payStatus, LocalDateTime payTime) {
        LogisticsBillCostEntity entity = Optional.ofNullable(super.getById(id)).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "自发货费用"));
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
		this.update(updateDataDTO , false);
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

	@Transactional(rollbackFor = Exception.class)
	@Override
	@DataIdempotent(keyIdName = "id")
	public BatchResultDTO pushAllocation(String id, String reportDate) {
		LogisticsBillCostEntity entity = getById(id);
		String reconciliationStatus = entity.getReconciliationStatus();
		if(!(ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode().equals(reconciliationStatus)
				|| ReconciliationStatusEnum.CONFIRMED.getCode().equals(reconciliationStatus))) {
			throw new ServiceException("只支持对账状态为暂估确认或账单确认下推分摊");
		}
		List<SmallBagCostAllocationMainEntity> smallBagCostAllocationMainEntityList = smallBagCostAllocationMainService.lambdaQuery()
				.eq(SmallBagCostAllocationMainEntity::getCostId, id).list();
		if(CollUtil.isNotEmpty(smallBagCostAllocationMainEntityList)) {
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
			String warehouseId = FeignQuery.getById(SoOutstockEntity.class, outstockId).getWarehouseId();
			soOutstockDetailEntityList.forEach(s -> s.setWarehouseId(warehouseId));
		}
		
		LogisticsBillCostTypeEnum costType = ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode().equals(reconciliationStatus) 
				? LogisticsBillCostTypeEnum.ESTIMATED : LogisticsBillCostTypeEnum.ACTUAL;
		List<TmsCostDetailDTO.CostViewDTO> costList = tmsCostDetailService.listCostByMainIdList(Collections.singletonList(id));
		Map<String, List<CostViewDTO>> costCategoryMaps = new HashMap<>();
		if(CollUtil.isNotEmpty(costList)) {
			costList = costList.stream().filter(c -> costType.getCode().equals(c.getType())).collect(Collectors.toList());
			costCategoryMaps = costList.stream().collect(Collectors.groupingBy(TmsCostDetailDTO.CostViewDTO::getDictCostCategory));
		}
		
		List<SmallBagCostAllocationEntity> addSmallBagCostAllocationEntityList = new ArrayList<>();
		List<SmallBagCostAllocationDetailEntity> addSmallBagCostAllocationDetailEntityList = new ArrayList<>();
		
		CfgSettingEntity byKey = cfgSettingService.getByKey(CfgSettingEnum.ALLOCATION_SETTING.getCode());
		Map<String, String> feeTypeSettingMaps = new HashMap<>();
		AllocationSettingDTO allocationSettingDTO = JSON.parseObject(byKey.getDataJson().toJSONString(0), AllocationSettingDTO.class);
		String weightPackageAllocation = allocationSettingDTO.getWeightPackageAllocation();
        String packageOrgId = allocationSettingDTO.getPackageOrgId();
        String packageWarehouseId = allocationSettingDTO.getPackageWarehouseId();
        AllocationFeeTypeEnum[] values = AllocationFeeTypeEnum.values();
		for(AllocationFeeTypeEnum allocationFeeTypeEnum : values) {
			if(AllocationFeeTypeEnum.SHIPPING_COST == allocationFeeTypeEnum) {
				feeTypeSettingMaps.put(allocationFeeTypeEnum.getCode(), allocationSettingDTO.getPackageShippingCost());
			}else if(AllocationFeeTypeEnum.DECLARE_COST == allocationFeeTypeEnum) {
				feeTypeSettingMaps.put(allocationFeeTypeEnum.getCode(), allocationSettingDTO.getPackageTariffFee());
			}else if(AllocationFeeTypeEnum.OTHER_COST == allocationFeeTypeEnum) {
				feeTypeSettingMaps.put(allocationFeeTypeEnum.getCode(), allocationSettingDTO.getPackageOtherFee());
			}else if(AllocationFeeTypeEnum.DEDUCTIBLE_TAX == allocationFeeTypeEnum) {
				feeTypeSettingMaps.put(allocationFeeTypeEnum.getCode(), allocationSettingDTO.getPackageDeductibleTax());
			}
		}

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
		
		Map<String, String> orgIdNameMaps = sysUserFeign.getAccountingCompanyList(new ArrayList<>(wareIdOrgIdMaps.values())).stream().collect(Collectors.toMap(CodeDTO::getId, CodeDTO::getName));
		
		String feeRule = entity.getFeeRule();
		if(StringUtils.isNotBlank(feeRule) && !ShippingFeeRuleEnum.BILLING_WEIGHT.getCode().equals(feeRule)) {
			if(WeightAllocationEnum.OUTSTOCK_CHARGED_WEIGHT.getCode().equals(weightPackageAllocation)) {
				weightPackageAllocation = feeRule;
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

            String orgId = CharSequenceUtil.isBlank(packageOrgId) ? wareIdOrgIdMaps.get(soOutstockDetailEntity.getWarehouseId()) : packageOrgId;
			String orgName = orgIdNameMaps.get(orgId);
			
			Integer actualQty = soOutstockDetailEntity.getActualQty();
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
				BigDecimal billAmountExchange = smallBagCostAllocationDetailEntity.getBillAmount().multiply(rate).setScale(4 , RoundingMode.DOWN);
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
        downloadTaskFeign.saveImportTask("自发货列表导入", IMPORT_TMS_LOGISTICS_BILL_COST.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importLogisticsBillCost(BaseDTO.ImportDTO dto) {
        LogisticsBillCostExcelListener excelListenerUtil = new LogisticsBillCostExcelListener(dto.getTaskId());
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), LogisticsBillCostExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        //导出错误数据
        List<LogisticsBillCostExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "自发货费用错误信息.xlsx";
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
}
