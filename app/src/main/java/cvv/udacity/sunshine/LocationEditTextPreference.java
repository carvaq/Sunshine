package cvv.udacity.sunshine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Caro Vaquero
 * Date: 21.09.2016
 * Project: Sunshine
 */

public class LocationEditTextPreference extends EditTextPreference {

    private static final int DEFAULT_MINIMUM_LOCATION_LENGTH = 2;

    private int mMinLength;

    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LocationEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.LocationEditTextPreference, 0, 0);
        try {
            mMinLength = a.getInteger(a.getIndex(R.styleable.LocationEditTextPreference_minLength),
                    DEFAULT_MINIMUM_LOCATION_LENGTH);
        } finally {
            a.recycle();
        }
    }

    public int getMinLength() {
        return mMinLength;
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        EditText editText = getEditText();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Dialog dialog = getDialog();
                if (dialog instanceof AlertDialog) {
                    Button button = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    button.setEnabled(editable.length() >= mMinLength);
                }
            }
        });

    }
}
