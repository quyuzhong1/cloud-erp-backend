package com.erp.server.tms.service;
import com.erp.model.tms.entity.CfgLogisticsCostImportFieldEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.CfgLogisticsCostImportFieldDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 费用项配置字段基础表 服务类
 * </p>
 *
 * @author jack
 * @since 2026-01-20
 */
public interface CfgLogisticsCostImportFieldService extends SuperService<CfgLogisticsCostImportFieldEntity> {



    List<CfgLogisticsCostImportFieldDTO.ListDTO> listByBusinessType(String businessType);

    List<CfgLogisticsCostImportFieldDTO.TreeDTO> tree(String businessType);
}
