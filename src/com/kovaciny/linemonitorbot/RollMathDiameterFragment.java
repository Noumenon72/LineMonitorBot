package com.kovaciny.linemonitorbot;

import java.text.DecimalFormat;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kovaciny.helperfunctions.HelperFunction;
import com.kovaciny.helperfunctions.MutuallyExclusiveViewSet;
import com.kovaciny.primexmodel.PrimexModel;

public class RollMathDiameterFragment extends Fragment implements View.OnClickListener {

    Button mBtn_getDiameter;
    
    EditText mEdit_linearFeet;
    EditText mEdit_orderedGauge;
    EditText mEdit_grossWeight;
    EditText mEdit_materialDensity;
    EditText mEdit_width;
    
    LinearLayout mContainer_diameterInputs1;
    LinearLayout mContainer_diameterInputs2;
    
    MutuallyExclusiveViewSet<ViewGroup> mMutuallyExclusiveViewSet;
    
    TextView mTxt_rollDiameter;
    TextView mTxt_rollDiameterHigh;
    
    double mWidth;
    int mLinearFeet;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
        mLinearFeet = settings.getInt("RollMath.linearFeet", 0);
        mWidth = Double.valueOf(settings.getFloat("RollMath.width", 0f));
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.roll_math_diameter_fragment, container, false);
        
        mBtn_getDiameter = (Button) rootView.findViewById(R.id.btn_get_diameter);
        mBtn_getDiameter.setOnClickListener(this);
    
        mContainer_diameterInputs1 = (LinearLayout) rootView.findViewById(R.id.container_diameter_inputs_1);
        mContainer_diameterInputs2 = (LinearLayout) rootView.findViewById(R.id.container_diameter_inputs_2);
        
        mEdit_orderedGauge = (EditText) rootView.findViewById(R.id.edit_ordered_gauge);
        mEdit_linearFeet = (EditText) rootView.findViewById(R.id.edit_linear_feet);
        if (mLinearFeet > 0) {
            mEdit_linearFeet.setText(String.valueOf(mLinearFeet));
        }
        mEdit_grossWeight = (EditText) rootView.findViewById(R.id.edit_gross_weight);
        mEdit_materialDensity = (EditText) rootView.findViewById(R.id.edit_material_density);
        mEdit_width = (EditText) rootView.findViewById(R.id.edit_width);
        if (mWidth > 0) {
        	mEdit_width.setText(String.valueOf(mWidth));
        }
        mTxt_rollDiameter = (TextView) rootView.findViewById(R.id.txt_roll_diameter);
        mTxt_rollDiameterHigh = (TextView) rootView.findViewById(R.id.txt_roll_diameter_high);
        
        HashMap<ViewGroup, EditText> containerToRequiredFieldMap = new HashMap<ViewGroup, EditText>();
        containerToRequiredFieldMap.put(mContainer_diameterInputs1, mEdit_orderedGauge);
        containerToRequiredFieldMap.put(mContainer_diameterInputs2, mEdit_grossWeight);
        mMutuallyExclusiveViewSet = 
                new MutuallyExclusiveViewSet<ViewGroup>(
                        getActivity(), containerToRequiredFieldMap, R.drawable.selector_viewgroup_exclusive);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_get_diameter) {
            if (validateInputs()) {
                HelperFunction.hideKeyboard(getActivity());
                
                ViewGroup selectedGroup = (ViewGroup) getView().findViewById(mMutuallyExclusiveViewSet.getValidGroupId());
                if (selectedGroup.findFocus() != null) {
                    selectedGroup.findFocus().clearFocus();
                }
                
                SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                
                double diameter = 0d;
                SpannableStringBuilder diameterSb = new SpannableStringBuilder();
                SpannableStringBuilder diameterHighSb = new SpannableStringBuilder();
                
                if (selectedGroup.getId() == R.id.container_diameter_inputs_1) { 
                    int linearFeet = Integer.valueOf(mEdit_linearFeet.getText().toString());
                    double orderedGauge = Double.valueOf(mEdit_orderedGauge.getText().toString());
                    diameter = ((RollMathActivity)getActivity())
                            .calculateRollDiameter(((RollMathActivity)getActivity()).getCoreType(), linearFeet, orderedGauge);
                    editor.putFloat("RollMath.orderedGauge", (float) orderedGauge);
                    editor.putInt("RollMath.linearFeet", linearFeet);
                    
                } else if (selectedGroup.getId() == R.id.container_diameter_inputs_2) {
                    double rollWidth = Double.valueOf(mEdit_width.getText().toString());
                    double grossWeight = Double.valueOf(mEdit_grossWeight.getText().toString());
                    double materialDensity = Double.valueOf(mEdit_materialDensity.getText().toString());
                    double diameterHigh = ((RollMathActivity)getActivity())
                            .calculateRollDiameter(((RollMathActivity)getActivity()).getCoreType(), rollWidth, grossWeight, materialDensity);
                    diameter = diameterHigh * .975; //Represents running at -2.5% of ordered gauge
                    editor.putFloat("RollMath.width", (float) rollWidth);
                    editor.putFloat("RollMath.grossWeight", (float) grossWeight);
                    editor.putFloat("RollMath.materialDensity", (float) materialDensity);
                    diameterHighSb.append("(")
                    .append(HelperFunction.formatDecimalAsProperFraction(diameterHigh, 8d))
                    .append("\" at full ordered gauge)");
                }
                 
                diameterSb.append(HelperFunction.formatDecimalAsProperFraction(diameter, 8d))
                    .append("\"");
                mTxt_rollDiameter.setText(diameterSb);
                mTxt_rollDiameterHigh.setText(diameterHighSb);

                editor.putFloat("RollMath.diameter", (float) diameter);
                editor.commit();
            }
        }
    }

    private boolean validateInputs() {        
        boolean validInputs = true;
        int selectedGroup = mMutuallyExclusiveViewSet.getValidGroupId();
        if (selectedGroup == 0) {
            validInputs = false;
        } else {
            LinearLayout validGroup = (LinearLayout) getView().findViewById(mMutuallyExclusiveViewSet.getValidGroupId());
            if (validGroup == mContainer_diameterInputs1) {
                if (mEdit_linearFeet.getText().length() == 0) {
                    mEdit_linearFeet.setError(getString(R.string.error_empty_field));
                    validInputs = false;
                }   
                //auto-convert if user enters gauge as a whole number instead of a decimal, then format as gauge
                double gaugeValue = Double.valueOf(mEdit_orderedGauge.getText().toString());
                if (gaugeValue > PrimexModel.MAXIMUM_POSSIBLE_GAUGE) {
                    gaugeValue /= 1000;
                }
                String threeDecimalsPlus = new DecimalFormat("#.000#").format(gaugeValue);
                mEdit_orderedGauge.setText(threeDecimalsPlus);
            } else if (validGroup == mContainer_diameterInputs2) {
                if (mEdit_materialDensity.getText().length() == 0) {
                    mEdit_materialDensity.setError(getString(R.string.error_empty_field));
                    validInputs = false;
                }

                if (mEdit_width.getText().length() == 0) {
                    mEdit_width.setError(getString(R.string.error_empty_field));
                    validInputs = false;
                }

            } else {
                validInputs = false;
            } 
        }
        return validInputs;
    }
    
    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

}
