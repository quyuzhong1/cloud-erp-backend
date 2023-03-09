package com.erp.server.plm.listener;

import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.constant.BusinessNoConstant;
import com.common.core.enums.ApiError;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.excel.BomInfoExcelDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.BomOperationTypeEnum;
import com.erp.model.plm.enums.BomStateEnum;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.enums.BusinessNoTypeEnum;
import com.erp.server.plm.constant.BomOperateContent;
import com.erp.server.plm.service.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BomInfoExcelListener extends AnalysisEventListener<BomInfoExcelDTO> {

    private BomInfoService bomInfoService;

    private ProductDetailService productDetailService;

    private BomSkuService bomSkuService;

    private SysCodeService sysCodeService;

    private BomOperateLogService bomOperateLogService;

    private ProductBomHistoryService productBomHistoryService;

    private ProductBomSkuHistoryService productBomSkuHistoryService;

    private  List<ProductDetailEntity> productDetailList;

    private  List<BomSkuEntity> allBomSkuList;

    /**
     * 错误信息
     */
    private List<BomInfoExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<BomInfoExcelDTO> dataList = new ArrayList<>();

    /**
     * 需要添加修改日志的数据
     */
    private List<BomSkuEntity> updateLogList = new ArrayList<>();





    public BomInfoExcelListener(BomInfoService bomInfoService, ProductDetailService productDetailService, BomOperateLogService bomOperateLogService,ProductBomHistoryService productBomHistoryService,
                                ProductBomSkuHistoryService productBomSkuHistoryService,BomSkuService bomSkuService, SysCodeService sysCodeService, List<ProductDetailEntity> productDetailList, List<BomSkuEntity> allBomSkuList) {
        this.bomInfoService = bomInfoService;
        this.productDetailService = productDetailService;
        this.bomOperateLogService = bomOperateLogService;
        this.productBomHistoryService = productBomHistoryService;
        this.productBomSkuHistoryService = productBomSkuHistoryService;
        this.bomSkuService = bomSkuService;
        this.sysCodeService = sysCodeService;
        this.productDetailList = productDetailList;
        this.allBomSkuList = allBomSkuList;
    }

   /**
    * @description: 每解析一行数据回调一遍
    * @author Will
    * @date: 2023/3/7 11:22
    * @param bomInfoExcelDTO BOM导入信息
    * @param analysisContext
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(BomInfoExcelDTO bomInfoExcelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        BomSkuEntity bomSkuEntity = new BomSkuEntity();

        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(bomInfoExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //父级sku
        String parentSku = bomInfoExcelDTO.getParentSku();
        //子sku
        String childSku = bomInfoExcelDTO.getChildSku();

        if (StringUtils.equals(parentSku,childSku)) {
            errorMsgList.add("父级sku和子级sku不能重复");
        }
        //已审核父级sku
        ProductDetailEntity parent = productDetailList.stream().filter(obj -> StringUtils.equals(parentSku, obj.getSkuNo())).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(parent)) {
            errorMsgList.add(ApiError.ERROR_95152.msg);
        }
        //已审核子sku
        ProductDetailEntity child = productDetailList.stream().filter(obj -> StringUtils.equals(childSku, obj.getSkuNo())).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(child)) {
            errorMsgList.add(ApiError.ERROR_95153.msg);
        }
        List<BomSkuEntity> bomSkuList = new ArrayList<>();

        if (ObjectUtils.isNotEmpty(parent)) {
            //查询是否已存在bom信息
             bomSkuList = bomSkuService.getByParentSkuId(parent.getId());

            if (CollectionUtils.isNotEmpty(bomSkuList)) {
                //bom信息验证
                String bomId = bomSkuList.get(0).getBomId();
                BomInfoEntity bomInfoEntity = bomInfoService.getById(bomId);
                if (ObjectUtils.isEmpty(bomInfoEntity)) {
                    errorMsgList.add("未发现父级SKU对应BOM");
                }
                if (!BomStateEnum.WAIT_SUBMIT_AUDIT.getState().equals(bomInfoEntity.getState()) && !BomStateEnum.AUDIT_NO_PASS.getState().equals(bomInfoEntity.getState())) {
                    errorMsgList.add("仅待提交审核和审核不通过BOM支持更新");
                }
            }
        }
        //添加数据用于判断是否为空
        dataList.add(bomInfoExcelDTO);
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            bomInfoExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(bomInfoExcelDTO);
            return;
        }
        bomSkuEntity.setSkuId(child.getId());
        bomSkuEntity.setSkuNo(child.getSkuNo());
        bomSkuEntity.setProductId(child.getProductId());
        bomSkuEntity.setParentSkuId(parent.getId());
        bomSkuEntity.setParentSkuNo(parent.getSkuNo());
        bomSkuEntity.setQuantity(Integer.valueOf(bomInfoExcelDTO.getQuantityStr()));


        if (CollectionUtils.isEmpty(bomSkuList)) {
            //新增
            BomInfoEntity bomInfoEntity = new BomInfoEntity();
            bomInfoEntity.setVersion(MathUtil.ONE);
            bomInfoEntity.setType(BomTypeEnum.getType(bomInfoExcelDTO.getTypeName()));
            bomInfoEntity.setState(BomStateEnum.WAIT_SUBMIT_AUDIT.getState());
            Integer maxSequence = bomInfoService.getMaxSequence();
            //获取到 编号
            String serialNumber = sysCodeService.getBusinessNo(BusinessNoConstant.BOM, BusinessNoTypeEnum.Bom_NO);
            bomInfoEntity.setSerialNumber(serialNumber);
            bomInfoEntity.setSequence(maxSequence + 1);
            bomInfoEntity.setId(IdUtil.getSnowflake().nextIdStr());
            bomInfoService.save(bomInfoEntity);

            //添加关联明细
            bomSkuEntity.setBomId(bomInfoEntity.getId());
            bomSkuService.save(bomSkuEntity);

            //保存历史bom信息
            ProductBomHistoryEntity bomHistory = new ProductBomHistoryEntity();
            bomHistory.setBomId(bomInfoEntity.getId());
            bomHistory.setSerialNumber(bomInfoEntity.getSerialNumber());
            bomHistory.setType(bomInfoEntity.getType());
            bomHistory.setVersion(bomInfoEntity.getVersion());
            productBomHistoryService.save(bomHistory);
            //保存历史bomsku信息
            updateBomSKuHistory(bomSkuEntity,bomHistory.getId());
            //添加 bom的操作日志
            String operateContent = String.format(BomOperateContent.ADD, serialNumber);
            bomOperateLogService.saveOperate(bomInfoEntity.getId(), BomOperationTypeEnum.ADD.getType(), operateContent);
            return;
        }
        //根据父级sku及子级sku查询是否存在
        BomSkuEntity found = bomSkuList.stream().filter(obj -> obj.getSkuId().equals(child.getId())).findFirst().orElse(null);
        if (ObjectUtils.isNotEmpty(found)) {
            bomSkuEntity.setId(found.getId());
        }

        String bomId = bomSkuList.get(0).getBomId();
        bomSkuEntity.setBomId(bomId);

        updateLogList.add(bomSkuEntity);
        //需要新增或修改的数据
        bomSkuService.saveOrUpdate(bomSkuEntity);

        //保存历史bomsku信息
        List<ProductBomHistoryEntity> productBomHistoryList= productBomHistoryService.listByBomId(bomId);
        if (CollectionUtils.isNotEmpty(productBomHistoryList) && StringUtils.isBlank(bomSkuEntity.getId())) {
            updateBomSKuHistory(bomSkuEntity,productBomHistoryList.get(0).getId());
        }
    }

    public List<BomInfoExcelDTO> getErrorList(){
        return errorList;
    }

    public List<BomInfoExcelDTO> getExcelDateList(){
        return dataList;
    }


    private void updateBomSKuHistory (BomSkuEntity bomSkuEntity,String bomHistoryId) {
        //保存关联数据历史版本
        ProductBomSkuHistoryEntity entity = new ProductBomSkuHistoryEntity();
        entity.setParentSkuId(bomSkuEntity.getParentSkuId());
        entity.setParentSkuNo(bomSkuEntity.getParentSkuId());
        entity.setSkuId(bomSkuEntity.getSkuId());
        entity.setSkuNo(bomSkuEntity.getSkuNo());
        entity.setQuantity(bomSkuEntity.getQuantity());
        entity.setBomHistoryId(bomHistoryId);
        entity.setProductId(bomSkuEntity.getProductId());
        productBomSkuHistoryService.save(entity);

    }
    /**
     * @description: 数据全部解析完后删除明细
     * @author Will
     * @date: 2023/3/7 15:32
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        //父级对应数据库数据为空直接返回
        if (CollectionUtils.isEmpty(allBomSkuList)) {
            return;
        }
        //需要删除的数据
         List<BomSkuEntity> removeEntityList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(updateLogList)) {
            Map<String, List<BomSkuEntity>> map = updateLogList.stream().collect(Collectors.groupingBy(BomSkuEntity::getBomId));
            for (Map.Entry<String, List<BomSkuEntity>> entry : map.entrySet()) {
                String bomId = entry.getKey();
                List<BomSkuEntity> newList = entry.getValue();
                List<BomSkuEntity> oldList = allBomSkuList.stream().filter(e -> e.getBomId().equals(bomId)).collect(Collectors.toList());
                //添加更新日志
                String operateContent = bomInfoService.getExcelUpdateContent(newList, oldList);
                if (StringUtils.isNotBlank(operateContent)) {
                    bomOperateLogService.saveOperate(bomId, BomOperationTypeEnum.UPDATE.getType(), operateContent);
                }
                //需要删除数据
                String parentSkuId = newList.get(0).getParentSkuId();
                List<String> skuIdList = newList.stream().map(BomSkuEntity::getSkuId).collect(Collectors.toList());
                List<BomSkuEntity> removeList = allBomSkuList.stream().filter(e -> e.getBomId().equals(bomId) && parentSkuId.equals(e.getParentSkuId()) && !skuIdList.contains(e.getSkuId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(removeList)) {
                    removeEntityList.addAll(removeList);
                }
            }
        }
        if (CollectionUtils.isEmpty(removeEntityList)) {
            return;
        }
        //需要删除的关联数据
        List<String> removeIds = removeEntityList.stream().map(BomSkuEntity::getId).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(removeIds)) {
            return;
        }
        bomSkuService.removeByIds(removeIds);
        removeEntityList.forEach(obj->{
            //删除历史版本信息
            productBomSkuHistoryService.removeByBomSku(obj);
        });

    }
}
