package com.erp.server.plm.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.excel.MoldInfoImportExcelDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.plm.service.MoldInfoService;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author jack
 * @Classname MoldInfoExcelListener
 * @Date 2025-10-11
 */
public class MoldInfoExcelListener extends AnalysisEventListener<MoldInfoImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;


    //sku信息
    private Map<String,SkuVO> skuMap ;
    //用户
    private List<FindUserDTO> userList ;
    //部门
    private List<SysDepartmentDTO> deptList ;

    private final MoldInfoService moldInfoService = SpringUtil.getBean(MoldInfoService.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    /**
     * 错误信息
     */
    @Getter
    private List<MoldInfoImportExcelDTO> errorList = new ArrayList<>();

    @Getter
    private List<MoldInfoImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    public MoldInfoExcelListener(String taskId,
                                 String importType,
                                 Integer importCount) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
    }

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter dateTimeFormatter2 = DateTimeFormatter.ofPattern("yyyy/M/d");
    private final DateTimeFormatter dateTimeFormatter3 = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    /**
     * 每解析一行数据回调一遍
     *
     * @param excelDTO
     * @param analysisContext
     * @return void
     * @author jack
     * @date 2025-08-26
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(MoldInfoImportExcelDTO excelDTO, AnalysisContext analysisContext) {
//        count += 1;
//        //已经导入的数据跳过进度
//        if (Objects.nonNull(importCount) && count < importCount){
//            return;
//        }
//
//        List<String> errorMsgList = new ArrayList<>();
//        //基础验证
//        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
//        if (CollectionUtils.isNotEmpty(msgList)) {
//            errorMsgList.addAll(msgList);
//        }
//        //借入人
//        String borrowUserName = excelDTO.getBorrowUserName();
//        if(StringUtils.isNotBlank(borrowUserName)){
//            FindUserDTO findUserDTO = userList.stream().filter(e -> borrowUserName.equals(e.getUserName())).findFirst().orElse(null);
//            if(Objects.isNull(findUserDTO)){
//                errorMsgList.add("借入人不存在");
//            }else {
//                excelDTO.setBorrowUserId(findUserDTO.getUserId());
//                excelDTO.setBorrowUserName(findUserDTO.getUserName());
//            }
//        }
//
//        //借出人
//        String lendUserName = excelDTO.getLendUserName();
//        if(StringUtils.isNotBlank(lendUserName)){
//            FindUserDTO lendUser = userList.stream().filter(e -> lendUserName.equals(e.getUserName())).findFirst().orElse(null);
//            if(Objects.isNull(lendUser)){
//                errorMsgList.add("借出人不存在");
//            }else {
//                excelDTO.setLendUserId(lendUser.getUserId());
//                excelDTO.setLendUserName(lendUser.getUserName());
//            }
//        }
//
//        //借入日期
//        String borrowDateStr = excelDTO.getBorrowDateStr();
//        if(StringUtils.isNotBlank(borrowDateStr)){
//            LocalDate borrowDate = null;
//            try {
//                borrowDate = LocalDate.parse(borrowDateStr, dateTimeFormatter);
//            } catch (Exception e1) {
//                try {
//                    borrowDate = LocalDate.parse(borrowDateStr, dateTimeFormatter2);
//                } catch (Exception e2) {
//                    try {
//                        borrowDate = LocalDate.parse(borrowDateStr, dateTimeFormatter3);
//                    } catch (Exception e3) {
//                        errorMsgList.add("借入时间格式错误，请使用 yyyy-MM-dd、yyyy/M/d 或 yyyy/MM/dd 格式");
//                    }
//                }
//            }
//            excelDTO.setBorrowDate(borrowDate);
//        }
//        //预计退回日期
//        String estimatedReturnDateStr = excelDTO.getEstimatedReturnDateStr();
//        if(StringUtils.isNotBlank(estimatedReturnDateStr)){
//            LocalDate estimatedReturnDate = null;
//            try {
//                estimatedReturnDate = LocalDate.parse(estimatedReturnDateStr, dateTimeFormatter);
//            } catch (Exception e1) {
//                try {
//                    estimatedReturnDate = LocalDate.parse(estimatedReturnDateStr, dateTimeFormatter2);
//                } catch (Exception e2) {
//                    try {
//                        estimatedReturnDate = LocalDate.parse(estimatedReturnDateStr, dateTimeFormatter3);
//                    } catch (Exception e3) {
//                        errorMsgList.add("预计退回日期格式错误，请使用 yyyy-MM-dd、yyyy/M/d 或 yyyy/MM/dd 格式");
//                    }
//                }
//            }
//            excelDTO.setEstimatedReturnDate(estimatedReturnDate);
//        }
//
//        //借入部门
//        String borrowDeptName = excelDTO.getBorrowDeptName();
//        if(StringUtils.isNotBlank(borrowDeptName)){
//            SysDepartmentDTO sysDepartmentDTO = deptList.stream().filter(e -> borrowDeptName.equals(e.getName())).findFirst().orElse(null);
//            if(Objects.isNull(sysDepartmentDTO)){
//                errorMsgList.add("借入部门不存在");
//            }else {
//                excelDTO.setBorrowDeptId(sysDepartmentDTO.getId());
//                excelDTO.setBorrowDeptName(sysDepartmentDTO.getName());
//            }
//        }
//
//        //借出部门
//        String lendDeptName = excelDTO.getLendDeptName();
//        if(StringUtils.isNotBlank(lendDeptName)){
//            SysDepartmentDTO lendDept = deptList.stream().filter(e -> lendDeptName.equals(e.getName())).findFirst().orElse(null);
//            if(Objects.isNull(lendDept)){
//                errorMsgList.add("借出部门不存在");
//            }else {
//                excelDTO.setLendDeptId(lendDept.getId());
//                excelDTO.setLendDeptName(lendDept.getName());
//            }
//        }
//
//        //sku
//        String skuNo = excelDTO.getSkuNo();
//        if(StringUtils.isNotBlank(skuNo)){
//            SkuVO skuVO = skuMap.getOrDefault(skuNo, null);
//            if(Objects.isNull(skuVO)){
//                errorMsgList.add("SKU不存在");
//            }else {
//                excelDTO.setSkuId(skuVO.getSkuId());
//                excelDTO.setSkuNo(skuVO.getSkuNo());
//                excelDTO.setProductName(skuVO.getSkuName());
//            }
//        }
//
//        //存在错误数据则直接返回
//        if (errorMsgList.size() > 0) {
//            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
//            errorList.add(excelDTO);
//            return;
//        }
//        successList.add(excelDTO);
//        if (successList.size() >= BATCH_COUNT){
//            try {
//                List<String> errorNoList = errorList.stream().map(MoldInfoImportExcelDTO::getNo).distinct().collect(Collectors.toList());
//                List<MoldInfoImportExcelDTO> errorList2 = new ArrayList<>();
//                moldInfoService.handleImportSuccessList(successList,errorNoList, errorList2,importType);
//                errorList.addAll(errorList2);
//            }catch (Exception e){
//                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
//                errorList.addAll(successList);
//            }
//            successList.clear();
//            updateTask(count);
//        }
    }


    /**
     * 数据全部解析完成后执行
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
//        if (!successList.isEmpty()){
//            try {
//                List<String> errorNoList = errorList.stream().map(MoldInfoImportExcelDTO::getNo).distinct().collect(Collectors.toList());
//                List<MoldInfoImportExcelDTO> errorList2 = new ArrayList<>();
//                moldInfoService.handleImportSuccessList(successList,errorNoList, errorList2,importType);
//                errorList.addAll(errorList2);
//            }catch (Exception e){
//                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
//                errorList.addAll(successList);
//            }
//            successList.clear();
//            updateTask(count);
//        }
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
