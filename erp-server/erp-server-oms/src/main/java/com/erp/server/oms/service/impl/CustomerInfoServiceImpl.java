package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.ApproveType;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.SearchType;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.CustomerGroupEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.DictGlobalAreaDTO;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.constant.OmsConstant;
import com.erp.server.oms.mapper.CustomerInfoMapper;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
@Slf4j
public class CustomerInfoServiceImpl extends SuperServiceImpl<CustomerInfoMapper, CustomerInfoEntity> implements CustomerInfoService {


    @Resource
    private CustomerContactService customerContactService;

    @Resource
    private CustomerAddressService customerAddressService;

    @Resource
    private CustomerInvoiceService customerInvoiceService;

    @Resource
    private CustomerSellerService customerSellerService;

    @Resource
    private CustomerGroupService customerGroupService;

    @Resource
    private OmsAttachmentService omsAttachmentService;


    @Resource
    private OperateLogService operateLogService;


    @Resource
    private SysUserFeign sysUserFeign;

    /**
     * 获取到分组的id 集合
     *
     * @param
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-11 18:10
     */
    @Override
    public List<String> listGroup() {
        List<CustomerInfoEntity> list = this.list();
        return list.stream().map(CustomerInfoEntity::getGroupId).distinct().collect(Collectors.toList());
    }


