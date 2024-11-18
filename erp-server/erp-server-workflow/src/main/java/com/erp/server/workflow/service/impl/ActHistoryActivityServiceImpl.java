package com.erp.server.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.constant.SqlConstants;
import com.erp.model.workflow.dto.ActivityDTO;
import com.erp.model.workflow.entity.ActHistoryActivityEntity;
import com.erp.server.workflow.mapper.ActHistoryActivityMapper;
import com.erp.server.workflow.service.ActHistoryActivityService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-08-12
 */
@Service
public class ActHistoryActivityServiceImpl extends ServiceImpl<ActHistoryActivityMapper, ActHistoryActivityEntity> implements ActHistoryActivityService {


    /**
     * 保存
     *
     * @param activityDTO
     * @return void
     * @author yl
     * @date 2022-08-12 11:46
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveActivity(ActivityDTO activityDTO) {
        //当前流程id
        String nowActivityId = activityDTO.getNowActivityId();
        //流程id
        String processInstanceId = activityDTO.getProcessInstanceId();
        //获取最后一个实体
        ActHistoryActivityEntity entity = findByProcessInstanceId(processInstanceId);
        ActHistoryActivityEntity saveEntity = new ActHistoryActivityEntity();
        String preActivityId = "";
        if (!Objects.isNull(entity)) {
            preActivityId = entity.getActivityId();
        }
        saveEntity.setActivityId(nowActivityId);

        saveEntity.setPreActivityId(preActivityId);
        saveEntity.setNextActivityId("");
        saveEntity.setProcessInstanceId(processInstanceId);
        if (StringUtils.isNotBlank(activityDTO.getAuditStatus())) {
            saveEntity.setAuditStatus(activityDTO.getAuditStatus());
        }
        this.save(saveEntity);

        if (!Objects.isNull(entity)) {
            //当前的节点 就上上一个节点 的下一节点
            entity.setNextActivityId(nowActivityId);
            this.updateById(entity);
        }

    }


    /**
     * 活动当前活动的上一个活动id
     *
     * @param processInstanceId 流程id
     * @param nowActivityId     当前活动id
     * @return java.lang.String
     * @author yl
     * @date 2022-08-12 12:24
     */
    @Override
    public String getProActivityId(String processInstanceId, String nowActivityId) {
        ActHistoryActivityEntity lastEntity = getLast(processInstanceId, nowActivityId);
        /**
         * 如果为空的话 有一种可能就是 最开始那条
         * 存在A->B  然后B驳回到A的
         */
        if (Objects.isNull(lastEntity)) {
            List<ActHistoryActivityEntity> list = getListByprocessInstanceId(processInstanceId);
            if (!CollectionUtils.isEmpty(list)) {
                return list.get(0).getActivityId();
            } else {
                return "";
            }

        }
        return lastEntity.getPreActivityId();

    }


    /**
     * 根据流程id 获取
     *
     * @param processId
     * @return java.util.List<com.erp.model.workflow.entity.ActHistoryActivityEntity>
     * @author yl
     * @date 2023-02-11 18:26
     */
    @Override
    public List<ActHistoryActivityEntity> getByProcessId(String processId) {
        LambdaQueryWrapper<ActHistoryActivityEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActHistoryActivityEntity::getProcessInstanceId, processId);
        queryWrapper.orderByDesc(ActHistoryActivityEntity::getCreateTime);
        return this.list(queryWrapper);
    }


    /**
     * 获取最后一个
     *
     * @param processInstanceId
     * @param nowActivityId
     * @return com.cloud.erp.workflow.modules.workflow.entity.ActHistoryActivityEntity
     * @author yl
     * @date 2022-08-12 16:28
     */

    public ActHistoryActivityEntity getLast(String processInstanceId, String nowActivityId) {
        LambdaQueryWrapper<ActHistoryActivityEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActHistoryActivityEntity::getProcessInstanceId, processInstanceId);
        queryWrapper.eq(ActHistoryActivityEntity::getActivityId, nowActivityId);
        queryWrapper.ne(ActHistoryActivityEntity::getPreActivityId, "");
        queryWrapper.orderByDesc(ActHistoryActivityEntity::getCreateTime);
        queryWrapper.last(SqlConstants.LIMIT_1);
        return this.getOne(queryWrapper);
    }

    public List<ActHistoryActivityEntity> getListByprocessInstanceId(String processInstanceId) {
        LambdaQueryWrapper<ActHistoryActivityEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActHistoryActivityEntity::getProcessInstanceId, processInstanceId);
        queryWrapper.orderByDesc(ActHistoryActivityEntity::getCreateTime);
        queryWrapper.last(SqlConstants.LIMIT_1);
        return this.list(queryWrapper);
    }

    /**
     * 根据 流程id　获取流程活动实体
     *
     * @param
     * @return com.cloud.erp.workflow.modules.workflow.entity.ActHistoryActivityEntity
     * @author yl
     * @date 2022-08-12 11:53
     */

    public ActHistoryActivityEntity findByProcessInstanceId(String processInstanceId) {
        LambdaQueryWrapper<ActHistoryActivityEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActHistoryActivityEntity::getProcessInstanceId, processInstanceId);
        queryWrapper.orderByDesc(ActHistoryActivityEntity::getCreateTime);
        queryWrapper.last(SqlConstants.LIMIT_1);
        return baseMapper.selectOne(queryWrapper);

    }
}
