package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoPriceChangeDTO;
import com.erp.model.oms.dto.SoPriceChangeDetailDTO;
import com.erp.model.oms.dto.SoPriceDetailDTO;
import com.erp.model.oms.dto.excel.SoPriceChangeExportExcelDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoPriceChangeTabFlagEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.oms.mapper.SoPriceChangeMapper;
import com.erp.server.oms.query.SoPriceChangeQueryHandler;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SO_PRICE_CHANGE;

/**
 * <p>
 * 销售价变更表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-03-24
 */
@Slf4j
@Service
public class SoPriceChangeServiceImpl extends SuperServiceImpl<SoPriceChangeMapper, SoPriceChangeEntity> implements SoPriceChangeService {

    @Resource
    private SoPriceDetailService soPriceDetailService;

    @Resource
    private SoPriceChangeDetailService soPriceChangeDetailService;

    @Resource
    private SoPriceHistoryService soPriceHistoryService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private OmsAttachmentService attachmentService;

    @Resource
    private OperateLogService moduleOperateLogService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoPriceChangeQueryHandler soPriceChangeQueryHandler;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private CustomerInfoService customerInfoService;

    /**
     * 添加销售价目变更
     * @param dto
     * @return com.erp.model.scm.entity.SoPriceChangeEntity
     * @author will
     * @date 2025-03-28 11:49
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public SoPriceChangeEntity add(SoPriceChangeDTO.AddDTO dto) {
        SoPriceChangeEntity changeEntity = new SoPriceChangeEntity();
        BeanMapper.copy(dto, changeEntity);
        //生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSTJ);
        changeEntity.setCode(code);
        changeEntity.setId(IdWorker.getIdStr());
        String pricingUserId = dto.getAdjustUserId();
        if (StringUtils.isNotBlank(pricingUserId)) {
            FindUserDTO user = sysUserFeign.getUserByUserId(pricingUserId);
            changeEntity.setAdjustUserName(user != null ? user.getUserName() : "");
        }
        //获取组织
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Collections.singletonList(dto.getSoOrgId()));
        if (CollectionUtils.isNotEmpty(orgList)) {
            changeEntity.setSoOrgName(orgList.get(0).getName());
        }
        //保存成功
        boolean addResult = this.save(changeEntity);
        if (!addResult) {
            throw new ServiceException(ApiError.ERROR_1002);
        }
        Class<SoPriceChangeEntity> credentialClass = SoPriceChangeEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String type = tableName.value();
        //保存附件
        attachmentService.batchSaveOrUpdate(dto.getAttachmentUrlList(), dto.getAttachmentNameList(), type, changeEntity.getId());

        //添加价格变更明细
        soPriceChangeDetailService.addPriceChangeDetail(changeEntity.getId(), dto.getSoPriceChangeDetailList());
        //添加日志
        String content = String.format("新增了一个{%s}-销售调价-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
        addModuleOperateLog(content, ModuleTypeEnum.SO_PRICE_CHANGE.getCode(), changeEntity.getId());
        return changeEntity;
    }

    /**
     * 提交并审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author will
     * @date 2025-03-28 14:08
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public SoPriceChangeEntity addAndSubmit(SoPriceChangeDTO.AddDTO dto) {
        SoPriceChangeEntity entity = this.add(dto);
        if (null == entity) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Collections.singletonList(entity.getId()), Boolean.TRUE);
        if (!result) {
            throw new ServiceException(ApiError.ERROR_1002);
        }
        return entity;
    }

    /**
     * 销售价目变更详情
     *
     * @param id
     * @return com.erp.model.scm.dto.SoPriceChangeDTO.UpdateDTO
     * @author will
     * @date 2025-03-28 14:24
     */
    @Override
    public SoPriceChangeDTO.ViewDTO view(String id) {
        SoPriceChangeEntity changeEntity = this.getById(id);
        if (Objects.isNull(changeEntity)) {
            throw new ServiceException(ApiError.ERROR_98028);
        }
        SoPriceChangeDTO.ViewDTO viewDTO = new SoPriceChangeDTO.ViewDTO();
        BeanMapper.copy(changeEntity, viewDTO);
        viewDTO.setApproveStatus(changeEntity.getApproveStatus().getStatus());
        //附件信息
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessId(id);
        List<String> attachmentUrlList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
        List<String> attachmentNameList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
        viewDTO.setAttachmentNameList(attachmentNameList);
        viewDTO.setAttachmentUrlList(attachmentUrlList);
        //获取明细信息
        List<SoPriceChangeDetailDTO.ViewDTO> soPriceDetailList = soPriceChangeDetailService.getByPriceChangeId(id);
        List<String> skuIds = soPriceDetailList.stream().map(SoPriceChangeDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuNoList = plmTaskFeign.listSkuProductByIds(skuIds);

        //获取供应商
        List<String> customerIds = soPriceDetailList.stream().map(SoPriceChangeDetailDTO.ViewDTO::getCustomerId).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerEntities = customerInfoService.listByIds(customerIds);

        for (SoPriceChangeDetailDTO.ViewDTO dto : soPriceDetailList) {
            SkuVO skuVO = skuNoList.stream().filter(obj -> obj.getSkuId().equals(dto.getSkuId())).findFirst().orElse(new SkuVO());
            dto.setProductName(skuVO.getSkuName());
            CustomerInfoEntity customerInfoEntity = customerEntities.stream().filter(req -> dto.getCustomerId().equals(req.getId())).findFirst().orElse(new CustomerInfoEntity());
            dto.setCustomerName(customerInfoEntity.getName());
        }
        //查询价目表主标Id
        List<String> soPriceDetailIds = soPriceDetailList.stream().map(SoPriceChangeDetailDTO.ViewDTO::getSoPriceDetailId).distinct().collect(Collectors.toList());
        List<SoPriceDetailEntity> soPriceDetailEntities = soPriceDetailService.listByIds(soPriceDetailIds);
        List<String> soPriceIdList = soPriceDetailEntities.stream().map(SoPriceDetailEntity::getMainId).distinct().collect(Collectors.toList());
        viewDTO.setSoPriceIdList(soPriceIdList);
        viewDTO.setSoPriceChangeDetailList(soPriceDetailList);
        return viewDTO;
    }


    /**
     * 修改销售价目变更
     *
     * @param dto
     * @return com.erp.model.scm.entity.SoPriceChangeEntity
     * @author will
     * @date 2025-03-28 16:40
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateSoPriceChange(SoPriceChangeDTO.UpdateDTO dto) {
        String id = dto.getId();
        SoPriceChangeEntity priceChangeEntity = this.getById(id);
        if (Objects.isNull(priceChangeEntity)) {
            throw new ServiceException(ApiError.ERROR_98028);
        }
        SoPriceChangeEntity old = new SoPriceChangeEntity();
        BeanMapper.copy(priceChangeEntity, old);
        //状态值
        String status = priceChangeEntity.getApproveStatus().getStatus();
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        if (!statusList.contains(status)) {
            throw new ServiceException(ApiError.ERROR_98019);
        }
        //code
        String code = priceChangeEntity.getCode();
        BeanMapper.copy(dto, priceChangeEntity);
        priceChangeEntity.setCode(code);
        String pricingUserId = dto.getAdjustUserId();
        if (StringUtils.isNotBlank(pricingUserId)) {
            FindUserDTO user = sysUserFeign.getUserByUserId(pricingUserId);
            priceChangeEntity.setAdjustUserName(user != null ? user.getUserName() : "");
        }
        String orgId = dto.getSoOrgId();
        //获取组织
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(orgId));
        if (CollectionUtils.isNotEmpty(orgList)) {
            priceChangeEntity.setSoOrgName(orgList.get(0).getName());
        }
        //修改成功
        Boolean result = this.updateById(priceChangeEntity);
        if (!result) {
           throw new ServiceException(ApiError.ERROR_1002);
        }
       //添加日志
        moduleOperateLogService.addModuleOperateLogByObj(old, priceChangeEntity, ModuleTypeEnum.SO_PRICE_CHANGE.getCode(), id, "", "");

        Class<SoPriceChangeEntity> credentialClass = SoPriceChangeEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String type = tableName.value();
        //保存附件
        attachmentService.batchSaveOrUpdate(dto.getAttachmentUrlList(), dto.getAttachmentNameList(), type, id);
        //修改明细
        soPriceChangeDetailService.updatePriceChangeDetail(id, dto.getSoPriceChangeDetailList());
        return id;
    }


    /**
     * 销售价目变更 提交审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author will
     * @date 2025-03-28 16:47
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids, Boolean isStartProcess) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        //要去掉已审核通过的
        List<SoPriceChangeEntity> priceChangeList = this.listByIds(ids);
        priceChangeList = priceChangeList.stream().filter(p -> !ApproveStatusEnum.APPROVE.equals(p.getApproveStatus())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(priceChangeList)) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }

        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        //审核中
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();

        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        long count = priceChangeList.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        if (isStartProcess) {
            //提交流程
            startProcess(priceChangeList);
        }
        List<Pair<String, String>> pairList = priceChangeList.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(waitSubmitStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = priceChangeList.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(rejectStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        Boolean result = this.updateApproveStatus(priceChangeList, ApproveStatusEnum.getByStatus(ingStatus));
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            batchAddModuleOperateLog(content, ModuleTypeEnum.SO_PRICE_CHANGE.getCode(), pairList, "状态变更");

            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.SO_PRICE_CHANGE.getCode(), rejectPairList, "状态变更");
        }
        return result;
    }


   /**
    * 销售价目变更 审核
    * @author will
    * @date 2025/3/26 10:54
    * @param entity
    * @param dto
    * @return com.common.business.dto.base.BatchResultDTO
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(SoPriceChangeEntity entity, ApproveOneDTO dto) {
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus().getStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }
        //调用审核流程
        approveProcess(entity, dto);
        //添加日志
        moduleOperateLogService.addModuleOperateLog(String.format("审核【%s】了一个销售价目【%s】", ApproveTypeEnum.getName(dto.getType()), entity.getCode()).concat(StringUtils.isNotBlank(dto.getComment()) ? String.format(",意见：%s", dto.getComment()) : ""), ModuleTypeEnum.SO_PRICE_CHANGE.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(entity.getApproveStatus()));
    }

   /**
    * 结束审核
    * @author will
    * @date 2025/3/26 10:47
    * @param dto
    * @param entity
    * @return com.common.business.dto.base.BatchResultDTO
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SoPriceChangeEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        Boolean result = this.updateApproveStatus(Collections.singletonList(entity), approveStatus);
        if (Boolean.FALSE.equals(result)) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        if (ApproveType.PASS.equals(dto.getType())) {
            //更新价目表数据
            soPriceChangeDetailService.updateSoPriceDetail(Collections.singletonList(entity));
        }
        return result;
    }

    /**
     * 取消流程
     *
     * @param ids
     * @return java.lang.Boolean
     * @author will
     * @date 2025-03-28 16:56
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        List<SoPriceChangeEntity> list = this.listByIds(ids);
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !s.getApproveStatus().getStatus().equals(approveIngStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //撤销现有流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ids.forEach(obj -> {
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.SO_PRICE_CHANGE.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);
        });

        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus));
        if (result) {
            String content = String.format("状态由[%s]变更为[%s] ", ApproveStatusEnum.APPROVE_ING.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            List<Pair<String, String>> pairList = list.stream().
                    map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
            batchAddModuleOperateLog(content, ModuleTypeEnum.SO_PRICE_CHANGE.getCode(), pairList, "取消流程");
        }
        return result;
    }

    /**
     * 分页获取销售价目变更数据
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.SoPriceChangeDTO.PagingViewDTO>
     * @author will
     * @date 2025-03-28 17:15
     */
    @Override
    public PagingVO<SoPriceChangeDTO.PagingViewDTO> paging(PagingDTO<SoPriceChangeDTO.PagingParamDTO> dto) {
        SoPriceChangeDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());

        IPage pageData = baseMapper.paging(query, params);
        handlePaging(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
     * 修改并审核
     * @param dto
     * @return java.lang.Boolean
     * @author will
     * @date 2025-03-29 9:42
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(SoPriceChangeDTO.UpdateDTO dto) {
        String id = this.updateSoPriceChange(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Collections.singletonList(id), Boolean.TRUE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<SoPriceChangeEntity> priceChangeList = this.listByIds(ids);
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        long count = priceChangeList.stream().filter(p -> !p.getApproveStatus().getStatus().equals(waitSubmitStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        //删除价目表
        Boolean result = this.removeByIds(ids);
        if (!result) {
          throw new ServiceException(ApiError.ERROR_1002);
        }
        //添加日志
        String content = "删除价目表[%s]";
        List<Pair<String, String>> pairList = priceChangeList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        batchAddModuleOperateLog(content, ModuleTypeEnum.SO_PRICE_CHANGE.getCode(), pairList, "删除");
        attachmentService.deleteByBusinessIds(ids);
        return result;
    }
    
    @Override
    public Boolean updateDetailRemark(List<String> ids, String remark) {
        if (CollectionUtils.isEmpty(ids)) {
            return Boolean.TRUE;
        }
        soPriceChangeDetailService.updateDetailRemark(ids,remark);
        return Boolean.TRUE;
    }

    @Override
    public void export(SoPriceChangeDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("销售调价数据", EXPORT_SO_PRICE_CHANGE.getCode(), dto);
    }

    /**
     * 修改状态
     * @param list
     * @param statusEnum
     * @return java.lang.Boolean
     * @author will
     * @date 2025-03-28 16:50
     */
    private Boolean updateApproveStatus(List<SoPriceChangeEntity> list, ApproveStatusEnum statusEnum) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (CollectionUtils.isNotEmpty(list)) {
            list.stream().forEach(obj -> {
                if (ApproveStatusEnum.APPROVE.equals(statusEnum) || ApproveStatusEnum.REJECT.equals(statusEnum)) {
                    obj.setApproveTime(LocalDateTime.now());
                    obj.setApproveUserId(userInfo.getUid());
                    obj.setApproveUserName(userInfo.getUserName());
                } else {
                    obj.setApproveTime(null);
                    obj.setApproveUserId("");
                    obj.setApproveUserName("");
                }
                obj.setApproveStatus(statusEnum);
            });
            return this.updateBatchById(list);
        }
        return false;
    }


    /**
     * 批量添加日志
     *
     * @param content
     * @param code
     * @param pairList
     * @param operation
     * @return void
     * @author will
     * @date 2025-03-28 16:46
     */
    private void batchAddModuleOperateLog(String content, String code, List<Pair<String, String>> pairList, String operation) {
        moduleOperateLogService.batchAddModuleOperateLog(content, code, pairList, operation);

    }


    /**
     * 添加日志
     *
     * @param content
     * @param code
     * @param businessId
     * @return void
     * @author will
     * @date 2025-03-28 12:25
     */
    private void addModuleOperateLog(String content, String code, String businessId) {
        moduleOperateLogService.addModuleOperateLog(content, code, businessId, "新增操作");

    }

    /**
     * @param list
     * @description: 提交流程
     * @author Will
     * @date: 2023/7/3 14:39
     */
    private void startProcess(List<SoPriceChangeEntity> list) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ValidList<ProcessManagementDTO.StartDTO> resultList = new ValidList<>();
        list.forEach(obj -> {
            ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
            startDTO.setBusinessId(obj.getId());
            startDTO.setBusinessCode(obj.getCode());
            startDTO.setBusinessKey(SourceTypeEnum.SO_PRICE_CHANGE.getCode());
            startDTO.setBusinessName(obj.getCode());
            startDTO.setUserId(userInfo.getUid());
            startDTO.setVariablesMap(BeanUtil.beanToMap(obj));
            resultList.add(startDTO);
        });
        ApiResult<List<ProcessManagementDTO.StartResultDTO>> listApiResult = workflowFeign.batchStartProcess(resultList);
        if (!listApiResult.isSuccess()) {
            throw new ServiceException(listApiResult.getMsg());
        }
    }

    /**
     * 流程审核
     * @author will
     * @date 2025/3/26 10:55
     * @param entity
     * @param dto
     * @return com.common.business.dto.base.BatchResultDTO
     */
    private void approveProcess(SoPriceChangeEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SO_PRICE_CHANGE.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> result = workflowFeign.approve(approveDTO);
        Integer code = result.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = result.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || Boolean.TRUE.equals(!data.getIsExistProcess())) {
             approveEnd(dto,entity);
        }
    }


    /**
     * variablesMap值赋值
     * @author jack
     * @date 2025/5/27 10:51
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(SoPriceChangeEntity entity) {
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        List<SoPriceChangeDetailEntity> detailList = soPriceChangeDetailService.lambdaQuery().eq(SoPriceChangeDetailEntity::getMainId,entity.getId()).list();
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        variablesMap.put(ThirdConstants.DETAIL_LIST, BeanUtil.copyToList(detailList,Map.class));
        return variablesMap;
    }

    @Override
    public List<SoPriceChangeDTO.TabListDTO> tabList(PermissionsDTO dto) {
        SoPriceChangeTabFlagEnum[] values = SoPriceChangeTabFlagEnum.values();
        List<SoPriceChangeDTO.TabListDTO> list = new ArrayList<>();
        for (SoPriceChangeTabFlagEnum item : values) {
            SoPriceChangeDTO.PagingParamDTO searchParamDTO = new SoPriceChangeDTO.PagingParamDTO();
            searchParamDTO.setPermissionSql(dto.getPermissionSql());
            SoPriceChangeDTO.TabListDTO resultDTO = new SoPriceChangeDTO.TabListDTO();
            String tabSql = soPriceChangeQueryHandler.getTabSql(item.getCode());
            HashMap<String,String> map = new HashMap<>();
            map.put("default",tabSql);
            searchParamDTO.setSqlMap(map);
            Integer count = this.baseMapper.tabList(searchParamDTO);
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public PagingVO<SoPriceChangeExportExcelDTO> exportSoPriceChange(PagingDTO<SoPriceChangeDTO.PagingParamDTO> dto) {
        //获取导出数据
        PagingVO<SoPriceChangeDTO.PagingViewDTO> page = this.paging(dto);
        if (CollUtil.isEmpty( page.getList())) {
            throw new ServiceException("导出数据为空");
        }
        List<SoPriceChangeExportExcelDTO> resultList = new ArrayList<>();
        for (SoPriceChangeDTO.PagingViewDTO item : page.getList()) {
            SoPriceChangeExportExcelDTO excelDTO = new SoPriceChangeExportExcelDTO();
            BeanMapper.copy(item, excelDTO);
            Integer minQty = item.getMinQty();
            Integer maxQty = item.getMaxQty();
            excelDTO.setQtySection(minQty + "-" + maxQty);

            //含税单价
            excelDTO.setTaxPrice(item.getCurrencySymbol() + item.getTaxPrice().toString());
            //旧含税单价
            excelDTO.setOldTaxPrice(item.getCurrencySymbol() + item.getOldTaxPrice().toString());
            excelDTO.setApproveTime(item.getApproveTime());
            resultList.add(excelDTO);
        }
        return new PagingVO<>(resultList, (int) page.getTotalPage(), dto.getPageSize(), dto.getCurrPage());
    }

    /**
     * 根据销售价目表id  获取对应产品信息
     *
     * @param dto
     * @return java.util.List<com.erp.model.scm.dto.SoPriceChangeDTO.ViewDTO>
     * @author yl
     * @date 2023-03-31 16:07
     */
    @Override
    public List<SoPriceChangeDetailDTO.ViewDTO> getSkuChangeList(SoPriceChangeDetailDTO.SkuChangeParamDTO dto) {
        return soPriceDetailService.listPriceChangeDetail(dto);
    }

    /**
     * 分页数据处理
     * @author will
     * @date 2025/3/28 09:30
     * @param list
     */
    private void handlePaging (List<SoPriceChangeDTO.PagingViewDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<String> skuIds = list.stream().map(SoPriceChangeDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuNoList = plmTaskFeign.listSkuProductByIds(skuIds);
        List<String> currencyIdList = list.stream().map(SoPriceChangeDTO.PagingViewDTO::getCurrency).collect(Collectors.toList());
        //币种信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);

        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SO_PRICE_CHANGE.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code,listApiResult.getMsg()));
            }
        }
        //历史调价数据
        List<String> changeDetailIdList = list.stream().map(SoPriceChangeDTO.PagingViewDTO::getChangeDetailId).collect(Collectors.toList());
        List<SoPriceHistoryEntity> soPriceHistoryList = soPriceHistoryService.listByChangeDetailIdList(changeDetailIdList);

