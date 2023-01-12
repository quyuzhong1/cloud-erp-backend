package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.dto.UpdateSubjectShareDTO;
import com.erp.model.bi.entity.BiSubjectEntity;
import com.erp.model.bi.entity.BiSubjectShareEntity;

import java.util.List;


/**
 * 主题分享表(BiSubjectShare)表服务接口
 *
 * @author yl
 * @since 2022-12-08 14:33:46
 */
public interface BiSubjectShareService extends IService<BiSubjectShareEntity> {


    /**
     * 设置专题分享信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-12-08 17:15
     */
    String setShare(UpdateSubjectShareDTO dto);


    /**
     * 获取分享给我的仪表盘id
     *
     * @param userId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-12-09 11:02
     */
    List<String> getShareToMeDashboardIds(String userId);


    /**
     * 方法说明
     *
     * @param userList
     * @param subjectId
     * @return void
     * @author yl
     * @date 2022-12-13 11:36
     */
    Boolean addSubjectShare(List<String> userList, String subjectId);

    /**
     * 根据专题id 删除分享信息
     *
     * @param id
     * @return void
     * @author yl
     * @date 2022-12-13 11:57
     */
    void deleteBySubjectId(String id);

    /**
     * 检查用户是否可见该专题
     *
     * @param userId
     * @param subject
     * @return void
     * @author yl
     * @date 2022-12-13 18:10
     */
    void checkPermission(String userId, BiSubjectEntity subject);

    /**
     * 获取可以看到的 专题 的用户id
     * @param subjectId
     * @return
     */
    List<String> getUserIdsBySubjectId(String subjectId);
}
