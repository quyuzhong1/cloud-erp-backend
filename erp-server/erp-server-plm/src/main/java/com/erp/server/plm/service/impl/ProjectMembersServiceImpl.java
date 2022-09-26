package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.model.plm.dto.ProjectMemberDTO;
import com.erp.model.plm.entity.ProjectMembersEntity;
import com.erp.server.plm.mapper.ProjectMembersMapper;
import com.erp.server.plm.service.ProjectMembersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * 项目成员表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
public class ProjectMembersServiceImpl extends ServiceImpl<ProjectMembersMapper, ProjectMembersEntity> implements ProjectMembersService {

    @Override
    public void add(String productId, String projectId, List<ProjectMemberDTO> members) {
        List<ProjectMembersEntity> addList = new LinkedList<>();
        for (ProjectMemberDTO item : members) {
            ProjectMembersEntity entity = new ProjectMembersEntity();
            entity.setMemberId(item.getUserId());
            entity.setMemberName(item.getUsetName());
            entity.setProjectId(projectId);
            entity.setProductId(productId);
            addList.add(entity);
        }
        this.saveBatch(addList);
    }


    /**
     * 保存模板的成员
     *
     * @param flagId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-09-20 14:52
     */
    @Override
    public void saveMember(String flagId, String productId) {
        List<ProjectMembersEntity> list = getListByProductId(productId);
        if (CollectionUtils.isNotEmpty(list)) {
            for (ProjectMembersEntity entity : list) {
                entity.setFlagId(flagId);
            }
            this.saveBatch(list);
        }
    }


    /**
     * 保存项目成员 从项目复制
     *
     * @param projectId
     * @param flagId
     * @return void
     * @author yl
     * @date 2022-09-20 17:36
     */
    @Override
    public void saveMemberByProject(String productId, String projectId, String flagId) {
        List<ProjectMembersEntity> list = getListByProductId(flagId);
        if (CollectionUtils.isNotEmpty(list)) {
            for (ProjectMembersEntity entity : list) {
                entity.setProjectId(projectId);
                entity.setProductId(productId);
            }
            this.saveBatch(list);
        }


    }

    @Override
    public void saveMemberByTemplate(String productId, String projectId, String flagId) {
        LambdaQueryWrapper<ProjectMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectMembersEntity::getFlagId, flagId);
        List<ProjectMembersEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            for (ProjectMembersEntity entity : list) {
                entity.setProjectId(projectId);
                entity.setProductId(productId);
            }
            this.saveBatch(list);
        }

    }



    @Override
    public List<ProjectMembersEntity> getListByProductId(String productId) {
        LambdaQueryWrapper<ProjectMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectMembersEntity::getProductId, productId);
        return this.list(queryWrapper);
    }
}
