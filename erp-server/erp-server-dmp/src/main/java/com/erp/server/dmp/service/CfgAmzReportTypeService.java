package com.erp.server.dmp.service;

import com.erp.model.dmp.entity.AmzReportTaskEntity;
import com.erp.model.dmp.entity.CfgAmzReportTypeEntity;
import com.common.business.service.SuperService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 亚马逊报告类型配置 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
 */
public interface CfgAmzReportTypeService extends SuperService<CfgAmzReportTypeEntity> {


    /**
     * 查询有效报告类型配置
     *
     * @Author Jim
     * @since 2024-01-19
     **/
    List<CfgAmzReportTypeEntity> findActive(List<String> subscribedTypeList);

    /**
     * 查询有效报告类型配置map
     *
     * @Author Jim
     * @since 2024-01-19
     **/
    Map<String, List<CfgAmzReportTypeEntity>> mapByReportGroup();

    /**
     * 根据recordType查询
     *
     * @Author Jim
     * @since 2024-01-19
     **/
    CfgAmzReportTypeEntity getByRecordType(String reportType);

    /**
     * 检查当前市场是否支持
     */
    boolean checkCountryList(CfgAmzReportTypeEntity config, String marketplace);

    /**
     * 查询配置并校验国家是否支持
     *
     */
    CfgAmzReportTypeEntity checkCountryAndGetByRecordType(AmzReportTaskEntity entity);
}
