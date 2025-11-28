package com.erp.server.oms.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.DeliveryBoxRuleDTO;
import com.erp.model.oms.dto.DeliveryBoxRuleDetailDTO;
import com.erp.model.oms.dto.excel.DeliveryBoxRuleImportExcelDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.oms.service.DeliveryBoxRuleService;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: wtr
 * @Date: 2025/11/27 17:17
 * @Param:
 * @Return:
 * @Description:
 **/
public class DeliveryBoxRuleExcelListener extends AnalysisEventListener<DeliveryBoxRuleImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;


    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<DeliveryBoxRuleImportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private List<DeliveryBoxRuleImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private List<DeliveryBoxRuleDTO.ImportDTO> successList = new ArrayList<>(BATCH_COUNT);

    /**
     * sku列表
     */
    private List<SkuVO> skuList;

    private final DeliveryBoxRuleService deliveryBoxRuleService = SpringUtil.getBean(DeliveryBoxRuleService.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    public DeliveryBoxRuleExcelListener(String taskId,
                                    String importType,
                                    Integer importCount,
                                    List<SkuVO> skuList) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
        this.skuList = skuList;
    }
    // 在类级别添加一个Map来按serialNumber分组存储detail数据
    Map<String, DeliveryBoxRuleDTO.ImportDTO> excelDTOMap = new HashMap<>();

    // 负责校验sku和发货sku是否重复
    Set<String> skuDeliverySkuPairSet = new HashSet<>();

    // 负责校验相同sku下，sort是否重复
    Set<String> skuSortSet = new HashSet<>();

    @Override
    public void invoke(DeliveryBoxRuleImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
        count += 1;
        //已经导入的数据跳过进度
        if (Objects.nonNull(importCount) && count < importCount){
            return;
        }

        // 添加数据用于判断是否为空
        allList.add(importExcelDTO);

        // 验证数据
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(importExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        // 如果存在错误，记录错误并返回
        if (errorMsgList.size() > 0) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }

        // 检查serialNumber是否已存在
        String skuNo = importExcelDTO.getSkuNo();
        DeliveryBoxRuleDTO.ImportDTO excelDTO;

        if (excelDTOMap.containsKey(skuNo)) {
            // 如果已存在，获取现有的excelDTO
            excelDTO = excelDTOMap.get(skuNo);
        } else {
            // 如果不存在，创建新的excelDTO并设置公共字段
            excelDTO = new DeliveryBoxRuleDTO.ImportDTO();
            if (CollectionUtils.isEmpty(skuList)) {
                errorMsgList.add("系统中未发现已启用的sku信息");
            } else {
                if (StringUtils.isNotBlank(importExcelDTO.getSkuNo())) {
                    SkuVO skuVO = skuList.stream()
                            .filter(obj -> obj.getSkuNo().equals(importExcelDTO.getSkuNo()))
                            .findFirst()
                            .orElse(null);
                    if (ObjectUtils.isEmpty(skuVO)) {
                        errorMsgList.add("请录入启用的sku信息");
                    } else {
                        excelDTO.setSkuId(skuVO.getSkuId());
                        excelDTO.setSkuNo(skuVO.getSkuNo());
                        excelDTO.setProductName(skuVO.getSkuName());
                    }
                }
            }

            // 初始化detailList
            excelDTO.setDetailImportDTOList(new ArrayList<>());
            excelDTOMap.put(skuNo, excelDTO);
        }

        // 创建新的detail并设置属性
        DeliveryBoxRuleDetailDTO.DetailImportDTO detail = new DeliveryBoxRuleDetailDTO.DetailImportDTO();

        // 发货sku
        if (CollectionUtils.isEmpty(skuList)) {
            errorMsgList.add("系统中未发现已启用的 SKU 信息");
        } else {
            String deliverySkuNo = importExcelDTO.getDeliverySkuNo();
            SkuVO deliverySkuVO = skuList.stream()
                    .filter(obj -> obj.getSkuNo().equals(deliverySkuNo))
                    .findFirst()
                    .orElse(null);

            if (ObjectUtils.isEmpty(deliverySkuVO)) {
                errorMsgList.add("请录入启用的发货 SKU 信息");
            } else {
                // 校验 SKU 和发货 SKU 是否相同
                if (skuNo.equals(deliverySkuNo)) {
                    errorMsgList.add("主 SKU 和发货 SKU 不能相同");
                }

                // 校验 SKU + 发货 SKU 是否重复
                String pairKey = excelDTO.getSkuId() + "_" + deliverySkuVO.getSkuId();
                if (skuDeliverySkuPairSet.contains(pairKey)) {
                    errorMsgList.add("SKU [" + skuNo + "] ,发货 SKU [" + deliverySkuNo + "]在文件中重复，请勿重复录入");
                } else {
                    skuDeliverySkuPairSet.add(pairKey);
                    detail.setDeliverySkuId(deliverySkuVO.getSkuId());
                    detail.setDeliverySkuNo(deliverySkuVO.getSkuNo());
                    detail.setDeliveryProductName(deliverySkuVO.getSkuName());
                }
            }
        }

        // 优先级
        if (isPositiveInteger(importExcelDTO.getSort())) {
            String pairKey = excelDTO.getSkuId() + "_" + importExcelDTO.getSort();
            if (skuSortSet.contains(pairKey)) {
                errorMsgList.add("SKU [" + skuNo + "] ,优先级 [" + importExcelDTO.getSort() + "]在文件中重复，请勿重复录入");
            } else {
                skuSortSet.add(pairKey);
                detail.setSort(Integer.parseInt(importExcelDTO.getSort()));
            }
        } else {
            errorMsgList.add("优先级需要是正整数");
        }

        // 发货箱规
        if (isPositiveInteger(importExcelDTO.getPerBoxQty())) {
            detail.setPerBoxQty(Integer.parseInt(importExcelDTO.getPerBoxQty()));
        } else {
            errorMsgList.add("发货箱规需要是正整数");
        }

        // 存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }

        excelDTO.getDetailImportDTOList().add(detail);

    }

    public static boolean isPositiveInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        // 检查第一位是否为1-9
        if (str.charAt(0) < '1' || str.charAt(0) > '9') {
            return false;
        }
        // 检查剩余字符是否为数字
        for (int i = 1; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private void checkSortDuplicate(List<DeliveryBoxRuleDetailDTO.DetailImportDTO> detail) {
        Map<Integer, Long> sortCountMap = detail.stream()
                .collect(Collectors.groupingBy(
                        DeliveryBoxRuleDetailDTO.DetailImportDTO::getSort,
                        Collectors.counting()
                ));

        List<Integer> duplicateSorts = sortCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(duplicateSorts)) {
            throw new ServiceException(
                    ApiError.ERROR_DUPLICATE_SORT,
                    duplicateSorts
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        successList.addAll(excelDTOMap.values());
        if (!successList.isEmpty()){
            try {
                deliveryBoxRuleService.handleImportSuccessList(successList);
            }catch (Exception e){
                errorList.forEach(excelDTO -> excelDTO.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
            }
            updateTask(count);
        }
    }

    public List<DeliveryBoxRuleImportExcelDTO> getAllList(){
        return allList;
    }

    public List<DeliveryBoxRuleImportExcelDTO> getErrorList(){
        return errorList;
    }

    public List<DeliveryBoxRuleDTO.ImportDTO> getSuccessList(){
        return successList;
    }

    private void updateTask(Integer count){
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(taskId);
        importResultDTO.setStatus(FileTaskStatusEnum.PROCESS.getCode());
        importResultDTO.setRemark("处理中");
        importResultDTO.setCount(count);
        downloadTaskFeign.updateTask(importResultDTO);
    }
}
