package com.devefx.validation;

import com.devefx.validation.kit.JsonKit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validator
 * Created by YYQ on 2016/5/26.
 */
public abstract class Validator implements Script {

    private final List<ConstraintValidator> modules;
    private boolean invalid;
    private boolean shortCircuit;
    private int globalId;
    private final Map<String, String> errorMap;
    /**
     * 构造函数
     */
    public Validator() {
        modules = new ArrayList<ConstraintValidator>();
        invalid = false;
        shortCircuit = false;
        errorMap = new HashMap<String, String>();
    }
    /**
     * 是否启用短路模式
     * @param shortCircuit
     */
    public void setShortCircuit(boolean shortCircuit) {
        this.shortCircuit = shortCircuit;
    }
    /**
     * 获取全局标识
     * @return int
     */
    public int getGlobalId() {
        return globalId;
    }
    /**
     * 设置全局标识
     * @param globalId
     */
    public void setGlobalId(int globalId) {
        this.globalId = globalId;
    }
    /**
     * 添加一个验证器
     * @param constraintValidator
     */
    public final void add(ConstraintValidator constraintValidator) {
        modules.add(constraintValidator);
    }
    /**
     * 重置
     */
    public final void reset() {
        invalid = false;
        errorMap.clear();
    }
    /**
     * 设置
     */
    public abstract void setup();
    /**
     * 执行验证
     * @param request
     * @param response
     * @return
     */
    public final boolean process(HttpServletRequest request, HttpServletResponse response) {
        synchronized (errorMap) {
            for (ConstraintValidator module: modules) {
                try {
                    if (!module.isValid(request)) {
                        invalid = true;
                        Error error = module.getError();
                        if (!errorMap.containsKey(error.getCode())) {
                            errorMap.put(error.getCode(), error.getMessage());
                        }
                        if (shortCircuit) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (invalid) {
                handlerError(request, response);
                return false;
            }
            return true;
        }
    }
    
    public final boolean moduleValidate(int moduleId, HttpServletRequest request, 
            HttpServletResponse response) throws Exception {
        int index = 0;
        for (ConstraintValidator module: modules) {
            if (module instanceof Script) {
                if (index == moduleId) {
                    return module.isValid(request);
                }
                index++;
            }
        }
        return false;
    }
    
    /**
     * 错误处理
     * @param request
     * @param response
     */
    public void handlerError(HttpServletRequest request, HttpServletResponse response) {
        Result<Map<String, String>> result = new Result<Map<String, String>>(!invalid, errorMap);
        try {
            response.setContentType("application/x-javascript; charset=utf-8");
            PrintWriter out = response.getWriter();
            out.write(JsonKit.toJson(result));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private volatile String script;
    /**
     * Script输出
     * @param out
     * @throws IOException
     */
    @Override
    public void output(Writer out) throws IOException {
        final String TAB = "    ";
        if (script == null) {
            StringWriter writer = new StringWriter();
            writer.append("(function() {\n");
            writer.append(TAB + "validatorManager.register(\"");
            writer.append(getClass().getSimpleName());
            writer.append("\", ");
            writer.append(String.valueOf(globalId));
            writer.append(", function() {\n");
            writer.append(TAB + TAB + "Validator.apply(this);\n");
            writer.append(TAB + TAB + "this.setup = function () {\n");
            if (shortCircuit) {
                writer.append(TAB + TAB + TAB + "this.setShortCircuit(true);\n");
            }
            for (ConstraintValidator module: modules) {
                if (module instanceof Script) {
                    writer.append(TAB + TAB + TAB + "this.add(new ");
                    ((Script) module).output(writer);
                    writer.append(");\n");
                }
            }
            writer.append(TAB + TAB + "};\n");
            writer.append(TAB + "});\n");
            writer.append("})();");
            script = writer.toString();
        }
        out.write(script);
    }
}
