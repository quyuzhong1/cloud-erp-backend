package com.erp.server.plm.listener;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.FindUserDTO;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.DocsDTO;
import com.erp.model.plm.dto.DocsNameDTO;
import com.erp.model.plm.dto.ProjectTaskDTO;
import com.erp.model.plm.dto.TaskChargeDistributionDTO;
import com.erp.model.plm.dto.excel.ProjectTaskExcelDTO;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.entity.ProjectPhaseEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.TaskChargeDistributionEntity;
import com.erp.model.plm.enums.ChargeSuperiorEnum;
import com.erp.model.plm.enums.DistributionTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.service.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectTaskExcelListener extends AnalysisEventListener<ProjectTaskExcelDTO> {
    private Integer importType;
    private String productId;
    private ProjectTaskService projectTaskService;

    private ProductInfoService productInfoService;

    private SysUserFeign sysUserFeign;

    private ProjectPhaseService projectPhaseService;

    private TaskDocsNameService taskDocsNameService;

    private TaskChargeDistributionService taskChargeDistributionService;

    private List<ProjectTaskExcelDTO> list;

    private List<ProjectTaskExcelDTO> dataList = new ArrayList<>();

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");

    public ProjectTaskExcelListener(Integer importType, String productId, ProjectTaskService projectTaskService, ProductInfoService productInfoService, SysUserFeign sysUserFeign,
                                    ProjectPhaseService projectPhaseService, TaskDocsNameService taskDocsNameService, TaskChargeDistributionService taskChargeDistributionService) {
        this.importType = importType;
        this.productId = productId;
        this.projectTaskService = projectTaskService;
        this.productInfoService = productInfoService;
        this.sysUserFeign = sysUserFeign;
        this.projectPhaseService = projectPhaseService;
        this.taskDocsNameService = taskDocsNameService;
        this.taskChargeDistributionService = taskChargeDistributionService;
        this.list = new ArrayList<>();
    }

    @Override
    public void invoke(ProjectTaskExcelDTO projectTaskExcelDTO, AnalysisContext analysisContext) {

        List<String> errorMsgList = new ArrayList<>();
        ProjectTaskDTO projectTaskDTO = new ProjectTaskDTO();

        //添加数据用于判断是否为空
        dataList.add(projectTaskExcelDTO);

        //注解基础校验
        List<String> msgList = FieldValidUtil.fieldValid(projectTaskExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
            projectTaskExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            list.add(projectTaskExcelDTO);
            return;
        }

        ProductInfoEntity productInfoEntity = productInfoService.getById(productId);

        if (!productInfoEntity.getName().equals(projectTaskExcelDTO.getProductName())) {
            errorMsgList.add("[所属产品]名称不正确，请输入导入界面的产品名称");
            projectTaskExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            list.add(projectTaskExcelDTO);
            return;
        }

        if (StringUtils.isNotBlank(projectTaskExcelDTO.getType())) {
            if (!projectTaskExcelDTO.getType().equals("一般任务") && !projectTaskExcelDTO.getType().equals("评审任务")) {
                errorMsgList.add("[任务类型]请输入'一般任务'或'评审任务'");
            }

            if (projectTaskExcelDTO.getType().equals("一般任务")){
                projectTaskDTO.setType(0);
            } else {
                projectTaskDTO.setType(1);
            }
        }
        List<TaskChargeDistributionDTO> TaskChargeDistributionlist = new ArrayList<>();

        if (ObjectUtil.isNotEmpty(productInfoEntity)) {


            ProjectTaskEntity projectTaskEntity = projectTaskService.getTaskByName(productInfoEntity.getId(), projectTaskExcelDTO.getName());

            //查询模板任务下审核人
            List<TaskChargeDistributionEntity> taskChargeDistributionList = taskChargeDistributionService.listBySourceAndTaskId(MathUtil.THREE, projectTaskEntity.getId());
            if (taskChargeDistributionList != null && taskChargeDistributionList.size() > 0) {
                TaskChargeDistributionlist = BeanMapperUtils.copyList(TaskChargeDistributionDTO.class, taskChargeDistributionList);
                TaskChargeDistributionlist.forEach(obj -> {
                    if (org.apache.commons.lang3.StringUtils.isBlank(obj.getCharges())) {
                        return;
                    }
                    List<String> collect = Arrays.stream(obj.getCharges().split(",")).collect(Collectors.toList());
                    //回显名称
                    if (DistributionTypeEnum.DISTRIBUTION_USER.getCode().equals(obj.getDistributionType())) {
                        //用户分配查询名称
                        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(collect);
                        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(userList)) {
                            List<String> usrNameList = userList.stream().map(FindUserDTO::getUserName).collect(Collectors.toList());
                            obj.setChargeNames(String.join(",", usrNameList));
                        }
                        obj.setChargeList(collect);
                    }
                    if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(obj.getDistributionType())) {
                        if (org.apache.commons.lang3.StringUtils.isBlank(obj.getChargeIds())) {
                            //角色分配直接取名称
                            obj.setChargeNames(obj.getCharges());
                            obj.setChargeList(collect);
                        } else {
                            List<String> collect1 = Arrays.stream(obj.getChargeIds().split(",")).collect(Collectors.toList());
                            //用户分配查询名称
                            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(collect1);
                            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(userList)) {
                                List<String> usrNameList = userList.stream().map(FindUserDTO::getUserName).collect(Collectors.toList());
                                obj.setChargeNames(String.join(",", usrNameList));
                            }
                            obj.setChargeList(collect1);
                        }
                    }
                    if (DistributionTypeEnum.DISTRIBUTION_SUPERIOR.getCode().equals(obj.getDistributionType())) {
                        if (org.apache.commons.lang3.StringUtils.isBlank(obj.getChargeIds())) {
                            //上级分配取枚举
                            List<String> superiors = collect.stream().map(e -> ChargeSuperiorEnum.getDesc(e)).collect(Collectors.toList());
                            obj.setChargeNames(String.join(",", superiors));
                            obj.setChargeList(superiors);
                        } else {
                            List<String> collect1 = Arrays.stream(obj.getChargeIds().split(",")).collect(Collectors.toList());
                            //用户分配查询名称
                            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(collect1);
                            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(userList)) {
                                List<String> usrNameList = userList.stream().map(FindUserDTO::getUserName).collect(Collectors.toList());
                                obj.setChargeNames(String.join(",", usrNameList));
                            }
                            obj.setChargeList(collect1);
                        }
                    }
                });
            }


            // 判断是修改还是新增 1：新增 2：修改
            /**
             * 任务状态 0:待发布 1:待开始
             * 2:待审核  3:进行中 4 已完成, 5 已关闭   6.完成待审核 7.审核中  8 审核通过  9 审核不通过 ,10 部分完成
             */
            if (importType == 2) {
                if (ObjectUtil.isEmpty(projectTaskEntity)) {
                    errorMsgList.add("[任务名称]在系统中不存在，请确认产品名称存在");
                } else { //可编辑：待审核  审核通过  已完成
                    if (projectTaskEntity.getStatus() == 0 || projectTaskEntity.getStatus() == 1 || projectTaskEntity.getStatus() == 3 ) {
                        projectTaskDTO.setId(projectTaskEntity.getId());
                        projectTaskDTO.setType(projectTaskEntity.getType());
                    } else {
                        errorMsgList.add("只有[任务状态]为待发布或待开始，进行中的任务可修改");
                    }
                }
            } else {
                if (ObjectUtil.isNotEmpty(projectTaskEntity)) {
                    errorMsgList.add("[任务名称]在系统中已存在，不可重复");
                }
            }
        }
        List<String> chargeNameList = new ArrayList<>();
        String chargeName = projectTaskExcelDTO.getChargeName();
        String[] chargeNames = chargeName.split(",");
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        for (String name : chargeNames) {
            FindUserDTO findUserDTO = userList.stream().filter(u -> name.equals(u.getUserName())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(findUserDTO)) {
                errorMsgList.add("[任务负责人]在系统中未找到，多个负责人请用英文逗号','隔开");
            } else {
                chargeNameList.add(findUserDTO.getUserId());
            }
        }

        ProjectPhaseEntity projectPhaseEntity = projectPhaseService.getProductPhaseByName(productInfoEntity.getId(), projectTaskExcelDTO.getPhaseName());
        if (ObjectUtil.isEmpty(projectPhaseEntity)) {
            errorMsgList.add("[阶段名称]在这个[所属产品]下不存在");
        }

        String preTask = projectTaskExcelDTO.getPreTask();
        List<String> preTaskList = new ArrayList<>();
        if (StringUtils.isNotBlank(preTask)) {
            List<ProjectTaskEntity> projectTaskEntities = projectTaskService.listByProductId(productInfoEntity.getId());
            String[] split = preTask.split(",");
            for (String task : split) {
                ProjectTaskEntity projectTaskEntity1 = projectTaskEntities.stream().filter(t -> t.getName().equals(task)).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(projectTaskEntity1)) {
                    errorMsgList.add("[前置任务]在这个[所属产品]下不存在，多个前置任务请用英文逗号','隔开");
                } else {
                    preTaskList.add(projectTaskEntity1.getId());
                }
            }
        }

        String priority = projectTaskExcelDTO.getPriority();
        if (StringUtils.isNotBlank(priority)) {
            if (!priority.equals("高") && !priority.equals("中") && !priority.equals("低")) {
                errorMsgList.add("[任务优先级]请输入'高'或'中''低'");
            }
        }

        String isMilepost = projectTaskExcelDTO.getIsMilepost();
        if (StringUtils.isNotBlank(isMilepost)) {
            if (!isMilepost.equals("是") && !isMilepost.equals("否")) {
                errorMsgList.add("[设置里程碑]请输入'是'或'否'");
            }
        }

        if (projectTaskExcelDTO.getPlanStartTime() != null) {
            if (projectTaskExcelDTO.getPlanEndTime() == null) {
                errorMsgList.add("[计划开始时间]存在的同时[计划结束时间]不能为空，");
            }
        }

        if (projectTaskExcelDTO.getPlanEndTime() != null) {
            if (projectTaskExcelDTO.getPlanStartTime() == null) {
                errorMsgList.add("[计划结束时间]存在的同时[计划开始时间]不能为空，");
            }
        }

        //目标交付文档
        String docsName = projectTaskExcelDTO.getDocsName();
        List<DocsDTO> docsNameList = new ArrayList<>();
        if (StringUtils.isNotBlank(docsName)) {
            List<DocsDTO> docsList = taskDocsNameService.getDocsNameList(productInfoEntity.getId());
            String[] split = docsName.split(",");
            for (String docs : split) {
                DocsDTO docsDTO = docsList.stream().filter(t -> t.getName().equals(docs)).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(docsDTO)) {
                    DocsNameDTO docsNameDTO = new DocsNameDTO();
                    docsNameDTO.setName(docs);
                    docsNameDTO.setProductId(productInfoEntity.getId());
                    String id = taskDocsNameService.saveDocs(docsNameDTO);
                    DocsDTO dto = new DocsDTO();
                    dto.setId(id);
                    dto.setName(docs);
                    dto.setState(false);
                    docsNameList.add(dto);
                } else {
                    docsNameList.add(docsDTO);
                }
            }
        }


        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            projectTaskExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            list.add(projectTaskExcelDTO);
            return;
        }


        projectTaskDTO.setProjectId(productInfoEntity.getId());
        projectTaskDTO.setProductId(productInfoEntity.getId());
        projectTaskDTO.setName(projectTaskExcelDTO.getName());

        projectTaskDTO.setChargeIds(chargeNameList);
        projectTaskDTO.setPreTaskIdList(preTaskList);
        if (projectTaskExcelDTO.getPlanStartTime() != null) {
            projectTaskDTO.setPlanStartTime(LocalDate.parse(projectTaskExcelDTO.getPlanStartTime(), dateTimeFormatter));
        }
        if (projectTaskExcelDTO.getPlanEndTime() != null) {
            projectTaskDTO.setPlanEndTime(LocalDate.parse(projectTaskExcelDTO.getPlanEndTime(), dateTimeFormatter));
        }
        if (StringUtils.isNotBlank(projectTaskExcelDTO.getPriority())) {
            if (projectTaskExcelDTO.getPriority().equals("高")) {
                projectTaskDTO.setPriority(3);
            } else if (projectTaskExcelDTO.getPriority().equals("中")) {
                projectTaskDTO.setPriority(2);
            } else {
                projectTaskDTO.setPriority(1);
            }
        }
        projectTaskDTO.setPhaseId(projectPhaseEntity.getId());
        projectTaskDTO.setPhaseName(projectPhaseEntity.getName());
        projectTaskDTO.setDescription(projectTaskExcelDTO.getDescription());
        projectTaskDTO.setProcessId("");
        projectTaskDTO.setApprovalList(TaskChargeDistributionlist);
        projectTaskDTO.setDeliveryDocsList(docsNameList);
        if (StringUtils.isNotBlank(isMilepost)) {
            if (isMilepost.equals("是")) {
                projectTaskDTO.setIsMilepost(1);
            } else {
                projectTaskDTO.setIsMilepost(0);
            }
        }

        projectTaskDTO.setRefSkuIdList(new ArrayList<>());
        projectTaskDTO.setRelatedSkuType("3");
        projectTaskDTO.setWorkPeriod(projectTaskExcelDTO.getWorkPeriod());
        if (importType == 2) {
            projectTaskService.updateTask(projectTaskDTO);
        } else {
            projectTaskService.save(projectTaskDTO);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<ProjectTaskExcelDTO> getDateList(){
        return list;
    }

    public List<ProjectTaskExcelDTO> getExcelDateList(){
        return dataList;
    }
}
