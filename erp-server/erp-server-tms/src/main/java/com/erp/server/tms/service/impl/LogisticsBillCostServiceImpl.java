package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BaseResultDTO.AddDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO.AddDataDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO.ConfirmAddDataDTO;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO.DetailDTO;
import com.erp.model.tms.dto.excel.LogisticsBillCostExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.tms.listener.LogisticsBillCostExcelListener;
import com.erp.server.tms.mapper.LogisticsBillCostMapper;
import com.erp.server.tms.query.LogisticsBillCostQueryHandler;
import com.erp.server.tms.query.LogisticsLastMileCostQueryHandler;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_BILL_COST;

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
        LogisticsBillCostEntity old = super.getById(updateDTO.getId());
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
            throw new ServiceException("自发货费用保存失败");
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
        
        //状态变更
        lambdaUpdate().eq(LogisticsBillCostEntity::getId, id)
        		.set(LogisticsBillCostEntity::getReconciliationStatus, reconciliationStatus)
                .set(confirmFlag,LogisticsBillCostEntity::getConfirmTime, confirmTime)
                .set(confirmFlag,LogisticsBillCostEntity::getConfirmUserId, UserContext.getDefaultLoginUser().getUid())
                .set(confirmFlag,LogisticsBillCostEntity::getConfirmUserName, UserContext.getDefaultLoginUser().getUserName())
                .set(confirmFlag,LogisticsBillCostEntity::getConfirmTime, null)
                .set(confirmFlag,LogisticsBillCostEntity::getConfirmUserId, "")
                .set(confirmFlag,LogisticsBillCostEntity::getConfirmUserName, "")
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
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        LogisticsBillCostExcelListener excelListenerUtil = new LogisticsBillCostExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), LogisticsBillCostExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        List<LogisticsBillCostExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<LogisticsBillCostExcelDTO > errorList = excelListenerUtil.getErrorList();

        List<LogisticsBillCostExcelDTO> successList = excelListenerUtil.getSuccessList();
        //处理验证成功数据
        handleImportSuccessList(successList, errorList,DictCostAttributionEnum.SELF_DELIVER.getCode());

        if (!errorList.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            String excelPath = "excel/logisticsBillCostError.xlsx";
            String name = "logisticsBillCostError";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.ERROR_95125);
            }
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
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
        //计费重
        BigDecimal billingWeight = MathUtil.compareTo(entity.getActualWeight(),entity.getVolumeWeight()) > MathUtil.ZERO
                ? entity.getActualWeight() : entity.getVolumeWeight();
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
        
        String sourceId = logisticsBillEntity.getSourceId();
        if(StringUtils.isNotBlank(sourceId)) {
        	List<SoB2cLogisticsEntity> soB2cLogisticsList = FeignQuery.create(SoB2cLogisticsEntity.class).eq(SoB2cLogisticsEntity::getMainId, sourceId).list();
        	if(CollUtil.isNotEmpty(soB2cLogisticsList)) {
        		SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsList.get(0);
				entity.setVolume(soB2cLogisticsEntity.getLength() + "*" + soB2cLogisticsEntity.getWidth() + "*" + soB2cLogisticsEntity.getHeight());
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
        List<String> currencyList = records.stream().map(LogisticsBillCostDTO.ListDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyViewList = sysUserFeign.listByCurrency(currencyList);

        //实际金额
        List<String> mainIdList = records.stream().map(LogisticsBillCostDTO.ListDTO::getId).collect(Collectors.toList());
        List<TmsCostDetailDTO.CostViewDTO> costList = tmsCostDetailService.listCostByMainIdList(mainIdList);

        Map<String, String> payStatusNameMap = new HashMap<>();
        payStatusNameMap.put("pay_payment", "待付款");
        payStatusNameMap.put("pay_paid", "已付款");
        payStatusNameMap.put("refund_payment", "待退款");
        payStatusNameMap.put("refund_paid", "已退款");
        for (LogisticsBillCostDTO.ListDTO listDTO : records) {
        	String payType = listDTO.getPayType();
        	String payStatus = listDTO.getPayStatus();
        	if(StringUtils.isNotBlank(payType) && StringUtils.isNotBlank(payStatus)) {
        		listDTO.setPayStatusName(payStatusNameMap.get(payType + "_" + payStatus));
        	}
        	listDTO.setCheckStatusName(LogisticsBillCostCheckStatusEnum.getName(listDTO.getCheckStatus()));
            listDTO.setOrderTypeName(OrderTypeEnum.getName(listDTO.getOrderType()));
            listDTO.setSourceTypeName(SourceTypeEnum.getName(listDTO.getSourceType()));
            listDTO.setReconciliationStatusName(ReconciliationStatusEnum.getName(listDTO.getReconciliationStatus()));
            //运输状态
            String name = transportStatusList.stream().filter(obj -> obj.getCode().equals(listDTO.getTransportStatus())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setTransportStatusName(name);
            //平台名称
            PlatformDictEnum platformDictEnum = PlatformDictEnum.getByCode(listDTO.getSalesPlatform());
            if (ObjectUtil.isNotEmpty(platformDictEnum)) {
                listDTO.setSalesPlatformName(platformDictEnum.getName());
            }
            //币别符号
            String currencySymbol = currencyViewList.stream().filter(obj -> obj.getId().equals(listDTO.getCurrency())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");
            listDTO.setCurrencySymbol(currencySymbol);

            //预估运费
            BigDecimal estimatedShippingCost = costList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(),listDTO.getId())
                            && CharSequenceUtil.equals(DictCostCategoryEnum.SHIPPING_COST.getCode(), obj.getDictCostCategory())
                            && CharSequenceUtil.equals(obj.getType(), LogisticsBillCostTypeEnum.ESTIMATED.getCode()))
                    .map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            listDTO.setEstimatedShippingCost(estimatedShippingCost);
            //预估关税费用
            BigDecimal estimatedDeclareCost = costList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(),listDTO.getId())
                            && CharSequenceUtil.equals(DictCostCategoryEnum.DECLARE_COST.getCode(), obj.getDictCostCategory())
                            && CharSequenceUtil.equals(obj.getType(), LogisticsBillCostTypeEnum.ESTIMATED.getCode()))
                    .map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            listDTO.setEstimatedDeclareCost(estimatedDeclareCost);
            //预估其他费用
            BigDecimal estimatedOtherCost = costList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(),listDTO.getId())
                            && CharSequenceUtil.equals(DictCostCategoryEnum.OTHER_COST.getCode(), obj.getDictCostCategory())
                            && CharSequenceUtil.equals(obj.getType(), LogisticsBillCostTypeEnum.ESTIMATED.getCode()))
                    .map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            listDTO.setEstimatedOtherCost(estimatedOtherCost);
            
            //预估可抵扣税金
            BigDecimal estimatedDeductibleTax = costList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(),listDTO.getId())
            		&& CharSequenceUtil.equals(DictCostCategoryEnum.DEDUCTIBLE_TAX.getCode(), obj.getDictCostCategory())
            		&& CharSequenceUtil.equals(obj.getType(), LogisticsBillCostTypeEnum.ESTIMATED.getCode()))
            		.map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            listDTO.setEstimatedDeductibleTax(estimatedDeductibleTax);

            //实际运费
            BigDecimal actualShippingCost = costList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(),listDTO.getId())
                            && CharSequenceUtil.equals(DictCostCategoryEnum.SHIPPING_COST.getCode(), obj.getDictCostCategory())
                            && CharSequenceUtil.equals(obj.getType(), LogisticsBillCostTypeEnum.ACTUAL.getCode()))
                    .map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            listDTO.setActualShippingCost(actualShippingCost);

            //运费差异
            listDTO.setDiffShippingCost(MathUtil.subtract(actualShippingCost,estimatedShippingCost));

            //实际报关费
            BigDecimal actualDeclareCost = costList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(),listDTO.getId())
                            && CharSequenceUtil.equals(DictCostCategoryEnum.DECLARE_COST.getCode(), obj.getDictCostCategory())
                            && CharSequenceUtil.equals(obj.getType(), LogisticsBillCostTypeEnum.ACTUAL.getCode()))
                    .map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            listDTO.setActualDeclareCost(actualDeclareCost);

            //实际其他费用
            BigDecimal actualOtherCost = costList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(),listDTO.getId())
                            && CharSequenceUtil.equals(DictCostCategoryEnum.OTHER_COST.getCode(), obj.getDictCostCategory())
                            && CharSequenceUtil.equals(obj.getType(), LogisticsBillCostTypeEnum.ACTUAL.getCode()))
                    .map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            listDTO.setActualOtherCost(actualOtherCost);
            
            //实际可抵扣税金
            BigDecimal actualDeductibleTax = costList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(),listDTO.getId())
            		&& CharSequenceUtil.equals(DictCostCategoryEnum.DEDUCTIBLE_TAX.getCode(), obj.getDictCostCategory())
            		&& CharSequenceUtil.equals(obj.getType(), LogisticsBillCostTypeEnum.ACTUAL.getCode()))
            		.map(TmsCostDetailDTO.CostViewDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            listDTO.setActualDeductibleTax(actualDeductibleTax);
            
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
                updateDetailList.add(updateDTO);
                String estimatedCostValue = excelDTO.getEstimatedCostValue();
                if(StringUtils.isNotBlank(estimatedCostValue)) {
                	updateDTO = new TmsCostDetailDTO.UpdateDTO();
                    updateDTO.setCostValue(new BigDecimal(estimatedCostValue));
                    updateDTO.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
                    updateDTO.setCfgCostId(tmsCfgCostEntity.getId());
                    updateDTO.setSourceType(SourceTypeEnum.LOGISTICS_BILL_COST.getCode());
                    updateDetailList.add(updateDTO);
                }
            }
            if (CollectionUtils.isEmpty(updateDetailList)) {
                return;
            }

            //物流单
            LogisticsBillDTO.LogisticsBillVo logisticsBillVo = logisticsBillVos.stream().filter(obj -> obj.getTrackNo().equals(billCostExcelDTO.getTrackNo())).findFirst().orElse(null);
            //物流费用单
            LogisticsBillDetailEntity logisticsBillDetailEntity = logisticsBillDetailList.stream().filter(obj -> obj.getMainId().equals(logisticsBillVo.getId())
                    && CharSequenceUtil.equals(obj.getTrackNo(),billCostExcelDTO.getTrackNo()))
                    .findFirst().orElse(null);
            //物流费用单
            LogisticsBillCostEntity logisticsBillCostEntity = logisticsBillCostList.stream().filter(obj -> obj.getLogisticsBillId().equals(logisticsBillVo.getId())
                    && CharSequenceUtil.equals(obj.getLogisticsBillDetailId(),logisticsBillDetailEntity.getId())
                    && CharSequenceUtil.equals(obj.getPayType(),billCostExcelDTO.getPayType()))
                    .findFirst().orElse(null);

            //数据赋值
            LogisticsBillCostDTO.UpdateDTO updateDataDTO = new LogisticsBillCostDTO.UpdateDTO();
            updateDataDTO.setId(logisticsBillCostEntity.getId());
            updateDataDTO.setBillingWeight(new BigDecimal(billCostExcelDTO.getBillingWeight()));
            updateDataDTO.setBillingWeightLogistics(new BigDecimal(billCostExcelDTO.getBillingWeightLogistics()));
            updateDataDTO.setCurrency(CharSequenceUtil.isBlank(billCostExcelDTO.getCurrency()) ? CurrencyEnum.CNY.getCurrencyCode() : billCostExcelDTO.getCurrency());
            updateDataDTO.setCostDetailList(updateDetailList);
            this.update(updateDataDTO,Boolean.TRUE);
        }
    }

    @Override
    public Boolean updateShopCharge(LogisticsBillCostDTO.UpdateShopChargeDTO dto) {
        //根据店铺查询物流单
        List<LogisticsBillEntity> logisticsBillList = logisticsBillService.listByShopIdList(Arrays.asList(dto.getShopId()));
        if (CollectionUtils.isEmpty(logisticsBillList)) {
            return Boolean.TRUE;
        }
        List<String> logisticsBillIdList = logisticsBillList.stream().map(LogisticsBillEntity::getId).distinct().collect(Collectors.toList());
        //更新店铺负责人
        updateShopChargeId(logisticsBillIdList,dto.getShopChargeId());
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
                    buildFirstMileCostDetail(detailEntity,entity);
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
     * @description: 更新店铺负责人
     * @author Will
     * @date: 2024/5/13 9:13
     * @param logisticsBillIdList
     * @param shopChargeId
     */
    private void updateShopChargeId(List<String> logisticsBillIdList,String shopChargeId) {
        if (CollectionUtils.isEmpty(logisticsBillIdList)) {
            return;
        }
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(shopChargeId);
        if (ObjectUtil.isEmpty(findUserDTO)) {
            throw new ServiceException("未找到店铺负责人");
        }
        lambdaUpdate().in(LogisticsBillCostEntity::getLogisticsBillId,logisticsBillIdList)
                .set(LogisticsBillCostEntity::getShopChargeId,shopChargeId)
                .set(LogisticsBillCostEntity::getShopChargeName,findUserDTO.getUserName())
                .update();
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
        LogisticsBillDTO.LogisticsBillVo logisticsBillVo = logisticsBillList.stream().filter(obj -> obj.getTrackNo().equals(excelDTO.getTrackNo())
                ).findFirst().orElse(null);
        if (ObjectUtil.isEmpty(logisticsBillVo)) {
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
                excelDTO.setCurrency(CharSequenceUtil.isBlank(excelDTO.getCurrency()) ? logisticsBillCostEntity.getCurrency() : excelDTO.getCurrency());

                if (ObjectUtil.isNotEmpty(logisticsBillCostEntity) && !CharSequenceUtil.equals(excelDTO.getCurrency(),logisticsBillCostEntity.getCurrency())) {
                    errorMsgList.add("导入币别与物流费用单币别不一致");
                }
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
	@Override
	public BaseResultDTO.AddDTO addPayAndRefund(AddDataDTO dto) {
		String sourceId = dto.getSourceId();
		LogisticsBillCostEntity logisticsBillCostEntity = getById(sourceId);
		LogisticsBillCostDTO.AddDTO addDTO = new LogisticsBillCostDTO.AddDTO();
		addDTO.setPayType(dto.getPayType());
		addDTO.setReconciliationStatus(ReconciliationStatusEnum.TO_BE_CONFIRM.getCode());
		addDTO.setLogisticsBillId(logisticsBillCostEntity.getLogisticsBillId());
		addDTO.setLogisticsBillDetailId(logisticsBillCostEntity.getLogisticsBillDetailId());
		addDTO.setActualWeight(logisticsBillCostEntity.getActualWeight());
		addDTO.setVolumeWeight(logisticsBillCostEntity.getVolumeWeight());
		String currency = dto.getCurrency();
		if(org.apache.commons.lang3.StringUtils.isBlank(currency)) {
			currency = CurrencyEnum.CNY.getCurrencyCode();
		}
		addDTO.setCurrency(currency);
		addDTO.setTrackNo(logisticsBillCostEntity.getTrackNo());
		addDTO.setChannelId(logisticsBillCostEntity.getChannelId());
		addDTO.setWeightLogistics(logisticsBillCostEntity.getWeightLogistics());
		addDTO.setVolumeWeightLogistics(logisticsBillCostEntity.getVolumeWeightLogistics());
		
		List<DetailDTO> newCostDetailList = dto.getCostDetailList();
		List<TmsCostDetailDTO.AddDTO>  costDetailList = new ArrayList<>();
		for(DetailDTO detailDTO : newCostDetailList) {
			TmsCostDetailDTO.AddDTO add = new TmsCostDetailDTO.AddDTO();
			add.setCfgCostId(detailDTO.getCfgCostId());
			add.setCostValue(detailDTO.getCostValue());
			add.setType(LogisticsBillCostTypeEnum.ACTUAL.getCode());
			costDetailList.add(add);
			
			BigDecimal estimatedValue = detailDTO.getEstimatedValue();
			if(estimatedValue != null && estimatedValue.compareTo(BigDecimal.ZERO) != 0) {
				add = new TmsCostDetailDTO.AddDTO();
				add.setCfgCostId(detailDTO.getCfgCostId());
				add.setCostValue(estimatedValue);
				add.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
				costDetailList.add(add);
			}
		}
		addDTO.setCostDetailList(costDetailList);
		
		return this.add(addDTO);
	}

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
	@Override
	public void addPayAndRefundConfirm(ConfirmAddDataDTO dto) {
		AddDTO addDTO = this.addPayAndRefund(dto);
		this.updateReconciliationStatus(addDTO.getId(), ReconciliationStatusEnum.CONFIRMED.getCode(), dto.getConfirmTime());
	}

	@Override
	public BatchResultDTO updatePayStatus(String id, String payStatus, LocalDateTime payTime) {
		LogisticsBillCostEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "自发货费用"));
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
		return BatchResultDTO.success(entity.getId(), entity.getTrackNo(), OperationTypeEnum.UPDATE_STATUS);
	}
}
