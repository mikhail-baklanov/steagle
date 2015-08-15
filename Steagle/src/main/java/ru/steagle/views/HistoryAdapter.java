package ru.steagle.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.steagle.R;
import ru.steagle.datamodel.DataModel;
import ru.steagle.datamodel.Event;
import ru.steagle.utils.Utils;

public class HistoryAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater;
    private List<Event> events = new ArrayList<>();
    private Context context;
    private DataModel dm;
    private SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat fe = new SimpleDateFormat("dd MMMM");


    public void setDataModel(DataModel dm) {
        this.dm = dm;
        notifyDataSetChanged();
    }

    public void addEvents(List<Event> list) {
        if (list != null) {
            events.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void cleatEvents() {
        events.clear();
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView time;
        TextView source;
        TextView mode;
        boolean withHeader;
        TextView headerDate;
    }

    public HistoryAdapter(Context context) {
        super();
        this.context = context;
        mLayoutInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        Event event = events.get(position);
        Date currentDate = event.getDate();
        Date prevDate = position == 0 ? null : events.get(position - 1).getDate();
        Date currentDay = Utils.getDay(currentDate);
        boolean withHeader = prevDate == null || !currentDay.equals(Utils.getDay(prevDate));

        if (convertView == null || withHeader != ((ViewHolder) convertView.getTag()).withHeader) {

            holder = new ViewHolder();
            holder.withHeader = withHeader;
            if (withHeader) {
                convertView = mLayoutInflater.inflate(R.layout.history_header,
                        null);
                holder.headerDate = (TextView) convertView
                        .findViewById(R.id.header);
            } else {
                convertView = mLayoutInflater.inflate(R.layout.history_row,
                        null);
            }
            holder.source = (TextView) convertView
                    .findViewById(R.id.source);
            holder.mode = (TextView) convertView
                    .findViewById(R.id.mode);
            holder.time = (TextView) convertView
                    .findViewById(R.id.time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (holder.withHeader) {
            holder.headerDate.setText(fe.format(currentDate).toUpperCase());
        }

        holder.time.setText(f.format(currentDate));
        String sensor;
        String mode;
        if (dm == null) {
            sensor = "...";
            mode = "...";
        } else {
            sensor = dm.getSensorDescription(event.getSensorId());
            if (sensor == null) {
                sensor = event.getSensorId() == null ? null : "...";
            }
            String source = dm.getDevModeSrcDescription(event.getDevModeSrcId());
            if (sensor == null) {
                if (source == null) {
                    sensor = "";
                } else {
                    sensor = source;
                }
            } else {
                // sensor != null
                if (source == null) {
                    if (event.getDevModeSrcId() != null)
                        sensor += " - " + "...";
                } else
                    sensor += " - " + source;
            }
            String predMode = dm.getDevModeDescription(event.getDevModeId());
            String curMode = dm.getDevModeDescription(event.getDevLastModeId());
            if (predMode == null) {
                predMode = event.getDevModeId() == null ? "" : "...";
            }
            if (curMode == null) {
                curMode = event.getDevLastModeId() == null ? "" : "...";
            }
            mode = ((predMode == null || predMode.isEmpty()) &&
                    (curMode == null || curMode.isEmpty())) ? "" : predMode + " -> " + curMode;
        }
        holder.source.setText(sensor);
        holder.mode.setText(mode);
        return convertView;
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int position) {
        return events.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}

