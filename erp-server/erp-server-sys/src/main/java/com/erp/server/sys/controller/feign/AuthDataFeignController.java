package com.erp.server.sys.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.sys.dto.AuthUserShopDTO;
import com.erp.model.sys.dto.AuthUserWarehouseDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.server.sys.service.AuthUserShopService;
import com.erp.server.sys.service.AuthUserWarehouseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zdy
 * @ClassName AuthDataFeignController
 * @description: 数据权限
 * @date 2025年03月04日
 * @version: 1.0
 */
@RestController
@RequestMapping("feign/auth")
public class AuthDataFeignController extends BaseController {
    @Resource
    private AuthUserShopService authUserShopService;
    @Resource
    private AuthUserWarehouseService authUserWarehouseService;


    /**
     * 根据用户id 获取 所有店铺
     *
     * @return
     */
    @PostMapping("/getShopUserList")
    public List<SysUserDTO.ShopDTO> getShopUserList(@RequestBody String userId) {
        return authUserShopService.getShopUserList(userId);
    }
    /**
     * 获取用户权限列表
     * @param userIdList
     * @return
     */
    @PostMapping("/listShopIdByUserIds")
    public List<SysUserDTO.ShopDTO> listShopIdByUserIds(@RequestBody List<String> userIdList){
        return authUserShopService.listShopIdByUserIds(userIdList);
    }
    /**
     * 根据用户id 获取 所有仓库
     *
     * @return
     */
    @PostMapping("/getWarehouseUserList")
    public List<SysUserDTO.WarehouseDTO> getWarehouseUserList(@RequestBody String userId) {
        return authUserWarehouseService.getWarehouseUserList(userId);
    }

    /**
     * 获取用户店铺权限
     * @return
     */
    @PostMapping("/getShopPermissionSql")
    public String getShopPermissionSql(@RequestBody String shopTableField){
        return authUserShopService.getShopPermissionSql(shopTableField , null);
    }
    /**
     * 获取用户店铺权限
     * @return
     */
    @PostMapping("/getShopPermissionSqlByDynamicDataSource")
    public String getShopPermissionSqlByDynamicDataSource(@RequestParam String shopTableField , @RequestParam String dynamicDataSource){
    	return authUserShopService.getShopPermissionSql(shopTableField , dynamicDataSource);
    }
    /**
     * 获取用户仓库权限
     * @return
     */
    @PostMapping("/getWarehousePermissionSql")
    public String getWarehousePermissionSql(@RequestBody String warehouseTableField){
        return authUserWarehouseService.getWarehousePermissionSql(warehouseTableField , null);
    }
    /**
     * 获取用户仓库权限
     * @return
     */
    @PostMapping("/getWarehousePermissionSqlByDynamicDataSource")
    public String getWarehousePermissionSqlByDynamicDataSource(@RequestParam String warehouseTableField , @RequestParam String dynamicDataSource){
    	return authUserWarehouseService.getWarehousePermissionSql(warehouseTableField , dynamicDataSource);
    }

    /**
     * @description: 根据店铺id查询已关联用户id
     * @author Will
     * @date: 2023/9/7 9:35
     * @param shopIdList
     * @return List<String>
     */
    @PostMapping("/listUserIdByShopIdList")
    public List<String> listUserIdByShopIdList(@RequestBody List<String> shopIdList){
        return authUserShopService.listUserIdByShopIdList(shopIdList);
    }
    /**
     * 增加用户店铺权限
     * @description:
     * @author zdy
     * @date: 2025/5/27 9:35
     * @param addUserShopAuthDTO
     * @return List<String>
     */
    @PostMapping("/addUserShopAuth")
    public void addUserShopAuth(@RequestBody AuthUserShopDTO.AddUserShopAuthDTO addUserShopAuthDTO){
        authUserShopService.addUserShopAuth(addUserShopAuthDTO);
    }
    /**
     * 增加用户仓库权限
     * @description:
     * @author zdy
     * @date: 2025/5/27 9:35
     * @param addUserWarehouseAuthDTO
     * @return List<String>
     */
    @PostMapping("/addUserWarehouseAuth")
    public void addUserWarehouseAuth(@RequestBody AuthUserWarehouseDTO.AddUserWarehouseAuthDTO addUserWarehouseAuthDTO){
        authUserWarehouseService.addUserWarehouseAuth(addUserWarehouseAuthDTO);
    }
}
