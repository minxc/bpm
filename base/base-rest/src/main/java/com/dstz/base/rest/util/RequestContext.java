package com.dstz.base.rest.util;

import com.dstz.base.core.util.BeanUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestContext {

    private static ThreadLocal<HttpServletRequest> requestLocal = new ThreadLocal<HttpServletRequest>();

    private static ThreadLocal<HttpServletResponse> responseLocal = new ThreadLocal<HttpServletResponse>();

    public static void setHttpServletRequest(HttpServletRequest request) {
        requestLocal.set(request);
    }

    /**
     * 清除request和response线程变量
     * void
     *
     * @throws
     * @since 1.0.0
     */
    public static void clearHttpReqResponse() {
        requestLocal.remove();
        responseLocal.remove();
    }

    /**
     * @param response void
     * @throws
     * @since 1.0.0
     */
    public static void setHttpServletResponse(HttpServletResponse response) {
        responseLocal.set(response);
    }

    /**
     * 获取当前请求的Request，需要保证AopFilter在web.xml里配置才能取到
     *
     * @return
     */
    public static HttpServletRequest getHttpServletRequest() {
        return requestLocal.get();
    }

    /**
     * 返回response。
     *
     * @return
     */
    public static HttpServletResponse getHttpServletResponse() {
        return responseLocal.get();
    }

    public static String getHttpCtx() {
        if (BeanUtils.isNotEmpty(requestLocal.get())) {
            return requestLocal.get().getContextPath();
        }
        return null;
    }


}
