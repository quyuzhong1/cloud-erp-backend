package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.date.DateUtil;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.server.plm.constant.ProductConstant;
import com.erp.server.plm.constant.SourceType;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.enums.ApprovalStatusEnum;
import com.erp.server.plm.enums.ProductInfoStateEnum;
import com.erp.server.plm.enums.ProjectStateEnum;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.mapper.ProjectInfoMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.math3.util.Pair;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

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


    @Autowired
    private ProjectPhaseService projectPhaseService;

    @Autowired
    private TaskDeliveryService taskDeliveryService;

    @Autowired
    private TaskDocsFinishService finishService;

    @Autowired
    private TemplateMembersService templateMembersService;
    @Autowired
    private TemplateTaskService templateTaskService;

    @Autowired
    private TemplateRoleService templateRoleService;

    @Autowired
    private TemplatePhaseService templatePhaseService;

    @Autowired
    private TemplateDeliveryDocsService templateDeliveryDocsService;

    @Autowired
    private TemplateTaskDocsNameService templateTaskDocsNameService;

    @Autowired
    private TemplateRoleRefMembersService templateRoleRefMembersService;

    @Autowired
    private TemplatePreTaskService templatePreTaskService;

    @Autowired
    private TemplateDocsPermissionService templateDocsPermissionService;

    @Autowired
    @Lazy
    private NoticeMessageService noticeMessageService;


    @Autowired
    private ProductDetailService productDetailService;


    @Autowired
    private ProjectTaskRefSkuService projectTaskRefSkuService;

    @Autowired
    private TemplateTaskRefSkuConfigService templateTaskRefSkuConfigService;

    @Autowired
    private TaskRefSkuConfigService taskRefSkuConfigService;

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

            //异步启动消息
            noticeMessageService.startProjectNotice(loginUser.getUserName(), productId);

            //如果是新建 就直接 复制成员
            if (SourceType.NEW.equals(sourceType)) {
                /**
                 * 从复制系统项目任务
                 * 返回已经添加过的sku配置的任务id
                 * 和任务列表
                 */
                Pair<List<String>, List<ProjectTaskEntity>> pair = projectTaskService.copyTaskBySys(productId, projectId);
                List<ProjectTaskEntity> addProjectTaskList = pair.getValue();
                //已经添加的任务id
                List<String> addTaskIdList = addProjectTaskList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
                List<String> alreadyRefSkuConfigTaskIdList = pair.getKey();
                /**
                 * 查找 当没有配置表单的时候 的任务id
                 * 则要自动生成配置表单
                 */
                List<String> noRefSkuConfigTaskIdList=addTaskIdList.stream().filter(a->!alreadyRefSkuConfigTaskIdList.contains(a)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(noRefSkuConfigTaskIdList)) {
                    taskRefSkuConfigService.autoCreateSkuConfig(noRefSkuConfigTaskIdList, TaskConstant.FILL_PRODUCT_INFO, productId);
                }
                //将已保存的任务id 与sku 关联 在一起
                projectTaskRefSkuService.saveBatchTaskRefSku(addTaskIdList, productId, skuList);
                //异步发送通知
                noticeMessageService.newTaskNotice(loginUser.getUserName(), addProjectTaskList, productId);
            }
            //如果是 从项目复制 那么从项目表 里面复制 复制成员
            if (SourceType.PROJECT.equals(sourceType)) {
                projectMembersService.saveMemberByProject(productId, projectId, flagId);
                projectTaskService.copyTaskByProject(productId, projectId, flagId);
            }

            //如果是 从模板复制  那么模板复制数据
            if (SourceType.TEMPLATE.equals(sourceType)) {
                ProjectTemplateEntity template = templateService.getById(flagId);
                if (Objects.isNull(template)) {
                    throw new ServiceException(ApiError.ERROR_95051);
                }
                //复制模板团队成员
                List<CopySourceDTO> copyMembersSourceList = templateMembersService.copyTemplateMembers(template.getId(), productId, projectId);
                //复制模板角色
                List<CopySourceDTO> copyRoleSourceList = templateRoleService.copyTemplateRole(flagId, productId, projectId);
                //复制角色关系表
                templateRoleRefMembersService.copyTemplateRoleRefMembers(flagId, productId, projectId, copyRoleSourceList, copyMembersSourceList);
                //复制 项目任务阶段
                List<CopySourceDTO> phaseSourceList = templatePhaseService.copyTemplatePhase(flagId, productId, projectId);

                //复制任务文档名 可能数据库已有数据
                List<CopySourceDTO> docsNameSourceList = templateTaskDocsNameService.copyTemplateDocsName(flagId, productId, projectId);

                //这个是任务的
                List<CopySourceDTO> taskSourceList = templateTaskService.copyTemplateTask(flagId, productId, projectId, phaseSourceList);
                //这个是复制前置任务关系
                templatePreTaskService.copyTemplatePreTask(flagId, productId, taskSourceList);


                /**
                 *  这个是复制任务与 sku 配置字段关系
                 *  返回已经添加配置关系的 任务id 集合
                 */
                List<String> alreadyRefSkuConfigTaskIdList = templateTaskRefSkuConfigService.copyTemplateTaskSkuConfig(flagId, productId, taskSourceList);
                List<String> addTaskIdList=taskSourceList.stream().map(CopySourceDTO::getNewCreateId).collect(Collectors.toList());
                /**
                 * 查找 当没有配置表单的时候 的任务id
                 * 则要自动生成配置表单
                 */
                List<String> noRefSkuConfigTaskIdList=addTaskIdList.stream().filter(a->!alreadyRefSkuConfigTaskIdList.contains(a)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(noRefSkuConfigTaskIdList)) {
                    taskRefSkuConfigService.autoCreateSkuConfig(noRefSkuConfigTaskIdList, TaskConstant.FILL_PRODUCT_INFO, productId);
                }

                //这个是交付文档
                List<CopySourceDTO> deliveryDocsSourceList = templateDeliveryDocsService.copyTemplateDeliveryDocs(flagId, productId, taskSourceList, docsNameSourceList);
                //这个是文档权限
                templateDocsPermissionService.copyTemplateDeliveryDocs(flagId, productId, taskSourceList, deliveryDocsSourceList);
                //将已保存的任务id 与sku 关联 在一起
                projectTaskRefSkuService.saveBatchTaskRefSku(addTaskIdList, productId, skuList);

            }
            if (CollectionUtils.isNotEmpty(chargeIdList)) {
                projectMembersService.saveByRoleAndMembers(productId,project.getId(),"项目经理",chargeIdList);
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
        dto.getParams().setParam(dto.getParam());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        ProductSearchDTO params = dto.getParams();
        //获取@RequestPermissions的产品id
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
            //查询阶段
            List<ProjectPhaseEntity> phaseList = projectPhaseService.listByProductIds(productIds);

            for (ProductShowDTO item : list) {

                //项目阶段，判断阶段任务是否全部完成
                setProjectPhase(taskList,phaseList,item);

                List<ProjectTaskEntity> productTaskList = taskList.stream().filter(t -> item.getProductId().equals(t.getProductId())).collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(myCollectProductIds) && myCollectProductIds.contains(item.getProductId())) {
                    item.setIfAddProduct(true);
                } else {
                    item.setIfAddProduct(false);
                }

                if (ProductConstant.ITERATION_PRODUCT.equals(item.getType())) {
                    item.setIfIteration(true);
                }

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
                    item.setProjectChargeIdList(Arrays.asList(projectChargeId.split(",")));
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
                    approvalProgress = ((double)approvalFinishTaskCount / approvalTaskCount) * 100;
                }
                //项目任务完成
                if (projectTaskCount != 0) {
                    projectProgress = ((double)projectFinishTaskCount / projectTaskCount) * 100;
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
    public void setProjectPhase(List<ProjectTaskEntity> taskList, List<ProjectPhaseEntity> phaseList,ProductShowDTO item) {
        //项目阶段，判断阶段任务是否全部完成
        if (CollectionUtils.isNotEmpty(taskList)) {
            List<ProjectTaskEntity> projectTaskList = taskList.stream().filter(e -> item.getProductId().equals(e.getProductId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(projectTaskList)) {
                //产品下任务阶段
                List<ProjectPhaseEntity> projectPhaseList = phaseList.stream().filter(e -> item.getProductId().equals(e.getProductId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(projectPhaseList)) {
                    //未开始
                    List<Pair<String, Integer>> unStartList = new ArrayList<>();
                    //进行中
                    List<Pair<String, Integer>> progressList = new ArrayList<>();
                    //已完成
                    List<Pair<String, Integer>> finishList = new ArrayList<>();
                    //进行中和已完成
                    List<Pair<String, Integer>> inFinishList = new ArrayList<>();
                    //结果集
                    List<String> resultList = new ArrayList<>();
                    for (int i = 0; i < projectPhaseList.size(); i++) {
                        ProjectPhaseEntity projectPhaseEntity = projectPhaseList.get(i);
                        List<ProjectTaskEntity> value = projectTaskList.stream().filter(e -> e.getPhaseId().equals(projectPhaseEntity.getId())).collect(Collectors.toList());
                        if (CollectionUtils.isEmpty(value)) {
                            continue;
                        }
                        //判断该阶段任务是否全部未开始
                        long count1 = value.stream().filter(e -> TaskStateEnum.TO_BE_RELEASED.getCode().equals(e.getStatus()) || TaskStateEnum.NOT_START.getCode().equals(e.getStatus())).count();
                        if (count1 == value.size()) {
                            unStartList.add(new Pair<>(projectPhaseEntity.getName(), Integer.valueOf(i)));
                            continue;
                        }
                        //判断该阶段任务是否全部未完成
                        long count2 = value.stream().filter(e -> TaskStateEnum.FINISH.getCode().equals(e.getStatus())
                                || TaskStateEnum.APPROVAL_ING.getCode().equals(e.getStatus())
                                || TaskStateEnum.APPROVAL_PASS.getCode().equals(e.getStatus())
                                || TaskStateEnum.APPROVAL_NO_PASS.getCode().equals(e.getStatus())
                                || TaskStateEnum.CLOSE.getCode().equals(e.getStatus())).count();
                        if (count2 == value.size()) {
                            finishList.add(new Pair<>(projectPhaseEntity.getName(), Integer.valueOf(i)));
                            inFinishList.add(new Pair<>(projectPhaseEntity.getName(), Integer.valueOf(i)));
                            continue;
                        }
                        //阶段下任务为进行中
                        progressList.add(new Pair<>(projectPhaseEntity.getName(), Integer.valueOf(i)));
                        inFinishList.add(new Pair<>(projectPhaseEntity.getName(), Integer.valueOf(i)));
                    }
                    //存在完成或进行中阶段
                    if (CollectionUtils.isNotEmpty(progressList)) {
                        List<String> progress = progressList.stream().map(e -> e.getKey()).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(progress)) {
                            resultList.addAll(progress);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(unStartList)) {
                        if (CollectionUtils.isEmpty(inFinishList)) {
                            //都是未开始，则显示第一个
                            resultList.add(unStartList.get(0).getKey());
                        } else {
                            Pair<String, Integer> pair = inFinishList.stream().max((a, b) -> Integer.compare(a.getValue(), b.getValue())).get();
                            List<String> unStart = unStartList.stream().filter(e -> pair.getValue() > e.getValue()).map(e -> e.getKey()).collect(Collectors.toList());
                            if (CollectionUtils.isNotEmpty(unStart)) {
                                resultList.addAll(unStart);
                            }
                        }
                    }
                    if ( CollectionUtils.isNotEmpty(finishList)){
                        if (CollectionUtils.isEmpty(progressList)  ) {
                            if (CollectionUtils.isNotEmpty(unStartList)) {
                                Pair<String, Integer> pair = finishList.stream().max((a, b) -> Integer.compare(a.getValue(), b.getValue())).get();
                                List<String> unStart = unStartList.stream().filter(e -> pair.getValue() > e.getValue()).map(e -> e.getKey()).collect(Collectors.toList());
                                if (CollectionUtils.isEmpty(unStart)) {
                                    //未开始阶段在已完成阶段后面则显示最后一条
                                    resultList.add(finishList.get(finishList.size() - 1).getKey());
                                }
                            } else {
                                //全部已完成则显示最后一条
                                resultList.add(finishList.get(finishList.size() - 1).getKey());
                            }

                        }
                    }
                    if (CollectionUtils.isNotEmpty(resultList)) {
                        List<ProjectPhaseEntity> phasesList = projectPhaseService.listByPhaseNames(resultList,item.getProductId());
                        if (CollectionUtils.isNotEmpty(phasesList)) {
                            List<String> names = phasesList.stream().map(ProjectPhaseEntity::getName).collect(Collectors.toList());
                            item.setProjectPhase(String.join(",", names));
                        }
                    }
                }

            }
        }
    }

    @Override
    public List<BasicDTO> listProjectInfo(ProductSearchDTO params) {
        //获取@RequestPermissions的产品id
        List<String> archiveProductIds = archiveService.getArchiveProductIds();
        LoginUser loginUser = commonService.getUserInfo();
        String userId = loginUser.getUid();
        //根据当前登录人id 获取收藏的列表
        List<String> myCollectProductIds = userAddProductService.getMyCollectProductIds(userId);

        List<BasicDTO> list = new ArrayList<>();
        //如果是我的收藏
        if (params.getIsMyCollect() != null && params.getIsMyCollect()) {
            if (CollectionUtils.isNotEmpty(myCollectProductIds)) {
                list = baseMapper.listMyCollectNotPaging(params, myCollectProductIds, archiveProductIds);
            }
        } else {
            list = baseMapper.listNotPaging(params, archiveProductIds);
        }
        return list;
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

//        StartItemSourceDTO project = new StartItemSourceDTO();
//        project.setSourceType(SourceType.PROJECT);
//        project.setSourceName("从项目中复制");
//        project.setFlagId(IdWorker.getIdStr());
//        project.setChildrenList(baseMapper.listMap(SourceType.PROJECT));
//        resultList.add(project);

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
            project.setProjectStatus(ProjectStateEnum.NOT_START.getState());
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
