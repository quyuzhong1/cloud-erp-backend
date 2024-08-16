package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.dto.BiShopInfoDTO;
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.dmp.entity.BiShopInfoEntity;
import com.erp.model.sys.dto.SysUserDeptDTO;

import java.util.List;

/**
 * 店铺信息服务类
 */
public interface BiDmpShopInfoService extends IService<BiShopInfoEntity> {
    /**
     * 添加店铺信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param biShopInfoEntity 店铺信息
     * @return java.lang.Boolean
     **/
    Boolean add(BiShopInfoEntity biShopInfoEntity);

    /**
     * 根据店铺编号查询店铺信息
     * @Author Luo_WG
     * @Date 2022/11/16 19:35
     * @param shopNo 店铺编号
     * @return com.erp.model.dmp.entity.DmpSkuInfoEntity
     **/
    BiShopInfoEntity getShopByShopNo(String shopNo);

    /**
     * 根据平台店铺id修改店铺信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param biShopInfoEntity 店铺信息
     * @return java.lang.Boolean
     **/
    Boolean updateShopByShopNo(BiShopInfoEntity biShopInfoEntity);

    /**
     * 校验店铺在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    void checkOrder(BiShopInfoEntity biShopInfoEntity);

    /**
     * 根据平台查询店铺信息
     *
     * @param platformSign 平台
     * @param userDeptList
     * @return java.util.List<com.erp.model.dmp.dto.ShopDTO>
     * @Author Luo_WG
     * @Date 2022/12/13 17:48
     **/
    BiShopInfoDTO queryShopByPlatformList(String shopNo, String platformSign, List<SysUserDeptDTO> userDeptList) ;

    void checkShopByKingDee(BiShopInfoEntity biShopInfoEntity);
    /**
     * @description: 根据id查询店铺信息
     * @author Will
     * @date: 2023/3/17 15:36
     * @param shopId
     * @return DmpShopInfoDTO
     */
    BiShopInfoDTO getShopById(String shopId);
}
