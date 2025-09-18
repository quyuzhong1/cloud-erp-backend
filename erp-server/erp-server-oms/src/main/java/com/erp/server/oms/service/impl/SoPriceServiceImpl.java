package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.oms.dto.SoPriceDTO;
import com.erp.model.oms.dto.SoPriceDetailDTO;
import com.erp.model.oms.dto.excel.ImportSoPriceExcelDTO;
import com.erp.model.oms.dto.excel.SoPriceExportExcelDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoPriceTabFlagEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.oms.listener.SoPriceExcelListener;
import com.erp.server.oms.mapper.SoPriceMapper;
import com.erp.server.oms.query.SoPriceQueryHandler;
import com.erp.server.oms.service.*;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SO_PRICE;


/**
 * <p>
 * 销售价目表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-03-24
 */
@Slf4j
@Service
public class SoPriceServiceImpl extends SuperServiceImpl<SoPriceMapper, SoPriceEntity> implements SoPriceService {

    @Autowired
    private DocNoGenHelper docNoGenHelper;

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
    private SoPriceDetailService soPriceDetailService;

    @Resource
    private SoPriceQueryHandler soPriceQueryHandler;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private CustomerInfoService customerInfoService;

    /**
     * 添加销售价目表
     *
     * @param dto
     * @return com.erp.model.scm.entity.SoPriceEntity
     * @author will
     * @date 2025-03-24 12:22
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public SoPriceEntity add(SoPriceDTO.AddDTO dto) {
        SoPriceEntity soPrice = new SoPriceEntity();
        BeanMapper.copy(dto, soPrice);
        //生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSJM);
        soPrice.setCode(code);
        String pricingUserId = dto.getPricingUserId();
        if (StringUtils.isNotBlank(pricingUserId)) {
            FindUserDTO user = sysUserFeign.getUserByUserId(pricingUserId);
            soPrice.setPricingUserName(user != null ? user.getUserName() : "");
        }
        String orgId = dto.getSoOrgId();
        //获取组织
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Collections.singletonList(orgId));
        if (CollectionUtils.isNotEmpty(orgList)) {
            soPrice.setSoOrgName(orgList.get(0).getName());
        }
        Boolean addResult = this.save(soPrice);
        //保存失败
        if (!addResult) {
           throw new ServiceException(ApiError.ERROR_1002);
        }
        Class<SoPriceEntity> credentialClass = SoPriceEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String type = tableName.value();
        //保存附件
        attachmentService.batchSave(dto.getAttachmentUrlList(), dto.getAttachmentNameList(), type, soPrice.getId());
        //添加明细
        soPriceDetailService.addPriceDetail(soPrice.getId(), dto.getSoPriceDetailList());
        //添加日志
        String content = String.format("新增了一个{%s}-销售价目-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
        addModuleOperateLog(content, ModuleTypeEnum.SO_PRICE.getCode(), soPrice.getId(), "新增操作");
        return soPrice;
    }

    /**
     * 添加日志
     *
     * @param content
     * @param code
     * @param businessId
     * @param operation
     * @return void
     * @author will
     * @date 2025-03-27 12:19
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        moduleOperateLogService.addModuleOperateLog(content, code, businessId, operation);

    }

    /**
     * 获取销售价目详情
     *
     * @param id
     * @return com.erp.model.scm.dto.SoPriceDTO.ViewDTO
     * @author will
     * @date 2025-03-27 9:11
     */
    @Override
    public SoPriceDTO.ViewDTO view(String id) {
        SoPriceEntity SoPrice = this.getById(id);
        if (Objects.isNull(SoPrice)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }
        SoPriceDTO.ViewDTO viewDTO = new SoPriceDTO.ViewDTO();
        BeanMapper.copy(SoPrice, viewDTO);
        viewDTO.setApproveStatus(SoPrice.getApproveStatus().getStatus());
        //附件信息
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessId(id);
        List<String> attachmentUrlList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
        List<String> attachmentNameList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
        viewDTO.setAttachmentNameList(attachmentNameList);
        viewDTO.setAttachmentUrlList(attachmentUrlList);
        //获取明细信息
        List<SoPriceDetailDTO.ViewDTO> SoPriceDetailList = soPriceDetailService.getBySoPriceId(id);
        List<String> skuIds = SoPriceDetailList.stream().map(SoPriceDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuNoList = plmTaskFeign.listSkuProductByIds(skuIds);
        SoPriceDetailList.forEach(req -> {
            SkuVO skuVO = skuNoList.stream().filter(obj -> obj.getSkuId().equals(req.getSkuId())).findFirst().orElse(new SkuVO());
            req.setProductName(skuVO.getSkuName());
        });

        viewDTO.setSoPriceDetailList(SoPriceDetailList);
        return viewDTO;
    }


    /**
     * 修改销售价目
     *
     * @param dto
     * @return com.erp.model.scm.entity.SoPriceEntity
     * @author will
     * @date 2025-03-27 10:52
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SoPriceEntity updateSoPrice(SoPriceDTO.UpdateDTO dto) {
        String id = dto.getId();
        SoPriceEntity soPrice = this.getById(id);
        if (Objects.isNull(soPrice)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }
        ApproveStatusEnum status = soPrice.getApproveStatus();
        SoPriceEntity old = new SoPriceEntity();
        BeanMapper.copy(soPrice, old);
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        if (!statusList.contains(status.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98019);
        }
        BeanMapper.copy(dto, soPrice);
        //编号
        String code = soPrice.getCode();
        soPrice.setCode(code);
        soPrice.setApproveStatus(status);

        String pricingUserId = dto.getPricingUserId();
        if (StringUtils.isNotBlank(pricingUserId)) {
            FindUserDTO user = sysUserFeign.getUserByUserId(pricingUserId);
            soPrice.setPricingUserName(user != null ? user.getUserName() : "");
        }
        String orgId = dto.getSoOrgId();
        //获取组织
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Collections.singletonList(orgId));
        if (CollectionUtils.isNotEmpty(orgList)) {
            soPrice.setSoOrgName(orgList.get(0).getName());
        }
        //修改成功
        boolean result = this.updateById(soPrice);
        if (!result) {
          throw new ServiceException(ApiError.ERROR_1002);
        }
        /**
         * 添加修改日志
         */
        moduleOperateLogService.addModuleOperateLogByObj(old, soPrice, ModuleTypeEnum.SO_PRICE.getCode(), id, "", "");

        Class<SoPriceEntity> credentialClass = SoPriceEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String type = tableName.value();
        attachmentService.batchSave(dto.getAttachmentUrlList(), dto.getAttachmentNameList(), type, id);
        //修改明细
        soPriceDetailService.updatePriceDetail(id, dto.getSoPriceDetailList());
        return soPrice;
    }

