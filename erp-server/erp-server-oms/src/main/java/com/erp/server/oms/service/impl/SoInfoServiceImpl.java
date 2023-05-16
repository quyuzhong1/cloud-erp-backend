package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.mapper.SoInfoMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售订单信息 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class SoInfoServiceImpl extends SuperServiceImpl<SoInfoMapper, SoInfoEntity> implements SoInfoService {


    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private OperateLogService operateLogService;

    /**
     * 添加销售订单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-15 16:28
     */
    @Override
    public String add(SoInfoDTO.AddDTO dto) {
        //id
        String id = IdWorker.getIdStr();
        SoInfoEntity addEntity = new SoInfoEntity();
        BeanMapper.copy(dto, addEntity);
        addEntity.setId(id);
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.XSD, BusinessNoTypeEnum.CODE_XSD.getCode()));
        addEntity.setCode(code);
        //销售组织
        String salesOrgId = dto.getSalesOrgId();
        //销售员
        String sellerId = dto.getSellerId();
        //用户信息
        FindUserDTO userInfo = sysUserFeign.getUserByUserId(sellerId);
        if (userInfo != null) {
            addEntity.setSellerName(userInfo.getUserName());
        }

        //仓库id
        String warehouseId = dto.getWarehouseId();
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
        String warehouseOrgId = "";
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            warehouseOrgId = warehouseList.get(0).getOrgId();
        }
        //组织列表
        List<BaseIdDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(salesOrgId, warehouseOrgId));
        String salesOrgName = orgList.stream().filter(d -> d.getId().equals(salesOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        addEntity.setSalesOrgName(salesOrgName);
        String finalWarehouseOrgId = warehouseOrgId;
        String warehouseOrgName = orgList.stream().filter(d -> d.getId().equals(finalWarehouseOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        addEntity.setWarehouseOrgId(warehouseOrgId);
        addEntity.setWarehouseOrgName(warehouseOrgName);
        //保存成功
        Boolean addResult = this.save(addEntity);
        if (addResult) {
            //添加明细
            soDetailService.addSoDetail(id, dto.getDetailList());
            //添加日志
            String content = String.format("新增了一个{%s}-销售单-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), id, "新增操作");
            return id;

        }


        return "";
    }


    /**
     * 提交
     * @author yl
     * @date 2023-05-16 14:41
     * @param ids
     * @return java.lang.Boolean
     */
    @Override
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<SoInfoEntity> list=this.listByIds(ids);
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
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), pairList, "状态变更");
            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.CUSTOMER.getCode(), rejectPairList, "状态变更");
        }
        return result;

    }


    /**
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        operateLogService.addModuleOperateLog(content, code, businessId, operation);
    }


    private Boolean updateApproveStatus(List<SoInfoEntity> list, ApproveStatusEnum statusEnum) {
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(s -> s.setApproveStatus(statusEnum));
            return this.updateBatchById(list);
        }
        return true;
    }
}
