package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.server.wms.service.ApproveHandler;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Setter
@Service
public class BatchApproveService {

    private ApproveHandler approveHandler;

    public List<BatchResultDTO> submit(List<String> ids) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        for (String id : ids) {
            BatchResultDTO submit;
            try {
                submit = approveHandler.submit(id, Boolean.TRUE);
            } catch (Exception e) {
                String code = approveHandler.getCodeById(id);
                if (ObjectUtil.isEmpty(code)) {
                    submit = BatchResultDTO.fail(id, id, "单据不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(id, code, e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS;
    }

    public List<BatchResultDTO> approve(BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = approveHandler.approve(new ApproveOneDTO(id, dto.getType(), dto.getComment()));
            } catch (Exception e) {
                String code = approveHandler.getCodeById(id);
                if (ObjectUtil.isEmpty(code)) {
                    approveResult = BatchResultDTO.fail(id, id, "单据不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(id, code, e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS;
    }

    public List<BatchResultDTO> disApprove(List<String> ids) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        for (String id : ids) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = approveHandler.disApprove(id);
            } catch (Exception e) {
                String code = approveHandler.getCodeById(id);
                if (Objects.isNull(code)) {
                    resultDTO = BatchResultDTO.fail(id, id, "单据不存在, 反审核失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(id, code, e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS;
    }

    public List<BatchResultDTO> cancelProcess(List<String> ids) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        for (String id : ids) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = approveHandler.cancelProcess(new ApproveDTO.CancelProcessDTO(id));
            } catch (Exception e) {
                String code = approveHandler.getCodeById(id);
                if (Objects.isNull(code)) {
                    resultDTO = BatchResultDTO.fail(id, id, "单据不存在, 撤销流程失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(id, code, e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS;
    }

}
