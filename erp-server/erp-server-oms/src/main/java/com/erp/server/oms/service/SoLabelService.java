package com.erp.server.oms.service;
import com.erp.model.oms.entity.SoLabelEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.SoLabelDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * B2B订单面单表 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-02-24
 */
public interface SoLabelService extends SuperService<SoLabelEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-02-24
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SoLabelDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-02-24
    * @param dto
    * @return
    */
    Boolean update(SoLabelDTO.UpdateDTO dto);

    /**
     * 根据主表id获取面单记录
     * @param mainId
     * @return
     */
    SoLabelEntity getByMainId(String mainId);


    /**
     * 列表物流面单
     * @param ids
     * @return
     */
    List<SoLabelDTO.PrintLabelDTO> listLogisticsLabel(List<String> ids);

    /**
     * 合并物流面单
     * @param ids
     * @return
     */
    String printLogisticsLabel(List<String> ids);

    void changeLogisticsLabelToUrl();
}
