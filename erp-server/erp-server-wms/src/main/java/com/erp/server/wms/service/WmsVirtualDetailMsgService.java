package com.erp.server.wms.service;
import com.erp.model.wms.entity.WmsVirtualDetailMsgEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.WmsVirtualDetailMsgDTO;

import java.util.List;

/**
 * <p>
 * wms虚拟仓明细同步表 服务类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
public interface WmsVirtualDetailMsgService extends SuperService<WmsVirtualDetailMsgEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(WmsVirtualDetailMsgDTO.AddDTO dto);

    /**
     * 虚拟仓明细同步任务
     * @author will
     * @date 2024/12/9 19:22
     */
    void virtualDetailMsgJob();
    /**
     * 更新状态
     * @author will
     * @date 2024/12/13 16:50
     * @param entity
     */
    void updateStatus(WmsVirtualDetailMsgEntity entity);
    /**
     * 根据业务ids变更状态
     * @author will
     * @date 2025/11/26 17:22
     * @param pageIdList
     * @return void
     */
    void updateStatusByBusinessIds(List<String> pageIdList,String status);
}
