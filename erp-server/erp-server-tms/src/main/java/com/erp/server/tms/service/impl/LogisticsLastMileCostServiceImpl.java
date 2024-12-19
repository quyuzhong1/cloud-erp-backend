package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.dto.excel.LogisticsLastMileCostExcelDTO;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.TmsCfgCostEntity;
import com.erp.model.tms.enums.DictCostAttributionEnum;
import com.erp.model.tms.enums.LogisticsBillCostTypeEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.listener.LogisticsLastMileCostExcelListener;
import com.erp.server.tms.mapper.LogisticsBillCostMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_LAST_MILE_COST;

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
    public Boolean update(LogisticsBillCostDTO.UpdateDTO dto, Boolean isImport) {
       return logisticsBillCostService.update(dto,isImport);
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
            throw new ServiceException(ApiError.ERROR_CFG_COST_EMPTY,"尾程");
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
        headerNameList.add("*平台订单号");
        headerNameList.add("*物流跟踪单号");
        headerNameList.add("*计费重[物流商]");
        headerNameList.add("*对账类型");
        headerNameList.add("币种[默认￥]");
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
        jsonObject.set("*平台订单号","platformCode");
        jsonObject.set("*物流跟踪单号","trackNo");
        jsonObject.set("*计费重[物流商]","billingWeightStr");
        jsonObject.set("*对账类型","payType");
        jsonObject.set("币种[默认￥]","currency");
        return jsonObject;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        LogisticsLastMileCostExcelListener excelListenerUtil = new LogisticsLastMileCostExcelListener();
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
        //导出错误数据
        List<JSONObject> errorList = excelListenerUtil.getErrorList();

        //处理验证成功数据
        handleImportSuccessList(excelListenerUtil, errorList);

        if (errorList.size() > 0) {
            String fileName = "尾程费用错误数据.xlsx";
            ExcelUtil.customExportUtil(excelListenerUtil.getHeadList(), errorList, fileName, response);
        }
        return Boolean.TRUE;
    }

    /**
     * @description: 导入数据处理
     * @author Will
     * @date: 2024/5/10 18:41
     * @param excelListenerUtil
     * @param errorList
     */
    private void handleImportSuccessList (LogisticsLastMileCostExcelListener excelListenerUtil, List<JSONObject> errorList) {

        //导入数据处理
        List<JSONObject> successList = excelListenerUtil.getSuccessList();
        //表头
        List<String> headList = excelListenerUtil.getHeadList();
        if (headList.size() != headList.stream().distinct().collect(Collectors.toList()).size()) {
            throw new ServiceException(ApiError.ERROR_EXCEL_IMPORT_HEAD_EXIST);
        }
        //表头Map
        Map<Integer,String> headMap = excelListenerUtil.getHeadMap();

        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //获取尾程费用配置信息
        List<TmsCfgCostEntity> cfgCostList = tmsCfgCostService.listByCostAttribution(DictCostAttributionEnum.LAST_MILE.getCode());
        //表头对应json
        JSONObject headerNameJsonObject = getHeaderNameJsonObject();
        //错误信息序号
        Integer errorIndex = getMapKey(headMap, "错误信息");
        //平台订单号序号
        Integer orderIndex = getMapKey(headMap,"*平台订单号");
        //物流跟踪单号序号
        Integer trackNoIndex = getMapKey(headMap,"*物流跟踪单号");
        //平台订单号
        List<String> platformCodeList = ObjectUtil.isEmpty(orderIndex) ? new ArrayList<>() : successList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.get(orderIndex.toString())))
                .map(obj -> obj.get(orderIndex.toString()).toString()).distinct().collect(Collectors.toList());
        //物流跟踪号
        List<String> trackNoList = ObjectUtil.isEmpty(trackNoIndex) ? new ArrayList<>() :  successList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.get(trackNoIndex.toString())))
                .map(obj -> obj.get(trackNoIndex.toString()).toString()).distinct().collect(Collectors.toList());

        //物流明细信息
        List<LogisticsBillDetailEntity> logisticsBillDetailList = logisticsBillDetailService.listByPlatformCodeAndTrackNo(platformCodeList,trackNoList);

        //物流费用信息
        List<String> logisticsBillDetailIdList = logisticsBillDetailList.stream().map(LogisticsBillDetailEntity::getId).distinct().collect(Collectors.toList());
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
            String payType = excelDTO.getPayType();
            if("付款".equals(payType)) {
            	payType = "pay";
            }else if("退款".equals(payType)){
            	payType = "refund";
            }
            excelDTO.setPayType(payType);
            //数据验证
            List<String> importMsgList = checkImportData(excelDTO,logisticsBillCostList,logisticsBillDetailList,DictCostAttributionEnum.LAST_MILE.getCode());
            if (CollectionUtils.isNotEmpty(importMsgList)) {
                errorMsgList.addAll(importMsgList);
            }
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                jsonObject.set(errorIndex.toString(),FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(jsonObject);
                continue;
            }
            //物流费用单
            LogisticsBillDetailEntity logisticsBillDetailEntity = logisticsBillDetailList.stream().filter(obj -> obj.getPlatformCode().equals(excelDTO.getPlatformCode())
                            && CharSequenceUtil.equals(obj.getTrackNo(),excelDTO.getTrackNo()))
                    .findFirst().orElse(null);
            //物流费用单
            LogisticsBillCostEntity logisticsBillCostEntity = logisticsBillCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getLogisticsBillDetailId(),logisticsBillDetailEntity.getId())
            		&& CharSequenceUtil.equals(obj.getPayType(),excelDTO.getPayType()))
                    .findFirst().orElse(null);

            //数据赋值
            LogisticsBillCostDTO.UpdateDTO updateDataDTO = new LogisticsBillCostDTO.UpdateDTO();
            updateDataDTO.setId(logisticsBillCostEntity.getId());
            updateDataDTO.setBillingWeightLogistics(new BigDecimal(excelDTO.getBillingWeightStr()));
            updateDataDTO.setCurrency(CharSequenceUtil.isBlank(excelDTO.getCurrency()) ? CurrencyEnum.CNY.getCurrencyCode() : excelDTO.getCurrency());
            updateDataDTO.setCostDetailList(updateList);
            this.update(updateDataDTO,Boolean.TRUE);
        }
    }

    /**
     * @description: 导入数据处理
     * @author Will
     * @date: 2024/5/11 14:24
     * @param excelDTO
     * @param logisticsBillCostList
     * @param logisticsBillDetailList
     * @param dictCostAttribution
     * @return List<String>
     */
    private List<String> checkImportData (LogisticsLastMileCostExcelDTO excelDTO
            , List<LogisticsBillCostEntity> logisticsBillCostList, List<LogisticsBillDetailEntity> logisticsBillDetailList , String dictCostAttribution) {
        List<String> errorMsgList = new ArrayList<>();
        //物流单明细
        LogisticsBillDetailEntity detailEntity = logisticsBillDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getPlatformCode(), excelDTO.getPlatformCode())
                && CharSequenceUtil.equals(excelDTO.getTrackNo(), obj.getTrackNo())).findFirst().orElse(null);
        if (ObjectUtil.isEmpty(detailEntity)) {
            errorMsgList.add("未找到出库单和物流跟踪单号对应的物流单明细");
            return errorMsgList;
        }
        //物流费用单
        List<LogisticsBillCostEntity> logisticsBillCostEntityList = logisticsBillCostList.stream().filter(obj ->
                CharSequenceUtil.equals(detailEntity.getId(),obj.getLogisticsBillDetailId()) 
                && CharSequenceUtil.equals(excelDTO.getPayType(),obj.getPayType()))
        		.collect(Collectors.toList());
        if (CollUtil.isEmpty(logisticsBillCostEntityList)) {
            errorMsgList.add("未找到出库单和运输单号对应对账类型的尾程费用单");
            return errorMsgList;
        }
        if (logisticsBillCostEntityList.size() > 1) {
            errorMsgList.add("出库单和运输单号对应对账类型的尾程费用单有多条，请在页面编辑指定物流费用单");
            return errorMsgList;
        }
        LogisticsBillCostEntity logisticsBillCostEntity = logisticsBillCostEntityList.get(0);
        if (!CharSequenceUtil.equals(logisticsBillCostEntity.getType(),dictCostAttribution)) {
            errorMsgList.add(CharSequenceUtil.format("需要导入【{}】尾程费用信息",DictCostAttributionEnum.getName(dictCostAttribution)));
        }
        //币别为空则取费用单币别
        excelDTO.setCurrency(CharSequenceUtil.isBlank(excelDTO.getCurrency()) ? logisticsBillCostEntity.getCurrency() : excelDTO.getCurrency());
        if (ObjectUtil.isNotEmpty(logisticsBillCostEntity) && !CharSequenceUtil.equals(excelDTO.getCurrency(),logisticsBillCostEntity.getCurrency())) {
            errorMsgList.add("导入币别与尾程费用单币别不一致");
        }
        if (ReconciliationStatusEnum.CONFIRMED.getCode().equals(logisticsBillCostEntity.getReconciliationStatus())
                || ReconciliationStatusEnum.INVALID.getCode().equals(logisticsBillCostEntity.getReconciliationStatus())) {
            errorMsgList.add("尾程费用单已确认或已作废不支持更新");
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
        Page<LogisticsBillCostDTO.ListDTO> page = logisticsBillCostMapper.listByExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            //数据赋值处理
            logisticsBillCostService.handleDataPaging(page.getRecords());
        }
        return new PagingVO<>(page);
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
