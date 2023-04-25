package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.QcRemarkDTO;
import com.erp.model.wms.entity.QcRemarkEntity;

import java.util.List;

/**
 * <p>
 * 质检单备注表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
public interface QcRemarkService extends SuperService<QcRemarkEntity> {

    /**
     * 质检备注 暂存
     * @author yl
     * @date 2023-04-19 11:23
     * @param billId
     * @param remarkList
     * @return void
     */
    void add(String billId, List<QcRemarkDTO.AddDTO> remarkList);

    /**
     * 获取到质检备注的信息
     * @author yl
     * @date 2023-04-19 14:01
     * @param id
     * @return java.util.List<com.erp.model.wms.dto.QcRemarkDTO.AddDTO>
     */
    List<QcRemarkDTO.AddDTO> getByMainId(String id);

    /**
     * 根据质检单id集合 查询备注信息
     * @author yl
     * @date 2023-04-19 19:31
     * @param billIdList
     * @return java.util.List<com.erp.model.wms.entity.QcBillRemarkEntity>
     */
    List<QcRemarkEntity> getByMainIdList(List<String> billIdList);
    /**
     * 根据质检单id 获取到质检信息
     *
     * @param mainIdList
     * @return com.erp.model.wms.entity.QcInfoEntity
     * @author yl
     * @date 2023-04-19 12:26
     */
    void removeByMainIds(List<String> mainIdList);
}
