package com.erp.server.wms.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.SampleLedgerDTO;
import com.erp.model.wms.dto.excel.SampleBackInfoImportExcelDTO;
import com.erp.model.wms.enums.SampleLedgerTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.wms.service.SampleBackInfoService;
import com.erp.server.wms.service.SampleLedgerService;
import com.erp.server.wms.service.WarehouseService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 样品退回单Excel导入监听器
 * @author wuhaotian
 * @date 2025-08-25
 */
@Slf4j
public class SampleBackInfoExcelListener extends AnalysisEventListener<SampleBackInfoImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;
    private final String importType;
    private final Integer importCount;

    @Getter
    private Integer count = 0;

    // SKU信息
    private Map<String, SkuVO> skuMap;
    // 用户
    private List<FindUserDTO> userList;
    // 部门
    private List<SysDepartmentDTO> deptList;

    private final SampleBackInfoService sampleBackInfoService = SpringUtil.getBean(SampleBackInfoService.class);
    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);
    private final WarehouseService warehouseService = SpringUtil.getBean(WarehouseService.class);
    private final SysUserFeign sysUserFeign = SpringUtil.getBean(SysUserFeign.class);
    private final SysDictFeign sysDictFeign = SpringUtil.getBean(SysDictFeign.class);
    private final SampleLedgerService sampleLedgerService = SpringUtil.getBean(SampleLedgerService.class);

    /**
     * 错误信息
     */
    @Getter
    private List<SampleBackInfoImportExcelDTO> errorList = new ArrayList<>();

    @Getter
    private List<SampleBackInfoImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    public SampleBackInfoExcelListener(String taskId,
                                     String importType,
                                     Integer importCount,
                                     List<SysDepartmentDTO> deptList,
                                     Map<String, SkuVO> skuMap,
                                     List<FindUserDTO> userList) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
        this.skuMap = skuMap;
        this.deptList = deptList;
        this.userList = userList;
    }

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 每解析一行数据回调一遍
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(SampleBackInfoImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        count += 1;
        // 已经导入的数据跳过进度
        if (Objects.nonNull(importCount) && count < importCount) {
            return;
        }

        List<String> errorMsgList = new ArrayList<>();
        // 基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        // 退回人
        String backUserName = excelDTO.getUserName();
        FindUserDTO findUserDTO = null;
        if (StringUtils.isNotBlank(backUserName)) {
            findUserDTO = userList.stream().filter(e -> backUserName.equals(e.getUserName())).findFirst().orElse(null);
        }
        if (Objects.isNull(findUserDTO)) {
            errorMsgList.add("退回人不存在");
        } else {
            excelDTO.setUserId(findUserDTO.getUserId());
            excelDTO.setUserName(findUserDTO.getUserName());
        }
        

        // 退回日期
        String backDateStr = excelDTO.getBackDateStr();
        if (StringUtils.isNotBlank(backDateStr)) {
            try {
                // 支持两种日期格式：yyyy-MM-dd 和 yyyy/M/d
                LocalDate backDate = null;
                try {
                    backDate = LocalDate.parse(backDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e1) {
                    try {
                        backDate = LocalDate.parse(backDateStr, DateTimeFormatter.ofPattern("yyyy/M/d"));
                    } catch (Exception e2) {
                        throw new IllegalArgumentException("日期格式错误");
                    }
                }
                excelDTO.setBackDate(backDate);
            } catch (Exception e) {
                errorMsgList.add("退回时间格式错误，请使用yyyy-MM-dd或yyyy/M/d格式");
            }
        }

        // 退回部门
        String backDeptName = excelDTO.getDeptName();
        SysDepartmentDTO sysDepartmentDTO = deptList.stream().filter(e -> backDeptName.equals(e.getName())).findFirst().orElse(null);
        if (Objects.isNull(sysDepartmentDTO)) {
            errorMsgList.add("退回部门不存在");
        } else {
            excelDTO.setDeptId(sysDepartmentDTO.getId());
            excelDTO.setDeptName(sysDepartmentDTO.getName());
        }

        // 收货仓库
        String warehouseName = excelDTO.getWarehouseName();
        if (StringUtils.isNotBlank(warehouseName)) {
            try {
                List<WarehouseDTO.ListDTO> warehouseList = warehouseService.listWarehouseByParams(
                        WarehouseDTO.ListParamDTO.builder()
                                .warehouseName(warehouseName)
                                .showByAuth(false)
                                .build()
                );
                if (CollectionUtils.isNotEmpty(warehouseList)) {
                    excelDTO.setWarehouseId(warehouseList.get(0).getId());
                } else {
                    errorMsgList.add("仓库不存在：" + warehouseName);
                }
            } catch (Exception e) {
                errorMsgList.add("查询仓库失败：" + warehouseName);
            }
        }

        // SKU
        String skuNo = excelDTO.getSkuNo();
        if (StringUtils.isNotBlank(skuNo)) {
            SkuVO skuVO = skuMap.getOrDefault(skuNo, null);
            if (Objects.isNull(skuVO)) {
                errorMsgList.add("SKU不存在");
            } else {
                excelDTO.setSkuId(skuVO.getSkuId());
                excelDTO.setSkuNo(skuVO.getSkuNo());
                excelDTO.setProductName(skuVO.getSkuName());
            }
        }

        // 处理使用方信息
        String useUserName = excelDTO.getUseUserName();
        String useUserId = null;
        if (StringUtils.isNotBlank(useUserName)) {
            // 先尝试从普通用户中查找
            FindUserDTO useUser = userList.stream()
                .filter(e -> useUserName.equals(e.getUserName()))
                .findFirst()
                .orElse(null);
            
            if (useUser != null) {
                useUserId = useUser.getUserId();
            } else {
                // 如果普通用户中找不到，查询示例用户
                useUserId = getSampleUseUserIdByName(useUserName);
                if (useUserId == null) {
                    errorMsgList.add("使用方不存在：" + useUserName);
                }
            }
        }
        
        // 查询台账信息并验证数量
        if (StringUtils.isNotBlank(excelDTO.getUserId()) && StringUtils.isNotBlank(excelDTO.getSkuId())) {
            try {
                SampleLedgerDTO.SearchDTO searchDTO = new SampleLedgerDTO.SearchDTO();
                searchDTO.setUserId(excelDTO.getUserId());
                searchDTO.setUseUserId(useUserId); // 设置使用方ID
                searchDTO.setSkuIds(new ArrayList<>(Arrays.asList(excelDTO.getSkuId())));
                searchDTO.setType(SampleLedgerTypeEnum.BACK.getCode());
                List<SampleLedgerDTO.SkuAvailableQtyDTO> skuAvailableQtyDTOS = sampleLedgerService.listLedgerByUserId(searchDTO);
                
                if (CollectionUtils.isNotEmpty(skuAvailableQtyDTOS)) {
                    SampleLedgerDTO.SkuAvailableQtyDTO ledgerDTO = skuAvailableQtyDTOS.get(0);
                    // 回填使用方信息
                    excelDTO.setUseUserId(ledgerDTO.getUseUserId());
                    excelDTO.setUseUserName(ledgerDTO.getUseUserName());
                    
                    // 验证退回数量是否小于等于可退数量
                    if (StringUtils.isNotBlank(excelDTO.getQty())) {
                        try {
                            Integer backQty = Integer.parseInt(excelDTO.getQty());
                            Integer availableQty = ledgerDTO.getAvailableQty();
                            
                            if (backQty > availableQty) {
                                errorMsgList.add("退回数量(" + backQty + ")不能大于可退数量(" + availableQty + ")");
                            }
                        } catch (NumberFormatException e) {
                            errorMsgList.add("退回数量格式错误：" + excelDTO.getQty());
                        }
                    }
                } else {
                    errorMsgList.add("台账中未找到该SKU的可退数量信息");
                }
            } catch (Exception e) {
                errorMsgList.add("查询台账失败：" + e.getMessage());
            }
        }

        // 退回组织
        String orgIdName = excelDTO.getOrgName();
        if (StringUtils.isNotBlank(orgIdName)) {
            try {
                SysAccountingCompanyEntity company = sysUserFeign.getCompanyByName(orgIdName);
                if (company != null) {
                    excelDTO.setOrgId(company.getId());
                } else {
                    errorMsgList.add("组织不存在：" + orgIdName);
                }
            } catch (Exception e) {
                errorMsgList.add("查询组织失败：" + orgIdName);
            }
        }

        // 设置创建人信息
        setCreateUserInfo(excelDTO);
        
        // 存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        successList.add(excelDTO);
        if (successList.size() >= BATCH_COUNT) {
            try {
                List<String> errorNoList = errorList.stream().map(SampleBackInfoImportExcelDTO::getNo).distinct().collect(Collectors.toList());
                List<SampleBackInfoImportExcelDTO> errorList2 = new ArrayList<>();
                sampleBackInfoService.handleImportSuccessList(successList, errorNoList, errorList2, importType);
                errorList.addAll(errorList2);
            } catch (Exception e) {
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }

    /**
     * 数据全部解析完成后执行
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!successList.isEmpty()) {
            try {
                List<String> errorNoList = errorList.stream().map(SampleBackInfoImportExcelDTO::getNo).distinct().collect(Collectors.toList());
                List<SampleBackInfoImportExcelDTO> errorList2 = new ArrayList<>();
                sampleBackInfoService.handleImportSuccessList(successList, errorNoList, errorList2, importType);
                errorList.addAll(errorList2);
            } catch (Exception e) {
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }

    /**
     * 设置创建人信息
     */
    private void setCreateUserInfo(SampleBackInfoImportExcelDTO data) {
        try {
            LoginUser loginUser = UserContext.getLoginUser();
            if (loginUser != null) {
                data.setCreateUserId(loginUser.getUid());
                data.setCreateUserName(loginUser.getUserName());
            } else {
                // 如果获取不到当前用户，使用系统用户
                data.setCreateUserId("0");
                data.setCreateUserName("system");
            }
        } catch (Exception e) {
            log.warn("获取当前登录用户信息失败，使用系统用户：{}", e.getMessage());
            data.setCreateUserId("0");
            data.setCreateUserName("system");
        }
    }

    /**
     * 根据使用方名称查询使用方ID
     */
    private String getSampleUseUserIdByName(String useUserName) {
        if (StringUtils.isBlank(useUserName)) {
            return null;
        }

        try {
            // 调用feign接口查询
            List<String> nameList = Collections.singletonList(useUserName);
            com.common.core.controller.vo.ApiResult<List<com.erp.model.sys.dto.SampleUseUserDTO.ViewDTO>> result = 
                sysUserFeign.getSampleUseUserListByNameList(nameList);
            
            if (result != null && result.isSuccess() && CollectionUtils.isNotEmpty(result.getData())) {
                com.erp.model.sys.dto.SampleUseUserDTO.ViewDTO user = result.getData().get(0);
                return user.getId();
            }
            
            return null;
        } catch (Exception e) {
            log.error("查询使用方ID失败，使用方名称：{}，错误：{}", useUserName, e.getMessage(), e);
            return null;
        }
    }

    private void updateTask(Integer count) {
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(taskId);
        importResultDTO.setStatus(FileTaskStatusEnum.PROCESS.getCode());
        importResultDTO.setRemark("处理中");
        importResultDTO.setCount(count);
        downloadTaskFeign.updateTask(importResultDTO);
    }
}
