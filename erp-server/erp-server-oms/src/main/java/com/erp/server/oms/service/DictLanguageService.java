package com.erp.server.oms.service;
import com.erp.model.oms.entity.DictLanguageEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.DictLanguageDTO;

import java.util.List;

/**
 * <p>
 * ISO 639-1 语言标准 服务类
 * </p>
 *
 * @author jack
 * @since 2025-12-01
 */
public interface DictLanguageService extends SuperService<DictLanguageEntity> {


    List<DictLanguageDTO.ListDTO> dropDown(DictLanguageDTO.SelectDTO dto);
}
