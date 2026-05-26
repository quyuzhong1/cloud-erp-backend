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
import com.erp.model.plm.entity.ProductPackEntity;
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
import com.erp.model.wms.entity.SoOutstockDetailEntity;
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
import java.math.RoundingMode;
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
        headerNameList.add("物流商");
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
        jsonObject.set("物流商","logisticsSupplierName");
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
        // 对账月份决定本次费用归属期间，新增费用时会用它判断是否存在相同月份费用单。
        String reconciliationMonth = (String)extMap.get("reconciliationMonth");
        if (StrUtil.isBlank(reconciliationMonth)) {
            throw new ServiceException(ApiError.LOGISTICS_BILL_COST_IMPORT_NOT_EXIST_RECONCILIATION_MONTH);
        }
        // 是否确认决定导入成功后是否直接把费用单流转为已确认。
        Boolean confirmStatus = (Boolean)extMap.get("confirmStatus");

        // 只查询尾程费用项配置，避免自发货费用项出现在尾程模板中被误识别。
        List<TmsCfgCostEntity> cfgCostList = tmsCfgCostService.listByCostAttribution(DictCostAttributionEnum.LAST_MILE.getCode());
        // 固定字段通过表头映射为 DTO 字段，费用项字段则通过费用配置动态识别。
        JSONObject headerNameJsonObject = getHeaderNameJsonObject();
        // 错误信息列由 Listener 追加，行级校验失败时直接写回该列供错误文件导出。
        Integer errorIndex = getMapKey(headMap, "错误信息");
        Integer orderIndex = getMapKey(headMap,"平台订单号");
        Integer soIndex = getMapKey(headMap,"销售订单号");
        Integer soDeliveryIndex = getMapKey(headMap,"发货订单号");
        Integer trackNoIndex = getMapKey(headMap,"物流跟踪单号");

        // 收集本批次所有可作为识别号的单号，用于一次性预查询物流单，避免逐行查询数据库。
        List<String> platformCodeList = ObjectUtil.isEmpty(orderIndex) ? new ArrayList<>() : successList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.get(orderIndex.toString())))
                .map(obj -> obj.get(orderIndex.toString()).toString()).distinct().collect(Collectors.toList());
        List<String> soCodeList = ObjectUtil.isEmpty(soIndex) ? new ArrayList<>() : successList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.get(soIndex.toString())))
                .map(obj -> obj.get(soIndex.toString()).toString()).distinct().collect(Collectors.toList());
        List<String> soDeliveryCodeList = ObjectUtil.isEmpty(soDeliveryIndex) ? new ArrayList<>() : successList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.get(soDeliveryIndex.toString())))
                .map(obj -> obj.get(soDeliveryIndex.toString()).toString()).distinct().collect(Collectors.toList());
        List<String> trackNoList = ObjectUtil.isEmpty(trackNoIndex) ? new ArrayList<>() :  successList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.get(trackNoIndex.toString())))
                .map(obj -> obj.get(trackNoIndex.toString()).toString()).distinct().collect(Collectors.toList());

        // 物流单预查询结果包含主单、明细和费用单关联字段，后续行级匹配都在内存中完成。
        List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos = logisticsBillService.listLogisticsBillVoByData(platformCodeList,soCodeList,soDeliveryCodeList,trackNoList);

        // 尾程费用更新依赖物流单明细 ID 定位费用单，先批量查询可更新的费用单集合。
        List<String> logisticsBillDetailIdList = logisticsBillVos.stream().map(LogisticsBillDTO.LogisticsBillVo::getDetailId).distinct().collect(Collectors.toList());
        List<LogisticsBillCostEntity> logisticsBillCostList = logisticsBillCostService.listByLogisticsBillDetailIdList(logisticsBillDetailIdList);

        Map<String, List<JSONObject>> importGroupMap = new LinkedHashMap<>();
        Map<JSONObject, LogisticsLastMileCostExcelDTO> rowExcelMap = new IdentityHashMap<>();
        Map<JSONObject, List<TmsCostDetailDTO.UpdateDTO>> rowUpdateMap = new IdentityHashMap<>();
        for (JSONObject jsonObject :  successList) {
            // successJson 收集固定字段，updateList 收集动态费用项字段，二者共同构成一行导入数据。
            JSONObject successJson = new JSONObject();
            List<TmsCostDetailDTO.UpdateDTO> updateList = new ArrayList<>();
            // 同一行不允许出现重复费用项，否则无法判断应覆盖还是累加。
            JSONObject hasData = new JSONObject();

            List<String> errorMsgList = new ArrayList<>();

            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                // 表头决定当前列是固定字段还是费用项；未知费用项要进入错误文件，避免静默丢失金额。
                String field = headList.get(Integer.valueOf(entry.getKey()));
                if (CharSequenceUtil.equals(field,"错误信息")) {
                    continue;
                }
                TmsCfgCostEntity tmsCfgCostEntity = cfgCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCostName(), field) && CharSequenceUtil.equals(obj.getDictCostAttribution(),DictCostAttributionEnum.LAST_MILE.getCode())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(tmsCfgCostEntity) && !getHeaderNameList().contains(field)) {
                    errorMsgList.add(CharSequenceUtil.format("费用管理尾程未找到该费用名称【{}】",field));
                    continue;
                }
                if (ObjectUtil.isNotEmpty(tmsCfgCostEntity)) {
                    if (ObjectUtil.isNotEmpty(hasData.get(tmsCfgCostEntity.getId()))) {
                        errorMsgList.add("费用已存在，请勿重复导入");
                    }
                    hasData.set(tmsCfgCostEntity.getId(), tmsCfgCostEntity.getCostName());
                }
                Object headName = headerNameJsonObject.get(field);
                if (ObjectUtil.isNotEmpty(headName)) {
                    // 固定字段统一转成 DTO 字段名，后续复用 DTO 校验注解做基础校验。
                    successJson.set(headName.toString(),String.valueOf(entry.getValue()));
                }
                if (ObjectUtil.isNotEmpty(tmsCfgCostEntity) && ObjectUtil.isNotEmpty(entry.getValue())) {
                    // 费用项列按实际费用导入，币种稍后统一取整行币种，保证同一行尾程费用币种一致。
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
            // 基础校验处理必填字段和金额格式，失败行只写入错误文件，不影响本批其他行。
            List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
            if (CollectionUtils.isNotEmpty(msgList)) {
                errorMsgList.addAll(msgList);
            }
            // Excel 填写的是对账类型名称，落库前统一转换为系统 code。
            excelDTO.setPayType(logisticsPayTypeEnum.getByName(excelDTO.getPayType()));
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                jsonObject.set(errorIndex.toString(),FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(jsonObject);
                continue;
            }
            updateList.forEach(updateDTO -> updateDTO.setCurrency(excelDTO.getCurrency()));
            rowExcelMap.put(jsonObject, excelDTO);
            rowUpdateMap.put(jsonObject, updateList);
            importGroupMap.computeIfAbsent(buildImportGroupKey(excelDTO), key -> new ArrayList<>()).add(jsonObject);
        }

        for (Map.Entry<String, List<JSONObject>> entry : importGroupMap.entrySet()) {
            List<JSONObject> value = entry.getValue();
            LogisticsLastMileCostExcelDTO excelDTO = rowExcelMap.get(value.get(0));
            List<String> errorMsgList = new ArrayList<>();
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
            } else if (CollUtil.isEmpty(logisticsBillVoList)) {
                errorMsgList.add("未找到对应物流单");
            }
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                value.forEach(jsonObject -> jsonObject.set(errorIndex.toString(),FieldValidUtil.getMsgSort(errorMsgList)));
                errorList.addAll(value);
                continue;
            }
            List<TmsCostDetailDTO.UpdateDTO> updateList = new ArrayList<>();
            for (JSONObject jsonObject : value) {
                updateList.addAll(rowUpdateMap.getOrDefault(jsonObject, Collections.emptyList()));
            }
            if (CollUtil.isEmpty(updateList)) {
                errorMsgList.add("物流费用项不能为空");
                value.forEach(jsonObject -> jsonObject.set(errorIndex.toString(),FieldValidUtil.getMsgSort(errorMsgList)));
                errorList.addAll(value);
                continue;
            }
            validateSameCostCurrency(updateList, cfgCostList, errorMsgList);
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                value.forEach(jsonObject -> jsonObject.set(errorIndex.toString(),FieldValidUtil.getMsgSort(errorMsgList)));
                errorList.addAll(value);
                continue;
            }
            updateList = mergeImportUpdateDetails(updateList);
            LogisticsBillCostEntity logisticsBillCostEntity;
            // 对账确认日期按同识别号首行取值，保持与原逐行导入的主数据取值一致。
            List<Pair<String,LocalDateTime>> pairList = new ArrayList<>();
            LocalDateTime confirmTime = CharSequenceUtil.isBlank(excelDTO.getConfirmTimeStr()) ? LocalDateTime.now() : LocalDateUtil.stringToLocalDateTime(excelDTO.getConfirmTimeStr());

            if (CollUtil.isNotEmpty(logisticsBillVoList)) {
                Map<String, List<TmsCostDetailDTO.UpdateDTO>> allocatedCostMap = new HashMap<>();
                if (logisticsBillVoList.size() > 1) {
                    Map<String, BigDecimal> weightMap = buildOrderWeightMap(logisticsBillVoList, errorMsgList);
                    if (CollectionUtils.isEmpty(errorMsgList)) {
                        allocatedCostMap = allocateCostDetailMap(updateList, logisticsBillVoList, weightMap);
                    }
                }
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    value.forEach(jsonObject -> jsonObject.set(errorIndex.toString(),FieldValidUtil.getMsgSort(errorMsgList)));
                    errorList.addAll(value);
                    continue;
                }
                List<Pair<LogisticsBillDTO.LogisticsBillVo, LogisticsBillCostEntity>> targetPairList = new ArrayList<>();
                Map<String, List<TmsCostDetailDTO.UpdateDTO>> targetUpdateMap = new HashMap<>();
                for (LogisticsBillDTO.LogisticsBillVo logisticsBillVo : logisticsBillVoList) {
                    logisticsBillVo.setReconciliationMonth(reconciliationMonth);
                    List<TmsCostDetailDTO.UpdateDTO> currentUpdateList = logisticsBillVoList.size() > 1
                            ? allocatedCostMap.getOrDefault(logisticsBillVo.getDetailId(), Collections.emptyList())
                            : updateList;
                    List<String> importMsgList = checkImportData(excelDTO,logisticsBillCostList,logisticsBillVo,DictCostAttributionEnum.LAST_MILE.getCode(), importType);
                    if (CollectionUtils.isNotEmpty(importMsgList)) {
                        errorMsgList.addAll(importMsgList);
                        break;
                    }
                    // 更新费用时优先匹配待确认或暂估确认且核对中的费用单，避免覆盖已完成对账的数据。
                    logisticsBillCostEntity = logisticsBillCostList.stream().filter(obj ->
                                    CharSequenceUtil.equals(obj.getLogisticsBillDetailId(),logisticsBillVo.getDetailId())
                                            && (CharSequenceUtil.equals(obj.getReconciliationStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode()) || (CharSequenceUtil.equals(ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode(),obj.getReconciliationStatus())
                                            && CharSequenceUtil.equals(obj.getCheckStatus(),LogisticsBillCostCheckStatusEnum.CHECKING.getCode())))
                                            && CharSequenceUtil.equals(obj.getPayType(),excelDTO.getPayType()))
                            .findFirst().orElse(null);
                    if (CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode().equals(importType) && Objects.isNull(logisticsBillCostEntity)){
                        logisticsBillCostEntity = logisticsBillCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getLogisticsBillDetailId(),logisticsBillVo.getDetailId())).findFirst().orElse(null);
                    }
                    if (Objects.isNull(logisticsBillCostEntity)) {
                        errorMsgList.add("未找到对应物流费用单");
                        break;
                    }
                    checkCategoryCurrency(currentUpdateList, logisticsBillCostEntity, cfgCostList, errorMsgList);
                    if (CollectionUtils.isNotEmpty(errorMsgList)) {
                        break;
                    }
                    targetPairList.add(new Pair<>(logisticsBillVo, logisticsBillCostEntity));
                    targetUpdateMap.put(logisticsBillVo.getDetailId(), currentUpdateList);
                }
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    value.forEach(jsonObject -> jsonObject.set(errorIndex.toString(),FieldValidUtil.getMsgSort(errorMsgList)));
                    errorList.addAll(value);
                    continue;
                }
                for (Pair<LogisticsBillDTO.LogisticsBillVo, LogisticsBillCostEntity> targetPair : targetPairList) {
                    LogisticsBillDTO.LogisticsBillVo logisticsBillVo = targetPair.getKey();
                    logisticsBillCostEntity = targetPair.getValue();
                    List<TmsCostDetailDTO.UpdateDTO> currentUpdateList = targetUpdateMap.get(logisticsBillVo.getDetailId());
                    LogisticsBillCostDTO.UpdateDTO updateDataDTO = handleLogisticsBillCostImportData(logisticsBillCostEntity, excelDTO, currentUpdateList);
                    updateDataDTO.setReconciliationMonth(reconciliationMonth);
                    if (CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode().equals(importType)){
                        List<LogisticsBillCostDTO.AddDataDTO> dtoList = buildAddDTO(updateDataDTO,currentUpdateList);
                        List<BaseResultDTO.AddDTO> addDTOS = logisticsBillCostService.addPayAndRefund(dtoList);
                        pairList.addAll(addDTOS.stream()
                                .map(obj -> new Pair<String, LocalDateTime>(obj.getId(), confirmTime))
                                .collect(Collectors.toList()));
                    }else {
                        updateDataDTO.setCostDetailList(currentUpdateList);
                        BaseResultDTO.UpdateDTO update = logisticsBillCostService.update(updateDataDTO, Boolean.TRUE);
                        pairList.add(new Pair<>(update.getId(),confirmTime));
                    }
                }
            } else {
                LogisticsBillEntity logisticsBillEntity = addImportLogisticBill(excelDTO);
                List<LogisticsBillCostEntity> logisticsBillCost = logisticsBillCostService.getByLogisticsBillIds(Collections.singletonList(logisticsBillEntity.getId()));
                if (CollUtil.isEmpty(logisticsBillCost)) {
                    throw new ServiceException("新增物流单后未找到对应的物流费用数据，无法进行后续处理");
                }
                logisticsBillCostEntity = logisticsBillCost.get(0);

                checkCategoryCurrency(updateList, logisticsBillCostEntity, cfgCostList, errorMsgList);
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    value.forEach(jsonObject -> jsonObject.set(errorIndex.toString(),FieldValidUtil.getMsgSort(errorMsgList)));
                    errorList.addAll(value);
                    continue;
                }
                LogisticsBillCostDTO.UpdateDTO updateDataDTO = handleLogisticsBillCostImportData(logisticsBillCostEntity, excelDTO,updateList);
                updateDataDTO.setReconciliationMonth(reconciliationMonth);
                updateDataDTO.setCostDetailList(updateList);
                BaseResultDTO.UpdateDTO update = logisticsBillCostService.update(updateDataDTO, Boolean.TRUE);
                pairList.add(new Pair<>(update.getId(),confirmTime));
            }
            // 已确认状态是后续对账流程入口，只有用户选择导入并确认时才在本次导入末尾流转。
            if (confirmStatus) {
                pairList.forEach(obj -> this.updateReconciliationStatus(obj.getKey(), ReconciliationStatusEnum.CONFIRMED.getCode(), obj.getValue()));
            }
        }
    }

    /**
     * 构建尾程标准导入聚合键。
     */
    private String buildImportGroupKey(LogisticsLastMileCostExcelDTO excelDTO) {
        return CharSequenceUtil.format("{}_{}_{}_{}_{}",
                excelDTO.getPlatformCode(),
                excelDTO.getSoCode(),
                excelDTO.getSoDeliveryCode(),
                excelDTO.getTrackNo(),
                excelDTO.getPayType());
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
            existsDTO.setCostValue(existsDTO.getCostValue().add(updateDTO.getCostValue()));
        }
        return new ArrayList<>(updateDetailMap.values());
    }

    /**
     * 校验同一费用项同一费用类型的币种一致性，空币种也作为独立币种参与比较。
     */
    private void validateSameCostCurrency(List<TmsCostDetailDTO.UpdateDTO> updateDetailList,
                                          List<TmsCfgCostEntity> tmsCfgCostList,
                                          List<String> errorMsgList) {
        if (CollUtil.isEmpty(updateDetailList)) {
            return;
        }
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

    /**
     * 订单重量 = SKU毛重(g) * 上游出库单实发数量，多物流单匹配时作为费用分摊依据。
     */
    private Map<String, BigDecimal> buildOrderWeightMap(List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList, List<String> errorMsgList) {
        Map<String, BigDecimal> weightMap = new HashMap<>();
        List<String> outstockIdList = logisticsBillVoList.stream().map(LogisticsBillDTO.LogisticsBillVo::getOutstockId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (logisticsBillVoList.stream().anyMatch(vo -> CharSequenceUtil.isBlank(vo.getOutstockId()))) {
            errorMsgList.add("无法获取上游出库单用于重量分摊");
            return weightMap;
        }
        List<SoOutstockDetailEntity> outstockDetailList = FeignQuery.create(SoOutstockDetailEntity.class).in(SoOutstockDetailEntity::getMainId, outstockIdList).list();
        if (CollUtil.isEmpty(outstockDetailList)) {
            errorMsgList.add("无法获取上游出库明细用于重量分摊");
            return weightMap;
        }
        Map<String, List<SoOutstockDetailEntity>> outstockDetailMap = outstockDetailList.stream().collect(Collectors.groupingBy(SoOutstockDetailEntity::getMainId));
        List<String> skuIdList = outstockDetailList.stream().map(SoOutstockDetailEntity::getSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(skuIdList)) {
            errorMsgList.add("出库明细缺少SKU信息，无法按重量分摊");
            return weightMap;
        }
        List<ProductPackEntity> productPackList = FeignQuery.create(ProductPackEntity.class).in(ProductPackEntity::getSkuId, skuIdList).list();
        Map<String, ProductPackEntity> productPackMap = CollUtil.isEmpty(productPackList) ? new HashMap<>() : productPackList.stream().collect(Collectors.toMap(ProductPackEntity::getSkuId, obj -> obj, (first, second) -> first));
        for (LogisticsBillDTO.LogisticsBillVo logisticsBillVo : logisticsBillVoList) {
            List<SoOutstockDetailEntity> detailList = outstockDetailMap.get(logisticsBillVo.getOutstockId());
            if (CollUtil.isEmpty(detailList)) {
                errorMsgList.add("无法获取上游出库明细用于重量分摊：" + logisticsBillVo.getOutstockCode());
                continue;
            }
            BigDecimal orderWeight = BigDecimal.ZERO;
            for (SoOutstockDetailEntity detailEntity : detailList) {
                ProductPackEntity productPackEntity = productPackMap.get(detailEntity.getSkuId());
                if (ObjectUtil.isNull(productPackEntity) || ObjectUtil.isNull(productPackEntity.getGrossWeight()) || productPackEntity.getGrossWeight().compareTo(BigDecimal.ZERO) <= 0) {
                    errorMsgList.add("缺少SKU毛重，无法按重量分摊：" + detailEntity.getSkuNo());
                    continue;
                }
                if (ObjectUtil.isNull(detailEntity.getActualQty())) {
                    errorMsgList.add("出库实发数量为空，无法按重量分摊：" + detailEntity.getSkuNo());
                    continue;
                }
                orderWeight = orderWeight.add(productPackEntity.getGrossWeight().multiply(BigDecimal.valueOf(detailEntity.getActualQty())));
            }
            weightMap.put(logisticsBillVo.getDetailId(), orderWeight);
        }
        BigDecimal totalWeight = weightMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            errorMsgList.add("总订单重量为0，无法执行费用分摊");
        }
        return weightMap;
    }

    /**
     * 多物流单匹配时按订单重量占比分摊费用，最后一条补差，避免四舍五入误差。
     */
    private Map<String, List<TmsCostDetailDTO.UpdateDTO>> allocateCostDetailMap(List<TmsCostDetailDTO.UpdateDTO> updateList,
                                                                                List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList,
                                                                                Map<String, BigDecimal> weightMap) {
        Map<String, List<TmsCostDetailDTO.UpdateDTO>> resultMap = new LinkedHashMap<>();
        BigDecimal totalWeight = logisticsBillVoList.stream().map(vo -> weightMap.getOrDefault(vo.getDetailId(), BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add);
        for (TmsCostDetailDTO.UpdateDTO updateDTO : updateList) {
            BigDecimal allocatedSum = BigDecimal.ZERO;
            for (int i = 0; i < logisticsBillVoList.size(); i++) {
                LogisticsBillDTO.LogisticsBillVo logisticsBillVo = logisticsBillVoList.get(i);
                BigDecimal allocatedCost = i == logisticsBillVoList.size() - 1
                        ? updateDTO.getCostValue().subtract(allocatedSum)
                        : updateDTO.getCostValue().multiply(weightMap.getOrDefault(logisticsBillVo.getDetailId(), BigDecimal.ZERO)).divide(totalWeight, 4, RoundingMode.HALF_UP);
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
     * 校验导入费用和存量费用在同一费用分类下的币种一致性。
     */
    private void checkCategoryCurrency(List<TmsCostDetailDTO.UpdateDTO> updateList,
                                       LogisticsBillCostEntity logisticsBillCostEntity,
                                       List<TmsCfgCostEntity> cfgCostList,
                                       List<String> errorMsgList) {
        List<TmsCostDetailEntity> validateList = BeanMapperUtils.copyList(TmsCostDetailEntity.class, updateList);
        List<TmsCostDetailEntity> tmsCostDetailEntityList = tmsCostDetailService.listByMainIdList(Collections.singletonList(logisticsBillCostEntity.getId()));
        if(CollUtil.isNotEmpty(tmsCostDetailEntityList)) {
            List<String> cfgCostIds = validateList.stream().map(TmsCostDetailEntity::getCfgCostId).collect(Collectors.toList());
            validateList.addAll(tmsCostDetailEntityList.stream().filter(t -> !cfgCostIds.contains(t.getCfgCostId())).collect(Collectors.toList()));
        }
        Set<String> validateCategoryCurrency = tmsCostDetailService.validateCategoryCurrency(validateList);
        if (CollUtil.isEmpty(validateCategoryCurrency)) {
            return;
        }
        Map<String, String> costIdTypeListMap = new LinkedHashMap<>();
        for(String validateCategory : validateCategoryCurrency) {
            String[] split = validateCategory.split("_");
            List<UpdateDTO> removeList = updateList.stream().filter(u -> u.getDictCostCategory().equals(split[0]) && u.getType().equals(split[1])).collect(Collectors.toList());
            for(UpdateDTO remove : removeList) {
                String costName = cfgCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), remove.getCfgCostId())).findFirst().orElse(null).getCostName();
                costIdTypeListMap.put(costName, AllocationFeeTypeEnum.getName(split[0]) + "-" + LogisticsBillCostTypeEnum.getName(split[1]) + "分类下所有费用币种必须一致");
            }
        }
        errorMsgList.addAll(costIdTypeListMap.values());
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
                    addDTO.setOrderType(OrderTypeEnum.B2B.getCode());
                }
            } else if (excelDTO.getSoCode().startsWith("XSDS")) {
                List<SoB2cEntity> list = FeignQuery.create(SoB2cEntity.class).eq(SoB2cEntity::getCode, excelDTO.getSoCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addDTO.setSourceId(list.get(0).getId());
                    addDTO.setOrderType(OrderTypeEnum.B2C.getCode());
                }
            }else {
                addDTO.setOrderType(OrderTypeEnum.OTHER.getCode());
            }
        }
        //订单类型默认其他
        if (CharSequenceUtil.isBlank(addDTO.getOrderType())) {
            addDTO.setOrderType(OrderTypeEnum.OTHER.getCode());
        }

        addDTO.setSourceCode(excelDTO.getSoCode());
        addDTO.setPlatformCode(excelDTO.getPlatformCode());
        addDTO.setSoDeliveryCode(excelDTO.getSoDeliveryCode());

        LogisticsBillDetailDTO.AddDTO addDetailDTO = new LogisticsBillDetailDTO.AddDTO();
        addDetailDTO.setTrackNo(excelDTO.getTrackNo());
        addDetailDTO.setTrackEnable(Boolean.FALSE);
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
    private LogisticsBillCostDTO.UpdateDTO handleLogisticsBillCostImportData(LogisticsBillCostEntity logisticsBillCostEntity,
                                                                             LogisticsLastMileCostExcelDTO excelDTO,
                                                                             List<TmsCostDetailDTO.UpdateDTO> updateList) {
        // 费用主表导入字段统一在这里组装，新增和更新两种导入类型共用同一套格式化规则。
        LogisticsBillCostDTO.UpdateDTO updateDataDTO = new LogisticsBillCostDTO.UpdateDTO();
        updateDataDTO.setId(logisticsBillCostEntity.getId());
        updateDataDTO.setBillingWeightLogistics(new BigDecimal(excelDTO.getBillingWeightStr()));
        updateDataDTO.setCurrency(CharSequenceUtil.isBlank(excelDTO.getCurrency()) ? CurrencyEnum.CNY.getCurrencyCode() : excelDTO.getCurrency());
        updateDataDTO.setPayType(excelDTO.getPayType());

        // 物流商账单尺寸和实重用于对账展示，不参与本方法中的费用计算。
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
        return errorMsgList;
    }

    @Override
    public Boolean exportExcel(LogisticsBillCostDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("尾程费用(平台发货)", EXPORT_TMS_LOGISTICS_LAST_MILE_COST.getCode(), dto);
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
        downloadTaskFeign.saveImportTask("尾程费用(平台发货)", IMPORT_TMS_LOGISTICS_LAST_MILE_COST.getCode(), dto);
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
