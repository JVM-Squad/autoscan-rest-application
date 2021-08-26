package com.jongsoft.finance.bpmn.delegate.rule;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.serialized.RuleConfigJson;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ParseTransactionRuleDelegate implements JavaDelegate {

    private final StorageService storageService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Processing raw json file in {}",
                execution.getCurrentActivityName(),
                execution.getActivityInstanceId());

        String storageToken = (String) execution.getVariableLocal("storageToken");
        final byte[] rawRuleConfig = storageService.read(storageToken).block();

        var configJson = RuleConfigJson.read(new String(rawRuleConfig));

        execution.setVariable("ruleLines", configJson.getRules());
    }

}
