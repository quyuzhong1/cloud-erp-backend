package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.bi.dto.UpdateDashboardShareDTO;
import com.erp.model.bi.entity.BiSubjectEntity;
import com.erp.model.bi.entity.BiSubjectShareEntity;
import com.erp.server.bi.constant.IsDeleted;
import com.erp.server.bi.enums.DashboardEnum;
import com.erp.server.bi.mapper.BiSubjectShareMapper;
import com.erp.server.bi.service.BiSubjectService;
import com.erp.server.bi.service.BiSubjectShareService;
import com.erp.server.bi.service.CommonService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 主题分享表(BiSubjectShare)表服务实现类
 *
 * @author yl
 * @since 2022-12-08 14:33:46
 */
@Service
public class BiSubjectShareServiceImpl extends ServiceImpl<BiSubjectShareMapper, BiSubjectShareEntity> implements BiSubjectShareService {

    @Resource
    private BiSubjectService subjectService;

    @Resource
    private CommonService commonService;


    /**
     * 仪表盘设置权限
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-12-08 17:19
     */
    @Override
    public Boolean setShare(UpdateDashboardShareDTO dto) {
        //主题id
        String subjectId = dto.getId();
        BiSubjectEntity subject = subjectService.getById(subjectId);
        if (Objects.isNull(subject)) {
            throw new ServiceException(ApiError.ERROR_97000);
        }
        String  loginUserId = commonService.getUserInfo().getUid();
        if(!loginUserId.equals(subject.getCreateUserId())){
            throw new ServiceException(ApiError.ERROR_97002);
        }
        String shareFlag = dto.getShareFlag();
        String name = dto.getName();
        //当名子不一样就改变名字
        if (!name.equals(subject.getName())) {
            subjectService.checkName(subjectId, name);
            subject.setName(name);
        }
        subject.setShareFlag(shareFlag);
        //如果是分享
        if (DashboardEnum.SHARE.getFlag().equals(shareFlag)) {
            List<BiSubjectShareEntity> addList = new ArrayList<>();
            List<String> userList = dto.getUserIdList();
            for (String userId : userList) {
                BiSubjectShareEntity share = new BiSubjectShareEntity();
                share.setSubjectId(subjectId);
                share.setUserId(userId);
                addList.add(share);
            }
            return this.saveBatch(addList);
        }
        return true;
    }


    /**
     * 获取分享给我的仪表盘id
     *
     * @param userId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-12-09 11:02
     */
    @Override
    public List<String> getShareToMeDashboardIds(String userId) {
        LambdaQueryWrapper<BiSubjectShareEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(BiSubjectShareEntity::getSubjectId);
        queryWrapper.eq(BiSubjectShareEntity::getUserId, userId);
        return this.listObjs(queryWrapper, Object::toString);
    }

    /**
     * 根据主题id  删除 对应的分享信息
     *
     * @param subjectId
     * @return void
     * @author yl
     * @date 2022-12-08 17:22
     */
    public void deleteShare(String subjectId) {
        LambdaUpdateWrapper<BiSubjectShareEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(BiSubjectShareEntity::getIsDeleted, IsDeleted.YES);
        updateWrapper.eq(BiSubjectShareEntity::getSubjectId, subjectId);
        this.update(updateWrapper);

    }
}
