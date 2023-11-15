package com.erp.server.oms.service;

import com.common.business.dto.PlatformOrderDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoB2cFinanceDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cFinanceEntity;

import java.util.List;

/**
 * <p>
 * B2C销售订单财务信息表 服务类
 * </p>
 *
 * @author will
 * @since 2023-09-08
 */
public interface SoB2cFinanceService extends SuperService<SoB2cFinanceEntity> {

    /**
    * 新增
    * @author will
    * @date: 2023-09-08
    * @param dto
    * @return
    */
    String add(SoB2cFinanceDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2023-09-08
    * @param dto
    * @return
    */
    Boolean update(SoB2cFinanceDTO.UpdateDTO dto);

    /**
     * 根据父级id查询
     */
    SoB2cFinanceEntity getByMainId(String mainId);

    Boolean deleteByMainIds(List<String> mainIds);
    /**
     * 平台订单明细更新或保存
     *
     * @Author Jim
     * @since 2023-11-10
     **/
    void saveOrUpdateEntity(PlatformOrderDTO dto, SoB2cEntity mainEntity);
}
