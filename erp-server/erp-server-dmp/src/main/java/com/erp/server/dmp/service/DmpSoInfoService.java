package com.erp.server.dmp.service;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSoInfoDTO;
import com.erp.model.dmp.gyy.GyyOrderEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 中台销售订单表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-24
 */
public interface DmpSoInfoService extends SuperService<DmpSoInfoEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-24
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSoInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-24
    * @param dto
    * @return
    */
    Boolean update(DmpSoInfoDTO.UpdateDTO dto);

    void addGyyOrder(List<GyyOrderEntity> mongoData);

    /**
     * 查询缺失明细的订单
     */
    List<DmpSoInfoEntity> findSoMissingDetail(LocalDateTime startTime, LocalDateTime endTime, String sourceSystem, String nextLevelId);

    /**
     * 领星导入
     */
    void lxSoUpdate();

    /**
     *
     * 根据平台订单号查询明细
     * @param platformCode
     * @return
     */
    List<AfterSaleDTO.DropDownDTO> listDetailByPlatformCode(String platformCode);

}
