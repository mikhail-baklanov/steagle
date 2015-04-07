package ru.steagle.views;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import ru.steagle.R;

/**
 * Created by bmw on 09.02.14.
 */
public class MasterPasswordDialogFragment extends DialogFragment {

    public interface Listener {
        void onYesClick(String password, Dialog dialog);
    }

    private Listener listener;
    private View view;
    private EditText password1EditText;
    private EditText password2EditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_master_password_dialog, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setTitle(getString(R.string.changeMasterPassword));
        password1EditText = (EditText) (view.findViewById(R.id.password1));
        password2EditText = (EditText) (view.findViewById(R.id.password2));

        view.findViewById(R.id.btnYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!password1EditText.getText().toString().equals(password2EditText.getText().toString())) {
                    password2EditText.setError(getString(R.string.error_repeat_password));
                    password2EditText.requestFocus();
                } else {
                    if (listener != null)
                        listener.onYesClick(password1EditText.getText().toString(), getDialog());
                }
            }
        });

        view.findViewById(R.id.btnNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().cancel();
            }
        });
    }

    public MasterPasswordDialogFragment(Listener listener) {
        this.listener = listener;
    }

}