package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.SearchType;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.TransferInDTO;
import com.erp.model.wms.dto.TransferInDetailDTO;
import com.erp.model.wms.entity.TransferInEntity;
import com.erp.model.wms.entity.TransferOutDetailEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.TransferInMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.TransferInDetailService;
import com.erp.server.wms.service.TransferInService;
import com.erp.server.wms.service.TransferOutDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 分布式调入单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class TransferInServiceImpl extends SuperServiceImpl<TransferInMapper, TransferInEntity> implements TransferInService {


    @Resource
    private TransferOutDetailService transferOutDetailService;

    @Resource
    private TransferInDetailService transferInDetailService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private OperateLogService operateLogService;

    @Override
    public List<TransferInDTO.TabListDTO> tabList() {
        List<TransferInDTO.TabListDTO> resultList = new ArrayList<>(4);
        List<TransferInDTO.ApproveCountDTO> approveCountList = baseMapper.listApproveCount();
        int allCount = approveCountList.stream().mapToInt(TransferInDTO.ApproveCountDTO::getCount).sum();
        TransferInDTO.TabListDTO all = new TransferInDTO.TabListDTO();
        all.setCount(allCount);
        all.setSearchType(SearchType.ALL);
        resultList.add(all);
        //待审核
        String ing = ApproveStatusEnum.APPROVE_ING.getStatus();
        TransferInDTO.TabListDTO waitApprove = new TransferInDTO.TabListDTO();
        int waitApproveCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(ing)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        waitApprove.setCount(waitApproveCount);
        waitApprove.setSearchType(SearchType.WAIT_APPROVE);
        resultList.add(waitApprove);

        //已审核
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        TransferInDTO.TabListDTO approve = new TransferInDTO.TabListDTO();
        int approveCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(approveStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        approve.setCount(approveCount);
        approve.setSearchType(approveStatus);
        resultList.add(approve);
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        TransferInDTO.TabListDTO reject = new TransferInDTO.TabListDTO();
        int rejectCount = approveCountList.stream().filter(a -> a.getApproveStatus().equals(rejectStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        reject.setCount(rejectCount);
        reject.setSearchType(rejectStatus);
        resultList.add(reject);
        return resultList;
    }

    /**
     * 下推单据保存
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-26 11:33
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateTransferIn(ValidList<TransferInDTO.ViewGenerateTransferInDTO> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            return Boolean.FALSE;
        }
        Map<String, List<TransferInDTO.ViewGenerateTransferInDTO>> map = list.stream().collect(Collectors.groupingBy(TransferInDTO.ViewGenerateTransferInDTO::getSourceId));
        List<TransferInDTO.AddDTO> addList = new ArrayList<>(map.size());

        //这个是调出详情id
        List<String> outDetailIds = list.stream().map(TransferInDTO.ViewGenerateTransferInDTO::getSourceDetailId).collect(Collectors.toList());
        //调出单详情
        List<TransferOutDetailEntity> outDetailList = CollectionUtils.isNotEmpty(outDetailIds) ? transferOutDetailService.listByIds(outDetailIds) : Collections.emptyList();
        String sourceType = SourceTypeEnum.TRANSFER_OUT.getCode();
        for (Map.Entry<String, List<TransferInDTO.ViewGenerateTransferInDTO>> entry : map.entrySet()) {
            //来源id
            String sourceId = entry.getKey();
            List<TransferInDTO.ViewGenerateTransferInDTO> generateInfoList = entry.getValue();
            TransferInDTO.ViewGenerateTransferInDTO viewGenerate = generateInfoList.stream().filter(g -> StringUtils.isNotBlank(g.getSourceCode())).findFirst().orElse(null);
            if (viewGenerate != null) {
                TransferInDTO.AddDTO addDTO = new TransferInDTO.AddDTO();
                addDTO.setInWarehouseId(viewGenerate.getInWarehouseId());
                addDTO.setOutWarehouseId(viewGenerate.getOutWarehouseId());
                addDTO.setSourceCode(viewGenerate.getSourceCode());
                addDTO.setSourceId(sourceId);
                addDTO.setSourceType(sourceType);
                addDTO.setTransferDirection(viewGenerate.getTransferDirection());
                addDTO.setTransferType(viewGenerate.getTransferType());
                addDTO.setBillDate(LocalDate.now());
                List<TransferInDetailDTO.AddDTO> detailList = new ArrayList<>(generateInfoList.size());
                for (TransferInDTO.ViewGenerateTransferInDTO item : generateInfoList) {
                    TransferInDetailDTO.AddDTO detail = new TransferInDetailDTO.AddDTO();
                    detail.setOutWarehouseLocation(item.getOutWarehouseLocation());
                    detail.setSkuId(item.getSkuId());
                    detail.setSourceDetailId(item.getSourceDetailId());
                    detail.setSkuNo(item.getSkuNo());
                    Integer planQty = item.getPlanQty();
                    String sourceDetailId = item.getSourceDetailId();
                    Integer outQty = outDetailList.stream().filter(o -> o.getId().equals(sourceDetailId)).findFirst().
                            flatMap(obj -> Optional.ofNullable(obj.getQty())).orElse(0);
                    if (planQty > outQty) {
                        throw new ServiceException(ApiError.ERROR_99065);
                    }
                    detail.setQty(planQty);
                    detail.setPlanQty(planQty);
                    detail.setRemark(item.getRemark());
                    detail.setTransitDamageQty(0);
                    detailList.add(detail);
                }
                addDTO.setDetailList(detailList);
                addList.add(addDTO);
            }
        }

        return this.batchAdd(addList);
    }

    /**
     * 批量添加数据
     *
     * @param addList
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAdd(List<TransferInDTO.AddDTO> addList) {
        if (CollectionUtils.isEmpty(addList)) {
            return Boolean.FALSE;
        }
        addList.forEach(obj -> add(obj));
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public String add(TransferInDTO.AddDTO dto) {
        String id = IdWorker.getIdStr();
        TransferInEntity transferIn = new TransferInEntity();
        BeanMapper.copy(dto, transferIn);
        String warehouseKeeperId = dto.getWarehouseKeeperId();
        if (StringUtils.isNotBlank(warehouseKeeperId)) {
            //用户信息
            FindUserDTO userInfo = sysUserFeign.getUserByUserId(warehouseKeeperId);
            if (userInfo != null) {
                transferIn.setWarehouseKeeperName(userInfo.getUserName());
            }
        }
        transferIn.setId(id);
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.FBDR, BusinessNoTypeEnum.CODE_FBDR.getCode()));
        transferIn.setCode(code);
        Boolean addResult = this.save(transferIn);
        //添加成功
        if (addResult) {
            transferInDetailService.add(id, dto.getDetailList());
            //添加日志
            String content = String.format("新增了一个{%s}-分布式调入单-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.TRANSFER_IN.getCode(), id, "新增操作");
            return id;
        }
        return "";
    }

    /**
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        operateLogService.addModuleOperateLog(content, code, businessId, operation);
    }

}
