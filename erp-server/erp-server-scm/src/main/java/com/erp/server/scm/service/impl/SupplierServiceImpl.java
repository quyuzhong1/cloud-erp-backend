package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.ApproveType;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.scm.dto.SupplierAccountDTO;
import com.erp.model.scm.dto.SupplierContactDTO;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.dto.excel.SupplierExportExcelDTO;
import com.erp.model.scm.dto.excel.SupplierImportExcelDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.DictBasicEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.SupplierPhaseEnum;
import com.erp.model.srm.vo.SupplierConfigVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.enums.SysDictBasicEnum;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.srm.feign.SrmCfgSettingFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.scm.kingdee.SyncKingdeeSupplierService;
import com.erp.server.scm.listener.SupplierExcelListener;
import com.erp.server.scm.mapper.SupplierMapper;
import com.erp.server.scm.service.*;
import com.google.common.collect.Maps;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 供应商表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
@Slf4j
public class SupplierServiceImpl extends SuperServiceImpl<SupplierMapper, SupplierEntity> implements SupplierService {


    @Resource
    private SupplierAccountService supplierAccountService;

    @Resource
    private SupplierContactService supplierContactService;

    @Resource
    private SupplierCredentialService supplierCredentialService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SupplierGradeService supplierGradeService;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;

    //采购价目表
    @Resource
    private PurchasePriceService purchasePriceService;

    @Resource
    private SyncKingdeeSupplierService syncKingdeeSupplierService;

    @Autowired
    private SysDictFeign sysDictFeign;

    @Autowired
    private CommonService commonService;

    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private LogisticsFeign logisticsFeign;

    @Resource
    private SrmCfgSettingFeign srmCfgSettingFeign;

    @Resource
    private SupplierRefUserService supplierRefUserService;

    /**
     * 保存供应商信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-17 15:12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addSupplier(SupplierDTO.AddDTO dto) {
        checkName(null, dto.getName());
        //供应商联系信息
        List<SupplierContactDTO.AddDTO> contactList = dto.getContactList();
        //检查联系人默认是否多个
        supplierContactService.checkIsDefault(contactList);
        //供应商资质信息
        List<SupplierCredentialDTO.AddDTO> credentialList = dto.getCredentialList();
        //检查资质日期
        supplierCredentialService.checkDate(credentialList);
        //验证付款条件是否正确
        if (StrUtils.isNotEmpty(dto.getPaymentCondition())) {
            List<DictBasicDTO.ViewDTO> paymentConditionList = sysDictFeign.getByType(SysDictBasicEnum.PAYMENT_CONDITION.getCode());
            List<String> paymentConditionCodes = paymentConditionList.stream().map(DictBasicDTO.ViewDTO::getValue).distinct().collect(Collectors.toList());
            if (!paymentConditionCodes.contains(dto.getPaymentCondition())) {
                throw new ServiceException("付款条件错误");
            }
        }
        //供应商id
        String supplierId = IdWorker.getIdStr();
        SupplierEntity addEntity = new SupplierEntity();
        BeanMapper.copy(dto, addEntity);

        List<String> keyList = new ArrayList<>(1);
        keyList.add(DictBasicEnum.SUPPLIER_CATEGORY.getType());
        //根据 key list 获取到对应数据
        List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(keyList);
        String categoryId = dto.getCategoryId();
        String categoryName = dictBasicList.stream().filter(d -> d.getId().equals(categoryId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");

        //获取供应商等级
        List<SupplierGradeEntity> supplierGradeList = supplierGradeService.list();
        String gradeId = dto.getGradeId();
        String gradeName = supplierGradeList.stream().filter(d -> d.getId().equals(gradeId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        addEntity.setGradeName(gradeName);

        addEntity.setId(supplierId);
        addEntity.setCategoryName(categoryName);
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.GYS, BusinessNoTypeEnum.CODE_GYS.getCode()));
        addEntity.setCode(code);
        String purchaseUserId = dto.getPurchaseUserId();
        if (StringUtils.isNotBlank(purchaseUserId)) {
            FindUserDTO user = sysUserFeign.getUserByUserId(purchaseUserId);
            addEntity.setPurchaseUserName(user != null ? user.getUserName() : "");
        }
        Boolean result = this.save(addEntity);
        //保存成功
        if (result) {
            //供应商账号信息
            List<SupplierAccountDTO.AddDTO> bankAccountList = dto.getBankAccountList();
            supplierAccountService.saveBatchBankAccount(supplierId, bankAccountList);
            //供应商联系人信息
            supplierContactService.saveBatchContact(supplierId, contactList);

            supplierCredentialService.saveBatchCredential(supplierId, credentialList);
            //添加日志
            String content = String.format("新增了一个{%s}-供应商信息-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), supplierId, "新增操作");
            return supplierId;
        }

        return "";
    }

    /**
     * 保存并提交审核供应商
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 9:13
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(SupplierDTO.AddDTO dto) {
        String supplierId = this.addSupplier(dto);
        if (StringUtils.isBlank(supplierId)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Arrays.asList(supplierId));
        return result;
    }


    /**
     * 供应商详情
     *
     * @param supplierId
     * @return com.erp.model.scm.dto.SupplierDTO.updateDTO
     * @author yl
     * @date 2023-03-20 10:00
     */
    @Override
    public SupplierDTO.SupplierViewDTO view(String supplierId) {
        SupplierDTO.SupplierViewDTO result = new SupplierDTO.SupplierViewDTO();
        SupplierEntity supplier = this.getById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        BeanMapper.copy(supplier, result);
        result.setApproveStatus(supplier.getApproveStatus().getStatus());
        result.setPhase(supplier.getPhase().getPhase());
        //付款条件
        List<DictBasicDTO.ViewDTO> paymentConditionList = sysDictFeign.getByType(SysDictBasicEnum.PAYMENT_CONDITION.getCode());
        if (StrUtils.isNotEmpty(result.getPaymentCondition())) {
            DictBasicDTO.ViewDTO dict = paymentConditionList.stream().filter(r -> Objects.equals(r.getValue(), result.getPaymentCondition())).findFirst().orElse(null);
            if (Objects.nonNull(dict)) {
                result.setPaymentConditionName(dict.getName());
            }
        }
        //根据供应商id 查询 联系人信息
        List<SupplierContactDTO.UpdateDTO> contactList = supplierContactService.listBySupplierId(supplierId);
        result.setContactList(contactList);

        //根据供应商id 查询账户信息
        List<SupplierAccountDTO.UpdateDTO> bankAccountList = supplierAccountService.getBySupplierId(supplierId);
        result.setBankAccountList(bankAccountList);
        //根据供应商id 获取资质信息
        List<SupplierCredentialDTO.UpdateDTO> credentialList = supplierCredentialService.getBySupplierId(supplierId);
        result.setCredentialList(credentialList);
        return result;
    }


