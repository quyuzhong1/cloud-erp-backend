package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.QcReportDTO;
import com.erp.model.wms.dto.QcRuleDTO;
import com.erp.model.wms.entity.QcRuleEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.QcRuleMapper;
import com.erp.server.wms.service.QcReportService;
import com.erp.server.wms.service.QcRuleService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 质检规则 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-13
 */
@Service
public class QcRuleServiceImpl extends SuperServiceImpl<QcRuleMapper, QcRuleEntity> implements QcRuleService {


    @Resource
    private QcReportService qcReportService;

    @Resource
    private SysUserFeign sysUserFeign;

    /**
     * 添加质检规则
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-04-13 10:18
     */
    @Override
    public String add(QcRuleDTO.AddDTO dto) {
        //TODO 产品等级 校验
        //是否有质检报告
        Boolean existReport = dto.getExistReport();
        //质检报告
        List<QcReportDTO.AddDTO> reportList = dto.getQcReportLList();
        //如果有 报告不能为空
        if (existReport) {
            if (CollectionUtils.isEmpty(reportList)) {
                throw new ServiceException(ApiError.ERROR_EXIST_REPORT);
            }
        }
        QcRuleEntity rule = new QcRuleEntity();
        String id = IdWorker.getIdStr();
        BeanMapper.copy(dto, rule);
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.QCGZ, BusinessNoTypeEnum.CODE_ZJGZ.getCode()));
        rule.setCode(code);
        rule.setId(id);
        Boolean addResult = this.save(rule);
        //添加成功
        if (addResult) {
            //添加质检报告
            qcReportService.addQcReport(id, reportList);
            return id;
        }
        return "";
    }
}
