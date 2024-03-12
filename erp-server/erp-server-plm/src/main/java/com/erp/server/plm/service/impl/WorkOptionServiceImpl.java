package com.erp.server.plm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProductSkuDTO;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.dto.TaskSearchParamDTO;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.mapper.WorkOptionMapper;
import com.erp.server.plm.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    @Resource
    private CommonService commonService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ProductChangeService productChangeService;

    @Resource
    private BomInfoService bomInfoService;

    @Resource
    private ProductDetailService productDetailService;

    @Override
    public List<WorkOptionDTO.MyWorkOptionDTO> getTableNum(List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOList) {
        for (WorkOptionDTO.MyWorkOptionDTO myWorkOptionDTO : myWorkOptionDTOList) {
            myWorkOptionDTO.setModuleCode(SourceTypeEnum.getByCode(myWorkOptionDTO.getModuleCode()).getTableName());
            if (myWorkOptionDTO.getModuleCode().equals("project_task")) {
                PagingDTO<TaskSearchParamDTO> pagingDTO = JSONObject.parseObject(myWorkOptionDTO.getModuleParam(), PagingDTO.class);
                TaskSearchParamDTO params = JSONObject.parseObject(JSONObject.toJSONString(pagingDTO.getParams()), TaskSearchParamDTO.class);
                pagingDTO.setParams(params);


                if (myWorkOptionDTO.getModuleStatus().equals("assignNotStarted") || myWorkOptionDTO.getModuleStatus().equals("assignExecutable")) {
                    myWorkOptionDTO.setTableNumber(projectTaskService.assignToMePaging(pagingDTO).getTotalCount());
                } else {
                    myWorkOptionDTO.setTableNumber(projectTaskService.assignToMeWaitAuditPaging(pagingDTO).getTotalCount());
                }
            }
            if (myWorkOptionDTO.getModuleCode().equals("product_detail")) {
                Integer status = Integer.valueOf(myWorkOptionDTO.getModuleStatus());
                PagingDTO<ProductSkuDTO> pagingDTO = JSONObject.parseObject(myWorkOptionDTO.getModuleParam(), PagingDTO.class);
                ProductSkuDTO params = JSONObject.parseObject(JSONObject.toJSONString(pagingDTO.getParams()), ProductSkuDTO.class);
                pagingDTO.setParams(params);
                myWorkOptionDTO.setTableNumber(productDetailService.paging(pagingDTO).getTotalCount());
            }
            if (myWorkOptionDTO.getModuleCode().equals("product_bom_info")) {
                Integer status = Integer.valueOf(myWorkOptionDTO.getModuleStatus());
                PagingDTO<SearchPagingDTO> pagingDTO = JSONObject.parseObject(myWorkOptionDTO.getModuleParam(), PagingDTO.class);
                SearchPagingDTO params = JSONObject.parseObject(JSONObject.toJSONString(pagingDTO.getParams()), SearchPagingDTO.class);
                pagingDTO.setParams(params);
                myWorkOptionDTO.setTableNumber(bomInfoService.paging(pagingDTO).getTotalCount());
            }
            if (myWorkOptionDTO.getModuleCode().equals("product_change")) {
                PagingDTO<SearchPagingDTO> pagingDTO = JSONObject.parseObject(myWorkOptionDTO.getModuleParam(), PagingDTO.class);
                SearchPagingDTO params = JSONObject.parseObject(JSONObject.toJSONString(pagingDTO.getParams()), SearchPagingDTO.class);
                pagingDTO.setParams(params);
                PagingVO paging = productChangeService.paging(pagingDTO);
                myWorkOptionDTO.setTableNumber(paging.getTotalCount());
            }
        }
        return myWorkOptionDTOList;
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
