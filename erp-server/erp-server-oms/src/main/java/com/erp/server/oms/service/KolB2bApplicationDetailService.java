package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.KolB2bApplicationDetailDTO;
import com.erp.model.oms.entity.KolB2bApplicationDetailEntity;

import java.util.List;

/**
 * <p>
 * B2B寄样申请明细表 服务类
 * </p>
 *
 * @author will
 * @since 2025-12-01
 */
public interface KolB2bApplicationDetailService extends SuperService<KolB2bApplicationDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-12-01
    * @param detailList
    * @param mainId
    * @return
    */
    Boolean add(List<KolB2bApplicationDetailDTO.AddDTO> detailList,String mainId);

    /**
    * 修改
    * @author will
    * @date: 2025-12-01
    * @param detailList
    * @param mainId
    * @return
    */
    Boolean update(List<KolB2bApplicationDetailDTO.UpdateDTO> detailList,String mainId);

    /**
     * 根据主表id删除明细
     * @author will
     * @date 2025/12/1 19:12
     * @param mainId
     * @return Boolean
     */
    Boolean deleteByMainId(String mainId);
    /**
     * 根据主表id列表获取明细列表
     * @author will
     * @date 2025/12/2 09:35
     * @param strings
     * @return List<KolB2bApplicationDetailEntity>
     */
    List<KolB2bApplicationDetailEntity> listByMainIdList(List<String> strings);
}
