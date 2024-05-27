package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.dto.SplitSkuDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.dmp.pull.mapper.DmpRefundItemMapper;
import com.erp.server.dmp.service.DmpBomService;
import com.erp.server.dmp.service.DmpOrderItemSplitService;
import com.erp.server.dmp.service.DmpRefundItemService;
import com.erp.server.dmp.service.DmpSkuCostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 退款商品列表服务类
 */
@Service
public class DmpRefundItemServiceImpl extends ServiceImpl<DmpRefundItemMapper, DmpRefundItemEntity>
    implements DmpRefundItemService {
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private DmpBomService dmpBomService;
    @Resource
    private DmpSkuCostService dmpSkuCostService;
    @Resource
    private DmpOrderItemSplitService dmpOrderItemSplitService;

    /**
     * 添加退款商品信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpRefundItemEntity 退款列表信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean add(DmpRefundItemEntity dmpRefundItemEntity, String platformSign) {
        List<DmpRefundItemEntity> dmpRefundItemEntities = splitOrderItem(Arrays.asList(dmpRefundItemEntity), platformSign);
        return this.saveBatch(dmpRefundItemEntities);
    }

    /**
     * 批量添加退款商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpRefundItemEntityList 退款列表信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean batchAdd(List<DmpRefundItemEntity> dmpRefundItemEntityList, String platformSign) {
        List<DmpRefundItemEntity> dmpRefundItemEntities = splitOrderItem(dmpRefundItemEntityList, platformSign);
        return this.saveBatch(dmpRefundItemEntities, 500);
    }

    /**
     * 批量修改退款商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpRefundItemEntityList 退款列表信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean batchUpdate(List<DmpRefundItemEntity> dmpRefundItemEntityList, String platformSign) {
        List<DmpRefundItemEntity> dmpRefundItemEntities = splitOrderItem(dmpRefundItemEntityList, platformSign);
        return this.saveOrUpdateBatch(dmpRefundItemEntities);
    }

    /**
     * 根据退货订单表id删除退款商品信息
     * @Author Luo_WG
     * @Date 2022/11/16 11:05
     * @param refundId 退货订单表id
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean deleteRefundItemByRefundId(String refundId) {
        LambdaQueryWrapper<DmpRefundItemEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpRefundItemEntity::getRefundId, refundId);
        return this.remove(lambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOrderItem(List<DmpRefundItemEntity> itemList, String platformSign) {
        List<DmpRefundItemEntity> insertList = new ArrayList<>();
        List<DmpRefundItemEntity> updateList = new ArrayList<>();
        for (DmpRefundItemEntity orderItemBean : itemList) {
            Optional<DmpRefundItemEntity> dmpRefundItemEntity = lambdaQuery()
                    .eq(DmpRefundItemEntity::getErpOrderItemId, orderItemBean.getErpOrderItemId())
                    .oneOpt();
            if (dmpRefundItemEntity.isPresent()) {
                //如果数据有变动需要更新数据库订单商品信息
                if (!dmpRefundItemEntity.get().toString().equals(orderItemBean.toString())) {
                    orderItemBean.setId(dmpRefundItemEntity.get().getId());
//                    updateById(orderItemBean);
                    baseMapper.deleteById(dmpRefundItemEntity.get().getId());
                    updateList.add(orderItemBean);
                }
            } else {
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
    public List<DmpRefundItemEntity> splitOrderItem(List<DmpRefundItemEntity> itemEntityList, String platformSign) {
        List<DmpRefundItemEntity> itemListAll = new ArrayList<>();
        //1、根据sku查询马帮bom检查是否有bom，有就需要拆单
        List<String> skuList = itemEntityList.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
        //根据财务编码查询ERP的bom
        List<BomChildrenSkuDTO> allBomList = plmTaskFeign.listBomChildBySkuNos(skuList);
        //根据sku查询加工件
        List<DmpBomEntity> machining = dmpBomService.listFindBomBySkuList(skuList, PlatformEnum.MABANG.getDesc(), "machining");
        //获取财务编码
        List<String> financialCodeList = machining.stream().map(req -> req.getFinancialCode()).distinct().collect(Collectors.toList());
        if (PlatformEnum.MABANG.getDesc().equals(platformSign)) {
            allBomList = plmTaskFeign.listBomChildBySkuNos(financialCodeList);
        }
        //遍历订单详情
        for (DmpRefundItemEntity itemEntity : itemEntityList) {
            //设置通用参数
            SplitSkuDTO splitSkuDTO = new SplitSkuDTO();
            splitSkuDTO.setPlatformSign(platformSign);
            splitSkuDTO.setId(itemEntity.getErpOrderItemId());
            splitSkuDTO.setCleanCostPrice(itemEntity.getCleanCostPrice());
            splitSkuDTO.setIsSplitSku(itemEntity.getIsSplitSku());
            splitSkuDTO.setOriginalSkuNo(itemEntity.getOriginalSkuNo());
            splitSkuDTO.setSkuNo(itemEntity.getSkuNo());
            splitSkuDTO.setQuantity(itemEntity.getRefundNum());
            if (itemEntity.getIsGift() != null) {
                splitSkuDTO.setIsGift(itemEntity.getIsGift());
            } else {
                splitSkuDTO.setIsGift(2);
            }
            splitSkuDTO.setAmountAfter(itemEntity.getAmountAfter());
            //拆单
            List<SplitSkuDTO> splitSkuDTOS = dmpOrderItemSplitService.splitSku(splitSkuDTO, machining, allBomList);
            if (CollectionUtil.isEmpty(splitSkuDTOS)){
                continue;
            }
            for (int i = 0;i < splitSkuDTOS.size(); i++) {
                SplitSkuDTO skuDTO = splitSkuDTOS.get(i);
                DmpRefundItemEntity entity = new DmpRefundItemEntity();
                BeanMapper.copy(itemEntity, entity);
                //仅第一条拆分数据保存原单的成本、金额、数量
                if (i == 0) {
                    entity.setOriginalQuantity(skuDTO.getOriginalQuantity());
                    entity.setOriginalCostPrice(skuDTO.getOriginalCostPrice());
                    entity.setOriginalAmountAfter(skuDTO.getOriginalAmountAfter());
                }
                entity.setIsSplitSku(skuDTO.getIsSplitSku());
                entity.setAmountAfter(skuDTO.getAmountAfter());
                entity.setCleanCostPrice(skuDTO.getCleanCostPrice());
                entity.setSkuNo(skuDTO.getSkuNo());
                entity.setOriginalSkuNo(skuDTO.getOriginalSkuNo());
                entity.setIsGift(skuDTO.getIsGift());
                entity.setQuantity(skuDTO.getQuantity());
                //拆分单的条件下erpOrderItemId上拼接新sku编码
                if (MathUtil.ONE.equals(skuDTO.getIsSplitSku()) ) {
                    String erpOrderItemId = StrUtil.format("{}_{}",itemEntity.getErpOrderItemId(),skuDTO.getSkuNo());
                    entity.setErpOrderItemId(erpOrderItemId);
                }
                itemListAll.add(entity);
            }
        }
        return itemListAll;
    }
}




