package com.erp.server.scm.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.scm.dto.AssetPurchaseOrderDetailDTO;
import com.erp.model.scm.dto.excel.AssetPurchaseOrderImportExcelDTO;
import com.erp.model.scm.entity.AssetNoticeEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.SupplierAccountEntity;
import com.erp.model.scm.entity.SupplierContactEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.scm.service.AssetPurchaseOrderService;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: wtr
 * @Date: 2025/10/24 19:00
 * @Param:
 * @Return:
 * @Description:
 **/
public class AssetPurchaseOrderExcelListener extends AnalysisEventListener<AssetPurchaseOrderImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;

    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<AssetPurchaseOrderImportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private List<AssetPurchaseOrderImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private List<AssetPurchaseOrderDetailDTO.MoldImportDTO> successList = new ArrayList<>(BATCH_COUNT);

    /**
     * 模具数据
     */
    private List<SkuVO> skuList;

    /**
     * 核算公司
     */
    private List<BaseIdDTO> companyList;

    /**
     * 用户
     */
    List<FindUserDTO> userList;

    /**
     * 部门
     */
    List<SysDepartmentDTO> deptList;

    /**
     * 资产通知单
     */
    List<AssetNoticeEntity> assetNoticeEntityList;

    /**
     * 结算方式
     */
    Map<String, String> settleDictMap;

    /**
     * 付款条件
     */
    Map<String, String> paymentConditionMap;

    /**
     * 供应商信息
     */
    List<SupplierEntity> supplierEntityList;

    /**
     * 供应商联系人信息
     */
    List<SupplierContactEntity> supplierContactEntityList;

    /**
     * 供应商账户信息
     */
    List<SupplierAccountEntity> supplierAccountEntityList;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter dateTimeFormatter2 = DateTimeFormatter.ofPattern("yyyy/M/d");
    private final DateTimeFormatter dateTimeFormatter3 = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final AssetPurchaseOrderService assetPurchaseOrderService = SpringUtil.getBean(AssetPurchaseOrderService.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);


    public AssetPurchaseOrderExcelListener(String taskId,
                                           String importType,
                                           Integer importCount,
                                           List<SkuVO> skuList,
                                           List<FindUserDTO> userList,
                                           List<SysDepartmentDTO> deptList,
                                           List<BaseIdDTO> companyList,
                                           List<AssetNoticeEntity> assetNoticeEntityList,
                                           Map<String, String> settleDictMap,
                                           Map<String, String> paymentConditionMap,
                                           List<SupplierEntity> supplierEntityList,
                                           List<SupplierContactEntity> supplierContactEntityList,
                                           List<SupplierAccountEntity> supplierAccountEntityList) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
        this.skuList = skuList;
        this.userList = userList;
        this.deptList = deptList;
        this.companyList = companyList;
        this.assetNoticeEntityList = assetNoticeEntityList;
        this.settleDictMap = settleDictMap;
        this.paymentConditionMap = paymentConditionMap;
        this.supplierEntityList = supplierEntityList;
        this.supplierContactEntityList = supplierContactEntityList;
        this.supplierAccountEntityList = supplierAccountEntityList;
    }
    // 在类级别添加一个Map来按serialNumber分组存储detail数据
    Map<String, AssetPurchaseOrderDetailDTO.MoldImportDTO> excelDTOMap = new HashMap<>();

    @Override
    public void invoke(AssetPurchaseOrderImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
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
        String serialNumber = importExcelDTO.getSerialNumber(); // 假设DTO中有getSerialNumber方法
        AssetPurchaseOrderDetailDTO.MoldImportDTO excelDTO;

        if (excelDTOMap.containsKey(serialNumber)) {
            // 如果已存在，获取现有的excelDTO
            excelDTO = excelDTOMap.get(serialNumber);
        } else {
            // 如果不存在，创建新的excelDTO并设置公共字段
            excelDTO = new AssetPurchaseOrderDetailDTO.MoldImportDTO();

            excelDTO.setSourceType(SourceTypeEnum.SELF_ADD.getCode());
            excelDTO.setPurchaseDate(parseDate(importExcelDTO.getApplyDate()));
            excelDTO.setSerialNumber(importExcelDTO.getSerialNumber());
            //来源单信息
            if (!assetNoticeEntityList.isEmpty()) {
                AssetNoticeEntity assetNoticeEntity = assetNoticeEntityList.stream()
                        .filter(obj -> obj.getCode().equals(importExcelDTO.getAssetNoticeCode()))
                        .findFirst()
                        .orElse(null);
                if (!Objects.nonNull(assetNoticeEntity)) {
                    errorMsgList.add("请先录入开模通知单");
                } else {
                    excelDTO.setSourceId(assetNoticeEntity.getId());
                    excelDTO.setSourceCode(assetNoticeEntity.getCode());
                    excelDTO.setSourceType(SourceTypeEnum.ASSET_NOTICE.getCode());
                }
            }

            // 采购员
            if (!userList.isEmpty()) {
                FindUserDTO findUserDTO = userList.stream()
                        .filter(obj -> obj.getUserName().equals(importExcelDTO.getPurchaseUserName()))
                        .findFirst()
                        .orElse(null);
                if (!Objects.nonNull(findUserDTO)) {
                    errorMsgList.add("请录入申请人信息");
                } else {
                    excelDTO.setPurchaseUserId(findUserDTO.getUserId());
                    excelDTO.setPurchaseUserId(findUserDTO.getUserName());
                }
            }

            // 采购部门
            if (!deptList.isEmpty() && StringUtils.isNotBlank(importExcelDTO.getPurchaseDeptName())) {
                SysDepartmentDTO sysDepartmentDTO = deptList.stream()
                        .filter(obj -> obj.getName().equals(importExcelDTO.getPurchaseDeptName()))
                        .findFirst()
                        .orElse(null);
                if (ObjectUtils.isEmpty(sysDepartmentDTO)) {
                    errorMsgList.add("请录入申请部门信息");
                } else {
                    excelDTO.setPurchaseDeptId(sysDepartmentDTO.getId());
                    excelDTO.setPurchaseDeptName(sysDepartmentDTO.getName());
                }
            }

            // 采购组织
            if (CollectionUtils.isEmpty(companyList)) {
                errorMsgList.add("系统中未发现已启用的采购组织");
            } else {
                if (StringUtils.isNotBlank(importExcelDTO.getPurchaseOrgName())) {
                    BaseIdDTO baseIdDTO = companyList.stream()
                            .filter(obj -> obj.getName().equals(importExcelDTO.getPurchaseOrgName()))
                            .findFirst()
                            .orElse(null);
                    if (ObjectUtils.isEmpty(baseIdDTO)) {
                        errorMsgList.add("请录入启用采购组织");
                    } else {
                        excelDTO.setPurchaseOrgId(baseIdDTO.getId());
                        excelDTO.setPurchaseOrgName(importExcelDTO.getPurchaseOrgName());
                    }
                }
            }

            //供应商信息
            AssetPurchaseOrderDetailDTO.SupplierImportDTO supplierImportDTO = new AssetPurchaseOrderDetailDTO.SupplierImportDTO();
            SupplierEntity supplierEntity = new SupplierEntity();

            if (CollectionUtils.isEmpty(supplierEntityList)) {
                errorMsgList.add("系统中未发现供应商信息");
            } else {
                if (StringUtils.isNotBlank(importExcelDTO.getSupplierName())) {
                    supplierEntity = supplierEntityList.stream()
                            .filter(obj -> Objects.equals(obj.getName(), importExcelDTO.getSupplierName()))
                            .findFirst()
                            .orElse(null);
                    if (ObjectUtils.isEmpty(supplierEntity)) {
                        errorMsgList.add("请录入供应商信息");
                    } else {
                        supplierImportDTO.setSupplierId(supplierEntity.getId());
                        supplierImportDTO.setSupplierName(importExcelDTO.getSupplierName().trim());
                    }
                }
            }

            //供应商联系人
            if (StringUtils.isNotBlank(importExcelDTO.getPaymentConditionName())) {
                if (CollectionUtils.isEmpty(supplierContactEntityList)) {
                    errorMsgList.add("系统中未发现供应商联系人信息");
                } else {
                    //根据供应商名称获取联系人
                    if (StringUtils.isNotBlank(importExcelDTO.getSupplierName()) && Objects.nonNull(supplierEntity)) {
                        SupplierEntity finalSupplierEntity = supplierEntity;
                        List<SupplierContactEntity> collect = supplierContactEntityList.stream()
                                .filter(obj -> obj.getSupplierId().equals(finalSupplierEntity.getId()))
                                .collect(Collectors.toList());
                        if (ObjectUtils.isEmpty(collect)) {
                            errorMsgList.add("请录入供应商联系人信息");
                        } else {
                            for (SupplierContactEntity supplierContactEntity : collect) {
                                supplierImportDTO.setPayCurrency(supplierEntity.getPayCurrency());
                                if (supplierContactEntity.getPerson().equals(importExcelDTO.getContactName())) {
                                    supplierImportDTO.setContactId(supplierContactEntity.getId());
                                    supplierImportDTO.setContactName(supplierContactEntity.getPerson());
                                    if (StringUtils.isNotBlank(importExcelDTO.getContactTelNumber())) {
                                        supplierImportDTO.setContactTelNumber(importExcelDTO.getContactTelNumber());
                                    }else {
                                        importExcelDTO.setContactTelNumber(supplierContactEntity.getTelNumber());
                                    }
                                }
                            }

                        }
                    }
                }
            }

            //结算方式
            if (StringUtils.isNotBlank(importExcelDTO.getPayMethodName())) {
                if (settleDictMap.isEmpty()) {
                    errorMsgList.add("系统中未发现结算方式信息");
                } else {
                    supplierImportDTO.setPayMethodId(settleDictMap.get(importExcelDTO.getPayMethodName()));
                    supplierImportDTO.setPayMethodName(importExcelDTO.getPayMethodName());
                }
            }

            //付款条件
            if (StringUtils.isNotBlank(importExcelDTO.getPaymentConditionName())) {
                if (paymentConditionMap.isEmpty()) {
                    errorMsgList.add("系统中未发现付款条件信息");
                } else {
                    supplierImportDTO.setPaymentCondition(paymentConditionMap.get(importExcelDTO.getPaymentConditionName()));
                    supplierImportDTO.setPaymentConditionName(importExcelDTO.getPaymentConditionName());
                }
            }


            //账户名称
            if (StringUtils.isNotBlank(importExcelDTO.getPayee())) {
                if (CollectionUtils.isEmpty(supplierAccountEntityList)) {
                    errorMsgList.add("系统中未发现供应商账户信息");
                } else {
                    //根据供应商名称获取账户信息
                    if (StringUtils.isNotBlank(importExcelDTO.getSupplierName()) && Objects.nonNull(supplierEntity)) {
                        SupplierEntity finalSupplierEntity = supplierEntity;
                        List<SupplierAccountEntity> collect = supplierAccountEntityList.stream()
                                .filter(obj -> obj.getSupplierId().equals(finalSupplierEntity.getId()))
                                .collect(Collectors.toList());
                        if (ObjectUtils.isEmpty(collect)) {
                            errorMsgList.add("请录入供应商账户信息");
                        } else {
                            SupplierAccountEntity supplierAccountEntity = collect.stream()
                                    .filter(obj -> Boolean.TRUE.equals(obj.getIsDefault()))
                                    .findFirst()
                                    .orElse(collect.stream().findFirst().orElse(null));
                            if (Objects.nonNull(supplierAccountEntity)) {
                                supplierImportDTO.setPayee(supplierAccountEntity.getPayee());
                                supplierImportDTO.setBankAccount(supplierAccountEntity.getBankAccount());
                                supplierImportDTO.setBankName(supplierAccountEntity.getBankName());
                            }
                        }
                    }
                }
            }

            excelDTO.setSupplierImportDTO(supplierImportDTO);

            // 初始化detailList
            excelDTO.setMoldDetailImportDTOList(new ArrayList<>());
            excelDTOMap.put(serialNumber, excelDTO);
        }

        // 创建新的detail并设置属性
        AssetPurchaseOrderDetailDTO.MoldDetailImportDTO detail = new AssetPurchaseOrderDetailDTO.MoldDetailImportDTO();

        // sku信息
        if (CollectionUtils.isEmpty(skuList)) {
            errorMsgList.add("系统中未发现已启用的sku信息");
        } else {
            if (StringUtils.isNotBlank(importExcelDTO.getAssetCode())) {
                SkuVO skuVO = skuList.stream()
                        .filter(obj -> obj.getSkuNo().equals(importExcelDTO.getAssetCode()))
                        .findFirst()
                        .orElse(null);
                if (ObjectUtils.isEmpty(skuVO)) {
                    errorMsgList.add("请录入启用的sku信息");
                } else {
                    detail.setAssetId(skuVO.getSkuId());
                    detail.setAssetCode(skuVO.getSkuNo());
                    detail.setAssetName(skuVO.getSkuName());
                }
            }
        }

        // 存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }

        // 设置detail的其他属性
        detail.setPlanDeliveryDate(parseDate(importExcelDTO.getPlanDeliveryDateStr()));
        detail.setPurchaseQty(new BigDecimal(importExcelDTO.getPurchaseQtyStr()));
        detail.setIsUrgent(importExcelDTO.getIsUrgentName().equals("是") ? Boolean.TRUE : Boolean.FALSE);
        detail.setRemark(importExcelDTO.getRemark());

        // 将detail添加到对应的excelDTO的detailList中
        excelDTO.getMoldDetailImportDTOList().add(detail);
    }



    private LocalDate parseDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, dateTimeFormatter);
        } catch (Exception e1) {
            try {
                return LocalDate.parse(dateStr, dateTimeFormatter2);
            } catch (Exception e2) {
                try {
                    return LocalDate.parse(dateStr, dateTimeFormatter3);
                } catch (Exception e3) {
                    throw new RuntimeException("日期格式错误，请使用 yyyy-MM-dd、yyyy/M/d 或 yyyy/MM/dd 格式");
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        successList.addAll(excelDTOMap.values());
        if (!successList.isEmpty()){
            try {
                assetPurchaseOrderService.handleImportSuccessList(successList);
            }catch (Exception e){
                errorList.forEach(excelDTO -> excelDTO.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
            }
        }
    }

    public List<AssetPurchaseOrderImportExcelDTO> getAllList(){
        return allList;
    }

    public List<AssetPurchaseOrderImportExcelDTO> getErrorList(){
        return errorList;
    }

    public List<AssetPurchaseOrderDetailDTO.MoldImportDTO> getSuccessList(){
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