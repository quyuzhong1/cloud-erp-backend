package com.erp.server.scm.listener;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.dto.FindUserDTO;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.SupplierVisitDTO;
import com.erp.model.scm.dto.excel.SupplierVisitImportExcelDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.SupplierVisitEnum;
import com.erp.model.scm.enums.SupplierVisitResultEnum;
import com.erp.server.scm.service.SupplierVisitService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jack
 * @Classname SupplierVisitExcelListener
 * @Date 2025-06-27
 */
public class SupplierVisitExcelListener extends AnalysisEventListener<SupplierVisitImportExcelDTO> {

    private SupplierVisitService supplierVisitService;
    //供应商
    private List<SupplierEntity> supplierList;
    //sku信息
    private Map<String,SkuVO> skuMap ;
    //用户
    private List<FindUserDTO> userList ;

    /**
     * 错误信息
     */
    private List<SupplierVisitImportExcelDTO> errorList = new ArrayList<>();

    private List<SupplierVisitDTO.ImportAddDTO> addList = new ArrayList<>();

    public SupplierVisitExcelListener(SupplierVisitService supplierVisitService,
                                      List<SupplierEntity> supplierList,
                                      Map<String,SkuVO> skuMap,
                                      List<FindUserDTO> userList) {
        this.supplierVisitService = supplierVisitService;
        this.skuMap = skuMap;
        this.supplierList = supplierList;
        this.userList = userList;
    }

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 每解析一行数据回调一遍
     *
     * @param excelDTO
     * @param analysisContext
     * @return void
     * @author jack
     * @date 2025-06-27
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(SupplierVisitImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        SupplierVisitDTO.ImportAddDTO addDTO = new SupplierVisitDTO.ImportAddDTO();
        String id = IdWorker.getIdStr();
        addDTO.setId(id);

        //供应商名称
        String supplierId ="";
        String name = excelDTO.getName();
        if(StringUtils.isNotBlank(name)){
            SupplierEntity supplierEntity = supplierList.stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null);
            if (Objects.isNull(supplierEntity)) {
                errorMsgList.add("供应商不存在");
            }else {
                supplierId = supplierEntity.getId();
                addDTO.setSupplierId(supplierId);
            }
        }

        //拜访类型
        String visitTypeName = excelDTO.getVisitTypeName();
        if(StringUtils.isNotBlank(visitTypeName)){
            addDTO.setVisitType(SupplierVisitEnum.getType(visitTypeName));
        }

        //拜访时间
        String visitTimeStr = excelDTO.getVisitTime();
        if(StringUtils.isNotBlank(visitTimeStr)){
            try {
                LocalDate visitTime = StringUtils.isBlank(visitTimeStr) ? null : LocalDate.parse(visitTimeStr, dateTimeFormatter);
                addDTO.setVisitTime(visitTime);
            }catch (Exception e){
                errorMsgList.add("拜访时间格式错误");
            }
        }

        //拜访人
        if(StringUtils.isNotBlank(excelDTO.getPeoples())){
            List<String> peoples = Arrays.asList(excelDTO.getPeoples().split(","));
            List<FindUserDTO> userInfoList = userList.stream().filter(e -> peoples.contains(e.getUserName())).collect(Collectors.toList());
            if(CollUtil.isEmpty(userInfoList)){
                errorMsgList.add("拜访人不存在");
            }else {
                List<String> userNameList = userInfoList.stream().map(FindUserDTO::getUserName).collect(Collectors.toList());
                List<String> missUserNameList = peoples.stream().filter(e -> !userNameList.contains(e)).collect(Collectors.toList());
                if(CollUtil.isNotEmpty(missUserNameList)){
                    errorMsgList.add("拜访人【"+String.join(",",missUserNameList)+"】不存在");
                }
                String people = String.join(",", userInfoList.stream().map(FindUserDTO::getUserId).collect(Collectors.toList()));
                addDTO.setPeople(people);
            }
        }

        //拜访内容
        addDTO.setContent(excelDTO.getContent());

        //拜访结果
        String resultName = excelDTO.getResultName();
        if(StringUtils.isNotBlank(resultName)){
            addDTO.setResult(SupplierVisitResultEnum.getCode(resultName));
        }

        //拜访物料
        if(StringUtils.isNotBlank(excelDTO.getSkus())){
            List<String> skus = Arrays.asList(excelDTO.getSkus().split(","));

            StringBuffer sb = new StringBuffer();
            List<SupplierVisitSkuEntity> supplierVisitSkuEntityList = new ArrayList<>();
            for (String skuNo : skus) {

                SkuVO skuVO = skuMap.getOrDefault(skuNo, null);
                if(Objects.isNull(skuVO)){
                    sb.append(skuNo);
                    sb.append(",");
                }else {
                    SupplierVisitSkuEntity supplierVisitSkuEntity = new SupplierVisitSkuEntity();
                    supplierVisitSkuEntity.setSupplierId(supplierId);
                    supplierVisitSkuEntity.setSkuId(skuVO.getSkuId());
                    supplierVisitSkuEntity.setSupplierVisitId(id);
                    supplierVisitSkuEntityList.add(supplierVisitSkuEntity);
                }
            }
            if(CollUtil.isNotEmpty(supplierVisitSkuEntityList)){
                addDTO.setSupplierVisitSkuEntityList(supplierVisitSkuEntityList);
            }
            String missSkuNo = sb.toString();
            if(StringUtils.isNotBlank(missSkuNo)){
                missSkuNo = missSkuNo.replaceAll(", $", "");
                errorMsgList.add("【"+missSkuNo+"】不存在");
            }
        }

        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }

        addList.add(addDTO);
    }


    /**
     * 数据全部解析完成后执行
     *
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-03-30 9:51
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (CollectionUtils.isNotEmpty(addList)) {
            supplierVisitService.batchImportVisit(addList);
        }
    }


    /**
     * 获取错误信息
     */
    public List<SupplierVisitImportExcelDTO> getErrorList() {
        return errorList;
    }


    /**
     * 检查obj所有字段是否为空
     *
     * @param obj
     * @return
     */
    public boolean checkObjAllFieldsIsNull(Object obj) {
        // 如果对象为null直接返回true
        if (null == obj) {
            return true;
        }
        try {
            // 挨个获取对象属性值
            for (Field f : obj.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                // 如果有一个属性值不为null，且值不是空字符串，就返回false
                if (f.get(obj) != null && StringUtils.isNotBlank(f.get(obj).toString())) {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;

    }
}
