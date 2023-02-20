package com.erp.server.plm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.business.dto.FindUserDTO;
import com.erp.common.business.enums.SkuApproveConfigureEnum;
import com.erp.common.business.enums.WorkflowBusinessEnum;
import com.common.core.utils.BeanMapper;
import com.erp.common.business.dto.base.PagingDTO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.workflow.dto.ProcessPassDTO;
import com.erp.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductChangeEntity;
import com.erp.model.plm.vo.BomVO;
import com.erp.model.plm.vo.ProductChangePagingVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.workflow.dto.*;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.model.workflow.vo.MyToDoTaskVO;
import com.erp.model.workflow.vo.ProcessCurrentAuditorVO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.BomConstant;
import com.erp.server.plm.constant.SearchType;
import com.erp.server.plm.controller.AuditParamDTO;
import com.erp.model.plm.enums.ProductChangeStateEnum;
import com.erp.server.plm.mapper.ProductChangeMapper;
import com.erp.server.plm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 变更信息表(ProductChange)表服务实现类
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
@Service
@Slf4j
public class ProductChangeServiceImpl extends ServiceImpl<ProductChangeMapper, ProductChangeEntity> implements ProductChangeService {


    @Resource
    private ProductChangeDetailsService changeDetailsService;

    @Resource
    private BomInfoService bomInfoService;


    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private CommonService commonService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private BomSkuService bomSkuService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private ProductChangeDetailsService productChangeDetailsService;

    @Autowired
    private SysLogService sysLogService;


    //变更财务人员审核
    @Value("${changeFinancialAudit}")
    private String financial;

    /**
     * 添加变更
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-14 15:02
     */
    @Override
    @Transactional
    public Boolean add(AddChangeDTO dto) {
        ProductChangeEntity change = new ProductChangeEntity();
        String type = dto.getType();
        String changeBom = BomConstant.CHANGE_BOM;
        Boolean isBom = changeBom.equals(type);
        String sourceId = dto.getSourceId();
        if (isBom) {
            //检查能否变更 只有归档才可以
            bomInfoService.checkIfChange(sourceId);
        }
        BeanMapper.copy(dto, change);
        String id = IdWorker.getIdStr();
        change.setId(id);
        //如果是bom 检查审核人为空不
        if (isBom) {
            checkBomChangeAuditor(sourceId);
        } else {
            checkSkuChangeAuditor(sourceId);
        }

        Boolean saveResult = this.save(change);
        if (saveResult) {
            changeDetailsService.saveChangeDetails(id, dto.getDetailsJson());
        }

        //启动一个流程
        startChangeProcess(change);
        return saveResult;
    }


    /**
     * 检查bom 变更审核人是否为空
     *
     * @param
     * @return void
     * @author yl
     * @date 2023-02-01 18:45
     */
    public void checkBomChangeAuditor(String sourceId) {

        List<BomSkuDTO> skuList = bomSkuService.getByBomId(sourceId);
        //skuId
        List<String> skuIdList = skuList.stream().map(BomSkuDTO::getSkuId).collect(Collectors.toList());
        //产品经理
        List<String> productManagerList = productDetailService.getManagerBySkuIds(skuIdList);
        if (CollectionUtils.isEmpty(productManagerList)) {
            throw new ServiceException(ApiError.ERROR_9030);
        }
        //产品经理上级
        List<String> productManagerSupervisorList = productDetailService.getApproveLead(SkuApproveConfigureEnum.SECOND_APPROVE.getDesc());
        if (CollectionUtils.isEmpty(productManagerSupervisorList)) {
            throw new ServiceException(ApiError.ERROR_9031);
        }

        //产品研发中心负责人
        List<String> departmentHeadList = productDetailService.getApproveLead(SkuApproveConfigureEnum.FIVE_APPROVE.getDesc());
        if (CollectionUtils.isEmpty(departmentHeadList)) {
            throw new ServiceException(ApiError.ERROR_9032);
        }

    }


