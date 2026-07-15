package com.erp.server.tms.service;
import com.erp.model.tms.entity.TmsDeclareBillDetailEntity;
import com.common.business.service.SuperService;
import com.erp.model.tms.dto.TmsDeclareBillDetailDTO;
import com.erp.model.tms.entity.TmsDeclareBillEntity;

import java.util.List;

/**
 * <p>
 * 报关单明细 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-03-27
 */
public interface TmsDeclareBillDetailService extends SuperService<TmsDeclareBillDetailEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-03-27
    * @return
    */
    Boolean add(TmsDeclareBillEntity tmsDeclareBillEntity, List<TmsDeclareBillDetailEntity> detailEntityList);

    /**
    * 修改
    * @author lrp
    * @date: 2024-03-27
    * @param dto
    * @return
    */
    Boolean update(TmsDeclareBillDetailDTO.UpdateDTO dto);

    List<TmsDeclareBillDetailEntity> listByMainIds(List<String> mainId);
    /**
     * 根据主表id删除详情
     * @author will
     * @date 2026/4/30 10:57
     * @param mainIdList
     */
    Boolean deleteDetailByMainIdList(List<String> mainIdList);
}
