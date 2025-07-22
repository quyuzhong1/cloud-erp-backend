package com.erp.server.oms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.constant.FileTemplateConstant;
import com.common.business.threadlocal.UserContext;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.oms.dto.excel.SkuMappingCustomerImportExcelDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.LabelSourceTypeEnum;
import com.erp.model.oms.enums.ListingInfoPlatformStatusEnum;
import com.erp.model.oms.enums.ListingMatchResultEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.oms.dto.OperateLogDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.service.CustomerInfoService;
import com.erp.server.oms.service.ListingInfoService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SkuMappingService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class SkuMappingCustomerExcelListener extends AnalysisEventListener<SkuMappingCustomerImportExcelDTO> {

    /**
     * 导入错误数据
     */
    @Getter
    private final List<SkuMappingCustomerImportExcelDTO> errorList = new ArrayList<>(10);

    private final List<SkuMappingCustomerImportExcelDTO> allList = new ArrayList<>();

    private final CustomerInfoService customerInfoService = SpringUtil.getBean(CustomerInfoService.class);

    private final ListingInfoService listingInfoService = SpringUtil.getBean(ListingInfoService.class);

    private final SkuMappingService skuMappingService = SpringUtil.getBean(SkuMappingService.class);

    private final OperateLogService operateLogService = SpringUtil.getBean(OperateLogService.class);

    private PlmTaskFeign plmTaskFeign = SpringUtil.getBean(PlmTaskFeign.class);
    @Override
    public void invoke(SkuMappingCustomerImportExcelDTO data, AnalysisContext context) {
        List<String> msgList = FieldValidUtil.fieldValid(data);
        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        LocalDateTime effectiveTime = LocalDateUtil.parseStrToLocalTime(data.getEnabledTime());
        if(Objects.isNull(effectiveTime)){
            errorMsgList.add("启用时间[时间]格式不正确");
        }
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            data.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(data);
            return;
        }

        //导入的数据不能存在相同客户，客户sku
        SkuMappingCustomerImportExcelDTO exist = allList.stream().filter(v->v.equals(data)).findFirst().orElse(null);
        if(exist != null){
            data.setErrorMsg("导入文件中已存在相同客户，相同客户sku的数据");
            errorList.add(data);
            return;
        }
        allList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if(CollectionUtils.isEmpty(allList)){
            return;
        }
        List<String> customerNameList = allList.stream().map(SkuMappingCustomerImportExcelDTO::getCustomer).collect(Collectors.toList());
        List<String> platformSkuNoList = allList.stream().map(SkuMappingCustomerImportExcelDTO::getPlatformSkuNo).collect(Collectors.toList());
        List<String> skuNoList = allList.stream().map(SkuMappingCustomerImportExcelDTO::getSkuNo).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listBySkuNoList(skuNoList);

        List<CustomerInfoEntity> customerInfoEntities = customerInfoService.listByNameList(customerNameList);
        List<String> customerIds = customerInfoEntities.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<ListingInfoEntity> listingInfoEntityList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(customerIds)){
            listingInfoEntityList =  listingInfoService.listByAuth(RuleTypeEnum.CUSTOMER.getCode(),platformSkuNoList,customerIds);
        }
        List<SkuMappingEntity> skuMappingEntityList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(listingInfoEntityList)){
            List<String> listingIds = listingInfoEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            skuMappingEntityList = skuMappingService.listByListingIds(listingIds);
        }
        List<ListingInfoEntity> addListingList = new ArrayList<>();
        List<SkuMappingEntity> addSkuMappingList = new ArrayList<>();
        List<ListingInfoEntity> updateListingList = new ArrayList<>();
        List<SkuMappingEntity> updateSkuMappingList = new ArrayList<>();
        List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList = new ArrayList<>();
        for (SkuMappingCustomerImportExcelDTO excelDTO : allList) {
            CustomerInfoEntity customerInfo = customerInfoEntities.stream().filter(v->v.getName().equals(excelDTO.getCustomer())).findFirst().orElse(null);
            if(Objects.isNull(customerInfo)){
                excelDTO.setErrorMsg("客户不存在");
                errorList.add(excelDTO);
                continue;
            }
            if(customerInfo.getDisabled()){
                excelDTO.setErrorMsg("客户已禁用");
                errorList.add(excelDTO);
                continue;
            }
            SkuVO skuVO = skuList.stream().filter(v->v.getSkuNo().equals(excelDTO.getSkuNo())).findFirst().orElse(null);
            if(Objects.isNull(skuVO)){
                excelDTO.setErrorMsg("产品sku不存在");
                errorList.add(excelDTO);
                continue;
            }
            ListingInfoEntity existEntity = listingInfoEntityList.stream().filter(v->v.getAuthId().equals(customerInfo.getId()) && v.getPlatformSkuNo().equals(excelDTO.getPlatformSkuNo())).findFirst().orElse(null);
            if(Objects.isNull(existEntity)){
                buildAdd(excelDTO, customerInfo, addListingList, skuVO, addSkuMappingList,operateLogList);
            }else{
                SkuMappingEntity skuMapping = skuMappingEntityList.stream().filter(v->v.getListingId().equals(existEntity.getId())).findFirst().orElse(null);
                if(Objects.isNull(skuMapping)){
                    excelDTO.setErrorMsg("skumapping为空");
                    errorList.add(excelDTO);
                    continue;
                }
                operateLogList.add(new OperateLogDTO.AddModuleOperateLogDTO(StrUtil.format("通过导入修改客户sku，客户sku名称从【{}】修改为【{}】，产品sku从【{}】修改为【{}】", existEntity.getPlatformSkuName(), excelDTO.getPlatformSkuName(), skuMapping.getProductSkuNo(), excelDTO.getPlatformSkuNo()),ModuleTypeEnum.LISTING_INFO.getCode(), existEntity.getId(), "导入更新"));
                existEntity.setPlatformSkuName(excelDTO.getPlatformSkuName());
                existEntity.setProductSkuNo(excelDTO.getSkuNo());
                if(StringUtils.isNotBlank(excelDTO.getPlatformStatusName())){
                    String platformStatus = ListingInfoPlatformStatusEnum.getCodeByName(excelDTO.getPlatformStatusName());
                    existEntity.setPlatformStatus(platformStatus);
                }
                updateListingList.add(existEntity);

                LocalDateTime effectiveTime = LocalDateUtil.parseStrToLocalTime(excelDTO.getEnabledTime());
                if(skuMapping.getProductSkuId().equals(skuVO.getSkuId())){
                    skuMapping.setEffectiveTime(effectiveTime);
                    skuMapping.setExpireTime(effectiveTime.plusYears(MathUtil.NUMBER_100));
                    updateSkuMappingList.add(skuMapping);
                }else if (skuMapping.getEffectiveTime().equals(effectiveTime) || effectiveTime.isBefore(skuMapping.getEffectiveTime())){
                    excelDTO.setErrorMsg("已映射的生效时间晚于表格的生效时间，不能更新");
                    errorList.add(excelDTO);
                    continue;
                }else{
                    skuMapping.setIsExpire(true);
                    updateSkuMappingList.add(skuMapping);

                    SkuMappingEntity addSkuMapping = new SkuMappingEntity();
                    addSkuMapping.setType(RuleTypeEnum.CUSTOMER);
                    addSkuMapping.setProductSkuId(skuVO.getSkuId());
                    addSkuMapping.setProductSkuNo(skuVO.getSkuNo());
                    addSkuMapping.setProductName(skuVO.getSkuName());
                    addSkuMapping.setListingId(existEntity.getId());
                    //生效时间
                    addSkuMapping.setEffectiveTime(effectiveTime);
                    addSkuMapping.setExpireTime(effectiveTime.plusYears(MathUtil.NUMBER_100));
                    addSkuMappingList.add(addSkuMapping);
                }
            }
        }

        submit(addListingList, addSkuMappingList, updateListingList, updateSkuMappingList,operateLogList);
    }

    public void submit(List<ListingInfoEntity> addListingList, List<SkuMappingEntity> addSkuMappingList, List<ListingInfoEntity> updateListingList, List<SkuMappingEntity> updateSkuMappingList,List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList) {
        if(CollectionUtils.isNotEmpty(addListingList)){
            for (ListingInfoEntity listingInfoEntity : addListingList) {
                String labelUrl = skuMappingService.getLabelUrl(listingInfoEntity.getProductSkuNo(), listingInfoEntity.getPlatformSkuNo());
                if (CharSequenceUtil.isNotBlank(labelUrl)){
                    //根据模板生成pdf文件
                    listingInfoEntity.setLabelUrl(labelUrl);
                    listingInfoEntity.setLabelFileName(FileTemplateConstant.CUSTOMER_SKU_LABEL + ".pdf");
                    listingInfoEntity.setLabelSourceType(LabelSourceTypeEnum.SYSTEM.getCode());
                    String msg =  CharSequenceUtil.format("用户【{}】新增【{}】为【{}】产品标签【{}】链接【{}】", UserContext.getDefaultLoginUser().getUserName(), "客户sku", listingInfoEntity.getPlatformSkuNo(),listingInfoEntity.getLabelFileName(),listingInfoEntity.getLabelUrl());
                    operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LISTING_INFO.getCode(), listingInfoEntity.getId(), "新增操作");
                }
            }
            listingInfoService.saveBatch(addListingList);
        }
        if(CollectionUtils.isNotEmpty(addSkuMappingList)){
            skuMappingService.saveBatch(addSkuMappingList);
        }
        if(CollectionUtils.isNotEmpty(updateListingList)){
            for (ListingInfoEntity listingInfoEntity : updateListingList){
                String labelUrl = listingInfoEntity.getLabelUrl();
                if (CharSequenceUtil.isNotBlank(labelUrl)){
                    continue;
                }
               labelUrl = skuMappingService.getLabelUrl(listingInfoEntity.getProductSkuNo(), listingInfoEntity.getPlatformSkuNo());
                if (CharSequenceUtil.isNotBlank(labelUrl)){
                    //根据模板生成pdf文件
                    listingInfoEntity.setLabelUrl(labelUrl);
                    listingInfoEntity.setLabelFileName(FileTemplateConstant.CUSTOMER_SKU_LABEL + ".pdf");
                    listingInfoEntity.setLabelSourceType(LabelSourceTypeEnum.SYSTEM.getCode());
                    String msg =  CharSequenceUtil.format("用户【{}】新增【{}】为【{}】产品标签【{}】链接【{}】", UserContext.getDefaultLoginUser().getUserName(), "客户sku", listingInfoEntity.getPlatformSkuNo(),listingInfoEntity.getLabelFileName(),listingInfoEntity.getLabelUrl());
                    operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LISTING_INFO.getCode(), listingInfoEntity.getId(), "新增操作");
                }
            }
            listingInfoService.updateBatchById(updateListingList);
        }
        if(CollectionUtils.isNotEmpty(updateSkuMappingList)){
            skuMappingService.updateBatchById(updateSkuMappingList);
        }
        operateLogService.batchAddModuleOperateLog(operateLogList);
    }

    private static void buildAdd(SkuMappingCustomerImportExcelDTO excelDTO, CustomerInfoEntity customerInfo, List<ListingInfoEntity> addListingList, SkuVO skuVO, List<SkuMappingEntity> addSkuMappingList,List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList) {
        ListingInfoEntity listingInfoEntity = new ListingInfoEntity();
        listingInfoEntity.setId(IdWorker.getIdStr());
        listingInfoEntity.setPlatformSkuNo(excelDTO.getPlatformSkuNo());
        listingInfoEntity.setPlatformSkuName(excelDTO.getPlatformSkuName());
        listingInfoEntity.setType(RuleTypeEnum.CUSTOMER.getCode());
        listingInfoEntity.setAuthId(customerInfo.getId());
        listingInfoEntity.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
        listingInfoEntity.setProductSkuNo(skuVO.getSkuNo());
        if(StringUtils.isNotBlank(excelDTO.getPlatformStatusName())){
            String platformStatus = ListingInfoPlatformStatusEnum.getCodeByName(excelDTO.getPlatformStatusName());
            listingInfoEntity.setPlatformStatus(platformStatus);
        }
        addListingList.add(listingInfoEntity);
        SkuMappingEntity skuMappingEntity = new SkuMappingEntity();
        skuMappingEntity.setType(RuleTypeEnum.CUSTOMER);
        skuMappingEntity.setProductSkuId(skuVO.getSkuId());
        skuMappingEntity.setProductSkuNo(skuVO.getSkuNo());
        skuMappingEntity.setProductName(skuVO.getSkuName());
        skuMappingEntity.setListingId(listingInfoEntity.getId());
        //生效时间
        skuMappingEntity.setEffectiveTime(LocalDateUtil.parseStrToLocalTime(excelDTO.getEnabledTime()));
        skuMappingEntity.setExpireTime(skuMappingEntity.getEffectiveTime().plusYears(MathUtil.NUMBER_100));
        addSkuMappingList.add(skuMappingEntity);
        operateLogList.add(new OperateLogDTO.AddModuleOperateLogDTO("通过导入新增客户sku",ModuleTypeEnum.LISTING_INFO.getCode(), listingInfoEntity.getId(), "导入新增"));
    }
}
