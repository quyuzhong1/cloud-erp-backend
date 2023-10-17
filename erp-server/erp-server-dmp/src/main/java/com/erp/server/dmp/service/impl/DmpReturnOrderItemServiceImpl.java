package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.dmp.dto.SplitSkuDTO;
import com.erp.model.dmp.entity.DmpBomEntity;
import com.erp.model.dmp.entity.DmpReturnOrderItemEntity;
import com.erp.model.dmp.entity.DmpSkuCostEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.dmp.pull.mapper.DmpReturnOrderItemMapper;
import com.erp.server.dmp.service.DmpBomService;
import com.erp.server.dmp.service.DmpOrderItemService;
import com.erp.server.dmp.service.DmpReturnOrderItemService;
import com.erp.server.dmp.service.DmpSkuCostService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 中台订单退货服务类
 */
@Service
public class DmpReturnOrderItemServiceImpl extends ServiceImpl<DmpReturnOrderItemMapper, DmpReturnOrderItemEntity>
    implements DmpReturnOrderItemService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DmpBomService dmpBomService;

    @Resource
    private DmpSkuCostService dmpSkuCostService;

    @Resource
    private DmpOrderItemService dmpOrderItemService;

    /**
     * 添加退货订单商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpReturnOrderItemEntity 订单商品信息
     * @return java.lang.Boolean
     **/
    public Boolean add(DmpReturnOrderItemEntity dmpReturnOrderItemEntity, String platformSign) {
        //拆单sku
        List<DmpReturnOrderItemEntity> dmpReturnOrderItemEntities = splitOrderItem(Arrays.asList(dmpReturnOrderItemEntity), platformSign);
        return this.saveBatch(dmpReturnOrderItemEntities);
    }

    /**
     * 批量添加退货订单商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpOrderInfoEntityList 退货订单商品信息集合
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean batchAdd(List<DmpReturnOrderItemEntity> dmpOrderInfoEntityList, String platformSign) {
        //拆单sku
        List<DmpReturnOrderItemEntity> dmpReturnOrderItemEntities = splitOrderItem(dmpOrderInfoEntityList, platformSign);
        return this.saveBatch(dmpReturnOrderItemEntities, 500);
    }

    /**
     * 批量修改退货订单商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpOrderInfoEntityList 退货订单商品信息集合
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean batchUpdate(List<DmpReturnOrderItemEntity> dmpOrderInfoEntityList, String platformSign) {
        //拆单sku
        List<DmpReturnOrderItemEntity> dmpReturnOrderItemEntities = splitOrderItem(dmpOrderInfoEntityList, platformSign);
        return this.saveOrUpdateBatch(dmpReturnOrderItemEntities);
    }

    /**
     * 根据退货订单表id查询退货订单商品信息
     * @Author Luo_WG
     * @Date 2022/11/16 11:04
     * @param returnOrderId 退货订单表id
     * @return com.erp.model.dmp.entity.DmpReturnOrderItemEntity
     **/
    public DmpReturnOrderItemEntity getOrderByReturnOrderId(String returnOrderId) {
        LambdaQueryWrapper<DmpReturnOrderItemEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpReturnOrderItemEntity::getReturnOrderId, returnOrderId);
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据退货订单表id删除退货订单商品信息
     * @Author Luo_WG
     * @Date 2022/11/16 11:05
     * @param returnOrderId 退货订单表id
     * @return java.lang.Boolean
     **/
    public Boolean deleteOrderByReturnOrderId(String returnOrderId) {
        LambdaQueryWrapper<DmpReturnOrderItemEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpReturnOrderItemEntity::getReturnOrderId, returnOrderId);
        return this.remove(lambdaQueryWrapper);
    }

    @Override
    public void checkOrderItem(List<DmpReturnOrderItemEntity> itemList, String platformSign) {
        List<DmpReturnOrderItemEntity> insertList = new ArrayList<>();
        List<DmpReturnOrderItemEntity> updateList = new ArrayList<>();
        for (DmpReturnOrderItemEntity orderItemBean : itemList) {
            Optional<DmpReturnOrderItemEntity> dmpReturnOrderItemEntity = lambdaQuery()
                    .eq(DmpReturnOrderItemEntity::getErpOrderItemId, orderItemBean.getErpOrderItemId())
                    .last("limit 1")
                    .oneOpt();
            if (dmpReturnOrderItemEntity.isPresent()) {
                //如果数据有变动需要更新数据库订单商品信息
                if (!dmpReturnOrderItemEntity.get().toString().equals(orderItemBean.toString())) {
                    orderItemBean.setId(dmpReturnOrderItemEntity.get().getId());
                    baseMapper.deleteById(dmpReturnOrderItemEntity.get().getId());
                    updateList.add(orderItemBean);
                }
            } else {
                if (orderItemBean.getIsDeleted()){
                    continue;
                }
                insertList.add(orderItemBean);
            }
        }
        if (CollectionUtil.isNotEmpty(insertList)) {
            this.batchAdd(insertList, platformSign);
        }
        if(CollectionUtil.isNotEmpty(updateList)){
            this.batchUpdate(updateList, platformSign);
        }
    }

    /**
     * 采购订单sku拆分
     * @Author Luo_WG
     * @Date 2023/9/13 14:01
     * @param itemEntityList
     * @return java.util.List<com.erp.model.dmp.entity.DmpOrderItemEntity>
     **/
    @Override
    public List<DmpReturnOrderItemEntity> splitOrderItem(List<DmpReturnOrderItemEntity> itemEntityList, String platformSign) {
        List<DmpReturnOrderItemEntity> itemListAll = new ArrayList<>();
        //1、根据sku查询马帮bom检查是否有bom，有就需要拆单
        List<String> skuList = itemEntityList.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
        //根据财务编码查询ERP的bom
        List<BomChildrenSkuDTO> allBomList = plmTaskFeign.listBomChildBySkuNos(new ArrayList<>());
        //根据sku查询加工件
        List<DmpBomEntity> machining = dmpBomService.listFindBomBySkuList(skuList, PlatformEnum.MABANG.getDesc(), "machining");
        //获取财务编码
        List<String> financialCodeList = machining.stream().map(req -> req.getFinancialCode()).distinct().collect(Collectors.toList());
        if (PlatformEnum.MABANG.getDesc().equals(platformSign)) {
            allBomList = plmTaskFeign.listBomChildBySkuNos(financialCodeList);
        }
        //获取所有成本
        List<DmpSkuCostEntity> allSkuCostList = dmpSkuCostService.list();
        //遍历订单详情
        for (DmpReturnOrderItemEntity itemEntity : itemEntityList) {
            //设置通用参数
            SplitSkuDTO splitSkuDTO = new SplitSkuDTO();
            splitSkuDTO.setPlatformSign(platformSign);
            splitSkuDTO.setId(itemEntity.getErpOrderItemId());
            splitSkuDTO.setCleanCostPrice(itemEntity.getCleanCostPrice());
            splitSkuDTO.setIsSplitSku(itemEntity.getIsSplitSku());
            splitSkuDTO.setOriginalSkuNo(itemEntity.getOriginalSkuNo());
            splitSkuDTO.setSkuNo(itemEntity.getSkuNo());
            if (itemEntity.getIsGift() != null) {
                splitSkuDTO.setIsGift(itemEntity.getIsGift());
            } else {
                splitSkuDTO.setIsGift(2);
            }
            splitSkuDTO.setQuantity(itemEntity.getQuantity());
            splitSkuDTO.setAmountAfter(itemEntity.getAmountAfter());
            //拆单
            List<SplitSkuDTO> splitSkuDTOS = dmpOrderItemService.splitSku(splitSkuDTO, machining, allBomList, allSkuCostList);
            for (SplitSkuDTO skuDTO : splitSkuDTOS) {
                DmpReturnOrderItemEntity entity = new DmpReturnOrderItemEntity();
                BeanMapper.copy(itemEntity, entity);
                entity.setIsSplitSku(skuDTO.getIsSplitSku());
                entity.setAmountAfter(skuDTO.getAmountAfter());
                entity.setCleanCostPrice(skuDTO.getCleanCostPrice());
                entity.setSkuNo(skuDTO.getSkuNo());
                entity.setOriginalSkuNo(skuDTO.getOriginalSkuNo());
                entity.setIsGift(skuDTO.getIsGift());
                entity.setQuantity(skuDTO.getQuantity());
                itemListAll.add(entity);
            }
        }
        return itemListAll;
    }

    @Override
    public List<DmpReturnOrderItemEntity> getItemByMainId(String mainId) {
        return this.lambdaQuery().eq(DmpReturnOrderItemEntity::getReturnOrderId, mainId)
                .eq(DmpReturnOrderItemEntity::getIsDeleted,false)
                .list();
    }
}




