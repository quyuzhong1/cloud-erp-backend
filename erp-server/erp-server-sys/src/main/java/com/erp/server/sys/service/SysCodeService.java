package com.erp.server.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysCodeSkuDTO;
import com.erp.model.sys.entity.SysCodeEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/21 11:35
 */
public interface SysCodeService extends IService<SysCodeEntity> {

    /**
     * @description: 根据编码信息生成sku编码
     * @author Will
     * @date: 2022/11/21 12:15
     * @param dto
     * @return String
     */
    String getSkuNo(SysCodeSkuDTO dto);
    /**
     * @description: 根据编码信息生成spu编码
     * @author Will
     * @date: 2023/1/7 10:05
     * @param dto
     * @return String
     */
    String getSpuNo(SysCodeDTO dto);

    /**
     * @description: 根据编码信息生成时间格式的业务编码
     * @author Will
     * @date: 2023/3/9 10:28
     * @param dto
     * @return String
     */
    String getBusinessNo(SysCodeDTO dto);
}
