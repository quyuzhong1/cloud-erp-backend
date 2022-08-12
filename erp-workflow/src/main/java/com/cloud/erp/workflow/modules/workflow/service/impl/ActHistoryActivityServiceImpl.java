package com.cloud.erp.workflow.modules.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.erp.workflow.modules.workflow.dto.ActivityDTO;
import com.cloud.erp.workflow.modules.workflow.entity.ActHistoryActivityEntity;
import com.cloud.erp.workflow.modules.workflow.mapper.ActHistoryActivityMapper;
import com.cloud.erp.workflow.modules.workflow.service.ActHistoryActivityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
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
    @Transactional
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
        this.save(saveEntity);

        if(!Objects.isNull(entity)){
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
        ActHistoryActivityEntity  lastEntity=getLast(processInstanceId,nowActivityId);
        /**
         * 如果为空的话 有一种可能就是 最开始那条
         * 存在A->B  然后B驳回到A的
         */
        if(Objects.isNull(lastEntity)){
            List<ActHistoryActivityEntity>  list=getListByprocessInstanceId(processInstanceId);
            if(CollectionUtils.isNotEmpty(list)){
                return list.get(0).getActivityId();
            }else{
                return "";
            }

        }
        return  lastEntity.getPreActivityId();

    }





    /**
     * 获取最后一个
     * @author yl
     * @date 2022-08-12 16:28
     * @param processInstanceId
     * @param nowActivityId
     * @return com.cloud.erp.workflow.modules.workflow.entity.ActHistoryActivityEntity
     */

    public ActHistoryActivityEntity getLast(String processInstanceId, String nowActivityId){
        LambdaQueryWrapper<ActHistoryActivityEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActHistoryActivityEntity::getProcessInstanceId,processInstanceId);
        queryWrapper.eq(ActHistoryActivityEntity::getActivityId,nowActivityId);
        queryWrapper.ne(ActHistoryActivityEntity::getPreActivityId,"");
        queryWrapper.orderByDesc(ActHistoryActivityEntity::getCreateTime);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    public List<ActHistoryActivityEntity> getListByprocessInstanceId(String processInstanceId){
        LambdaQueryWrapper<ActHistoryActivityEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActHistoryActivityEntity::getProcessInstanceId,processInstanceId);
        queryWrapper.orderByDesc(ActHistoryActivityEntity::getCreateTime);
        queryWrapper.last("LIMIT 1");
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
        queryWrapper.last("LIMIT 1");
        return baseMapper.selectOne(queryWrapper);

    }
}
