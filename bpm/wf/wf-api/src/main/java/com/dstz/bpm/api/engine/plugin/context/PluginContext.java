package com.dstz.bpm.api.engine.plugin.context;

import com.dstz.bpm.api.engine.plugin.def.BpmPluginDef;
import com.dstz.bpm.api.engine.plugin.runtime.RunTimePlugin;

import java.io.Serializable;

public interface PluginContext extends Serializable{


    public final static String PLUGINCONTEXT = "PluginContext";

    /**
     * 插件运行时的Class
     *
     * @return String
     * @throws
     * @since 1.0.0
     */
    Class<? extends RunTimePlugin> getPluginClass();

    /**
     * 返回流程插件定义
     *
     * @return BpmPluginDef
     * @throws
     * @since 1.0.0
     */
    BpmPluginDef getBpmPluginDef();

    /**
     * 获取插件标题。
     *
     * @return String
     */
    String getTitle();
    
}
