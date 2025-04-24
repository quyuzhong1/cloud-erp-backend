package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.dto.UpdateSubjectShareDTO;
import com.erp.model.bi.entity.BiSubjectEntity;
import com.erp.model.bi.entity.BiSubjectShareEntity;
import com.erp.model.bi.enums.BiShareIdentityTypeEnum;

import java.util.List;
import java.util.Map;


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
     * @param roleIdList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-12-09 11:02
     */
    /**
     * @deprecated
     * This method is deprecated and will be removed in future versions.
     * Please use {@link #getUserVisibleModuleIdsNew(String)} instead.
     */
    @Deprecated
    List<String> getShareToMeDashboardIds(String userId, List<String> roleIdList);


    /**
     * 方法说明
     *
     * @param identityIdList
     * @param subjectId
     * @param identityTypeEnum
     * @return void
     * @author yl
     * @date 2022-12-13 11:36
     */
    Boolean addSubjectShare(List<String> identityIdList, String subjectId, BiShareIdentityTypeEnum identityTypeEnum);

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
    void checkPermission(String userId, BiSubjectEntity subject, List<String> roleIdList);

    /**
     * 获取可以看到的 专题 的用户id
     * @param subjectId
     * @return
     */
    List<String> getUserIdsBySubjectId(String subjectId);

    /**
     * 检查和添加共享记录
     * @param shareFlagIdList
     * @param subjectId
     * @param shareFlag
     */
    void checkAndAddSubjectShare(List<String> shareFlagIdList, String subjectId, String shareFlag);

    /**
     * 查询用户支持的专题
     * @param userId
     * @param roleIdList
     * @return
     */
    List<String> findSubjectId(String userId, List<String> roleIdList);

    /**
     * 当前用户是否有权限
     */
    Boolean getShare(String userId, String subjectId, List<String> roleIdList);

    /**
     * 当通过Subject查询
     */
    List<BiSubjectShareEntity> findBySubjectId(String subjectId);

    /**
     * 查询权限信息
     */
    Map<String, List<BiSubjectShareEntity>> mapBySubjectIds(List<String> subjectIds);
}
