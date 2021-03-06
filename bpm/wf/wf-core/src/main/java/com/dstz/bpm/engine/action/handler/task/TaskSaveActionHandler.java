package com.dstz.bpm.engine.action.handler.task;

import com.dstz.bpm.api.constant.ActionType;
import com.dstz.bpm.api.engine.action.cmd.BaseActionCmd;
import com.dstz.bpm.engine.action.cmd.DefualtTaskActionCmd;
import com.dstz.bpm.engine.action.handler.task.AbstractTaskActionHandler;
import org.springframework.stereotype.Component;

@Component
public class TaskSaveActionHandler extends AbstractTaskActionHandler<DefualtTaskActionCmd> {
	public ActionType getActionType() {
		return ActionType.SAVE;
	}

	public void a(DefualtTaskActionCmd actionModel) {
	}

	protected void d(DefualtTaskActionCmd actionModel) {
	}

	protected void e(DefualtTaskActionCmd actionModel) {
	}

	public int getSn() {
		return 1;
	}

	public String getConfigPage() {
		return "";
	}

	public Boolean isDefault() {
		return true;
	}
	@Override
	protected void i(DefualtTaskActionCmd baseActionCmd) {
		this.d(baseActionCmd);
	}
	@Override
	protected void h(DefualtTaskActionCmd baseActionCmd) {
		this.e(baseActionCmd);
	}

	public void b(DefualtTaskActionCmd baseActionCmd) {
		this.a(baseActionCmd);
	}

}