        //原调价表数据
        List<String> priceDetailIdList = list.stream().map(SoPriceChangeDTO.PagingViewDTO::getSoPriceDetailId).collect(Collectors.toList());
        List<SoPriceDetailDTO.ViewDTO> priceDetailList = soPriceDetailService.listBySoPriceDetailIds(priceDetailIdList);


        List<String> customerIdList = list.stream().map(SoPriceChangeDTO.PagingViewDTO::getCustomerId).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerEntities = customerInfoService.listByIds(customerIdList);

        for (SoPriceChangeDTO.PagingViewDTO item : list) {
            SkuVO skuVO = skuNoList.stream().filter(req -> req.getSkuId().equals(item.getSkuId())).findFirst().orElse(new SkuVO());
            item.setProductName(skuVO.getSkuName());
            ApproveStatusEnum approveStatusEnum = item.getApproveStatus();
            item.setApproveStatusCode(approveStatusEnum.getStatus());
            item.setApproveStatusName(approveStatusEnum.getName());
            //币种
            String currency = item.getCurrency();
            String currencySymbol = currencyList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("￥");
            item.setCurrencySymbol(currencySymbol);

            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(item.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                item.setApproveUserName(curApprove);
            }

            //客户
            CustomerInfoEntity customerInfoEntity = customerEntities.stream().filter(req -> item.getCustomerId().equals(req.getId())).findFirst().orElse(new CustomerInfoEntity());
            item.setCustomerName(customerInfoEntity.getName());
            //历史调价
            SoPriceHistoryEntity soPriceHistoryEntity = soPriceHistoryList.stream().filter(obj -> StrUtil.equals(item.getChangeDetailId(), obj.getChangeDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soPriceHistoryEntity)) {
                item.setOldTaxPrice(soPriceHistoryEntity.getTaxPrice());
                //升降比例
                BigDecimal offsetRate = MathUtil.divide(MathUtil.subtract(item.getTaxPrice(), soPriceHistoryEntity.getTaxPrice()), soPriceHistoryEntity.getTaxPrice()).multiply(MathUtil.BigDecimal_100);
                item.setOffsetRate(StrUtil.format("{}%",offsetRate.stripTrailingZeros().toPlainString()));
            } else {
                SoPriceDetailDTO.ViewDTO viewDTO = priceDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), item.getSoPriceDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(viewDTO)) {
                    continue;
                }
                item.setOldTaxPrice(viewDTO.getTaxPrice());
                //升降比例
                BigDecimal offsetRate = MathUtil.divide(MathUtil.subtract(item.getTaxPrice(), viewDTO.getTaxPrice()), viewDTO.getTaxPrice()).multiply(MathUtil.BigDecimal_100);
                item.setOffsetRate(StrUtil.format("{}%",offsetRate.stripTrailingZeros().toPlainString()));
            }
        }
    }
}
