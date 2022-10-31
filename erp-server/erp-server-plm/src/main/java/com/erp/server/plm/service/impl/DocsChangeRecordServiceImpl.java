package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.entity.DocsChangeRecordEntity;
import com.erp.server.plm.mapper.DocsChangeRecordMapper;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.DocsChangeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 *
 */
@Service
public class DocsChangeRecordServiceImpl extends ServiceImpl<DocsChangeRecordMapper, DocsChangeRecordEntity>
        implements DocsChangeRecordService {

    @Autowired
    private CommonService commonService;


    /**
     * 添加变更记录
     *
     * @param content
     * @param taskId
     * @param finishDocsId
     * @param processId
     * @return void
     * @author yl
     * @date 2022-10-14 12:11
     */
    @Override
    public void addRecord(String content, String taskId, String finishDocsId, String processId) {
        DocsChangeRecordEntity recordEntity = new DocsChangeRecordEntity();
        LoginUser loginUser = commonService.getUserInfo();
        recordEntity.setContent(content);
        recordEntity.setCreateUserId(loginUser.getUid());
        recordEntity.setCreateUserName(loginUser.getUserName());
        recordEntity.setFinishDocsId(finishDocsId);
        recordEntity.setProcessId(processId);
        recordEntity.setTaskId(taskId);
        this.save(recordEntity);
    }

    /**
     * 获取变更记录
     *
     * @param taskId
     * @return java.util.List<com.erp.model.plm.entity.DocsChangeRecordEntity>
     * @author yl
     * @date 2022-10-14 12:19
     */
    @Override
    public List<DocsChangeRecordEntity> listByTaskId(String taskId) {
        LambdaQueryWrapper<DocsChangeRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocsChangeRecordEntity::getTaskId, taskId);
        queryWrapper.orderByDesc(DocsChangeRecordEntity::getCreateTime);
        return this.list(queryWrapper);
    }
}




