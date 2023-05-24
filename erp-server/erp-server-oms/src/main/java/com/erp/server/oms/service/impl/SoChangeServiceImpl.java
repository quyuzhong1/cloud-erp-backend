package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.SearchType;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BillApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.SoChangeDTO;
import com.erp.model.oms.entity.SoChangeEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.SoChangeMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoChangeDetailService;
import com.erp.server.oms.service.SoChangeService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
 * 销售订单变更 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class SoChangeServiceImpl extends SuperServiceImpl<SoChangeMapper, SoChangeEntity> implements SoChangeService {


    @Resource
    private SysUserFeign sysUserFeign;


    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SoChangeDetailService soChangeDetailService;

    /**
     * 添加销售订单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-18 11:54
     */
    @Override
    public String add(SoChangeDTO.AddDTO dto) {
        String id = IdWorker.getIdStr();
        SoChangeEntity soChange = new SoChangeEntity();
        BeanMapper.copy(dto, soChange);
        String useId = dto.getUseId();
        String deptId = dto.getDeptId();
        String useName = "";
        String deptName = "";
        if (StringUtils.isEmpty(useId)) {
            FindUserDTO userInfo = sysUserFeign.getUserByUserId(useId);
            if (userInfo != null) {
                useName = userInfo.getUserName();
            }
        }
        if (StringUtils.isNotBlank(deptId)) {
            SysDepartmentDTO dept = sysUserFeign.getUserDeptById(deptId);
            if (dept != null) {
                deptName = dept.getName();
            }
        }
        soChange.setDeptName(deptName);
        soChange.setUserName(useName);
        soChange.setId(id);
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.XSBG, BusinessNoTypeEnum.CODE_XSBG.getCode()));
        soChange.setCode(code);
        Boolean addResult = this.save(soChange);
        if (addResult) {
            //添加日志
            soChangeDetailService.addDetailList(id, dto.getDetailList());
            String content = String.format("新增了一个{%s}-销售变更单-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SO_CHANGE.getCode(), id, "新增操作");
            return id;
        }
        return "";
    }


    /**
     * 提交审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-24 14:45
     */
    @Override
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<SoChangeEntity> list = this.listByIds(ids);
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
            String content = String.format("状态由[%s]变更为[%s]", BillApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), pairList, "状态变更");
            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.SO.getCode(), rejectPairList, "状态变更");
        }
        return result;

    }


    /**
     * 新增并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-24 14:53
     */
    @Override
    public Boolean addAndSubmit(SoChangeDTO.AddDTO dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Arrays.asList(id));
        return result;
    }


    /**
     * 获取tab 列表
     *
     * @param
     * @return java.util.List<com.erp.model.oms.dto.SoChangeDTO.TabListDTO>
     * @author yl
     * @date 2023-05-24 14:56
     */
    @Override
    public List<SoChangeDTO.TabListDTO> tabList() {
        List<SoChangeDTO.TabListDTO> resultList = new ArrayList<>(4);
        List<SoChangeDTO.ApproveCountDTO> approveCountList = baseMapper.listApproveCount();
        int allCount = approveCountList.stream().mapToInt(SoChangeDTO.ApproveCountDTO::getCount).sum();
        SoChangeDTO.TabListDTO all = new SoChangeDTO.TabListDTO();
        all.setCount(allCount);
        all.setSearchType(SearchType.ALL);
        resultList.add(all);

        //待审核
        String ing = ApproveStatusEnum.APPROVE_ING.getStatus();
        SoChangeDTO.TabListDTO waitApprove = new SoChangeDTO.TabListDTO();
        int waitApproveCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(ing)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        waitApprove.setCount(waitApproveCount);
        waitApprove.setSearchType(SearchType.WAIT_APPROVE);
        resultList.add(waitApprove);

        //已审核
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        SoChangeDTO.TabListDTO approve = new SoChangeDTO.TabListDTO();
        int approveCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(approveStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        approve.setCount(approveCount);
        approve.setSearchType(approveStatus);
        resultList.add(approve);
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        SoChangeDTO.TabListDTO reject = new SoChangeDTO.TabListDTO();
        int rejectCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(rejectStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        reject.setCount(rejectCount);
        reject.setSearchType(rejectStatus);
        resultList.add(reject);
        return resultList;
    }

    private Boolean updateApproveStatus(List<SoChangeEntity> list, ApproveStatusEnum statusEnum) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (SoChangeEntity item : list) {
                item.setApproveStatus(statusEnum);
            }
            return this.updateBatchById(list);
        }
        return Boolean.TRUE;
    }


    /**
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        operateLogService.addModuleOperateLog(content, code, businessId, operation);
    }
}
