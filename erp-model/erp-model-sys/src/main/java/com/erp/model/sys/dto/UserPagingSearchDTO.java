package com.erp.model.sys.dto;

import com.common.business.dto.AdvanceQueryDTO;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author zdy
 * @ClassName UserPagingSearchDTO
 * @description: TODO
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
     * 用户id
     */
    private List<String> userIds;
//
//
//    //开始时间
//    private LocalDate startTime;
//
//    //结束时间
//    private LocalDate endTime;
//
//    //状态 1 正常  0 不正常
//    private Integer state;
//
    /**
     * 用户类型 erp srm
     */
    private String userType ;
//
//    private String searchType;
//
    /**
     * 是否超级管理员 false 不是管理员
     */
    private Boolean isSuper;
//
//    private String searchKeyword;
//    /**
//     * 用来控制供应商数据
//     */
//    private String supplierId;
}
