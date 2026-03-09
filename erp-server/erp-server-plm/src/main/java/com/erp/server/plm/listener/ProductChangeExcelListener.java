package com.erp.server.plm.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.excel.ProductChangeImportExcelDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.ProductChangeFieldEnum;
import com.erp.model.plm.enums.ProductSalesPlatformEnum;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysFeign;
import com.erp.server.plm.service.*;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: 组合产品导入监听
 * @date 2023/8/17 14:17
 */
public class ProductChangeExcelListener extends AnalysisEventListener<ProductChangeImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;

    private final ProductChangeService productChangeService = SpringUtil.getBean(ProductChangeService.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    private final BasicDictService basicDictService = SpringUtil.getBean(BasicDictService.class);

    private final BasicCategoryService basicCategoryService = SpringUtil.getBean(BasicCategoryService.class);

    private final ProductRDTTeamService productRDTTeamService = SpringUtil.getBean(ProductRDTTeamService.class);

    private final ProductBrandService productBrandService = SpringUtil.getBean(ProductBrandService.class);

    private final ApplicationCategoryService applicationCategoryService = SpringUtil.getBean(ApplicationCategoryService.class);

    private final BasicProductBuService basicProductBuService = SpringUtil.getBean(BasicProductBuService.class);

    private final SysFeign sysFeign = SpringUtil.getBean(SysFeign.class);

    private final List<BasicDictEntity> basicDictList;

    private final List<BasicCategoryEntity> basicCategoryEntities;

    private final List<ProductRDTTeamEntity> productRDTTeamEntities;

    private final List<ProductBrandEntity> productBrandEntities;

    private final List<ApplicationCategoryEntity> applicationCategoryEntities;

    private final List<BasicProductBuEntity> basicProductBuEntities;

    private final List<DictCountryDTO.ListDTO> countryList;
    /**
     * 错误信息
     */
    @Getter
    private List<ProductChangeImportExcelDTO> errorList = new ArrayList<>();

    @Getter
    private List<ProductChangeImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    public ProductChangeExcelListener(String taskId,
                                       String importType,
                                       Integer importCount) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
        this.basicDictList = basicDictService.list();
        this.basicCategoryEntities = basicCategoryService.list();
        this.productRDTTeamEntities = productRDTTeamService.list();
        this.productBrandEntities = productBrandService.list();
        this.applicationCategoryEntities = applicationCategoryService.list();
        this.basicProductBuEntities = basicProductBuService.list();
        this.countryList = sysFeign.countryList().getData();
    }

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
    public void invoke(ProductChangeImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        count += 1;
        //已经导入的数据跳过进度
        if (Objects.nonNull(importCount) && count < importCount){
            return;
        }

        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        ProductChangeFieldEnum productChangeFieldEnum = ProductChangeFieldEnum.getByFieldLabel(excelDTO.getField());
        if(productChangeFieldEnum == null){
            errorMsgList.add("字段名称不存在");
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }

        // 单据日期
        String billDateStr = excelDTO.getBillDateStr();
        if (StringUtils.isNotBlank(billDateStr)) {
            try {
                // 支持两种日期格式：yyyy-MM-dd 和 yyyy/M/d
                LocalDate billDate = null;
                try {
                    billDate = LocalDate.parse(billDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e1) {
                    try {
                        billDate = LocalDate.parse(billDateStr, DateTimeFormatter.ofPattern("yyyy/M/d"));
                    } catch (Exception e2) {
                        throw new IllegalArgumentException("日期格式错误");
                    }
                }
                excelDTO.setBillDate(billDate);
            } catch (Exception e) {
                errorMsgList.add("单据日期格式错误，请使用yyyy-MM-dd或yyyy/M/d格式");
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                return;
            }
        }

        //校验输入的值
        String newValueStr = excelDTO.getNewValue();
        Object newValue;
        try {
            newValue = productChangeFieldEnum.convert(newValueStr);
        }catch (Exception e) {
            errorMsgList.add("格式不正确，字段：" + productChangeFieldEnum.getName() + "，值：" + newValueStr);
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        if (newValue == null) {
            errorMsgList.add("格式不正确，字段：" + productChangeFieldEnum.getName() + "，值：" + newValueStr);
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        excelDTO.setNewValueObj(newValue);
        switch (productChangeFieldEnum) {
            case SALE_MODE:
                BasicDictEntity saleMode = basicDictList.stream().filter(e ->"saleMethod".equals(e.getType()) && Objects.equals(e.getName(), newValue)).findFirst().orElse(null);
                if(Objects.isNull(saleMode)){
                    errorMsgList.add("销售方式不存在，值：" + newValue);
                }
                break;
            case SALE_CHANNEL:
                BasicDictEntity saleChannel = basicDictList.stream().filter(e ->"salesChannel".equals(e.getType()) && Objects.equals(e.getName(), newValue)).findFirst().orElse(null);
                if(Objects.isNull(saleChannel)){
                    errorMsgList.add("销售渠道不存在，值：" + newValue);
                }
                break;
            case PRODUCT_ATTRIBUTE:
                BasicDictEntity basicDictEntity = basicDictList.stream().filter(e -> Objects.equals(e.getName(), newValue)).findFirst().orElse(null);
                if(Objects.isNull(basicDictEntity)){
                    errorMsgList.add("产品属性不存在，值：" + newValue);
                }else{
                    excelDTO.setNewValueObj(basicDictEntity.getId());
                }
                break;
            case PRODUCT_CATEGORY:
                BasicCategoryEntity basicCategoryEntity = basicCategoryEntities.stream().filter(e -> Objects.equals(e.getName(),  newValue)).findFirst().orElse(null);
                if(Objects.isNull(basicCategoryEntity)){
                    errorMsgList.add("产品分类不存在，值：" + newValue);
                }else{
                    excelDTO.setNewValueObj(basicCategoryEntity.getId());
                }
                break;
            case APPLICATION_CATEGORY:
                //可能是多选用逗号隔开的，校验每个名称是否存在
                List<String> applicationCategoryIds = new ArrayList<>();
                String[] applicationCategoryNames = ((String) newValue).split(",");
                for (String applicationCategoryName : applicationCategoryNames) {
                    ApplicationCategoryEntity applicationCategoryEntity = applicationCategoryEntities.stream().filter(e -> Objects.equals(e.getName(), applicationCategoryName.trim())).findFirst().orElse(null);
                    if(Objects.isNull(applicationCategoryEntity)){
                        errorMsgList.add("应用分类不存在，值：" + applicationCategoryName);
                    }else{
                        applicationCategoryIds.add(applicationCategoryEntity.getId());
                    }
                }
                //applicationCategoryIds用逗号隔开
                excelDTO.setNewValueObj(String.join(",", applicationCategoryIds));
                break;
            case R_D_TEAM:
                ProductRDTTeamEntity productRDTTeamEntity = productRDTTeamEntities.stream().filter(e -> Objects.equals(e.getName(), newValue)).findFirst().orElse(null);
                if(Objects.isNull(productRDTTeamEntity)){
                    errorMsgList.add("研发团队不存在，值：" + newValue);
                }else{
                    excelDTO.setNewValueObj(productRDTTeamEntity.getId());
                }
                break;
            case BRAND:
                ProductBrandEntity productBrandEntity = productBrandEntities.stream().filter(e -> Objects.equals(e.getName(), newValue)).findFirst().orElse(null);
                if(Objects.isNull(productBrandEntity)){
                    errorMsgList.add("品牌不存在，值：" + newValue);
                }else{
                    excelDTO.setNewValueObj(productBrandEntity.getId());
                }
                break;
            case PRODUCT_GRADE:
                BasicDictEntity gradeDict = basicDictList.stream().filter(e -> Objects.equals(e.getName(), newValue)).findFirst().orElse(null);
                if(Objects.isNull(gradeDict)){
                    errorMsgList.add("产品等级不存在，值：" + newValue);
                }else{
                    excelDTO.setNewValueObj(gradeDict.getId());
                }
                break;

            // product_ref_bu
            case BU_LINE:
                BasicProductBuEntity basicProductBuEntity = basicProductBuEntities.stream().filter(e -> Objects.equals(e.getName(), newValue)).findFirst().orElse(null);
                if(Objects.isNull(basicProductBuEntity)){
                    errorMsgList.add("BU线不存在，值：" + newValue);
                }else{
                    excelDTO.setNewValueObj(basicProductBuEntity.getId());
                }
                break;

            case SALE_COUNTRY:
                //可能是多选用逗号隔开的，校验每个名称是否存在
                List<String> countryIds = new ArrayList<>();
                String[] countryNames = ((String) newValue).split(",");
                for (String countryName : countryNames) {
                    DictCountryDTO.ListDTO country = countryList.stream().filter(e -> Objects.equals(e.getNameCn(), countryName.trim())).findFirst().orElse(null);
                    if(Objects.isNull(country)){
                        errorMsgList.add("国家不存在，值：" + countryName);
                    }else{
                        countryIds.add(country.getId());
                    }
                }
                excelDTO.setNewValueObj(String.join(",", countryIds));
                break;
            case SALE_PLATFORM:
                ProductSalesPlatformEnum productSalesPlatformEnum = ProductSalesPlatformEnum.getByName((String) newValue);
                if(Objects.isNull(productSalesPlatformEnum)) {
                    errorMsgList.add("销售平台不存在，值：" + newValue);
                }else{
                    excelDTO.setNewValueObj(productSalesPlatformEnum.getCode());
                }
                break;
            default:
                break;
        }

        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        successList.add(excelDTO);
    }


    /**
     * 数据全部解析完成后执行
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!successList.isEmpty()){
            try {
                List<String> errorNoList = errorList.stream().map(ProductChangeImportExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
                List<ProductChangeImportExcelDTO> errorList2 = new ArrayList<>();
                productChangeService.handleImportSuccessList(successList,errorNoList, errorList2,importType);
                errorList.addAll(errorList2);
            }catch (Exception e){
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            updateTask(count);
        }
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
