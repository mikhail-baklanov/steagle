package ru.steagle.protocol.responce;

import org.xml.sax.Attributes;

public class BaseResult {
    private boolean ok;
    private String message;

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }

    protected void setMessage(String message) {
        this.message = message;
    }
    protected void setOk(boolean ok) {
        this.ok = ok;
    }

    protected void readBaseFields(Attributes attrib) {
        ok = "success".equals(attrib
                .getValue("res"));
        if (!ok)
            setMessage(attrib.getValue("msg"));
    }

    protected void setParsingError(Throwable e) {
        ok = false;
        setMessage("Ошибка парсинга данных: " + e.getMessage());
    }
}
