package com.lordsinfotech.fosifeel;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.lordsinfotech.fosifeel.classes.RawData;
import com.lordsinfotech.fosifeel.helper.Constants;
import com.lordsinfotech.fosifeel.helper.RequestHandler;
import com.lordsinfotech.fosifeel.helper.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddLeadActivity extends AppCompatActivity {
    ImageView ivClose;
    EditText etCustomerPhone,etCustomerName,etCompanyName,etAddress,etEmail,etDOB,etPan,etIncome,etExistingCC,etLimit,etRemark;
    Spinner spIncomeType,spProductType,spLeadType;
    private ProgressDialog progressDialog;
    ArrayList<String> productType = new ArrayList<String>();
    ArrayList<String> incomeTypes = new ArrayList<String>();
    ArrayList<String> leadType = new ArrayList<String>();
    RawData rawData;
    String tc_id;
    Button btnSave,btnCancel;
    public ArrayAdapter<String> productArrayAdapter,incomeTypesAdapter,leadTypeAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lead);

        tc_id= SharedPrefManager.getInstance(this).getId();
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        etCustomerPhone=(EditText)findViewById(R.id.etCustomerPhone);
        etCustomerName=(EditText)findViewById(R.id.etCustomerName);
        etCompanyName=(EditText)findViewById(R.id.etCompanyName);
        etEmail=(EditText)findViewById(R.id.etEmail);
        etAddress=(EditText)findViewById(R.id.etCustomerAddress);
        etDOB=(EditText)findViewById(R.id.etDOB);
        etPan=(EditText)findViewById(R.id.etPan);
        etIncome=(EditText)findViewById(R.id.etIncome);
        etExistingCC=(EditText)findViewById(R.id.etExistingCC);
        etLimit=(EditText)findViewById(R.id.etLimit);
        etRemark=(EditText)findViewById(R.id.etRemark);

        spLeadType=(Spinner) findViewById(R.id.spLeadType);
        spIncomeType=(Spinner) findViewById(R.id.spIncomeType);
        spProductType=(Spinner) findViewById(R.id.spProductType);

        leadType.add("Personal Contact");
        leadType.add("Return Call");
        leadTypeAdapter=new ArrayAdapter<String>(getApplicationContext(),R.layout.spin_layout,leadType);
        spLeadType.setAdapter(leadTypeAdapter);

        incomeTypes.add("Select");
        incomeTypes.add("Salary");
        incomeTypes.add("Wages");
        incomeTypes.add("Business");
        incomeTypes.add("Self Employed");
        incomeTypesAdapter=new ArrayAdapter<String>(getApplicationContext(),R.layout.spin_layout,incomeTypes);
        spIncomeType.setAdapter(incomeTypesAdapter);

        btnSave=(Button)findViewById(R.id.btnSave);
        btnCancel=(Button)findViewById(R.id.btnCancel);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save_calling_data();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ivClose=(ImageView)findViewById(R.id.ivClose);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    finish();
            }
        });

        etCustomerPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                spProductType.setSelection(productType.indexOf("-Product Type-"));
                leadTypeAdapter=null;
                etCustomerName.setText("");
                etAddress.setText("");
                etPan.setText("");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String phone = etCustomerPhone.getText().toString();
                if (phone.length() == 10) {
                    search_customer(phone);
                }
            }
        });

        load_spinner_data();

    }

    private void search_customer(String phone) {
        rawData=null;
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                Constants.SEARCH_CUSTOMER+"/"+phone,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getBoolean("status")) {
                                rawData=new RawData(
                                        obj.getInt("id"),
                                        obj.getString("name"),
                                        obj.getString("address"),
                                        obj.getString("pan"),
                                        obj.getString("phone"),
                                        obj.getString("prod_name")
                                );
                                spLeadType.setSelection(leadType.indexOf("Return Call"),true);
                                spProductType.setSelection(productType.indexOf(obj.getString("prod_name")));
                                etCustomerName.setText(obj.getString("name"));
                                etAddress.setText(obj.getString("address"));
                                if(obj.getString("pan") != null)
                                    etPan.setText(obj.getString("pan"));

                            } else {
                                spLeadType.setSelection(leadType.indexOf("Personal Contact"),true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 30000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
    }

    private void load_spinner_data() {
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                Constants.GET_DISPOSITIONS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getBoolean("status")) {
                                JSONArray products = obj.getJSONArray("products");
                                productType.add("-Product Type-");
                                for (int i = 0; i < products.length(); i++) {
                                    JSONObject json_data = products.getJSONObject(i);
                                    productType.add(json_data.getString("name"));
                                }
                                productArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spin_layout, productType);
                                spProductType.setAdapter(productArrayAdapter);

                            } else {
                                Toast.makeText(getApplicationContext(), "No dispositions found..", Toast.LENGTH_SHORT).show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 30000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
    }

    private void save_calling_data() {
        progressDialog.show();
        StringRequest stringRequest=new StringRequest(
                Request.Method.POST,
                Constants.SAVE_CALLING_DATA_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject obj=new JSONObject(response);
                            if(obj.getBoolean("status")){
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(getApplicationContext(),obj.getString("message"),Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();

                if(rawData!=null) {
                    params.put("raw_id", String.valueOf(rawData.getId()));
                }
                else {
                    params.put("raw_id", "0");
                }
                    params.put("disposition","8");
                    params.put("lead","1");
                    params.put("presentation","0");
                    params.put("callDuration","0");
                    params.put("callback","0");
                    params.put("tc_id",tc_id);
                    params.put("name",etCustomerName.getText().toString());
                    params.put("company",etCompanyName.getText().toString());
                    params.put("email",etEmail.getText().toString());
                    params.put("address",etAddress.getText().toString());
                    params.put("dob",etDOB.getText().toString());
                    params.put("pan",etPan.getText().toString());
                    params.put("income_type",spIncomeType.getSelectedItem().toString());
                    params.put("income",etIncome.getText().toString());
                    params.put("existing_cc",etExistingCC.getText().toString());
                    params.put("cc_limit",etLimit.getText().toString());
                    params.put("product_type",spProductType.getSelectedItem().toString());
                    params.put("sub_product_type","");
                    params.put("remark",etRemark.getText().toString());
                    params.put("callback","0");

                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 30000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
    }

}