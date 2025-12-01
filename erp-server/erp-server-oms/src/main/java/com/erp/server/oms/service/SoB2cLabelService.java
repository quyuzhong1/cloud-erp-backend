package com.erp.server.oms.service;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoB2cLabelDTO;
import com.erp.model.oms.entity.SoB2cLabelEntity;

import java.util.List;

/**
 * <p>
 * 订单标签，面单表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-04-18
 */
public interface SoB2cLabelService extends SuperService<SoB2cLabelEntity> {

    /**
     * 新增订单标签
     * @Author Luo_WG
     * @Date 2024/4/18 9:17
     * @param dtoList
     * @return java.lang.Boolean
     **/
    Boolean saveSoB2cLabel(List<SoB2cLabelDTO.UpdateDTO> dtoList);

    /**
     * 根据主表id删除数据
     * @Author Luo_WG
     * @Date 2024/4/18 9:22
     * @param mainIds
     * @return java.lang.Boolean
     **/
    Boolean deleteByMainIds(List<String> mainIds);

    /**
     * 根据订单id查询标签
     * @Author Luo_WG
     * @Date 2024/4/18 9:31
     * @param mainIds
     * @return java.util.List<com.erp.model.oms.entity.SoB2cLabelEntity>
     **/
    List<SoB2cLabelEntity> listSoB2cLabelByMainIds(List<String> mainIds);

    void ManualUploadLabel(String base64, String id);

    /**
     * 更新跨境物流面单url
     * @param mainId
     * @param crossLabelUrl
     */

    void updateCrossLabelUrl(String mainId, String crossLabelUrl);
}
