package ru.steagle.protocol.request;

import android.content.Context;

/**
 * Created by bmw on 16.02.14.
 */
public class GetNotifySessionCommand extends AccountCommand {
    public GetNotifySessionCommand(Context context) {
        super(context);
    }

    @Override
    protected String getRootTagName() {
        return "user";
    }

    @Override
    protected String getCommandName() {
        return "reg";
    }


}
