package com.erp.server.oms.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.enums.OmsPlatformEnum;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.excel.SkuMappingWarehouseImportExcelDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.ListingMatchResultEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.service.ListingInfoService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SkuMappingService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname SkuMapingExcelListener
 * @Date 2023-06-28 18:07
 * @Created by yl
 */
@Slf4j
public class SkuMappingWarehouseExcelListener extends AnalysisEventListener<SkuMappingWarehouseImportExcelDTO> {

    /**
     * 仓库信息
     */
    List<WarehouseDTO.UpdateDTO> warehouseList;

    /**
     * listing
     */
    private final ListingInfoService listingInfoService = SpringUtil.getBean(ListingInfoService.class);

    private final SkuMappingService skuMappingService = SpringUtil.getBean(SkuMappingService.class);

    private final OperateLogService operateLogService = SpringUtil.getBean(OperateLogService.class);

    /**
     * listing 信息
     */
    private final List<ListingInfoEntity> addListingInfoEntityList = new ArrayList<>(10);

    private final List<SkuMappingEntity> addSkuMappingList = new ArrayList<>(10);


    /**
     * 更新的信息
     */
    private final List<SkuMappingEntity> updateSkuMappingList = new ArrayList<>(10);

    /**
     * 更新的listing
     */
    private final List<ListingInfoEntity> updateListingInfoList = new ArrayList<>(10);

    /**
     * 导入错误数据
     */
    @Getter
    private final List<SkuMappingWarehouseImportExcelDTO> errorList = new ArrayList<>(10);

    private final List<SkuMappingWarehouseImportExcelDTO> allList = new ArrayList<>();

    private PlmTaskFeign plmTaskFeign = SpringUtil.getBean(PlmTaskFeign.class);

    /**
     * 海外仓平台
     */
    private final Map<String, WarehouseDTO.ListDTO> overseasWareHouseMap;

    private final List<Pair<String, String>> addLogPairList = new ArrayList<>();

    private final List<Pair<String, String>> updateLogPairList = new ArrayList<>();

    public SkuMappingWarehouseExcelListener(List<WarehouseDTO.UpdateDTO> warehouseList,Map<String, WarehouseDTO.ListDTO> overseasWareHouseMap ) {
        this.warehouseList = warehouseList;
        this.overseasWareHouseMap = overseasWareHouseMap;
    }

    /**
     * 每解析一行执行一次
     *
     * @param importExcelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-06-28 18:12
     */
    @Override
    public void invoke(SkuMappingWarehouseImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
        List<String> msgList = FieldValidUtil.fieldValid(importExcelDTO);
        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }

        if (StringUtils.isNotBlank(importExcelDTO.getHasMappingAllStr())) {
            if (!importExcelDTO.getHasMappingAllStr().equals("是") && !importExcelDTO.getHasMappingAllStr().equals("否")) {
                errorMsgList.add("[对照关系适用于该服务商所有仓库]请输入'是'或'否'");
            }
        }

        String warehouseId = "";
        String warehouseName = importExcelDTO.getWarehouseName();
        //仓库名称
        if (StringUtils.isNotBlank(warehouseName)){
            WarehouseDTO.UpdateDTO warehouse = warehouseList.stream().filter(w -> w.getName().equals(warehouseName)).
                    findFirst().orElse(null);
            if (null == warehouse) {
                errorMsgList.add("仓库不存在");
            } else {
                warehouseId = warehouse.getId();
            }

        } else {
            errorMsgList.add("仓库不能为空");
        }

        // 传仓库名称校验
        if (StringUtils.isBlank(warehouseId) && StringUtils.isNotBlank(importExcelDTO.getWarehouseName())){
            WarehouseDTO.UpdateDTO warehouse = warehouseList.stream().filter(w -> w.getName().equals(importExcelDTO.getWarehouseName())).
                    findFirst().orElse(null);
            if (null == warehouse) {
                errorMsgList.add("仓库不存在");
            } else {
                warehouseId = warehouse.getId();
            }
        }

        // 服务商校验
        OmsPlatformEnum platformEnum;
        if (StringUtils.isNotBlank(importExcelDTO.getPlatformName())){
            platformEnum = Arrays.stream(OmsPlatformEnum.values())
                    .filter(e -> e.getCode().equalsIgnoreCase(importExcelDTO.getPlatformName()) || e.getName().equalsIgnoreCase(importExcelDTO.getPlatformName()))
                    .findFirst().orElse(null);
            if (null == platformEnum){
                errorMsgList.add("服务商不存在");
            }else{
                errorMsgList.add("有API对接的服务商不允许导入");
            }
        } else {
            platformEnum = null;
        }

