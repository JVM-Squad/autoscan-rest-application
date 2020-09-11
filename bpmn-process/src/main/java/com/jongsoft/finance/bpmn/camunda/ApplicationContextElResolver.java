package com.jongsoft.finance.bpmn.camunda;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.Qualifier;
import io.micronaut.inject.qualifiers.Qualifiers;

public class ApplicationContextElResolver extends ELResolver {

    private final ApplicationContext applicationContext;

    public ApplicationContextElResolver(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if (base == null) {
            // according to javadoc, can only be a String
            String key = (String) property;

            Qualifier<Object> qualifier = Qualifiers.byName(key);
            if (applicationContext.containsBean(Object.class, qualifier)) {
                context.setPropertyResolved(true);
                return applicationContext.getBean(Object.class, qualifier);
            }
        }

        return null;
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return true;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        if (base == null) {
            String key = (String) property;
            if (applicationContext.containsBean(Object.class, Qualifiers.byName(key))) {
                throw new ProcessEngineException("Cannot set value of '" + property +
                        "', it resolves to a bean defined in the Micronaut application-context.");
            }
        }
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object arg) {
        return Object.class;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object arg) {
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object arg1, Object arg2) {
        return Object.class;
    }

}
