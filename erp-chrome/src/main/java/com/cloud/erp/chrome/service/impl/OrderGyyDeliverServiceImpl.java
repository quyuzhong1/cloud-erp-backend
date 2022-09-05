package com.cloud.erp.chrome.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.cloud.erp.chrome.constant.TaskState;
import com.cloud.erp.chrome.dto.GyyShipmentsDTO;
import com.cloud.erp.chrome.entity.OrderGyyDeliverEntity;
import com.cloud.erp.chrome.mapper.OrderGyyDeliverMapper;
import com.cloud.erp.chrome.service.ChromeTaskInfoService;
import com.cloud.erp.chrome.service.CsvServer;
import com.cloud.erp.chrome.service.OrderGyyDeliverService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * <p>
 * 管易云ERP 发货信息表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-01
 */
@Service
public class OrderGyyDeliverServiceImpl extends ServiceImpl<OrderGyyDeliverMapper, OrderGyyDeliverEntity> implements OrderGyyDeliverService {


    @Autowired
    private CsvServer csvServer;

    @Autowired
    private ChromeTaskInfoService chromeTaskInfoService;


    /**
     * 保存管易云发货信息表
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-09-01 18:04
     */
    @Override
    public void saveDeliverCsvByUrl(GyyShipmentsDTO dto) {
        File file = null;
        try {
            File directory = new File("erp-chrome/src/main/resources");
            String reportPath = directory.getCanonicalPath();
            String csvPath = reportPath + ("\\csv");
            file = HttpUtil.downloadFileFromUrl(dto.getOssUrl(), FileUtil.newFile(csvPath));
            List<OrderGyyDeliverEntity> saveList = csvServer.getObjectListByFile(file, OrderGyyDeliverEntity.class);
            if (CollectionUtils.isNotEmpty(saveList)) {
                this.saveBatch(saveList);
            }
        } catch (Exception e) {
            log.error("saveDeliverCsvByUrl 出错了 e " + e);
        } finally {
            if (file != null) {
                file.delete();
            }
        }
        chromeTaskInfoService.updateTaskState(dto.getTaskId(), TaskState.FINISH);
    }
}