        if (null == platformEnum && StringUtils.isNotBlank(warehouseId)){
            WarehouseDTO.ListDTO dto = overseasWareHouseMap.get(warehouseId);
            if (null != dto && StringUtils.isNotBlank(dto.getDictPlatform())){
                errorMsgList.add("仓库已配置有服务商,不允许无服务商映射配置");
            }
        }

        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }
        importExcelDTO.setWarehouseId(warehouseId);
        //添加到集合中，最后统一处理，避免每次调用都去查询数据库
        allList.add(importExcelDTO);

    }


    //所有执行完后 在执行
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if(CollectionUtils.isEmpty(allList)){
            return;
        }
        //已处理的SKU
        Set<String> handleSkuSet = new HashSet<>();
        List<String> warehouseSkuList = allList.stream().map(SkuMappingWarehouseImportExcelDTO::getWarehouseSkuNo).distinct().collect(Collectors.toList());
        List<String> erpSkuNoList = allList.stream().map(SkuMappingWarehouseImportExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listBySkuNoList(erpSkuNoList);
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setPlatform("");
        paramDTO.setType(RuleTypeEnum.WAREHOUSE.getCode());
        paramDTO.setPlatformSkuNoList(warehouseSkuList);
        List<ListingInfoEntity> listingInfoEntityList = listingInfoService.listByParam(RuleTypeEnum.WAREHOUSE.getCode(),"",warehouseSkuList);
        List<String> listingIdList = listingInfoEntityList.stream().map(ListingInfoEntity::getId).collect(Collectors.toList());
        List<SkuMappingEntity> skuMappingEntityList = skuMappingService.listByListingIds(listingIdList);
        for (SkuMappingWarehouseImportExcelDTO dto : allList) {
            SkuVO skuVO = skuList.stream().filter(v->v.getSkuNo().equals(dto.getSkuNo())).findFirst().orElse(null);
            if(Objects.isNull(skuVO)){
                dto.setErrorMsg("产品sku不存在");
                errorList.add(dto);
                continue;
            }
            if(handleSkuSet.contains(dto.getWarehouseSkuNo()+dto.getWarehouseName())){
                continue;
            }
            handleSkuSet.add(dto.getWarehouseSkuNo()+dto.getWarehouseName());
            ListingInfoEntity listingInfoEntity = listingInfoEntityList.stream().filter(v->v.getPlatformSkuNo().equals(dto.getWarehouseSkuNo())).findFirst().orElse(null);
            String listingId;
            //为空，则新增
            if(Objects.isNull(listingInfoEntity)){
                listingId = IdWorker.getIdStr();
                ListingInfoEntity addListingInfoEntity = new ListingInfoEntity();
                addListingInfoEntity.setId(listingId);
                addListingInfoEntity.setType(RuleTypeEnum.WAREHOUSE.getCode());
                addListingInfoEntity.setPlatformSkuNo(dto.getWarehouseSkuNo());
                addListingInfoEntity.setPlatformSkuName(dto.getWarehouseProductName());
                addListingInfoEntity.setPlatform("");
                addListingInfoEntity.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
                addListingInfoEntityList.add(addListingInfoEntity);
                //封装新增skuMapping
                SkuMappingEntity addSkuMapping = new SkuMappingEntity();
                addSkuMapping.setWarehouseId(dto.getWarehouseId());
                addSkuMapping.setWarehouseName(dto.getWarehouseName());
                addSkuMapping.setType(RuleTypeEnum.WAREHOUSE);
                addSkuMapping.setProductSkuId(skuVO.getSkuId());
                addSkuMapping.setProductSkuNo(skuVO.getSkuNo());
                addSkuMapping.setListingId(listingId);
                addSkuMapping.setDictPlatform("");
                addSkuMapping.setPlatformName("");
                addSkuMapping.setHasMappingAll(false);
                //生效时间
                addSkuMapping.setEffectiveTime(LocalDateTime.now());
                addSkuMapping.setExpireTime(LocalDateTime.now().plusYears(MathUtil.NUMBER_100));
                addSkuMappingList.add(addSkuMapping);
                Pair<String, String> pair = new Pair<>(listingId,listingId);
                addLogPairList.add(pair);
                listingInfoEntityList.add(addListingInfoEntity);
            }else{
                //将原来的skuMapping设置过期，再新增
                listingId = listingInfoEntity.getId();
                List<SkuMappingEntity> existSkuMappingList = skuMappingEntityList.stream().filter(v->v.getListingId().equals(listingId) && v.getWarehouseId().equals(dto.getWarehouseId())).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(existSkuMappingList)){
                    existSkuMappingList.forEach(v->{
                        v.setExpireTime(LocalDateTime.now());
                        v.setIsExpire(Boolean.TRUE);
                    });
                    updateSkuMappingList.addAll(existSkuMappingList);
                }else{
                    Pair<String, String> pair = new Pair<>(listingId,listingId);
                    addLogPairList.add(pair);
                }

                //封装新增skuMapping
                SkuMappingEntity addSkuMapping = new SkuMappingEntity();
                addSkuMapping.setWarehouseId(dto.getWarehouseId());
                addSkuMapping.setWarehouseName(dto.getWarehouseName());
                addSkuMapping.setType(RuleTypeEnum.WAREHOUSE);
                addSkuMapping.setProductSkuId(skuVO.getSkuId());
                addSkuMapping.setProductSkuNo(skuVO.getSkuNo());
                addSkuMapping.setListingId(listingId);
                addSkuMapping.setDictPlatform("");
                addSkuMapping.setPlatformName("");
                addSkuMapping.setHasMappingAll(false);
                //生效时间
                addSkuMapping.setEffectiveTime(LocalDateTime.now());
                addSkuMapping.setExpireTime(LocalDateTime.now().plusYears(MathUtil.NUMBER_100));
                addSkuMappingList.add(addSkuMapping);
                //设置日志
                for (SkuMappingEntity skuMappingEntity : existSkuMappingList) {
                    List<String> contentList = operateLogService.getContentByObj(skuMappingEntity,addSkuMapping,"");
                    for(String content:contentList){
                        Pair<String, String> pair = new Pair<>(addSkuMapping.getListingId(),content);
                        updateLogPairList.add(pair);
                    }
                }

                listingInfoEntity.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
                updateListingInfoList.add(listingInfoEntity);
            }
        }
        listingInfoService.saveBatchImport(addListingInfoEntityList,updateSkuMappingList,updateListingInfoList,addSkuMappingList,addLogPairList,updateLogPairList);
    }

}
