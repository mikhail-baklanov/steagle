package ru.steagle.views;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import ru.steagle.R;

/**
 * Created by bmw on 08.02.14.
 */
public class MenuFragment extends Fragment {
    private static final int[][] IMAGES = {{R.drawable.account, R.drawable.account_green},
            {R.drawable.devices, R.drawable.devices_green},
            {R.drawable.journal, R.drawable.journal_green},
            {R.drawable.settings, R.drawable.settings_green}};
    private static final int HOME_INDEX = 0;
    private static final int DEVICES_INDEX = 1;
    private static final int JOURNAL_INDEX = 2;
    private static final int SETTINGS_INDEX = 3;

    public interface Listener {
        void onAccountClick();

        void onDevicesClick();

        void onHistoryClick();

        void onSettingsClick();

    }

    private View view;
    private Listener listener;
    private int currentIndex = -1;

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = activity instanceof Listener ? (Listener) activity : null;
    }

    public boolean goHome() {
        if (currentIndex != HOME_INDEX) {
            changeCurrentButton(HOME_INDEX);
            return true;
        } else {
            return false;
        }
    }

    private void changeCurrentButton(int index) {
        if (currentIndex == index)
            return;
        currentIndex = index;
        ((ImageButton) (view.findViewById(R.id.btnAccount))).setImageResource(IMAGES[0][index == HOME_INDEX ? 1 : 0]);
        ((ImageButton) (view.findViewById(R.id.btnDevices))).setImageResource(IMAGES[1][index == DEVICES_INDEX ? 1 : 0]);
        ((ImageButton) (view.findViewById(R.id.btnHistory))).setImageResource(IMAGES[2][index == JOURNAL_INDEX ? 1 : 0]);
        ((ImageButton) (view.findViewById(R.id.btnSettings))).setImageResource(IMAGES[3][index == SETTINGS_INDEX ? 1 : 0]);
        if (listener != null)
            switch (index) {
                case HOME_INDEX:
                    listener.onAccountClick();
                    break;
                case DEVICES_INDEX:
                    listener.onDevicesClick();
                    break;
                case JOURNAL_INDEX:
                    listener.onHistoryClick();
                    break;
                default:
                    listener.onSettingsClick();
            }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_menu, container, false);
        view.findViewById(R.id.btnAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeCurrentButton(HOME_INDEX);
            }
        });
        view.findViewById(R.id.btnDevices).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeCurrentButton(DEVICES_INDEX);
            }
        });
        view.findViewById(R.id.btnHistory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeCurrentButton(JOURNAL_INDEX);
            }
        });
        view.findViewById(R.id.btnSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeCurrentButton(SETTINGS_INDEX);
            }
        });
        goHome();
        return view;
    }
}