package ru.steagle.views;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ru.steagle.R;

/**
 * Created by bmw on 09.02.14.
 */
public class PeriodEditDialogFragment extends DialogFragment {

    public interface Listener {
        void onYesClick(Date startDate, Date endDate);
    }

    private Listener listener;
    private View view;
    private String title;
    private Date startDate;
    private Date endDate;
    private String yesButtonText;
    private String noButtonText;
    private TextView tvStartDate;
    private TextView tvEndDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_period, container, false);
        tvStartDate = (TextView) view.findViewById(R.id.period_start);
        tvEndDate = (TextView) view.findViewById(R.id.period_end);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setTitle(title);

        updateDateFields();
        tvStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener myCallBack = new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        Calendar c = GregorianCalendar.getInstance();
                        c.set(year, monthOfYear, dayOfMonth);
                        startDate = c.getTime();
                        updateDateFields();
                    }
                };
                Calendar c = GregorianCalendar.getInstance();
                c.setTime(startDate == null ? new Date() : startDate);
                DatePickerDialog dialog = new DatePickerDialog(getActivity(), myCallBack, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dialog.show();

            }
        });
        tvEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener myCallBack = new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        Calendar c = GregorianCalendar.getInstance();
                        c.set(year, monthOfYear, dayOfMonth);
                        endDate = c.getTime();
                        updateDateFields();
                    }
                };
                Calendar c = GregorianCalendar.getInstance();
                c.setTime(endDate == null ? new Date() : endDate);
                DatePickerDialog dialog = new DatePickerDialog(getActivity(), myCallBack, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });
        ((Button) (view.findViewById(R.id.btnYes))).setText(yesButtonText);
        view.findViewById(R.id.btnYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onYesClick(startDate, endDate);
                getDialog().cancel();
            }
        });
        ((Button) (view.findViewById(R.id.btnNo))).setText(noButtonText);
        view.findViewById(R.id.btnNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().cancel();
            }
        });

    }

    private void updateDateFields() {
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        tvStartDate.setText(
                startDate == null ? getActivity().getResources().getString(R.string.empty_date) : df.format(startDate));
        tvEndDate.setText(
                endDate == null ? getActivity().getResources().getString(R.string.empty_date) : df.format(endDate));
    }

    public PeriodEditDialogFragment(String title, Date startDate, Date endDate, String yesButtonText, String noButtonText, Listener listener) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.yesButtonText = yesButtonText;
        this.noButtonText = noButtonText;
        this.listener = listener;
    }

}