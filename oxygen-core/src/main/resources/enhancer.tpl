package ${packageName};

import java.lang.reflect.Method;
import vip.justlive.oxygen.core.aop.Callback;
import vip.justlive.oxygen.core.aop.Invocation;
import vip.justlive.oxygen.core.aop.proxy.CompilerBeanProxy;

public class ${className}${generic} extends ${targetClass}${targetGeneric} {

  #{for (var i in constructors){ var constructor = constructors[i];}
  public ${className}(#{for (var j in constructor.params) {}${constructor.params[j]} p${j}#{if (j < constructor.params.length - 1) {}, #{\}}#{\}}) #{for (var k in constructor.exceptions) { var exception = constructor.exceptions[k];}#{if (k == 0) {} throws #{\}}${exception}#{if (k < constructor.exceptions.length - 1) {}, #{\}}#{\}}{
    super(#{for (var h in constructor.params) {}p${h}#{if (h < constructor.params.length - 1) {}, #{\}}#{\}});
  }#{\}}

	#{for (var i in methods) { var method = methods[i];}
	@Override
	public ${method.generic}${method.returnType} ${method.name}(#{for (var j in method.params) {}${method.params[j]} p${j}#{if (j < method.params.length - 1) {}, #{\}}#{\}}) #{for (var k in method.exceptions) { var exception = method.exceptions[k];}#{if (k == 0) {}throws #{\}}${exception}#{if (k < method.exceptions.length - 1) {}, #{\}}#{\}} {
		Method method = CompilerBeanProxy.lookup(${method.key});
		Invocation invocation = new Invocation(this, method, new Object[]{#{for (var h in method.params) {}p${h}#{if (h < method.params.length - 1) {}, #{\}}#{\}}});
		invocation.intercept(new Callback() {
			@Override
			public Object apply(Invocation ivt) throws Throwable {
			  #{if (method.returnType != 'void') {}return #{\}}${className}.super.${method.name}(#{for (var h in method.params) {}p${h}#{if (h < method.params.length - 1) {}, #{\}}#{\}});
			  #{if (method.returnType == 'void') {}return null;#{\}}
			}
		});
		#{if (method.returnType != 'void') {}return (${method.returnType}) invocation.getReturnValue();#{\}}
	}
	#{ \}}
}