    /**
     * 添加客户信息
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-12 10:30
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(CustomerDTO.AddDTO dto) {
        //检查名称
        checkName(null, dto.getName());
        //客户联系人
        List<CustomerContactDTO.AddDTO> contactList = dto.getContactList();
        //检查联系人默认是否多个
        customerContactService.checkIsDefault(contactList);

        //检查默认地址是否多个
        List<CustomerAddressDTO.AddDTO> addressList = dto.getAddressList();
        customerAddressService.checkIsDefault(addressList);

        //检查默认发票 银行账号
        List<InvoiceDTO.AddDTO> invoiceList = dto.getInvoiceList();
        customerInvoiceService.checkIsDefault(invoiceList);
        //销售员信息
        List<SellerDTO.AddDTO> sellerList = dto.getSellerList();
        customerSellerService.checkDate(sellerList);

        //id
        String id = IdWorker.getIdStr();
        CustomerInfoEntity addEntity = new CustomerInfoEntity();
        BeanMapper.copy(dto, addEntity);
        addEntity.setId(id);
        //国家id
        String countryId = dto.getCountryId();

        addEntity.setAreaId(countryId);
        //分组id
        String groupId = dto.getGroupId();
        //付款方
        List<String> payCodeList = dto.getPayCodeList();
        String payCode = CollectionUtils.isNotEmpty(payCodeList) ? payCodeList.stream().collect(Collectors.joining(",")) : "";
        addEntity.setCode(payCode);
        //获取客户分组信息
        List<CustomerGroupEntity> customerGroupList = customerGroupService.listById(groupId);
        String groupName = customerGroupList.stream().filter(d -> d.getId().equals(groupId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        addEntity.setGroupName(groupName);
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.CUST, BusinessNoTypeEnum.CODE_CUST.getCode()));
        addEntity.setCode(code);
        //对应组织
        String innerOrgId = dto.getInnerOrgId();

        //使用组织
        String useOrgId = dto.getUseOrgId();
        //组织列表
        List<BaseIdDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(innerOrgId, useOrgId));

        String innerOrgName = orgList.stream().filter(d -> d.getId().equals(innerOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        addEntity.setInnerOrgName(innerOrgName);

        String useOrgName = orgList.stream().filter(d -> d.getId().equals(useOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        addEntity.setUseOrgName(useOrgName);
        //保存客户
        Boolean addResult = this.save(addEntity);
        if (addResult) {
            Class<CustomerInfoEntity> customerClass = CustomerInfoEntity.class;
            TableName tableName = customerClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //保存附件
            omsAttachmentService.batchSave(dto.getAttachUrlList(), dto.getAttachNameList(), type, id);
            //添加日志
            String content = String.format("新增了一个{%s}-客户-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SUPPLIER.getCode(), id, "新增操作");

            //批量保存联系人信息
            customerContactService.saveBatchContact(id, contactList);

            //批量保存地址信息
            customerAddressService.saveBatchAddress(id, addressList);

            //批量保存发票信息
            customerInvoiceService.saveBatchInvoice(id, invoiceList);

            //批量销售员信息
            customerSellerService.saveBatchSeller(id, sellerList);

            return id;

        }

        return "";
    }


    /**
     * 提交
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-12 16:47
     */
    @Override
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<CustomerInfoEntity> list = this.listByIds(ids);

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
        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().getStatus().equals(waitSubmitStatus)).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(rejectStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(ingStatus));
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.CUSTOMER.getCode(), pairList, "状态变更");
            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.CUSTOMER.getCode(), rejectPairList, "状态变更");
        }
        return result;

    }


    /**
     * 获取tab list
     *
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CustomerDTO.TabListDTO>
     * @author yl
     * @date 2023-05-12 17:01
     */
    @Override
    public List<CustomerDTO.TabListDTO> tabList() {
        List<CustomerDTO.TabListDTO> resultList = new ArrayList<>(4);
        //全部
        List<CustomerInfoEntity> list = this.list();
        CustomerDTO.TabListDTO all = new CustomerDTO.TabListDTO();
        all.setCount(list.size());
        all.setSearchType(SearchType.ALL);
        resultList.add(all);

        //待审核
        ApproveStatusEnum ing = ApproveStatusEnum.getByStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
        CustomerDTO.TabListDTO waitApprove = new CustomerDTO.TabListDTO();
        waitApprove.setCount((int) list.stream().filter(l -> ing.equals(l.getApproveStatus())).count());
        waitApprove.setSearchType(SearchType.WAIT_APPROVE);
        resultList.add(waitApprove);

        //已审核
        ApproveStatusEnum approveStatus = ApproveStatusEnum.getByStatus(ApproveStatusEnum.APPROVE.getStatus());
        CustomerDTO.TabListDTO approve = new CustomerDTO.TabListDTO();
        approve.setCount((int) list.stream().filter(l -> approveStatus.equals(l.getApproveStatus())).count());
        approve.setSearchType(ApproveStatusEnum.APPROVE.getStatus());
        resultList.add(approve);

        //审核不通过
        ApproveStatusEnum rejectStatus = ApproveStatusEnum.getByStatus(ApproveStatusEnum.REJECT.getStatus());
        CustomerDTO.TabListDTO reject = new CustomerDTO.TabListDTO();
        reject.setCount((int) list.stream().filter(l -> rejectStatus.equals(l.getApproveStatus())).count());
        reject.setSearchType(ApproveStatusEnum.REJECT.getStatus());
        resultList.add(reject);
        return resultList;
    }

    /**
     * 分页信息
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.CustomerDTO.PagingViewDTO>
     * @author yl
     * @date 2023-05-12 17:21
     */
    @Override
    public PagingVO<CustomerDTO.PagingViewDTO> paging(PagingDTO<CustomerDTO.PagingParamDTO> dto) {
        CustomerDTO.PagingParamDTO params = dto.getParams();
        params.setParam(dto.getParam());
        String searchType = params.getSearchType();
        List<String> approveList = new ArrayList<>();
        //待审核
        if (OmsConstant.WAIT_APPROVE.equals(searchType)) {
            approveList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
        }

        //已审核
        if (OmsConstant.APPROVE.equals(searchType)) {
            approveList.add(ApproveStatusEnum.APPROVE.getStatus());
        }

        //审核不通过
        if (OmsConstant.REJECT.equals(searchType)) {
            approveList.add(ApproveStatusEnum.REJECT.getStatus());
        }

        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params, approveList);
        List<CustomerDTO.PagingViewDTO> list = pageData.getRecords();
        List<String> groupIdList = list.stream().map(CustomerDTO.PagingViewDTO::getGroupId).collect(Collectors.toList());
        List<CustomerGroupEntity> groupList = CollectionUtils.isNotEmpty(groupIdList) ? customerGroupService.listByIds(groupIdList) : Collections.emptyList();
        for (CustomerDTO.PagingViewDTO item : list) {
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            String groupId = item.getGroupId();
            String groupName = groupList.stream().filter(g -> g.getId().equals(groupId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setGroupName(groupName);
        }

        return new PagingVO<>(pageData);
    }

    /**
     * 新增并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 9:21
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(CustomerDTO.AddDTO dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Arrays.asList(id));
        return result;

    }


    /**
     * 客户详情
     *
     * @param id
     * @return com.erp.model.oms.dto.CustomerDTO.ViewDTO
     * @author yl
     * @date 2023-05-15 9:24
     */
    @Override
    public CustomerDTO.ViewDTO view(String id) {
        CustomerDTO.ViewDTO view = new CustomerDTO.ViewDTO();
        CustomerInfoEntity customer = this.getById(id);
        if (Objects.isNull(customer)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }
        BeanMapper.copy(customer, view);
        String areaId = customer.getAreaId();
        String payCode = customer.getPayCode();
        view.setPayCodeList(StringUtils.isNotBlank(payCode)?Arrays.asList(payCode.split(",")):Collections.emptyList());
        List<DictGlobalAreaDTO.InfoDTO> globalAreaList = sysUserFeign.listGlobalAreaByCountryIds(Arrays.asList(areaId));
        String regionName = globalAreaList.stream().filter(d -> d.getId().equals(areaId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getRegionName())).orElse("");
        view.setAreaName(regionName);
        view.setApproveStatusName(customer.getApproveStatus().getName());
        List<OmsAttachmentDTO.UpdateDTO> attachmentList = omsAttachmentService.getByBusinessIds(Arrays.asList(id));
        List<String> attachmentUrlList = attachmentList.stream().
                map(OmsAttachmentDTO.UpdateDTO::getAttachUrl).
                collect(Collectors.toList());
        List<String> attachmentNameList = attachmentList.stream().
                map(OmsAttachmentDTO.UpdateDTO::getAttachName).
                collect(Collectors.toList());
        view.setAttachUrlList(attachmentUrlList);
        view.setAttachNameList(attachmentNameList);
        //联系人信息
        List<CustomerContactDTO.ViewDTO> contactList = customerContactService.listByMainId(id);
        view.setContactList(contactList);

        //地址信息
        List<CustomerAddressDTO.ViewDTO> addressList = customerAddressService.listByMainId(id);
        view.setAddressList(addressList);

        //发票信息
        List<InvoiceDTO.ViewDTO> invoiceList = customerInvoiceService.listByMainId(id);
        view.setInvoiceList(invoiceList);

        //销售员信息
        List<SellerDTO.ViewDTO> sellerList = customerSellerService.listByMainId(id);
        view.setSellerList(sellerList);

        return view;
    }


    /**
     * 修改客户信息
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-15 10:39
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateCustomer(CustomerDTO.UpdateDTO dto) {
        String id = dto.getId();
        CustomerInfoEntity customer = this.getById(id);
        if (Objects.isNull(customer)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }

        //检查名称
        checkName(id, dto.getName());
        //客户联系人
        List<CustomerContactDTO.ViewDTO> contactList = dto.getContactList();
        List<CustomerContactDTO.AddDTO> contactAddList = BeanMapper.copyList(contactList, CustomerContactDTO.AddDTO.class);
        //检查联系人默认是否多个
        customerContactService.checkIsDefault(contactAddList);

        //检查默认地址是否多个
        List<CustomerAddressDTO.ViewDTO> addressList = dto.getAddressList();
        List<CustomerAddressDTO.AddDTO> addressAddList = BeanMapper.copyList(addressList, CustomerAddressDTO.AddDTO.class);
        customerAddressService.checkIsDefault(addressAddList);

        //检查默认发票 银行账号
        List<InvoiceDTO.ViewDTO> invoiceList = dto.getInvoiceList();
        List<InvoiceDTO.AddDTO> invoiceAddList = BeanMapper.copyList(invoiceList, InvoiceDTO.AddDTO.class);
        customerInvoiceService.checkIsDefault(invoiceAddList);

        //销售员信息
        List<SellerDTO.ViewDTO> sellerList = dto.getSellerList();
        List<SellerDTO.AddDTO> sellerAddList = BeanMapper.copyList(sellerList, SellerDTO.AddDTO.class);
        customerSellerService.checkDate(sellerAddList);

        String code = customer.getCode();

        //旧的
        CustomerInfoEntity old = new CustomerInfoEntity();
        BeanMapper.copy(customer, old);

        BeanMapper.copy(dto, customer);

        //分组id
        String groupId = dto.getGroupId();
        //付款方
        List<String> payCodeList = dto.getPayCodeList();
        String payCode = CollectionUtils.isNotEmpty(payCodeList) ? payCodeList.stream().collect(Collectors.joining(",")) : "";
        customer.setPayCode(payCode);
        customer.setAreaId(dto.getCountryId());
        customer.setCode(code);
        //获取客户分组信息
        List<CustomerGroupEntity> customerGroupList = customerGroupService.listById(groupId);
        String groupName = customerGroupList.stream().filter(d -> d.getId().equals(groupId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        customer.setGroupName(groupName);

        //对应组织
        String innerOrgId = dto.getInnerOrgId();

        //使用组织
        String useOrgId = dto.getUseOrgId();
        //组织列表
        List<BaseIdDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(innerOrgId, useOrgId));

        String innerOrgName = orgList.stream().filter(d -> d.getId().equals(innerOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        customer.setInnerOrgName(innerOrgName);

        String useOrgName = orgList.stream().filter(d -> d.getId().equals(useOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        customer.setUseOrgName(useOrgName);
        Boolean updateResult = this.updateById(customer);
        if (updateResult) {

            /**
             * 添加修改日志
             */
            operateLogService.addModuleOperateLogByObj(old, customer, ModuleTypeEnum.CUSTOMER.getCode(), id, "", "");

            Class<CustomerInfoEntity> customerClass = CustomerInfoEntity.class;
            TableName tableName = customerClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //修改附件
            omsAttachmentService.batchSave(dto.getAttachUrlList(), dto.getAttachNameList(), type, id);

            //批量修改联系人信息
            customerContactService.updateBatchContact(id, dto.getContactList());

            //批量修改地址信息
            customerAddressService.updateBatchAddress(id, dto.getAddressList());

            //批量修改发票信息
            customerInvoiceService.updateBatchInvoice(id, dto.getInvoiceList());

            //批量修改销售员信息
            customerSellerService.updateBatchSeller(id, dto.getSellerList());

            return id;
        }


        return "";
    }

    /**
     * 修改并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(CustomerDTO.UpdateDTO dto) {
        String id = this.updateCustomer(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(id));
    }


    /**
     * 审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:17
     */
    @Override
    public Boolean approve(BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<CustomerInfoEntity> list = this.listByIds(ids);
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !ingStatus.equals(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        String ingStatusName = ApproveStatusEnum.APPROVE_ING.getName();
        //意见
        String comment = dto.getComment();
        Boolean result = true;
        String content = "";
        if (dto.getType().equals(ApproveType.PASS)) {
            //审核通过
            String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
            result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(approveStatus));
            content = String.format("状态由[%s]变更为[%s] , 意见:%s", ingStatusName, ApproveStatusEnum.APPROVE.getName(), comment);
        } else {
            //审核不通过
            String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
            result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(rejectStatus));
            content = String.format("状态由[%s]变更为[%s] 【不通过原因:%s】", ingStatusName, ApproveStatusEnum.REJECT.getName(), comment);
        }
        if (result) {
            //添加日志
            List<Pair<String, String>> pairList = list.stream().
                    map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.CUSTOMER.getCode(), pairList, "状态变更");
        }

        return result;
    }


    /**
     * 反审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:25
     */
    @Override
    public Boolean disApprove(List<String> ids) {
        List<CustomerInfoEntity> list = this.listByIds(ids);
        //审核中
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();

        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(approveIngStatus);
        statusList.add(approveStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(approveIngStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(approveStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus));
        //反审核
        if (result) {
            //添加日志
            String ingContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE_ING.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.batchAddModuleOperateLog(ingContent, ModuleTypeEnum.CUSTOMER.getCode(), pairList, "状态变更");
            //审核通过
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.CUSTOMER.getCode(), rejectPairList, "状态变更");

        }
        return result;
    }

    /**
     * 删除客户
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:30
     */
    @Override
    public Boolean deleteByIds(List<String> ids) {
        List<CustomerInfoEntity> list = this.listByIds(ids);
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        long count = list.stream().filter(s -> !s.getApproveStatus().getStatus().equals(waitSubmitStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        //TODO 检查能否删除

        //删除客户
        Boolean result = this.removeByIds(ids);
        if (result) {
            //添加日志
            String content = "删除供应商[%s]";
            List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.CUSTOMER.getCode(), pairList, "删除");
        }
        return result;
    }


    /**
     * 导出 客户列表
     *
     * @param dto
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:53
     */
    @Override
    public Boolean exportExcel(CustomerDTO.ExportDTO dto, HttpServletResponse response) {
        List<CustomerDTO.PagingViewDTO> list = baseMapper.listExport(dto);
        for (CustomerDTO.PagingViewDTO item : list) {
            Boolean disabled = item.getDisabled();
            String disabledName = disabled ? "停用" : "启用";
            item.setDisabledName(disabledName);
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
        }
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/CustomerExport.xlsx";
        String name = "客户列表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("客户列表导出出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;


    }

    @Override
    public List<CustomerDTO.InfoDTO> listCustomer() {
        LambdaQueryWrapper<CustomerInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(CustomerInfoEntity::getId,
                CustomerInfoEntity::getCode,
                CustomerInfoEntity::getName,
                CustomerInfoEntity::getDisabled);
        String approve = ApproveStatusEnum.APPROVE.getStatus();
        ApproveStatusEnum approveStatusEnum = ApproveStatusEnum.getByStatus(approve);
        queryWrapper.eq(CustomerInfoEntity::getApproveStatus, approveStatusEnum);
        List<CustomerInfoEntity> list = this.list(queryWrapper);
        return BeanMapper.copyList(list, CustomerDTO.InfoDTO.class);
    }


    /**
     * 启用或者停用客户
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 15:30
     */
    @Override
    public Boolean updateStatus(UpdateStateDTO.BatchUpdateDTO dto) {
        List<String> ids = dto.getIds();
        List<CustomerInfoEntity> customerList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(customerList)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }
        Boolean disabled = dto.getDisabled();
        long count = customerList.stream().filter(d -> !d.getDisabled() == disabled).count();
        if (count != customerList.size()) {
            throw new ServiceException(ApiError.ERROR_98027);
        }
        customerList.forEach(d -> d.setDisabled(disabled));
        //添加日志
        List<Pair<String, String>> pairList = customerList.stream().
                map(obj -> new Pair<>(obj.getId(), obj.getName())).collect(Collectors.toList());
        String content = String.format("启用状态[%s]变更为[%s]", disabled ? "启用" : "停用", disabled ? "停用" : "启用");
        String finalContent = "[%s]," + content;
        operateLogService.batchAddModuleOperateLog(finalContent, ModuleTypeEnum.CUSTOMER.getCode(), pairList, "状态变更");

        return this.updateBatchById(customerList);


    }


    /**
     * 撤销流程
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 15:39
     */
    @Override
    public Boolean cancelProcess(List<String> ids) {
        List<CustomerInfoEntity> list = this.listByIds(ids);
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //TODO 撤销流程
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("客户【%s】取消流程", ModuleTypeEnum.CUSTOMER.getCode(), pairList, "取消流程操作");
        return result;
    }


    /**
     * 获取启用的列表
     *
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CustomerDTO.InfoDTO>
     * @author yl
     * @date 2023-05-15 16:05
     */
    @Override
    public List<CustomerDTO.InfoDTO> listEnable() {
        LambdaQueryWrapper<CustomerInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(CustomerInfoEntity::getId,
                CustomerInfoEntity::getCode,
                CustomerInfoEntity::getName,
                CustomerInfoEntity::getDisabled);
        String approve = ApproveStatusEnum.APPROVE.getStatus();
        ApproveStatusEnum approveStatusEnum = ApproveStatusEnum.getByStatus(approve);
        queryWrapper.eq(CustomerInfoEntity::getApproveStatus, approveStatusEnum);
        queryWrapper.eq(CustomerInfoEntity::getDisabled, Boolean.FALSE);
        List<CustomerInfoEntity> list = this.list(queryWrapper);
        return BeanMapper.copyList(list, CustomerDTO.InfoDTO.class);
    }


    /**
     * 获取客户的默认联系人
     *
     * @param customerId
     * @return com.erp.model.oms.dto.CustomerDTO.BaseDTO
     * @author yl
     * @date 2023-05-15 16:15
     */
    @Override
    public CustomerDTO.BaseDTO getBase(String customerId) {
        CustomerDTO.BaseDTO base = new CustomerDTO.BaseDTO();
        CustomerInfoEntity customer = this.getById(customerId);
        if (Objects.isNull(customer)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }
        base.setId(customer.getId());
        base.setCode(customer.getCode());
        List<CustomerContactDTO.ViewDTO> contactList = customerContactService.listByMainId(customerId);
        CustomerContactDTO.ViewDTO contact = contactList.stream().filter(c -> c.getIsDefault()).findFirst().orElse(null);
        if (contact != null) {
            base.setPerson(contact.getPerson());
            base.setTelNumber(contact.getTelNumber());
        }

        List<CustomerAddressDTO.ViewDTO> addressList = customerAddressService.listByMainId(customerId);
        CustomerAddressDTO.ViewDTO address = addressList.stream().filter(c -> c.getIsDefault()).findFirst().orElse(null);
        if (address != null) {
            base.setAddress(address.getAddress());
        }
        return base;
    }

    private Boolean updateApproveStatus(List<CustomerInfoEntity> list, ApproveStatusEnum statusEnum) {
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(s -> s.setApproveStatus(statusEnum));
            return this.updateBatchById(list);
        }
        return true;
    }


    /**
     * 检查名称
     *
     * @param id
     * @param name
     * @return void
     * @author yl
     * @date 2023-05-12 15:08
     */
    private void checkName(String id, String name) {
        LambdaQueryWrapper<CustomerInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(CustomerInfoEntity::getId, id);
        }
        queryWrapper.eq(CustomerInfoEntity::getName, name);
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_1028);
        }
    }


    /**
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        operateLogService.addModuleOperateLog(content, code, businessId, operation);
    }
}
