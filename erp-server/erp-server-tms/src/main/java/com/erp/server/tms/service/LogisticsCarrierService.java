package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsCarrierEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsCarrierDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 物流快递/海运/空运公司列表 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-05-08
 */
public interface LogisticsCarrierService extends SuperService<LogisticsCarrierEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-05-08
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsCarrierDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-05-08
    * @param dto
    * @return
    */
    Boolean update(LogisticsCarrierDTO.UpdateDTO dto);

    /**
     * 导入配置
     * @param excelFile
     * @param logisticsType
     * @param response
     * @return
     */
    Boolean importFile(MultipartFile excelFile, String logisticsType, HttpServletResponse response);

    /**
     * 根据类型获取下拉框
     * @param logisticsType
     * @return
     */
    List<LogisticsCarrierEntity> dropDown(String logisticsType);
}
