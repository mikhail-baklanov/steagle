package ru.steagle.protocol.responce;

import ru.steagle.datamodel.Currency;

/**
 * Created by bmw on 15.02.14.
 */
public class Currencies extends SimpleDictionary<Currency> {

    public Currencies(String xml) {
        super(xml);
    }

    @Override
    public Currency getNewInstance() {
        return new Currency();
    }

    @Override
    public String getIdName() {
        return "id_currency";
    }
}
