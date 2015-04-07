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

    public interface Listener {
        void onAccountClick();

        void onDevicesClick();

        void onHistoryClick();

        void onSettingsClick();

    }

    private View view;
    private Listener listener;

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

    private void changeCurrentButton(int index) {
        ((ImageButton)(view.findViewById(R.id.btnAccount))).setImageResource(IMAGES[0][index == 0 ? 1:0]);
        ((ImageButton)(view.findViewById(R.id.btnDevices))).setImageResource(IMAGES[1][index == 1 ? 1:0]);
        ((ImageButton)(view.findViewById(R.id.btnHistory))).setImageResource(IMAGES[2][index == 2 ? 1:0]);
        ((ImageButton)(view.findViewById(R.id.btnSettings))).setImageResource(IMAGES[3][index == 3 ? 1:0]);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_menu, container, false);
        view.findViewById(R.id.btnAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeCurrentButton(0);
                if (listener != null)
                    listener.onAccountClick();
            }
        });
        view.findViewById(R.id.btnDevices).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeCurrentButton(1);
                if (listener != null)
                    listener.onDevicesClick();
            }
        });
        view.findViewById(R.id.btnHistory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeCurrentButton(2);
                if (listener != null)
                    listener.onHistoryClick();
            }
        });
        view.findViewById(R.id.btnSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeCurrentButton(3);
                if (listener != null)
                    listener.onSettingsClick();
            }
        });
        changeCurrentButton(0);
        if (listener != null)
            listener.onAccountClick();
        return view;
    }
}