    /**
     * 修改供应商信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 10:56
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateSupplier(SupplierDTO.UpdateDTO dto) {
        //供应商id
        String supplierId = dto.getId();
        SupplierEntity supplier = this.getById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }

        //验证付款条件是否正确
        if (StrUtils.isNotEmpty(dto.getPaymentCondition())) {
            List<DictBasicDTO.ViewDTO> paymentConditionList = sysDictFeign.getByType(SysDictBasicEnum.PAYMENT_CONDITION.getCode());
            List<String> paymentConditionCodes = paymentConditionList.stream().map(DictBasicDTO.ViewDTO::getValue).distinct().collect(Collectors.toList());
            if (!paymentConditionCodes.contains(dto.getPaymentCondition())) {
                throw new ServiceException("付款条件错误");
            }
        }

        //旧的
        SupplierEntity old = new SupplierEntity();
        BeanMapper.copy(supplier, old);

        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        if (!statusList.contains(supplier.getApproveStatus().getStatus())) {
            throw new ServiceException(ApiError.ERROR_98019);
        }
        //资质信息
        List<SupplierCredentialDTO.UpdateDTO> credentialList = dto.getCredentialList();
        List<SupplierCredentialDTO.AddDTO> credentialAddList = BeanMapper.copyList(credentialList, SupplierCredentialDTO.AddDTO.class);
        supplierCredentialService.checkDate(credentialAddList);
        String code = supplier.getCode();
        //检查供应商名称
        checkName(supplierId, dto.getName());
        //联系人信息
        List<SupplierContactDTO.UpdateDTO> contactList = dto.getContactList();
        long count = contactList.stream().filter(c -> c.getIsDefault() != null && c.getIsDefault()).count();
        if (count > 1) {
            throw new ServiceException(ApiError.ERROR_98003);
        }
        BeanMapper.copy(dto, supplier);

        String purchaseUserId = dto.getPurchaseUserId();
        if (StringUtils.isNotBlank(purchaseUserId)) {
            FindUserDTO user = sysUserFeign.getUserByUserId(purchaseUserId);
            supplier.setPurchaseUserName(user != null ? user.getUserName() : "");
        }
        List<String> keyList = new ArrayList<>(1);
        keyList.add(DictBasicEnum.SUPPLIER_CATEGORY.getType());
        //根据 key list 获取到对应数据
        List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(keyList);
        String categoryId = dto.getCategoryId();
        String categoryName = dictBasicList.stream().filter(d -> d.getId().equals(categoryId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        supplier.setCategoryName(categoryName);
        supplier.setCode(code);


        //获取供应商等级
        List<SupplierGradeEntity> supplierGradeList = supplierGradeService.list();
        String gradeId = dto.getGradeId();
        String gradeName = supplierGradeList.stream().filter(d -> d.getId().equals(gradeId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        supplier.setGradeName(gradeName);

        Boolean result = this.updateById(supplier);
        //修改成功
        if (result) {
            /**
             * 添加修改日志
             */
            moduleOperateLogService.addModuleOperateLogByObj(old, supplier, ModuleTypeEnum.SUPPLIER.getCode(), supplierId, "", "");

