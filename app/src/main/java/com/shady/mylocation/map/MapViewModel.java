package com.shady.mylocation.map;

import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

public class MapViewModel extends ViewModel {
    public ObservableField<String> address = new ObservableField<>();
    public ObservableField<Boolean> addressEnable = new ObservableField<>();

    public void onAddressChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            addressEnable.set(true);
        } else {
            addressEnable.set(false);
        }
    }
}
