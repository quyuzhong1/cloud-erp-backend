package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.dto.UpdateSubjectShareDTO;
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
     * 设置专题分享信息
     * @author yl
     * @date 2022-12-08 17:15
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean setShare(UpdateSubjectShareDTO dto);

    
    /**
     * 获取分享给我的仪表盘id
     * @author yl
     * @date 2022-12-09 11:02
     * @param userId
     * @return java.util.List<java.lang.String>
     */
    List<String> getShareToMeDashboardIds(String userId);

    
    /**
     * 方法说明
     * @author yl
     * @date 2022-12-13 11:36
     * @param userList
     * @param subjectId
     * @return void
     */
    void addSubjectShare(List<String> userList, String subjectId);

    /**
     * 根据专题id 删除分享信息
     * @author yl
     * @date 2022-12-13 11:57
     * @param id
     * @return void
     */
    void deleteBySubjectId(String id);
}
