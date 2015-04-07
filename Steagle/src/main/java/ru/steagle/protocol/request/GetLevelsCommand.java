package ru.steagle.protocol.request;

/**
 * Created by bmw on 16.02.14.
 */
public class GetLevelsCommand extends Command {
    @Override
    protected String getRootTagName() {
        return "var";
    }

    @Override
    protected String getCommandName() {
        return "level";
    }

}

