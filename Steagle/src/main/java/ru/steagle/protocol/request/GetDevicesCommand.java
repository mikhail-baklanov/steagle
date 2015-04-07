package ru.steagle.protocol.request;

import android.content.Context;

/**
 * Created by bmw on 16.02.14.
 */
public class GetDevicesCommand extends AccountCommand {
    public GetDevicesCommand(Context context) {
        super(context);
    }

    @Override
    protected String getRootTagName() {
        return "dev";
    }

    @Override
    protected String getCommandName() {
        return "fetch";
    }


}