            //联系人的
            supplierContactService.updateSupplierContact(contactList, supplierId);
            //账户信息
            List<SupplierAccountDTO.UpdateDTO> bankAccountList = dto.getBankAccountList();
            supplierAccountService.updateAccount(bankAccountList, supplierId);
            //资质的
            supplierCredentialService.updateCredential(credentialList, supplierId);
            return supplierId;
        }

        return "";
    }


    /**
     * 分页获取供应商信息
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.SupplierDTO.PagingViewDTO>
     * @author yl
     * @date 2023-03-20 14:11
     */
    @Override
    public PagingVO<SupplierDTO.PagingViewDTO> paging(PagingDTO<SupplierDTO.PagingParamDTO> dto) {
        SupplierDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<SupplierDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
        }
        List<String> keyList = new ArrayList<>(3);
        keyList.add(DictBasicEnum.SUPPLIER_ACCOUNT_PAYMENT.getType());
        keyList.add(DictBasicEnum.SUPPLIER_PAY_MODE.getType());
        keyList.add(DictBasicEnum.SUPPLIER_CATEGORY.getType());
        //获取供应商等级
        List<SupplierGradeEntity> supplierGradeList = supplierGradeService.list();
        //根据 key list 获取到对应数据
        List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(keyList);
        //供应商id 集合
        List<String> supplierIdList = list.stream().map(SupplierDTO.PagingViewDTO::getId).collect(Collectors.toList());
        //获取供应商默认联系人信息
        List<SupplierContactEntity> contactList = supplierContactService.getDefaultBySupplierIdList(supplierIdList);

        //获取到采购订单数据
        List<PurchaseOrderSupplierEntity> orderSupplierList = purchaseOrderSupplierService.getBySupplierIds(supplierIdList);
        //付款条件
        List<DictBasicDTO.ViewDTO> paymentConditionList = sysDictFeign.getByType(SysDictBasicEnum.PAYMENT_CONDITION.getCode());


        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SUPPLIER.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.Default.code, listApiResult.getMsg()));
            }
        }
        //TODO 获取srm 供应商订单规则
        List<SupplierConfigVO> configs = srmCfgSettingFeign.getConfigList(supplierIdList);
        Map<String, SupplierConfigVO> configVOMap = configs.stream().collect(Collectors.toMap(SupplierConfigVO::getSupplierId, Function.identity()));
        for (SupplierDTO.PagingViewDTO item : list) {
            String id = item.getId();
            //等级id
            String gradeId = item.getGradeId();
            String gradeName = supplierGradeList.stream().filter(g -> g.getId().equals(gradeId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setGradeName(gradeName);
            //分类id
            String categoryId = item.getCategoryId();
            String categoryName = dictBasicList.stream().filter(d -> d.getId().equals(categoryId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setCategoryName(categoryName);
            //结算方式
            String payMethodId = item.getPayMethodId();
            String payMethodName = dictBasicList.stream().filter(d -> d.getId().equals(payMethodId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setPayMethodName(payMethodName);
            //付款条件
            String paymentConditionName = paymentConditionList.stream().filter(obj -> obj.getValue().equals(item.getPaymentCondition())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setPaymentConditionName(paymentConditionName);

            ApproveStatusEnum statusEnum = item.getApproveStatus();
            item.setApproveStatusName(statusEnum.getName());
            SupplierPhaseEnum phaseEnum = item.getPhase();
            item.setPhaseName(phaseEnum.getName());
            item.setApproveStatusCode(statusEnum.getStatus());
            item.setPhaseCode(phaseEnum.getPhase());
            SupplierContactEntity contact = contactList.stream().filter(c -> c.getSupplierId().equals(id)).findFirst().orElse(null);
            if (contact != null) {
                item.setContactPerson(contact.getPerson());
                item.setContactTelNumber(contact.getTelNumber());
            }
            //采购次数
            long purchasesCount = orderSupplierList.stream().filter(o -> o.getSupplierId().equals(id)).count();
            item.setPurchasesCount((int) purchasesCount);
            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(item.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                item.setApproveUserName(curApprove);
            }
            SupplierConfigVO configVO = configVOMap.get(item.getId());
            if (Objects.nonNull(configVO)){
                item.setOrderAcceptRule(configVO.getOrderAcceptRule());
                item.setReturnConfirmRule(configVO.getReturnConfirmRule());
            }
        }

        return new PagingVO(pageData);
    }


    /**
     * 根据表id集合删除 数据
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 18:38
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        List<SupplierEntity> supplierList = this.listByIds(ids);
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        long count = supplierList.stream().filter(s -> !s.getApproveStatus().getStatus().equals(waitSubmitStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        //检查是否关联供应商 如果有就不能删除
        purchasePriceService.checkIsRefSupplier(ids);
        //检查采购订单是否有关联到供应商id  如果有就不能删除
        purchaseOrderSupplierService.checkIsRefSupplier(ids);
        //删除供应商
        Boolean result = this.removeByIds(ids);
        if (result) {
            //根据 供应商id 删除联系人信息
            supplierContactService.removeBySupplierIds(ids);

            //根据 供应商id 删除账户信息
            supplierAccountService.removeBySupplierIds(ids);

            //根据 供应商id 删除资质信息
            supplierCredentialService.removeBySupplierIds(ids);


            //添加日志
            String content = "删除供应商[%s]";
            List<Pair<String, String>> pairList = supplierList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());

            batchAddModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), pairList, "删除");
            //删除同步金蝶
            supplierList.forEach(obj -> syncKingdeeSupplierService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_DELETE.getCode()));
        }


        return result;
    }


    /**
     * 批量提交审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 19:07
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<SupplierEntity> list = this.getByIds(ids);
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
        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().getStatus().equals(waitSubmitStatus)).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(rejectStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(ingStatus));
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            batchAddModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), pairList, "状态变更");
            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.SUPPLIER.getCode(), rejectPairList, "状态变更");
        }
        return result;
    }


    /**
     * 审核 供应商
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 19:41
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean approve(BaseApproveParamDTO dto) {
        List<String> supplierIds = dto.getIds();
        List<SupplierEntity> list = this.getByIds(supplierIds);
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !ingStatus.equals(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //调用审核流程
        approveProcess(list, dto);

        //添加日志
        List<Pair<String, String>> pairList = list.stream().
                map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        batchAddModuleOperateLog(String.format("审核【%s】了一个供应商信息", ApproveTypeEnum.getName(dto.getType())).concat("【%s】").concat(StringUtils.isNotBlank(dto.getComment()) ? String.format(",意见：%s", dto.getComment()) : ""), ModuleTypeEnum.SUPPLIER.getCode(), pairList, "审核操作");
        return Boolean.TRUE;
    }

    /**
     * @param dto
     * @param list
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/3 15:25
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean approveEnd(BaseApproveParamDTO dto, List<SupplierEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus;
        if (dto.getType().equals(ApproveType.PASS)) {
            //审核通过
            approveStatus = ApproveStatusEnum.APPROVE;
        } else {
            //审核不通过
            approveStatus = ApproveStatusEnum.REJECT;
        }
        Boolean result = this.updateApproveStatus(list, approveStatus);
        if (!result) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        if (dto.getType().equals(ApproveType.PASS)) {
            //审核通过发送金蝶
            list.forEach(obj -> syncKingdeeSupplierService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_APPROVE.getCode()));
        }
        return Boolean.TRUE;
    }


    /**
     * 更改供应商更改状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-21 8:56
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean updateStatus(UpdateStateDTO dto) {
        String supplierId = dto.getId();
        SupplierEntity supplier = this.getById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        Boolean state = dto.getState();
        //表示禁用
        if (state) {
            List<BaseIdDTO.CodeDTO> logisticsChannelList = logisticsFeign.listBySupplierId(supplierId);
            long count = logisticsChannelList.stream().filter(c -> !c.getDisabled()).count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_DISABLED_EXIST);
            }

        }
        supplier.setDisabled(state);

        //添加日志
        String content = String.format("编辑了供应商[%s] 启用状态 有[%s] 变更为[%s]", supplier.getName(), dto.getState() == true ? "启用" : "停用", dto.getState() == true ? "停用" : "启用");
        addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), supplierId, "修改操作");

        //发送金蝶
        if (dto.getState()) {
            syncKingdeeSupplierService.syncDataToKingdee(supplier, SyncOperateEnum.OPERATE_DISABLE.getCode());
        } else {
            syncKingdeeSupplierService.syncDataToKingdee(supplier, SyncOperateEnum.OPERATE_ENABLE.getCode());
        }

        LogisticsSupplierDTO.UpdateDisabledDTO updateDisabledDTO = new LogisticsSupplierDTO.UpdateDisabledDTO();
        updateDisabledDTO.setSupplierId(supplierId);
        updateDisabledDTO.setDisabled(state);
        logisticsFeign.updateDisabledBySupplierId(updateDisabledDTO);


        return this.updateById(supplier);
    }

    @Override
    public Boolean updateSrmStatus(UpdateStateDTO dto) {
        String supplierId = dto.getId();
        SupplierEntity supplier = this.getById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        Boolean state = dto.getState();
        supplier.setSrmDisabled(state);
        //添加日志
        String content = String.format("编辑了供应商[%s] 启用SRM协同状态 有[%s] 变更为[%s]", supplier.getName(), dto.getState() == true ? "启用" : "停用", dto.getState() == true ? "停用" : "启用");
        addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), supplierId, "修改操作");
        return this.updateById(supplier);
    }


    /**
     * 获取供应商
     * 获取 审核通过且开启的供应商
     *
     * @return
     * @author yl
     */
    @Override
    public List<Map<String, Object>> listApproveSupplier() {
        LambdaQueryWrapper<SupplierEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SupplierEntity::getId, SupplierEntity::getName, SupplierEntity::getDisabled);
        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        queryWrapper.eq(SupplierEntity::getApproveStatus, ApproveStatusEnum.getByStatus(approveStatus));
        return this.listMaps(queryWrapper);
    }


    /**
     * 反审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-23 10:26
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean disApprove(List<String> ids) {
        List<SupplierEntity> list = this.listByIds(ids);
        //审核中
//        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();

        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
//        List<String> statusList = new ArrayList<>(2);
//        statusList.add(approveIngStatus);
//        statusList.add(approveStatus);
        long count = list.stream().filter(s -> !approveStatus.equals(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(approveStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus));
        //反审核
        if (result) {
            //审核通过
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            batchAddModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), rejectPairList, "状态变更");
            //发送金蝶
            list.forEach(obj -> syncKingdeeSupplierService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_DISAPPROVE.getCode()));
        }
        return result;
    }


    /**
     * 下载模板
     *
     * @param response
     * @return void
     * @author yl
     * @date 2023-03-24 10:58
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/supplier.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.Default);
        }

    }


    /**
     * 修改并审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-24 18:23
     */
    @Override
    public Boolean updateAndSubmit(SupplierDTO.UpdateDTO dto) {
        String supplierId = this.updateSupplier(dto);
        if (StringUtils.isBlank(supplierId)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(supplierId));
    }


    /**
     * 供应商导出
     *
     * @param dto
     * @param response
     * @return void
     * @author yl
     * @date 2023-03-29 14:50
     */
    @Override
    public void exportSupplier(SupplierDTO.ExportDTO dto, HttpServletResponse response) {
        List<SupplierDTO.PagingViewDTO> list = baseMapper.getExportSupplier(dto);
        List<SupplierExportExcelDTO> resultList = new ArrayList<>(list.size());
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> keyList = new ArrayList<>(3);
            keyList.add(DictBasicEnum.SUPPLIER_ACCOUNT_PAYMENT.getType());
            keyList.add(DictBasicEnum.SUPPLIER_PAY_MODE.getType());
            keyList.add(DictBasicEnum.SUPPLIER_CATEGORY.getType());
            //获取供应商等级
            List<SupplierGradeEntity> supplierGradeList = supplierGradeService.list();
            //根据 key list 获取到对应数据
            List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(keyList);
            //供应商id 集合
            List<String> supplierIdList = list.stream().map(SupplierDTO.PagingViewDTO::getId).collect(Collectors.toList());
            //获取供应商默认联系人信息
            List<SupplierContactEntity> contactList = supplierContactService.getDefaultBySupplierIdList(supplierIdList);
            //付款条件
            List<DictBasicDTO.ViewDTO> paymentConditionList = sysDictFeign.getByType(SysDictBasicEnum.PAYMENT_CONDITION.getCode());
            //获取到采购订单数据
            List<PurchaseOrderSupplierEntity> orderSupplierList = purchaseOrderSupplierService.getBySupplierIds(supplierIdList);

            //获取供应商配置
            List<SupplierConfigVO> supplierConfigVOS = srmCfgSettingFeign.getConfigList(supplierIdList);
            Map<String, SupplierConfigVO> configVOMap = supplierConfigVOS.stream().collect(Collectors.toMap(SupplierConfigVO::getSupplierId, Function.identity()));
            for (SupplierDTO.PagingViewDTO item : list) {
                String id = item.getId();
                SupplierExportExcelDTO exportExcel = new SupplierExportExcelDTO();
                exportExcel.setName(item.getName());
                exportExcel.setCode(item.getCode());
                //禁用状态 true 禁用
                boolean disabled = item.getDisabled();
                exportExcel.setEnableStatus(disabled ? "停用" : "启用");
                boolean srmDisabled = item.getSrmDisabled();
                exportExcel.setSrmDisabled(srmDisabled ? "停用" : "启用");
                SupplierConfigVO supplierConfigVO = configVOMap.get(id);
                if (Objects.nonNull(supplierConfigVO)){
                    exportExcel.setOrderAcceptRule(supplierConfigVO.getOrderAcceptRule());
                    exportExcel.setReturnConfirmRule(supplierConfigVO.getReturnConfirmRule());
                }
                ApproveStatusEnum approveStatus = item.getApproveStatus();
                exportExcel.setApproveStatusName(approveStatus.getName());
                //阶段
                SupplierPhaseEnum phaseEnum = item.getPhase();
                exportExcel.setPhaseName(phaseEnum.getName());
                //等级id
                String gradeId = item.getGradeId();
                String gradeName = supplierGradeList.stream().filter(g -> g.getId().equals(gradeId)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                exportExcel.setGradeName(gradeName);
                //分类
                String categoryId = item.getCategoryId();
                String categoryName = dictBasicList.stream().filter(d -> d.getId().equals(categoryId)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                exportExcel.setCategoryName(categoryName);
                //结算方式
                String payMethodId = item.getPayMethodId();
                String payMethodName = dictBasicList.stream().filter(d -> d.getId().equals(payMethodId)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                exportExcel.setPayMethodName(payMethodName);
                //付款条件
                String paymentConditionName = paymentConditionList.stream().filter(obj -> obj.getValue().equals(item.getPaymentCondition())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                item.setPaymentConditionName(paymentConditionName);
                //采购员
                exportExcel.setPurchaseUserName(item.getPurchaseUserName());
                SupplierContactEntity contact = contactList.stream().filter(c -> c.getSupplierId().equals(item.getId())).findFirst().orElse(null);
                if (contact != null) {
                    exportExcel.setContactPerson(contact.getPerson());
                    exportExcel.setContactTelNumber(contact.getTelNumber());
                }
                //采购次数
                long purchasesCount = orderSupplierList.stream().filter(o -> o.getSupplierId().equals(id)).count();
                exportExcel.setPurchasesCount((int) purchasesCount);


                resultList.add(exportExcel);

            }
            String fileName = "供应商数据";
            try {
                ExcelUtil.export(fileName, "供应商数据", resultList, SupplierExportExcelDTO.class, response);
            } catch (Exception e) {
                throw new ServiceException(ApiError.ERROR_1015);
            }
        }
    }

    /**
     * 供应商导入
     *
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-30 9:44
     */
    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        List<String> keyList = new ArrayList<>(3);
        keyList.add(DictBasicEnum.SUPPLIER_ACCOUNT_PAYMENT.getType());
        keyList.add(DictBasicEnum.SUPPLIER_PAY_MODE.getType());
        keyList.add(DictBasicEnum.SUPPLIER_CATEGORY.getType());
        //获取供应商等级
        List<SupplierGradeEntity> supplierGradeList = supplierGradeService.list();
        //根据 key list 获取到对应数据
        List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(keyList);
        //用户信息
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        List<SupplierEntity> supplierList = this.list();
        //币种信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(new ArrayList<>());
        List<BaseIdDTO> bankList = sysUserFeign.getBankList(new ArrayList<>());
        SupplierExcelListener excelListener = new SupplierExcelListener(this, supplierGradeList, dictBasicList, supplierList, userList, currencyList, bankList);
        try {
            EasyExcel.read(excelFile.getInputStream(), SupplierImportExcelDTO.class, excelListener).sheet(0).doRead();
        } catch (Exception e) {
            log.error("供应商导入错误！", e);
            return Boolean.FALSE;
        }
        List<SupplierImportExcelDTO> errorList = excelListener.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "供应商导入错误信息";
            ExcelUtil.export(fileName, "supplierError", errorList, SupplierImportExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    /**
     * 获取供应商的一些信息
     *
     * @param supplierId
     * @return com.erp.model.scm.dto.SupplierDTO.ViewDTO
     * @author yl
     * @date 2023-03-30 10:48
     */
    @Override
    public SupplierDTO.ViewDTO getBySupplierId(String supplierId) {
        SupplierEntity entity = this.getById(supplierId);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        SupplierDTO.ViewDTO view = new SupplierDTO.ViewDTO();
        List<SupplierContactEntity> contactList = supplierContactService.getDefaultBySupplierIdList(Arrays.asList(supplierId));
        if (CollectionUtils.isNotEmpty(contactList)) {
            SupplierContactEntity contact = contactList.get(0);
            BeanMapper.copy(contact, view);
            view.setContactId(contact.getId());
        }
        List<CurrencyDTO.ViewDTO> currency = sysUserFeign.listByCurrency(Arrays.asList(entity.getPayCurrency()));
        if (CollectionUtils.isNotEmpty(currency)) {
            CurrencyDTO.ViewDTO viewDTO = currency.get(0);
            view.setCurrencySymbol(viewDTO.getSymbol());
        }
        view.setPayMethodId(entity.getPayMethodId());
        view.setPayCurrency(entity.getPayCurrency());
        view.setPaymentCondition(entity.getPaymentCondition());
        view.setCompanyAddress(entity.getCompanyAddress());
        return view;
    }


    /**
     * 批量保存 导入的供应商
     *
     * @param addList
     * @return void
     * @author yl
     * @date 2023-03-30 20:01
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchImportSupplier(List<SupplierDTO.ImportAddDTO> addList) {
        if (CollectionUtils.isNotEmpty(addList)) {
            int initSize = addList.size();
            //供应商添加信息
            List<SupplierEntity> addSupplierList = new ArrayList<>(initSize);
            //供应商联系信息
            List<SupplierContactEntity> addContactList = new ArrayList<>(initSize);
            //账户信息
            List<SupplierAccountEntity> addAccountList = new ArrayList<>(initSize);
            //资质信息
            List<SupplierCredentialEntity> addCredentialList = new ArrayList<>(initSize);
            for (SupplierDTO.ImportAddDTO item : addList) {
                SupplierEntity supplier = new SupplierEntity();
                BeanMapper.copy(item, supplier);
                String supplierId = IdWorker.getIdStr();
                supplier.setId(supplierId);
                String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.GYS, BusinessNoTypeEnum.CODE_GYS.getCode()));
                supplier.setCode(code);
                supplier.setSrmDisabled(true);
                addSupplierList.add(supplier);
                //账户
                List<SupplierAccountDTO.ImportAddDTO> accountList = item.getBankAccountList();
                addAccountList.addAll(supplierAccountService.transform(supplierId, accountList));
                //联系人信息
                List<SupplierContactDTO.ImportAddDTO> contactList = item.getContactList();
                addContactList.addAll(supplierContactService.transform(supplierId, contactList));
                //资质信息
                List<SupplierCredentialDTO.ImportAddDTO> credentialList = item.getCredentialList();
                addCredentialList.addAll(supplierCredentialService.transform(supplierId, credentialList));
            }

            this.saveBatch(addSupplierList);
            supplierAccountService.saveBatch(addAccountList);
            supplierContactService.saveBatch(addContactList);
            supplierCredentialService.saveBatch(addCredentialList);

            List<Pair<String, String>> pairList = addSupplierList.stream().
                    map(obj -> new Pair<>(obj.getId(), obj.getName())).collect(Collectors.toList());
            String content = "导入一个供应商信息[%s]";
            batchAddModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), pairList, "新增操作");

        }


    }


    /**
     * 查询是否 有供应商占用 要删除的id 如果有就不能删除
     *
     * @param gradeIdList
     * @return int
     * @author yl
     * @date 2023-03-31 11:07
     */
    @Override
    public int occupiedGrade(List<String> gradeIdList) {
        if (CollectionUtils.isEmpty(gradeIdList)) {
            return 0;
        }
        return lambdaQuery().in(SupplierEntity::getGradeId, gradeIdList).count();
    }


    /**
     * 阶段审核通过后 更改供应商的阶段
     *
     * @param list
     * @return void
     * @author yl
     * @date 2023-03-31 16:45
     */
    @Override
    public void updatePhase(List<SupplierPhaseEntity> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> supplierIds = list.stream().map(SupplierPhaseEntity::getSupplierId).distinct().collect(Collectors.toList());
            List<SupplierEntity> supplierList = this.listByIds(supplierIds);
            List<SupplierEntity> updateList = new ArrayList<>(list.size());
            for (SupplierPhaseEntity item : list) {
                String supplierId = item.getSupplierId();
                SupplierEntity supplier = supplierList.stream().filter(s -> s.getId().equals(supplierId)).findFirst().orElse(null);
                if (supplier != null) {
                    String targetPhase = item.getTargetPhase();
                    SupplierPhaseEnum target = SupplierPhaseEnum.getPhase(targetPhase);
                    if (target != null) {
                        supplier.setPhase(target);
                        updateList.add(supplier);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(updateList)) {
                this.updateBatchById(updateList);
            }
        }
    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(SupplierEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), SupplierEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }


    /**
     * 根据供应商类型 获取对应供应商
     *
     * @param categoryType
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO>
     * @author yl
     * @date 2023-05-23 16:31
     */
    @Override
    public List<BaseIdDTO> listSupplierByCategoryType(String categoryType) {
        String supplierCategory = DictBasicEnum.SUPPLIER_CATEGORY.getType();
        List<SupplierDTO.SupplierSimpleDTO> dataList = baseMapper.listSupplierByCategoryType(supplierCategory, categoryType, null);
        if (CollUtil.isNotEmpty(dataList)) {
            return dataList.stream().map(data -> {
                BaseIdDTO baseIdDTO = new BaseIdDTO();
                baseIdDTO.setId(data.getId());
                baseIdDTO.setName(data.getName());
                return baseIdDTO;
            }).collect(Collectors.toList());
        }
        return new ArrayList<>(0);
    }

    @Override
    public Map<String, SupplierDTO.SupplierSimpleDTO> getSupplierSimpleInfo(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Maps.newHashMap();
        }
        ids = ids.stream().distinct().collect(Collectors.toList());
        List<SupplierEntity> supplierList = lambdaQuery().in(SupplierEntity::getId, ids).list();
        if (CollUtil.isEmpty(supplierList)) {
            return Maps.newHashMap();
        }
        Map<String, SupplierEntity> supplierEntityMap = supplierList.stream().collect(Collectors.toMap(SupplierEntity::getId, Function.identity()));

        Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap = Maps.newHashMapWithExpectedSize(supplierEntityMap.size());
        supplierEntityMap.forEach((id, sup) -> {
            SupplierDTO.SupplierSimpleDTO supplierSimpleDTO = new SupplierDTO.SupplierSimpleDTO();
            supplierSimpleDTO.setId(id);
            supplierSimpleDTO.setCode(sup.getCode());
            supplierSimpleDTO.setName(sup.getName());
            supplierSimpleDTO.setDisabled(sup.getDisabled());
            supplierMap.put(id, supplierSimpleDTO);
        });
        return supplierMap;
    }

    @Override
    public List<SupplierDTO.SupplierSimpleDTO> listApproveSupplierByCategoryType(String categoryType) {
        String supplierCategory = DictBasicEnum.SUPPLIER_CATEGORY.getType();
        List<SupplierDTO.SupplierSimpleDTO> dataList = baseMapper.listSupplierByCategoryType(supplierCategory, categoryType, null);
        if (CollUtil.isNotEmpty(dataList)) {
            // 未审核通过的设置为禁用
            dataList.stream().forEach(data -> {
                if (!Objects.equals(data.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
                    data.setDisabled(Boolean.TRUE);
                }
            });
        }
        List<SupplierDTO.SupplierSimpleDTO> wantList=new ArrayList<>(dataList.size());
        List<SupplierDTO.SupplierSimpleDTO> list1=dataList.stream().filter(d->!d.getDisabled()).collect(Collectors.toList());
        List<SupplierDTO.SupplierSimpleDTO> list2=dataList.stream().filter(d->d.getDisabled()).collect(Collectors.toList());
        wantList.addAll(list1);
        wantList.addAll(list2);
        return wantList;
    }

    @Override
    public List<SupplierEntity> listByCodes(List<String> supplierCodes) {
        if (CollectionUtils.isEmpty(supplierCodes)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery()
                .in(SupplierEntity::getCode, supplierCodes)
                .list();
    }

    @Override
    public List<SupplierEntity> listByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery()
                .in(SupplierEntity::getId, ids)
                .list();
    }


    /**
     * 根据名称获取供应商
     *
     * @param supplierNames
     * @return java.util.List<com.erp.model.scm.entity.SupplierEntity>
     * @author yl
     * @date 2023-09-22 19:51
     */
    @Override
    public List<SupplierEntity> listBySupplierByNames(List<String> supplierNames) {
        if (CollectionUtils.isEmpty(supplierNames)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SupplierEntity::getName, supplierNames).list();
    }

    @Override
    public SupplierEntity getSupplierByUid(String uid) {
        SupplierRefUserEntity supplier = supplierRefUserService.getSupplierRelUserByUid(uid);
        if(Objects.isNull(supplier)){
            return null;
        }
        return this.getById(supplier.getSupplierId());
    }

    /**
     * @param list
     * @description: 提交流程
     * @author Will
     * @date: 2023/7/3 14:39
     */
    private void startProcess(List<SupplierEntity> list) {
        LoginUser userInfo = commonService.getUserInfo();
        ValidList<ProcessManagementDTO.StartDTO> resultList = new ValidList<>();
        list.forEach(obj -> {
            ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
            startDTO.setBusinessId(obj.getId());
            startDTO.setBusinessCode(obj.getCode());
            startDTO.setBusinessKey(SourceTypeEnum.SUPPLIER.getCode());
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
     * @param list
     * @param dto
     * @description: 流程审核
     * @author Will
     * @date: 2023/7/3 15:24
     */
    private void approveProcess(List<SupplierEntity> list, BaseApproveParamDTO dto) {
        ValidList<ProcessManagementDTO.ApproveDTO> resultList = new ValidList<>();
        LoginUser userInfo = commonService.getUserInfo();
        list.forEach(obj -> {
            ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
            approveDTO.setBusinessId(obj.getId());
            approveDTO.setBusinessKey(SourceTypeEnum.SUPPLIER.getCode());
            approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
            approveDTO.setComment(dto.getComment());
            approveDTO.setUserId(userInfo.getUid());
            approveDTO.setVariablesMap(BeanUtil.beanToMap(obj));
            resultList.add(approveDTO);
        });
        ApiResult<List<ProcessManagementDTO.ApproveResultDTO>> listApiResult = workflowFeign.batchApproveProcess(resultList);
        Integer code = listApiResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        List<ProcessManagementDTO.ApproveResultDTO> data = listApiResult.getData();
        List<String> updateIdList = data.stream()
                .filter(obj -> ObjectUtils.isEmpty(obj.getIsExistProcess()) || !obj.getIsExistProcess())
                .map(ProcessManagementDTO.ApproveResultDTO::getBusinessId)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(updateIdList)) {
            //无需走流程的数据则直接更新状态
            List<SupplierEntity> updateList = list.stream().filter(obj -> updateIdList.contains(obj.getId())).collect(Collectors.toList());
            approveEnd(dto, updateList);
        }
    }

    /**
     * 更改状态
     */
    private Boolean updateApproveStatus(List<SupplierEntity> list, ApproveStatusEnum statusEnum) {
        if (CollectionUtils.isNotEmpty(list)) {
            list.stream().forEach(obj -> {
                obj.setApproveStatus(statusEnum);
            });
            return this.updateBatchById(list);
        }
        return true;

    }


    private List<SupplierEntity> getByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SupplierEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SupplierEntity::getId, ids);
        return this.list(queryWrapper);

    }


    /**
     * 检查名称不能重复
     *
     * @param id
     * @param name
     * @return void
     * @author yl
     * @date 2023-03-17 16:48
     */
    private void checkName(String id, String name) {
        LambdaQueryWrapper<SupplierEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(SupplierEntity::getId, id);
        }
        queryWrapper.eq(SupplierEntity::getName, name);
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98034);
        }
    }


    /**
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        moduleOperateLogService.addModuleOperateLog(content, code, businessId, operation);
    }

    /**
     * 批量添加日志
     */

    private void batchAddModuleOperateLog(String content, String code, List<Pair<String, String>> pairList, String operation) {
        moduleOperateLogService.batchAddModuleOperateLog(content, code, pairList, operation);
    }


}