    /**
     * 检查sku审核人是否为空
     *
     * @param sourceId
     * @return void
     * @author yl
     * @date 2023-02-01 18:47
     */
    public void checkSkuChangeAuditor(String sourceId) {
        List<String> skuIdList = Arrays.asList(sourceId);
        //产品经理
        List<String> productManagerList = productDetailService.getManagerBySkuIds(skuIdList);
        if (CollectionUtils.isEmpty(productManagerList)) {
            throw new ServiceException(ApiError.ERROR_9030);
        }

        //产品经理上级
        List<String> productManagerSupervisorList = productDetailService.getApproveLead(SkuApproveConfigureEnum.SECOND_APPROVE.getDesc());
        if (CollectionUtils.isEmpty(productManagerSupervisorList)) {
            throw new ServiceException(ApiError.ERROR_9031);
        }
        //财务人员检查
        if (StringUtils.isBlank(financial)) {
            throw new ServiceException(ApiError.ERROR_9035);
        }
        //品质部人员审核
        List<String> qualityPeople = productDetailService.getApproveLead(SkuApproveConfigureEnum.THIRD_APPROVE.getDesc());
        if (CollectionUtils.isEmpty(qualityPeople)) {
            throw new ServiceException(ApiError.ERROR_9034);
        }


        //产品中心部门负责人，供应链中心部门负责人
        List<String> departmentHeadList = productDetailService.getApproveLead(SkuApproveConfigureEnum.FOURTH_APPROVE.getDesc());
        if (CollectionUtils.isEmpty(departmentHeadList)) {
            throw new ServiceException(ApiError.ERROR_9033);
        }


    }


    /**
     * 启动一个变更流程
     *
     * @param
     * @return void
     * @author yl
     * @date 2023-01-31 14:44
     */
    public void startChangeProcess(ProductChangeEntity entity) {
        if (entity != null) {
            String type = entity.getType();
            String changeBom = BomConstant.CHANGE_BOM;
            String changeSku = BomConstant.CHANGE_SKU;
            String userId = commonService.getUserInfo().getUid();

            //如果是Bom 就要启动bom变更流程
            if (changeBom.equals(type)) {
                startChangeBomProcess(entity.getId(), userId, entity.getSourceId());
            }
            //如果是sku 就要启动sku变更流程
            if (changeSku.equals(type)) {
                startChangeSkuProcess(entity.getId(), userId, entity.getSourceId());
            }
        }


    }


