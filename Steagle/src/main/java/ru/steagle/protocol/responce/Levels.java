package ru.steagle.protocol.responce;

import ru.steagle.datamodel.Level;

/**
 * Created by bmw on 15.02.14.
 */
public class Levels extends SimpleDictionary<Level> {

    public Levels(String xml) {
        super(xml);
    }

    @Override
    public Level getNewInstance() {
        return new Level();
    }

    @Override
    public String getIdName() {
        return "level";
    }

}
