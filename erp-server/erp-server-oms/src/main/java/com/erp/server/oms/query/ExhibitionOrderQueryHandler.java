package com.erp.server.oms.query;

import cn.hutool.core.collection.CollUtil;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import jodd.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ExhibitionOrderQueryHandler extends AbstractQueryHandler {


    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private WorkflowFeign workflowFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        /**
         * SPU编号查询
         */
        if("spuNo".equals(field)){
            String queryField = "pi.spu_no";
            List<AdvanceQueryDTO> advanceQueryDTOList = new ArrayList<>();
            QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
            if(queryConditionEnum.equals(QueryConditionEnum.EQ) || queryConditionEnum.equals(QueryConditionEnum.IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.CONTAINS)
                    || queryConditionEnum.equals(QueryConditionEnum.STARTS_WITH) ||  queryConditionEnum.equals(QueryConditionEnum.ENDS_WITH)){
                AdvanceQueryDTO advanceQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO(queryField,queryConditionEnum,value,QueryDataTypeEnum.STRING);
                advanceQueryDTOList.add(advanceQueryDTO);
                AdvanceQueryContainer advanceQueryContainer = AdvanceQueryContainer.builder().advanceQueryDTOList(advanceQueryDTOList).build();
                List<SkuVO> skuVOS = plmTaskFeign.getSkuInfoAdvanceQuery(advanceQueryContainer);
                List<String> skuIds = skuVOS.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
                if(CollectionUtils.isEmpty(skuIds)){
                    return this.getQueryEmptySql();
                }
                super.buildSplicingSQLDTO("eod.sku_id",QueryConditionEnum.IN_LIST,skuIds, QueryDataTypeEnum.STRING);
            }
            if(queryConditionEnum.equals(QueryConditionEnum.NE) || queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.NOT_CONTAINS)){
                AdvanceQueryDTO advanceQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO(queryField,QueryConditionEnum.IN_LIST,value,QueryDataTypeEnum.STRING);
                advanceQueryDTOList.add(advanceQueryDTO);
                AdvanceQueryContainer advanceQueryContainer = AdvanceQueryContainer.builder().advanceQueryDTOList(advanceQueryDTOList).build();
                List<SkuVO> skuVOS = plmTaskFeign.getSkuInfoAdvanceQuery(advanceQueryContainer);
                List<String> skuIds = skuVOS.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
                if(CollectionUtils.isEmpty(skuIds)){
                    return this.getQueryAllSql();
                }
                super.buildSplicingSQLDTO("eod.sku_id",QueryConditionEnum.NOT_IN_LIST,skuIds,QueryDataTypeEnum.STRING);
            }
        }
        if("eo.approve_user_id".equals(field)){
            ValidList<ProcessManagementDTO.ApproveActivityDTO> dtoList = new ValidList<>();

            List<String> list = new ArrayList<>();
            if (value instanceof List) {
                list = (List<String>) value;
            }else {
                list.add(value.toString());
            }

            for (String curApproveId : list) {
                ProcessManagementDTO.ApproveActivityDTO dto = new ProcessManagementDTO.ApproveActivityDTO();
                dto.setCurApproveId(curApproveId);
                dto.setBusinessKey(SourceTypeEnum.EXHIBITION_ORDER.getCode());
                dtoList.add(dto);
            }
            ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.batchCurApproverByApprove(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }

            QueryConditionEnum compareCode = AdvanceQueryContext.getCompareCode();
            List<ProcessManagementDTO.CurApproveInfoDTO> data = listApiResult.getData();
            if(CollUtil.isNotEmpty(data)){
                List<String> ids = data.stream().map(ProcessManagementDTO.CurApproveInfoDTO::getBusinessId).filter(StringUtil::isNotBlank).collect(Collectors.toList());
                if(compareCode.equals(QueryConditionEnum.NOT_IN_LIST) || compareCode.equals(QueryConditionEnum.NE) ){
                    super.buildSplicingSQLDTO("eo.id",QueryConditionEnum.NOT_IN_LIST,ids,QueryDataTypeEnum.STRING);
                }else {
                    super.buildSplicingSQLDTO("eo.id",QueryConditionEnum.IN_LIST,ids,QueryDataTypeEnum.STRING);
                }
            }else {
                super.buildSplicingSQLDTO("eo.id",QueryConditionEnum.EQ,"-1",QueryDataTypeEnum.STRING);
            }
        }
        return null;
    }

    public String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }

        super.buildDefaultDTO("eo.approve_status", value);
        return super.getSplicingSQL();
    }
}
