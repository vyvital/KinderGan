
package com.devsoul.dima.kindergarten.fabbo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.devsoul.dima.kindergarten.R;


/**
 * Custom FabOptions buttons ({@link ImageView}) container, enables runtime view insertion
 */

public class FabOptionsButtonContainer extends LinearLayout {

    public FabOptionsButtonContainer(Context context) {
        this(context, null);
    }

    public FabOptionsButtonContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FabOptionsButtonContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AppCompatImageView addButton(Context context, int buttonId, CharSequence title, Drawable drawableIcon) {
        return addButton(context, buttonId, title, drawableIcon, null);
    }

    public AppCompatImageView addButton(Context context, int buttonId, CharSequence title, Drawable drawableIcon, Integer index) {
        AppCompatImageView fabOptionButton =
                (AppCompatImageView) LayoutInflater.from(context).inflate(R.layout.faboptions_button, this, false);

        fabOptionButton.setImageDrawable(drawableIcon);
        fabOptionButton.setContentDescription(title);
        fabOptionButton.setId(buttonId);

        if (index == null) {
            addView(fabOptionButton);
        } else {
            addView(fabOptionButton, index);
        }
        return fabOptionButton;
    }

    public View addSeparator(Context context) {
        View separator = LayoutInflater.from(context).inflate(R.layout.faboptions_separator, this, false);
        addView(separator, (getChildCount() / 2));
        return separator;
    }
}
