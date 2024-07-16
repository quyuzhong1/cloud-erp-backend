package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.BiSkuInfoEntity;

/**
 * 商品信息服务类
 */
public interface BiSkuInfoService extends IService<BiSkuInfoEntity> {
    /**
     * 添加订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param biSkuInfoEntity 订单信息
     * @return java.lang.Boolean
     **/
    Boolean add(BiSkuInfoEntity biSkuInfoEntity);

    /**
     * 根据sku查询商品信息
     *
     * @param skuNo     商品sku
     * @param companyId
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     **/
    BiSkuInfoEntity getSkuBySkuNo(String skuNo, String companyId);

    /**
     * 根据平台订单id修改订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param biSkuInfoEntity 商品信息
     * @return java.lang.Boolean
     **/
    Boolean updateSkuBySkuNo(BiSkuInfoEntity biSkuInfoEntity);

    /**
     * 校验商品在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    void checkOrder(BiSkuInfoEntity biSkuInfoEntity);
}
