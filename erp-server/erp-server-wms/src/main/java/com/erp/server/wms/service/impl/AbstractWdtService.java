package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CommonCreateBillGoodsReq;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 旺店通抽象类
 */
@Slf4j
@Service
public class AbstractWdtService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    protected <T extends CommonCreateBillGoodsReq> List<T> handleGoodsList(List<T> goodsList) {
        if(CollectionUtils.isEmpty(goodsList)){
            return new ArrayList<>();
        }
        List<T> handlerGoodsList = new ArrayList<>();
        //根据SKU查询BOM判断是否是组合SKU，组合SKU需要拆分
        List<String> skuNoList = goodsList.stream().map(CommonCreateBillGoodsReq::getSpecNo).collect(Collectors.toList());
        List<BomChildrenSkuDTO> allBomChildrenList = plmTaskFeign.listBomChildBySkuNos(skuNoList);
        for (T goods : goodsList) {
            List<BomChildrenSkuDTO> bomChildrenSkuDTOList = allBomChildrenList.stream().filter(req -> req.getParentSkuNo().equals(goods.getSpecNo()) && BomTypeEnum.COMBINATION.getType().equals(req.getType())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(bomChildrenSkuDTOList)){
                handlerGoodsList.add(goods);
            }else{
                bomChildrenSkuDTOList.forEach(v->{
                    T handlerGoods = BeanUtil.copyProperties(goods, (Class<T>) goods.getClass());
                    handlerGoods.setSpecNo(v.getSkuNo());
                    handlerGoods.setPositionNo(goods.getPositionNo());
                    handlerGoods.setNum(goods.getNum().multiply(new BigDecimal(v.getQuantity())));
                    handlerGoodsList.add(handlerGoods);
                });
            }
        }
        return handlerGoodsList;
    }
}