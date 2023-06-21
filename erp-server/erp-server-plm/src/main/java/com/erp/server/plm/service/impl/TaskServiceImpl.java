package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.TaskDTO;
import com.erp.model.plm.dto.TaskPagingShowDTO;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.server.plm.mapper.ProjectTaskMapper;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lambda
 * @Classname TaskServiceImpl
 * @Description TODO
 * @Date 2023-06-20 19:50
 * @Created by yl
 */
@Service
public class TaskServiceImpl extends ServiceImpl<ProjectTaskMapper, ProjectTaskEntity> implements TaskService {


    @Resource
    private CommonService commonService;

    /**
     * 执行的任务列表分页查询 可执行表示没有前置任务 或者 任务没有完成和取消
     *
     * @param searchParamDTO
     * @return com.common.business.vo.PagingVO<java.util.List < com.erp.model.plm.dto.TaskPagingShowDTO>>
     * @author yl
     * @date 2023-06-21 9:04
     */
    @Override
    public PagingVO<List<TaskPagingShowDTO>> allExecutablePaging(PagingDTO<TaskDTO.TaskPagingParamDTO> searchParamDTO) {
        //当前登录的用户id
        String loginUserId = commonService.getUserInfo().getUid();
        TaskDTO.TaskPagingParamDTO params = searchParamDTO.getParams();
        params.setPermissionSql(searchParamDTO.getPermissionSql());
        Page query = new Page(searchParamDTO.getCurrPage(), searchParamDTO.getPageSize());
        //分组的标示
        String groupNameFlag = params.getGroupNameFlag();
        //是否分组
        Boolean ifGroup = false;
        //表示不分组
        if (StringUtils.isNotBlank(groupNameFlag) && !"no".equals(groupNameFlag)) {
            ifGroup = true;
        }

        return null;
    }
}
