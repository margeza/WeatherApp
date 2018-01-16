package com.example.marta.weatherapp;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.marta.weatherapp.data.Forecast;

/**
 * Created by Marta on 2018-01-05.
 */

class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private Forecast forecast;
    private static RecyclerViewClickListener itemListener;
    private int focusedItem = 1;

    public MainAdapter(Forecast forecast, RecyclerViewClickListener itemListener) {
        this.forecast = forecast;
        this.itemListener = itemListener;
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        recyclerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();

                // Return false if scrolled to the bounds and allow focus to move off the list
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return tryMoveSelection(lm, 1);
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        return tryMoveSelection(lm, -1);
                    }
                }

                return false;
            }
        });
    }

    private boolean tryMoveSelection(RecyclerView.LayoutManager lm, int direction) {
        int tryFocusItem = focusedItem + direction;

        // If still within valid bounds, move the selection, notify to redraw, and scroll
        if (tryFocusItem >= 0 && tryFocusItem < getItemCount()) {
            notifyItemChanged(focusedItem);
            focusedItem = tryFocusItem;
            notifyItemChanged(focusedItem);
            lm.scrollToPosition(focusedItem);
            return true;
        }

        return false;
    }

    @Override
    public MainAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MainAdapter.ViewHolder holder, final int position) {
        if(focusedItem == position)
            holder.itemView.setBackgroundResource(R.color.transparent_color_selected);
        else
            holder.itemView.setBackgroundResource(R.color.transparent_color);

        holder.itemView.setSelected(focusedItem == position);
        Typeface custom_font = Typeface.createFromAsset(holder.itemView.getContext().getAssets(), "fonts/abelregular.ttf");
        int resourceForecast = holder.itemView.getContext().getResources().getIdentifier("drawable/icon_"+ forecast.getCode(position), null, holder.itemView.getContext().getPackageName());
        Drawable weatherForecastIconDrawable = holder.itemView.getContext().getResources().getDrawable(resourceForecast);
        holder.mTitle.setText(forecast.getDay(position));
        holder.mImage.setImageDrawable(weatherForecastIconDrawable);
        holder.mMaxTemp.setText(forecast.getTempHigh(position)+"\u00B0");
        holder.mMinTemp.setText(forecast.getTempLow(position)+"\u00B0");
        holder.mTitle.setTypeface(custom_font);
        holder.mMaxTemp.setTypeface(custom_font);
        holder.mMinTemp.setTypeface(custom_font);

        holder.mListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemListener.recyclerViewListClicked(view, position, forecast);
                notifyItemChanged(focusedItem);
                focusedItem = holder.getLayoutPosition();
                notifyItemChanged(focusedItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return forecast.getDataLenght();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView mTitle;
        ImageView mImage;
        TextView mMaxTemp;
        TextView mMinTemp;
        LinearLayout mListItem;



        ViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.day);
            mImage = (ImageView) itemView.findViewById(R.id.weather_image);
            mMaxTemp = (TextView) itemView.findViewById(R.id.tempMax);
            mMinTemp = (TextView) itemView.findViewById(R.id.tempMin);
            mListItem = (LinearLayout) itemView.findViewById(R.id.list_item_linear_layout);
        }
    }
}
