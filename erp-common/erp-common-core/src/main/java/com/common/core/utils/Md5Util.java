package com.common.core.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.lang.generator.SnowflakeGenerator;
import cn.hutool.core.math.MathUtil;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author yl
 * @Classname Md5Util MD5 工具
 * @Description TODO
 * @Date 2022-07-06 11:51
 */
public class Md5Util {

    /**
     * 简单MD5
     *
     * @param str
     * @return
     */
    public static String md5(String str) {

        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] array = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte item : array) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100), 1, 3);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    public static String getMd5(String plainText){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            //可以填UTF-8或GBK
            md.update(plainText.getBytes("UTF-8"));
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            //32位加密
            return buf.toString();
            // 16位的加密
            //return buf.toString().substring(8, 24);
        } catch (NoSuchAlgorithmException|UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        String str = "…123452016-07-27 13:10:10";
        str = "{'receiving_code':'RVG2199-230321-0001','reference_no':'','customer_code':'G2199','receiving_type':0,'receiving_status':10,'warehouse_code':'USWE','warehouse_id':'4','addTime':'2023-03-21 09:10:10','updateTime':'2023-04-03 01:08:25','receivingDetail':[{'reference_box_no':'','box_no':1,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':2,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':3,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':4,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':5,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':6,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':7,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':8,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':9,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':10,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':11,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9}]}a39ab99c1437c991ec07fad4e1f78f8f2023-04-11 16:34:53";
        str = "{'receiving_code':'RVG2199-230321-0001','reference_no':'','customer_code':'G2199','receiving_type':0,'receiving_status':10,'warehouse_code':'USWE','warehouse_id':'4','addTime':'2023-03-21 09:10:10','updateTime':'2023-04-03 01:08:25','receivingDetail':[{'reference_box_no':'','box_no':1,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':2,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':3,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':4,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':5,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':6,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':7,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':8,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':9,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':10,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9},{'reference_box_no':'','box_no':11,'product_sku':'3318','product_barcode':'G2199-3318','deliveryQty':9,'receiptQty':9,'putawayQty':9,'unsellableQty':0,'sellableQty':9}]}a39ab99c1437c991ec07fad4e1f78f8f2023-04-18 09:14:44";;
        System.out.println(getMd5(str));//签名结果：e01429e834a2f11616320f7e3cb4cb9a
        System.out.println(md5(str));
    }
}
