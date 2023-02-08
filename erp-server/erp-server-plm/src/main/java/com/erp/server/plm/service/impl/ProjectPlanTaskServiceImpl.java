package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.dto.ProjectPlanTaskConditionDTO;
import com.erp.model.plm.entity.ProjectPlanTaskEntity;
import com.erp.model.plm.vo.ProductItemScheduleVO;
import com.erp.model.plm.vo.ProductTaskVO;
import com.erp.model.sys.dto.CustomizeFieldDisplayDTO;
import com.erp.server.plm.mapper.ProjectPlanTaskMapper;
import com.erp.server.plm.mapper.ProjectTaskMapper;
import com.erp.server.plm.service.ProjectPlanTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 项目计划任务表(ProjectPlanTask)表服务实现类
 *
 * @author yl
 * @since 2023-02-03 15:05:42
 */
@Service
public class ProjectPlanTaskServiceImpl extends ServiceImpl<ProjectPlanTaskMapper, ProjectPlanTaskEntity> implements ProjectPlanTaskService {


    @Resource
    private ProjectTaskMapper projectTaskMapper;

    /**
     * 根据条件获取到项目计划任务
     *
     * @param dto
     * @return com.erp.model.plm.vo.ProductItemScheduleVO
     * @author yl
     * @date 2023-02-03 15:54
     */
    @Override
    public ProductItemScheduleVO getTaskList(ProjectPlanTaskConditionDTO dto) {
        ProductItemScheduleVO resultVO = new ProductItemScheduleVO();

        List<ProductTaskVO> taskList = projectTaskMapper.getScheduleTask(dto);


        return null;
    }


    /**
     * 导出
     */
    @Override
    public void export() {

    }


    /**
     * 导入
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-02-03 18:37
     */
    @Override
    public Boolean importTaskschedule() {
        return null;
    }

    @Override
    public Boolean fieldSet(List<CustomizeFieldDisplayDTO> dto) {
        return null;
    }

    @Override
    public List<CustomizeFieldDisplayDTO> fieldShow() {
        return null;
    }
}
