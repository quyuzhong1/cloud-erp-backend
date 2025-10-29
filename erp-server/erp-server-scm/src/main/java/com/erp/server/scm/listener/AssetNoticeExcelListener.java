package com.erp.server.scm.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.scm.dto.AssetNoticeDetailDTO;
import com.erp.model.scm.dto.excel.AssetNoticeImportExcelDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.server.scm.service.AssetNoticeService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Author: wtr
 * @Date: 2025/10/20 11:50
 * @Param:
 * @Return:
 * @Description:
 **/
public class AssetNoticeExcelListener extends AnalysisEventListener<AssetNoticeImportExcelDTO> {


    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<AssetNoticeImportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private List<AssetNoticeImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private List<AssetNoticeDetailDTO.MoldImportDTO> successList = new ArrayList<>();

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

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter dateTimeFormatter2 = DateTimeFormatter.ofPattern("yyyy/M/d");
    private final DateTimeFormatter dateTimeFormatter3 = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final AssetNoticeService assetNoticeService = SpringUtil.getBean(AssetNoticeService.class);

    public AssetNoticeExcelListener(List<SkuVO> skuList, List<FindUserDTO> userList, List<SysDepartmentDTO> deptList, List<BaseIdDTO> companyList) {
        this.skuList = skuList;
        this.userList = userList;
        this.deptList = deptList;
        this.companyList = companyList;
    }
    // 在类级别添加一个Map来按serialNumber分组存储detail数据
    Map<String, AssetNoticeDetailDTO.MoldImportDTO> excelDTOMap = new HashMap<>();

    @Override
    public void invoke(AssetNoticeImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
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
        AssetNoticeDetailDTO.MoldImportDTO excelDTO;

        if (excelDTOMap.containsKey(serialNumber)) {
            // 如果已存在，获取现有的excelDTO
            excelDTO = excelDTOMap.get(serialNumber);
        } else {
            // 如果不存在，创建新的excelDTO并设置公共字段
            excelDTO = new AssetNoticeDetailDTO.MoldImportDTO();
            excelDTO.setApplyDate(parseDate(importExcelDTO.getApplyDate()));
            excelDTO.setSerialNumber(importExcelDTO.getSerialNumber());

            // 申请人
            if (!userList.isEmpty()) {
                FindUserDTO findUserDTO = userList.stream()
                        .filter(obj -> obj.getUserName().equals(importExcelDTO.getApplyUserName()))
                        .findFirst()
                        .orElse(null);
                if (!Objects.nonNull(findUserDTO)) {
                    errorMsgList.add("请录入申请人信息");
                } else {
                    excelDTO.setApplyUserId(findUserDTO.getUserId());
                    excelDTO.setApplyUserName(findUserDTO.getUserName());
                }
            }

            // 申请部门
            if (!deptList.isEmpty()) {
                SysDepartmentDTO sysDepartmentDTO = deptList.stream()
                        .filter(obj -> obj.getName().equals(importExcelDTO.getApplyDeptName()))
                        .findFirst()
                        .orElse(null);
                if (ObjectUtils.isEmpty(sysDepartmentDTO)) {
                    errorMsgList.add("请录入申请部门信息");
                } else {
                    excelDTO.setApplyDeptId(sysDepartmentDTO.getId());
                    excelDTO.setApplyDeptName(sysDepartmentDTO.getName());
                }
            }

            // 初始化detailList
            excelDTO.setMoldDetailImportDTOList(new ArrayList<>());
            excelDTOMap.put(serialNumber, excelDTO);
        }

        // 创建新的detail并设置属性
        AssetNoticeDetailDTO.MoldDetailImportDTO detail = new AssetNoticeDetailDTO.MoldDetailImportDTO();

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
                    detail.setPurchaseOrgId(baseIdDTO.getId());
                    detail.setPurchaseOrgName(importExcelDTO.getPurchaseOrgName());
                }
            }
        }

        // 模具信息
        if (CollectionUtils.isEmpty(skuList)) {
            errorMsgList.add("系统中未发现已启用的模具信息");
        } else {
            if (StringUtils.isNotBlank(importExcelDTO.getAssertCode())) {
                SkuVO skuVO = skuList.stream()
                        .filter(obj -> obj.getSkuNo().equals(importExcelDTO.getAssertCode()))
                        .findFirst()
                        .orElse(null);
                if (ObjectUtils.isEmpty(skuVO)) {
                    errorMsgList.add("请录入启用的模具信息");
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
        detail.setApplyQty(new BigDecimal(importExcelDTO.getApplyQtyStr()));
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
                assetNoticeService.handleImportSuccessList(successList);
            }catch (Exception e){
                errorList.forEach(excelDTO -> excelDTO.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
            }
        }
    }

    public List<AssetNoticeImportExcelDTO> getAllList(){
        return allList;
    }

    public List<AssetNoticeImportExcelDTO> getErrorList(){
        return errorList;
    }

    public List<AssetNoticeDetailDTO.MoldImportDTO> getSuccessList(){
        return successList;
    }
}
