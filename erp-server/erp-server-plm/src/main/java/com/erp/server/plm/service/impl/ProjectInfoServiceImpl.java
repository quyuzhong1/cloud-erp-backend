package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.entity.ProjectInfoEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.constant.ProductConstant;
import com.erp.server.plm.constant.SourceType;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.enums.ProductInfoStateEnum;
import com.erp.server.plm.enums.ProjectStateEnum;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.ProjectInfoMapper;
import com.erp.server.plm.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 产品项目表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
public class ProjectInfoServiceImpl extends ServiceImpl<ProjectInfoMapper, ProjectInfoEntity> implements ProjectInfoService {

    @Autowired
    private ProjectMembersService projectMembersService;

    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    private ProductArchiveService archiveService;

    @Autowired
    private UserAddProductService userAddProductService;

    @Autowired
    private ProjectTemplateService templateService;

    @Autowired
    private CommonService commonService;


    /**
     * 项目概述
     *
     * @param productId
     * @return java.util.List<com.erp.model.plm.dto.PhaseDistributeDTO>
     * @author yl
     * @date 2022-09-19 12:23
     */
    @Override
    public ProjectInfoDTO projectInfo(String productId) {
        ProductInfoEntity entity = productInfoService.getById(productId);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_95010);
        }
        ProjectInfoDTO result = new ProjectInfoDTO();
        List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productId);
        //产品名
        result.setProductName(entity.getName());
        Map<String, Integer> taskMap = getTaskCount(productId, taskList, new Date());
        Integer totalTaskCount = taskMap.get("totalTaskCount");
        Integer finishTaskCount = taskMap.get("finishTaskCount");
        Integer postponeTaskCount = taskMap.get("postponeTaskCount");
        Integer unfinishedTaskCount = taskMap.get("unfinishedTaskCount");
        int finishRatio = 0;
        int postponeRatio = 0;
        if (totalTaskCount != 0) {
            finishRatio = (finishTaskCount / totalTaskCount) * 100;
            postponeRatio = (postponeTaskCount / totalTaskCount) * 100;
        }
        result.setFinishTaskCount(finishTaskCount);
        result.setUnfinishedTaskCount(unfinishedTaskCount);
        result.setTotalTaskCount(totalTaskCount);
        result.setFinishRatio(finishRatio);
        result.setPostponeTaskCount(postponeTaskCount);
        result.setPostponeRatio(postponeRatio);

        //获取任务阶段分布
        PhaseDistributeDTO taskPhase = phaseDistributeList(taskList);
        result.setPhaseDistribute(taskPhase);
        List<Map<String, Object>> finishTaskTrend = getFinishTaskTrend(30, taskList);
        result.setFinishTaskTrend(finishTaskTrend);
        return result;
    }


    //获取到任务的数量
    public Map<String, Integer> getTaskCount(String productId, List<ProjectTaskEntity> taskList, Date date) {
        if (CollectionUtils.isEmpty(taskList) && StringUtils.isNotBlank(productId)) {
            taskList = projectTaskService.getByProductId(productId);
        }
        //完成任务数
        int finishTaskCount = taskList.stream().filter(t -> TaskStateEnum.FINISH.getCode().equals(t.getStatus())).collect(Collectors.toList()).size();
        //未完成任务数
        int unfinishedTaskCount = taskList.stream().filter(t -> !TaskStateEnum.FINISH.getCode().equals(t.getStatus())).collect(Collectors.toList()).size();
        //总任务数
        int totalTaskCount = taskList.size();
        //延期的任务数
        int postponeTaskCount = taskList.stream().filter(t -> date.compareTo(t.getPlanEndTime()) == 1).collect(Collectors.toList()).size();
        Map<String, Integer> map = new HashMap<>(4);
        map.put("finishTaskCount", finishTaskCount);
        map.put("unfinishedTaskCount", unfinishedTaskCount);
        map.put("totalTaskCount", totalTaskCount);
        map.put("postponeTaskCount", postponeTaskCount);
        return map;

    }

    /**
     * 启动项目
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-20 16:05
     */
    @Override
    @Transactional
    public Boolean startProject(StartProjectDTO dto) {
        //项目id
        String projectId = dto.getProjectId();
        ProjectInfoEntity project = this.getById(projectId);
        if (Objects.isNull(project)) {
            throw new ServiceException(ApiError.ERROR_95026);
        }
        List<String> chargeIdList = dto.getChargeIdList();
        String chargeName = commonService.getNameByIds(chargeIdList);
        //负责人id
        project.setChargeId(StringUtils.join(chargeIdList, ","));
        project.setChargeName(chargeName);
        //来源
        project.setSourceType(dto.getSourceType());
        //开始时间
        project.setStartTime(dto.getStartTime());
        //结束时间
        project.setEndTime(dto.getEndTime());
        project.setDescribe(dto.getDescribe());
        project.setProjectStatus(ProjectStateEnum.YES_START.getState());
        boolean flag = updateById(project);
        Integer sourceType = dto.getSourceType();
        if (flag) {
            String productId = project.getProductId();
            String flagId = dto.getFlagId();
            //如果是新建 就直接 复制成员
            if (SourceType.NEW.equals(sourceType)) {
                projectMembersService.add(productId, projectId, dto.getMembers());
                //从复制系统项目任务
                projectTaskService.copyTaskBySys(productId, projectId);
            }
            //如果是 从项目复制 那么从项目表 里面复制 复制成员
            if (SourceType.PROJECT.equals(sourceType)) {
                projectMembersService.saveMemberByProject(productId, projectId, flagId);
                projectTaskService.copyTaskByProject(productId, projectId, flagId);
            }

            //如果是 从模板复制 那么从项目表 里面复制 复制成员
            if (SourceType.PROJECT.equals(sourceType)) {
                projectMembersService.saveMemberByTemplate(productId, projectId, flagId);
                projectTaskService.copyTaskByTemplate(productId, projectId, flagId);
            }


        }

        return flag;
    }


    /**
     * 修改项目负责人 望里面添加
     *
     * @param projectId
     * @param useName
     * @param userId
     * @return void
     * @author yl
     * @date 2022-09-26 18:20
     */
    @Override
    public void updateCharge(String projectId, String useName, String userId, Boolean isUpdate) {
        ProjectInfoEntity entity = this.getById(projectId);
        if (!Objects.isNull(entity)) {
            //当是修改的时候直接覆盖
            if (isUpdate) {
                entity.setChargeName(useName);
                entity.setChargeId(userId);
            } else {
                String chargeId = entity.getChargeId();
                if (StringUtils.isNotBlank(chargeId)) {
                    chargeId = chargeId + "," + userId;
                }
                String chargeName = entity.getChargeName();
                if (StringUtils.isNotBlank(chargeName)) {
                    chargeName = chargeName + "," + useName;
                }
                entity.setChargeName(chargeName);
                entity.setChargeId(chargeId);
            }
            this.updateById(entity);
        }
    }


    /**
     * 项目分页
     *
     * @param dto
     * @return com.erp.common.vo.PagingVO
     * @author yl
     * @date 2022-09-28 10:06
     */

    @Override
    public PagingVO<List<ProductShowDTO>> paging(PagingDTO<ProductSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        ProductSearchDTO params = dto.getParams();
        List<String> archiveProductIds = archiveService.getArchiveProductIds();
        LoginUser loginUser = commonService.getUserInfo();
        String userId = loginUser.getUid();
        //根据当前登录人id 获取收藏的列表
        List<String> myCollectProductIds = userAddProductService.getMyCollectProductIds(userId);

        IPage pageData = new Page();
        //如果是我的收藏
        if (params.getIsMyCollect() != null && params.getIsMyCollect()) {
            if (CollectionUtils.isNotEmpty(myCollectProductIds)) {
                pageData = baseMapper.myCollectPaging(query, params, myCollectProductIds, archiveProductIds);
            }
        } else {
            pageData = baseMapper.paging(query, params, archiveProductIds);
        }
        List<ProductShowDTO> list = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(list)) {
            //获取到所有出产品id
            List<String> productIds = list.stream().map(ProductShowDTO::getProductId).collect(Collectors.toList());
            List<ProjectTaskEntity> taskList = projectTaskService.getByProductIds(productIds);
            for (ProductShowDTO item : list) {

                if (CollectionUtils.isNotEmpty(myCollectProductIds) && myCollectProductIds.contains(item.getProductId())) {
                    item.setIfAddProduct(true);
                } else {
                    item.setIfAddProduct(false);
                }

                if (ProductConstant.ITERATION_PRODUCT.equals(item.getType())) {
                    item.setIfIteration(true);
                }

                //这是立项任务
                int approvalTaskCount = taskList.stream().filter(t -> TaskConstant.APPROVAL_TASK.equals(t.getProperty())).collect(Collectors.toList()).size();
                //这是立项完成任务
                int approvalFinishTaskCount = taskList.stream().filter(t -> TaskConstant.APPROVAL_TASK.equals(t.getProperty()) && TaskStateEnum.FINISH.getCode().equals(t.getStatus()))
                        .collect(Collectors.toList()).size();
                item.setApprovalFinishTaskCount(approvalFinishTaskCount);
                item.setApprovalTaskCount(approvalTaskCount);

                //这是项目任务
                int projectTaskCount = taskList.stream().filter(t -> TaskConstant.PROJECT_TASK.equals(t.getProperty())).collect(Collectors.toList()).size();
                int projectFinishTaskCount = taskList.stream().filter(t -> TaskConstant.PROJECT_TASK.equals(t.getProperty()) && TaskStateEnum.FINISH.getCode().equals(t.getStatus())).
                        collect(Collectors.toList()).size();

                item.setProjectTaskCount(projectTaskCount);
                item.setProjectFinishTaskCount(projectFinishTaskCount);
                //总的任务数
                int taskCount = approvalTaskCount + projectTaskCount;
                item.setTaskCount(taskCount);
                int approvalProgress = 0;
                int projectProgress = 0;
                //立项任务完成
                if (approvalTaskCount != 0) {
                    approvalProgress = (approvalFinishTaskCount / approvalTaskCount) * 100;
                }
                //项目任务完成
                if (projectTaskCount != 0) {
                    projectProgress = (projectFinishTaskCount / projectTaskCount) * 100;
                }
                item.setApprovalProgress(approvalProgress);
                item.setProjectProgress(projectProgress);
            }
        }
        return new PagingVO(pageData);
    }

    /**
     * 检查项目是否完成
     *
     * @param productId
     * @return void
     * @author yl
     * @date 2022-10-09 14:54
     */
    @Override
    public void checkProjectFinish(String productId) {
        LambdaQueryWrapper<ProjectInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectInfoEntity::getProductId, productId);
        queryWrapper.eq(ProjectInfoEntity::getProjectStatus, ProjectStateEnum.FINISH.getState());
        ProjectInfoEntity entity = baseMapper.selectOne(queryWrapper);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_95019);
        }
    }


    /**
     * 获取启动项目的来源 树形结构
     *
     * @param
     * @return java.util.List<com.erp.model.plm.dto.StartItemSourceDTO>
     * @author yl
     * @date 2022-10-11 18:56
     */
    @Override
    public List<StartItemSourceDTO> getStartItemSourceList() {
        List<StartItemSourceDTO> resultList = new ArrayList<>();
        //这个是新建
        StartItemSourceDTO newAdd = new StartItemSourceDTO();
        newAdd.setSourceType(SourceType.NEW);
        newAdd.setSourceName("自定义新建");
        newAdd.setSourceType(100);
        newAdd.setFlagId(IdWorker.getIdStr());
        resultList.add(newAdd);

        StartItemSourceDTO project = new StartItemSourceDTO();
        project.setSourceType(SourceType.PROJECT);
        project.setSourceName("从项目中复制");
        project.setFlagId(IdWorker.getIdStr());
        project.setChildrenList(baseMapper.listMap(SourceType.PROJECT));
        resultList.add(project);

        StartItemSourceDTO template = new StartItemSourceDTO();
        template.setSourceType(SourceType.TEMPLATE);
        template.setSourceName("从模板中复制");
        template.setFlagId(IdWorker.getIdStr());
        template.setChildrenList(templateService.startItemSource(SourceType.TEMPLATE));
        resultList.add(template);
        return resultList;
    }

    /**
     * 产品列表 修改状态为已立项 添加产品信息
     *
     * @param productId
     * @return void
     * @author yl
     * @date 2022-10-12 10:49
     */
    @Override
    public void addProject(String productId, String productName) {
        int getIfExist = getIfExist(productId);
        if (getIfExist == 0) {
            ProjectInfoEntity project = new ProjectInfoEntity();
            project.setName(productName);
            project.setProductId(productId);
            this.save(project);
        }


    }

    /**
     * 根据产品id 删除项目
     *
     * @param productId
     * @return void
     * @author yl
     * @date 2022-10-12 10:59
     */

    @Override
    public void removeByProductId(String productId) {
        LambdaQueryWrapper<ProjectInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectInfoEntity::getProductId, productId);
        this.remove(queryWrapper);

    }


    /**
     * 归档
     *
     * @param productId
     * @return
     */
    @Override
    public boolean archive(String productId) {
        //检查项目完成情况
        checkProjectFinish(productId);
        //添加归档信息
        Boolean flag = archiveService.saveArchive(productId);
        return flag;
    }


    /**
     * 根据产品id 查询是否存在
     *
     * @param productId
     * @return
     */
    public int getIfExist(String productId) {
        LambdaQueryWrapper<ProjectInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectInfoEntity::getProductId, productId);
        return this.count(queryWrapper);

    }


    /**
     * 获取近三十天 数据
     *
     * @param days
     * @param taskList
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @author yl
     * @date 2022-09-20 11:45
     */
    private List<Map<String, Object>> getFinishTaskTrend(int days, List<ProjectTaskEntity> taskList) {
        DateTime dateTime = new DateTime(new Date());
        List<Map<String, Object>> finishTaskTrend = new LinkedList<>();
        String fmt = DateUtil.fmt_day;
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        for (int i = days; i >= 0; i--) {
            Map<String, Object> finishTaskMap = new HashMap<>();
            Date date = dateTime.plusDays(-i).toDate();
            long count = taskList.stream().filter(t -> t.getRealityEndTime() != null && DateUtils.isSameDay(date, t.getRealityEndTime())).count();
            finishTaskMap.put("date", sdf.format(date.getTime()));
            finishTaskMap.put("quantity", count);
            finishTaskTrend.add(finishTaskMap);
        }

        return finishTaskTrend;
    }


    /**
     * 获取任务阶段
     *
     * @param
     * @return java.util.List<com.erp.model.plm.dto.PhaseDistributeDTO>
     * @author yl
     * @date 2022-09-19 15:20
     */
    public PhaseDistributeDTO phaseDistributeList(List<ProjectTaskEntity> taskList) {
        PhaseDistributeDTO phaseDistribute = new PhaseDistributeDTO();
        //以阶段名分组
        Map<String, List<ProjectTaskEntity>> map = taskList.parallelStream().
                collect(Collectors.groupingBy(ProjectTaskEntity::getPhaseName));
        //状态列表
        List<Map<String, Object>> statusList = new LinkedList<>();
        //阶段的 集合  以阶段名作为key 以对应结果为值
        List<ProductPhaseDistributeDTO> phaseDistributeList = new LinkedList<>();
        for (Map.Entry<String, List<ProjectTaskEntity>> item : map.entrySet()) {
            ProductPhaseDistributeDTO phaseDistributeDTO = new ProductPhaseDistributeDTO();
            //阶段名
            String phaseName = item.getKey();
            phaseDistributeDTO.setPhaseName(phaseName);
            //分类后的任务
            List<ProjectTaskEntity> groupList = item.getValue();
            List<Map<String, Object>> phaseStateList = getPhaseStateList(groupList);
            phaseDistributeDTO.setPhaseDataList(phaseStateList);
            phaseDistributeList.add(phaseDistributeDTO);
        }
        //获取枚举的所有值
        for (ProductInfoStateEnum e : ProductInfoStateEnum.values()) {
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("name", e.getName());
            statusMap.put("colourState", e.getColourState());
            statusList.add(statusMap);
        }
        phaseDistribute.setPhaseList(phaseDistributeList);
        phaseDistribute.setStatusList(statusList);


        return phaseDistribute;
    }


    /**
     * 根据分组的后的 任务 找出对应的任务状态
     *
     * @param groupList
     * @return
     * @author yl
     * @date 2022-09-20 9:16
     */
    private List<Map<String, Object>> getPhaseStateList(List<ProjectTaskEntity> groupList) {
        List<Map<String, Object>> list = new LinkedList<>();
        //待发布
        Integer toBeReleased = TaskStateEnum.TO_BE_RELEASED.getCode();
        //未启动
        Integer notStart = TaskStateEnum.NOT_START.getCode();
        //进行中
        Integer ing = TaskStateEnum.ING.getCode();
        //已完成
        Integer finish = TaskStateEnum.FINISH.getCode();

        //完成待确认
        Integer waitConfirm = TaskStateEnum.FINISH_WAIT_CONFIRM.getCode();
        //审核中
        Integer approvalIng = TaskStateEnum.APPROVAL_PASS.getCode();
        //审核不通过
        Integer noPass = TaskStateEnum.APPROVAL_NO_PASS.getCode();
        //审核通过
        Integer pass = TaskStateEnum.APPROVAL_PASS.getCode();

        int toBeReleasedValue = 0;
        int notStartValue = 0;
        int ingValue = 0;
        int approvalIngValue = 0;
        int approvalNoPassValue = 0;
        int finishValue = 0;
        int waitConfirmValue = 0;
        int approvalPassValue = 0;
        //这是 审核任务的
        for (ProjectTaskEntity item : groupList) {
            Integer status = item.getStatus();
            //待发布
            if (toBeReleased.equals(status)) {
                toBeReleasedValue++;
            }
            //未开始
            if (notStart.equals(status)) {
                notStartValue++;
            }
            if (ing.equals(status)) {
                ingValue++;
            }
            if (finish.equals(status)) {
                finishValue++;
            }
            if (waitConfirm.equals(status)) {
                waitConfirmValue++;
            }
            if (noPass.equals(status)) {
                approvalNoPassValue++;
            }
            if (approvalIng.equals(status)) {
                approvalIngValue++;
            }
            if (pass.equals(status)) {
                approvalPassValue++;
            }

        }

        Map<String, Object> WaitReleasedMap = new HashMap<>();
        WaitReleasedMap.put("name", ProductInfoStateEnum.TO_BE_RELEASED.getName());
        WaitReleasedMap.put("value", toBeReleasedValue);
        list.add(WaitReleasedMap);

        Map<String, Object> notStartMap = new HashMap<>();
        notStartMap.put("name", ProductInfoStateEnum.NOT_START.getName());
        notStartMap.put("value", notStartValue);
        list.add(notStartMap);

        Map<String, Object> ingMap = new HashMap<>();
        ingMap.put("name", ProductInfoStateEnum.ING.getName());
        ingMap.put("value", ingValue);
        list.add(ingMap);

        Map<String, Object> approvalIngMap = new HashMap<>();
        approvalIngMap.put("name", ProductInfoStateEnum.APPROVAL_ING.getName());
        approvalIngMap.put("value", approvalIngValue);
        list.add(approvalIngMap);

        Map<String, Object> approvalNoPassMap = new HashMap<>();
        approvalNoPassMap.put("name", ProductInfoStateEnum.APPROVAL_NO_PASS.getName());
        approvalNoPassMap.put("value", approvalNoPassValue);
        list.add(approvalNoPassMap);

        Map<String, Object> finishMap = new HashMap<>();
        finishMap.put("name", ProductInfoStateEnum.FINISH.getName());
        finishMap.put("value", finishValue);
        list.add(finishMap);

        Map<String, Object> waitConfirmMap = new HashMap<>();
        waitConfirmMap.put("name", ProductInfoStateEnum.WAIT_CONFIRM.getName());
        waitConfirmMap.put("value", waitConfirmValue);
        list.add(waitConfirmMap);

        Map<String, Object> approvalPassMap = new HashMap<>();
        approvalPassMap.put("name", ProductInfoStateEnum.APPROVAL_PASS.getName());
        approvalPassMap.put("value", approvalPassValue);
        list.add(approvalPassMap);

        return list.stream().filter(m -> (Integer) m.get("value") != 0).collect(Collectors.toList());
    }
}
