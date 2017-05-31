package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.icu.text.DecimalFormat;
import android.icu.text.NumberFormat;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.util.Locale;

/**
 * Created by vlado on 5/31/17.
 */

public class StockWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = StockWidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[0]),
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                String stockSymbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                Float stockPrice = data.getFloat(Contract.Quote.POSITION_PRICE);
                Float absoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                int backgroundDrawable;

                DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+");
                dollarFormatWithPlus.setMaximumFractionDigits(2);
                dollarFormat.setMaximumFractionDigits(2);
                dollarFormat.setMinimumFractionDigits(2);
                dollarFormatWithPlus.setMinimumFractionDigits(2);

                if (absoluteChange > 0) {
                    backgroundDrawable = R.drawable.percent_change_pill_green;
                } else {
                    backgroundDrawable = R.drawable.percent_change_pill_red;
                }
                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.list_item_quote);
                remoteViews.setTextViewText(R.id.symbol, stockSymbol);
                remoteViews.setTextViewText(R.id.price, dollarFormat.format(stockPrice));
                remoteViews.setTextViewText(R.id.change, dollarFormatWithPlus.format(absoluteChange));
                remoteViews.setInt(R.id.change, "setBackgroundResource", backgroundDrawable);
                //remoteViews.setInt(R.id.layout_list_item_quote, "setBackgroundResource", R.color.colorPrimaryDark);
                final Intent intent = new Intent();
                intent.putExtra(Intent.EXTRA_TEXT, stockSymbol);
                remoteViews.setOnClickFillInIntent(R.id.layout_list_item_quote, intent);
                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(Contract.Quote.POSITION_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}