package com.dstz.bpm.engine.parser.node;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dstz.base.core.util.AppUtil;
import com.dstz.bpm.api.engine.plugin.context.BpmPluginContext;
import com.dstz.bpm.api.engine.plugin.def.BpmDef;
import com.dstz.bpm.api.engine.plugin.def.BpmPluginDef;
import com.dstz.bpm.api.model.nodedef.impl.BaseBpmNodeDef;
import com.dstz.bpm.engine.parser.node.AbsNodeParse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class NodePluginsParser extends AbsNodeParse<BpmPluginContext> {
	
	
	@Override
	public Object parseDef(BaseBpmNodeDef userNodeDef, String json) {
		JSONObject plugins = JSON.parseObject((String) json);
		ArrayList<BpmPluginContext> pluginContextList = new ArrayList<BpmPluginContext>();
		for (String pluginName : plugins.keySet()) {
			BpmPluginContext pluginContext = (BpmPluginContext) AppUtil
					.getBean((String) (pluginName + "PluginContext"));
			if (pluginContext instanceof BpmPluginContext) {
				Object object = plugins.get((Object) pluginName);
				pluginContext.parse((JSON) object);
			}
			pluginContextList.add(pluginContext);
		}
		return pluginContextList;
	}

	public String getKey() {
		return "plugins";
	}

	public String validate(Object o) {
		return null;
	}
	@Override
	public void setDefParam(BaseBpmNodeDef userNodeDef, Object object) {
		ArrayList pluginContextList = (ArrayList) object;
		userNodeDef.setBpmPluginContexts((List) pluginContextList);
	}
}