package com.erp.server.sys.service;
import com.erp.model.sys.entity.DictThirdCity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 第三方城市字典表 服务类
 * </p>
 *
 * @author lrp
 * @since 2023-11-23
 */
public interface DictThirdCityService extends SuperService<DictThirdCity> {

    boolean saveOrUpdateByRegionId(DictThirdCity entity);

    List<DictThirdCity> listByDictIdList(List<String> dictIds);
}
