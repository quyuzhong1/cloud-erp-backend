package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.*;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.enums.ImportTypeEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO.UpdateDTO;
import com.erp.model.tms.dto.excel.LogisticsLastMileCostExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.tms.listener.LogisticsLastMileCostExcelListener;
import com.erp.server.tms.mapper.LogisticsBillCostMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_LAST_MILE_COST;
import static com.common.business.enums.FileTaskEventEnum.IMPORT_TMS_LOGISTICS_LAST_MILE_COST;

/**
 * @author Will
 * @version 1.0
 * @date 2024/5/9 18:21
 */
@Slf4j
@Service
public class LogisticsLastMileCostServiceImpl implements LogisticsLastMileCostService {

    @Resource
    private LogisticsBillCostService logisticsBillCostService;
    @Resource
    private LogisticsBillCostMapper logisticsBillCostMapper;

    @Resource
    private TmsCfgCostService tmsCfgCostService;

    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;

    @Resource
    private LogisticsBillService logisticsBillService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private TmsCostDetailService tmsCostDetailService;
    @Resource
    private FileFeign fileFeign;
    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    @Override
    public List<LogisticsBillCostDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<LogisticsBillCostDTO.TabListDTO> tabList = logisticsBillCostService.tabList(dto,DictCostAttributionEnum.LAST_MILE);
        return tabList;
    }

    @Override
    public PagingVO<LogisticsBillCostDTO.ListDTO> paging(PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto) {
        PagingVO<LogisticsBillCostDTO.ListDTO> records = logisticsBillCostService.paging(dto);
        //数据赋值处理
        handleDataPaging((List<LogisticsBillCostDTO.ListDTO>)records.getList());
        return records;
    }

    @Override
    public BaseResultDTO.UpdateDTO update(LogisticsBillCostDTO.UpdateDTO dto, Boolean isImport) {
        return logisticsBillCostService.update(dto, isImport);
    }

    @Override
    public LogisticsBillCostDTO.ViewDTO view(String id) {
        return logisticsBillCostService.view(id);
    }

    @Override
    public BatchResultDTO updateReconciliationStatus(String id, String reconciliationStatus , LocalDateTime confirmTime) {
        return logisticsBillCostService.updateReconciliationStatus(id,reconciliationStatus,confirmTime);
    }

    @Override
    public Boolean downloadTemplate(HttpServletResponse response) {
        List<TmsCfgCostEntity> cfgCostList = tmsCfgCostService.listByCostAttribution(DictCostAttributionEnum.LAST_MILE.getCode());
        if (CollectionUtils.isEmpty(cfgCostList)) {
            throw new ServiceException(ApiError.LOGISTICS_COST_CONFIG_NOT_FOUND,"尾程");
        }
        LinkedList<String> headerNameList = getHeaderNameList();
        LinkedList<String> costNameList = cfgCostList.stream().map(TmsCfgCostEntity::getCostName).distinct().collect(Collectors.toCollection(LinkedList::new));
        if (CollectionUtils.isNotEmpty(costNameList)) {
            headerNameList.addAll(costNameList);
        }
        // 去重
        String configExcelName = "templateConfig.xlsx";
        ExcelUtil.downloadDynamicTemplate(headerNameList, configExcelName, response);
        return Boolean.TRUE;
    }

    /**
     * @description:表头名称
     * @author Will
     * @date: 2024/5/10 18:29
     * @return LinkedList<String>
     */
    private LinkedList<String> getHeaderNameList() {
        LinkedList<String> headerNameList = new LinkedList<>();
        headerNameList.add("平台订单号");
        headerNameList.add("销售订单号");
        headerNameList.add("发货订单号");
        headerNameList.add("物流跟踪单号");
        headerNameList.add("*计费重[物流商]");
        headerNameList.add("*对账类型");
        headerNameList.add("*币种");
        headerNameList.add("尺寸长(物流商)");
        headerNameList.add("尺寸宽(物流商)");
        headerNameList.add("尺寸高(物流商)");
        headerNameList.add("实重(物流商)");
        headerNameList.add("账单确认时间");
        return headerNameList;
    }

    /**
     * @description: 表头对应编码
     * @author Will
     * @date: 2024/5/11 10:08
     * @return JSONObject
     */
    private JSONObject getHeaderNameJsonObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("平台订单号","platformCode");
        jsonObject.set("销售订单号","soCode");
        jsonObject.set("发货订单号","soDeliveryCode");
        jsonObject.set("物流跟踪单号","trackNo");
        jsonObject.set("*计费重[物流商]","billingWeightStr");
        jsonObject.set("*对账类型","payType");
        jsonObject.set("*币种","currency");
        jsonObject.set("尺寸长(物流商)","thirdLength");
        jsonObject.set("尺寸宽(物流商)","thirdWidth");
        jsonObject.set("尺寸高(物流商)","thirdHeight");
        jsonObject.set("实重(物流商)","thirdActualWeight");
        jsonObject.set("账单确认时间","confirmTimeStr");
        return jsonObject;
    }

    /**
     * @param successList 成功数据
     * @param errorList   错误数据
     * @param headList    表头
     * @param headMap     表头
     * @param importType
     * @description: 导入数据处理
     * @author Will
     * @date: 2024/5/10 18:41
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public void handleImportSuccessList(List<JSONObject> successList, List<JSONObject> errorList, List<String> headList, Map<Integer,String> headMap, String importType,Map<String,Object> extMap) {

        if (headList.size() != headList.stream().distinct().collect(Collectors.toList()).size()) {
            throw new ServiceException(ApiError.FILE_EXCEL_IMPORT_HEAD_EXIST);
        }

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

        //获取尾程费用配置信息
        List<TmsCfgCostEntity> cfgCostList = tmsCfgCostService.listByCostAttribution(DictCostAttributionEnum.LAST_MILE.getCode());
        //表头对应json
        JSONObject headerNameJsonObject = getHeaderNameJsonObject();
        //错误信息序号
        Integer errorIndex = getMapKey(headMap, "错误信息");
        //平台订单号序号
        Integer orderIndex = getMapKey(headMap,"平台订单号");
        //销售订单号序号
        Integer soIndex = getMapKey(headMap,"销售订单号");
        //发货订单号序号
        Integer soDeliveryIndex = getMapKey(headMap,"发货订单号");
        //物流跟踪单号序号
        Integer trackNoIndex = getMapKey(headMap,"物流跟踪单号");
        //平台订单号
        List<String> platformCodeList = ObjectUtil.isEmpty(orderIndex) ? new ArrayList<>() : successList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.get(orderIndex.toString())))
                .map(obj -> obj.get(orderIndex.toString()).toString()).distinct().collect(Collectors.toList());
        //销售订单号
        List<String> soCodeList = ObjectUtil.isEmpty(soIndex) ? new ArrayList<>() : successList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.get(soIndex.toString())))
                .map(obj -> obj.get(soIndex.toString()).toString()).distinct().collect(Collectors.toList());
        //发货订单号
        List<String> soDeliveryCodeList = ObjectUtil.isEmpty(soDeliveryIndex) ? new ArrayList<>() : successList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.get(soDeliveryIndex.toString())))
                .map(obj -> obj.get(soDeliveryIndex.toString()).toString()).distinct().collect(Collectors.toList());
        //物流跟踪号
        List<String> trackNoList = ObjectUtil.isEmpty(trackNoIndex) ? new ArrayList<>() :  successList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.get(trackNoIndex.toString())))
                .map(obj -> obj.get(trackNoIndex.toString()).toString()).distinct().collect(Collectors.toList());

        //物流明细信息
        List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos = logisticsBillService.listLogisticsBillVoByData(platformCodeList,soCodeList,soDeliveryCodeList,trackNoList);

        //物流费用信息
        List<String> logisticsBillDetailIdList = logisticsBillVos.stream().map(LogisticsBillDTO.LogisticsBillVo::getDetailId).distinct().collect(Collectors.toList());
        List<LogisticsBillCostEntity> logisticsBillCostList = logisticsBillCostService.listByLogisticsBillDetailIdList(logisticsBillDetailIdList);
        for (JSONObject jsonObject :  successList) {
            //主数据
            JSONObject successJson = new JSONObject();
            //费用数据
            List<TmsCostDetailDTO.UpdateDTO> updateList = new ArrayList<>();
            //用于判断是否存在重复的费用数据
            JSONObject hasData = new JSONObject();

            List<String> errorMsgList = new ArrayList<>();

            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                //字段名称
                String field = headList.get(Integer.valueOf(entry.getKey()));
                if (CharSequenceUtil.equals(field,"错误信息")) {
                    continue;
                }
                TmsCfgCostEntity tmsCfgCostEntity = cfgCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCostName(), field) && CharSequenceUtil.equals(obj.getDictCostAttribution(),DictCostAttributionEnum.LAST_MILE.getCode())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(tmsCfgCostEntity) && !getHeaderNameList().contains(field)) {
                    errorMsgList.add(CharSequenceUtil.format("费用管理尾程未找到该费用名称【{}】",field));
                    continue;
                }
                //校验后面数据是否存在重复的
                if (ObjectUtil.isNotEmpty(tmsCfgCostEntity)) {
                    if (ObjectUtil.isNotEmpty(hasData.get(tmsCfgCostEntity.getId()))) {
                        errorMsgList.add("费用已存在，请勿重复导入");
                    }
                    hasData.set(tmsCfgCostEntity.getId(), tmsCfgCostEntity.getCostName());
                }
                Object headName = headerNameJsonObject.get(field);
                if (ObjectUtil.isNotEmpty(headName)) {
                    successJson.set(headName.toString(),String.valueOf(entry.getValue()));
                }
                if (ObjectUtil.isNotEmpty(tmsCfgCostEntity) && ObjectUtil.isNotEmpty(entry.getValue())) {
                    TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
                    updateDTO.setCostValue(new BigDecimal(entry.getValue().toString()));
                    updateDTO.setType(LogisticsBillCostTypeEnum.ACTUAL.getCode());
                    updateDTO.setCfgCostId(tmsCfgCostEntity.getId());
                    updateDTO.setDictCostCategory(tmsCfgCostEntity.getDictCostCategory());
                    updateDTO.setSourceType(SourceTypeEnum.LAST_MILE_LOGISTICS_BILL_COST.getCode());
                    updateList.add(updateDTO);
                }
            }
            LogisticsLastMileCostExcelDTO excelDTO = BeanUtil.toBean(successJson, LogisticsLastMileCostExcelDTO.class);
            //基础验证
            List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
            if (CollectionUtils.isNotEmpty(msgList)) {
                errorMsgList.addAll(msgList);
            }
            //付款类型
            excelDTO.setPayType(logisticsPayTypeEnum.getName(excelDTO.getPayType()));

            //物流单明细
            List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList = logisticsBillVos.stream()
                    .filter(obj -> CharSequenceUtil.isBlank(excelDTO.getPlatformCode()) || obj.getPlatformCode().equals(excelDTO.getPlatformCode()))
                    .filter(obj -> CharSequenceUtil.isBlank(excelDTO.getSoDeliveryCode()) || obj.getSoDeliveryCode().equals(excelDTO.getSoDeliveryCode()))
                    .filter(obj -> CharSequenceUtil.isBlank(excelDTO.getSoCode()) || obj.getSourceCode().equals(excelDTO.getSoCode()))
                    .filter(obj -> CharSequenceUtil.isBlank(excelDTO.getTrackNo()) || obj.getTrackNo().equals(excelDTO.getTrackNo()))
                    .collect(Collectors.toList());
            if (CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_NEW.getCode().equals(importType)) {
                if (CollUtil.isNotEmpty(logisticsBillVoList)) {
                    errorMsgList.add("单号已存在无法新增，请核查单号");
                }
                if (CharSequenceUtil.isBlank(excelDTO.getTrackNo())) {
                    errorMsgList.add("物流单号不能为空");
                }
                if (CharSequenceUtil.isBlank(excelDTO.getLogisticsSupplierName())) {
                    errorMsgList.add("物流商不能为空");
                }
            } else {
                if (CollUtil.isEmpty(logisticsBillVoList)) {
                    errorMsgList.add("未找到对应物流单");
                }
                if (logisticsBillVoList.size() > 1){
                    errorMsgList.add("对应物流单有多条，请在补全导入订单信息后重新导入");
                }
            }
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                jsonObject.set(errorIndex.toString(),FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(jsonObject);
                continue;
            }

            LogisticsBillCostEntity logisticsBillCostEntity;
            //对账确认日期
            List<Pair<String,LocalDateTime>> pairList = new ArrayList<>();
            LocalDateTime confirmTime = CharSequenceUtil.isBlank(excelDTO.getConfirmTimeStr()) ? LocalDateTime.now() : LocalDateUtil.stringToLocalDateTime(excelDTO.getConfirmTimeStr());

            if (CollUtil.isNotEmpty(logisticsBillVoList)) {
                LogisticsBillDTO.LogisticsBillVo logisticsBillVo = logisticsBillVoList.get(0);
                logisticsBillVo.setReconciliationMonth(reconciliationMonth);
                //数据验证
                List<String> importMsgList = checkImportData(excelDTO,logisticsBillCostList,logisticsBillVo,DictCostAttributionEnum.LAST_MILE.getCode(), importType);
                if (CollectionUtils.isNotEmpty(importMsgList)) {
                    errorMsgList.addAll(importMsgList);
                }
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    jsonObject.set(errorIndex.toString(),FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(jsonObject);
                    continue;
                }
                //物流费用单
                 logisticsBillCostEntity = logisticsBillCostList.stream().filter(obj ->
                                CharSequenceUtil.equals(obj.getLogisticsBillDetailId(),logisticsBillVo.getDetailId())
                                        && CharSequenceUtil.equals(obj.getReconciliationStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())
                                        && CharSequenceUtil.equals(obj.getPayType(),excelDTO.getPayType()))
                        .findFirst().orElse(null);
                if (ImportTypeEnum.ADD.getCode().equals(importType) && Objects.isNull(logisticsBillCostEntity)){
                    logisticsBillCostEntity = logisticsBillCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getLogisticsBillDetailId(),logisticsBillVo.getDetailId())).findFirst().orElse(null);
                }
                LogisticsBillCostDTO.UpdateDTO updateDataDTO = handleLogisticsBillCostImportData(logisticsBillCostEntity, excelDTO, updateList, errorList, jsonObject, errorIndex, cfgCostList);
                //对账月份
                updateDataDTO.setReconciliationMonth(reconciliationMonth);

                if (CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode().equals(importType)){
                    List<LogisticsBillCostDTO.AddDataDTO> dtoList = buildAddDTO(updateDataDTO,updateList);
                    List<BaseResultDTO.AddDTO> addDTOS = logisticsBillCostService.addPayAndRefund(dtoList);
                    pairList = addDTOS.stream()
                            .map(obj -> new Pair<String, LocalDateTime>(obj.getId(), confirmTime))
                            .collect(Collectors.toList());
                }else {
                    updateDataDTO.setCostDetailList(updateList);
                    BaseResultDTO.UpdateDTO update = logisticsBillCostService.update(updateDataDTO, Boolean.TRUE);
                    pairList.add(new Pair<>(update.getId(),confirmTime));
                }
            } else {
                LogisticsBillEntity logisticsBillEntity = addImportLogisticBill(excelDTO);
                //查询物流费用加下更新
                List<LogisticsBillCostEntity> logisticsBillCost = logisticsBillCostService.getByLogisticsBillIds(Collections.singletonList(logisticsBillEntity.getId()));
                if (CollUtil.isEmpty(logisticsBillCost)) {
                    throw new ServiceException("新增物流单后未找到对应的物流费用数据，无法进行后续处理");
                }
                logisticsBillCostEntity = logisticsBillCost.get(0);

                LogisticsBillCostDTO.UpdateDTO updateDataDTO = handleLogisticsBillCostImportData(logisticsBillCostEntity, excelDTO,updateList, errorList, jsonObject, errorIndex, cfgCostList);
                updateDataDTO.setCostDetailList(updateList);
                BaseResultDTO.UpdateDTO update = logisticsBillCostService.update(updateDataDTO, Boolean.TRUE);
                pairList.add(new Pair<>(update.getId(),confirmTime));
            }
            //确认
            if (confirmStatus) {
                pairList.forEach(obj -> this.updateReconciliationStatus(obj.getKey(), ReconciliationStatusEnum.CONFIRMED.getCode(), obj.getValue()));
            }
        }
    }

    /**
     * 新增物流单
     * @author will
     * @date 2026/1/22 20:04
     * @param excelDTO
     * @return com.erp.model.tms.entity.LogisticsBillEntity
     */
    private LogisticsBillEntity addImportLogisticBill (LogisticsLastMileCostExcelDTO excelDTO) {
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
                    addDTO.setOrderType(OrderTypeEnum.B2B.getCode());
                }
            } else if (excelDTO.getSoDeliveryCode().startsWith("FHDC")) {
                List<SoB2cDeliveryEntity> list = FeignQuery.create(SoB2cDeliveryEntity.class).eq(SoB2cDeliveryEntity::getCode, excelDTO.getSoDeliveryCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addDTO.setSoDeliveryCode(list.get(0).getId());
                    addDTO.setSourceCode(list.get(0).getSourceCode());
                    addDTO.setOrderType(OrderTypeEnum.B2C.getCode());
                }
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
                    addDTO.setOrderType(OrderTypeEnum.B2B.getCode());
                }
            } else if (excelDTO.getSoCode().startsWith("XSDS")) {
                List<SoB2cEntity> list = FeignQuery.create(SoB2cEntity.class).eq(SoB2cEntity::getCode, excelDTO.getSoCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addDTO.setSourceId(list.get(0).getId());
                    addDTO.setOrderType(OrderTypeEnum.B2C.getCode());
                }
            }
        }
        addDTO.setSourceCode(excelDTO.getSoCode());
        addDTO.setPlatformCode(excelDTO.getPlatformCode());
        addDTO.setSoDeliveryCode(excelDTO.getSoDeliveryCode());

        LogisticsBillDetailDTO.AddDTO addDetailDTO = new LogisticsBillDetailDTO.AddDTO();
        addDetailDTO.setTrackNo(excelDTO.getTrackNo());
        addDTO.setDetailList(Collections.singletonList(addDetailDTO));
        return logisticsBillService.add(addDTO);
    }

    /**
     * 数据处理
     * @author will
     * @date 2026/1/23 11:47
     * @param logisticsBillCostEntity
     * @param excelDTO
     * @param updateList
     * @param errorList
     * @param jsonObject
     * @param errorIndex
     * @param cfgCostList
     * @return UpdateDTO
     */
    private LogisticsBillCostDTO.UpdateDTO handleLogisticsBillCostImportData(LogisticsBillCostEntity logisticsBillCostEntity, LogisticsLastMileCostExcelDTO excelDTO,
                                                                             List<TmsCostDetailDTO.UpdateDTO> updateList,
                                                                             List<JSONObject> errorList,
                                                                             JSONObject jsonObject,
                                                                             Integer errorIndex,
                                                                             List<TmsCfgCostEntity> cfgCostList) {
        //数据赋值
        LogisticsBillCostDTO.UpdateDTO updateDataDTO = new LogisticsBillCostDTO.UpdateDTO();
        updateDataDTO.setId(logisticsBillCostEntity.getId());
        updateDataDTO.setBillingWeightLogistics(new BigDecimal(excelDTO.getBillingWeightStr()));
        updateDataDTO.setCurrency(CharSequenceUtil.isBlank(excelDTO.getCurrency()) ? CurrencyEnum.CNY.getCurrencyCode() : excelDTO.getCurrency());
        updateDataDTO.setPayType(excelDTO.getPayType());

        //尺寸
        String thirdHeight = excelDTO.getThirdHeight();
        if(StringUtils.isNotBlank(thirdHeight)) {
            updateDataDTO.setThirdHeight(new BigDecimal(thirdHeight));
        }
        String thirdWidth = excelDTO.getThirdWidth();
        if(StringUtils.isNotBlank(thirdWidth)) {
            updateDataDTO.setThirdWidth(new BigDecimal(thirdWidth));
        }
        String thirdLength = excelDTO.getThirdLength();
        if(StringUtils.isNotBlank(thirdHeight)) {
            updateDataDTO.setThirdLength(new BigDecimal(thirdLength));
        }
        //实重
        String thirdActualWeight = excelDTO.getThirdActualWeight();
        if(StringUtils.isNotBlank(thirdActualWeight)) {
            updateDataDTO.setThirdActualWeight(new BigDecimal(thirdActualWeight));
        }
        updateList.forEach(u -> u.setCurrency(updateDataDTO.getCurrency()));
        List<TmsCostDetailEntity> validateList = BeanMapperUtils.copyList(TmsCostDetailEntity.class, updateList);
        List<TmsCostDetailEntity> tmsCostDetailEntityList = tmsCostDetailService.listByMainIdList(Arrays.asList(logisticsBillCostEntity.getId()));
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
                    String costName = cfgCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), remove.getCfgCostId())).findFirst().orElse(null).getCostName();
                    costIdTypeListMap.put(costName, AllocationFeeTypeEnum.getName(split[0]) + "-" + LogisticsBillCostTypeEnum.getName(split[1]) + "分类下所有费用币种必须一致");
                }
                updateList.removeIf(u -> u.getDictCostCategory().equals(split[0]) && u.getType().equals(split[1]));
            }
            if(!costIdTypeListMap.isEmpty()) {
                jsonObject.set(errorIndex.toString(),FieldValidUtil.getMsgSort(new ArrayList<>(costIdTypeListMap.values())));
                errorList.add(jsonObject);
            }
        }
        return updateDataDTO;
    }


    private List<LogisticsBillCostDTO.AddDataDTO> buildAddDTO(LogisticsBillCostDTO.UpdateDTO updateDataDTO, List<UpdateDTO> updateList) {
        List<LogisticsBillCostDTO.AddDataDTO> dtoList = new ArrayList<>();
        updateList.forEach(u -> {
            LogisticsBillCostDTO.AddDataDTO addDataDTO = new LogisticsBillCostDTO.AddDataDTO();
            addDataDTO.setPayType(updateDataDTO.getPayType());
            addDataDTO.setSourceId(updateDataDTO.getId());
            addDataDTO.setBillingWeight(updateDataDTO.getBillingWeight());
            addDataDTO.setBillingWeightLogistics(updateDataDTO.getBillingWeightLogistics());
            addDataDTO.setCurrency(updateDataDTO.getCurrency());

            addDataDTO.setReconciliationMonth(updateDataDTO.getReconciliationMonth());
            addDataDTO.setThirdHeight(updateDataDTO.getThirdHeight());
            addDataDTO.setThirdWidth(updateDataDTO.getThirdWidth());
            addDataDTO.setThirdLength(updateDataDTO.getThirdLength());
            addDataDTO.setThirdActualWeight(updateDataDTO.getThirdActualWeight());
            addDataDTO.setCfgCostId(u.getCfgCostId());
            addDataDTO.setCostValue(u.getCostValue());
            dtoList.add(addDataDTO);
        });
        return dtoList;
    }

    /**
     * @param excelDTO
     * @param logisticsBillCostList
     * @param logisticsBillVo
     * @param dictCostAttribution
     * @param importType
     * @return List<String>
     * @description: 导入数据处理
     * @author Will
     * @date: 2024/5/11 14:24
     */
    private List<String> checkImportData (LogisticsLastMileCostExcelDTO excelDTO
            , List<LogisticsBillCostEntity> logisticsBillCostList, LogisticsBillDTO.LogisticsBillVo logisticsBillVo , String dictCostAttribution, String importType) {
        List<String> errorMsgList = new ArrayList<>();
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
            if(!ImportTypeEnum.ADD.getCode().equals(importType)){
                errorMsgList.add("未找到对应对账类型的物流费用单");
            }
        } else {
            if(logisticsBillCostEntityList.size() > 1) {
                if (ImportTypeEnum.UPDATE.getCode().equals(importType)) {
                    long count = logisticsBillCostEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getReconciliationStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())).count();
                    if (count > 1) {
                        errorMsgList.add("出库单和运输单号对应对账类型的物流费用单有多条，请在页面编辑指定物流费用单");
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
                if (ImportTypeEnum.UPDATE.getCode().equals(importType) ) {
                    if (CharSequenceUtil.equals(ReconciliationStatusEnum.CONFIRMED.getCode(),logisticsBillCostEntity.getReconciliationStatus())) {
                        errorMsgList.add("物流费用单已确认不支持更新");
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
        return errorMsgList;
    }

    @Override
    public Boolean exportExcel(LogisticsBillCostDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("尾程费用列表", EXPORT_TMS_LOGISTICS_LAST_MILE_COST.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<LogisticsBillCostDTO.ListDTO> exportLogisticsLastMileCost(PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<LogisticsBillCostDTO.ListDTO> page = logisticsBillCostMapper.listByExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            //数据赋值处理
            logisticsBillCostService.handleDataPaging(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public Boolean importExcel(BaseDTO.ImportDTO dto) {
        downloadTaskFeign.saveImportTask("尾程费用列表", IMPORT_TMS_LOGISTICS_LAST_MILE_COST.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importLogisticsLastMileCost(BaseDTO.ImportDTO dto) {
        LogisticsLastMileCostExcelListener excelListenerUtil = new LogisticsLastMileCostExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount(),dto.getExtMap());
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        //导出错误数据
        List<JSONObject> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "尾程费用错误数据.xlsx";
            File file = ExcelUtil.customExportUtil(fileName, errorList, excelListenerUtil.getHeadList());
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    /**
     * @description: 分页数据查询
     * @author Will
     * @date: 2024/5/10 16:12
     * @param records
     */
    private void handleDataPaging (List<LogisticsBillCostDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        //TODO

    }

    /**
     * @description: 根据value获取对应的key
     * @author Will
     * @date: 2024/5/11 11:41
     * @param headMap
     * @param targetValue
     * @return String
     */
    private Integer getMapKey (Map<Integer,String> headMap,String targetValue) {
        Integer resultKey = null;
        for (Integer key : headMap.keySet()) {
            // 获取对应的value
            String value = headMap.get(key);

            // 如果value等于目标值，输出对应的key
            if (value.equals(targetValue)) {
                resultKey = key;
                // 如果只需要找到一个匹配的key，可以break
                break;
            }
        }
        return  resultKey;
    }
}
