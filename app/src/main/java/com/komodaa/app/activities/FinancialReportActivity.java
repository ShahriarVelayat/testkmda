package com.komodaa.app.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.interfaces.NetworkCallback;
import com.komodaa.app.models.Error;
import com.komodaa.app.models.FinancialReport;
import com.komodaa.app.utils.ApiClient;
import com.komodaa.app.utils.Utils;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FinancialReportActivity extends AppCompatActivity {

    @BindView(R.id.tvCurrentCreditBalance) TextView tvCurrentCreditBalance;
    @BindView(R.id.tvPaymentSum) TextView tvPaymentSum;
    @BindView(R.id.tvSoldSum) TextView tvSoldSum;
    @BindView(R.id.recList) RecyclerView recList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial_report);

        getSupportActionBar().setTitle(Utils.generateTypeFacedString(this, getTitle().toString()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        tvCurrentCreditBalance.setText(String.format("%s تومان", 0));
        tvPaymentSum.setText(String.format("%s تومان", 0));
        tvSoldSum.setText(String.format("%s تومان", 0));

        String confs = App.getPrefs().getString("user_meta", null);
        try {
            JSONObject j = new JSONObject(confs);
            // Set address field based on user meta
            if (j.has("meta")) {
                JSONArray meta = j.getJSONArray("meta");
                for (int i = 0; i < meta.length(); i++) {
                    JSONObject m = meta.getJSONObject(i);
                    if (m.getString("meta").equals("user_balance")) {
                        int amount = m.getInt("value");
                        tvCurrentCreditBalance.setText(String.format("%s تومان", Utils.formatNumber(amount > 0 ? amount : 0)));
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recList.setLayoutManager(mLayoutManager);
        recList.setHasFixedSize(true);
        recList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).size(Utils.dpToPx(this, 8)).colorResId(android.R.color.transparent).build());
        final ProgressDialog pd = new ProgressDialog(this);

        pd.setMessage(getString(R.string.loadin_please_wait));
        pd.setIndeterminate(true);
        pd.show();
        ApiClient.makeRequest(this, "GET", "/finance/report", null, new NetworkCallback() {
            @Override
            public void onSuccess(int status, JSONObject data) {
                pd.dismiss();
                if (data == null) {
                    return;
                }
                try {
                    List<FinancialReport> list = new ArrayList<>();
                    JSONArray array = data.getJSONArray("data");
                    int sum = data.getInt("sum");
                    Log.d("Sum", data.getString("sum_sold"));
                    tvPaymentSum.setText(String.format("%s تومان", sum));
                    tvSoldSum.setText(String.format("%s تومان", data.getInt("sum_sold")));
                    for (int i = 0; i < array.length(); i++) {
                        list.add(new FinancialReport(array.getJSONObject(i)));
                    }
                    ReportAdapter adapter = new ReportAdapter(list);
                    recList.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int status, Error error) {
                pd.dismiss();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.help:
                Intent intent = new Intent(this, HelpActivity.class);
                intent.putExtra("title", "راهنما");
                intent.putExtra("url", "https://komodaa.com/help/financial");
                startActivity(intent);
                return true;

            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.financial_report, menu);
        return true;
    }

    class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
        private List<FinancialReport> list;
        private Context c;

        public ReportAdapter(List<FinancialReport> list) {
            this.list = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            c = parent.getContext();
            return new ViewHolder(getLayoutInflater().inflate(R.layout.row_report, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            FinancialReport f = list.get(position);
            holder.amount.setText(String.format(Locale.US, "%d تومان", f.getAmount()));
            holder.date.setText(Utils.getPersianDateText(f.getDate(), true));
            if (f.getStatusCode() == 1) {
                holder.ind.setBackgroundColor(Utils.getColorFromResource(c, R.color.colorPrimary));
            } else {
                holder.ind.setBackgroundColor(Utils.getColorFromResource(c, R.color.notification_error));
            }

        }

        @Override
        public int getItemCount() {
            //return 5;
            return list == null ? 0 : list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.tvAmount)
            TextView amount;
            @BindView(R.id.tvDate)
            TextView date;
            @BindView(R.id.vInc)
            View ind;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