    /**
     * 保存并提交审核 价目
     *
     * @param dto
     * @return java.lang.Boolean
     * @author will
     * @date 2025-03-27 11:46
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public SoPriceEntity addAndSubmit(SoPriceDTO.AddDTO dto) {
        SoPriceEntity entity = this.add(dto);
        if (null == entity) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        entity = this.getById(entity.getId());
        this.submit(entity);
        return entity;
    }


    /**
     * 修改并审核销售价目
     *
     * @param dto
     * @return java.lang.Boolean
     * @author will
     * @date 2025-03-27 11:58
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public SoPriceEntity updateAndSubmit(SoPriceDTO.UpdateDTO dto) {
        SoPriceEntity entity = this.updateSoPrice(dto);
        if (null == entity) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        this.submit(entity);
        return entity;
    }


    /**
     * 批量删除销售价目信息
     *
     * @param entity
     * @return java.lang.Boolean
     * @author will
     * @date 2025-03-27 12:04
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO delete(SoPriceEntity entity) {
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        long count = Stream.of(entity).filter(p -> !p.getApproveStatus().getStatus().equals(waitSubmitStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        List<String> ids = Collections.singletonList(entity.getId());
        //删除价目表
        boolean result = this.removeByIds(ids);
        if (!result) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
        }
        //添加日志
        String content = "删除价目表[%s]";
        List<Pair<String, String>> pairList = Stream.of(entity).map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        batchAddModuleOperateLog(content, ModuleTypeEnum.SO_PRICE.getCode(), pairList, "删除");
        attachmentService.deleteByBusinessIds(ids);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }


    /**
     * 销售价目表 提交审核
     *
     * @param entity
     * @return java.lang.Boolean
     * @author will
     * @date 2025-03-27 12:11
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO submit(SoPriceEntity entity) {
        List<SoPriceEntity> list = Collections.singletonList(entity);
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();

        //审核中
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        //提交流程
        startProcess(list);

        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(waitSubmitStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(rejectStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(ingStatus));
        if (!result) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
        }
        //添加日志
        String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
        batchAddModuleOperateLog(content, ModuleTypeEnum.SO_PRICE.getCode(), pairList, "状态变更");
        //审核不通过
        String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
        batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.SO_PRICE.getCode(), rejectPairList, "状态变更");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO approve(SoPriceEntity entity, ApproveOneDTO dto) {
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus().getStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }
        //调用审核流程
        approveProcess(entity, dto);
        //添加日志
        moduleOperateLogService.addModuleOperateLog(String.format("审核【%s】了一个销售价目【%s】", ApproveTypeEnum.getName(dto.getType()), entity.getCode()).concat(StringUtils.isNotBlank(dto.getComment()) ? String.format(",意见：%s", dto.getComment()) : ""), ModuleTypeEnum.SO_PRICE.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    /**
     * 结束审核
     * @author will
     * @date 2025/3/26 10:45
     * @param dto
     * @param entity
     * @return java.lang.Boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean approveEnd(ApproveOneDTO dto, SoPriceEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        Boolean result = this.updateApproveStatus(Collections.singletonList(entity), approveStatus);
        if (Boolean.FALSE.equals(result)) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        return Boolean.TRUE;
    }


    /**
     * 取消流程
     *
     * @param entity
     * @return java.lang.Boolean
     * @author will
     * @date 2025-03-27 14:04
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO cancelProcess(SoPriceEntity entity) {
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = Stream.of(entity).filter(s -> !s.getApproveStatus().getStatus().equals(approveIngStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        List<String> ids = Collections.singletonList(entity.getId());

        //撤销现有流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ids.forEach(obj -> {
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.SO_PRICE.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);
        });

        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        Boolean result = this.updateApproveStatus(Collections.singletonList(entity), ApproveStatusEnum.getByStatus(waitSubmitStatus));
        if (!result) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
        }
        String content = String.format("状态由[%s]变更为[%s] ", ApproveStatusEnum.APPROVE_ING.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
        List<Pair<String, String>> pairList = Stream.of(entity).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        batchAddModuleOperateLog(content, ModuleTypeEnum.SO_PRICE.getCode(), pairList, "取消流程");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }


    /**
     * 销售价目表明细
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.SoPriceDTO.PagingViewDTO>
     * @author will
     * @date 2025-03-27 14:43
     */
    @Override
    public PagingVO<SoPriceDTO.PagingViewDTO> paging(PagingDTO<SoPriceDTO.PagingParamDTO> dto) {
        SoPriceDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<SoPriceDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        List<SoPriceDTO.PagingViewDTO> list = pageData.getRecords();
        handlePaging(list);
        return new PagingVO<>(pageData);
    }

