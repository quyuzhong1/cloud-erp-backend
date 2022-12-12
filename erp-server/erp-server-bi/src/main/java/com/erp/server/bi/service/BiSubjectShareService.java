package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.dto.UpdateDashboardShareDTO;
import com.erp.model.bi.entity.BiSubjectShareEntity;

import java.util.List;


/**
 * 主题分享表(BiSubjectShare)表服务接口
 *
 * @author yl
 * @since 2022-12-08 14:33:46
 */
public interface BiSubjectShareService  extends IService<BiSubjectShareEntity> {



    /**
     * 设置仪表盘分享信息
     * @author yl
     * @date 2022-12-08 17:15
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean setShare(UpdateDashboardShareDTO dto);

    
    /**
     * 获取分享给我的仪表盘id
     * @author yl
     * @date 2022-12-09 11:02
     * @param userId
     * @return java.util.List<java.lang.String>
     */
    List<String> getShareToMeDashboardIds(String userId);
}
