package com.erp.server.plm.service;

import com.common.business.service.SuperService;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.model.plm.entity.MouldDetailEntity;
import com.erp.model.plm.entity.MouldInfoEntity;

import java.util.List;

/**
 * <p>
 * 模具明细 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
public interface MouldDetailService extends SuperService<MouldDetailEntity> {


    /**
     * 新增详情
     * @param detailList 详情
     * @param entity     主表
     */
    void add(List<MouldInfoDTO.DetailDTO> detailList, MouldInfoEntity entity);
}
