package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.business.dto.base.BaseSearchDTO;
import com.erp.common.business.dto.base.PagingDTO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.common.business.vo.PagingVO;
import com.erp.model.plm.dto.BasicDTO;
import com.erp.model.plm.dto.TaskPhaseDTO;
import com.erp.model.plm.dto.UpdateBasicNameDTO;
import com.erp.model.plm.entity.SysTaskPhaseEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.mapper.SysTaskPhaseMapper;
import com.erp.server.plm.service.ProjectTaskSysService;
import com.erp.server.plm.service.SysTaskPhaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname SysTaskPhaseServiceImpl
 * @Description TODO
 * @Date 2022-09-13 16:35
 * @Created by yl
 */
@Service
public class SysTaskPhaseServiceImpl extends ServiceImpl<SysTaskPhaseMapper, SysTaskPhaseEntity> implements SysTaskPhaseService {


    @Autowired
    private ProjectTaskSysService projectTaskSysService;

    /**
     * 修改阶段名称
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-09-13 16:44
     */
    @Override
    public void updateTaskPhase(UpdateBasicNameDTO dto) {

        String taskPhaseName = dto.getName();
        //检查任务阶段名 是否存在
        checkTaskPhaseName(taskPhaseName);
        SysTaskPhaseEntity entity = this.getById(dto.getId());
        if (!Objects.isNull(entity)) {
            Integer isProjectApproval = entity.getIsProjectApproval();
            //如果是立项任务阶段名  那就不能更改
            if (IsConstant.YES.equals(isProjectApproval) && !taskPhaseName.equals(entity.getName())) {
                throw new ServiceException(ApiError.ERROR_95008);
            }
        }
        this.updateById(entity);
    }


    /**
     * 保存 系统阶段
     *
     * @param list
     * @return void
     * @author yl
     * @date 2022-09-13 16:51
     */
    @Override
    public void batchSaveOrUpdate(List<UpdateBasicNameDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_95002);
        }
        List<String> phaseNames = list.stream().filter(p -> StringUtils.isBlank(p.getId())).map(UpdateBasicNameDTO::getName).collect(Collectors.toList());
        //获取系统 任务阶段名集合
        List<String> sysTaskPhaseNames = getSysTaskPhaseNameList();
        //获取交集
        List<String> intersections = phaseNames.stream().filter(item -> sysTaskPhaseNames.contains(item)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(intersections)) {
            String intersectionName = String.join(",", intersections);
            throw new ServiceException(1, intersectionName + " 阶段名已存在");
        }
        List<SysTaskPhaseEntity> saveList = new LinkedList<>();
        for (UpdateBasicNameDTO item : list) {
            SysTaskPhaseEntity entity = new SysTaskPhaseEntity();
            entity.setName(item.getName());
            entity.setId(item.getId());
            if (TaskConstant.APPROVAL_TASK_NAME.equals(item.getName())) {
                entity.setIsProjectApproval(IsConstant.YES);
            } else {
                entity.setIsProjectApproval(IsConstant.NO);
            }
            saveList.add(entity);
        }
        this.saveOrUpdateBatch(saveList);
    }

    private List<String> getSysTaskPhaseNameList() {
        LambdaQueryWrapper<SysTaskPhaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SysTaskPhaseEntity::getName);
        queryWrapper.eq(SysTaskPhaseEntity::getIsProjectApproval,IsConstant.NO);
        return listObjs(queryWrapper,Object::toString);
    }


    //获取到系统任务阶段名集合
    @Override
    public List<SysTaskPhaseEntity> getSysTaskPhaseNames() {
        LambdaQueryWrapper<SysTaskPhaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTaskPhaseEntity::getIsProjectApproval,IsConstant.NO);
        return list(queryWrapper);
    }

    /**
     * 删除阶段名
     *
     * @param id
     * @return boolean
     * @author yl
     * @date 2022-10-09 15:57
     */
    @Override
    public boolean removeSysTaskPhase(String id) {
        SysTaskPhaseEntity taskPhase = this.getById(id);
        projectTaskSysService.checkQuotePhase(id);

        if (!Objects.isNull(taskPhase) && IsConstant.YES.equals(taskPhase.getIsProjectApproval())) {
            throw new ServiceException(ApiError.ERROR_95020);
        }
        return this.removeById(id);
    }

    @Override
    public List<BasicDTO> getSysTaskPhaseList() {
        List<SysTaskPhaseEntity> list = this.list();
        List<BasicDTO> resultList = new ArrayList<>(list.size());
        for (SysTaskPhaseEntity item : list) {
            BasicDTO basic = new BasicDTO();
            basic.setId(item.getId());
            basic.setName(item.getName());
            if (item.getIsProjectApproval().equals(IsConstant.YES)) {
                basic.setIfQuote(true);
            }
            resultList.add(basic);
        }

        return resultList;
    }

    @Override
    public PagingVO<SysTaskPhaseEntity> paging(PagingDTO<BaseSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params);

        return new PagingVO(pageData);
    }


    /**
     * 获取系统的 任务阶段名
     *
     * @param
     * @return java.util.List<com.erp.model.plm.dto.TaskPhaseDTO>
     * @author yl
     * @date 2022-09-13 17:42
     */
    @Override
    public List<TaskPhaseDTO> getSysTaskPhase(List<String> nameList) {
        return baseMapper.getSysTaskPhase(nameList);
    }


    /**
     * 检查阶段名是否存在
     *
     * @param taskPhaseName
     * @return void
     * @author yl
     * @date 2022-09-13 16:47
     */
    private void checkTaskPhaseName(String taskPhaseName) {
        LambdaQueryWrapper<SysTaskPhaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTaskPhaseEntity::getName, taskPhaseName);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95001);
        }

    }


}
