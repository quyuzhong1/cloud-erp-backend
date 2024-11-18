package com.erp.model.sys.dto;

import com.common.business.dto.AdvanceQueryDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author zdy
 * @ClassName UserPagingSearchDTO
 * @date 2024年01月05日
 * @version: 1.0
 */
@Data
public class UserPagingSearchDTO {

    /**
     * 页面高级查询
     */
    private List<AdvanceQueryDTO> advanceQueryDTOList;

    /**
     * sqlMap 默认key default
     */
    private Map<String,String> sqlMap;

    /**
     * 供应商ids
     */
    private List<String> supplierIds;
    /**
     * 用户所在供应商
     */
    private String supplierId;
    /**
     * 用户id
     */
    private List<String> userIds;
    /**
     * 用户类型 erp srm
     */
    private String userType ;

    /**
     * 是否超级管理员 false 不是管理员
     */
    private Boolean isSuper;
}
