package com.cloud.erp.chrome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.cloud.erp.chrome.constant.ErpPlatform;
import com.cloud.erp.chrome.constant.TaskState;
import com.cloud.erp.chrome.dto.FindTaskDTO;
import com.cloud.erp.chrome.entity.ChromeTaskInfoEntity;
import com.cloud.erp.chrome.mapper.ChromeTaskInfoMapper;
import com.cloud.erp.chrome.service.ChromeTaskInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-08-25
 */
@Service
public class ChromeTaskInfoServiceImpl extends ServiceImpl<ChromeTaskInfoMapper, ChromeTaskInfoEntity> implements ChromeTaskInfoService {


    /**
     * 获取任务列表
     *
     * @param
     * @return java.util.List<com.cloud.erp.chrome.entity.ChromeTaskInfoEntity>
     * @author yl
     * @date 2022-08-26 9:10
     */
    @Override
    public List<ChromeTaskInfoEntity> getChromeTaskList(FindTaskDTO  dto) {
        List<String> list= Arrays.asList(ErpPlatform.GYY,ErpPlatform.MABANG,ErpPlatform.YXK);
        if(!list.contains(dto.getPlatform())){
            throw new ServiceException(1,"平台类型有误");
        }
        LambdaQueryWrapper<ChromeTaskInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChromeTaskInfoEntity::getTaskStatus, TaskState.NOT_START);
        queryWrapper.eq(ChromeTaskInfoEntity::getPlatform,dto.getPlatform());
        queryWrapper.last("LIMIT 1");
        return baseMapper.selectList(queryWrapper);
    }


    /**
     * 修改任务状态
     * @author yl
     * @date 2022-08-26 9:38
     * @param taskId
     * @param taskState
     * @return void
     */
    @Override
    public void updateTaskState(Integer taskId, Integer taskState) {
        UpdateWrapper<ChromeTaskInfoEntity> updateWrapper=new UpdateWrapper<>();
        updateWrapper.lambda().eq(ChromeTaskInfoEntity::getId,taskId);
        updateWrapper.lambda().set(ChromeTaskInfoEntity::getTaskStatus,taskState);
        updateWrapper.lambda().set(ChromeTaskInfoEntity::getUpdateTime,new Date());
        this.update(updateWrapper);

    }
}
