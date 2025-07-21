package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.UnitEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.erp.model.oms.entity.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.tms.dto.CfgReconciliationFieldDTO;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDTO;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDetailDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO.CostViewDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO.UpdateDTO;
import com.erp.model.tms.dto.excel.DeclareReconciliationStandardExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.tms.listener.DeclareReconciliationConfigExcelListener;
import com.erp.server.tms.listener.DeclareReconciliationStandardExcelListener;
import com.erp.server.tms.mapper.TmsB2cDeclareReconciliationDetailMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_TMS_B2C_DECLARE_RECONCILIATION_DETAIL;

/**
 * <p>
 * b2c报关对账单明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-03-19
 */
@Slf4j
@Service
public class TmsB2cDeclareReconciliationDetailServiceImpl extends SuperServiceImpl<TmsB2cDeclareReconciliationDetailMapper, TmsB2cDeclareReconciliationDetailEntity> implements TmsB2cDeclareReconciliationDetailService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private TmsB2cDeclareReconciliationService tmsB2cDeclareReconciliationService;

    @Resource
    private TransferDeclareService transferDeclareService;

    @Resource
    private TransferDeclareDetailService transferDeclareDetailService;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private CfgReconciliationFieldService cfgReconciliationFieldService;

    @Resource
    private TmsCostDetailService tmsCostDetailService;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private SysDictFeign sysDictFeign;

    @Resource
    private TransferLogisticsSupplierService transferLogisticsSupplierService;

    @Resource
    private TmsCfgCostService tmsCfgCostService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SettingForecastService settingForecastService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private FileFeign filefeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(List<TmsB2cDeclareReconciliationDetailDTO.AddDTO> detailList) {
        List<TmsB2cDeclareReconciliationDetailEntity> reconciliationDetailList = BeanMapperUtils.copyList(TmsB2cDeclareReconciliationDetailEntity.class, detailList);

        //新增数据验证
        checkAddData(reconciliationDetailList);
        //新增数据处理
        List<TmsB2cDeclareReconciliationDetailEntity> resultList = handleAddData(reconciliationDetailList);
        //无新增数据则直接返回
        if (CollectionUtils.isEmpty(resultList)) {
            return new BaseResultDTO.AddDTO();
        }
        log.info("开始新增报关对账单明细");
        boolean save = super.saveBatch(reconciliationDetailList);
        if(!save) {
            throw new ServiceException("报关对账单明细保存失败");
        }
        // 操作日志
        return new BaseResultDTO.AddDTO(reconciliationDetailList.get(0).getId(), reconciliationDetailList.get(0).getId());
    }


    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<TmsB2cDeclareReconciliationDetailDTO.UpdateDTO> detailList, String mainId) {

        if (CollectionUtils.isEmpty(detailList)) {
            return Boolean.TRUE;
        }
        List<TmsB2cDeclareReconciliationDetailEntity> list =  BeanMapperUtils.copyList(TmsB2cDeclareReconciliationDetailEntity.class, detailList);

        handleUpdateData (list,mainId);

        //原明细数据被删除的需要清除mainId
        List<TmsB2cDeclareReconciliationDetailEntity> oldList = this.listMainIdList(Arrays.asList(mainId));
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<TmsB2cDeclareReconciliationDetailEntity> deleteList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = deleteList.stream().map(obj -> new Pair<>(mainId, obj.getSoCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个销售订单【%s】", ModuleTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode(),pairList,"编辑操作");
            //更新主表id
            if (CollectionUtils.isNotEmpty(deleteList)) {
                deleteList.stream().forEach(obj -> obj.setMainId(""));
                list.addAll(deleteList);
            }
        }

        log.info("编辑 开始修改报关对账单数据，id：【{}】", mainId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("报关对账单明细保存失败");
        }
        List<String> toBeConfirmIds = list.stream().filter(l -> TmsB2cDeclareReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equals(l.getStatus())).map(TmsB2cDeclareReconciliationDetailEntity::getId).collect(Collectors.toList());
		if(CollUtil.isNotEmpty(toBeConfirmIds)) {
			lambdaUpdate().in(TmsB2cDeclareReconciliationDetailEntity::getId, toBeConfirmIds)
        	.set(TmsB2cDeclareReconciliationDetailEntity::getConfirmDate, null)
        	.update();
		}
		List<String> confirmIds = list.stream().filter(l -> !TmsB2cDeclareReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equals(l.getStatus())).map(TmsB2cDeclareReconciliationDetailEntity::getId).collect(Collectors.toList());
		if(CollUtil.isNotEmpty(confirmIds)) {
			lambdaUpdate().in(TmsB2cDeclareReconciliationDetailEntity::getId, confirmIds)
				.isNull(TmsB2cDeclareReconciliationDetailEntity::getConfirmDate)
				.set(TmsB2cDeclareReconciliationDetailEntity::getConfirmDate, new Date())
				.update();
		}

        //更新费用信息
        addOrUpdateCost(list);
        return Boolean.TRUE;
    }

    /**
     * @description: 更新费用信息
     * @author Will
     * @date: 2024/3/27 18:24
     * @param list
     */
    private void addOrUpdateCost(List<TmsB2cDeclareReconciliationDetailEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<TmsB2cDeclareReconciliationDetailEntity> allNewDetailList = list.stream().filter(l -> StringUtils.isNotBlank(l.getMainId())).collect(Collectors.toList());
        TmsB2cDeclareReconciliationEntity declareReconciliationEntity = tmsB2cDeclareReconciliationService.getById(allNewDetailList.get(0).getMainId());
        
        List<String> channelIds = allNewDetailList.stream().map(TmsB2cDeclareReconciliationDetailEntity::getLogisticsChannelId).collect(Collectors.toList());
        List<String> dgFlagList = settingForecastService.lambdaQuery().in(SettingForecastEntity::getTransferLogisticsChannelId, channelIds).list()
        		.stream().filter(SettingForecastEntity::getDgWarseHouse).map(SettingForecastEntity::getTransferLogisticsChannelId).collect(Collectors.toList());
        List<String> xgFlagList = settingForecastService.lambdaQuery().in(SettingForecastEntity::getTransferLogisticsChannelId, channelIds).list()
        		.stream().filter(SettingForecastEntity::getXgWarseHouse).map(SettingForecastEntity::getTransferLogisticsChannelId).collect(Collectors.toList());
        BigDecimal dgWarseHouseFee = declareReconciliationEntity.getDgWarseHouseFee();
        BigDecimal xgWarseHouseFee = declareReconciliationEntity.getXgWarseHouseFee();
        BigDecimal dgTotalWeight = BigDecimal.ZERO;
        BigDecimal xgTotalWeight = BigDecimal.ZERO;
        String lastdgId = "";
        String lastxgId = "";
        for (TmsB2cDeclareReconciliationDetailEntity detailEntity : allNewDetailList) {
        	BigDecimal estimateWeight = detailEntity.getEstimateWeight();
        	if(estimateWeight != null) {
        		if(detailEntity.getEstimateWeightUnit().equals("kg")) {
        			estimateWeight = estimateWeight.multiply(new BigDecimal("1000"));
        		}
        		String logisticsChannelId = detailEntity.getLogisticsChannelId();
            	if(dgFlagList.contains(logisticsChannelId)) {
            		dgTotalWeight = dgTotalWeight.add(estimateWeight);
            		lastdgId = detailEntity.getSourceDetailId();
            	}
            	if(xgFlagList.contains(logisticsChannelId)) {
            		xgTotalWeight = xgTotalWeight.add(estimateWeight);
            		lastxgId = detailEntity.getSourceDetailId();
            	}
        	}
        }
        
        BigDecimal totalDg = BigDecimal.ZERO;
        BigDecimal totalXg = BigDecimal.ZERO;
        Map<String, String> feeNameIdMaps = tmsCfgCostService.lambdaQuery()
	        .eq(TmsCfgCostEntity::getDictCostAttribution, DictCostAttributionEnum.DECLARE.getCode())
	        .eq(TmsCfgCostEntity::getDictCostCategory, DictCostCategoryEnum.SHIPPING_COST.getCode())
	        .in(TmsCfgCostEntity::getCostName, Arrays.asList("东莞仓运费" , "香港仓运费"))
	        .list().stream().collect(Collectors.toMap(TmsCfgCostEntity::getCostName, TmsCfgCostEntity::getId));
        String dgCostId = feeNameIdMaps.get("东莞仓运费");
        if(StringUtils.isBlank(dgCostId)) {
        	throw new ServiceException("报关物流运费的东莞仓运费未配置");
        }
        String xgCostId = feeNameIdMaps.get("香港仓运费");
        if(StringUtils.isBlank(xgCostId)) {
        	throw new ServiceException("报关物流运费的香港仓运费未配置");
        }
        
        Map<String, String> mainIdCurrencyMap = tmsCostDetailService.listCostByMainIdList(list.stream().map(TmsB2cDeclareReconciliationDetailEntity::getId).collect(Collectors.toList()))
        	.stream().filter(t -> t.getDictCostCategory().equals(DictCostCategoryEnum.SHIPPING_COST.getCode()))
        	.collect(Collectors.toMap(CostViewDTO::getMainId, CostViewDTO::getCurrency , (m1 , m2) -> m1));
        List<String> shipingCostIds = tmsCfgCostService.listCostAttributionAndCategory(DictCostAttributionEnum.DECLARE.getCode(), DictCostCategoryEnum.SHIPPING_COST.getCode())
        	.stream().map(TmsCfgCostEntity::getId).collect(Collectors.toList());
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        Map<String, BigDecimal> rateMap = new HashMap<>();
        for (TmsB2cDeclareReconciliationDetailEntity detailEntity : list) {
            String currency = "";
        	List<TmsCostDetailDTO.UpdateDTO> updateList = detailEntity.getUpdateList();
            if(updateList == null) {
            	updateList = new ArrayList<>();
            }
            
            UpdateDTO updateDto = updateList.stream().filter(u -> shipingCostIds.contains(u.getCfgCostId())).findFirst().orElse(null);
            if(updateDto != null) {
            	currency = updateDto.getCurrency();
            }else {
            	currency = mainIdCurrencyMap.get(detailEntity.getId());
            }
            if(StringUtils.isBlank(currency)) {
            	currency = "CNY";
            }
            
            BigDecimal unitDgFee = BigDecimal.ZERO;
            BigDecimal unitXgFee = BigDecimal.ZERO;
            BigDecimal estimateWeight = detailEntity.getEstimateWeight();
            if(estimateWeight != null) {
            	String logisticsChannelId = detailEntity.getLogisticsChannelId();
            	if(detailEntity.getEstimateWeightUnit().equals("kg")) {
        			estimateWeight = estimateWeight.multiply(new BigDecimal("1000"));
        		}
            	String sourceDetailId = detailEntity.getSourceDetailId();
            	if(dgWarseHouseFee != null && dgTotalWeight.compareTo(BigDecimal.ZERO) != 0 && dgFlagList.contains(logisticsChannelId)) {
            		unitDgFee = dgWarseHouseFee.multiply(estimateWeight).divide(dgTotalWeight , 4 , RoundingMode.HALF_UP);
            		if(lastdgId.equals(sourceDetailId)) {
            			unitDgFee = dgWarseHouseFee.subtract(totalDg);
            		}
            		totalDg = totalDg.add(unitDgFee);
                }
                if(xgWarseHouseFee != null && xgTotalWeight.compareTo(BigDecimal.ZERO) != 0 && xgFlagList.contains(logisticsChannelId)) {
                	unitXgFee = xgWarseHouseFee.multiply(estimateWeight).divide(xgTotalWeight , 4 , RoundingMode.HALF_UP);
                	if(lastxgId.equals(sourceDetailId)) {
                		unitXgFee = xgWarseHouseFee.subtract(totalXg);
            		}
                	totalXg = totalXg.add(unitXgFee);
                }
            }
            
            BigDecimal rate = rateMap.get(currency);
        	if(rate == null) {
        		rate = dmpTaskFeign.getRate(date, currency);
        		if(ObjectUtil.isEmpty(rate)){
                    log.error("币别【{}】,汇率为空，请维护汇率",currency);
                    throw new ServiceException("汇率为空，请维护汇率");
                }
        	}
        	rateMap.put(currency, rate);
            TmsCostDetailDTO.UpdateDTO dgDto = new TmsCostDetailDTO.UpdateDTO();
            dgDto.setCostValue(unitDgFee.divide(rate , 4 , RoundingMode.HALF_UP));
            dgDto.setType(LogisticsBillCostTypeEnum.ACTUAL.getCode());
            dgDto.setCfgCostId(dgCostId);
            dgDto.setSourceType(SourceTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode());
            dgDto.setCurrency(currency);
    		updateList.add(dgDto);
    		
    		TmsCostDetailDTO.UpdateDTO xgDto = new TmsCostDetailDTO.UpdateDTO();
    		xgDto.setCostValue(unitXgFee.divide(rate , 4 , RoundingMode.HALF_UP));
    		xgDto.setType(LogisticsBillCostTypeEnum.ACTUAL.getCode());
    		xgDto.setCfgCostId(xgCostId);
    		xgDto.setSourceType(SourceTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode());
    		xgDto.setCurrency(currency);
    		updateList.add(xgDto);
            
            tmsCostDetailService.batchUpdate(updateList,detailEntity.getId(),DictCostAttributionEnum.DECLARE,Boolean.FALSE);
        }
        //更新报关明细实际费用
        updateDeclareReconciliationDetailCost(list);
    }


    @Override
    public void exportDetailList(TmsB2cDeclareReconciliationDetailDTO.ExportDTO param) {
        downloadTaskFeign.saveExportTask("b2c报关对账单明细导出", EXPORT_TMS_TMS_B2C_DECLARE_RECONCILIATION_DETAIL.getCode(), param);
    }

    @Override
    public PagingVO<TmsB2cDeclareReconciliationDetailDTO.ListDTO> paging(PagingDTO<TmsB2cDeclareReconciliationDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TmsB2cDeclareReconciliationDetailDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<TmsB2cDeclareReconciliationDetailEntity> newList, List<TmsB2cDeclareReconciliationDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(TmsB2cDeclareReconciliationDetailEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(TmsB2cDeclareReconciliationDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * @description: 根据主表id集合查询
     * @author Will
     * @date: 2024/3/26 12:01
     * @param mainIdList
     * @return List<TmsB2cDeclareReconciliationDetailEntity>
     */
    @Override
    public List<TmsB2cDeclareReconciliationDetailEntity> listMainIdList (List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.EMPTY_LIST;
        }
       return lambdaQuery().in(TmsB2cDeclareReconciliationDetailEntity::getMainId,mainIdList).orderByDesc(TmsB2cDeclareReconciliationDetailEntity::getDate).list();
    }

    @Override
    public BatchResultDTO updateStatus(String id, String status) {
        TmsB2cDeclareReconciliationDetailEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到b2c报关对账单明细数据"));
        if (!CharSequenceUtil.equals(entity.getStatus(), TmsB2cDeclareReconciliationStatusEnum.TO_BE_CONFIRM.getCode())) {
            throw new ServiceException("只有待对账数据支持更新对账");
        }
        lambdaUpdate().eq(TmsB2cDeclareReconciliationDetailEntity::getId,id)
                .set(TmsB2cDeclareReconciliationDetailEntity::getStatus,status)
                .update();
        // 记录主单操作日志
        operateLogService.addModuleOperateLog(CharSequenceUtil.format("销售订单【{}】更新状态为【{}】",entity.getSoCode(), TmsB2cDeclareReconciliationStatusEnum.getName(status)), ModuleTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode(), entity.getId(), "更新状态操作");
        return BatchResultDTO.success(entity.getId(), entity.getSoCode(), OperationTypeEnum.UPDATE_STATUS);
    }

    @Override
    public Boolean cleanDetailMainId(String id) {
        return  lambdaUpdate().eq(TmsB2cDeclareReconciliationDetailEntity::getMainId,id)
                .set(TmsB2cDeclareReconciliationDetailEntity::getMainId,"")
                .update();
    }

    @Override
    public String getCurrencyById(String id) {
        return  baseMapper.getCurrencyById(id);
    }

    @Override
    public void autoGenerateDeclareReconciliation(LocalDate startDate, LocalDate endDate) {
        List<TmsB2cDeclareReconciliationDetailEntity> list =  baseMapper.listAutoGenerateDeclareReconciliation(startDate,endDate);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Map<String, List<TmsB2cDeclareReconciliationDetailEntity>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getLogisticsSupplierId()));
        for (Map.Entry<String, List<TmsB2cDeclareReconciliationDetailEntity>> entry : map.entrySet()) {
            List<TmsB2cDeclareReconciliationDetailEntity> value = entry.getValue();
            TmsB2cDeclareReconciliationDTO.AddDTO addDTO = new TmsB2cDeclareReconciliationDTO.AddDTO();
            addDTO.setStartDate(startDate);
            addDTO.setEndDate(endDate);
            addDTO.setLogisticsSupplierId(entry.getKey());
            addDTO.setLogisticsSupplierName(value.get(0).getLogisticsSupplierName());
            List<TmsB2cDeclareReconciliationDetailDTO.UpdateDTO> detailList = BeanMapperUtils.copyList(TmsB2cDeclareReconciliationDetailDTO.UpdateDTO.class, value);
            addDTO.setDetailList(detailList);
            tmsB2cDeclareReconciliationService.add(addDTO);
        }
    }

    @Override
    public LinkedList<String> thirdFieldListName(TmsB2cDeclareReconciliationDetailDTO.ExcelDownloadTemplateDTO dto) {
        TmsB2cDeclareReconciliationEntity old = tmsB2cDeclareReconciliationService.getById(dto.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "b2c报关对账单"));

        TransferLogisticsSupplierEntity transferLogisticsSupplierEntity = transferLogisticsSupplierService.getById(old.getLogisticsSupplierId());
        Optional.ofNullable(transferLogisticsSupplierEntity).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中转物流商"));

        LinkedList<String> thirdFieldList = cfgReconciliationFieldService.thirdFieldListName(Arrays.asList(CfgReconciliationTypeEnum.B2C_DECLARE.getCode()), transferLogisticsSupplierEntity.getSupplierId(), Boolean.TRUE);
        return thirdFieldList;
    }

    @Override
    public PagingVO<TmsB2cDeclareReconciliationDetailDTO.ListDTO> exportB2cDeclareReconciliationDetail(PagingDTO<TmsB2cDeclareReconciliationDetailDTO.ExportDTO> dto) {
        Page<TmsB2cDeclareReconciliationDetailDTO.ListDTO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            fillList(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public TmsB2cDeclareReconciliationDetailDTO.ImportDTO importFile(TmsB2cDeclareReconciliationDetailDTO.ExcelImportDTO excelImportDTO, HttpServletResponse response) {
        switch (excelImportDTO.getTypeEnum()) {
            case STANDARD:
                return importStandardFile(excelImportDTO.getExcelFile(),excelImportDTO.getId());
            case CONFIG:
                return importConfigFile(excelImportDTO.getExcelFile(),excelImportDTO.getId());
            default:
                throw new ServiceException("输入类型有误");
        }
    }
    
    /**
     * @description: 导入标准模板
     * @author Will
     * @date: 2024/4/15 15:34
     * @param excelFile 
     * @param id
     * @return ImportDTO 
     */
    private TmsB2cDeclareReconciliationDetailDTO.ImportDTO importStandardFile(MultipartFile excelFile,String id) {
        TmsB2cDeclareReconciliationDetailDTO.ImportDTO importDTO = new TmsB2cDeclareReconciliationDetailDTO.ImportDTO();

        DeclareReconciliationStandardExcelListener excelListenerUtil = new DeclareReconciliationStandardExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), DeclareReconciliationStandardExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<DeclareReconciliationStandardExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<DeclareReconciliationStandardExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<DeclareReconciliationStandardExcelDTO> errorList = excelListenerUtil.getErrorList();
        //导入数据保存
        List<TmsB2cDeclareReconciliationDetailDTO.ViewDTO> successImortList = handleImportStandardData(successList, errorList,id);

        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "报关对账单错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, DeclareReconciliationStandardExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        fillImportList(successImortList);
        importDTO.setSuccessList(successImortList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }


    /**
     * @description: 标准版导入数据处理
     * @author Will
     * @date: 2024/3/27 12:07
     * @param successList
     * @param errorList
     * @return List<AddDTO>
     */
    private List<TmsB2cDeclareReconciliationDetailDTO.ViewDTO> handleImportStandardData (List<DeclareReconciliationStandardExcelDTO> successList,
                                                                                        List<DeclareReconciliationStandardExcelDTO> errorList,String id) {
        if (CollectionUtils.isEmpty(successList)) {
            return Collections.EMPTY_LIST;
        }
        List<TmsB2cDeclareReconciliationDetailDTO.ViewDTO> resultList = new ArrayList<>();

        //对账单信息
        TmsB2cDeclareReconciliationEntity oldMainEntity = tmsB2cDeclareReconciliationService.getById(id);
        if (ObjectUtil.isEmpty(oldMainEntity)) {
            throw new ServiceException(ApiError.ERROR_DECLARE_RECONCILIATION_NOT_EXIST);
        }
        //对账单明细
        List<TmsB2cDeclareReconciliationDetailEntity> oldDetailList = this.listMainIdList(Arrays.asList(oldMainEntity.getId()));

        Map<String, List<TmsCostDetailEntity>> mainIdListMap = new HashMap<>();
        if(CollUtil.isNotEmpty(oldDetailList)) {
        	List<TmsCostDetailEntity> listByMainIdList = tmsCostDetailService.listByMainIdList(oldDetailList.stream().map(TmsB2cDeclareReconciliationDetailEntity::getId).collect(Collectors.toList()));
        	mainIdListMap = listByMainIdList.stream().collect(Collectors.groupingBy(TmsCostDetailEntity::getMainId));
        }
        
        //配置信息
        List<CfgReconciliationFieldDTO.ErpFieldDropDownDTO> erpFieldList = cfgReconciliationFieldService.erpFieldList(Arrays.asList(CfgReconciliationTypeEnum.B2C_DECLARE.getCode()));

        Map<String, List<DeclareReconciliationStandardExcelDTO>> map = successList.stream().collect(Collectors.groupingBy(DeclareReconciliationStandardExcelDTO::getSoCode));
        for ( Map.Entry<String, List<DeclareReconciliationStandardExcelDTO>> entry : map.entrySet()) {
            List<DeclareReconciliationStandardExcelDTO> value = entry.getValue();
            DeclareReconciliationStandardExcelDTO reconciliationStandardExcelDTO = value.get(0);
            //对账单明细信息
            TmsB2cDeclareReconciliationDetailEntity detailEntity = oldDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSoCode(), reconciliationStandardExcelDTO.getSoCode())).findFirst().orElse(null);

            //需要更新的费用
            List<TmsCostDetailDTO.UpdateDTO> updateList = new ArrayList<>();
            JSONObject hasData = new JSONObject();
            for (DeclareReconciliationStandardExcelDTO excelDTO : value) {
                List<String> errorMsgList = new ArrayList<>();

                if( ObjectUtil.isEmpty(detailEntity))  {
                    errorMsgList.add("对账单中不存在该对账明细");
                } else {
                    if (!CharSequenceUtil.equals(detailEntity.getLogisticsSupplierId(),oldMainEntity.getLogisticsSupplierId())) {
                        errorMsgList.add("物流商不一致不支持导入");
                    }
                    if (!CharSequenceUtil.equals(detailEntity.getStatus(),TmsB2cDeclareReconciliationStatusEnum.TO_BE_CONFIRM.getCode())) {
                        errorMsgList.add("仅待确认支持导入更新");
                    }
                }
                if (ObjectUtil.isEmpty(oldMainEntity)) {
                    errorMsgList.add("未找到销售订单报关对账单信息");
                }
                CfgReconciliationFieldDTO.ErpFieldDropDownDTO erpFieldDropDownDTO = erpFieldList.stream().filter(obj -> CharSequenceUtil.equals(obj.getErpFieldName(), excelDTO.getCostName())).findFirst().orElse(null);
                if (CharSequenceUtil.isNotBlank(excelDTO.getCostName()) && ObjectUtil.isEmpty(erpFieldDropDownDTO)) {
                    errorMsgList.add("字段配置中未找到费用项");
                }
                //校验后面数据是否存在重复的
                if (ObjectUtil.isNotEmpty(erpFieldDropDownDTO)) {
                    if (ObjectUtil.isNotEmpty(hasData.get(erpFieldDropDownDTO.getSourceId()))) {
                        errorMsgList.add("费用已存在，请勿重复导入");
                    }
                    hasData.set(erpFieldDropDownDTO.getSourceId(), excelDTO.getCostName());
                }

                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(excelDTO);
                    continue;
                }
                //存在费用并且数量大于0
                if (ObjectUtil.isNotEmpty(erpFieldDropDownDTO) && CharSequenceUtil.isNotBlank(excelDTO.getCostValue())) {
                    TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
                    updateDTO.setCostValue(CharSequenceUtil.isBlank(excelDTO.getCostValue()) ? BigDecimal.ZERO : MathUtil.valueOf(excelDTO.getCostValue()));
                    updateDTO.setType(LogisticsBillCostTypeEnum.ACTUAL.getCode());
                    updateDTO.setCfgCostId(erpFieldDropDownDTO.getSourceId());
                    String currency = excelDTO.getCurrency();
                    if(StringUtils.isBlank(currency)) {
                    	currency = "CNY";
                    }
					updateDTO.setCurrency(currency);
					updateDTO.setDictCostCategory(erpFieldDropDownDTO.getSourceCodeValue());
                    updateList.add(updateDTO);
                }
            }
            
            List<TmsCostDetailEntity> validateList = BeanMapperUtils.copyList(TmsCostDetailEntity.class, updateList);
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
            		List<UpdateDTO> removeList = updateList.stream().filter(u -> u.getDictCostCategory().equals(split[0]) && u.getType().equals(split[1])).collect(Collectors.toList());
            		for(UpdateDTO remove : removeList) {
            			String costName = erpFieldList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceId(), remove.getCfgCostId())).findFirst().orElse(null).getErpFieldName();
            			Set<String> set = costIdTypeListMap.get(costName);
            			if(CollUtil.isEmpty(set)) {
            				set = new HashSet<>();
            			}
            			set.add(AllocationFeeTypeEnum.getName(split[0]) + "-" + LogisticsBillCostTypeEnum.getName(split[1]) + "分类下所有一级费用币种必须一致");
            			costIdTypeListMap.put(costName, set);
            		}
            		updateList.removeIf(u -> u.getDictCostCategory().equals(split[0]) && u.getType().equals(split[1]));
            	}
            	if(!costIdTypeListMap.isEmpty()) {
            		for(DeclareReconciliationStandardExcelDTO excelDTO : value) {
            			Set<String> set = costIdTypeListMap.get(excelDTO.getCostName());
						if(CollUtil.isNotEmpty(set)) {
							excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(new ArrayList<>(set)));
		                    errorList.add(excelDTO);
            			}
            		}
            	}
            }
            
            //为空则无需返回
            if (ObjectUtil.isEmpty(detailEntity)) {
                return resultList;
            }
            TmsB2cDeclareReconciliationDetailDTO.ViewDTO  viewDTO= BeanMapperUtils.map(TmsB2cDeclareReconciliationDetailDTO.ViewDTO.class,detailEntity);
            viewDTO.setActualWeight(CharSequenceUtil.isBlank(reconciliationStandardExcelDTO.getActualWeight()) ? detailEntity.getActualWeight() : MathUtil.valueOf(reconciliationStandardExcelDTO.getActualWeight()));
            viewDTO.setActualBillingWeight(CharSequenceUtil.isBlank(reconciliationStandardExcelDTO.getActualBillingWeight()) ? detailEntity.getActualBillingWeight() : MathUtil.valueOf(reconciliationStandardExcelDTO.getActualBillingWeight()));
            viewDTO.setActualWeightUnit(CharSequenceUtil.isBlank(reconciliationStandardExcelDTO.getActualWeightUnit()) ? UnitEnum.WeightUnitEnum.KG.getCode() : reconciliationStandardExcelDTO.getActualWeightUnit());
            viewDTO.setUpdateList(updateList);
            //费用处理
            handleCost(updateList,viewDTO,detailEntity.getId());
            resultList.add(viewDTO);
        }
        return resultList;
    }

    /**
     * @description: 字段配置导入
     * @author Will
     * @date: 2024/3/27 17:05
     * @param excelFile
     * @return ImportDTO
     */
    private TmsB2cDeclareReconciliationDetailDTO.ImportDTO importConfigFile(MultipartFile excelFile,String id) {
        TmsB2cDeclareReconciliationDetailDTO.ImportDTO importDTO = new TmsB2cDeclareReconciliationDetailDTO.ImportDTO();

        DeclareReconciliationConfigExcelListener excelListenerUtil = new DeclareReconciliationConfigExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<JSONObject> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<JSONObject> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<JSONObject> errorList = excelListenerUtil.getErrorList();
        //表头
        List<String> headList = excelListenerUtil.getHeadList();
        //导入数据保存
        List<TmsB2cDeclareReconciliationDetailDTO.ViewDTO> successImortList = handleImportConfigData(successList, errorList,headList,id);

        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "报关对账单错误数据.xlsx";
            List<List<Object>> exportList = errorList.stream().map(obj -> obj.entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList())).collect(Collectors.toList());
            File file = ExcelUtil.exportFile(fileName, "error", exportList, headList);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        fillImportList(successImortList);
        importDTO.setSuccessList(successImortList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }

    /**
     * @description: 配置导入
     * @author Will
     * @date: 2024/3/28 15:27
     * @param successList
     * @param errorList
     * @return List<ViewDTO>
     */
    private List<TmsB2cDeclareReconciliationDetailDTO.ViewDTO> handleImportConfigData (List<JSONObject> successList,List<JSONObject> errorList
            ,List<String> headList ,String id) {
        if (CollectionUtils.isEmpty(successList)) {
            return Collections.EMPTY_LIST;
        }

        //对账单信息
        TmsB2cDeclareReconciliationEntity oldMainEntity = tmsB2cDeclareReconciliationService.getById(id);
        if (ObjectUtil.isEmpty(oldMainEntity)) {
            throw new ServiceException(ApiError.ERROR_DECLARE_RECONCILIATION_NOT_EXIST);
        }

        //中转物流供应商查询
        TransferLogisticsSupplierEntity transferLogisticsSupplierEntity = transferLogisticsSupplierService.getById(oldMainEntity.getLogisticsSupplierId());
        if (ObjectUtil.isEmpty(transferLogisticsSupplierEntity)) {
            throw new ServiceException("对账单中转物流供应商未找到");
        }

        //字段配置信息
        List<CfgReconciliationFieldDTO.ErpFieldViewDTO> erpFieldList = cfgReconciliationFieldService.getByReconciliationType(CfgReconciliationTypeEnum.B2C_DECLARE.getCode());
        //字段配置信息
        List<CfgReconciliationFieldDTO.ErpFieldViewDTO> erpFieldResultList = erpFieldList.stream().filter(obj -> CharSequenceUtil.equals(obj.getThirdCode(), transferLogisticsSupplierEntity.getSupplierId())).collect(Collectors.toList());
        if (ObjectUtil.isEmpty(erpFieldResultList)) {
            throw new ServiceException("物流商未设置字段配置，请配置后导入");
        }
        Map<String, CfgReconciliationFieldDTO.ErpFieldViewDTO> map = erpFieldResultList.stream().collect(Collectors.toMap(CfgReconciliationFieldDTO.ErpFieldViewDTO::getThirdFieldName, Function.identity()));

        Integer soIndex = MathUtil.ZERO;
        for (int i = 0 ; i < headList.size();i++) {
            CfgReconciliationFieldDTO.ErpFieldViewDTO erpFieldViewDTO = map.get(headList.get(i));
            if (ObjectUtil.isEmpty(erpFieldViewDTO) || !CharSequenceUtil.equals(erpFieldViewDTO.getErpFieldCode(),"soCode")) {
                continue;
            }
            //销售订单单号下标
            soIndex = Integer.valueOf(i);
        }
        //明细数据
        List<TmsB2cDeclareReconciliationDetailEntity> oldDetailList = this.listMainIdList(Arrays.asList(oldMainEntity.getId()));
        Map<String, List<TmsCostDetailEntity>> mainIdListMap = new HashMap<>();
        if(CollUtil.isNotEmpty(oldDetailList)) {
        	List<TmsCostDetailEntity> listByMainIdList = tmsCostDetailService.listByMainIdList(oldDetailList.stream().map(TmsB2cDeclareReconciliationDetailEntity::getId).collect(Collectors.toList()));
        	mainIdListMap = listByMainIdList.stream().collect(Collectors.groupingBy(TmsCostDetailEntity::getMainId));
        }
        
        List<TmsB2cDeclareReconciliationDetailDTO.ViewDTO> resultList = new ArrayList<>();
        for (JSONObject jsonObject :  successList) {
            //主数据
            JSONObject successJson = new JSONObject();
            //费用数据
            List<TmsCostDetailDTO.UpdateDTO> updateList = new ArrayList<>();
            List<String> errorMsgList = new ArrayList<>();
            if (ObjectUtil.isEmpty(map)) {
                errorMsgList.add("未发现字段配置");
            } else {
                JSONObject hasData = new JSONObject();
                for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                    //字段名称
                    String field = headList.get(Integer.valueOf(entry.getKey()));
                    CfgReconciliationFieldDTO.ErpFieldViewDTO erpFieldViewDTO = map.get(field);
                    if (ObjectUtil.isEmpty(erpFieldViewDTO) || CharSequenceUtil.isBlank(erpFieldViewDTO.getErpFieldName())) {
                        errorMsgList.add(CharSequenceUtil.format("未发现该字段配置项【{}】",field));
                        continue;
                    }
                    //校验后面数据是否存在重复的
                    if (ObjectUtil.isNotEmpty(erpFieldViewDTO)) {
                        if (ObjectUtil.isNotEmpty(hasData.get(erpFieldViewDTO.getSourceId()))) {
                            errorMsgList.add("费用已存在，请勿重复导入");
                        }
                        hasData.set(erpFieldViewDTO.getSourceId(), erpFieldViewDTO.getErpFieldName());
                    }

                    if (CharSequenceUtil.isNotBlank(erpFieldViewDTO.getErpFieldCode())) {
                        successJson.set(erpFieldViewDTO.getErpFieldCode(),String.valueOf(entry.getValue()));
                    }
                    if (CharSequenceUtil.isBlank(erpFieldViewDTO.getErpFieldCode()) && ObjectUtil.isNotEmpty(entry.getValue())) {
                        TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
                        updateDTO.setCfgCostId(erpFieldViewDTO.getSourceId());
                        updateDTO.setCostValue(MathUtil.valueOf(entry.getValue()));
                        updateDTO.setType(LogisticsBillCostTypeEnum.ACTUAL.getCode());
                        updateDTO.setDictCostCategory(erpFieldViewDTO.getDictCostCategory());
                        updateList.add(updateDTO);
                    }
                }
            }
            DeclareReconciliationStandardExcelDTO excelDTO = BeanUtil.toBean(successJson, DeclareReconciliationStandardExcelDTO.class);
            //基础验证
            List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
            if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(msgList)) {
                errorMsgList.addAll(msgList);
            }
            //对账单明细信息
            Integer finalSoIndex = soIndex;
            TmsB2cDeclareReconciliationDetailEntity detailEntity = oldDetailList.stream().filter(obj -> ObjUtil.isNotEmpty(jsonObject.get(finalSoIndex.toString())))
                    .filter(obj -> CharSequenceUtil.equals(obj.getSoCode(), jsonObject.get(finalSoIndex.toString()).toString()))
                    .findFirst().orElse(null);
            if( ObjectUtil.isEmpty(detailEntity))  {
                errorMsgList.add("对账单中不存在该对账明细");
            } else {
                if (!CharSequenceUtil.equals(detailEntity.getLogisticsSupplierId(),oldMainEntity.getLogisticsSupplierId())) {
                    errorMsgList.add("物流商不一致不支持导入");
                }
                if (!CharSequenceUtil.equals(detailEntity.getStatus(),TmsB2cDeclareReconciliationStatusEnum.TO_BE_CONFIRM.getCode())) {
                    errorMsgList.add("仅待确认支持导入更新");
                }
            }
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                jsonObject.set("错误信息",FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(jsonObject);
                continue;
            }
            
            TmsB2cDeclareReconciliationDetailDTO.ViewDTO  viewDTO= BeanMapperUtils.map(TmsB2cDeclareReconciliationDetailDTO.ViewDTO.class,detailEntity);
            viewDTO.setActualWeight(CharSequenceUtil.isBlank(excelDTO.getActualWeight()) ? detailEntity.getActualWeight() : MathUtil.valueOf(excelDTO.getActualWeight()));
            viewDTO.setActualBillingWeight(CharSequenceUtil.isBlank(excelDTO.getActualBillingWeight()) ? detailEntity.getActualBillingWeight() : MathUtil.valueOf(excelDTO.getActualBillingWeight()));
            viewDTO.setActualWeightUnit(CharSequenceUtil.isBlank(excelDTO.getActualWeightUnit()) ? UnitEnum.WeightUnitEnum.KG.getCode() : excelDTO.getActualWeightUnit());
            
            updateList.forEach(u -> u.setCurrency(excelDTO.getCurrency()));
            List<TmsCostDetailEntity> validateList = BeanMapperUtils.copyList(TmsCostDetailEntity.class, updateList);
            List<TmsCostDetailEntity> tmsCostDetailEntityList = mainIdListMap.get(detailEntity.getId());
            if(CollUtil.isNotEmpty(tmsCostDetailEntityList)) {
            	List<String> cfgCostIds = validateList.stream().map(TmsCostDetailEntity::getCfgCostId).collect(Collectors.toList());
            	validateList.addAll(tmsCostDetailEntityList.stream().filter(t -> !cfgCostIds.contains(t.getCfgCostId())).collect(Collectors.toList()));
            }
            Set<String> validateCategoryCurrency = tmsCostDetailService.validateCategoryCurrency(validateList);
            if(!validateCategoryCurrency.isEmpty()) {
            	Map<String, String> costIdTypeListMap = new HashMap<>();
            	for(String validateCategory : validateCategoryCurrency) {
            		String[] split = validateCategory.split("_");
            		List<UpdateDTO> removeList = updateList.stream().filter(u -> u.getDictCostCategory().equals(split[0]) && u.getType().equals(split[1])).collect(Collectors.toList());
            		for(UpdateDTO remove : removeList) {
            			String costName = erpFieldList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceId(), remove.getCfgCostId())).findFirst().orElse(null).getErpFieldName();
            			costIdTypeListMap.put(costName, AllocationFeeTypeEnum.getName(split[0]) + "-" + LogisticsBillCostTypeEnum.getName(split[1]) + "分类下所有一级费用币种必须一致");
            		}
            		updateList.removeIf(u -> u.getDictCostCategory().equals(split[0]) && u.getType().equals(split[1]));
            	}
            	if(!costIdTypeListMap.isEmpty()) {
            		jsonObject.set("错误信息",FieldValidUtil.getMsgSort(new ArrayList<>(costIdTypeListMap.values())));
                    errorList.add(jsonObject);
                    continue;
            	}
            }
            
            viewDTO.setUpdateList(updateList);

            //费用处理
            handleCost(updateList,viewDTO,detailEntity.getId());
            resultList.add(viewDTO);
        }
        return resultList;
    }

    /**
     * @description: 导入数据处理
     * @author Will
     * @date: 2024/3/29 16:04
     * @param list
     */
    private void fillImportList(List<TmsB2cDeclareReconciliationDetailDTO.ViewDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //店铺信息
        List<String> shopIdList = list.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getShopId())).map(TmsB2cDeclareReconciliationDetailDTO.ViewDTO::getShopId).collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = shopInfoFeign.listShopInfoByIds(shopIdList);
        //国家信息
        List<String> countryIdList = list.stream().map(TmsB2cDeclareReconciliationDetailDTO.ViewDTO::getCountry).collect(Collectors.toList());
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(countryIdList);
        for (TmsB2cDeclareReconciliationDetailDTO.ViewDTO listDTO :list) {
            //店铺名称
            String shopName = shopInfoList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), listDTO.getShopId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setShopName(shopName);
            //国家名称
            String countryName = countryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), listDTO.getCountry()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
            listDTO.setCountryName(countryName);
            listDTO.setStatusName(TmsB2cDeclareReconciliationStatusEnum.getName(listDTO.getStatus()));
        }
    }

    /**
     * @description: 根据销售订单编码集合查询
     * @author Will
     * @date: 2024/3/27 15:53
     * @param soCodeList 
     */
    private List<TmsB2cDeclareReconciliationDetailEntity> listBySoCodeList (List<String> soCodeList) {
        if (CollectionUtils.isEmpty(soCodeList)) {
            return Collections.EMPTY_LIST;
        }
       return lambdaQuery().in(TmsB2cDeclareReconciliationDetailEntity::getSoCode,soCodeList).list();
    }

    /**
     * @description: 保存校验
     * @author Will
     * @date: 2024/3/26 16:57
     * @param reconciliationDetailList
     */
    private void checkAddData (List<TmsB2cDeclareReconciliationDetailEntity> reconciliationDetailList) {
        if (CollectionUtils.isEmpty(reconciliationDetailList)) {
            return;
        }
        List<String> sourceDetailIdList = reconciliationDetailList.stream().map(TmsB2cDeclareReconciliationDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<TmsB2cDeclareReconciliationDetailEntity> oldDetailList = this.listDetailBySourceDetailIdList(sourceDetailIdList);

        if (CollectionUtils.isNotEmpty(oldDetailList)) {
            String codes = oldDetailList.stream().map(TmsB2cDeclareReconciliationDetailEntity::getSourceCode).collect(Collectors.joining(","));
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_HAS_GENERATE,codes);
        }
    }

    /**
     * @description: 新增数据处理
     * @author Will
     * @date: 2024/3/26 17:05
     * @param reconciliationDetailList
     * @return List<TmsB2cDeclareReconciliationDetailEntity>
     */
    private  List<TmsB2cDeclareReconciliationDetailEntity> handleAddData (List<TmsB2cDeclareReconciliationDetailEntity> reconciliationDetailList) {
        //可新增数据
        List<TmsB2cDeclareReconciliationDetailEntity> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(reconciliationDetailList)) {
            return resultList;
        }
        //中专报关明细数据
        List<String> sourceDetailIdList = reconciliationDetailList.stream().map(TmsB2cDeclareReconciliationDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<TransferDeclareDetailEntity> transferDeclareDetailList = transferDeclareDetailService.listByIds(sourceDetailIdList);

        //中专报关数据
        List<String> declareIdList = transferDeclareDetailList.stream().map(TransferDeclareDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<TransferDeclareEntity> transferDeclareList = transferDeclareService.listByIds(declareIdList);

        //B2c销售订单
        List<String> soIdList = transferDeclareDetailList.stream().map(TransferDeclareDetailEntity::getSoId).collect(Collectors.toList());
        List<SoB2cEntity> soB2cList = soB2cFeign.listByIds(soIdList);

        //b2c销售订单明细
        List<SoB2cDetailEntity> soB2cDetailList = soB2cFeign.listDetailByMainIds(soIdList);

        //b2c收货订单
        List<SoB2cReceiverEntity> soB2cReceiverList = soB2cFeign.listSoB2cReceiverByMainIdList(soIdList);

        //b2c销售订单物流信息
        List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cFeign.listSoB2cLogisticsByMainIdList(soIdList);

        for (TmsB2cDeclareReconciliationDetailEntity detailEntity : reconciliationDetailList) {
            //中转报关明细
            TransferDeclareDetailEntity transferDeclareDetail = transferDeclareDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), detailEntity.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(transferDeclareDetail)) {
                throw new ServiceException("未找到中专报关明细");
            }
            //中专报关主表信息
            TransferDeclareEntity transferDeclareEntity = transferDeclareList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), transferDeclareDetail.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(transferDeclareEntity)) {
                throw new ServiceException("未找到中专报关信息");
            }
            detailEntity.setSourceId(transferDeclareEntity.getId());
            detailEntity.setSourceCode(transferDeclareEntity.getCode());
            detailEntity.setDate(transferDeclareEntity.getInstockForecastDate());
            //销售订单
            SoB2cEntity soB2cEntity = soB2cList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), transferDeclareDetail.getSoId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soB2cEntity)) {
                throw new ServiceException("未找到B2c销售订单");
            }
            detailEntity.setSoId(transferDeclareDetail.getSoId());
            detailEntity.setSoCode(transferDeclareDetail.getSoCode());
            detailEntity.setShopId(soB2cEntity.getShopId());
            detailEntity.setLogisticsSupplierId(transferDeclareEntity.getTransferLogisticsSupplierId());
            detailEntity.setLogisticsSupplierName(transferDeclareEntity.getTransferLogisticsSupplierName());
            detailEntity.setLogisticsChannelId(transferDeclareEntity.getTransferChannelId());
            detailEntity.setLogisticsChannelName(transferDeclareEntity.getTransferChannelName());

            //销售订单明细
            List<SoB2cDetailEntity> detailList = soB2cDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(), soB2cEntity.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(detailList)) {
                throw new ServiceException("未找到B2c销售订单明细");
            }
            long skuCount = detailList.stream().map(SoB2cDetailEntity::getSkuId).distinct().count();
            detailEntity.setQty(Math.toIntExact(skuCount));

            SoB2cReceiverEntity soB2cReceiverEntity = soB2cReceiverList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(), soB2cEntity.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soB2cReceiverEntity)) {
                throw new ServiceException("未找到B2c销售订单买家信息");
            }
            detailEntity.setCountry(soB2cReceiverEntity.getCountry());
            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(), soB2cEntity.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soB2cLogisticsEntity)) {
                throw new ServiceException("未找到B2c销售订单物流信息");
            }
            detailEntity.setEstimateWeight(soB2cLogisticsEntity.getWeight());
            detailEntity.setEstimateWeightUnit(UnitEnum.WeightUnitEnum.G.getCode());
            resultList.add(detailEntity);
        }
        return resultList;
    }

    /**
     * @description: 根据来源明细id集合查询
     * @author Will
     * @date: 2024/3/26 16:56
     * @param sourceDetailIdList
     * @return List<TmsB2cDeclareReconciliationDetailEntity>
     */
    private List<TmsB2cDeclareReconciliationDetailEntity> listDetailBySourceDetailIdList (List<String> sourceDetailIdList) {
        if (CollectionUtils.isEmpty(sourceDetailIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(TmsB2cDeclareReconciliationDetailEntity::getSourceDetailId,sourceDetailIdList)
                .list();
    }

    /**
     * @description: 分页查询数据处理
     * @author Will
     * @date: 2024/3/26 10:02
     * @param list
     */
    private void fillList (List<TmsB2cDeclareReconciliationDetailDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //店铺信息
        List<String> shopIdList = list.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getShopId())).map(TmsB2cDeclareReconciliationDetailDTO.ListDTO::getShopId).collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = CollectionUtils.isEmpty(shopIdList) ? new ArrayList<>() : shopInfoFeign.listShopInfoByIds(shopIdList);
        //国家信息
        List<String> countryIdList = list.stream().map(TmsB2cDeclareReconciliationDetailDTO.ListDTO::getCountry).collect(Collectors.toList());
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(countryIdList);
        Map<String, String> idSymbolMap = FeignQuery.list(DictCurrencyEntity.class).stream().collect(Collectors.toMap(DictCurrencyEntity::getId, DictCurrencyEntity::getSymbol));
        for (TmsB2cDeclareReconciliationDetailDTO.ListDTO listDTO :list) {
            //店铺名称
            String shopName = shopInfoList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), listDTO.getShopId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setShopName(shopName);
            //国家名称
            String countryName = countryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), listDTO.getCountry()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
            listDTO.setCountryName(countryName);
            //入库预报状态
            listDTO.setInstockForecastStatus(InstockForecastStatusEnum.UPLOAD_SUCCESS.getCode());
            listDTO.setInstockForecastStatusName(InstockForecastStatusEnum.UPLOAD_SUCCESS.getName());
            listDTO.setStatusName(TmsB2cDeclareReconciliationStatusEnum.getName(listDTO.getStatus()));
            
            listDTO.setActualShippingCostStr(idSymbolMap.get(listDTO.getActualShippingCurrency()) + listDTO.getActualShippingCost());
            listDTO.setActualDeclareCostStr(idSymbolMap.get(listDTO.getActualDeclareCurrency()) + listDTO.getActualDeclareCost());
            listDTO.setActualOtherCostStr(idSymbolMap.get(listDTO.getActualOtherCurrency()) + listDTO.getActualOtherCost());
        }
    }

    private void handleUpdateData (List<TmsB2cDeclareReconciliationDetailEntity> list,String mainId) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //已存在对应明细
        List<String> detailIdList = list.stream().map(TmsB2cDeclareReconciliationDetailEntity::getId).collect(Collectors.toList());
        List<TmsB2cDeclareReconciliationDetailEntity> declareReconciliationDetailList = this.listByIds(detailIdList);
        //对账单
        TmsB2cDeclareReconciliationEntity declareReconciliationEntity = tmsB2cDeclareReconciliationService.getById(mainId);
        if (ObjectUtils.isEmpty(declareReconciliationEntity)) {
            throw new ServiceException(ApiError.ERROR_DECLARE_RECONCILIATION_NOT_EXIST);
        }
        //新增不需要添加新增SKU的日志
        List<TmsB2cDeclareReconciliationDetailEntity> addList = list.stream().filter(obj -> CharSequenceUtil.isBlank(obj.getMainId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList)) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSoCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条销售订单【%s】", ModuleTypeEnum.PO_RECONCILIATION.getCode(), addPairList, "编辑操作");
        }
        for (TmsB2cDeclareReconciliationDetailEntity entity : list) {

            //添加日志
            TmsB2cDeclareReconciliationDetailEntity old = declareReconciliationDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), entity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(old)) {
                throw new ServiceException(ApiError.ERROR_DECLARE_RECONCILIATION_DETAIL_NOT_EXIST);
            }
            //供应商、结算组织验证
            if (!CharSequenceUtil.equals(declareReconciliationEntity.getLogisticsSupplierId(),old.getLogisticsSupplierId())) {
                throw new ServiceException(ApiError.ERROR_DECLARE_RECONCILIATION_ADD_DETAIL,declareReconciliationEntity.getCode(),declareReconciliationEntity.getLogisticsSupplierName());
            }

            entity.setMainId(mainId);
            //操作日志
            operateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode(),mainId,"",String.format("【%s】",old.getSoCode()));
        }
    }

    /**
     * @description: 更新报关明细实际费用
     * @author Will
     * @date: 2024/4/15 10:21
     * @param list
     */
    private void updateDeclareReconciliationDetailCost (List<TmsB2cDeclareReconciliationDetailEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> mainIdList = list.stream().map(TmsB2cDeclareReconciliationDetailEntity::getId).collect(Collectors.toList());
        List<TmsCostDetailDTO.CostViewDTO> tmsCostDetailList = tmsCostDetailService.listCostByMainIdList(mainIdList);
        if (CollectionUtils.isEmpty(tmsCostDetailList)) {
            return;
        }
        for (TmsB2cDeclareReconciliationDetailEntity entity : list) {
            List<CostViewDTO> costViewDTOList = tmsCostDetailList.stream().filter(obj -> CharSequenceUtil.equals(entity.getId(), obj.getMainId()) && CharSequenceUtil.equals(obj.getDictCostCategory(),DictCostCategoryEnum.SHIPPING_COST.getCode())).collect(Collectors.toList());
			if(CollUtil.isNotEmpty(costViewDTOList)) {
				 BigDecimal shippingCost = costViewDTOList.stream().map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
	             entity.setActualShippingCost(shippingCost);
	             entity.setActualShippingCurrency(costViewDTOList.get(0).getCurrency());
			}else {
				 entity.setActualShippingCost(BigDecimal.ZERO);
	             entity.setActualShippingCurrency("CNY");
			}
			costViewDTOList = tmsCostDetailList.stream().filter(obj -> CharSequenceUtil.equals(entity.getId(), obj.getMainId()) && CharSequenceUtil.equals(obj.getDictCostCategory(),DictCostCategoryEnum.DECLARE_COST.getCode())).collect(Collectors.toList());
			if(CollUtil.isNotEmpty(costViewDTOList)) {
				 BigDecimal declareCost = costViewDTOList.stream().map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
		         entity.setActualDeclareCost(declareCost);
		         entity.setActualDeclareCurrency(costViewDTOList.get(0).getCurrency());
			}else {
				 entity.setActualDeclareCost(BigDecimal.ZERO);
	             entity.setActualDeclareCurrency("CNY");
			}
			costViewDTOList = tmsCostDetailList.stream().filter(obj -> CharSequenceUtil.equals(entity.getId(), obj.getMainId()) && CharSequenceUtil.equals(obj.getDictCostCategory(),DictCostCategoryEnum.OTHER_COST.getCode())).collect(Collectors.toList());
			if(CollUtil.isNotEmpty(costViewDTOList)) {
				 BigDecimal otherCost = costViewDTOList.stream().map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
		         entity.setActualOtherCost(otherCost);
		         entity.setActualOtherCurrency(costViewDTOList.get(0).getCurrency());
			}else {
				 entity.setActualOtherCost(BigDecimal.ZERO);
	             entity.setActualOtherCurrency("CNY");
			}
        }
        this.updateBatchById(list);
    }

    /**
     * @description: 费用处理
     * @author Will
     * @date: 2024/4/15 14:27
     * @param updateList
     * @param viewDTO
     */
    private void handleCost (List<TmsCostDetailDTO.UpdateDTO> updateList,TmsB2cDeclareReconciliationDetailDTO.ViewDTO  viewDTO,String detailId) {
        //需要查询配置的id
        List<String> totalCfgCostIdList = new ArrayList<>();
        //传入费用设置
        List<String> cfgCostIdList = updateList.stream().map(TmsCostDetailDTO.UpdateDTO::getCfgCostId).distinct().collect(Collectors.toList());
        totalCfgCostIdList.addAll(cfgCostIdList);
        //原先费用设置
        List<TmsCostDetailDTO.CostViewDTO> costViewList = tmsCostDetailService.listCostByMainIdList(Arrays.asList(detailId));
        List<String> oldCfgIdList = costViewList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getId()))
                .map(TmsCostDetailDTO.CostViewDTO::getCfgCostId)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(oldCfgIdList)) {
            totalCfgCostIdList.addAll(oldCfgIdList);
        }
        List<TmsCfgCostEntity> tmsCfgCostList = tmsCfgCostService.listByIds(totalCfgCostIdList);
        if (CollectionUtils.isEmpty(tmsCfgCostList)) {
            throw new ServiceException("未发现费用字段配置信息");
        }

        //实际物流运费
         BigDecimal actualShippingCost = BigDecimal.ZERO;
        //实际报关费
         BigDecimal actualDeclareCost = BigDecimal.ZERO;
        //实际其他费
         BigDecimal actualOtherCost = BigDecimal.ZERO;

         for (DictCostCategoryEnum dictCostCategoryEnum :  DictCostCategoryEnum.values()) {
             //费用类型
             List<String> cfgIdList = tmsCfgCostList.stream().filter(obj -> CharSequenceUtil.equals(dictCostCategoryEnum.getCode(), obj.getDictCostCategory()))
                     .map(TmsCfgCostEntity::getId).collect(Collectors.toList());

             //导入的费用
             BigDecimal importCost = BigDecimal.ZERO;
             String currency = "CNY";
             List<UpdateDTO> importList = updateList.stream().filter(obj -> cfgIdList.contains(obj.getCfgCostId())).collect(Collectors.toList());
             if(CollUtil.isNotEmpty(importList)) {
            	 importCost = importList.stream().map(TmsCostDetailDTO.UpdateDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            	 currency = importList.get(0).getCurrency();
             }

             //需要累加到界面显示的原费用
             BigDecimal oldCost = costViewList.stream().filter(obj -> !cfgCostIdList.contains(obj.getCfgCostId()) && CharSequenceUtil.equals(obj.getDictCostCategory(), dictCostCategoryEnum.getCode()))
                     .map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
             if(StringUtils.isBlank(currency) && CollUtil.isNotEmpty(costViewList)) {
            	 currency = costViewList.get(0).getCurrency();
             }

             //实际物流运费
             if(CharSequenceUtil.equals(dictCostCategoryEnum.getCode(),DictCostCategoryEnum.SHIPPING_COST.getCode())) {
            	 actualShippingCost = MathUtil.add(actualShippingCost.add(oldCost),importCost);
            	 viewDTO.setActualShippingCurrency(currency);
             }
             //实际报关费
             if(CharSequenceUtil.equals(dictCostCategoryEnum.getCode(),DictCostCategoryEnum.DECLARE_COST.getCode())) {
            	 actualDeclareCost = MathUtil.add(actualDeclareCost.add(oldCost),importCost);
            	 viewDTO.setActualDeclareCurrency(currency);
             }
             //实际其他费
             if(CharSequenceUtil.equals(dictCostCategoryEnum.getCode(),DictCostCategoryEnum.OTHER_COST.getCode())) {
            	 actualOtherCost = MathUtil.add(actualOtherCost.add(oldCost),importCost);
            	 viewDTO.setActualOtherCurrency(currency);
             }
             
         }
        viewDTO.setActualShippingCost(actualShippingCost);
        viewDTO.setActualDeclareCost(actualDeclareCost);
        viewDTO.setActualOtherCost(actualOtherCost);
        Map<String, String> idSymbolMap = FeignQuery.getByIds(DictCurrencyEntity.class, viewDTO.getActualShippingCurrency() , viewDTO.getActualDeclareCurrency() , viewDTO.getActualOtherCurrency())
        	.stream().collect(Collectors.toMap(DictCurrencyEntity::getId, DictCurrencyEntity::getSymbol));
        viewDTO.setActualShippingCurrencySymbol(idSymbolMap.get(viewDTO.getActualShippingCurrency()));
        viewDTO.setActualDeclareCurrencySymbol(idSymbolMap.get(viewDTO.getActualDeclareCurrency()));
        viewDTO.setActualOtherCurrencySymbol(idSymbolMap.get(viewDTO.getActualOtherCurrency()));
    }


	@Override
	public List<TmsB2cDeclareReconciliationDetailEntity> listAutoGenerateCost(LocalDate startDate, LocalDate endDate) {
		return this.getBaseMapper().listAutoGenerateCost(startDate, endDate);
	}

}
