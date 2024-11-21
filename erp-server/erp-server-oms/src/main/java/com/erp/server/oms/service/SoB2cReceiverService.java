package com.erp.server.oms.service;

import com.common.business.dto.PlatformOrderDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoB2cReceiverDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.sys.entity.DictCountryEntity;

import java.util.List;


/**
 * <p>
 * B2C销售订单买家信息表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
public interface SoB2cReceiverService extends SuperService<SoB2cReceiverEntity> {

    /**
     * @description: 新增
     * @author Will
     * @date: 2023/8/21 17:13
     * @param receiverDTO
     * @param mainId
     * @return Boolean
     */
    Boolean add(SoB2cReceiverDTO.AddDTO receiverDTO, String mainId);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/8/21 17:13
     * @param receiverDTO
     * @param mainId
     * @return Boolean
     */
    Boolean update(SoB2cReceiverDTO.UpdateDTO receiverDTO, String mainId);
    /**
     * @description: 根据主表id查询
     * @author Will
     * @date: 2023/8/22 11:16
     * @param mainId
     * @return SoB2cReceiverEntity
     */
    SoB2cReceiverEntity getByMainId(String mainId);
    /**
     * @description: 根据主表ids查询
     * @author Will
     * @date: 2023/8/23 10:04
     * @param mainIds
     * @return List<SoB2cReceiverEntity>
     */
    List<SoB2cReceiverEntity> listByMainIds(List<String> mainIds);
    /**
     * @description: 根据主表id删除
     * @author Will
     * @date: 2023/8/23 12:27
     * @param mainIds
     * @return Boolean
     */
    Boolean deleteByMainIds(List<String> mainIds);

    /**
     * 平台订单明细更新或保存
     *
     * @Author Jim
     * @since 2023-11-10
     **/
    SoB2cReceiverEntity saveOrUpdateEntity(PlatformOrderDTO dto, SoB2cEntity mainEntity, List<DictCountryEntity> countryList ,boolean notUpdateAddress);

    /**
     * 更新指定字段
     * @param receiver
     */
    void updateFieldById(SoB2cReceiverEntity receiver);
}
