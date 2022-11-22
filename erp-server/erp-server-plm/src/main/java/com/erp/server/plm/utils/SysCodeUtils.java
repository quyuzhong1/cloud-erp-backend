package com.erp.server.plm.utils;

import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Will
 * @version 1.0
 * @description: 编码生成
 * @date 2022/11/22 9:27
 */
@Component
public class SysCodeUtils {

    @Autowired
    private SysUserFeign sysUserFeign;

    /**
     * @description: 生成sku编码
     * @author Will
     * @date: 2022/11/22 9:36
     * @param entity
     * @return String
     */
    public String getSkuNo(ProductInfoEntity entity){

        return "";
    }

}
