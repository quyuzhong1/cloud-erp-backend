package com.erp.server.plm.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.entity.ProjectInfoEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.enums.*;
import com.erp.server.plm.constant.ProductConstant;
import com.erp.server.plm.constant.SourceType;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.mapper.ProjectInfoMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
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


    @Autowired
    private ProjectPhaseService projectPhaseService;

    @Autowired
    private TaskDeliveryService taskDeliveryService;

    @Autowired
    private TaskDocsFinishService finishService;


    @Autowired
    @Lazy
    private NoticeMessageService noticeMessageService;


    @Autowired
    private ProductDetailService productDetailService;


    @Autowired
    private ProjectStatusTimeService projectStatusTimeService;

    @Autowired
    private ProductPlanService productPlanService;

    @Autowired
    private BasicCategoryService basicCategoryService;

    /**
     * 项目概述
     *
     * @param productTaskCountShowDTO
     * @return java.util.List<com.erp.model.plm.dto.PhaseDistributeDTO>
     * @author yl
     * @date 2022-09-19 12:23
     */
    @Override
    public ProjectInfoDTO projectInfo(ProductTaskCountShowDTO productTaskCountShowDTO) {
        ProductInfoEntity entity = productInfoService.getById(productTaskCountShowDTO.getProductId());
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_95010);
        }
        ProjectInfoDTO result = new ProjectInfoDTO();
        List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productTaskCountShowDTO.getProductId());
        //产品名
        result.setProductName(entity.getName());
        //获取产品任务情况
        ProductTaskCountDTO taskCount = projectTaskService.getProductTaskCount(productTaskCountShowDTO, new Date());
        //总任务数
        Integer totalTaskCount = taskCount.getTotalTaskCount();
        //完成任务数
        Integer finishTaskCount = taskCount.getFinishTaskCount();
        //
        Integer postponeTaskCount = taskCount.getPostponeTaskCount();
        Integer unfinishedTaskCount = taskCount.getUnfinishedTaskCount();
        double finishRatio = 0;
        double postponeRatio = 0;
        Double totalCount = Double.valueOf(totalTaskCount);
        if (totalTaskCount != 0) {
            finishRatio = (Double.valueOf(finishTaskCount) / totalCount) * 100;
            finishRatio = Math.round(finishRatio * 100) / 100.0;
            postponeRatio = (Double.valueOf(postponeTaskCount) / totalTaskCount) * 100;
            postponeRatio = Math.round(postponeRatio * 100) / 100.0;
        }
        result.setFinishTaskCount(finishTaskCount);
        result.setUnfinishedTaskCount(unfinishedTaskCount);
        result.setTotalTaskCount(totalTaskCount);
        result.setFinishRatio(finishRatio);
        result.setPostponeTaskCount(postponeTaskCount);
        result.setPostponeRatio(postponeRatio);

        //获取任务阶段分布
        PhaseDistributeDTO taskPhase = phaseDistributeList(productTaskCountShowDTO.getProductId(), taskList);
        result.setPhaseDistribute(taskPhase);
        List<Map<String, Object>> finishTaskTrend = getFinishTaskTrend(30, taskList);
        result.setFinishTaskTrend(finishTaskTrend);
        return result;
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
    public Boolean startProject(StartProjectDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();
        //项目id
        String projectId = dto.getProjectId();
        ProjectInfoEntity project = this.getById(projectId);
        if (Objects.isNull(project)) {
            throw new ServiceException(ApiError.ERROR_95026);
        }
        //检查是否有SKU生成
        List<ProductDetailEntity> skuList = productDetailService.getSkuListByProductId(dto.getProductId());
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95067);
        }

        String chargeId = dto.getChargeId();
        String chargeName = commonService.getNameById(chargeId);
        //负责人id
        project.setChargeId(chargeId);
        project.setChargeName(chargeName);

        //开始时间
        project.setStartTime(LocalDateTimeUtil.of(dto.getStartTime()));
        //结束时间
        project.setEndTime(LocalDateTimeUtil.of(dto.getEndTime()));
        project.setDescribe(dto.getDescribe());
        project.setProjectStatus(ProjectStateEnum.YES_START.getState());
        boolean flag = updateById(project);
        if (flag) {
            String productId = project.getProductId();
            ProductInfoEntity productInfo = productInfoService.getById(productId);
            if (productInfo != null) {
                productInfo.setProjectChargeId(chargeId);
                productInfoService.updateById(productInfo);
            }
            //异步启动消息
            noticeMessageService.startProjectNotice(loginUser.getUserName(), productId);

            if (StringUtils.isNotBlank(chargeId)) {
                projectMembersService.saveByRoleAndMembers(productId, project.getId(), "项目经理", Arrays.asList(chargeId));
            }
            //记录产品状态更新时间
            projectStatusTimeService.saveOrUpdateProjectStatusTime(dto.getProjectId(), dto.getProductId(), ProjectStateEnum.YES_START.getState());
            //更新产品规划的产品状态
            productPlanService.updateProductPlanStatus(productId, ProjectStateEnum.YES_START.getState(), MathUtil.TWO);
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
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        ProductSearchDTO params = dto.getParams();
        IPage pageData = new Page();
        //获取@RequestPermissions的产品id
        List<String> archiveProductIds = archiveService.getArchiveProductIds();
        LoginUser loginUser = commonService.getUserInfo();
        String userId = loginUser.getUid();
        //根据当前登录人id 获取收藏的列表
        List<String> myCollectProductIds = userAddProductService.getMyCollectProductIds(userId);
        //分类id
        String categoryId = params.getCategoryId();
        List<String> categoryIdList = basicCategoryService.getChildrenCategoryIds(categoryId);
        pageData = baseMapper.paging(query, params, archiveProductIds, categoryIdList);
        Integer finish = TaskStateEnum.FINISH.getCode();
        Integer approvalPass = TaskStateEnum.APPROVAL_PASS.getCode();
        List<ProductShowDTO> list = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(list)) {
            //根据产品id 获取到对应的要交付的文档数
            List<CountDTO> productDocs = taskDeliveryService.getTaskDocsCountByProductId();
            //根据产品id 获取到对应完成的文档数
            List<CountDTO> productFinishDocs = finishService.getTaskDocsCountByProductId();
            //   获取到 产品迭代的数量
            List<CountDTO> productRelevance = productInfoService.getProductRelevanceList();

            //获取到所有出产品id
            List<String> productIds = list.stream().map(ProductShowDTO::getProductId).collect(Collectors.toList());
            List<ProjectTaskEntity> taskList = projectTaskService.getByProductIds(productIds);
            for (ProductShowDTO item : list) {
                List<ProjectTaskEntity> productTaskList = taskList.stream().filter(t -> item.getProductId().equals(t.getProductId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(myCollectProductIds) && myCollectProductIds.contains(item.getProductId())) {
                    item.setIfAddProduct(true);
                } else {
                    item.setIfAddProduct(false);
                }

                if (ProductConstant.ITERATION_PRODUCT.equals(item.getType())) {
                    item.setIfIteration(true);
                }
                String progressStatus = item.getProgressStatus();
                item.setProgressStatusName(ProductProgressStatusEnum.getName(progressStatus));
                //总的文档数
                CountDTO totalDocsDTO = productDocs.stream().filter(p -> item.getProductId().equals(p.getFlagId())).findFirst().orElse(null);
                if (totalDocsDTO != null) {
                    item.setTotalDocsCount(totalDocsDTO.getCount());
                } else {
                    item.setTotalDocsCount(0);
                }
                //完成的
                CountDTO finishDocsDTO = productFinishDocs.stream().filter(p -> item.getProductId().equals(p.getFlagId())).findFirst().orElse(null);
                if (finishDocsDTO != null) {
                    item.setFinishDocsCount(finishDocsDTO.getCount());
                } else {
                    item.setFinishDocsCount(0);
                }
                //迭代数
                CountDTO relevanceDTO = productRelevance.stream().filter(p -> item.getProductId().equals(p.getFlagId())).findFirst().orElse(null);
                if (relevanceDTO != null) {
                    item.setIterateCount(relevanceDTO.getCount());
                } else {
                    item.setIterateCount(0);
                }

                String projectChargeId = item.getProjectChargeId();
                if (StringUtils.isNotBlank(projectChargeId)) {
                    item.setProjectChargeId(projectChargeId);
                }
                String productChargeId = item.getProductChargeId();
                if (StringUtils.isNotBlank(productChargeId)) {
                    item.setProductChargeIdList(Arrays.asList(productChargeId.split(",")));
                }

                //这是立项任务
                int approvalTaskCount = productTaskList.stream().filter(t -> TaskConstant.APPROVAL_TASK.equals(t.getProperty())).collect(Collectors.toList()).size();
                //这是立项完成任务
                int approvalFinishTaskCount = productTaskList.stream().filter(t -> TaskConstant.APPROVAL_TASK.equals(t.getProperty()) && (finish.equals(t.getStatus()) || approvalPass.equals(t.getStatus())))
                        .collect(Collectors.toList()).size();
                item.setApprovalFinishTaskCount(approvalFinishTaskCount);
                item.setApprovalTaskCount(approvalTaskCount);

                Integer approvalStatus = item.getApprovalStatus();
                item.setApprovalStatusName(ApprovalStatusEnum.getName(approvalStatus));

                Integer projectStatus = item.getProjectStatus();
                item.setProjectStatusName(ProjectStateEnum.getName(projectStatus));
                //这是项目任务
                int projectTaskCount = productTaskList.stream().filter(t -> TaskConstant.PROJECT_TASK.equals(t.getProperty())).collect(Collectors.toList()).size();
                int projectFinishTaskCount = productTaskList.stream().filter(t -> TaskConstant.PROJECT_TASK.equals(t.getProperty()) && (finish.equals(t.getStatus()) || approvalPass.equals(t.getStatus()))).
                        collect(Collectors.toList()).size();

                item.setProjectTaskCount(projectTaskCount);
                item.setProjectFinishTaskCount(projectFinishTaskCount);
                //总的任务数
                int taskCount = approvalTaskCount + projectTaskCount;
                item.setTaskCount(taskCount);
                double approvalProgress = 0;
                double projectProgress = 0;
                //立项任务完成
                if (approvalTaskCount != 0) {
                    approvalProgress = ((double) approvalFinishTaskCount / approvalTaskCount) * 100;
                }
                //项目任务完成
                if (projectTaskCount != 0) {
                    projectProgress = ((double) projectFinishTaskCount / projectTaskCount) * 100;
                }
                approvalProgress = Math.round(approvalProgress * 100) / 100;
                projectProgress = Math.round(projectProgress * 100) / 100;
                item.setApprovalProgress(approvalProgress);
                item.setProjectProgress(projectProgress);
            }
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<BasicDTO> listProjectInfo(ProductSearchDTO params) {
        //获取@RequestPermissions的产品id
        List<String> archiveProductIds = archiveService.getArchiveProductIds();
        LoginUser loginUser = commonService.getUserInfo();
        String userId = loginUser.getUid();
        List<BasicDTO> list = new ArrayList<>();
        list = baseMapper.listNotPaging(params, archiveProductIds);
        return list;
    }


    /**
     * 根据产品id 集合 获取到 项目信息
     *
     * @param productIdList
     * @return
     */
    @Override
    public List<ProjectInfoEntity> getByProductIdList(List<String> productIdList) {
        if (CollectionUtils.isEmpty(productIdList)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ProjectInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProjectInfoEntity::getProductId, productIdList);
        return this.list(queryWrapper);

    }

    @Override
    public void updateChargeByProductId(String productId, String projectChargeId) {
        String userName = commonService.getNameById(projectChargeId);
        lambdaUpdate().eq(ProjectInfoEntity::getProductId, productId)
                .set(ProjectInfoEntity::getChargeId, projectChargeId)
                .set(ProjectInfoEntity::getChargeName, userName)
                .update();
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
        newAdd.setSourceName("自研项目流程");
        newAdd.setFlagId(IdWorker.getIdStr());
        resultList.add(newAdd);

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
    public void addProject(String productId, String productName, String projectChargeId) {
        int getIfExist = getIfExist(productId);
        if (getIfExist == 0) {
            ProjectInfoEntity project = new ProjectInfoEntity();
            project.setName(productName);
            project.setProductId(productId);
            project.setProjectStatus(ProjectStateEnum.NOT_START.getState());
            if (StringUtils.isNotBlank(projectChargeId)) {
                project.setChargeId(projectChargeId);
                String name = commonService.getNameById(projectChargeId);
                project.setChargeName(name);
            }

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
        LoginUser loginUser = commonService.getUserInfo();
        //检查项目完成情况
        checkProjectFinish(productId);
        //添加归档信息
        Boolean flag = archiveService.saveArchive(productId);
        if (flag) {
            //发送归档项目通知
            noticeMessageService.archiveProjectNotice(loginUser.getUserName(), productId);
        }
        return flag;
    }


    /**
     * 根据产品id 获取项目信息
     *
     * @param productId
     * @return com.erp.model.plm.entity.ProjectInfoEntity
     * @author yl
     * @date 2022-11-29 15:33
     */
    @Override
    public ProjectInfoEntity getByProductId(String productId) {
        LambdaQueryWrapper<ProjectInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectInfoEntity::getProductId, productId);
        queryWrapper.last("LIMIT 1");
        return getOne(queryWrapper);
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
        LocalDateTime dateTime = LocalDateTime.now();
        List<Map<String, Object>> finishTaskTrend = new LinkedList<>();
        String fmt = DateUtil.fmt_day;
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        for (int i = days; i >= 0; i--) {
            Map<String, Object> finishTaskMap = new HashMap<>();
            Date date = LocalDateUtil.localDateTime2Date(dateTime.plusDays(-i));
            long count = taskList.stream().filter(t -> t.getRealityEndTime() != null && DateUtils.isSameDay(date, Date.from(t.getRealityEndTime().atZone(ZoneId.systemDefault()).toInstant()))).count();
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
    public PhaseDistributeDTO phaseDistributeList(String productId, List<ProjectTaskEntity> taskList) {
        PhaseDistributeDTO phaseDistribute = new PhaseDistributeDTO();

        List<String> phaseNameList = projectPhaseService.getPhaseNameName(productId);
        //以阶段名分组
//        Map<String, List<ProjectTaskEntity>> map = taskList.parallelStream().
//                collect(Collectors.groupingBy(ProjectTaskEntity::getPhaseName));
        //状态列表
        List<Map<String, Object>> statusList = new LinkedList<>();
        List<ProductPhaseDistributeDTO> phaseDistributeList = new LinkedList<>();
        for (String phaseName : phaseNameList) {
            ProductPhaseDistributeDTO phaseDistributeDTO = new ProductPhaseDistributeDTO();
            phaseDistributeDTO.setPhaseName(phaseName);
            //分类后的任务
            List<ProjectTaskEntity> groupList = taskList.stream().filter(t -> phaseName.equals(t.getPhaseName())).collect(Collectors.toList());
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
        //待审核
        Integer waitConfirm = TaskStateEnum.WAIT_CONFIRM.getCode();
        //待开始
        Integer notStart = TaskStateEnum.NOT_START.getCode();
        //进行中
        Integer ing = TaskStateEnum.ING.getCode();
        //已完成
        Integer finish = TaskStateEnum.FINISH.getCode();


        //审核中
        Integer approvalIng = TaskStateEnum.APPROVAL_ING.getCode();
        //审核不通过
        Integer noPass = TaskStateEnum.APPROVAL_NO_PASS.getCode();
        //审核通过
        Integer pass = TaskStateEnum.APPROVAL_PASS.getCode();

        int toBeReleasedValue = 0;
        int notStartValue = 0;
        int waitConfirmValue = 0;
        int ingValue = 0;
        int approvalIngValue = 0;
        int approvalNoPassValue = 0;
        int finishValue = 0;
        int finishWaitConfirmValue = 0;
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
            //待审核
            if (waitConfirm.equals(status)) {
                waitConfirmValue++;
            }
            if (ing.equals(status)) {
                ingValue++;
            }
            if (finish.equals(status)) {
                finishValue++;
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


        //待审核
        Map<String, Object> waitConfirmMap = new HashMap<>();
        waitConfirmMap.put("name", ProductInfoStateEnum.WAIT_APPROVAL.getName());
        waitConfirmMap.put("value", waitConfirmValue);
        list.add(waitConfirmMap);

        Map<String, Object> finishWaitConfirmMap = new HashMap<>();
        finishWaitConfirmMap.put("name", ProductInfoStateEnum.WAIT_CONFIRM.getName());
        finishWaitConfirmMap.put("value", finishWaitConfirmValue);
        list.add(finishWaitConfirmMap);

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


        Map<String, Object> approvalPassMap = new HashMap<>();
        approvalPassMap.put("name", ProductInfoStateEnum.APPROVAL_PASS.getName());
        approvalPassMap.put("value", approvalPassValue);
        list.add(approvalPassMap);

        // return list.stream().filter(m -> (Integer) m.get("value") != 0).collect(Collectors.toList());
        return list;
    }


}
