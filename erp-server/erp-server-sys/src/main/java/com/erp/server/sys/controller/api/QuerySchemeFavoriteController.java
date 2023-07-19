package com.erp.server.sys.controller.api;

import com.erp.model.sys.dto.QuerySchemeFavoriteDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.sys.service.QuerySchemeFavoriteService;
import com.common.core.controller.vo.ApiResult;

import java.util.List;


/**
 * 
 *
 * @author Cloud
 * @since 2023-07-19
 */
@RestController
@RequestMapping("/querySchemeFavorite")
public class QuerySchemeFavoriteController extends BaseController {

    @Autowired
    private QuerySchemeFavoriteService querySchemeFavoriteService;

    /**
     *  查询指定用户所有保存方案
     */
    @GetMapping("/list")
    public ApiResult<List<QuerySchemeFavoriteDTO.ViewDTO>> list(@RequestParam(value = "userId", required = false) String userId) {
        return success(querySchemeFavoriteService.listByUserId(userId));
    }

    /**
     * 保存个人方案
     */
    @PostMapping("/add")
    public ApiResult<Boolean> add(@Validated @RequestBody QuerySchemeFavoriteDTO.AddDTO addDTO) {
        return success(querySchemeFavoriteService.add(addDTO));
    }

    /**
     * 修改个人方案
     */
    @PostMapping("/update")
    public ApiResult<Boolean> update(@Validated @RequestBody QuerySchemeFavoriteDTO.UpdateDTO updateDTO) {
        return success(querySchemeFavoriteService.updateById(updateDTO));
    }



}
