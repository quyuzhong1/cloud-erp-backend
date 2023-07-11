package com.erp.server.sys.service;

import com.erp.model.sys.dto.DeptKingdeeDTO;
import com.erp.model.sys.entity.DeptKingdeeEntity;
import com.common.business.service.SuperService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-07
 */
public interface DeptKingdeeService extends SuperService<DeptKingdeeEntity> {

    /**
     * 导入
     * @param excelFile
     * @param response
     * @return
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);

    DeptKingdeeEntity getInfo(DeptKingdeeDTO.FindDeptKingdeeDTO dto);








}
