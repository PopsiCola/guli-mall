package com.llb.common.constant;

/**
 * 错误码和错误信息定义
 * 1.错误码定义规则为5个数字
 * 2.前两位表示业务场景，最后三位表示错误码。
 * <p>
 * 错误码列表：
 * 10：通用
 * 001：参数校验
 * 11：商品
 * 12：订单
 * 13：购物车
 * 14：物流
 *
 * @Author liulebin
 * @Date 2021/4/24 21:43
 */
public enum BizCodeEnum {

    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(100001, "参数格式校验失败"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常");

    private int code;
    private String msg;

    BizCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
