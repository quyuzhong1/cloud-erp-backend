package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.SoOutstockDetiailDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.WmsAttachmentEntity;
import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.SoOutstockDetailMapper;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.WmsAttachmentService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售订单出库明细 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class SoOutstockDetailServiceImpl extends SuperServiceImpl<SoOutstockDetailMapper, SoOutstockDetailEntity> implements SoOutstockDetailService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoInfoFeign soInfoFeign;


    @Resource
    private WmsAttachmentService wmsAttachmentService;

    @Resource
    private InventoryService inventoryService;


    @Override
    public List<SoOutstockDetailEntity> listDetailBySourceDetailId(List<String> sourceDetailIds) {
        if (CollectionUtils.isEmpty(sourceDetailIds)) {
            return Collections.emptyList();
        }
        return baseMapper.listSoOutstockBySourceDetailId(sourceDetailIds);

    }


    /**
     * 保存销售出库单明细
     *
     * @param mainId
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-05-19 10:18
     */
    @Override
    public void add(String mainId, List<SoOutstockDetiailDTO.AddDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<String> skuIdList = detailList.stream().map(SoOutstockDetiailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        Class<SoOutstockDetailEntity> credentialClass = SoOutstockDetailEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        List<SoOutstockDetailEntity> addList = new ArrayList<>(detailList.size());
        //获取到表名
        String type = tableName.value();
        List<WmsAttachmentEntity> batchAttachmentList = new ArrayList<>(10);

        for (SoOutstockDetiailDTO.AddDTO item : detailList) {
            SoOutstockDetailEntity addEntity = new SoOutstockDetailEntity();
            BeanMapper.copy(item, addEntity);
            String id = IdWorker.getIdStr();
            String skuId = item.getSkuId();
            String skuNo = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
            addEntity.setSkuNo(skuNo);
            addEntity.setMainId(mainId);
            addEntity.setId(id);
            addList.add(addEntity);
            //附件集合
            List<String> attachmentUrlList = item.getAttachUrlList();
            //附件名
            List<String> attachmentNameList = item.getAttachNameList();
            if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {
                for (int i = 0; i < attachmentUrlList.size(); i++) {
                    WmsAttachmentEntity attachment = new WmsAttachmentEntity();
                    attachment.setAttachUrl(attachmentUrlList.get(i));
                    attachment.setAttachName(attachmentNameList.get(i));
                    attachment.setBusinessId(id);
                    attachment.setType(type);
                    batchAttachmentList.add(attachment);
                }
            }
        }
        this.saveBatch(addList);
        wmsAttachmentService.saveBatch(batchAttachmentList);
    }

    /**
     * 根据 main id  获取对应数据
     *
     * @param mainId
     * @return java.util.List<com.erp.model.wms.dto.SoOutstockDetiailDTO.ViewDTO>
     * @author yl
     * @date 2023-05-19 11:32
     */
    @Override
    public List<SoOutstockDetiailDTO.ViewDTO> listByMainId(String mainId, String warehouseId) {
        List<SoOutstockDetailEntity> dbList = this.listBaseByMainId(mainId);
        List<SoOutstockDetiailDTO.ViewDTO> resultList = BeanMapper.copyList(dbList, SoOutstockDetiailDTO.ViewDTO.class);
        List<String> skuIdList = resultList.stream().map(SoOutstockDetiailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<String> warehouseLocationList = resultList.stream().map(SoOutstockDetiailDTO.ViewDTO::getWarehouseLocation).collect(Collectors.toList());
        InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        skuInventoryDTO.setSkuIdList(skuIdList);
        skuInventoryDTO.setWarehouseIdList(Arrays.asList(warehouseId));
        skuInventoryDTO.setWarehouseLocationIdList(warehouseLocationList);
        skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        //可用库存
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryList = inventoryService.listSkuInventory(skuInventoryDTO);
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        for (SoOutstockDetiailDTO.ViewDTO item : resultList) {
            String skuId = item.getSkuId();
            String warehouseLocation = item.getWarehouseLocation();
            String skuName = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
            item.setProductName(skuName);
            Integer curInventoryQty = skuInventoryList.stream().filter(i -> i.getSkuId().equals(skuId) && i.getWarehouseLocationId().equals(warehouseLocation)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
            item.setCurInventoryQty(curInventoryQty);
        }
        return resultList;
    }


    /**
     * 删除明细
     *
     * @param mainIdList
     * @return void
     * @author yl
     * @date 2023-05-19 12:28
     */
    @Override
    public void removeByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return;
        }
        LambdaQueryWrapper<SoOutstockDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoOutstockDetailEntity::getMainId, mainIdList);
        this.remove(queryWrapper);

    }


    /**
     * 检查数量
     *
     * @param soId       销售订单id
     * @param sourceId
     * @param sourceType
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-05-22 15:54
     */
    @Override
    public void checkOutQty(String warehouseId, String soId, String sourceId, String sourceType, List<SoOutstockDetiailDTO.AddDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_92029);
        }
        //手动新增
        String selfAdd = SourceTypeEnum.SELF_ADD.getCode();
        List<String> sourceDetailList = detailList.stream().map(SoOutstockDetiailDTO.AddDTO::getSourceDetailId).collect(Collectors.toList());
        List<String> skuIdList = detailList.stream().map(SoOutstockDetiailDTO.AddDTO::getSkuId).collect(Collectors.toList());

        List<String> warehouseLocationList = detailList.stream().map(SoOutstockDetiailDTO.AddDTO::getWarehouseLocation).collect(Collectors.toList());

        //这个是已出数量
        List<SoOutstockDetailEntity> soOutstockDetailList = this.listDetailBySourceDetailId(sourceDetailList);


        //表示新增加
        if (selfAdd.equals(sourceType)) {
            InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
            skuInventoryDTO.setSkuIdList(skuIdList);
            skuInventoryDTO.setWarehouseIdList(Arrays.asList(warehouseId));
            skuInventoryDTO.setWarehouseLocationIdList(warehouseLocationList);
            skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
            //可用数量
            List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryList = inventoryService.listSkuInventory(skuInventoryDTO);

            //这个是销售订单的
            List<SoDetailEntity> soDetailList = soInfoFeign.listSoDetailByIds(sourceDetailList);
            for (SoOutstockDetiailDTO.AddDTO item : detailList) {
                String skuId = item.getSkuId();
                //库位
                String warehouseLocation = item.getWarehouseLocation();
                //实发数量
                Integer actualQty = item.getActualQty();
                //应发数量
                Integer planQty = item.getPlanQty();
                if (actualQty > planQty) {
                    throw new ServiceException(ApiError.ERROR_92027);
                }

                String sourceDetailId = item.getSourceDetailId();
                //这个是销售数量
                Integer soQty = soDetailList.stream().filter(s -> s.getId().equals(sourceDetailId)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getQty())).orElse(0);

                //这个是已出的数量
                Integer outStockQty = soOutstockDetailList.stream().filter(s -> s.getSourceDetailId().equals(sourceDetailId)).
                        mapToInt(SoOutstockDetailEntity::getActualQty).sum();
                if (outStockQty + planQty > soQty) {
                    throw new ServiceException(ApiError.ERROR_92028);
                }
                //即时库存
                Integer inventory = skuInventoryList.stream().filter(s -> s.getSkuId().equals(skuId) && s.getWarehouseLocationId().
                        equals(warehouseLocation)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
                if(planQty>inventory){
                    throw new ServiceException(ApiError.ERROR_92030);
                }
            }


        }


    }


    /**
     * 根据来源id 集合获取到对应的数据
     *
     * @return
     */
    public List<SoOutstockDetailEntity> listBySourceDetailIds(List<String> sourceDetailIdList) {
        if (CollectionUtils.isEmpty(sourceDetailIdList)) {
            return Collections.emptyList();
        }
        List<SoOutstockDetailEntity> list = baseMapper.listSoOutstockBySourceDetailId(sourceDetailIdList);
        return list.stream().filter(s -> !s.getInvalidStatus()).collect(Collectors.toList());

    }


    /**
     * 获取基础销售出库单列表
     *
     * @param mainId
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockDetailEntity>
     * @author yl
     * @date 2023-05-19 11:36
     */
    private List<SoOutstockDetailEntity> listBaseByMainId(String mainId) {
        if (StringUtils.isNotBlank(mainId)) {
            return this.lambdaQuery().eq(SoOutstockDetailEntity::getMainId, mainId).list();
        }
        return Collections.emptyList();

    }
}
