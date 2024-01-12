package com.erp.server.srm.service;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.srm.dto.HomePageDTO;

/**
 * <p>
 *  首页service
 * </p>
 *
 */
public interface HomePageService  {

    HomePageDTO.AccountInfoDTO getAccountInfo();

    HomePageDTO.ToDoItems getToDoItems();
}