    /**
     * 启动 sku 变更流程
     *
     * @param id
     * @param userId
     * @return void
     * @author yl
     * @date 2023-02-01 16:56
     */
    private void startChangeSkuProcess(String id, String userId, String sourceId) {
        FindProcessDTO findProcess = new FindProcessDTO();
        String businessType = WorkflowBusinessEnum.SKU_CHANGE.getBusinessType();
        String platform = WorkflowBusinessEnum.SKU_CHANGE.getPlatform();
        findProcess.setBusinessType(businessType);
        findProcess.setPlatform(platform);
        //获取到业务的信息
        BusinessInfoDTO business = workflowFeign.getBusiness(findProcess);
        if (business != null) {
            StartProcessDTO startProcess = new StartProcessDTO();
            startProcess.setUserId(userId);
            startProcess.setProcessDefinitionKey(business.getProcessDefinitionKey());
            startProcess.setBusinessKey(business.getBusinessKey());
            Map<String, Object> parameterMap = new HashMap<>();

            List<String> skuIdList = Arrays.asList(sourceId);
            //产品经理
            List<String> productManagerList = productDetailService.getManagerBySkuIds(skuIdList);
            if (CollectionUtils.isEmpty(productManagerList)) {
                throw new ServiceException(ApiError.ERROR_9030);
            }
            //产品经理
            parameterMap.put("productManagerList", productManagerList);

            List<String> productManagerSupervisorList = productDetailService.getApproveLead(SkuApproveConfigureEnum.SECOND_APPROVE.getDesc());
            if (CollectionUtils.isEmpty(productManagerSupervisorList)) {
                throw new ServiceException(ApiError.ERROR_9031);
            }
            //产品经理上级
            parameterMap.put("productManagerSupervisorList", productManagerSupervisorList);


            //品质部人员审核
            List<String> qualityPeople = productDetailService.getApproveLead(SkuApproveConfigureEnum.THIRD_APPROVE.getDesc());
            if (CollectionUtils.isEmpty(qualityPeople)) {
                throw new ServiceException(ApiError.ERROR_9034);
            }
            parameterMap.put("qualityPeopleList", qualityPeople);

            //财务人员
            if (StringUtils.isBlank(financial)) {
                throw new ServiceException(ApiError.ERROR_9035);
            }
            parameterMap.put("financial", financial);


            List<String> departmentHeadList = productDetailService.getApproveLead(SkuApproveConfigureEnum.FOURTH_APPROVE.getDesc());
            if (CollectionUtils.isEmpty(productManagerSupervisorList)) {
                throw new ServiceException(ApiError.ERROR_9033);
            }
            //产品中心部门负责人，供应链中心部门负责人
            parameterMap.put("departmentHeadList", departmentHeadList);
            startProcess.setParameterMap(parameterMap);
            //启动流程
            ProcessNodeDTO processResult = workflowFeign.startProcess(startProcess);
            //流程id
            String processId = processResult.getProcessId();
            if (StringUtils.isNotBlank(processId)) {
                WorkflowBusinessProcessDTO businessProcess = new WorkflowBusinessProcessDTO();
                businessProcess.setBusinessId(business.getId());
                businessProcess.setCreateTime(LocalDateTime.now());
                businessProcess.setBusinessTableId(id);
                businessProcess.setCreateUserId(userId);
                businessProcess.setProcessId(processId);
                //保存业务与流程的信息
                workflowFeign.saveBusinessProcess(businessProcess);
            }

        }
    }


