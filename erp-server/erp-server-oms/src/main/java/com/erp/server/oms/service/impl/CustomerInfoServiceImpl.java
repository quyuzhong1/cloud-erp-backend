package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DataPermission;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.SearchType;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.CustomerGroupEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.CustomerInfoMapper;
import com.erp.server.oms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
        //分组id
        String groupId = dto.getGroupId();
        //获取客户分组信息
        List<CustomerGroupEntity> customerGroupList = customerGroupService.listById(groupId);
        String gradeName = customerGroupList.stream().filter(d -> d.getId().equals(groupId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        addEntity.setGroupName(gradeName);
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
        List<CustomerInfoEntity> list = this.list();

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
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);

        List<CustomerDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        List<String> groupIdList = list.stream().map(CustomerDTO.PagingViewDTO::getGroupId).collect(Collectors.toList());
        List<CustomerGroupEntity> groupList = customerGroupService.listByIds(groupIdList);
        for (CustomerDTO.PagingViewDTO item : list) {
            String groupId = item.getGroupId();
            String groupName = groupList.stream().filter(g -> groupId.equals(g.getId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setGroupName(groupName);
            ApproveStatusEnum approveStatusEnum = item.getApproveStatus();
            item.setApproveStatusName(approveStatusEnum.getName());
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
        if(Objects.isNull(customer)){

        }

        return null;
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
