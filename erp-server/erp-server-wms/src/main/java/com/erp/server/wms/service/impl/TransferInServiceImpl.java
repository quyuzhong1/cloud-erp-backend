package com.erp.server.wms.service.impl;

import com.common.business.constant.SearchType;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.erp.model.wms.dto.TransferInDTO;
import com.erp.model.wms.entity.TransferInEntity;
import com.erp.server.wms.mapper.TransferInMapper;
import com.erp.server.wms.service.TransferInService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public Boolean generateTransferIn(ValidList<TransferInDTO.ViewGenerateTransferInDTO> list) {
        if (CollectionUtils.isNotEmpty(list)) {

        }


        return null;
    }
}
