package com.cloud.erp.service;

import com.erp.model.dmp.entity.CfgAppClientEntity;

/**
 * @author Lambda
 * @Classname ShopAuthServer
 * @Description TODO
 * @Date 2023-08-28 14:10
 * @Created by yl
 */
public interface ShopServer {


   /**
    * 方法说明
    * @author yl
    * @date 2023-08-28 16:37
    @param entity
    * @return
    */
   public String getShopAuthorizeUrl(CfgAppClientEntity entity,String name);
}
