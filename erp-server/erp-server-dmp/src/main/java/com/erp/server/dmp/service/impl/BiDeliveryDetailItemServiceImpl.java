package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.dmp.dto.SplitSkuDTO;
import com.erp.model.dmp.entity.BiDeliveryDetailItemEntity;
import com.erp.model.dmp.entity.DmpBomEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.dmp.pull.mapper.BiDeliveryDetailItemMapper;
import com.erp.server.dmp.service.DmpBomService;
import com.erp.server.dmp.service.BiDeliveryDetailItemService;
import com.erp.server.dmp.service.BiOrderItemSplitService;
import com.erp.server.dmp.service.DmpSkuCostService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 发货详情商品信息
 */
@Service
public class BiDeliveryDetailItemServiceImpl extends ServiceImpl<BiDeliveryDetailItemMapper, BiDeliveryDetailItemEntity>
    implements BiDeliveryDetailItemService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DmpBomService dmpBomService;

    @Resource
    private DmpSkuCostService dmpSkuCostService;

    @Resource
    private BiOrderItemSplitService biOrderItemSplitService;

    /**
     * 添加发货详情商品信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param biDeliveryDetailItemEntity 退款列表信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean add(BiDeliveryDetailItemEntity biDeliveryDetailItemEntity, String platformSign) {
        List<BiDeliveryDetailItemEntity> dmpDeliveryDetailItemEntities = splitOrderItem(Arrays.asList(biDeliveryDetailItemEntity), platformSign);
        return this.saveBatch(dmpDeliveryDetailItemEntities);
    }

    /**
     * 批量添加发货详情商品信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param biDeliveryDetailItemEntityList 发货详情商品信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean batchAdd(List<BiDeliveryDetailItemEntity> biDeliveryDetailItemEntityList, String platformSign) {
        biDeliveryDetailItemEntityList = splitOrderItem(biDeliveryDetailItemEntityList, platformSign);
        return this.saveBatch(biDeliveryDetailItemEntityList, 500);
    }

    /**
     * 根据发货详情商品表id删除发货详情商品信息
     * @Author Luo_WG
     * @Date 2022/11/16 11:05
     * @param deliveryDetailId 发货详情商品表id
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean deleteDeliveryDetailItemByDetailId(String deliveryDetailId) {
        LambdaQueryWrapper<BiDeliveryDetailItemEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(BiDeliveryDetailItemEntity::getDeliveryDetailId, deliveryDetailId);
        return this.remove(lambdaQueryWrapper);
    }

    /**
     * 采购订单sku拆分
     * @Author Luo_WG
     * @Date 2023/9/13 14:01
     * @param itemEntityList
     * @return java.util.List<com.erp.model.dmp.entity.DmpOrderItemEntity>
     **/
    @Override
    public List<BiDeliveryDetailItemEntity> splitOrderItem(List<BiDeliveryDetailItemEntity> itemEntityList, String platformSign) {
        List<BiDeliveryDetailItemEntity> itemListAll = new ArrayList<>();
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
        for (BiDeliveryDetailItemEntity biDeliveryDetailItemEntity : itemEntityList) {
            //设置通用参数
            SplitSkuDTO splitSkuDTO = new SplitSkuDTO();
            splitSkuDTO.setPlatformSign(platformSign);
            splitSkuDTO.setId(biDeliveryDetailItemEntity.getItemId());
            splitSkuDTO.setCleanCostPrice(biDeliveryDetailItemEntity.getCleanCostPrice());
            splitSkuDTO.setIsSplitSku(biDeliveryDetailItemEntity.getIsSplitSku());
            splitSkuDTO.setOriginalSkuNo(biDeliveryDetailItemEntity.getOriginalSkuNo());
            splitSkuDTO.setSkuNo(biDeliveryDetailItemEntity.getSkuNo());
            splitSkuDTO.setQuantity(biDeliveryDetailItemEntity.getQuantity());
            if (biDeliveryDetailItemEntity.getIsGift() != null) {
                splitSkuDTO.setIsGift(biDeliveryDetailItemEntity.getIsGift());
            } else {
                splitSkuDTO.setIsGift(2);
            }
            splitSkuDTO.setAmountAfter(biDeliveryDetailItemEntity.getAmount());
            //拆单
            List<SplitSkuDTO> splitSkuDTOS = biOrderItemSplitService.splitSku(splitSkuDTO, machining, allBomList);
            if (CollectionUtil.isEmpty(splitSkuDTOS)){
                continue;
            }
            for (int i = 0;i < splitSkuDTOS.size(); i++) {
                SplitSkuDTO skuDTO = splitSkuDTOS.get(i);
                BiDeliveryDetailItemEntity entity = new BiDeliveryDetailItemEntity();
                BeanMapper.copy(biDeliveryDetailItemEntity, entity);
                //仅第一条拆分数据保存原单的成本、金额、数量
                if (i == 0) {
                    entity.setOriginalQuantity(skuDTO.getOriginalQuantity());
                    entity.setOriginalCostPrice(skuDTO.getOriginalCostPrice());
                    entity.setOriginalAmountAfter(skuDTO.getOriginalAmountAfter());
                }
                entity.setIsSplitSku(skuDTO.getIsSplitSku());
                entity.setAmount(skuDTO.getAmountAfter());
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
    public List<BiDeliveryDetailItemEntity> getItemByMainId(String mainId) {
        LambdaQueryWrapper<BiDeliveryDetailItemEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiDeliveryDetailItemEntity::getDeliveryDetailId, mainId);
        return baseMapper.selectList(queryWrapper);
    }
}




