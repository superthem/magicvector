package cn.magicvector.common.application.annotation.monitor;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD})
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface LogFormat {
	/**
	 * Whether the Object text should be formated(e.g., JSON object format).<br>
	 * <b>false</b> means the object would be output as plain text.<br>
	 * <b>otherwise</b>, the object would be output as formatted text.<br>
	 * <b>default value</b>: false
	 */
	public boolean enable() default false;
	/**
	 * Whether the request and response should be output respectively.<br>
	 * <b>false</b> means the logger would output request and response together in one log information.<br>
	 * <b>otherwise</b>, it would output the request and response respectively one by one.<br>
	 * <b>default value</b>: false
	 */
	public boolean seperate() default false;
}
