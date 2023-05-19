package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.constant.ApproveType;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetiailDTO;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.SoOutstockMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.SoOutstockService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售订单出库单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
@Slf4j
public class SoOutstockServiceImpl extends SuperServiceImpl<SoOutstockMapper, SoOutstockEntity> implements SoOutstockService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Override
    public List<SoOutstockEntity> listBySourceId(List<String> ids) {
        return lambdaQuery().in(SoOutstockEntity::getSourceId, ids).list();
    }


    /**
     * 添加销售出库单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-19 9:50
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(SoOutstockDTO.AddDTO dto) {
        //TODO 对应检查数量
        String id = IdWorker.getIdStr();
        SoOutstockEntity soOutstock = new SoOutstockEntity();
        BeanMapper.copy(dto, soOutstock);
        soOutstock.setId(id);
        List<SoOutstockDetiailDTO.AddDTO> detailList = dto.getDetailList();
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.XSCK, BusinessNoTypeEnum.CODE_XSCK.getCode()));
        soOutstock.setCode(code);
        //发货组织
        String deliveryOrgId = dto.getDeliveryOrgId();
        //仓库id
        String warehouseId = dto.getWarehouseId();
        //仓管员
        String warehouseKeeperId = dto.getWarehouseKeeperId();
        if (StringUtils.isNotBlank(warehouseKeeperId)) {
            //用户信息
            FindUserDTO userInfo = sysUserFeign.getUserByUserId(warehouseKeeperId);
            if (userInfo != null) {
                soOutstock.setWarehouseKeeperName(userInfo.getUserName());
            }
        }
        //组织列表
        List<BaseIdDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(deliveryOrgId));
        String salesOrgName = orgList.stream().filter(d -> d.getId().equals(deliveryOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        soOutstock.setDeliveryOrgName(salesOrgName);
        WarehouseEntity warehouse = warehouseService.getById(warehouseId);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        soOutstock.setWarehouseName(warehouse.getName());
        Boolean addResult = this.save(soOutstock);
        //添加成功
        if (addResult) {
            soOutstockDetailService.add(id, detailList);
            //添加日志
            String content = String.format("新增了一个{%s}-销售出库单-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), id, "新增操作");
            return id;
        }

        return "";
    }

    /**
     * 批量提交
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 10:34
     */
    @Override
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<SoOutstockEntity> list = this.listByIds(ids);
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

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().getStatus().equals(rejectStatus)).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(ingStatus));
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "状态变更");
            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.SO_OUT_STOCK.getCode(), rejectPairList, "状态变更");
        }
        return result;
    }


    /**
     * 新增并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 10:42
     */
    @Override
    public Boolean addAndSubmit(SoOutstockDTO.AddDTO dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Arrays.asList(id));
        return result;
    }


    /**
     * 销售出库单详情
     *
     * @param id
     * @return com.erp.model.wms.dto.SoOutstockDTO.ViewDTO
     * @author yl
     * @date 2023-05-19 10:45
     */
    @Override
    public SoOutstockDTO.ViewDTO view(String id) {
        SoOutstockEntity soOutstock = this.getById(id);
        if (Objects.isNull(soOutstock)) {
            throw new ServiceException(ApiError.ERROR_99058);
        }
        SoOutstockDTO.ViewDTO result = new SoOutstockDTO.ViewDTO();
        BeanMapper.copy(soOutstock, result);
        String soId = soOutstock.getSoId();
        SoInfoDTO.CustomerDTO soInfo = soInfoFeign.getSoBaseById(soId);
        if (soInfo != null) {
            result.setCustomerName(soInfo.getCustomerName());
            result.setSoCode(soInfo.getCode());
            result.setReceiveAddress(soInfo.getReceiveAddress());
            result.setReceiverName(soInfo.getReceiverName());
            result.setDeliveryModeName(soInfo.getDeliveryModeName());
            result.setSalesOrgName(soInfo.getSalesOrgName());
            result.setRequireDate(soInfo.getRequireDate());
            result.setTelNumber(soInfo.getTelNumber());
            result.setTypeName(soInfo.getTypeName());
            result.setSellerName(soInfo.getSellerName());
        }
        List<SoOutstockDetiailDTO.ViewDTO> detailList = soOutstockDetailService.listByMainId(id);
        result.setDetailList(detailList);
        return result;
    }


    /**
     * 审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 11:42
     */
    @Override
    public Boolean approve(BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<SoOutstockEntity> list = this.listByIds(ids);
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
        //TODO 需要做什么 释放冻结 销售订单的发货状态
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
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "状态变更");
        }

        return result;
    }


    /**
     * 反审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 12:10
     */
    @Override
    public Boolean disApprove(BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<SoOutstockEntity> list = this.listByIds(ids);
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
        //反审核 TODO 需要做什么
        if (result) {
            //添加日志
            String ingContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE_ING.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.batchAddModuleOperateLog(ingContent, ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "状态变更");
            //审核通过
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), rejectPairList, "状态变更");

        }
        return result;
    }

    /**
     * 撤销流程
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 12:13
     */
    @Override
    public Boolean cancelProcess(List<String> ids) {
        List<SoOutstockEntity> list = this.listByIds(ids);
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //TODO 撤销流程
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("销售出库单【%s】取消流程", ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "取消流程操作");
        return result;

    }


    /**
     * 删除销售出库单
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 12:16
     */
    @Override
    public Boolean delete(List<String> ids) {
        List<SoOutstockEntity> list = this.listByIds(ids);
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        long count = list.stream().filter(s -> !s.getApproveStatus().getStatus().equals(waitSubmitStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        long invalidCount = list.stream().filter(s -> s.getInvalidStatus()).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        Boolean result = this.removeByIds(ids);
        if (result) {
            //添加日志
            String content = "删除销售订单[%s]";
            List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "删除");
            //删除明细
            soOutstockDetailService.removeByMainIdList(ids);

        }
        return result;
    }


    /**
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        operateLogService.addModuleOperateLog(content, code, businessId, operation);
    }


    /**
     * 更改状态
     *
     * @param list
     * @param statusEnum
     * @return
     */
    private Boolean updateApproveStatus(List<SoOutstockEntity> list, ApproveStatusEnum statusEnum) {
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(s -> s.setApproveStatus(statusEnum));
            return this.updateBatchById(list);
        }
        return true;
    }

}
