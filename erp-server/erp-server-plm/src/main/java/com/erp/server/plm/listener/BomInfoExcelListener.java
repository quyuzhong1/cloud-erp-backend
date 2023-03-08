package com.erp.server.plm.listener;

import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.enums.ApiError;
import com.common.core.utils.BusinessNoCreateUtil;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.excel.BomInfoExcelDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.BomSkuEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomStateEnum;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.server.plm.constant.BomConstant;
import com.erp.server.plm.service.BomInfoService;
import com.erp.server.plm.service.BomSkuService;
import com.erp.server.plm.service.ProductDetailService;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BomInfoExcelListener extends AnalysisEventListener<BomInfoExcelDTO> {

    private BomInfoService bomInfoService;

    private ProductDetailService productDetailService;

    private BomSkuService bomSkuService;

    private  List<ProductDetailEntity> productDetailList;

    /**
     * 错误信息
     */
    private List<BomInfoExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<BomInfoExcelDTO> dataList = new ArrayList<>();
    /**
     * 需要新增的BOM主表数据
     */
    private List<BomInfoEntity> addBomInfoList = new ArrayList<>();
    /**
     * 需要新增或修改的明细数据
     */
    private List<BomSkuEntity> addList = new ArrayList<>();
    /**
     * 数据库存在的明细数据（用于删除非导入的数据）
     */
    private List<BomSkuEntity> addEntityList = new ArrayList<>();



    public BomInfoExcelListener(BomInfoService bomInfoService, ProductDetailService productDetailService,
                                BomSkuService bomSkuService,List<ProductDetailEntity> productDetailList) {
        this.bomInfoService = bomInfoService;
        this.productDetailService = productDetailService;
        this.bomSkuService = bomSkuService;
        this.productDetailList = productDetailList;
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
                String bomId = bomSkuList.get(0).getBomId();
                BomInfoEntity bomInfoEntity = bomInfoService.getById(bomId);
                if (ObjectUtils.isEmpty(bomInfoEntity)) {
                    errorMsgList.add("未发现父级SKU对应BOM");
                }
                if (BomStateEnum.AUDIT_PASS.getState().equals(bomInfoEntity.getState())) {
                    errorMsgList.add("BOM已审核不支持更新");
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
            String serialNumber = BusinessNoCreateUtil.getBusinessNo(BomConstant.BOM, maxSequence);
            bomInfoEntity.setSerialNumber(serialNumber);
            bomInfoEntity.setSequence(maxSequence + 1);
            bomInfoEntity.setId(IdUtil.getSnowflake().nextIdStr());
            addBomInfoList.add(bomInfoEntity);

            //添加关联明细
            bomSkuEntity.setBomId(bomInfoEntity.getId());
            addList.add(bomSkuEntity);
            return;
        }

        //根据父级sku及子级sku查询是否存在
        BomSkuEntity found = bomSkuList.stream().filter(obj -> obj.getSkuId().equals(child.getId())).findFirst().orElse(null);
        if (ObjectUtils.isNotEmpty(found)) {
            bomSkuEntity.setId(found.getId());
        }
        bomSkuEntity.setBomId(bomSkuList.get(0).getBomId());

        //需要新增或修改的数据
        addList.add(bomSkuEntity);
        if (CollectionUtils.isNotEmpty(bomSkuList)) {
            //用于回调删除原明细
            addEntityList.addAll(bomSkuList);
        }
    }

    public List<BomInfoExcelDTO> getErrorList(){
        return errorList;
    }

    public List<BomInfoExcelDTO> getExcelDateList(){
        return dataList;
    }

    /**
     * @description: 数据全部解析完后删除明细
     * @author Will
     * @date: 2023/3/7 15:32
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        //新增BOM主表信息
        if (CollectionUtils.isNotEmpty(addBomInfoList)) {
            bomInfoService.saveBatch(addBomInfoList);
        }
        //新增关联明细数据为空直接返回
        if (CollectionUtils.isEmpty(addList)) {
            return;
        }
        //新增或修改
        bomSkuService.saveOrUpdateBatch(addList);

        //父级对应数据库数据为空直接返回
        if (CollectionUtils.isEmpty(addEntityList)) {
            return;
        }
        List<String> removeIds = new ArrayList<>();
        Map<String, List<BomSkuEntity>> map = addList.stream().collect(Collectors.groupingBy(BomSkuEntity::getParentSkuId));
        for (Map.Entry<String, List<BomSkuEntity>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<BomSkuEntity> value = entry.getValue();
            //skuId集合
            List<String> skuIds = value.stream().map(BomSkuEntity::getSkuId).collect(Collectors.toList());
            //需要删除的明细
            List<String> ids = addEntityList.stream().filter(obj -> obj.getParentSkuId().equals(key) && !skuIds.contains(obj.getSkuId())).map(BomSkuEntity::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(ids)) {
                removeIds.addAll(ids);
            }
        }
        if (CollectionUtils.isEmpty(removeIds)) {
            return;
        }
        bomSkuService.removeByIds(removeIds);
    }
}