    /**
     * 启动  bom变更流程
     *
     * @param id
     * @param userId
     * @return void
     * @author yl
     * @date 2023-02-01 16:49
     */
    private void startChangeBomProcess(String id, String userId, String sourceId) {
        FindProcessDTO findProcess = new FindProcessDTO();
        String businessType = WorkflowBusinessEnum.BOM_CHANGE.getBusinessType();
        String platform = WorkflowBusinessEnum.BOM_CHANGE.getPlatform();
        findProcess.setBusinessType(businessType);
        findProcess.setPlatform(platform);
        //获取到业务的信息
        BusinessInfoDTO business = workflowFeign.getBusiness(findProcess);
        if (business != null) {
            StartProcessDTO startProcess = new StartProcessDTO();
            startProcess.setUserId(userId);
            startProcess.setProcessDefinitionKey(business.getProcessDefinitionKey());
            startProcess.setBusinessKey(business.getBusinessKey());
            Map<String, Object> parameterMap = new HashMap<>();

            List<BomSkuDTO> skuList = bomSkuService.getByBomId(sourceId);
            List<String> skuIdList=bomInfoService.getSkuIdList(skuList);




            //产品经理
            List<String> productManagerList = productDetailService.getManagerBySkuIds(skuIdList);
            if (CollectionUtils.isEmpty(productManagerList)) {
                throw new ServiceException(ApiError.ERROR_9030);
            }

            //产品经理
            parameterMap.put("productManagerList", productManagerList);


            List<String> productManagerSupervisorList = productDetailService.getApproveLead(SkuApproveConfigureEnum.SECOND_APPROVE.getDesc());
            if (CollectionUtils.isEmpty(productManagerSupervisorList)) {
                throw new ServiceException(ApiError.ERROR_9031);
            }
            //产品经理上级
            parameterMap.put("productManagerSupervisorList", productManagerSupervisorList);

            List<String> departmentHeadList = productDetailService.getApproveLead(SkuApproveConfigureEnum.FIVE_APPROVE.getDesc());
            if (CollectionUtils.isEmpty(departmentHeadList)) {
                throw new ServiceException(ApiError.ERROR_9032);
            }
            //产品部负责人
            parameterMap.put("departmentHead", departmentHeadList.get(0));
            startProcess.setParameterMap(parameterMap);
            //启动流程
            ProcessNodeDTO processResult = workflowFeign.startProcess(startProcess);
            //流程id
            String processId = processResult.getProcessId();
            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(processId)) {
                WorkflowBusinessProcessDTO businessProcess = new WorkflowBusinessProcessDTO();
                businessProcess.setBusinessId(business.getId());
                businessProcess.setCreateTime(LocalDateTime.now());
                businessProcess.setBusinessTableId(id);
                businessProcess.setCreateUserId(userId);
                businessProcess.setProcessId(processId);
                //保存业务与流程的信息
                workflowFeign.saveBusinessProcess(businessProcess);
            }

        }
    }


    /**
     * 分页获取变更信息
     *
     * @param dto
     * @return com.erp.common.vo.PagingVO<java.util.List < com.erp.model.plm.vo.ProductChangePagingVO>>
     * @author yl
     * @date 2023-01-28 11:50
     */
    @Override
    public PagingVO<List<ProductChangePagingVO>> paging(PagingDTO<SearchPagingDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        SearchPagingDTO params = dto.getParams();
        String searchKeyword = params.getSearchKeyword();
        String searchType = params.getSearchType();
        List<String> changeIdList = new ArrayList<>();

        //当这个不为空的时候 表示可能要搜索 sku 或者 sku名称 或者bom 编号
        List<String> changeSearch = new ArrayList<>();
        if (StringUtils.isNotBlank(searchKeyword)) {
            changeSearch = baseMapper.getChangeSearchCondition(searchKeyword);
        }
        //待审核
        if (SearchType.WAIT_AUDIT.equals(searchType)) {
            String userId = commonService.getUserInfo().getUid();
            //获取我的待办信息
            List<MyToDoTaskVO> myToDoTasks = workflowFeign.getMyToDoTasks(userId);
            changeIdList = myToDoTasks.stream().map(MyToDoTaskVO::getBusinessTableId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(changeIdList)) {
                IPage pageData = new Page();
                return new PagingVO(pageData);
            }
        }
        //如果搜索是空就返回空
        if (CollectionUtils.isEmpty(changeSearch) && StringUtils.isNotBlank(searchKeyword)) {
            IPage pageData = new Page();
            return new PagingVO(pageData);
        }

        IPage pageData = baseMapper.paging(query, changeSearch, changeIdList);
        List<ProductChangePagingVO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(new Page());
        }
        String changeBom = BomConstant.CHANGE_BOM;
        String changeSku = BomConstant.CHANGE_SKU;
        List<String> businessTableIds = list.stream().map(ProductChangePagingVO::getId).collect(Collectors.toList());
        //当前审核人
        List<ProcessCurrentAuditorVO> currentAuditorList = workflowFeign.getProcessCurrentAudit(businessTableIds);

        List<FindUserDTO> userList = sysUserFeign.getUserList();

        //获取到类型是bom 的 源 id
        List<String> bomIdList = list.stream().filter(c -> changeBom.equals(c.getType())).
                map(ProductChangePagingVO::getSourceId).collect(Collectors.toList());


        List<BomVO> bomList = new ArrayList<>();
        //当不为空的时候表示有 bom 的
        if (CollectionUtils.isNotEmpty(bomIdList)) {
            bomList = bomInfoService.getByIds(bomIdList);
        }
        //获取到类型是sku 的 源 id
        List<String> skuIdList = list.stream().filter(c -> changeSku.equals(c.getType())).
                map(ProductChangePagingVO::getSourceId).collect(Collectors.toList());

        List<SkuVO> skuList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(skuIdList)) {
            skuList = productDetailService.getSkuBySkuIds(skuIdList);
        }
        for (ProductChangePagingVO item : list) {
            String type = item.getType();
            String sourceId = item.getSourceId();
            //如果是bom
            if (changeBom.equals(type)) {
                BomVO bom = bomList.stream().filter(b -> b.getBomId().equals(sourceId))
                        .findFirst().orElse(null);
                if (bom != null) {
                    item.setChangeSourceNo(bom.getSerialNumber());
                }
            }
            //如果是sku
            if (changeSku.equals(type)) {
                SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(sourceId))
                        .findFirst().orElse(null);
                if (sku != null) {
                    item.setChangeSourceNo(sku.getSkuNo());
                    item.setChangeSourceName(sku.getSkuName());
                }
            }

            ProcessCurrentAuditorVO currentAuditor = currentAuditorList.stream().filter(c -> c.getBusinessTableId().
                    equals(item.getId())).findFirst().orElse(null);
            List<String> userNameList = new ArrayList<>(5);
            if (currentAuditor != null) {
                List<String> handleUserIdList = currentAuditor.getHandleUserIdList();
                for (String  handleUserId:handleUserIdList) {
                    FindUserDTO user = userList.stream().filter(u -> u.getUserId().equals(handleUserId)).
                            findFirst().orElse(null);
                    if (user != null) {
                        userNameList.add(user.getUserName());
                    }
                }
            }
            if(CollectionUtils.isNotEmpty(userNameList)){
                item.setPersonApproving(String.join(",",userNameList));
            }

        }

        return new PagingVO(pageData);
    }


    /**
     * 作废
     *
     * @param productChangeId
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-28 16:41
     */
    @Override
    public Boolean cancellation(String productChangeId) {
        ProductChangeEntity entity = this.getById(productChangeId);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_95105);
        }
        Integer state = entity.getState();
        //待审核
        Integer waitAudit = ProductChangeStateEnum.WAIT_AUDIT.getState();
        //审核不通过
        Integer auditNoPassState = ProductChangeStateEnum.AUDIT_NO_PASS.getState();
        //当不等于他们的时候
        if (!waitAudit.equals(state) && !auditNoPassState.equals(state)) {
            throw new ServiceException(ApiError.ERROR_95106);
        }
        entity.setState(ProductChangeStateEnum.CANCELLATION.getState());
        return this.updateById(entity);
    }


    /**
     * 根据变更的类型 获取到对应的数据
     *
     * @param type
     * @return java.util.List<com.erp.common.dto.base.BaseIdDTO>
     * @author yl
     * @date 2023-01-28 17:02
     */
    @Override
    public List<ChangeInfoDTO> getChangeByType(String type, String searchKeyword) {
        String changeBom = BomConstant.CHANGE_BOM;
        String changeSku = BomConstant.CHANGE_SKU;
        if (changeBom.equals(type)) {
            return bomInfoService.getBomInfo(searchKeyword);
        }
        if (changeSku.equals(type)) {
            return productDetailService.getSku(searchKeyword);
        }
        return new ArrayList<>();
    }


    /**
     * 变更详情
     *
     * @param id
     * @return com.erp.model.plm.dto.ProductChangeDTO
     * @author yl
     * @date 2023-01-30 10:50
     */
    @Override
    public ProductChangeDTO details(String id) {
        return null;
    }


    /**
     * 编辑 变更信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-30 11:54
     */
    @Override
    public Boolean edit(UpdateChangeDTO dto) {
        String id = dto.getId();
        //获取到变更信息
        ProductChangeEntity changeEntity = this.getById(id);
        if (Objects.isNull(changeEntity)) {
            throw new ServiceException(ApiError.ERROR_95105);
        }
        Integer state = changeEntity.getState();
        Integer waitAudit = ProductChangeStateEnum.WAIT_AUDIT.getState();
        //只有待审核才能编辑
        if (!waitAudit.equals(state)) {
            throw new ServiceException(ApiError.ERROR_95109);
        }
        //新的
        String sourceId = dto.getSourceId();
        changeEntity.setSourceId(sourceId);
        changeEntity.setType(dto.getType());
        Boolean result = this.updateById(changeEntity);
        if (result) {
            /**
             * 如果变更成功 如果是bom
             * 那么原来老的 bom 状态要改回来
             * bom 要改状态
             *
             */
            changeDetailsService.saveChangeDetails(id, dto.getDetailsJson());

        }
        return result;
    }


    /**
     * 变更审核通过
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-01-30 14:03
     */
    @Override
    public void approvalPass(AuditParamDTO dto) {
        String id = dto.getId();
        //获取到变更信息
        ProductChangeEntity changeEntity = this.getById(id);
        if (Objects.isNull(changeEntity)) {
            throw new ServiceException(ApiError.ERROR_95105);
        }
        String comment = dto.getComment();
        changeEntity.setState(ProductChangeStateEnum.AUDIT_ING.getState());
        if (StringUtils.isNotBlank(dto.getComment())) {
            changeEntity.setRemark(dto.getComment());
        }

        String userId = commonService.getUserInfo().getUid();
        BusinessTableDTO tableDTO = new BusinessTableDTO();
        tableDTO.setBusinessTableId(id);
        tableDTO.setUserId(userId);
        //获取到用户该业务表的待办任务
        MyToDoTaskVO processTask = workflowFeign.getByBusinessTableId(tableDTO);
        if (Objects.isNull(processTask)) {
            throw new ServiceException(ApiError.ERROR_94005);
        }

        //审核
        ApproveProcessDTO approveProcess = new ApproveProcessDTO();
        approveProcess.setTaskId(processTask.getTaskId());
        approveProcess.setProcessInstanceId(processTask.getProcessInstanceId());
        approveProcess.setUserId(userId);
        approveProcess.setComment(comment);

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("agree", true);
        approveProcess.setParameterMap(parameterMap);
        this.updateById(changeEntity);
        ProcessNodeDTO node = workflowFeign.taskPass(approveProcess);


    }


    /**
     * 变更审核不通过
     * 不通过要停止流程吗
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-01-30 14:10
     */
    @Override
    public void approvalNoPass(AuditParamDTO dto) {
        String id = dto.getId();
        //获取到变更信息
        ProductChangeEntity changeEntity = this.getById(id);
        if (Objects.isNull(changeEntity)) {
            throw new ServiceException(ApiError.ERROR_95105);
        }
        if (StringUtils.isNotBlank(dto.getComment())) {
            changeEntity.setRemark(dto.getComment());
        }
        changeEntity.setApprovalFinishTime(new Date());
        changeEntity.setState(ProductChangeStateEnum.AUDIT_NO_PASS.getState());
        this.updateById(changeEntity);

        String userId = commonService.getUserInfo().getUid();
        BusinessTableDTO tableDTO = new BusinessTableDTO();
        tableDTO.setBusinessTableId(id);
        tableDTO.setUserId(userId);
        //获取到用户该业务表的待办任务
        MyToDoTaskVO processTask = workflowFeign.getByBusinessTableId(tableDTO);
        if (Objects.isNull(processTask)) {
            throw new ServiceException(ApiError.ERROR_94005);
        }

        if (processTask != null) {
            ApproveProcessDTO process = new ApproveProcessDTO();
            process.setComment(dto.getComment());
            process.setProcessInstanceId(processTask.getProcessInstanceId());
            process.setUserId(userId);
            process.setTaskId(processTask.getTaskId());

            Map<String, Object> parameterMap = new HashMap<>();
            parameterMap.put("agree", false);
            process.setParameterMap(parameterMap);

            //终止流程
            workflowFeign.taskNoPass(process);
        }
    }


    /**
     * 流程最终通过后的
     * 操作
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-01-30 16:41
     */
    @Override
    public void processPass(ProcessPassDTO dto) {
        //从流程那边获取到具体业务表id
        String id = dto.getBusinessTableId();
        if (StringUtils.isNotBlank(id)) {
            //获取到变更信息
            ProductChangeEntity change = this.getById(id);
            if (change != null) {
                String type = change.getType();
                change.setApprovalFinishTime(new Date());
                change.setState(ProductChangeStateEnum.AUDIT_PASS.getState());
                this.updateById(change);
                //获取到对应的 json
                String detailsJson = changeDetailsService.getDetailsJson(change.getId());
                if (StringUtils.isNotBlank(detailsJson)) {
                    //对应就是bom
                    if (BomConstant.CHANGE_BOM.equals(type)) {
                        BomDTO bom = JSONObject.parseObject(detailsJson, BomDTO.class);
                        //变更bom
                        bomInfoService.changeBom(bom);
                    }

                    //对应就是sku
                    if (BomConstant.CHANGE_SKU.equals(type)) {
                        ProductSmallestUnitDTO sku = JSONObject.parseObject(detailsJson, ProductSmallestUnitDTO.class);
                        productDetailService.changeSku(sku);
                    }
                }
            }

        }
    }


    /**
     * 重启流程
     *
     * @param id
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-02-01 17:03
     */
    @Override
    public Boolean restartAudit(String id) {
        //获取到变更信息
        ProductChangeEntity change = this.getById(id);
        if (Objects.isNull(change)) {
            throw new ServiceException(ApiError.ERROR_95105);
        }

        String type = change.getType();
        String changeBom = BomConstant.CHANGE_BOM;
        Boolean isBom = changeBom.equals(type);
        //如果是bom 检查审核人为空不
        if (isBom) {
            checkBomChangeAuditor(change.getSourceId());
        } else {
            checkSkuChangeAuditor(change.getSourceId());
        }
        Integer state = change.getState();
        if (!ProductChangeStateEnum.AUDIT_NO_PASS.getState().equals(state)) {
            throw new ServiceException(ApiError.ERROR_95099);
        }
        change.setState(ProductChangeStateEnum.WAIT_AUDIT.getState());
        Boolean result = this.updateById(change);
        if (result) {
            //启动流程
            startChangeProcess(change);
        }
        return result;
    }

    /**
     * bom 详情
     *
     * @param changeEntity
     * @return com.erp.model.plm.dto.ProductBomChangeDTO
     * @author yl
     * @date 2023-02-02 9:49
     */
    @Override
    public ProductBomChangeDTO getBomDetails(ProductChangeEntity changeEntity) {
        try {
            if (changeEntity != null) {
                ProductBomChangeDTO result = new ProductBomChangeDTO();
                result.setId(changeEntity.getId());
                result.setSourceId(changeEntity.getSourceId());
                result.setType(changeEntity.getType());
                //获取到对应的 json
                String detailsJson = changeDetailsService.getDetailsJson(changeEntity.getId());
                if (StringUtils.isNotBlank(detailsJson)) {
                    BomDTO bom = JSONObject.parseObject(detailsJson, BomDTO.class);
                    result.setInfo(bom);
                }
                return result;
            }
        } catch (Exception e) {
            log.error("getBomDetails", e);
        }
        return null;

    }

    @Override
    public ProductChangeDTO skuDetails(ProductChangeEntity changeEntity) {
        try {
            if (changeEntity != null) {
                ProductChangeDTO result = new ProductChangeDTO();
                result.setId(changeEntity.getId());
                result.setSourceId(changeEntity.getSourceId());
                result.setType(changeEntity.getType());
                //获取到对应的 json
                String detailsJson = changeDetailsService.getDetailsJson(changeEntity.getId());
                if (StringUtils.isNotBlank(detailsJson)) {
                    ProductSmallestUnitDTO bom = JSONObject.parseObject(detailsJson, ProductSmallestUnitDTO.class);
                    result.setInfo(bom);
                }
                return result;
            }
        } catch (Exception e) {
            log.error("skuDetails", e);
        }
        return null;

    }

    /**
     * 获取到源 id 审核中（变更中）
     *
     * @param sourceIds
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-02-02 16:29
     */
    @Override
    public List<String> getBySourceId(List<String> sourceIds) {
        if (CollectionUtils.isNotEmpty(sourceIds)) {
            List<Integer> stateList = new ArrayList<>(2);
            stateList.add(ProductChangeStateEnum.AUDIT_ING.getState());
            stateList.add(ProductChangeStateEnum.WAIT_AUDIT.getState());
            LambdaQueryWrapper<ProductChangeEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(ProductChangeEntity::getSourceId);
            queryWrapper.in(ProductChangeEntity::getSourceId, sourceIds);
            queryWrapper.in(ProductChangeEntity::getState, stateList);
            return this.listObjs(queryWrapper, Object::toString);
        }
        return new ArrayList<>();

    }


    /**
     * 根据关键字搜索 sku 或者bom 的编号
     *
     * @param searchKeyword
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-02-03 16:07
     */
    @Override
    public List<String> getChangeSearchCondition(String searchKeyword) {
        return baseMapper.getChangeSearchCondition(searchKeyword);
    }

    /**
     * 审核情况
     *
     * @param id
     * @return void
     * @author yl
     * @date 2023-02-08 9:00
     */
    @Override
    public List<ApproveNodeRecordVO> auditInfo(String id) {
        if (StringUtils.isNotBlank(id)) {
            List<ApproveNodeRecordVO> list = workflowFeign.getHistoryTaskByBusinessTableId(id);
            return list;
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> listChangeField(String id) {
        ProductChangeEntity productChangeEntity = this.getById(id);
        if (ObjectUtils.isEmpty(productChangeEntity)) {
            throw new ServiceException(ApiError.ERROR_95127);
        }
        //查询变更后的json字符串
        String detailsJson = productChangeDetailsService.getDetailsJson(id);
        if (StringUtils.isBlank(detailsJson)) {
            throw new ServiceException(ApiError.ERROR_95128);
        }
        //变更后数据
        ProductSmallestUnitDTO newBom = JSONObject.parseObject(detailsJson, ProductSmallestUnitDTO.class);
        //变更前数据
        ProductSmallestUnitDTO oldbom = productDetailService.getSkuBySkuId(productChangeEntity.getSourceId());
        if (ObjectUtils.isEmpty(oldbom)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        List<String> resultList = new ArrayList<>();
        setList(newBom.getProductManySpecBaseDTO(), oldbom.getProductManySpecBaseDTO(), resultList);
        setList(CollectionUtils.isNotEmpty(newBom.getProductCertificateShowDTOList()) ? newBom.getProductCertificateShowDTOList().get(0) : null, CollectionUtils.isNotEmpty(oldbom.getProductCertificateShowDTOList()) ? oldbom.getProductCertificateShowDTOList().get(0) : null, resultList);
        setList(newBom.getProductCostShowDTO(), oldbom.getProductCostShowDTO(), resultList);
        setList(newBom.getProductSaleShowDTO(), oldbom.getProductSaleShowDTO(), resultList);
        setList(newBom.getProductManySkuDetail(), oldbom.getProductManySkuDetail(), resultList);
        setList(newBom.getProductPurchaseShowDTO(), oldbom.getProductPurchaseShowDTO(), resultList);
        setList(CollectionUtils.isNotEmpty(newBom.getRemarkEntityList()) ? newBom.getRemarkEntityList().get(0) : null, CollectionUtils.isNotEmpty(oldbom.getRemarkEntityList()) ? oldbom.getRemarkEntityList().get(0) : null, resultList);
        setList(newBom.getProductLogisticsShowDTO(), oldbom.getProductLogisticsShowDTO(), resultList);
        setList(newBom.getProductPackShowDTO(), oldbom.getProductPackShowDTO(), resultList);
        if (CollectionUtils.isNotEmpty(resultList)) {
            resultList = resultList.stream().filter(e -> !"createTime".equals(e) && !"updateTime".equals(e) && !"updateUserId".equals(e) && !"updateUserName".equals(e) && !"createUserName".equals(e)).distinct().collect(Collectors.toList());
        }
        return resultList;
    }


    private void setList(Object newObj, Object oldObj, List<String> resultList) {
        List<String> list = sysLogService.listSysLogField(newObj, oldObj);
        if (CollectionUtils.isNotEmpty(list)) {
            resultList.addAll(list);
        }
    }
}
