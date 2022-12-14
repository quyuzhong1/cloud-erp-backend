package com.erp.server.dmp.pull.service.dmp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.dto.ShopDTO;
import com.erp.model.dmp.entity.DmpShopInfoEntity;

import java.util.List;

/**
 * 店铺信息服务类
 */
public interface DmpShopInfoService extends IService<DmpShopInfoEntity> {
    /**
     * 添加店铺信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpShopInfoEntity 店铺信息
     * @return java.lang.Boolean
     **/
    Boolean add(DmpShopInfoEntity dmpShopInfoEntity);

    /**
     * 根据店铺编号查询店铺信息
     * @Author Luo_WG
     * @Date 2022/11/16 19:35
     * @param shopNo 店铺编号
     * @param platformSign 平台标识
     * @return com.erp.model.dmp.entity.DmpSkuInfoEntity
     **/
    DmpShopInfoEntity getShopByShopNo(String shopNo, String platformSign);

    /**
     * 根据平台店铺id修改店铺信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param dmpShopInfoEntity 店铺信息
     * @return java.lang.Boolean
     **/
    Boolean updateShopByShopNo(DmpShopInfoEntity dmpShopInfoEntity);

    /**
     * 校验店铺在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    void checkOrder(DmpShopInfoEntity dmpShopInfoEntity);

    /**
     * 根据平台查询店铺信息
     * @Author Luo_WG
     * @Date 2022/12/13 17:48
     * @param platformSign 平台
     * @return java.util.List<com.erp.model.dmp.dto.ShopDTO>
     **/
    List<ShopDTO> queryShopByPlatformList(String platformSign);
}
