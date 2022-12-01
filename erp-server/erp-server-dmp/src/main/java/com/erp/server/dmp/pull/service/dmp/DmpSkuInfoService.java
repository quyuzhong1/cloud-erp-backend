package com.erp.server.dmp.pull.service.dmp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.server.dmp.entity.dmp.DmpSkuInfoEntity;

/**
 * 商品信息服务类
 */
public interface DmpSkuInfoService extends IService<DmpSkuInfoEntity> {
    /**
     * 添加订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpSkuInfoEntity 订单信息
     * @return java.lang.Boolean
     **/
    Boolean add(DmpSkuInfoEntity dmpSkuInfoEntity);

    /**
     * 根据sku查询商品信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     * @param skuNo 商品sku
     * @param platformSign 平台标识
     * @return com.erp.server.dmp.entity.dmp.DmpOrderInfoEntity
     **/
    DmpSkuInfoEntity getSkuBySkuNo(String skuNo, String platformSign);

    /**
     * 根据平台订单id修改订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param dmpSkuInfoEntity 商品信息
     * @return java.lang.Boolean
     **/
    Boolean updateSkuBySkuNo(DmpSkuInfoEntity dmpSkuInfoEntity);

    /**
     * 校验商品在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    void checkOrder(DmpSkuInfoEntity dmpSkuInfoEntity);
}
