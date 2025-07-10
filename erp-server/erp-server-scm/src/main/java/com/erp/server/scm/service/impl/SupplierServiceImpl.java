package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
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
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.dto.excel.SupplierExportExcelDTO;
import com.erp.model.scm.dto.excel.SupplierImportExcelDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.DictBasicEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.SupplierPhaseEnum;
import com.erp.model.scm.enums.SupplierTabEnum;
import com.erp.model.srm.vo.SupplierConfigVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.SysUserThirdEntity;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import com.erp.model.wms.dto.SupplierCountDTO;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.srm.feign.SrmCfgSettingFeign;
import com.erp.rpc.srm.feign.SrmPoReconciliationFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.TransferLogisticsFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
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
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUPPLIER;

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

    @Resource
    private SrmPoReconciliationFeign srmPoReconciliationFeign;
    @Resource
    private WmsTaskFeign wmsTaskFeign;
    @Autowired
    private TransferLogisticsFeign transferLogisticsFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private KingdeePaymentConditionService  kingdeePaymentConditionService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private CfgQueryOptionFeign cfgQueryOptionFeign;

    @Resource
    private SupplierService self;
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
    public SupplierEntity addSupplier(SupplierDTO.AddDTO dto) {
        checkName(null, dto.getName());
        //供应商联系信息
        List<SupplierContactDTO.AddDTO> contactList = dto.getContactList();
        //检查联系人默认是否多个
        supplierContactService.checkIsDefault(contactList);
        //供应商资质信息
        List<SupplierCredentialDTO.AddDTO> credentialList = dto.getCredentialList();
        //检查资质日期
        supplierCredentialService.checkListDate(credentialList);
        String paymentConditionCode = dto.getPaymentCondition();
        //验证付款条件是否正确
        if (StrUtils.isNotEmpty(paymentConditionCode)) {
            KingdeePaymentConditionEntity paymentCondition = kingdeePaymentConditionService.getByCode(paymentConditionCode);
            if (Objects.isNull(paymentCondition)) {
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
        if (CharSequenceUtil.isBlank(categoryName)) {
            throw new ServiceException(ApiError.ERROR_NOT_FOUND,CharSequenceUtil.format("供应商分类{}", categoryId));
        }

        //获取供应商等级
        List<SupplierGradeEntity> supplierGradeList = supplierGradeService.list();
        String gradeId = dto.getGradeId();
        String gradeName = supplierGradeList.stream().filter(d -> d.getId().equals(gradeId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        if (CharSequenceUtil.isBlank(gradeName)) {
            throw new ServiceException(ApiError.ERROR_NOT_FOUND,CharSequenceUtil.format("供应商等级{}", gradeId));
        }
        addEntity.setGradeName(gradeName);

        addEntity.setId(supplierId);
        addEntity.setCategoryName(categoryName);
        //生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_GYS);
        addEntity.setCode(code);
        String purchaseUserId = dto.getPurchaseUserId();
        if (StringUtils.isNotBlank(purchaseUserId)) {
            FindUserDTO user = sysUserFeign.getUserByUserId(purchaseUserId);
            addEntity.setPurchaseUserName(user != null ? user.getUserName() : "");
        }
        if (Objects.isNull(addEntity.getSrmDisabled())){
            addEntity.setSrmDisabled(Boolean.TRUE);//默认禁用
        }
        addEntity.setSrmDisabledDate(LocalDate.now());
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (Objects.nonNull(userInfo)){
            addEntity.setSrmOperateUserId(userInfo.getUid());
            addEntity.setSrmOperateUserName(userInfo.getUserName());
        }
        //如果付款公司是空，那就是自己公司付款
        if (CharSequenceUtil.isBlank(addEntity.getPaymentCompanyName())) {
            addEntity.setPaymentCompanyName(addEntity.getName());
        }
        Boolean result = this.save(addEntity);
        //保存成功
        if (result) {
            //供应商账号信息
            List<SupplierAccountDTO.AddDTO> bankAccountList = dto.getBankAccountList();
            supplierAccountService.saveBatchBankAccount(supplierId, bankAccountList);
            //供应商联系人信息
            supplierContactService.saveBatchContact(supplierId, contactList);

            credentialList.stream().forEach(e-> e.setSupplierId(supplierId));
            supplierCredentialService.saveBatchCredential(credentialList);
            //添加日志
            String content = String.format("新增了一个{%s}-供应商信息-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), supplierId, "新增操作");
            return addEntity;
        }

        return null;
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
        SupplierEntity entity = this.addSupplier(dto);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        BatchResultDTO submit = this.submit(entity);
        return submit.getSuccess();
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
        String paymentConditionName="";
        String paymentConditionCode = supplier.getPaymentCondition();
        //付款条件
        if (StringUtils.isNotBlank(paymentConditionCode)) {
            KingdeePaymentConditionEntity paymentCondition = kingdeePaymentConditionService.getByCode(paymentConditionCode);
            if (Objects.nonNull(paymentCondition)) {
                paymentConditionName = paymentCondition.getName();
            }
        }
        result.setPaymentCondition(paymentConditionCode);
        result.setPaymentConditionName(paymentConditionName);
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
        String paymentConditionCode = dto.getPaymentCondition();
        //验证付款条件是否正确
        if (StrUtils.isNotEmpty(paymentConditionCode)) {
            KingdeePaymentConditionEntity paymentCondition = kingdeePaymentConditionService.getByCode(paymentConditionCode);
            if (Objects.isNull(paymentCondition)) {
                throw new ServiceException("付款条件错误");
            }
        }

        //旧的
        SupplierEntity old = new SupplierEntity();
        BeanMapper.copy(supplier, old);
        //供应商srm状态是否修改
        if (Objects.nonNull(dto.getSrmDisabled()) && !supplier.getSrmDisabled().equals(dto.getSrmDisabled())){
            LoginUser user = UserContext.getDefaultLoginUser();
            if (Objects.nonNull(user)){
                supplier.setSrmOperateUserId(user.getUid());
                supplier.setSrmOperateUserName(user.getUserName());
            }
            supplier.setSrmDisabledDate(LocalDate.now());
            //设置为启用时：校验当前周期是否存在收货单【按确认日期】，若有则提示【SRM协同开启后，下月生效】，若无关联单据则直接启用
            //设置为停用时：供应商协同开启后关闭--新增校验：存在待对账明细/未确认的对账单，请完成对账后关闭
            if (dto.getSrmDisabled()){
                //禁用
                Integer count = srmPoReconciliationFeign.countSupplierUnConfirmOrderDetail(supplierId);
                if (Objects.nonNull(count) && count > 0){
                    throw new ServiceException(ApiError.ERROR_SUPPLIER_EXIST_PO_RECONCILIATION_DETAIL);
                }
            }else {
                //启用时 检查当前周期确认订单是否存在，存在则下月生效
                SupplierCountDTO countDTO = wmsTaskFeign.countOrderBySupplierId(supplierId);
                if (Objects.nonNull(countDTO) && Objects.nonNull(countDTO.getLocalDate()) && countDTO.getCount() > 0){
                    supplier.setSrmDisabledDate(countDTO.getLocalDate().plusDays(1));
                }else {
                    supplier.setSrmDisabledDate(LocalDate.now());
                }
            }
        }
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
        supplierCredentialService.checkListDate(credentialAddList);
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

        //如果付款公司是空，那就是自己公司付款
        if (CharSequenceUtil.isBlank(supplier.getPaymentCompanyName())) {
            supplier.setPaymentCompanyName(supplier.getName());
        }

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
            credentialList.stream().forEach(e-> e.setSupplierId(supplierId));
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
        List<KingdeePaymentConditionEntity> paymentConditionList = kingdeePaymentConditionService.list();


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
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
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
            String paymentConditionName = paymentConditionList.stream().filter(obj -> obj.getCode().equals(item.getPaymentCondition())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
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

            //发送金蝶
            sendPushTask(supplierList,SyncOperateEnum.OPERATE_DELETE.getCode());
        }


        return result;
    }


    /**
     * 批量提交审核
     *
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 19:07
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO submit(SupplierEntity entity) {
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        //审核中
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        if (!statusList.contains(entity.getApproveStatus().getStatus())) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        //提交流程
        startProcess(entity);
        Boolean result = this.updateApproveStatus(Collections.singletonList(entity), ApproveStatusEnum.getByStatus(ingStatus));
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), entity.getId(), "状态变更");
            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            addModuleOperateLog(rejectContent, ModuleTypeEnum.SUPPLIER.getCode(), entity.getId(), "状态变更");
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }


    /**
     * 审核 供应商
     *
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 19:41
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(SupplierEntity entity, String type, String comment, Boolean isNeedProcess) {
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus().getStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }
        //调用审核流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SUPPLIER.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(type));
        approveDTO.setComment(comment);
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> result = workflowFeign.approve(approveDTO);
        Integer code = result.getCode();
        if (200 != code) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_94006.msg);
        }
        ProcessManagementDTO.ApproveResultDTO data = result.getData();
        if (Objects.nonNull(data) && ObjectUtils.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            //无需走流程的数据则直接更新状态
            approveEnd(entity,type,comment);
        }
        //添加日志
        addModuleOperateLog(String.format("审核【%s】了一个供应商信息【%s】", ApproveTypeEnum.getName(type),entity.getCode()).concat(StringUtils.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.SUPPLIER.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    /**
     * @param entity
     * @param type
     * @param comment
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/3 15:25
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean approveEnd(SupplierEntity entity,String type, String comment) {
        if (Objects.isNull(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(type);
        Boolean result = this.updateApproveStatus(Collections.singletonList(entity), approveStatus);
        if (!result) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        if (ApproveType.PASS.equals(type)) {
            //发送金蝶
            sendSinglePushTask(entity,SyncOperateEnum.OPERATE_APPROVE.getCode());
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
            List<BaseIdDTO.CodeDTO> transferLogisticsChannelList = transferLogisticsFeign.listBySupplierId(supplierId);
            long transferLogisticsChannelCount = transferLogisticsChannelList.stream().filter(c -> !c.getDisabled()).count();
            if (transferLogisticsChannelCount > 0) {
                throw new ServiceException(ApiError.ERROR_TRANSFER_LOGISTICS_CHANNEL_DISABLED_EXIST);
            }
        }
        supplier.setDisabled(state);

        //添加日志
        String content = String.format("编辑了供应商[%s] 启用状态 由[%s] 变更为[%s]", supplier.getName(), dto.getState() == true ? "启用" : "停用", dto.getState() == true ? "停用" : "启用");
        addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), supplierId, "修改操作");

        LogisticsSupplierDTO.UpdateDisabledDTO updateDisabledDTO = new LogisticsSupplierDTO.UpdateDisabledDTO();
        updateDisabledDTO.setSupplierId(supplierId);
        updateDisabledDTO.setDisabled(state);
        logisticsFeign.updateDisabledBySupplierId(updateDisabledDTO);

        //修改中转服务商启用状态
        TransferLogisticsSupplierDTO.UpdateDisabledDTO transferUpdateDisabledDTO = new TransferLogisticsSupplierDTO.UpdateDisabledDTO();
        transferUpdateDisabledDTO.setSupplierId(supplierId);
        transferUpdateDisabledDTO.setDisabled(state);
        transferLogisticsFeign.updateDisabledBySupplierId(transferUpdateDisabledDTO);

        //发送金蝶
        String operate = SyncOperateEnum.OPERATE_ENABLE.getCode();
        if (dto.getState()) {
            operate = SyncOperateEnum.OPERATE_DISABLE.getCode();
        }
        sendPushTask(Arrays.asList(supplier),operate);
        return this.updateById(supplier);
    }

    @Override
    public ApiResult<String> updateSrmStatus(UpdateStateDTO dto) {
        String supplierId = dto.getId();
        SupplierEntity supplier = this.getById(supplierId);
        String msg = "操作成功";
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        supplier.setSrmDisabledDate(LocalDate.now());
        //当启用后 禁用时 校验是否存在未确认采购对账单明细
        if (!supplier.getSrmDisabled() && dto.getState()){
            Integer count = srmPoReconciliationFeign.countSupplierUnConfirmOrderDetail(supplierId);
            if (Objects.nonNull(count) && count > 0){
                throw new ServiceException(ApiError.ERROR_SUPPLIER_EXIST_PO_RECONCILIATION_DETAIL);
            }
        }
        //禁用后启用 校验当前周期是否存在收货单【按确认日期】，若有则提示【SRM协同开启后，下月生效】，若无关联单据则直接启用
        if (supplier.getSrmDisabled() && !dto.getState()){
            //启用时 检查当前周期确认订单是否存在，存在则下月生效
            //启用时 检查当前周期确认订单是否存在，存在则下月生效
            SupplierCountDTO countDTO = wmsTaskFeign.countOrderBySupplierId(supplierId);
            if (Objects.nonNull(countDTO) && Objects.nonNull(countDTO.getLocalDate()) && countDTO.getCount() > 0){
                //SRM协同开启后，下月生效
                msg = "SRM协同开启后，下月生效";
                supplier.setSrmDisabledDate(countDTO.getLocalDate().plusDays(1));
            }else {
                supplier.setSrmDisabledDate(LocalDate.now());
            }
        }
        Boolean state = dto.getState();
        supplier.setSrmDisabled(state);
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (Objects.nonNull(userInfo)){
            supplier.setSrmOperateUserId(userInfo.getUid());
            supplier.setSrmOperateUserName(userInfo.getUserName());
        }
        //添加日志
        String content = String.format("编辑了供应商[%s] 启用SRM协同状态 由[%s] 变更为[%s]", supplier.getName(), dto.getState() ? "启用" : "停用", dto.getState() ? "停用" : "启用");
        addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), supplierId, "修改操作");
        this.updateById(supplier);
        return ApiResult.successMsg(msg);
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
        queryWrapper.select(SupplierEntity::getId, SupplierEntity::getName, SupplierEntity::getDisabled, SupplierEntity::getSrmDisabled);
        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        queryWrapper.eq(SupplierEntity::getApproveStatus, ApproveStatusEnum.getByStatus(approveStatus));
        queryWrapper.orderByAsc(SupplierEntity::getSrmDisabled).orderByAsc(SupplierEntity::getDisabled);
        return this.listMaps(queryWrapper);
    }

    /**
     * 反审核
     *
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-23 10:26
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(SupplierEntity entity){
        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        if (!approveStatus.equals(entity.getApproveStatus().getStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98014.msg);
        }
        Boolean result = this.updateApproveStatus(Collections.singletonList(entity), ApproveStatusEnum.getByStatus(waitSubmitStatus));
        //反审核
        if (result) {
            //审核通过
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), entity.getId(), "状态变更");

            //发送金蝶
            sendSinglePushTask(entity,SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.DEFAULT);
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
        SupplierEntity entity = this.getById(supplierId);
        if (ObjUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98031);
        }
        BatchResultDTO submit = this.submit(entity);
        return submit.getSuccess();
    }


    /**
     * 供应商导出
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-03-29 14:50
     */
    @Override
    public void exportSupplier(SupplierDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("供应商数据", EXPORT_SCM_SUPPLIER.getCode(), dto);
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
        //结算方式名称
        DictBasicEntity payMethod = dictBasicService.getById(entity.getPayMethodId());
        if (ObjectUtils.isNotEmpty(payMethod)) {
            view.setPayMethodName(payMethod.getName());
        }

        view.setPayCurrency(entity.getPayCurrency());
        view.setPaymentCondition(entity.getPaymentCondition());
        String paymentConditionCode = entity.getPaymentCondition();

        //付款条件名称
        String paymentConditionName = "";
        if(StringUtils.isNotBlank(paymentConditionCode)){
            KingdeePaymentConditionEntity paymentCondition = kingdeePaymentConditionService.getByCode(paymentConditionCode);
            if (Objects.nonNull(paymentCondition)) {
                paymentConditionName = paymentCondition.getName();
            }
        }
        view.setPaymentConditionName(paymentConditionName);
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
//                String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.GYS, BusinessNoTypeEnum.CODE_GYS.getCode()));
                String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_GYS);
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
    public Boolean cancelProcess(List<String> ids) {
        //根据ids查询
        List<SupplierEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }

        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("供应商撤销流程，ids=【{}】", ids);

        //撤销现有流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ids.forEach(obj -> {
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.SUPPLIER.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);
        });

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT);
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("供应商【%s】取消流程", ModuleTypeEnum.SUPPLIER.getCode(), pairList, "取消流程操作");

        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(SupplierDTO.BatchUpdateCategoryDTO dto) {
        List<SupplierEntity> supplierEntityList = this.listByIds(dto.getIds());
        if(CollectionUtils.isEmpty(supplierEntityList)){
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        DictBasicEntity dictBasicEntity = dictBasicService.getById(dto.getCategoryId());
        if(Objects.isNull(dictBasicEntity)){
            throw new ServiceException("供应商分类信息为空");
        }
        List<SupplierEntity> updateList = new ArrayList<>();
        List<Pair<String, String>> pairList = new ArrayList<>();
        supplierEntityList.forEach(v->{
            if(!v.getCategoryId().equals(dto.getCategoryId())){
                Pair<String, String> pair = new Pair<>(v.getId(),v.getCategoryName());
                v.setCategoryId(dictBasicEntity.getId());
                v.setCategoryName(dictBasicEntity.getName());
                pairList.add(pair);
                updateList.add(v);
            }
        });
        if(CollectionUtils.isEmpty(updateList)){
            return;
        }
        if(!this.updateBatchById(updateList)){
            throw new ServiceException("更新供应商分类信息失败");
        }
        String content = "供应商分类由[%s]变更为"+dictBasicEntity.getName();
        batchAddModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), pairList, "供应商分类变更");

    }

    @Override
    public PagingVO<BaseDropDownDTO.RemarkDTO> pagingSelect(PagingDTO<BaseDropDownDTO.SelectDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseDropDownDTO.SelectDTO params = dto.getParams();
        IPage<BaseDropDownDTO.RemarkDTO> pagResult = baseMapper.pagingSelect(query, params);
        List<BaseDropDownDTO.RemarkDTO> records = pagResult.getRecords();
        //排序
        List<BaseDropDownDTO.RemarkDTO> list = records.stream().sorted(Comparator.comparing(BaseDropDownDTO.RemarkDTO::getDisabled)).collect(Collectors.toList());
        pagResult.setRecords(list);
        return new PagingVO<>(pagResult);
    }

    @Override
    public PagingVO<SupplierExportExcelDTO> exportSupplier(PagingDTO<SupplierDTO.PagingParamDTO> dto) {
        Page<SupplierDTO.PagingViewDTO> page = baseMapper.getExportSupplier(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        List<SupplierExportExcelDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PagingVO<>();
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
        List<String> supplierIdList = page.getRecords().stream().map(SupplierDTO.PagingViewDTO::getId).collect(Collectors.toList());
        //获取供应商默认联系人信息
        List<SupplierContactEntity> contactList = supplierContactService.getDefaultBySupplierIdList(supplierIdList);
        //付款条件
        List<KingdeePaymentConditionEntity> paymentConditionList = kingdeePaymentConditionService.list();
        //获取到采购订单数据
        List<PurchaseOrderSupplierEntity> orderSupplierList = purchaseOrderSupplierService.getBySupplierIds(supplierIdList);

        //获取供应商配置
        List<SupplierConfigVO> supplierConfigVOS = srmCfgSettingFeign.getConfigList(supplierIdList);
        Map<String, SupplierConfigVO> configVOMap = supplierConfigVOS.stream().collect(Collectors.toMap(SupplierConfigVO::getSupplierId, Function.identity()));
        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        page.getRecords().forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SUPPLIER.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(ApiError.ERROR_500);
            }
        }

        for (SupplierDTO.PagingViewDTO item : page.getRecords()) {
            String id = item.getId();
            SupplierExportExcelDTO exportExcel = new SupplierExportExcelDTO();
            exportExcel.setName(item.getName());
            exportExcel.setCode(item.getCode());
            exportExcel.setVoucherNo(item.getVoucherNo());
            //禁用状态 true 禁用
            boolean disabled = Objects.nonNull(item.getDisabled()) ? item.getDisabled() : true;
            exportExcel.setEnableStatus(disabled ? "停用" : "启用");
            boolean srmDisabled = Objects.nonNull(item.getSrmDisabled()) ? item.getSrmDisabled() : true;
            exportExcel.setSrmDisabled(srmDisabled ? "否" : "是");
            SupplierConfigVO supplierConfigVO = configVOMap.get(id);
            if (Objects.nonNull(supplierConfigVO)) {
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
            String paymentCondition = item.getPaymentCondition();
            String paymentConditionName = paymentConditionList.stream().filter(obj -> obj.getCode().equals(paymentCondition)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            exportExcel.setPaymentConditionName(paymentConditionName);
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
            exportExcel.setCreateTime(item.getCreateTime());
            exportExcel.setCreateUserName(item.getCreateUserName());
            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(item.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                exportExcel.setApproveUserName(curApprove);
            }
            exportExcel.setApproveTime(item.getApproveTime());
            resultList.add(exportExcel);

        }
        return new PagingVO<>(resultList, (int) page.getTotal(), dto.getPageSize(), dto.getCurrPage());
    }

    @Override
    public Boolean updateVoucherNo(List<String> ids, String voucherNo) {
        if (CollectionUtils.isEmpty(ids)) {
            return Boolean.TRUE;
        }
        this.lambdaUpdate()
                .in(SupplierEntity::getId, ids)
                .set(SupplierEntity::getVoucherNo, voucherNo)
                .update(new SupplierEntity());
        ids.forEach(v->{
            String content = StrUtil.format("更新外部平台单号为：{}", voucherNo);
            moduleOperateLogService.addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), v, "更新外部平台单号");
        });
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplierEntity add(SupplierDTO.InsertDTO addDTO) {
        SupplierEntity supplierEntity = addSupplier(addDTO);
        if(Objects.isNull(supplierEntity)){
            throw new ServiceException(ApiError.ERROR_1019);
        }
        SupplierEntity oldEntity = this.getById(supplierEntity.getId());
        //直接审核通过
        if (ObjectUtil.isNotEmpty(addDTO.getApprovalStatus()) && ApproveStatusEnum.APPROVE.equals(addDTO.getApprovalStatus())) {
            //根据id，更新审核状态
            self.updateApproveStatus(new SupplierDTO.UpdateApproveStatusDTO(addDTO.getThirdApprovalUserId(),addDTO.getThirdApproveTime(),oldEntity, ApproveStatusEnum.APPROVE));
        }
        return oldEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(SupplierDTO.UpdateApproveStatusDTO updateApproveStatusDTO) {
        ApproveStatusEnum approveStatus = updateApproveStatusDTO.getApproveStatus();
        SupplierEntity supplierEntity = updateApproveStatusDTO.getSupplierEntity();
        if (approveStatus == ApproveStatusEnum.APPROVE && CharSequenceUtil.isNotBlank(updateApproveStatusDTO.getThirdApprovalUserId())){
            SysUserThirdEntity userByThird = sysUserFeign.getUserByThird(ThirdpartyPlatformEnum.FS.getCode(), updateApproveStatusDTO.getThirdApprovalUserId());
            if (Objects.isNull(userByThird)) {
                throw new ServiceException("第三方用户信息不存在");
            }
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(userByThird.getUserId());
            if (ObjUtil.isEmpty(findUserDTO)) {
                throw new ServiceException(ApiError.ERROR_1037, userByThird.getUserId());
            }
            supplierEntity.setApproveUserId(findUserDTO.getUserId());
            supplierEntity.setApproveUserName(findUserDTO.getUserName());
            supplierEntity.setApproveTime(updateApproveStatusDTO.getThirdApproveTime());
        }
         approveEnd(supplierEntity,ApproveTypeEnum.PASS.getStatus(),"");
    }

    /**
     * @description: 更新状态
     * @author Will
     * @date: 2023/12/1 16:05
     * @param ids
     * @param approveStatus
     */
    private void updateApproveStatusForDisApprove(List<String> ids, ApproveStatusEnum approveStatus) {
        this.lambdaUpdate().in(SupplierEntity::getId, ids)
                .set(SupplierEntity::getApproveStatus, approveStatus)
                .update();
    }

    @Override
    public SupplierEntity getSupplierByUid(String uid) {
        SupplierRefUserEntity supplier = supplierRefUserService.getSupplierRelUserByUid(uid);
        if(Objects.isNull(supplier)){
            return null;
        }
        return this.getById(supplier.getSupplierId());
    }

    @Override
    public List<SupplierTabCountDTO> getTabCount() {
        List<SupplierTabCountDTO> dtos = new ArrayList<>();
        //全部
        getTotalCount(dtos);
        //待我审核
        getWaitMeApprove(dtos);
        //已审核
        getApproveCount(dtos);
        //不通过
        getRejectCount(dtos);
        return dtos;
    }

    @Override
    public List<SupplierEntity> listByPurchaseUserId(String purchaseUserId) {
        if (StringUtils.isBlank(purchaseUserId)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(SupplierEntity::getPurchaseUserId, purchaseUserId)
                .eq(SupplierEntity::getApproveStatus,ApproveStatusEnum.APPROVE.getStatus()).list();
    }

    @Override
    public List<SupplierDTO.SupplierDefaultDTO> listDefaultBySupplierIdList(List<String> supplierIdList) {
        if (CollectionUtils.isEmpty(supplierIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<SupplierDTO.SupplierDefaultDTO> list = new ArrayList<>();

        List<SupplierEntity> supplierList = this.listByIds(supplierIdList);
        if (CollectionUtils.isEmpty(supplierList)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        //联系人
        List<SupplierContactEntity> supplierContractList = supplierContactService.getDefaultBySupplierIdList(supplierIdList);

        //账号
        List<SupplierAccountEntity> supplierAccountList = supplierAccountService.listBySupplierIdList(supplierIdList);

        for (SupplierEntity supplierEntity : supplierList) {
            SupplierDTO.SupplierDefaultDTO supplierDefaultDTO = new SupplierDTO.SupplierDefaultDTO();
            supplierDefaultDTO.setSupplierId(supplierEntity.getId());
            supplierDefaultDTO.setSupplierEntity(supplierEntity);
            //联系人
            if (CollectionUtils.isNotEmpty(supplierContractList)) {
                SupplierContactEntity supplierContactEntity = supplierContractList.stream().filter(obj -> StrUtil.equals(supplierEntity.getId(), obj.getSupplierId())).findFirst().orElse(null);
                supplierDefaultDTO.setSupplierContactEntity(supplierContactEntity);
            }
            //账号
            if (CollectionUtils.isNotEmpty(supplierAccountList)) {
                SupplierAccountEntity accountEntity = supplierAccountList.stream().filter(obj -> StrUtil.equals(obj.getSupplierId(), supplierEntity.getId())).findFirst().orElse(null);
                supplierDefaultDTO.setAccountEntity(accountEntity);
            }
            list.add(supplierDefaultDTO);
        }
        return list;
    }

    private void getRejectCount(List<SupplierTabCountDTO> dtos) {
        int count = lambdaQuery().eq(SupplierEntity::getIsDeleted,false)
                .eq(SupplierEntity::getApproveStatus,ApproveStatusEnum.REJECT.getStatus())
                .count();
        dtos.add(SupplierTabCountDTO.builder().type(SupplierTabEnum.REJECT.getCode()).name(SupplierTabEnum.REJECT.getName()).count(count).build());
    }

    private void getApproveCount(List<SupplierTabCountDTO> dtos) {
        int count = lambdaQuery().eq(SupplierEntity::getIsDeleted,false)
                .eq(SupplierEntity::getApproveStatus,ApproveStatusEnum.APPROVE.getStatus())
                .count();
        dtos.add(SupplierTabCountDTO.builder().type(SupplierTabEnum.APPROVE.getCode()).name(SupplierTabEnum.APPROVE.getName()).count(count).build());
    }

    private void getWaitMeApprove(List<SupplierTabCountDTO> dtos) {
        List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.SUPPLIER.getCode());
        if (CollectionUtils.isNotEmpty(businessIds)){
            dtos.add(SupplierTabCountDTO.builder().type(SupplierTabEnum.TO_ME_CHECK_TASK.getCode()).name(SupplierTabEnum.TO_ME_CHECK_TASK.getName()).count(businessIds.size()).build());
        }else {
            dtos.add(SupplierTabCountDTO.builder().type(SupplierTabEnum.TO_ME_CHECK_TASK.getCode()).name(SupplierTabEnum.TO_ME_CHECK_TASK.getName()).count(0).build());
        }
    }

    private void getTotalCount(List<SupplierTabCountDTO> dtos) {
        int count = lambdaQuery().eq(SupplierEntity::getIsDeleted,false).count();
        dtos.add(SupplierTabCountDTO.builder().type(SupplierTabEnum.ALL_TASK.getCode()).name(SupplierTabEnum.ALL_TASK.getName()).count(count).build());
    }

    /**
     * @param entity
     * @description: 提交流程
     * @author Will
     * @date: 2023/7/3 14:39
     */
    private void startProcess(SupplierEntity entity) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SUPPLIER.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(userInfo.getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> listApiResult = workflowFeign.start(startDTO);
        if (!listApiResult.isSuccess()) {
            throw new ServiceException(listApiResult.getMsg());
        }
    }

    /**
     * variablesMap值赋值
     * @author jack
     * @date 2025-06-09
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(SupplierEntity entity) {
        CfgQueryOptionDTO.VariablesParamsDTO dto = new CfgQueryOptionDTO.VariablesParamsDTO();
        dto.setBusinessKey(CfgQueryOptionBussinessKeyEnum.SUPPLIER.getCode());
        dto.setVariablesMap(BeanUtil.beanToMap(entity));
        return cfgQueryOptionFeign.getVariablesMapByBusinessKey(dto);
    }

    /**
     * 更改状态
     */
    private Boolean updateApproveStatus(List<SupplierEntity> list, ApproveStatusEnum statusEnum) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (CollectionUtils.isNotEmpty(list)) {
            list.stream().forEach(obj -> {
                if (ApproveStatusEnum.APPROVE.equals(statusEnum) || ApproveStatusEnum.REJECT.equals(statusEnum)) {
                    obj.setApproveTime(ObjectUtil.isEmpty(obj.getApproveTime()) ? LocalDateTime.now() : obj.getApproveTime());
                    obj.setApproveUserId(CharSequenceUtil.isBlank(obj.getApproveUserId()) ? userInfo.getUid() : obj.getApproveUserId());
                    obj.setApproveUserName(CharSequenceUtil.isBlank(obj.getApproveUserName()) ? userInfo.getUserName() : obj.getApproveUserName());
                } else {
                    obj.setApproveTime(null);
                    obj.setApproveUserId("");
                    obj.setApproveUserName("");
                }
                obj.setApproveStatus(statusEnum);
            });
            return this.updateBatchById(list);
        }
        return true;

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
    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param list
     */
    private void sendPushTask (List<SupplierEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSupplierService.syncDataToKingdee(obj, operate);
            resultList.add(pushTaskEntity);
        });
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
    }
    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param entity
     */
    private void sendSinglePushTask(SupplierEntity entity, String operate) {
        //审核通过发送金蝶
        DmpPushTaskEntity pushTaskEntity = syncKingdeeSupplierService.syncDataToKingdee(entity, operate);
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(Collections.singletonList(pushTaskEntity));
            }
        });
    }

}
