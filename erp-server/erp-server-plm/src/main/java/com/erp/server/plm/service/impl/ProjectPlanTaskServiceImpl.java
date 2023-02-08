package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.CustomizeFieldEnum;
import com.erp.common.dto.base.SortParamDTO;
import com.erp.common.enums.ModuleEnum;
import com.erp.model.plm.dto.ProjectPlanTaskConditionDTO;
import com.erp.model.plm.entity.PreTaskEntity;
import com.erp.model.plm.entity.ProjectPlanTaskEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.TaskDeliveryDocsEntity;
import com.erp.model.plm.vo.ProductItemScheduleVO;
import com.erp.model.plm.vo.ProductTaskVO;
import com.erp.model.plm.vo.ScheduleTaskVO;
import com.erp.model.sys.dto.CustomizeFieldHiddenDTO;
import com.erp.model.sys.dto.FindCustomizeFieldDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.mapper.ProjectPlanTaskMapper;
import com.erp.server.plm.mapper.ProjectTaskMapper;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.PreTaskService;
import com.erp.server.plm.service.ProjectPlanTaskService;
import com.erp.server.plm.service.TaskDeliveryService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Resource
    private PreTaskService preTaskService;

    @Resource
    private TaskDeliveryService taskDeliveryService;


    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private CommonService commonService;

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
        List<SortParamDTO> sortList = dto.getSortList();
        StringBuilder sb = new StringBuilder();
        if (CollectionUtils.isNotEmpty(sortList)) {
            boolean flag = false;
            for (SortParamDTO sort : sortList) {
                if (flag) {
                    sb.append(",");
                }
                sb.append(sort.getField());
                sb.append(" ");
                sb.append(sort.getField());
                flag = true;
            }
        }
        String sql = sb.toString();
        ProductItemScheduleVO resultVO = new ProductItemScheduleVO();
        String productId = dto.getProductId();
        List<ProductTaskVO> taskList = projectTaskMapper.getScheduleTask(dto, sql);
        List<ProductTaskVO> resultList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(taskList)) {
            //前置任务列表
            List<PreTaskEntity> preTaskList = preTaskService.getPreTaskByProductId(dto.getProductId());
            //交付文档列表
            List<TaskDeliveryDocsEntity> deliveryDocsList = taskDeliveryService.getByProductId(productId);

            //根据阶段分组
            Map<String, List<ProductTaskVO>> map = taskList.stream().
                    collect(Collectors.groupingBy(ProductTaskVO::getPhaseId));

            int parentId = 1;
            for (Map.Entry<String, List<ProductTaskVO>> item : map.entrySet()) {
                ProductTaskVO parentVO = new ProductTaskVO();
                parentVO.setId(parentId);
                parentVO.setParentId(IsConstant.NO);

                List<ProductTaskVO> phaseTaskList = item.getValue();
                //阶段名
                String phaseName = phaseTaskList.get(0).getPhaseName();
                parentVO.setPhaseId(item.getKey());
                parentVO.setPhaseName(phaseName);
                parentId++;

                for (ProductTaskVO vo : phaseTaskList) {
                    String taskId = vo.getTaskId();
                    List<String> preTaskIds = preTaskList.stream().filter(p -> p.getTaskId().equals(taskId)).
                            map(PreTaskEntity::getPreTaskId).collect(Collectors.toList());
                    vo.setPreTaskIdList(preTaskIds);
                    List<String> preTaskNameList = taskList.stream().filter(t -> preTaskIds.contains(t.getTaskId()))
                            .map(ProductTaskVO::getName).collect(Collectors.toList());
                    vo.setPreTaskNames(String.join(",", preTaskNameList));
                    vo.setId(parentId);
                    vo.setParentId(parentVO.getId());
                    List<String> docsNameList = deliveryDocsList.stream().filter(d -> d.getTaskId().equals(taskId))
                            .map(TaskDeliveryDocsEntity::getDocsName).collect(Collectors.toList());
                    vo.setDeliveryDocsNames(String.join(",", docsNameList));
                    parentId++;
                }
                resultList.add(parentVO);
                resultList.addAll(phaseTaskList);

            }
        }

        resultVO.setTaskList(resultList);
        resultVO.setTotalTaskCount(taskList.size());
        return resultVO;
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
    public Boolean fieldSet(List<CustomizeFieldHiddenDTO> dto) {
        if (CollectionUtils.isNotEmpty(dto)) {
            String userId = commonService.getUserInfo().getUid();
            dto.stream().forEach(
                    c -> c.setUserId(userId)
            );
        }
        return sysUserFeign.batchAdd(dto);
    }

    @Override
    public List<CustomizeFieldHiddenDTO> allField() {
        String code = ModuleEnum.PLM_SCHEDULE_TASK.code;
        List<CustomizeFieldEnum> customizeFieldList = CustomizeFieldEnum.getByModuleCode(code);
        List<CustomizeFieldHiddenDTO> resultList = new ArrayList<>(customizeFieldList.size());
        for (CustomizeFieldEnum item : customizeFieldList) {
            CustomizeFieldHiddenDTO dto = new CustomizeFieldHiddenDTO();
            dto.setFieldName(item.getFieldName());
            dto.setFieldTitle(item.getFieldTitle());
            dto.setIsDefault(item.getIsDefault());
            dto.setModuleCode(item.getModuleCode());
            dto.setModuleName(item.getModuleName());
            resultList.add(dto);
        }
        return resultList;
    }


    /**
     * 获取用户隐藏的字段
     *
     * @return
     */
    @Override
    public List<CustomizeFieldHiddenDTO> getUserHiddenField() {
        FindCustomizeFieldDTO dto = new FindCustomizeFieldDTO();
        dto.setUserId(commonService.getUserInfo().getUid());
        dto.setModuleCode(ModuleEnum.PLM_SCHEDULE_TASK.code);
        return sysUserFeign.getByUserId(dto);
    }


    /**
     * 获取到审核的任务
     *
     * @param productId
     * @return
     */
    public List<ScheduleTaskVO> getScheduleTaskList(String productId, String status) {
        return baseMapper.getScheduleTaskList(productId, status);
    }


    /**
     * 保存任务
     *
     * @param
     * @return void
     * @author yl
     * @date 2023-02-08 19:16
     */
    @Override
    public void savePlanTask(String projectPlanId, String productId, List<ProjectTaskEntity> taskList) {
        if (CollectionUtils.isNotEmpty(taskList)) {
            List<ProjectPlanTaskEntity> saveList = new ArrayList<>(taskList.size());
            for (ProjectTaskEntity item : taskList) {
                ProjectPlanTaskEntity entity = new ProjectPlanTaskEntity();
                entity.setChangeEndTime(item.getPlanEndTime());
                entity.setChangeStartTime(item.getPlanStartTime());
                entity.setOriginEndTime(item.getPlanEndTime());
                entity.setOriginStartTime(item.getPlanStartTime());
                entity.setProductId(productId);
                entity.setProjectPlanId(projectPlanId);
                entity.setOriginChargeId(item.getChargeId());
                entity.setChangeChargeId(item.getChargeId());
                saveList.add(entity);
            }

            this.saveBatch(saveList);
        }

    }

}
