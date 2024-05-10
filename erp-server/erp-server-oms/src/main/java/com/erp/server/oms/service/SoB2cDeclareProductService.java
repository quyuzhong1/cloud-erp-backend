package com.erp.server.oms.service;
import com.erp.model.oms.entity.SoB2cDeclareProductEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.SoB2cDeclareProductDTO;

import java.util.List;

/**
 * <p>
 * B2C销售订单申报产品信息表 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-05-09
 */
public interface SoB2cDeclareProductService extends SuperService<SoB2cDeclareProductEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-05-09
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SoB2cDeclareProductDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-05-09
    * @param dto
    * @return
    */
    Boolean update(SoB2cDeclareProductDTO.UpdateDTO dto);

    /**
     * 根据销售订单id获取申报信息
     * @param id
     * @return
     */
    List<SoB2cDeclareProductEntity> listBySoId(String id);

    /**
     * 根据订单id删除申报信息
     * @param id
     */
    void removeBySoId(String id);

    /**
     * 查询订单申报信息
     * @param ids
     * @return
     */
    List<SoB2cDeclareProductDTO.ViewDTO> listViewBySoId(List<String> ids);
}
