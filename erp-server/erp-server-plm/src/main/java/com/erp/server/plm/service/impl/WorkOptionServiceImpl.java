package com.erp.server.plm.service.impl;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProductSkuDTO;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.dto.TaskSearchParamDTO;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.rpc.sys.feign.UserInfoFeign;
import com.erp.server.plm.mapper.WorkOptionMapper;
import com.erp.server.plm.service.*;
import jodd.util.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;

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
    private ProductChangeService productChangeService;

    @Resource
    private BomInfoService bomInfoService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private UserInfoFeign userInfoFeign;

    @Override
    public List<WorkOptionDTO.MyWorkOptionDTO> getTableNum(List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOList) {
        for (WorkOptionDTO.MyWorkOptionDTO myWorkOptionDTO : myWorkOptionDTOList) {
            String userDatePermissionSql = userInfoFeign.getUserDatePermissionSql(myWorkOptionDTO.getTableField(), myWorkOptionDTO.getMenuCode());

            myWorkOptionDTO.setModuleCode(SourceTypeEnum.getByCode(myWorkOptionDTO.getModuleCode()).getTableName());
            if (myWorkOptionDTO.getModuleCode().equals("project_task")) {
                PagingDTO<TaskSearchParamDTO> pagingDTO = parseObject(myWorkOptionDTO.getModuleParam(), PagingDTO.class);
                TaskSearchParamDTO params = parseObject(toJSONString(pagingDTO.getParams()), TaskSearchParamDTO.class);
                pagingDTO.setParams(params);
                if (StringUtil.isNotBlank(userDatePermissionSql)) {
                    pagingDTO.setPermissionSql(userDatePermissionSql);
                }

                if (myWorkOptionDTO.getModuleStatus().equals("assignNotStarted") || myWorkOptionDTO.getModuleStatus().equals("assignExecutable")) {
                    myWorkOptionDTO.setTableNumber(projectTaskService.assignToMePaging(pagingDTO).getTotalCount());
                } else {
                    myWorkOptionDTO.setTableNumber(projectTaskService.assignToMeWaitAuditPaging(pagingDTO).getTotalCount());
                }
            }
            if (myWorkOptionDTO.getModuleCode().equals("product_detail")) {
                Integer status = Integer.valueOf(myWorkOptionDTO.getModuleStatus());
                PagingDTO<ProductSkuDTO> pagingDTO = parseObject(myWorkOptionDTO.getModuleParam(), PagingDTO.class);
                ProductSkuDTO params = parseObject(toJSONString(pagingDTO.getParams()), ProductSkuDTO.class);
                pagingDTO.setParams(params);
                if (StringUtil.isNotBlank(userDatePermissionSql)) {
                    pagingDTO.setPermissionSql(userDatePermissionSql);
                }
                myWorkOptionDTO.setTableNumber(productDetailService.paging(pagingDTO).getTotalCount());
            }
            if (myWorkOptionDTO.getModuleCode().equals("product_bom_info")) {
                Integer status = Integer.valueOf(myWorkOptionDTO.getModuleStatus());
                PagingDTO<SearchPagingDTO> pagingDTO = parseObject(myWorkOptionDTO.getModuleParam(), PagingDTO.class);

                if (StringUtil.isNotBlank(userDatePermissionSql)) {
                    pagingDTO.setPermissionSql(userDatePermissionSql);
                }

                SearchPagingDTO params = parseObject(toJSONString(pagingDTO.getParams()), SearchPagingDTO.class);
                pagingDTO.setParams(params);
                Map<String, String> sqlMap = new HashMap<>();
                sqlMap.put("default", "1=1");
                params.setSqlMap(sqlMap);
                myWorkOptionDTO.setTableNumber(bomInfoService.paging(pagingDTO).getTotalCount());
            }
            if (myWorkOptionDTO.getModuleCode().equals("product_change")) {
                PagingDTO<SearchPagingDTO> pagingDTO = parseObject(myWorkOptionDTO.getModuleParam(), PagingDTO.class);

                if (StringUtil.isNotBlank(userDatePermissionSql)) {
                    pagingDTO.setPermissionSql(userDatePermissionSql);
                }

                SearchPagingDTO params = parseObject(toJSONString(pagingDTO.getParams()), SearchPagingDTO.class);
                Map<String, String> sqlMap = new HashMap<>();
                sqlMap.put("default", "1=1");
                params.setSqlMap(sqlMap);
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
