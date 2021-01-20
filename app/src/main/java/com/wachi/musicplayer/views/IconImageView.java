package com.wachi.musicplayer.views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.wachi.musicplayer.R;

public class IconImageView extends AppCompatImageView {
    public IconImageView(Context context) {
        super(context);
        init(context);
    }

    public IconImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IconImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (context == null) return;
        setColorFilter(ATHUtil.resolveColor(context, R.attr.iconColor), PorterDuff.Mode.SRC_IN);
    }
}