    /**
     * 分页查询处理
     * @author will
     * @date 2025/3/25 11:07
     * @param list
     */
    private void handlePaging ( List<SoPriceDTO.PagingViewDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<String> skuIds = list.stream().map(SoPriceDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuNoList = plmTaskFeign.listSkuProductByIds(skuIds);
        List<String> currencyIdList = list.stream().map(SoPriceDTO.PagingViewDTO::getCurrency).collect(Collectors.toList());
        //币种信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);

        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SO_PRICE.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code,listApiResult.getMsg()));
            }
        }

        List<String> customerIdList = list.stream().map(SoPriceDTO.PagingViewDTO::getCustomerId).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerEntities = customerInfoService.listByIds(customerIdList);

        for (SoPriceDTO.PagingViewDTO item : list) {
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

            //客户
            CustomerInfoEntity customerInfoEntity = customerEntities.stream().filter(req -> item.getCustomerId().equals(req.getId())).findFirst().orElse(new CustomerInfoEntity());
            item.setCustomerName(customerInfoEntity.getName());

            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(item.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                item.setApproveUserName(curApprove);
            }
        }
    }

    /**
     * 销售价目表导出
     *
     * @param dto
     * @return void
     * @author will
     * @date 2025-03-27 17:55
     */
    @Override
    public void exportSoPrice(SoPriceDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("销售价目数据", EXPORT_SO_PRICE.getCode(), dto);
    }

    /**
     * 批量保存日志
     *
     * @param content
     * @param code
     * @param pairList
     * @param operation
     * @return void
     * @author will
     * @date 2025-03-27 12:16
     */
    private void batchAddModuleOperateLog(String content, String code, List<Pair<String, String>> pairList, String operation) {
        moduleOperateLogService.batchAddModuleOperateLog(content, code, pairList, operation);

    }


    /**
     * 修改状态
     *
     * @param list
     * @param statusEnum
     * @return java.lang.Boolean
     * @author will
     * @date 2025-03-27 12:13
     */
    private Boolean updateApproveStatus(List<SoPriceEntity> list, ApproveStatusEnum statusEnum) {
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

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        SoPriceExcelListener excelListener = new SoPriceExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), ImportSoPriceExcelDTO.class, excelListener).sheet(0).doRead();
        } catch (Exception e) {
            log.error("销售价目导入错误", e);
            return Boolean.FALSE;
        }
        //导入数据处理
        List<ImportSoPriceExcelDTO> successList = excelListener.getSuccessList();
        //导出错误数据
        List<ImportSoPriceExcelDTO> errorList = excelListener.getErrorList();
        //处理校验导入成功数据
        handleImportFile(successList, errorList);

        if (errorList.size() > 0) {
            String fileName = "销售价目导入错误信息";
            ExcelUtil.export(fileName, "导入异常", errorList, ImportSoPriceExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 导入数据处理
     * @author will
     * @date 2025/4/2 11:48
     * @param successList
     * @param errorList
     */
    private void handleImportFile (List<ImportSoPriceExcelDTO> successList, List<ImportSoPriceExcelDTO> errorList) {
        if (CollUtil.isEmpty(successList)) {
            return;
        }
        //用户信息
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        // 查询所有审核通过的产品信息
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        // 组织
        List<BaseIdDTO> orgList = sysUserFeign.listAccountingCompany();
        //客户
        List<CustomerInfoEntity> customerList = customerInfoService.list();

        Map<String, List<ImportSoPriceExcelDTO>> map = successList.stream().collect(Collectors.groupingBy(obj -> obj.getCustomerName().concat(obj.getSoOrgName())));

        for (Map.Entry<String, List<ImportSoPriceExcelDTO>> entry : map.entrySet()) {
            List<ImportSoPriceExcelDTO> value = entry.getValue();
            ImportSoPriceExcelDTO excelDTO = value.get(0);
            List<String> errorMsgList = new ArrayList<>();

            SoPriceDTO.AddDTO addDTO = new SoPriceDTO.AddDTO();
            String customerName = StrUtils.null2EmptyWithTrim(excelDTO.getCustomerName());
            //客户信息
            CustomerInfoEntity customerInfoEntity = customerList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), customerName)).findFirst().orElse(null);
            if(Objects.isNull(customerInfoEntity)) {
                errorMsgList.add("客户不存在");
            } else {
                addDTO.setCustomerId(customerInfoEntity.getId());
            }
            // 报价日期
            String quotedDateStr = excelDTO.getQuotedDate();
            if(StrUtils.isNotEmpty(quotedDateStr)) {
                addDTO.setQuotedDate(LocalDateUtil.stringToLocalDateTime(quotedDateStr).toLocalDate());
            }
            // 定价员
            String pricingUserName = excelDTO.getPricingUserName();
            String userId = userList.stream().filter(r -> Objects.equals(pricingUserName, r.getUserName())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getUserId())).orElse("");
            if (StrUtils.isEmpty(userId)) {
                errorMsgList.add("定价员不存在");
            }
            addDTO.setPricingUserId(userId);
            // 销售组织
            String soOrgName = excelDTO.getSoOrgName();
            String orgId = orgList.stream().filter(r -> Objects.equals(soOrgName, r.getName())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            if (StrUtils.isEmpty(orgId)) {
                errorMsgList.add("销售组织不存在");
            }
            addDTO.setSoOrgId(orgId);
            // 币制代码
            addDTO.setCurrency("CNY");

            List<SoPriceDetailDTO.AddDTO> detailList = Lists.newArrayList();
            for (ImportSoPriceExcelDTO importExcelDTO : value) {
                SoPriceDetailDTO.AddDTO detailDTO = new  SoPriceDetailDTO.AddDTO();
                // 明细
                String skuNo = importExcelDTO.getSkuNo();
                SkuVO skuEntity = skuList.stream().filter(obj -> obj.getSkuNo().equals(skuNo)).findFirst().orElse(null);
                if (Objects.isNull(skuEntity)) {
                    errorMsgList.add("sku编码错误");
                } else {
                    detailDTO.setSkuId(skuEntity.getSkuId());
                    detailDTO.setSkuNo(skuNo);
                }
                // 区间从
                String minQtyStr = importExcelDTO.getMinQty();
                if(StrUtils.isNotEmpty(minQtyStr)) {
                    detailDTO.setMinQty(Integer.valueOf(minQtyStr));
                }
                // 区间到
                String maxQtyStr = importExcelDTO.getMaxQty();
                if(StrUtils.isNotEmpty(maxQtyStr)) {
                    detailDTO.setMaxQty(Integer.valueOf(maxQtyStr));
                }
                detailDTO.setTaxPrice(MathUtil.valueOf(importExcelDTO.getTaxPrice()));
                if(detailDTO.getTaxPrice().compareTo(BigDecimal.ZERO) < 1) {
                    errorMsgList.add("含税单价错误不能小于等于0");
                }
                detailDTO.setTaxRate(MathUtil.valueOf(importExcelDTO.getTaxRate()));
                if(detailDTO.getTaxRate().compareTo(BigDecimal.ZERO) < 0) {
                    errorMsgList.add("税率错误不能小于0");
                }
                // 生效时间
                String effectiveDateStr = importExcelDTO.getEffectiveDate();
                if(StrUtils.isNotEmpty(effectiveDateStr)) {
                    detailDTO.setEffectiveDate(LocalDateUtil.stringToLocalDateTime(effectiveDateStr).toLocalDate());
                }
                // 失效时间
                String expireDateStr = importExcelDTO.getExpireDate();
                if(StrUtils.isNotEmpty(expireDateStr)) {
                    detailDTO.setExpireDate(LocalDateUtil.stringToLocalDateTime(expireDateStr).toLocalDate());
                }
                if (ObjectUtil.isNotEmpty(detailDTO.getEffectiveDate()) && ObjectUtil.isNotEmpty(detailDTO.getExpireDate())) {
                    if (detailDTO.getEffectiveDate().isAfter(detailDTO.getExpireDate())) {
                        errorMsgList.add("生效日期不能大于失效日期");
                    }
                }
                // 验证 区间从和区间到
                if(Objects.nonNull(detailDTO.getMaxQty()) && Objects.nonNull(detailDTO.getMinQty())
                        && detailDTO.getMaxQty().intValue() == detailDTO.getMinQty().intValue()) {
                    errorMsgList.add("区间从，区间到两个值不能相同");
                }
                if(Objects.nonNull(detailDTO.getMaxQty()) && Objects.nonNull(detailDTO.getMinQty())
                        && detailDTO.getMaxQty() < detailDTO.getMinQty()) {
                    errorMsgList.add("区间从值不能大于区间到值");
                }
                if(CollUtil.isNotEmpty(errorMsgList)) {
                    importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(importExcelDTO);
                    continue;
                }
                detailList.add(detailDTO);
            }
            if (CollUtil.isEmpty(detailList)) {
                continue;
            }
            addDTO.setSoPriceDetailList(detailList);
            try {
                ApplicationContextUtils.getBean(SoPriceServiceImpl.class).add(addDTO);
            } catch (Exception e) {
                value.forEach(obj -> obj.setErrorMsg("1、" + e.getMessage()));
                errorList.addAll(value);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchImport(List<SoPriceDTO.ImportAddDTO> handList) {
        if(CollUtil.isEmpty(handList)) {
            return;
        }
        // 新增的销售价目信息
        List<SoPriceEntity> addList = Lists.newArrayList();
        // 新增销售价目明细信息
        List<SoPriceDetailEntity> addDetailList = Lists.newArrayList();
        // 修改的销售价目明细信息
        List<SoPriceDetailEntity> updateDetailList = Lists.newArrayList();
        for(SoPriceDTO.ImportAddDTO item : handList) {
            SoPriceEntity soPriceEntity = new SoPriceEntity();
            soPriceEntity.setCustomerId(item.getCustomerId());
            soPriceEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
            soPriceEntity.setQuotedDate(item.getQuotedDate());
            soPriceEntity.setPricingUserId(item.getPricingUserId());
            soPriceEntity.setPricingUserName(item.getPricingUserName());
            soPriceEntity.setSoOrgId(item.getSoOrgId());
            soPriceEntity.setSoOrgName(item.getSoOrgName());
            soPriceEntity.setCurrency(item.getCurrency());
            // 明细信息
            List<SoPriceDetailDTO.ImportSaveDTO> detailList = item.getDetailList();
            List<SoPriceDetailEntity> addItemList = Lists.newArrayList();
            List<SoPriceDetailEntity> updateItemList = Lists.newArrayList();
            // 此处需要过滤掉修改的明细
            for(SoPriceDetailDTO.ImportSaveDTO detailItem : detailList) {
                LocalDate expireDate = null;
                if(Objects.nonNull(detailItem.getEffectiveDate())) {
                    expireDate = detailItem.getEffectiveDate().plusDays(100);
                }
                BigDecimal taxRate = null;
                if(Objects.nonNull(detailItem.getTaxRate())) {
                    taxRate = detailItem.getTaxRate().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                }
                if(CollUtil.isNotEmpty(detailItem.getIds())) {
                    for(String detailId : detailItem.getIds()) {
                        SoPriceDetailEntity saveSoPriceDetailEntity = new SoPriceDetailEntity();
                        BeanMapper.copy(detailItem, saveSoPriceDetailEntity);
                        saveSoPriceDetailEntity.setExpireDate(expireDate);
                        saveSoPriceDetailEntity.setTaxRate(taxRate);
                        saveSoPriceDetailEntity.setId(detailId);
                        if (StringUtils.isBlank(saveSoPriceDetailEntity.getCurrency())){
                            saveSoPriceDetailEntity.setCurrency(item.getCurrency());
                        }
                        updateItemList.add(saveSoPriceDetailEntity);
                    }
                } else {
                    SoPriceDetailEntity saveSoPriceDetailEntity = new SoPriceDetailEntity();
                    BeanMapper.copy(detailItem, saveSoPriceDetailEntity);
                    saveSoPriceDetailEntity.setExpireDate(expireDate);
                    saveSoPriceDetailEntity.setTaxRate(taxRate);
                    saveSoPriceDetailEntity.setCurrency(item.getCurrency());
                    addItemList.add(saveSoPriceDetailEntity);
                }
            }
            // 当该新增的主单有明细时才新增
            if(CollUtil.isNotEmpty(addItemList)) {
                //生成单号
                String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CGJM);
                soPriceEntity.setCode(code);
                String id = IdWorker.getIdStr();
                soPriceEntity.setId(id);
                addList.add(soPriceEntity);
                addItemList.forEach(data->data.setMainId(id));
                addDetailList.addAll(addItemList);
            }
            if(CollUtil.isNotEmpty(updateItemList)) {
                updateDetailList.addAll(updateItemList);
            }
        }
        // 保存主单
        if(CollUtil.isNotEmpty(addList)) {
            super.saveBatch(addList);
            List<Pair<String, String>> pairList = addList.stream().
                    map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            String content = "导入销售价目信息[%s]";
            batchAddModuleOperateLog(content, ModuleTypeEnum.SO_PRICE.getCode(), pairList, "新增操作");
        }
        // 保存销售价目明细信息
        if(CollUtil.isNotEmpty(addDetailList)) {
            soPriceDetailService.saveBatch(addDetailList);
        }
        if(CollUtil.isNotEmpty(updateDetailList)) {
            List<String> detailIds = updateDetailList.stream().map(SoPriceDetailEntity::getId).distinct().collect(Collectors.toList());
            List<SoPriceDetailEntity> detailList = soPriceDetailService.listByIds(detailIds);
            for(SoPriceDetailEntity updateDetail : updateDetailList) {
                SoPriceDetailEntity old = detailList.stream().filter(r -> Objects.equals(r.getId(), updateDetail.getId())).findFirst().orElse(null);
                soPriceDetailService.updateDetail(updateDetail, old);
            }
        }
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/SoPriceTemplate.xlsx";
        String excelName = "SoPriceTemplate.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.DEFAULT);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO disApprove(SoPriceEntity entity,  List<SoPriceChangeDetailEntity> changeDetailEntityList) {
        if (!Objects.equals(ApproveStatusEnum.APPROVE, entity.getApproveStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98014.msg);
        }
        if (CollectionUtils.isNotEmpty(changeDetailEntityList)) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),String.format(ApiError.ERROR_NOT_DISAPPROVE_CHANGE.msg, entity.getCode()));
        }
        Boolean result = this.updateApproveStatus(Collections.singletonList(entity), ApproveStatusEnum.WAIT_SUBMIT);
        if (result) {
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            moduleOperateLogService.addModuleOperateLog(content, ModuleTypeEnum.SO_PRICE.getCode(), entity.getId(), "状态变更");
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    public Boolean updateDetailRemark(List<String> ids, String remark) {
        if (CollectionUtils.isEmpty(ids)) {
            return Boolean.TRUE;
        }
        soPriceDetailService.updateDetailRemark(ids,remark);
        return Boolean.TRUE;
    }

    @Override
    public List<SoPriceDTO.TabListDTO> tabList(PermissionsDTO dto) {
        SoPriceTabFlagEnum[] values = SoPriceTabFlagEnum.values();
        List<SoPriceDTO.TabListDTO> list = new ArrayList<>();
        for (SoPriceTabFlagEnum item : values) {
            SoPriceDTO.PagingParamDTO searchParamDTO = new SoPriceDTO.PagingParamDTO();
            searchParamDTO.setPermissionSql(dto.getPermissionSql());
            SoPriceDTO.TabListDTO resultDTO = new SoPriceDTO.TabListDTO();
            String tabSql = soPriceQueryHandler.getTabSql(item.getCode());
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
    public PagingVO<SoPriceExportExcelDTO> exportSoPrice(PagingDTO<SoPriceDTO.PagingParamDTO> dto) {
        //获取导出数据
        PagingVO<SoPriceDTO.PagingViewDTO> page = this.paging(dto);
        if (CollUtil.isEmpty( page.getList())) {
            throw new ServiceException("导出数据为空");
        }
        List<SoPriceExportExcelDTO> resultList = new ArrayList<>();
        for (SoPriceDTO.PagingViewDTO item : page.getList()) {
            SoPriceExportExcelDTO excelDTO = new SoPriceExportExcelDTO();
            BeanMapper.copy(item, excelDTO);
            Integer minQty = item.getMinQty();
            Integer maxQty = item.getMaxQty();
            excelDTO.setQtySection(minQty + "-" + maxQty);

            Boolean disabled = item.getDisabled();
            excelDTO.setEnabled((disabled != null && disabled) ? "停用" : "启用");
            //含税单价
            BigDecimal taxPrice = item.getTaxPrice();
            //币种
            excelDTO.setTaxPrice(item.getCurrencySymbol() + taxPrice.toString());
            excelDTO.setApproveTime(item.getApproveTime());
            resultList.add(excelDTO);
        }
        return new PagingVO<>(resultList, (int) page.getTotalPage(), dto.getPageSize(), dto.getCurrPage());
    }

    /**
     * 设置客户和组织
     * @author will
     * @date 2025/3/27 11:55
     * @param list
     */
    private void setCustomerAndOrgByShopId (List<SoPriceDTO.PriceParamDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<String> shopIdList = list.stream().map(SoPriceDTO.PriceParamDTO::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopList = CollUtil.isEmpty(shopIdList) ? Collections.emptyList() : FeignQuery.getByIds(ShopInfoEntity.class, shopIdList);
        if (CollUtil.isEmpty(shopList)) {
            return;
        }
        for (SoPriceDTO.PriceParamDTO priceParam : list) {
            ShopInfoEntity shopInfoEntity = shopList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), priceParam.getShopId())).findFirst().orElse(new ShopInfoEntity());
            priceParam.setCustomerId(shopInfoEntity.getCustomerId());
            priceParam.setSoOrgId(shopInfoEntity.getSalesOrgId());
        }
    }

    @Override
    public List<SoPriceDTO.PriceDTO> batchGetSoPrice(List<SoPriceDTO.PriceParamDTO> list) {

        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        //根据店铺取客户和销售组织
        setCustomerAndOrgByShopId(list);

        //销售组织Id
        List<String> soOrgIdList = list.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getSoOrgId())).map(SoPriceDTO.PriceParamDTO::getSoOrgId).distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> companyList = sysUserFeign.getAccountingCompanyList(soOrgIdList);
        if (CollectionUtils.isEmpty(companyList)){
            return Collections.emptyList();
        }
        //skuId
        List<String> skuIdList = list.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getSkuId())).map(SoPriceDTO.PriceParamDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
        if (CollectionUtils.isEmpty(skuVOList)) {
            return Collections.emptyList();
        }
        //客户Id
        List<String> customerIdList = list.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getCustomerId())).map(SoPriceDTO.PriceParamDTO::getCustomerId).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> supplierEntityList = customerInfoService.listByIds(customerIdList);
        if (CollectionUtils.isEmpty(supplierEntityList)) {
            return Collections.emptyList();
        }
        //采购数量-需要根据sku进行汇总
        Map<String, Integer> skuQtyList = list.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getSkuId())
                        && StrUtil.isNotBlank(e.getCustomerId()) && Objects.nonNull(e.getQty()))
                .collect(Collectors.groupingBy(e -> e.getSkuId() + "_" + e.getCustomerId(), Collectors.summingInt(SoPriceDTO.PriceParamDTO::getQty)));
        List<Integer> soQtyList = skuQtyList.values().stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(soQtyList)) {
            return Collections.emptyList();
        }
        //查询对应采购价目
        List<SoPriceDetailDTO.SoTaxPriceBatchViewDTO> viewList = soPriceDetailService.batchGetTaxPrice(skuIdList,customerIdList,soQtyList,soOrgIdList);
        if (CollectionUtils.isEmpty(viewList)){
            //未查到结果，直接返回
            return Collections.emptyList();
        }
        List<SoPriceDTO.PriceDTO> result = new ArrayList<>();
        for(SoPriceDTO.PriceParamDTO priceParamDTO : list){
            SoPriceDTO.PriceDTO priceDTO = new SoPriceDTO.PriceDTO();
            BeanUtil.copyProperties(priceParamDTO,priceDTO);
            if (StrUtil.isBlank(priceParamDTO.getSoOrgId()) || StrUtil.isBlank(priceParamDTO.getSkuId()) || StrUtil.isBlank(priceParamDTO.getCustomerId()) || Objects.isNull(priceParamDTO.getQty())){
                result.add(priceDTO);
                continue;
            }
            //获取sku汇总数量
            Integer purchaseQty = skuQtyList.getOrDefault(priceParamDTO.getSkuId() + "_" + priceParamDTO.getCustomerId(), null);
            SoPriceDetailDTO.SoTaxPriceBatchViewDTO viewDTO = viewList.stream().filter(obj -> StrUtil.isNotBlank(priceParamDTO.getSkuId())
                            && StrUtil.isNotBlank(obj.getSkuId()) && obj.getSkuId().equals(priceParamDTO.getSkuId())
                            && StrUtil.isNotBlank(obj.getCustomerId()) && StrUtil.isNotBlank(priceParamDTO.getCustomerId()) && obj.getCustomerId().equals(priceParamDTO.getCustomerId())
                            && StrUtil.isNotBlank(obj.getSoOrgId()) && StrUtil.isNotBlank(priceParamDTO.getSoOrgId()) && StrUtil.equals(obj.getSoOrgId(),priceParamDTO.getSoOrgId())
                            && ((obj.getEffectiveDate().equals(priceParamDTO.getDate()) || obj.getEffectiveDate().isBefore(priceParamDTO.getDate())) && (obj.getExpireDate().equals(priceParamDTO.getDate()) || obj.getExpireDate().isAfter(priceParamDTO.getDate())))
                            && Objects.nonNull(purchaseQty) && (purchaseQty >= obj.getMinQty() && obj.getMaxQty() > purchaseQty))
                    .findFirst().orElse(new SoPriceDetailDTO.SoTaxPriceBatchViewDTO());
            priceDTO.setTaxPrice(viewDTO.getTaxPrice());
            priceDTO.setTaxRate(viewDTO.getTaxRate());
            priceDTO.setCurrency(viewDTO.getCurrency());
            priceDTO.setCurrencySymbol(CurrencyEnum.getSymbolByCode(viewDTO.getCurrency()));
            priceDTO.setAmount(MathUtil.multiplyWithTwo(viewDTO.getTaxPrice(), priceParamDTO.getQty()).setScale(4, RoundingMode.DOWN).stripTrailingZeros().toPlainString());
            result.add(priceDTO);
        }
        return result;
    }

    /**
     * @description: 提交流程
     * @author Will
     * @date: 2023/7/3 14:39
     * @param list
     */
    private void startProcess (List<SoPriceEntity> list) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ValidList<ProcessManagementDTO.StartDTO> resultList = new ValidList<>();
        list.forEach(obj -> {
            ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
            startDTO.setBusinessId(obj.getId());
            startDTO.setBusinessCode(obj.getCode());
            startDTO.setBusinessKey(SourceTypeEnum.SO_PRICE.getCode());
            startDTO.setBusinessName(obj.getCode());
            startDTO.setUserId(userInfo.getUid());
            startDTO.setVariablesMap(getVariablesMap(obj));
            resultList.add(startDTO);
        });
        ApiResult<List<ProcessManagementDTO.StartResultDTO>> listApiResult = workflowFeign.batchStartProcess(resultList);
        if (!listApiResult.isSuccess()) {
            throw new ServiceException(listApiResult.getMsg());
        }
    }

    /**
     * @description: 流程审核
     * @author Will
     * @date: 2023/7/3 15:24
     * @param entity
     * @param dto
     */
    private void approveProcess (SoPriceEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SO_PRICE.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
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
    private Map<String,Object> getVariablesMap(SoPriceEntity entity) {
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        List<SoPriceDetailEntity> detailList = soPriceDetailService.listDetailByMainId(entity.getId());
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        variablesMap.put(ThirdConstants.DETAIL_LIST, BeanUtil.copyToList(detailList,Map.class));
        return variablesMap;
    }
}
