package com.zhoorta.android.pdf.signer.ui.config;

import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.zhoorta.android.pdf.signer.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class SignatureConfigFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    SharedPreferences config;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_signature_config, container, false);


        config = this.getActivity().getSharedPreferences("SignerConfig", 0); // 0 - for private mode

        Button button = root.findViewById(R.id.buttonSaveSignatureConfig);
        final EditText textXPos = root.findViewById(R.id.editXPos);
        final EditText textYPos = root.findViewById(R.id.editYPos);
        final EditText textWidth = root.findViewById(R.id.editWidth);
        final EditText textHeight = root.findViewById(R.id.editHeight);
        final EditText textPage = root.findViewById(R.id.editPage);


        textXPos.setText(config.getString("xpos", "110"));
        textYPos.setText(config.getString("ypos", "170"));
        textWidth.setText(config.getString("width", "200"));
        textHeight.setText(config.getString("height", "50"));
        textPage.setText(config.getString("page", "1"));

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                SharedPreferences.Editor editor = config.edit();
                editor.putString("xpos", textXPos.getText().toString());
                editor.putString("ypos", textYPos.getText().toString());
                editor.putString("width", textWidth.getText().toString());
                editor.putString("height", textHeight.getText().toString());
                editor.putString("page", textPage.getText().toString());
                editor.commit();

                //Intent returnIntent = new Intent();
                //setResult(Activity.RESULT_OK,returnIntent);
                getActivity().finish();


            }
        });

        return root;
    }
}