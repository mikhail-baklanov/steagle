package ru.steagle.protocol.request;

/**
 * Created by bmw on 16.02.14.
 */
public class GetUserStatusesCommand extends Command {
    @Override
    protected String getRootTagName() {
        return "var";
    }

    @Override
    protected String getCommandName() {
        return "status_user";
    }

}
