package ru.steagle.views;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import ru.steagle.R;

/**
 * Created by bmw on 22.03.14.
 */
public class ImportantSwitcher {
    private TextView tvImportant;
    private TextView tvAll;
    private boolean showImportant;
    private Context context;

    public void reset(Context context, View view, final Runnable action) {
        this.context = context;
        tvAll = (TextView) view.findViewById(R.id.tvAll);
        tvImportant = (TextView) view.findViewById(R.id.tvImportant);
        tvAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImportantSwitch(false);
                    action.run();
            }
        });
        tvImportant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImportantSwitch(true);
                action.run();
            }
        });
        showImportantSwitch(true);
    }
    private void showImportantSwitch(boolean showImportant) {
        android.content.res.Resources resources = context.getResources();
        this.showImportant = showImportant;
        if (showImportant) {
            tvImportant.setBackgroundColor(resources.getColor(R.color.historySwitchSelectedColor));
            tvImportant.setTextColor(resources.getColor(R.color.historySwitchSelectedTextColor));
            tvAll.setBackgroundColor(resources.getColor(R.color.historySwitchUnselectedColor));
            tvAll.setTextColor(resources.getColor(R.color.historySwitchUnselectedTextColor));
        } else {
            tvAll.setBackgroundColor(resources.getColor(R.color.historySwitchSelectedColor));
            tvAll.setTextColor(resources.getColor(R.color.historySwitchSelectedTextColor));
            tvImportant.setBackgroundColor(resources.getColor(R.color.historySwitchUnselectedColor));
            tvImportant.setTextColor(resources.getColor(R.color.historySwitchUnselectedTextColor));
        }
    }


    public boolean isImportantSwitch() {
        return showImportant;
    }
}
