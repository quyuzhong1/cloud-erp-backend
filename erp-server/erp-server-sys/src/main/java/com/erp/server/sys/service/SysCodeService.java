package com.erp.server.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysCodeSkuDTO;
import com.erp.model.sys.entity.SysCodeEntity;

import java.util.List;

/**
 * @author Will
 * @version 1.0

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
     * @description: 一次性生成 count 个连续的 sku 编号，避免循环调用导致的分布式锁竞争
     * @author Will
     * @param dto 编码请求（type、category）
     * @param count 需要生成的编号数量
     * @return List 按顺序返回 count 个 sku 编号
     */
    List<String> getSkuNoBatch(SysCodeSkuDTO dto, int count);

    /**
     * @description: 根据编码信息生成spu编码
     * @author Will
     * @date: 2023/1/7 10:05
     * @param dto
     * @return String
     */
    String getSpuNo(SysCodeDTO dto);


    /**
     * @description: 根据编码信息生成流水的业务编码
     * @author Will
     * @date: 2023/3/9 10:28
     * @param dto
     * @return String
     */
    String getSeqNo(SysCodeDTO dto);

    /**
     * @description: 根据编码信息生成时间格式的业务编码
     * @author Will
     * @date: 2023/3/9 10:28
     * @param dto
     * @return String
     */
//    String getBusinessNo(SysCodeDTO dto);
}
