package com.erp.server.plm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.TaskPagingShowDTO;
import com.erp.model.plm.dto.TaskSearchParamDTO;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.server.plm.mapper.WorkOptionMapper;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.WorkOptionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 工作台服务类
 * @Author Luo_WG
 * @Date 2023/4/21 15:33
 **/
@Service
public class WorkOptionServiceImpl implements WorkOptionService {

    @Resource
    private WorkOptionMapper workOptionMapper;

    @Resource
    private ProjectTaskService projectTaskService;

    /**
     * 根据入参查询单据数量
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    @Override
    public Integer getTableNum(WorkOptionDTO.TableNumDTO tableNumDTO) {
        //assignExecutable
        //assignNotStarted
        //waitAuditExecutable
        //waitAuditNotStarted
        if (tableNumDTO.getTableName().equals("project_task")) {
            PagingDTO pagingDTO = JSONObject.parseObject(tableNumDTO.getModuleParam(), PagingDTO.class);
            TaskSearchParamDTO params = JSONObject.parseObject(JSONObject.toJSONString(pagingDTO.getParams()), TaskSearchParamDTO.class);
            pagingDTO.setPageSize(99999);
            pagingDTO.setParams(params);
            PagingVO<List<TaskPagingShowDTO>> listPagingVO = projectTaskService.assignToMePaging(pagingDTO);
            return listPagingVO.getTotalCount();
        }
        if (tableNumDTO.getTableName().equals("product_detail")) {
            return workOptionMapper.getProductDetailNum(tableNumDTO);
        }
        if (tableNumDTO.getTableName().equals("product_bom_info")) {
            return workOptionMapper.getProductBomInfoNum(tableNumDTO);
        }
        if (tableNumDTO.getTableName().equals("product_change")) {
            return workOptionMapper.getProductChangeNum(tableNumDTO);
        }
        return 0;
    }

    /**
     * 根据用户获取各任务阶段数量
     * @Author Luo_WG
     * @Date 2023/4/24 9:34
     * @param optionUserId optionUserId
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.StageViewDTO>
     **/
    @Override
    public List<WorkOptionDTO.StageViewDTO> stageView(String optionUserId) {
        return workOptionMapper.stageView(optionUserId);
    }
}
