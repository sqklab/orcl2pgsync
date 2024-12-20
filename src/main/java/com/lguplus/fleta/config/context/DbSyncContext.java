package com.lguplus.fleta.config.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Sep 2021
 */
@Component
public final class DbSyncContext implements ApplicationContextAware {

	private static ApplicationContext context = null;

	/**
	 * Returns the Spring managed bean instance of the given class type if it exists.
	 * Returns null otherwise.
	 *
	 * @param beanClass
	 * @return
	 */
	public static <T extends Object> T getBean(Class<T> beanClass) {
		return context.getBean(beanClass);
	}

	/**
	 * Get applicationContext
	 *
	 * @return
	 */
	public static ApplicationContext getContext() {
		return context;
	}

	/**
	 * Get Bean through name
	 *
	 * @param name
	 * @return
	 */
	public static Object getBean(String name) {
		return getContext().getBean(name);
	}

	/**
	 * Return the specified Bean through name and Clazz
	 *
	 * @param name
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public static <T> T getBean(String name, Class<T> clazz) {
		return getContext().getBean(name, clazz);
	}

	/**
	 * Obtain a bean of type {@code T} from the given {@code BeanFactory} declaring a
	 * qualifier (e.g. via {@code <qualifier>} or {@code @Qualifier}) matching the given qualifier, or having a bean
	 * name matching the given qualifier.
	 *
	 * @param beanType
	 * @param qualifier
	 * @param <T>
	 * @return
	 */
	@Deprecated(since = "0.2.8")
	public static <T> T qualifiedBeanOfType(Class<T> beanType, String qualifier) {
		try {
			return BeanFactoryAnnotationUtils.qualifiedBeanOfType(getContext(), beanType, qualifier);
		} catch (BeansException ex) {
			return getContext().getBeansOfType(beanType).get(qualifier);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		// TODO: store ApplicationContext reference to access required beans later on
		if (DbSyncContext.context == null) {
			DbSyncContext.context = context;
		}
	}
}
